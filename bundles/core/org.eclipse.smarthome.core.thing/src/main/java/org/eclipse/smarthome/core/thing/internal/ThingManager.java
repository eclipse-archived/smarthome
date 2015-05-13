/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.smarthome.core.events.AbstractEventSubscriber;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.thing.link.ItemThingLinkRegistry;
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
 * mediates the communication between the {@link Thing} and the {@link ThingHandler} from the binding. Therefore it
 * tracks {@link ThingHandlerFactory}s and calls {@link ThingHandlerFactory#registerHandler(Thing)} for each thing, that
 * was
 * added to the {@link ThingRegistry}. In addition the {@link ThingManager} acts
 * as an {@link EventHandler} and subscribes to smarthome update and command
 * events.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Michael Grammling - Added dynamic configuration update
 * @author Stefan Bußweiler - Added new thing status handling 
 */
public class ThingManager extends AbstractEventSubscriber implements ThingTracker {

    private final class ThingHandlerTracker extends ServiceTracker<ThingHandler, ThingHandler> {

        public ThingHandlerTracker(BundleContext context) {
            super(context, ThingHandler.class.getName(), null);
        }

        @Override
        public ThingHandler addingService(ServiceReference<ThingHandler> reference) {
            ThingUID thingId = getThingId(reference);

            logger.debug("Thing handler for thing '{}' added.", thingId);

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
            logger.debug("Thing handler for thing '{}' removed.", thingId);
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

    private ItemThingLinkRegistry itemThingLinkRegistry;

    private List<ThingHandlerFactory> thingHandlerFactories = new CopyOnWriteArrayList<>();

    private Map<ThingUID, ThingHandler> thingHandlers = new ConcurrentHashMap<>();

    private ThingHandlerTracker thingHandlerTracker;

    private ThingHandlerCallback thingHandlerCallback = new ThingHandlerCallback() {

        @Override
        public void stateUpdated(ChannelUID channelUID, State state) {
            Set<String> items = itemChannelLinkRegistry.getLinkedItems(channelUID);
            for (String item : items) {
                eventPublisher.postUpdate(item, state, channelUID.toString());
            }
        }
        
        @Override
        public void postCommand(ChannelUID channelUID, Command command) {
            Set<String> items = itemChannelLinkRegistry.getLinkedItems(channelUID);
            for (String item : items) {
                eventPublisher.postCommand(item, command, channelUID.toString());
            }
        }

        @Override
        public void statusUpdated(Thing thing, ThingStatusInfo thingStatus) {
            thing.setStatusInfo(thingStatus);
            logger.debug("Thing status of thing {} changed: {}", thing.getUID(), thingStatus.toString());
            // TODO: send event

            if (thing instanceof Bridge) {
                Bridge bridge = (Bridge) thing;
                for (Thing bridgeThing : bridge.getThings()) {
                    if (thingStatus.getStatus() == ThingStatus.ONLINE) {
                        ThingStatusInfo statusInfo = ThingStatusInfoBuilder.create(ThingStatus.ONLINE).build();
                        bridgeThing.setStatusInfo(statusInfo);
                        // TODO: send event
                    } else if (thingStatus.getStatus() == ThingStatus.OFFLINE) {
                        ThingStatusInfo statusInfo = ThingStatusInfoBuilder.create(ThingStatus.OFFLINE,
                                ThingStatusDetail.BRIDGE_OFFLINE).build();
                        bridgeThing.setStatusInfo(statusInfo);
                        // TODO: send event
                    }
                }
            }
        }

        @Override
        public void thingUpdated(Thing thing) {
            thingUpdatedLock.add(thing.getUID());
            managedThingProvider.update(thing);
            thingUpdatedLock.remove(thing.getUID());
        }

    };

    private ItemRegistry itemRegistry;

    private ThingRegistryImpl thingRegistry;
    
    private ManagedThingProvider managedThingProvider;

    private Set<Thing> things = new CopyOnWriteArraySet<>();

    private ThingLinkManager thingLinkManager;

    private Set<ThingUID> registerHandlerLock = new HashSet<>();
    private Set<ThingUID> thingUpdatedLock = new HashSet<>();

    /**
     * Method is called when a {@link ThingHandler} is added.
     *
     * @param thing
     *            thing
     * @param thingHandler
     *            thing handler
     */
    public void handlerAdded(Thing thing, ThingHandler thingHandler) {
        logger.debug("Assigning handler for thing '{}'.", thing.getUID());
        thingHandler.setCallback(this.thingHandlerCallback);
        thing.setHandler(thingHandler);
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
        logger.debug("Removing handler and setting status to OFFLINE.", thing.getUID());
        thing.setHandler(null);
        ThingStatusInfo statusInfo = buildStatusInfo(ThingStatus.UNINITIALIZED, ThingStatusDetail.HANDLER_MISSING_ERROR);
        thing.setStatusInfo(statusInfo);
        thingHandler.setCallback(null);
    }

    @Override
    public void receiveCommand(String itemName, Command command, String source) {
        Set<ChannelUID> boundChannels = this.itemChannelLinkRegistry.getBoundChannels(itemName);
        for (ChannelUID channelUID : boundChannels) {
            // make sure a command event is not sent back to its source
            if (!channelUID.toString().equals(source)) {
                Thing thing = getThing(channelUID.getThingUID());
                if (thing != null) {
                    ThingHandler handler = thing.getHandler();
                    if (handler != null) {
                        logger.debug("Delegating command '{}' for item '{}' to handler for channel '{}'", command,
                                itemName, channelUID);
                        try {
                            handler.handleCommand(channelUID, command);
                        } catch (Exception ex) {
                            logger.error("Exception occured while calling handler: " + ex.getMessage(), ex);
                        }
                    } else {
                        logger.warn("Cannot delegate command '{}' for item '{}' to handler for channel '{}', "
                                + "because no handler is assigned. Maybe the binding is not installed or not "
                                + "propertly initialized.", command, itemName, channelUID);
                    }
                } else {
                    logger.warn("Cannot delegate command '{}' for item '{}' to handler for channel '{}', "
                            + "because no thing with the UID '{}' could be found.", command, itemName, channelUID,
                            channelUID.getThingUID());
                }
            }
        }
    }

    @Override
    public void receiveUpdate(String itemName, State newState, String source) {
        Set<ChannelUID> boundChannels = this.itemChannelLinkRegistry.getBoundChannels(itemName);
        for (ChannelUID channelUID : boundChannels) {
            // make sure an update event is not sent back to its source
            if (!channelUID.toString().equals(source)) {
                Thing thing = getThing(channelUID.getThingUID());
                if (thing != null) {
                    ThingHandler handler = thing.getHandler();
                    if (handler != null) {
                        logger.debug("Delegating update '{}' for item '{}' to handler for channel '{}'", newState,
                                itemName, channelUID);
                        try {
                            handler.handleUpdate(channelUID, newState);
                        } catch (Exception ex) {
                            logger.error("Exception occured while calling handler: " + ex.getMessage(), ex);
                        }
                    } else {
                        logger.warn("Cannot delegate update '{}' for item '{}' to handler for channel '{}', "
                                + "because no handler is assigned. Maybe the binding is not installed or not "
                                + "propertly initialized.", newState, itemName, channelUID);
                    }

                } else {
                    logger.warn("Cannot delegate update '{}' for item '{}' to handler for channel '{}', "
                            + "because no thing with the UID '{}' could be found.", newState, itemName, channelUID,
                            channelUID.getThingUID());
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
            registerHandler(thing);
        } else {
            logger.debug("Handler for thing '{}' already exists.", thing.getUID());
            handlerAdded(thing, thingHandler);
        }
        this.thingLinkManager.thingAdded(thing);
    }

    @Override
    public void thingRemoved(Thing thing, ThingTrackerEvent thingTrackerEvent) {
        this.thingLinkManager.thingRemoved(thing);
        if (thingTrackerEvent == ThingTrackerEvent.THING_REMOVED) {
            ThingUID thingId = thing.getUID();
            ThingHandler thingHandler = thingHandlers.get(thingId);
            if (thingHandler != null) {
                ThingHandlerFactory thingHandlerFactory = findThingHandlerFactory(thing);
                if (thingHandlerFactory != null) {
                    unregisterHandler(thing, thingHandlerFactory);
                    thingHandlerFactory.removeThing(thing.getUID());
                } else {
                    logger.warn("Cannot unregister handler. No handler factory for thing '{}' found.", thing.getUID());
                }
            }
        }
        logger.debug("Thing '{}' is no longer tracked by ThingManager.", thing.getUID());
        this.things.remove(thing);
    }

    @Override
    public void thingUpdated(Thing thing, ThingTrackerEvent thingTrackerEvent) {

        ThingUID thingUID = thing.getUID();
        Thing oldThing = getThing(thingUID);

        if (oldThing != thing) {
            this.things.remove(oldThing);
            this.things.add(thing);
        }

        thingLinkManager.thingUpdated(thing);

        ThingHandler thingHandler = thingHandlers.get(thingUID);
        if (thingHandler != null) {
            try {
                if (oldThing != thing) {
                    thing.setHandler(thingHandler);
                }
                // prevent infinite loops by not informing handler about self-initiated update
                if (!thingUpdatedLock.contains(thingUID)) {
                    thingHandler.thingUpdated(thing);
                }
            } catch (Exception ex) {
                logger.error("Cannot send Thing updated event to ThingHandler '" + thingHandler + "'!", ex);
            }
        } else {
            registerHandler(thing);
        }

        if (oldThing != thing) {
            oldThing.setHandler(null);
        }
    }

    private void registerHandler(Thing thing) {
        ThingUID thingUID = thing.getUID();

        // this check is needed to prevent infinite loops while a handler is initialized
        if (!registerHandlerLock.contains(thingUID)) {
            registerHandlerLock.add(thingUID);
            ThingHandlerFactory thingHandlerFactory = findThingHandlerFactory(thing);
            if (thingHandlerFactory != null) {
                registerHandler(thing, thingHandlerFactory);
            } else {
                logger.debug("Not registering a handler at this point since no handler factory for thing '{}' found.",
                        thingUID);
            }
            registerHandlerLock.remove(thingUID);
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

    private void registerHandler(Thing thing, ThingHandlerFactory thingHandlerFactory) {
        logger.debug("Creating handler for thing '{}'.", thing.getUID());
        try {
            ThingStatusInfo statusInfo = buildStatusInfo(ThingStatus.INITIALIZING, ThingStatusDetail.NONE);
            thing.setStatusInfo(statusInfo);
            thingHandlerFactory.registerHandler(thing, this.thingHandlerCallback);
        } catch (Exception ex) {
            ThingStatusInfo statusInfo = buildStatusInfo(ThingStatus.UNINITIALIZED,
                    ThingStatusDetail.HANDLER_INITIALIZING_ERROR, ex.getMessage());
            thing.setStatusInfo(statusInfo);
            logger.error("Exception occured while calling handler: " + ex.getMessage(), ex);
        }
    }

    private void unregisterHandler(Thing thing, ThingHandlerFactory thingHandlerFactory) {
        logger.debug("Removing handler for thing '{}'.", thing.getUID());
        try {
            thingHandlerFactory.unregisterHandler(thing);
        } catch (Exception ex) {
            logger.error("Exception occured while calling handler: " + ex.getMessage(), ex);
        }
    }

    protected void activate(ComponentContext componentContext) {
        this.thingLinkManager = new ThingLinkManager(itemRegistry, thingRegistry, itemChannelLinkRegistry,
                itemThingLinkRegistry);
        this.thingLinkManager.startListening();
        this.thingRegistry.addThingTracker(this);
        this.bundleContext = componentContext.getBundleContext();
        this.thingHandlerTracker = new ThingHandlerTracker(this.bundleContext);
        this.thingHandlerTracker.open();
    }

    protected void addThingHandlerFactory(ThingHandlerFactory thingHandlerFactory) {
        logger.debug("Thing handler factory '{}' added", thingHandlerFactory.getClass().getSimpleName());

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
        this.thingRegistry.removeThingTracker(this);
        this.thingLinkManager.stopListening();
    }

    protected void removeThingHandlerFactory(ThingHandlerFactory thingHandlerFactory) {
        logger.debug("Thing handler factory '{}' removed", thingHandlerFactory.getClass().getSimpleName());

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
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    protected void unsetItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = null;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = null;
    }

    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    protected void setItemThingLinkRegistry(ItemThingLinkRegistry itemThingLinkRegistry) {
        this.itemThingLinkRegistry = itemThingLinkRegistry;
    }

    protected void unsetItemThingLinkRegistry(ItemThingLinkRegistry itemThingLinkRegistry) {
        this.itemThingLinkRegistry = null;
    }
    
    protected void setManagedThingProvider(ManagedThingProvider managedThingProvider) {
        this.managedThingProvider = managedThingProvider;
    }
    
    protected void unsetManagedThingProvider(ManagedThingProvider managedThingProvider) {
        this.managedThingProvider = null;
    }
    
    private ThingStatusInfo buildStatusInfo(ThingStatus thingStatus, ThingStatusDetail thingStatusDetail, String description) {
        ThingStatusInfoBuilder statusInfoBuilder = ThingStatusInfoBuilder.create(thingStatus, thingStatusDetail);
        statusInfoBuilder.withDescription(description);
        return statusInfoBuilder.build();
    }

    private ThingStatusInfo buildStatusInfo(ThingStatus thingStatus, ThingStatusDetail thingStatusDetail) {
        return buildStatusInfo(thingStatus, thingStatusDetail, null);
    }

}
