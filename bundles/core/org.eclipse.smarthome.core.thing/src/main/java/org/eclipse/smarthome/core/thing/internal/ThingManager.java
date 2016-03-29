/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal;

import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.common.SafeMethodCaller;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.AbstractItemEventSubscriber;
import org.eclipse.smarthome.core.items.events.ItemCommandEvent;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.items.events.ItemStateEvent;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.eclipse.smarthome.core.thing.events.ThingEventFactory;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.thing.link.ItemThingLinkRegistry;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.eclipse.smarthome.core.thing.type.TypeResolver;
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
 * @author Stefan Bußweiler - Added new thing status handling, migration to new event mechanism,
 *         refactorings thing life cycle
 * @author Simon Kaufmann - Added remove handling
 */
public class ThingManager extends AbstractItemEventSubscriber implements ThingTracker {

    private static final String FORCEREMOVE_THREADPOOL_NAME = "forceRemove";

    private static final String THING_MANAGER_THREADPOOL_NAME = "thingManager";

    private final class ThingHandlerTracker extends ServiceTracker<ThingHandler, ThingHandler> {

        public ThingHandlerTracker(BundleContext context) {
            super(context, ThingHandler.class.getName(), null);
        }

        @Override
        public ThingHandler addingService(ServiceReference<ThingHandler> reference) {
            ThingUID thingId = getThingUID(reference);

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
            ThingUID thingUID = getThingUID(reference);
            logger.debug("Thing handler for thing '{}' removed.", thingUID);
            Thing thing = getThing(thingUID);
            if (thing != null) {
                handlerRemoved(thing, service);
            }
            thingHandlers.remove(thingUID);
        }

        private ThingUID getThingUID(ServiceReference<ThingHandler> reference) {
            return (ThingUID) reference.getProperty(ThingHandler.SERVICE_PROPERTY_THING_ID);
        }

    }

    private Logger logger = LoggerFactory.getLogger(ThingManager.class);

    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("thingManager");

    private BundleContext bundleContext;

    private EventPublisher eventPublisher;

    private ItemChannelLinkRegistry itemChannelLinkRegistry;

    private ItemThingLinkRegistry itemThingLinkRegistry;

    private List<ThingHandlerFactory> thingHandlerFactories = new CopyOnWriteArrayList<>();

    private Map<ThingUID, ThingHandler> thingHandlers = new ConcurrentHashMap<>();

    private ThingHandlerTracker thingHandlerTracker;

    private ThingTypeRegistry thingTypeRegistry;

