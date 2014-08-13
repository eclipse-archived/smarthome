/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.smarthome.core.events.AbstractEventSubscriber;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ThingManager} tracks all things in the {@link ThingRegistry} and
 * mediates the communication between the {@link Thing} and the
 * {@link ThingHandler} from the binding. Therefore it tracks
 * {@link ThingHandlerFactory}s and calls
 * {@link ThingHandlerFactory#registerHandler(Thing)} for each thing, that was
 * added to the {@link ThingRegistry}. In addition the {@link ThingManager} acts
 * as an {@link EventHandler} and subscribes to smarthome update and command
 * events.
 * 
 * @author Dennis Nobel - Initial contribution
 * @author Michael Grammling - Added dynamic configuration update
 */
public class ThingManager extends AbstractEventSubscriber implements ThingTracker {

    private final class ThingHandlerTracker extends ServiceTracker<ThingHandler, ThingHandler> {

        public ThingHandlerTracker(BundleContext context) {
            super(context, ThingHandler.class.getName(), null);
        }

        @Override
        public ThingHandler addingService(ServiceReference<ThingHandler> reference) {
            ThingUID thingId = getThingId(reference);

            logger.warn("Thing handler for thing '{}' added.", thingId);

            ThingHandler thingHandler = bundleContext.getService(reference);
            Thing thing = getThing(thingId);

            if (thing != null) {
                handlerAdded(thing, thingHandler);
            } else {
                logger.warn("Found handler for non-existing thing '{}'.", thingId);
            }

            thingHandlers.put(thingId, thingHandler);
            return thingHandler;
        }

        @Override
        public void removedService(ServiceReference<ThingHandler> reference, ThingHandler service) {
            ThingUID thingId = getThingId(reference);
            logger.warn("Thing handler for thing '{}' removed.", thingId);
            Thing thing = getThing(thingId);
            if (thing != null) {
                handlerRemoved(thing, service);
            }
            thingHandlers.remove(getThingId(reference));
        }

        private ThingUID getThingId(ServiceReference<ThingHandler> reference) {
            return (ThingUID) reference.getProperty("thing.id");
        }

    }

    private Logger logger = LoggerFactory.getLogger(ThingManager.class);

    private BundleContext bundleContext;

    private EventPublisher eventPublisher;

    private ItemChannelLinkRegistry itemChannelLinkRegistry;

    private List<ThingHandlerFactory> thingHandlerFactories = new CopyOnWriteArrayList<>();

    private Map<ThingUID, ThingHandler> thingHandlers = new ConcurrentHashMap<>();

    private ThingHandlerTracker thingHandlerTracker;

    private ThingListener thingListener = new ThingListener() {

        @Override
        public void channelUpdated(ChannelUID channelUID, State state) {
            String item = itemChannelLinkRegistry.getBoundItem(channelUID);
            if (item != null) {
                eventPublisher.postUpdate(item, state, channelUID.toString());
            }
        }

    };

    private ThingRegistryImpl thingRegistry;

    private Set<Thing> things = new CopyOnWriteArraySet<>();

    /**
     * Method is called when a {@link ThingHandler} is added.
     * 
     * @param thing
     *            thing
     * @param thingHandler
     *            thing handler
     */
    public void handlerAdded(Thing thing, ThingHandler thingHandler) {
        logger.info("Assigning handler and setting status to ONLINE.", thing.getUID());
        ((ThingImpl) thing).addThingListener(thingListener);
        thing.setHandler(thingHandler);
        thing.setStatus(ThingStatus.ONLINE);
    }

    /**
     * Method is called when a {@link ThingHandler} is removed.
     * 
     * @param thing
     *            thing
     * @param thingHandler
     *            thing handler
     */
    public void handlerRemoved(Thing thing, ThingHandler thingHandler) {
        logger.info("Removing handler and setting status to OFFLINE.", thing.getUID());
        ((ThingImpl) thing).removeThingListener(thingListener);
        thing.setHandler(null);
        thing.setStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void receiveCommand(String itemName, Command command) {
        for (Thing thing : this.things) {
            List<Channel> channels = thing.getChannels();
            for (Channel channel : channels) {
                if (isLinked(itemName, channel)) {
                    logger.info(
                            "Delegating command '{}' for item '{}' to handler for channel '{}'",
                            command, itemName, channel.getUID());
                    try {
                        thing.getHandler().handleCommand(channel.getUID(), command);
                    } catch (Exception ex) {
                        logger.error("Exception occured while calling handler: " + ex.getMessage(),
                                ex);
                    }
                }
            }
        }
    }

    @Override
    public void receiveUpdate(String itemName, State newState, String source) {
        for (Thing thing : this.things) {
            List<Channel> channels = thing.getChannels();
            for (Channel channel : channels) {
                if (isLinked(itemName, channel) && !channel.getUID().toString().equals(source)) {
                    ThingHandler handler = thing.getHandler();
                    if (handler != null) {
                        logger.info(
                                "Delegating update '{}' for item '{}' to handler for channel '{}'",
                                newState, itemName, channel.getUID());
                        try {
                            handler.handleUpdate(channel.getUID(), newState);
                        } catch (Exception ex) {
                            logger.error(
                                    "Exception occured while calling handler: " + ex.getMessage(),
                                    ex);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void thingAdded(Thing thing, ThingTrackerEvent thingTrackerEvent) {
        this.things.add(thing);
        logger.debug("Thing '{}' is tracked by ThingManager.", thing.getUID());
        ThingHandler thingHandler = thingHandlers.get(thing.getUID());
        if (thingHandler == null) {
            ThingHandlerFactory thingHandlerFactory = findThingHandlerFactory(thing);
            if (thingHandlerFactory != null) {
                registerHandler(thing, thingHandlerFactory);
            } else {
                logger.info("Cannot register handler. No handler factory for thing '{}' found.",
                        thing.getUID());
            }
        } else {
            logger.debug("Handler for thing '{}' already exists.", thing.getUID());
            handlerAdded(thing, thingHandler);
        }
    }

    @Override
    public void thingRemoved(Thing thing, ThingTrackerEvent thingTrackerEvent) {
        if (thingTrackerEvent == ThingTrackerEvent.THING_REMOVED) {
            ThingUID thingId = thing.getUID();
            ThingHandler thingHandler = thingHandlers.get(thingId);
            if (thingHandler != null) {
                ThingHandlerFactory thingHandlerFactory = findThingHandlerFactory(thing);
                if (thingHandlerFactory != null) {
                    unregisterHandler(thing, thingHandlerFactory);
                } else {
                    logger.info(
                            "Cannot unregister handler. No handler factory for thing '{}' found.",
                            thing.getUID());
                }
            }
        }
        logger.info("Thing '{}' is no longer tracked by ThingManager.", thing.getUID());
        this.things.remove(thing);
    }

    @Override
    public void thingUpdated(Thing thing, ThingTrackerEvent thingTrackerEvent) {
        if (thingTrackerEvent == ThingTrackerEvent.THING_UPDATED) {
            ThingUID thingId = thing.getUID();
            ThingHandler thingHandler = thingHandlers.get(thingId);
            if (thingHandler != null) {
                try {
                    thingHandler.thingUpdated(thing);
                } catch (Exception ex) {
                    logger.error("Cannot send Thing updated event to ThingHandler '"
                            + thingHandler + "'!", ex);
                }
            }
        }
    }

    private ThingHandlerFactory findThingHandlerFactory(Thing thing) {
        for (ThingHandlerFactory factory : thingHandlerFactories) {
            if (factory.supportsThingType(thing.getThingTypeUID())) {
                return factory;
            }
        }
        return null;
    }

    private Thing getThing(ThingUID id) {
        for (Thing thing : this.things) {
            if (thing.getUID().equals(id)) {
                return thing;
            }
        }

        return null;
    }

    private boolean isLinked(String itemName, Channel channel) {
        return itemChannelLinkRegistry.isLinked(itemName, channel.getUID());
    }

    private void registerHandler(Thing thing, ThingHandlerFactory thingHandlerFactory) {
        logger.info("Creating handler for thing '{}'.", thing.getUID());
        try {
            thingHandlerFactory.registerHandler(thing);
        } catch (Exception ex) {
            logger.error("Exception occured while calling handler: " + ex.getMessage(), ex);
        }
    }

    private void unregisterHandler(Thing thing, ThingHandlerFactory thingHandlerFactory) {
        logger.info("Removing handler for thing '{}'.", thing.getUID());
        try {
            thingHandlerFactory.unregisterHandler(thing);
        } catch (Exception ex) {
            logger.error("Exception occured while calling handler: " + ex.getMessage(), ex);
        }
    }

    protected void activate(ComponentContext componentContext) {
        this.bundleContext = componentContext.getBundleContext();
        this.thingHandlerTracker = new ThingHandlerTracker(this.bundleContext);
        this.thingHandlerTracker.open();
    }

    protected void addThingHandlerFactory(ThingHandlerFactory thingHandlerFactory) {
        logger.debug("Thing handler factory '{}' added",
                thingHandlerFactory.getClass().getSimpleName());

        thingHandlerFactories.add(thingHandlerFactory);

        for (Thing thing : this.things) {
            if (thingHandlerFactory.supportsThingType(thing.getThingTypeUID())) {
                ThingUID thingId = thing.getUID();

                ThingHandler thingHandler = thingHandlers.get(thingId);
                if (thingHandler == null) {
                    registerHandler(thing, thingHandlerFactory);
                } else {
                    logger.warn("Thing handler for thing '{}' already exists.", thingId);
                }
            }
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        this.thingHandlerTracker.close();
    }

    protected void removeThingHandlerFactory(ThingHandlerFactory thingHandlerFactory) {
        logger.info("Thing handler factory '{}' removed",
                thingHandlerFactory.getClass().getSimpleName());

        thingHandlerFactories.remove(thingHandlerFactory);
    }

    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void setItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
    }

    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = (ThingRegistryImpl) thingRegistry;
        this.thingRegistry.addThingTracker(this);
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    protected void unsetItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = null;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry.removeThingTracker(this);
        this.thingRegistry = null;
    }

}