    private ThingHandlerCallback thingHandlerCallback = new ThingHandlerCallback() {

        @Override
        public void stateUpdated(ChannelUID channelUID, State state) {
            Set<String> items = itemChannelLinkRegistry.getLinkedItems(channelUID);
            for (String item : items) {
                eventPublisher.post(ItemEventFactory.createStateEvent(item, state, channelUID.toString()));
            }
        }

        @Override
        public void postCommand(ChannelUID channelUID, Command command) {
            Set<String> items = itemChannelLinkRegistry.getLinkedItems(channelUID);
            for (String item : items) {
                eventPublisher.post(ItemEventFactory.createCommandEvent(item, command, channelUID.toString()));
            }
        }

        @Override
        public void statusUpdated(final Thing thing, ThingStatusInfo thingStatus) {
            // all provoked operations based on a status update should be executed asynchronously!

            if (ThingStatus.REMOVING.equals(thing.getStatus())
                    && !ThingStatus.REMOVED.equals(thingStatus.getStatus())) {
                // only allow REMOVING -> REMOVED transition and ignore all other state changes
                return;
            }
            ThingStatusInfo oldStatusInfo = thing.getStatusInfo();

            // update thing status and send event with new status
            setThingStatus(thing, thingStatus);

            // if thing is a bridge:
            if (thing instanceof Bridge) {
                Bridge bridge = (Bridge) thing;
                // notify all child-things about bridge initialization
                if (oldStatusInfo.getStatus() == ThingStatus.INITIALIZING && isInitialized(thing)) {
                    notifyThingsAboutBridgeInitialization(bridge);
                }
                // update status of child-things
                updateThingStatus(thingStatus, bridge);
                // notify child-things about bridge status change, if bridge status is ONLINE/OFFLINE
                if(!oldStatusInfo.equals(thingStatus)) {
                    notifyThingsAboutBridgeStatusChange(thingStatus, bridge);
                }
            }
            // if thing has a bridge: determine if bridge has been initialized and notify thing handler about it
            if (thing.getBridgeUID() != null && oldStatusInfo.getStatus() == ThingStatus.INITIALIZING
                    && isInitialized(thing)) {
                notifyThingAboutBridgeInitialization(thing);
            }
            // notify thing about its removal
            if (ThingStatus.REMOVING.equals(thing.getStatus())) {
                notifyThingAboutRemoval(thing);
            }
            // notify thing registry about thing removal
            else if (ThingStatus.REMOVED.equals(thing.getStatus())) {
                notifyRegistryAboutForceRemove(thing);
            }
        }

        @Override
        public void thingUpdated(final Thing thing) {
            thingUpdatedLock.add(thing.getUID());
            Thing ret = AccessController.doPrivileged(new PrivilegedAction<Thing>() {

                @Override
                public Thing run() {
                    return managedThingProvider.update(thing);
                }

            });
            thingUpdatedLock.remove(thing.getUID());
            if (ret == null) {
                throw new IllegalStateException(
                        MessageFormat.format("Could not update thing {0}. Most likely because it is read-only.",
                                thing.getUID().getAsString()));
            }
        }

        @Override
        public void configurationUpdated(Thing thing) {
            initializeHandler(thing);
        }

        @Override
        public void changeThingType(final Thing thing, final ThingTypeUID thingTypeUID,
                final Configuration configuration) {
            scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    ThingUID thingUID = thing.getUID();
                    ThingType thingType = thingTypeRegistry.getThingType(thingTypeUID);

                    // Remove the ThingHandler
                    final ThingHandlerFactory oldThingHandlerFactory = findThingHandlerFactory(thing.getThingTypeUID());
                    if (oldThingHandlerFactory != null) {
                        unregisterHandler(thing, oldThingHandlerFactory);
                    }

                    // Set the new channels
                    List<Channel> channels = ThingFactoryHelper.createChannels(thingType, thingUID,
                            configDescriptionRegistry);
                    ((ThingImpl) thing).setChannels(channels);

                    // Set the given configuration
                    ThingFactoryHelper.applyDefaultConfiguration(configuration, thingType, configDescriptionRegistry);
                    ((ThingImpl) thing).setConfiguration(configuration);

                    // Change the ThingType
                    ((ThingImpl) thing).setThingTypeUID(thingTypeUID);

                    // Register the new Handler - ThingManager.updateThing() is going to take care of that
                    thingRegistry.update(thing);

                    logger.debug("Changed ThingType of Thing {} to {}. New ThingHandler is {}.",
                            thing.getUID().toString(), thing.getThingTypeUID(), thing.getHandler().toString());
                }
            }, 0, TimeUnit.MILLISECONDS);
        }

    };

    private ItemRegistry itemRegistry;

    private ThingRegistryImpl thingRegistry;

    private ConfigDescriptionRegistry configDescriptionRegistry;

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
        initializeHandler(thing);
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
        logger.debug("Unassigning handler for thing '{}' and setting status to UNINITIALIZED.", thing.getUID());
        thing.setHandler(null);
        ThingStatusInfo statusInfo = buildStatusInfo(ThingStatus.UNINITIALIZED,
                ThingStatusDetail.HANDLER_MISSING_ERROR);
        setThingStatus(thing, statusInfo);
        thingHandler.setCallback(null);
        disposeHandler(thing, thingHandler);
        if (thing instanceof Bridge) {
            notifyThingsAboutBridgeDisposal((Bridge) thing);
        }
    }

    @Override
    protected void receiveCommand(ItemCommandEvent commandEvent) {
        String itemName = commandEvent.getItemName();
        final Command command = commandEvent.getItemCommand();
        Set<ChannelUID> boundChannels = this.itemChannelLinkRegistry.getBoundChannels(itemName);
        for (final ChannelUID channelUID : boundChannels) {
            // make sure a command event is not sent back to its source
            if (!channelUID.toString().equals(commandEvent.getSource())) {
                Thing thing = getThing(channelUID.getThingUID());
                if (thing != null) {
                    final ThingHandler handler = thing.getHandler();
                    if (handler != null) {
                        if (isInitialized(thing)) {
                            logger.debug("Delegating command '{}' for item '{}' to handler for channel '{}'", command,
                                    itemName, channelUID);
                            try {
                                SafeMethodCaller.call(new SafeMethodCaller.ActionWithException<Void>() {
                                    @Override
                                    public Void call() throws Exception {
                                        handler.handleCommand(channelUID, command);
                                        return null;
                                    }
                                });
                            } catch (TimeoutException ex) {
                                logger.warn("Handler for thing '{}' takes more than {}ms for processing event",
                                        handler.getThing().getUID(), SafeMethodCaller.DEFAULT_TIMEOUT);
                            } catch (Exception ex) {
                                logger.error("Exception occured while calling handler: " + ex.getMessage(), ex);
                            }
                        } else {
                            logger.info(
                                    "Not delegating command '{}' for item '{}' to handler for channel '{}', "
                                            + "because thing is not initialized (must be in status ONLINE or OFFLINE).",
                                    command, itemName, channelUID);
                        }
                    } else {
                        logger.warn("Cannot delegate command '{}' for item '{}' to handler for channel '{}', "
                                + "because no handler is assigned. Maybe the binding is not installed or not "
                                + "propertly initialized.", command, itemName, channelUID);
                    }
                } else {
                    logger.warn(
                            "Cannot delegate command '{}' for item '{}' to handler for channel '{}', "
                                    + "because no thing with the UID '{}' could be found.",
                            command, itemName, channelUID, channelUID.getThingUID());
                }
            }
        }
    }

    @Override
    protected void receiveUpdate(ItemStateEvent updateEvent) {
        String itemName = updateEvent.getItemName();
        final State newState = updateEvent.getItemState();
        Set<ChannelUID> boundChannels = this.itemChannelLinkRegistry.getBoundChannels(itemName);
        for (final ChannelUID channelUID : boundChannels) {
            // make sure an update event is not sent back to its source
            if (!channelUID.toString().equals(updateEvent.getSource())) {
                Thing thing = getThing(channelUID.getThingUID());
                if (thing != null) {
                    final ThingHandler handler = thing.getHandler();
                    if (handler != null) {
                        if (isInitialized(thing)) {
                            logger.debug("Delegating update '{}' for item '{}' to handler for channel '{}'", newState,
                                    itemName, channelUID);
                            try {
                                SafeMethodCaller.call(new SafeMethodCaller.ActionWithException<Void>() {
                                    @Override
                                    public Void call() throws Exception {
                                        handler.handleUpdate(channelUID, newState);
                                        return null;
                                    }
                                });
                            } catch (TimeoutException ex) {
                                logger.warn("Handler for thing {} takes more than {}ms for processing event",
                                        handler.getThing().getUID(), SafeMethodCaller.DEFAULT_TIMEOUT);
                            } catch (Exception ex) {
                                logger.error("Exception occured while calling handler: " + ex.getMessage(), ex);
                            }
                        } else {
                            logger.info(
                                    "Not delegating update '{}' for item '{}' to handler for channel '{}', "
                                            + "because thing is not initialized (must be in status ONLINE or OFFLINE).",
                                    newState, itemName, channelUID);
                        }
                    } else {
                        logger.warn("Cannot delegate update '{}' for item '{}' to handler for channel '{}', "
                                + "because no handler is assigned. Maybe the binding is not installed or not "
                                + "propertly initialized.", newState, itemName, channelUID);
                    }
                } else {
                    logger.warn(
                            "Cannot delegate update '{}' for item '{}' to handler for channel '{}', "
                                    + "because no thing with the UID '{}' could be found.",
                            newState, itemName, channelUID, channelUID.getThingUID());
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
    public void thingRemoving(Thing thing, ThingTrackerEvent thingTrackerEvent) {
        thingHandlerCallback.statusUpdated(thing, ThingStatusInfoBuilder.create(ThingStatus.REMOVING).build());
    }

    @Override
    public void thingRemoved(final Thing thing, ThingTrackerEvent thingTrackerEvent) {
        this.thingLinkManager.thingRemoved(thing);

        ThingUID thingId = thing.getUID();
        ThingHandler thingHandler = thingHandlers.get(thingId);
        if (thingHandler != null) {
            final ThingHandlerFactory thingHandlerFactory = findThingHandlerFactory(thing.getThingTypeUID());
            if (thingHandlerFactory != null) {
                unregisterHandler(thing, thingHandlerFactory);
                if (thingTrackerEvent == ThingTrackerEvent.THING_REMOVED) {
                    SafeMethodCaller.call(new SafeMethodCaller.Action<Void>() {
                        @Override
                        public Void call() throws Exception {
                            thingHandlerFactory.removeThing(thing.getUID());
                            return null;
                        }
                    });
                }
            } else {
                logger.warn("Cannot unregister handler. No handler factory for thing '{}' found.", thing.getUID());
            }
        }

        logger.debug("Thing '{}' is no longer tracked by ThingManager.", thing.getUID());
        this.things.remove(thing);
    }

    @Override
    public void thingUpdated(final Thing thing, ThingTrackerEvent thingTrackerEvent) {

        ThingUID thingUID = thing.getUID();
        Thing oldThing = getThing(thingUID);

        if (oldThing != thing) {
            this.things.remove(oldThing);
            this.things.add(thing);
        }

        thingLinkManager.thingUpdated(thing);

        final ThingHandler thingHandler = thingHandlers.get(thingUID);
        if (thingHandler != null) {
            if (oldThing != thing) {
                thing.setHandler(thingHandler);
            }
            if (isInitialized(thing)) {
                try {
                    // prevent infinite loops by not informing handler about self-initiated update
                    if (!thingUpdatedLock.contains(thingUID)) {
                        SafeMethodCaller.call(new SafeMethodCaller.ActionWithException<Void>() {

                            @Override
                            public Void call() throws Exception {
                                thingHandler.thingUpdated(thing);
                                return null;
                            }
                        });
                    }
                } catch (Exception ex) {
                    logger.error("Exception occured while calling thing updated at ThingHandler '" + thingHandler + ": "
                            + ex.getMessage(), ex);
                }
            } else {
                logger.debug("Cannot notify handler about updated thing {}, because thing is not initialized "
                        + "(must be in status ONLINE or OFFLINE).", thing.getThingTypeUID());
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
            ThingHandlerFactory thingHandlerFactory = findThingHandlerFactory(thing.getThingTypeUID());
            if (thingHandlerFactory != null) {
                registerHandler(thing, thingHandlerFactory);
            } else {
                logger.debug("Not registering a handler at this point since no handler factory for thing '{}' found.",
                        thingUID);
            }
            registerHandlerLock.remove(thingUID);
        } else {
            logger.warn("Attempt to register a handler twice for thing {} at the same time will be ignored.", thingUID);
        }
    }

    private ThingHandlerFactory findThingHandlerFactory(ThingTypeUID thingTypeUID) {
        for (ThingHandlerFactory factory : thingHandlerFactories) {
            if (factory.supportsThingType(thingTypeUID)) {
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

    private void registerHandler(final Thing thing, final ThingHandlerFactory thingHandlerFactory) {
        logger.debug("Calling registerHandler handler for thing '{}' at '{}'.", thing.getUID(), thingHandlerFactory);
        try {
            SafeMethodCaller.call(new SafeMethodCaller.ActionWithException<Void>() {

                @Override
                public Void call() throws Exception {
                    thingHandlerFactory.registerHandler(thing, ThingManager.this.thingHandlerCallback);
                    return null;
                }
            });
        } catch (TimeoutException ex) {
            logger.warn("Registering handler for thing '{}' takes more than {}ms.", thing.getUID(),
                    SafeMethodCaller.DEFAULT_TIMEOUT);
        } catch (Exception ex) {
            ThingStatusInfo statusInfo = buildStatusInfo(ThingStatus.UNINITIALIZED,
                    ThingStatusDetail.HANDLER_REGISTERING_ERROR,
                    ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
            setThingStatus(thing, statusInfo);
            logger.error("Exception occured while calling thing handler factory '" + thingHandlerFactory + "': "
                    + ex.getMessage(), ex);
        }
    }

    private void initializeHandler(final Thing thing) {
        if (isInitializable(thing)) {
            ThingStatusInfo statusInfo = buildStatusInfo(ThingStatus.INITIALIZING, ThingStatusDetail.NONE);
            setThingStatus(thing, statusInfo);
            initializeHandler(thing, thing.getHandler());
        } else {
            logger.debug("Thing '{}' not initializable, check required configuration parameters.", thing.getUID());
            ThingStatusInfo statusInfo = buildStatusInfo(ThingStatus.UNINITIALIZED,
                    ThingStatusDetail.HANDLER_CONFIGURATION_PENDING);
            setThingStatus(thing, statusInfo);
        }
    }

    private boolean isInitializable(Thing thing) {
        // determines if all 'required' configuration parameters are available in the configuration

        ThingType thingType = TypeResolver.resolve(thing.getThingTypeUID());
        if (thingType == null) {
            return true;
        }

        ConfigDescription description = resolve(thingType.getConfigDescriptionURI(), null);
        if (description == null) {
            return true;
        }

        List<String> requiredParameters = getRequiredParameters(description);
        Map<String, Object> properties = thing.getConfiguration().getProperties();
        return properties.keySet().containsAll(requiredParameters);
    }

    private ConfigDescription resolve(URI configDescriptionURI, Locale locale) {
        if (configDescriptionURI == null) {
            return null;
        }

        return configDescriptionRegistry != null
                ? configDescriptionRegistry.getConfigDescription(configDescriptionURI, locale) : null;
    }

    private List<String> getRequiredParameters(ConfigDescription description) {
        List<String> requiredParameters = new ArrayList<>();
        for (ConfigDescriptionParameter param : description.getParameters()) {
            if (param.isRequired()) {
                requiredParameters.add(param.getName());
            }
        }
        return requiredParameters;
    }

    private void initializeHandler(final Thing thing, final ThingHandler thingHandler) {
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                logger.debug("Calling initialize handler for thing '{}' at '{}'.", thing.getUID(), thingHandler);
                try {
                    SafeMethodCaller.call(new SafeMethodCaller.ActionWithException<Void>() {
                        @Override
                        public Void call() throws Exception {
                            thingHandler.initialize();
                            return null;
                        }
                    });
                } catch (TimeoutException ex) {
                    logger.warn("Initializing handler for thing '{}' takes more than {}ms.", thing.getUID(),
                            SafeMethodCaller.DEFAULT_TIMEOUT);
                } catch (Exception ex) {
                    ThingStatusInfo statusInfo = buildStatusInfo(ThingStatus.UNINITIALIZED,
                            ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                            ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                    setThingStatus(thing, statusInfo);
                    logger.error("Exception occured while initializing handler of thing '" + thing.getUID() + "': "
                            + ex.getMessage(), ex);
                }
            }
        }, 0, TimeUnit.NANOSECONDS);
    }

    private boolean isInitialized(Thing thing) {
        return thing.getStatus() == ThingStatus.ONLINE || thing.getStatus() == ThingStatus.OFFLINE;
    }

    private void notifyThingsAboutBridgeInitialization(Bridge bridge) {
        for (Thing child : bridge.getThings()) {
            notifyThingAboutBridgeInitialization(bridge, child);
        }
    }

    private void notifyThingAboutBridgeInitialization(Thing thing) {
        if (thing.getBridgeUID() != null) {
            Thing bridge = thingRegistry.get(thing.getBridgeUID());
            if (bridge instanceof Bridge) {
                notifyThingAboutBridgeInitialization((Bridge) bridge, thing);
            }
        }
    }

    private void notifyThingAboutBridgeInitialization(final Bridge bridge, final Thing childThing) {
        if (childThing.getHandler() == null) {
            return;
        }

        ThreadPoolManager.getPool(THING_MANAGER_THREADPOOL_NAME).execute(new Runnable() {
            @Override
            public void run() {
                try {
                    childThing.getHandler().bridgeHandlerInitialized(bridge.getHandler(), bridge);
                } catch (Exception ex) {
                    logger.error("Exception occured during notification of thing '" + childThing.getUID()
                            + "' about bridge initialization at '" + childThing.getHandler() + "': " + ex.getMessage(),
                            ex);
                }
            }
        });
    }

    private void unregisterHandler(final Thing thing, final ThingHandlerFactory thingHandlerFactory) {
        logger.debug("Calling unregisterHandler handler for thing '{}' at '{}'.", thing.getUID(), thingHandlerFactory);
        try {
            SafeMethodCaller.call(new SafeMethodCaller.ActionWithException<Void>() {
                @Override
                public Void call() throws Exception {
                    thingHandlerFactory.unregisterHandler(thing);
                    return null;
                }
            });
        } catch (Exception ex) {
            logger.error("Exception occured while calling thing handler factory '" + thingHandlerFactory + "': "
                    + ex.getMessage(), ex);
        }
    }

    private void disposeHandler(final Thing thing, final ThingHandler thingHandler) {
        logger.debug("Calling dispose handler for thing '{}' at '{}'.", thing.getUID(), thingHandler);
        try {
            SafeMethodCaller.call(new SafeMethodCaller.ActionWithException<Void>() {
                @Override
                public Void call() throws Exception {
                    thingHandler.dispose();
                    return null;
                }
            });
        } catch (TimeoutException ex) {
            logger.warn("Disposing handler for thing '{}' takes more than {}ms.", thing.getUID(),
                    SafeMethodCaller.DEFAULT_TIMEOUT);
        } catch (Exception ex) {
            logger.error(
                    "Exception occured while disposing handler of thing '" + thing.getUID() + "': " + ex.getMessage(),
                    ex);
        }
    }

    private void notifyThingsAboutBridgeDisposal(final Bridge bridge) {
        // notify all child-thing-handlers about bridge disposal
        for (Thing childThing : bridge.getThings()) {
            notifyThingAboutBridgeDisposal(bridge, childThing);
        }
    }

    private void notifyThingAboutBridgeDisposal(final Bridge bridge, final Thing childThing) {
        if (childThing.getHandler() == null) {
            return;
        }

        ThreadPoolManager.getPool(THING_MANAGER_THREADPOOL_NAME).execute(new Runnable() {
            @Override
            public void run() {
                try {
                    childThing.getHandler().bridgeHandlerDisposed(bridge.getHandler(), bridge);
                } catch (Exception ex) {
                    logger.error("Exception occured during notification of thing '" + childThing.getUID()
                            + "' about bridge disposal at '" + childThing.getHandler() + "': " + ex.getMessage(), ex);
                }
            }
        });
    }

    private void updateThingStatus(final ThingStatusInfo thingStatus, final Bridge bridge) {
        for (final Thing bridgeChildThing : bridge.getThings()) {
            final ThingStatusInfo bridgeChildThingStatus = bridgeChildThing.getStatusInfo();
            if (bridgeChildThingStatus.getStatus() == ThingStatus.ONLINE
                    || bridgeChildThingStatus.getStatus() == ThingStatus.OFFLINE) {

                ThreadPoolManager.getPool(THING_MANAGER_THREADPOOL_NAME).execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (thingStatus.getStatus() == ThingStatus.ONLINE
                                    && bridgeChildThingStatus.getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE) {
                                ThingStatusInfo statusInfo = ThingStatusInfoBuilder
                                        .create(ThingStatus.OFFLINE, ThingStatusDetail.NONE).build();
                                setThingStatus(bridgeChildThing, statusInfo);
                            } else if (thingStatus.getStatus() == ThingStatus.OFFLINE) {
                                ThingStatusInfo statusInfo = ThingStatusInfoBuilder
                                        .create(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE).build();
                                setThingStatus(bridgeChildThing, statusInfo);
                            }
                        } catch (Exception ex) {
                            logger.error("Exception occured during status update of thing '" + bridgeChildThing.getUID()
                                    + "': " + ex.getMessage(), ex);
                        }
                    }
                });
            }
        }
    }
    
    private void notifyThingsAboutBridgeStatusChange(final ThingStatusInfo bridgeStatus, final Bridge bridge) {
        if (bridgeStatus.getStatus() == ThingStatus.ONLINE || bridgeStatus.getStatus() == ThingStatus.OFFLINE) {

            for (final Thing bridgeChildThing : bridge.getThings()) {
                ThreadPoolManager.getPool(THING_MANAGER_THREADPOOL_NAME).execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ThingHandler handler = bridgeChildThing.getHandler();
                            if (handler != null) {
                                handler.bridgeStatusChanged(bridgeStatus);
                            }
                        } catch (Exception ex) {
                            logger.error("Exception occured during notification about bridge status change on thing '"
                                    + bridgeChildThing.getUID() + "': " + ex.getMessage(), ex);
                        }
                    }
                });
            }
        }
    }

    private void notifyThingAboutRemoval(final Thing thing) {
        logger.trace("Asking handler of thing '{}' to handle its removal.", thing.getUID());

        ThreadPoolManager.getPool(THING_MANAGER_THREADPOOL_NAME).execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ThingHandler handler = thing.getHandler();
                    if (handler != null) {
                        handler.handleRemoval();
                        logger.trace("Handler of thing '{}' returned from handling its removal.", thing.getUID());
                    } else {
                        logger.trace("No handler of thing '{}' available, so deferring the removal call.",
                                thing.getUID());
                    }
                } catch (Exception ex) {
                    logger.error("The ThingHandler caused an exception while handling the removal of its thing", ex);
                }
            }
        });
    }

    private void notifyRegistryAboutForceRemove(final Thing thing) {
        logger.debug("Removal handling of thing '{}' completed. Going to remove it now.", thing.getUID());

        // call asynchronous to avoid deadlocks in thing handler
        ThreadPoolManager.getPool(FORCEREMOVE_THREADPOOL_NAME).execute(new Runnable() {
            @Override
            public void run() {
                try {
                    AccessController.doPrivileged(new PrivilegedAction<Void>() {
                        @Override
                        public Void run() {
                            thingRegistry.forceRemove(thing.getUID());
                            return null;
                        }
                    });
                } catch (IllegalStateException ex) {
                    logger.debug("Could not remove thing {}. Most likely because it is not managed.", thing.getUID(),
                            ex);
                } catch (Exception ex) {
                    logger.error(
                            "Could not remove thing {}, because an unknwon Exception occured. Most likely because it is not managed.",
                            thing.getUID(), ex);
                }
            }
        });
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
        this.thingRegistry.removeThingTracker(this);
        this.thingHandlerTracker.close();
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

    protected void setConfigDescriptionRegistry(ConfigDescriptionRegistry configDescriptionRegistry) {
        this.configDescriptionRegistry = configDescriptionRegistry;
    }

    protected void unsetConfigDescriptionRegistry(ConfigDescriptionRegistry configDescriptionRegistry) {
        this.configDescriptionRegistry = null;
    }

    private ThingStatusInfo buildStatusInfo(ThingStatus thingStatus, ThingStatusDetail thingStatusDetail,
            String description) {
        ThingStatusInfoBuilder statusInfoBuilder = ThingStatusInfoBuilder.create(thingStatus, thingStatusDetail);
        statusInfoBuilder.withDescription(description);
        return statusInfoBuilder.build();
    }

    private ThingStatusInfo buildStatusInfo(ThingStatus thingStatus, ThingStatusDetail thingStatusDetail) {
        return buildStatusInfo(thingStatus, thingStatusDetail, null);
    }

    private void setThingStatus(Thing thing, ThingStatusInfo thingStatusInfo) {
        ThingStatusInfo oldStatusInfo = thing.getStatusInfo();
        thing.setStatusInfo(thingStatusInfo);
        try {
            eventPublisher.post(ThingEventFactory.createStatusInfoEvent(thing.getUID(), thingStatusInfo));
            if (!oldStatusInfo.equals(thingStatusInfo)) {
                eventPublisher.post(
                        ThingEventFactory.createStatusInfoChangedEvent(thing.getUID(), thingStatusInfo, oldStatusInfo));
            }
        } catch (Exception ex) {
            logger.error("Could not post 'ThingStatusInfoEvent' event: " + ex.getMessage(), ex);
        }
    }

    protected void setThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        this.thingTypeRegistry = thingTypeRegistry;
    }

    protected void unsetThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        this.thingTypeRegistry = null;
    }

}
