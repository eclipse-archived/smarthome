/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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
import java.util.Collection;
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

import org.eclipse.smarthome.config.core.BundleProcessor;
import org.eclipse.smarthome.config.core.BundleProcessor.BundleProcessorListener;
import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.common.SafeMethodCaller;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemUtil;
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
import org.eclipse.smarthome.core.thing.ThingTypeMigrationService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.eclipse.smarthome.core.thing.events.ThingEventFactory;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.eclipse.smarthome.core.thing.util.ThingHandlerHelper;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

/**
 * {@link ThingManager} tracks all things in the {@link ThingRegistry} and
 * mediates the communication between the {@link Thing} and the {@link ThingHandler} from the binding. Therefore it
 * tracks {@link ThingHandlerFactory}s and calls {@link ThingHandlerFactory#registerHandler(Thing)} for each thing, that
 * was added to the {@link ThingRegistry}. In addition the {@link ThingManager} acts
 * as an {@link EventHandler} and subscribes to smarthome update and command
 * events. Finally the {@link ThingManager} implement the {@link ThingTypeMigrationService} to offer
 * a way to change the thing type of a {@link Thing}.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Michael Grammling - Added dynamic configuration update
 * @author Stefan Bußweiler - Added new thing status handling, migration to new event mechanism,
 *         refactorings due to thing/bridge life cycle
 * @author Simon Kaufmann - Added remove handling, type conversion
 * @author Kai Kreuzer - Removed usage of itemRegistry and thingLinkRegistry, fixed vetoing mechanism
 * @author Andre Fuechsel - Added the {@link ThingTypeMigrationService} 
 */
public class ThingManager extends AbstractItemEventSubscriber
        implements ThingTracker, BundleProcessorListener, ThingTypeMigrationService {

    private static final String FORCEREMOVE_THREADPOOL_NAME = "forceRemove";

    private static final String THING_MANAGER_THREADPOOL_NAME = "thingManager";

    private final Multimap<Bundle, Object> initializerVetoes = Multimaps
            .synchronizedListMultimap(LinkedListMultimap.<Bundle, Object> create());
    private final Multimap<Long, ThingHandler> initializerQueue = Multimaps
            .synchronizedListMultimap(LinkedListMultimap.<Long, ThingHandler> create());

    private Logger logger = LoggerFactory.getLogger(ThingManager.class);

    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("thingManager");

    private EventPublisher eventPublisher;

    private ItemChannelLinkRegistry itemChannelLinkRegistry;

    private List<ThingHandlerFactory> thingHandlerFactories = new CopyOnWriteArrayList<>();

    private Map<ThingUID, ThingHandler> thingHandlers = new ConcurrentHashMap<>();

    private final SetMultimap<ThingHandlerFactory, ThingHandler> thingHandlersByFactory = Multimaps
            .synchronizedSetMultimap(HashMultimap.<ThingHandlerFactory, ThingHandler> create());

    private ThingTypeRegistry thingTypeRegistry;

    private ThingHandlerCallback thingHandlerCallback = new ThingHandlerCallback() {

        @Override
        public void stateUpdated(ChannelUID channelUID, State state) {
            Set<Item> items = itemChannelLinkRegistry.getLinkedItems(channelUID);
            for (Item item : items) {
                State acceptedState = ItemUtil.convertToAcceptedState(state, item);
                eventPublisher
                        .post(ItemEventFactory.createStateEvent(item.getName(), acceptedState, channelUID.toString()));
            }
        }

        @Override
        public void postCommand(ChannelUID channelUID, Command command) {
            Set<String> items = itemChannelLinkRegistry.getLinkedItemNames(channelUID);
            for (String item : items) {
                eventPublisher.post(ItemEventFactory.createCommandEvent(item, command, channelUID.toString()));
            }
        }

        @Override
        public void statusUpdated(Thing thing, ThingStatusInfo statusInfo) {
            // note: all provoked operations based on a status update should be executed asynchronously!
            ensureValidStatus(statusInfo.getStatus());

            ThingStatusInfo oldStatusInfo = thing.getStatusInfo();
            if (ThingStatus.REMOVING.equals(oldStatusInfo.getStatus())
                    && !ThingStatus.REMOVED.equals(statusInfo.getStatus())) {
                // only allow REMOVING -> REMOVED transition and ignore all other state changes
                return;
            }

            if (ThingStatus.UNKNOWN.equals(statusInfo.getStatus())
                    && !ThingStatus.INITIALIZING.equals(oldStatusInfo.getStatus())) {
                // only allow UNKNOWN in the beginning, not after ONLINE or OFFLINE
                return;
            }

            // update thing status and send event about new status
            setThingStatus(thing, statusInfo);

            // if thing is a bridge
            if (isBridge(thing)) {
                handleBridgeStatusUpdate((Bridge) thing, statusInfo, oldStatusInfo);
            }
            // if thing has a bridge
            if (hasBridge(thing)) {
                handleBridgeChildStatusUpdate(thing, oldStatusInfo);
            }
            // notify thing registry about thing removal
            if (ThingStatus.REMOVED.equals(thing.getStatus())) {
                notifyRegistryAboutForceRemove(thing);
            }
        }

        private void ensureValidStatus(ThingStatus status) {
            if (!(ThingStatus.UNKNOWN.equals(status) || ThingStatus.ONLINE.equals(status)
                    || ThingStatus.OFFLINE.equals(status) || ThingStatus.REMOVED.equals(status))) {
                throw new IllegalArgumentException(
                        MessageFormat.format("Illegal status {0}. Bindings only may set {1}, {2}, {3} or {4}.", status,
                                ThingStatus.UNKNOWN, ThingStatus.ONLINE, ThingStatus.OFFLINE, ThingStatus.REMOVED));
            }
        }

        private void handleBridgeStatusUpdate(Bridge bridge, ThingStatusInfo statusInfo,
                ThingStatusInfo oldStatusInfo) {
            if (ThingHandlerHelper.isHandlerInitialized(bridge)
                    && (ThingStatus.INITIALIZING.equals(oldStatusInfo.getStatus()))) {
                // bridge has just been initialized: initialize child things as well
                registerChildHandlers(bridge);
            } else if (!statusInfo.equals(oldStatusInfo)) {
                // bridge status has been changed: notify child things about status change
                notifyThingsAboutBridgeStatusChange(bridge, statusInfo);
            }
        }

        private void handleBridgeChildStatusUpdate(Thing thing, ThingStatusInfo oldStatusInfo) {
            if (ThingHandlerHelper.isHandlerInitialized(thing)
                    && ThingStatus.INITIALIZING.equals(oldStatusInfo.getStatus())) {
                // child thing has just been initialized: notify bridge about it
                notifyBridgeAboutChildHandlerInitialization(thing);
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
        public void migrateThingType(final Thing thing, final ThingTypeUID thingTypeUID,
                final Configuration configuration) {
            ThingManager.this.migrateThingType(thing, thingTypeUID, configuration);
        }

        @Override
        public void channelTriggered(Thing thing, ChannelUID channelUID, String event) {
            eventPublisher.post(ThingEventFactory.createTriggerEvent(event, channelUID));
        }

    };

    private ThingRegistryImpl thingRegistry;

    private ConfigDescriptionRegistry configDescriptionRegistry;

    private ManagedThingProvider managedThingProvider;

    private Set<Thing> things = new CopyOnWriteArraySet<>();

    private Set<ThingUID> registerHandlerLock = new HashSet<>();

    private Set<ThingUID> thingUpdatedLock = new HashSet<>();

    private Set<BundleProcessor> bundleProcessors = new HashSet<>();

    @Override
    public void migrateThingType(final Thing thing, final ThingTypeUID thingTypeUID,
            final Configuration configuration) {
        final ThingType thingType = thingTypeRegistry.getThingType(thingTypeUID);
        if (thingType == null) {
            throw new RuntimeException(
                    MessageFormat.format("No thing type {0} registered, cannot change thing type for thing {1}",
                            thingTypeUID.getAsString(), thing.getUID().getAsString()));
        }
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                ThingUID thingUID = thing.getUID();
                waitForRunningHandlerRegistrations(thingUID);

                // Remove the ThingHandler, if any
                final ThingHandlerFactory oldThingHandlerFactory = findThingHandlerFactory(thing.getThingTypeUID());
                if (oldThingHandlerFactory != null) {
                    ThingHandler thingHandler = thing.getHandler();
                    unregisterHandler(thing, oldThingHandlerFactory);
                    disposeHandler(thing, thingHandler);
                    waitUntilHandlerUnregistered(thing, 60 * 1000);
                } else {
                    logger.debug("No ThingHandlerFactory available that can handle {}", thing.getThingTypeUID());
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

                logger.debug("Changed ThingType of Thing {} to {}. New ThingHandler is {}.", thing.getUID().toString(),
                        thing.getThingTypeUID(), thing.getHandler().toString());
            }

            private void waitUntilHandlerUnregistered(final Thing thing, int timeout) {
                for (int i = 0; i < timeout / 100; i++) {
                    if (thing.getHandler() == null && thingHandlers.get(thing.getUID()) == null) {
                        return;
                    }
                    try {
                        Thread.sleep(100);
                        logger.debug("Waiting for handler deregistration to complete for thing {}. Took already {}ms.",
                                thing.getUID().getAsString(), (i + 1) * 100);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                String message = MessageFormat.format(
                        "Thing type migration failed for {0}. The handler deregistration did not complete within {1}ms.",
                        thing.getUID().getAsString(), timeout);
                logger.error(message);
                throw new RuntimeException(message);
            }

            private void waitForRunningHandlerRegistrations(ThingUID thingUID) {
                for (int i = 0; i < 10 * 10; i++) {
                    if (!registerHandlerLock.contains(thingUID)) {
                        return;
                    }
                    try {
                        // Wait a little to give running handler registrations a chance to complete...
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                String message = MessageFormat.format(
                        "Thing type migration failed for {0}. Could not obtain lock for hander registration.",
                        thingUID.getAsString());
                logger.error(message);
                throw new RuntimeException(message);
            }
        }, 0, TimeUnit.MILLISECONDS);
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
                        if (ThingHandlerHelper.isHandlerInitialized(thing)) {
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
                                logger.error("Exception occured while calling handler: {}", ex.getMessage(), ex);
                            }
                        } else {
                            logger.info(
                                    "Not delegating command '{}' for item '{}' to handler for channel '{}', "
                                            + "because handler is not initialized (thing must be in status UNKNOWN, ONLINE or OFFLINE).",
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
                        if (ThingHandlerHelper.isHandlerInitialized(thing)) {
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
                                logger.error("Exception occured while calling handler: {}", ex.getMessage(), ex);
                            }
                        } else {
                            logger.info(
                                    "Not delegating update '{}' for item '{}' to handler for channel '{}', "
                                            + "because handler is not initialized (thing must be in status UNKNOWN, ONLINE or OFFLINE).",
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

        if (!isHandlerRegistered(thing)) {
            registerAndInitializeHandler(thing, getThingHandlerFactory(thing));
        } else {
            logger.warn("Handler of tracked thing '{}' already registered.", thing.getUID());
        }
    }

    @Override
    public void thingRemoving(Thing thing, ThingTrackerEvent thingTrackerEvent) {
        setThingStatus(thing, ThingStatusInfoBuilder.create(ThingStatus.REMOVING).build());
        notifyThingHandlerAboutRemoval(thing);
    }

    @Override
    public void thingRemoved(final Thing thing, ThingTrackerEvent thingTrackerEvent) {
        logger.debug("Thing '{}' is no longer tracked by ThingManager.", thing.getUID());

        ThingHandler thingHandler = thingHandlers.get(thing.getUID());
        if (thingHandler != null) {
            final ThingHandlerFactory thingHandlerFactory = findThingHandlerFactory(thing.getThingTypeUID());
            if (thingHandlerFactory != null) {
                unregisterHandler(thing, thingHandlerFactory);
                disposeHandler(thing, thingHandler);
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

        final ThingHandler thingHandler = thingHandlers.get(thingUID);
        if (thingHandler != null) {
            if (oldThing != thing) {
                thing.setHandler(thingHandler);
            }
            if (ThingHandlerHelper.isHandlerInitialized(thing)) {
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
                    logger.error("Exception occured while calling thing updated at ThingHandler '{}': {}", thingHandler,
                            ex.getMessage(), ex);
                }
            } else {
                logger.debug(
                        "Cannot notify handler about updated thing '{}', because handler is not initialized (thing must be in status UNKNOWN, ONLINE or OFFLINE). Starting handler initialization instead.",
                        thing.getThingTypeUID());
                initializeHandler(thing);
            }
        } else {
            registerAndInitializeHandler(thing, getThingHandlerFactory(thing));
        }

        if (oldThing != thing) {
            oldThing.setHandler(null);
        }
    }

    private Thing getThing(ThingUID id) {
        for (Thing thing : this.things) {
            if (thing.getUID().equals(id)) {
                return thing;
            }
        }
        return null;
    }

    private ThingType getThingType(Thing thing) {
        return thingTypeRegistry.getThingType(thing.getThingTypeUID());
    }

    private ThingHandlerFactory findThingHandlerFactory(ThingTypeUID thingTypeUID) {
        for (ThingHandlerFactory factory : thingHandlerFactories) {
            if (factory.supportsThingType(thingTypeUID)) {
                return factory;
            }
        }
        return null;
    }

    private void registerHandler(Thing thing, ThingHandlerFactory thingHandlerFactory) {
        synchronized (thing) {
            if (!isHandlerRegistered(thing)) {
                if (!hasBridge(thing)) {
                    doRegisterHandler(thing, thingHandlerFactory);
                } else {
                    Bridge bridge = getBridge(thing.getBridgeUID());
                    if (bridge != null && ThingHandlerHelper.isHandlerInitialized(bridge)) {
                        doRegisterHandler(thing, thingHandlerFactory);
                    } else {
                        setThingStatus(thing,
                                buildStatusInfo(ThingStatus.UNINITIALIZED, ThingStatusDetail.HANDLER_MISSING_ERROR));
                    }
                }
            } else {
                logger.warn("Attempt to register a handler twice for thing {} at the same time will be ignored.",
                        thing.getUID());
            }
        }
    }

    private void doRegisterHandler(final Thing thing, final ThingHandlerFactory thingHandlerFactory) {
        logger.debug("Calling '{}.registerHandler()' for thing '{}'.", thingHandlerFactory.getClass().getSimpleName(),
                thing.getUID());
        try {
            ThingHandler thingHandler = thingHandlerFactory.registerHandler(thing);
            thingHandler.setCallback(ThingManager.this.thingHandlerCallback);
            thing.setHandler(thingHandler);
            thingHandlers.put(thing.getUID(), thingHandler);
            thingHandlersByFactory.put(thingHandlerFactory, thingHandler);
        } catch (Exception ex) {
            ThingStatusInfo statusInfo = buildStatusInfo(ThingStatus.UNINITIALIZED,
                    ThingStatusDetail.HANDLER_REGISTERING_ERROR,
                    ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
            setThingStatus(thing, statusInfo);
            logger.error("Exception occured while calling thing handler factory '{}': {}", thingHandlerFactory,
                    ex.getMessage(), ex);
        }
    }

    private void registerChildHandlers(final Bridge bridge) {
        for (final Thing child : bridge.getThings()) {
            logger.debug("Register and initialize child '{}' of bridge '{}'.", child.getUID(), bridge.getUID());
            ThreadPoolManager.getPool(THING_MANAGER_THREADPOOL_NAME).execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        registerAndInitializeHandler(child, getThingHandlerFactory(child));
                    } catch (Exception ex) {
                        logger.error(
                                "Registration resp. initialization of child '{}' of bridge '{}' has been failed: {}",
                                child.getUID(), bridge.getUID(), ex.getMessage(), ex);
                    }
                }
            });
        }
    }

    private void initializeHandler(Thing thing) {
        if (!isHandlerRegistered(thing)) {
            return;
        }
        synchronized (thing) {
            if (!isInitializing(thing)) {
                doInitializeHandler(thing, thing.getHandler());
            } else {
                logger.warn("Attempt to initialize a handler twice for thing '{}' at the same time will be ignored.",
                        thing.getUID());
            }
        }
    }

    private void doInitializeHandler(Thing thing, ThingHandler thingHandler) {
        if (!isVetoed(thingHandler)) {
            ThingType thingType = getThingType(thing);
            applyDefaultConfiguration(thing, thingType);
            if (isInitializable(thing, thingType)) {
                setThingStatus(thing, buildStatusInfo(ThingStatus.INITIALIZING, ThingStatusDetail.NONE));
                doInitializeHandler(thingHandler);
            } else {
                logger.debug("Thing '{}' not initializable, check required configuration parameters.", thing.getUID());
                setThingStatus(thing,
                        buildStatusInfo(ThingStatus.UNINITIALIZED, ThingStatusDetail.HANDLER_CONFIGURATION_PENDING));
            }
        }
    }

    private boolean isVetoed(final ThingHandler thingHandler) {
        boolean veto = false;
        Bundle bundle = getBundle(thingHandler.getClass());
        for (BundleProcessor proc : bundleProcessors) {
            if (!proc.hasFinishedLoading(bundle)) {
                veto = true;
                if (!initializerVetoes.containsEntry(bundle, proc)) {
                    logger.trace("Marking '{}' vetoed by '{}'", bundle.getSymbolicName(), proc);
                    initializerVetoes.put(bundle, proc);
                }
            } else {
                logger.trace("'{}' already finished processing '{}'", proc, bundle.getSymbolicName());
            }
        }
        if (veto) {
            if (!initializerQueue.containsEntry(bundle, thingHandler)) {
                logger.trace("Queueing '{}' in bundle '{}'", thingHandler, bundle.getSymbolicName());
                initializerQueue.put(bundle.getBundleId(), thingHandler);
            }
            logger.debug(
                    "Meta-data of bundle '{}' is not fully loaded ({}), deferring handler initialization for thing '{}'",
                    bundle.getSymbolicName(), initializerVetoes.get(bundle), thingHandler.getThing().getUID());
        } else {
            logger.debug("Finished loading meta-data of bundle '{}'", bundle.getSymbolicName());
        }
        return veto;
    }

    private Bundle getBundle(final Class<?> classFromBundle) {
        ClassLoader classLoader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                return classFromBundle.getClassLoader();
            }
        });

        if (classLoader instanceof BundleReference) {
            Bundle bundle = ((BundleReference) classLoader).getBundle();
            logger.trace("Bundle of {} is {}", classFromBundle, bundle.getSymbolicName());
            return bundle;
        }
        return null;
    }

    private void applyDefaultConfiguration(Thing thing, ThingType thingType) {
        if (thingType != null) {
            ThingFactoryHelper.applyDefaultConfiguration(thing.getConfiguration(), thingType,
                    configDescriptionRegistry);
        }
    }

    private boolean isInitializable(Thing thing, ThingType thingType) {
        // determines if all 'required' configuration parameters are available in the configuration
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

    private void doInitializeHandler(final ThingHandler thingHandler) {
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                logger.debug("Calling initialize handler for thing '{}' at '{}'.", thingHandler.getThing().getUID(),
                        thingHandler);
                try {
                    SafeMethodCaller.call(new SafeMethodCaller.ActionWithException<Void>() {
                        @Override
                        public Void call() throws Exception {
                            thingHandler.initialize();
                            return null;
                        }
                    });
                } catch (TimeoutException ex) {
                    logger.warn("Initializing handler for thing '{}' takes more than {}ms.",
                            thingHandler.getThing().getUID(), SafeMethodCaller.DEFAULT_TIMEOUT);
                } catch (Exception ex) {
                    ThingStatusInfo statusInfo = buildStatusInfo(ThingStatus.UNINITIALIZED,
                            ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                            ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                    setThingStatus(thingHandler.getThing(), statusInfo);
                    logger.error("Exception occured while initializing handler of thing '{}': {}",
                            thingHandler.getThing().getUID(), ex.getMessage(), ex);
                }
            }
        }, 0, TimeUnit.NANOSECONDS);
    }

    @Override
    public void bundleFinished(BundleProcessor context, Bundle bundle) {
        initializerVetoes.remove(bundle, context);
        if (initializerVetoes.get(bundle).isEmpty()) {
            synchronized (initializerQueue) {
                for (ThingHandler thingHandler : initializerQueue.removeAll(bundle.getBundleId())) {
                    initializeHandler(thingHandler.getThing());
                }
            }
        } else {
            logger.debug("'{}' still vetoed by '{}'", bundle.getSymbolicName(), initializerVetoes.get(bundle));
            logger.debug("'{}' queued '{}'", bundle.getSymbolicName(), initializerQueue.get(bundle.getBundleId()));
        }
    }

    private boolean isInitializing(Thing thing) {
        return thing.getStatus() == ThingStatus.INITIALIZING;
    }

    private boolean isHandlerRegistered(Thing thing) {
        ThingHandler handler = thingHandlers.get(thing.getUID());
        return handler != null && handler == thing.getHandler();
    }

    private boolean isBridge(Thing thing) {
        return thing instanceof Bridge;
    }

    private boolean hasBridge(final Thing thing) {
        return thing.getBridgeUID() != null;
    }

    private Bridge getBridge(ThingUID bridgeUID) {
        Thing bridge = thingRegistry.get(bridgeUID);
        return isBridge(bridge) ? (Bridge) bridge : null;
    }

    private void unregisterHandler(Thing thing, ThingHandlerFactory thingHandlerFactory) {
        synchronized (thing) {
            if (isHandlerRegistered(thing)) {
                if (!isBridge(thing)) {
                    doUnregisterHandler(thing, thingHandlerFactory);
                } else {
                    unregisterChildHandlers((Bridge) thing, thingHandlerFactory);
                    doUnregisterHandler(thing, thingHandlerFactory);
                }
            }
        }
    }

    private void doUnregisterHandler(final Thing thing, final ThingHandlerFactory thingHandlerFactory) {
        logger.debug("Calling unregisterHandler handler for thing '{}' at '{}'.", thing.getUID(), thingHandlerFactory);
        try {
            SafeMethodCaller.call(new SafeMethodCaller.ActionWithException<Void>() {
                @Override
                public Void call() throws Exception {
                    ThingHandler thingHandler = thing.getHandler();
                    thingHandlerFactory.unregisterHandler(thing);
                    thingHandler.setCallback(null);
                    thing.setHandler(null);
                    setThingStatus(thing,
                            buildStatusInfo(ThingStatus.UNINITIALIZED, ThingStatusDetail.HANDLER_MISSING_ERROR));
                    thingHandlers.remove(thing.getUID());
                    thingHandlersByFactory.remove(thingHandlerFactory, thingHandler);
                    return null;
                }
            });
        } catch (Exception ex) {
            logger.error("Exception occured while calling thing handler factory '{}' ", thingHandlerFactory,
                    ex.getMessage(), ex);
        }
    }

    private void disposeHandler(Thing thing, ThingHandler thingHandler) {
        synchronized (thing) {
            doDisposeHandler(thingHandler);
            if (hasBridge(thing)) {
                notifyBridgeAboutChildHandlerDisposal(thing, thingHandler);
            }
        }
    }

    private void doDisposeHandler(final ThingHandler thingHandler) {
        logger.debug("Calling dispose handler for thing '{}' at '{}'.", thingHandler.getThing().getUID(), thingHandler);
        try {
            SafeMethodCaller.call(new SafeMethodCaller.ActionWithException<Void>() {
                @Override
                public Void call() throws Exception {
                    thingHandler.dispose();
                    return null;
                }
            });
        } catch (TimeoutException ex) {
            logger.warn("Disposing handler for thing '{}' takes more than {}ms.", thingHandler.getThing().getUID(),
                    SafeMethodCaller.DEFAULT_TIMEOUT);
        } catch (Exception e) {
            logger.error("Exception occured while disposing handler of thing '{}': {}",
                    thingHandler.getThing().getUID(), e.getMessage(), e);
        }
    }

    private void unregisterChildHandlers(Bridge bridge, ThingHandlerFactory thingHandlerFactory) {
        addThingsToBridge(bridge);
        for (Thing child : bridge.getThings()) {
            ThingHandler handler = child.getHandler();
            if (handler != null) {
                logger.debug("Unregister and dispose child '{}' of bridge '{}'.", child.getUID(), bridge.getUID());
                unregisterHandler(child, thingHandlerFactory);
                disposeHandler(child, handler);
            }
        }
    }

    private void addThingsToBridge(Bridge bridge) {
        Collection<Thing> things = thingRegistry.getAll();
        for (Thing thing : things) {
            ThingUID bridgeUID = thing.getBridgeUID();
            if (bridgeUID != null && bridgeUID.equals(bridge.getUID())) {
                if (bridge instanceof BridgeImpl && !bridge.getThings().contains(thing)) {
                    ((BridgeImpl) bridge).addThing(thing);
                }
            }
        }
    }

    private void notifyThingsAboutBridgeStatusChange(final Bridge bridge, final ThingStatusInfo bridgeStatus) {
        if (ThingHandlerHelper.isHandlerInitialized(bridge)) {
            for (final Thing child : bridge.getThings()) {
                ThreadPoolManager.getPool(THING_MANAGER_THREADPOOL_NAME).execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ThingHandler handler = child.getHandler();
                            if (handler != null && ThingHandlerHelper.isHandlerInitialized(child)) {
                                handler.bridgeStatusChanged(bridgeStatus);
                            }
                        } catch (Exception e) {
                            logger.error(
                                    "Exception occured during notification about bridge status change on thing '{}': {}",
                                    child.getUID(), e.getMessage(), e);
                        }
                    }
                });
            }
        }
    }

    private void notifyBridgeAboutChildHandlerInitialization(final Thing thing) {
        final Bridge bridge = getBridge(thing.getBridgeUID());
        if (bridge != null) {
            ThreadPoolManager.getPool(THING_MANAGER_THREADPOOL_NAME).execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        BridgeHandler bridgeHandler = bridge.getHandler();
                        if (bridgeHandler != null) {
                            bridgeHandler.childHandlerInitialized(thing.getHandler(), thing);
                        }
                    } catch (Exception e) {
                        logger.error(
                                "Exception occured during bridge handler ('{}') notification about handler initialization of child '{}': {}",
                                bridge.getUID(), thing.getUID(), e.getMessage(), e);
                    }
                }
            });
        }
    }

    private void notifyBridgeAboutChildHandlerDisposal(final Thing thing, final ThingHandler thingHandler) {
        final Bridge bridge = getBridge(thing.getBridgeUID());
        if (bridge != null) {
            ThreadPoolManager.getPool(THING_MANAGER_THREADPOOL_NAME).execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        BridgeHandler bridgeHandler = bridge.getHandler();
                        if (bridgeHandler != null) {
                            bridgeHandler.childHandlerDisposed(thingHandler, thing);
                        }
                    } catch (Exception ex) {
                        logger.error(
                                "Exception occured during bridge handler ('{}') notification about handler disposal of child '{}': {}",
                                bridge.getUID(), thing.getUID(), ex.getMessage(), ex);
                    }
                }
            });
        }
    }

    private void notifyThingHandlerAboutRemoval(final Thing thing) {
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
        this.thingRegistry.addThingTracker(this);
    }

    protected void addThingHandlerFactory(ThingHandlerFactory thingHandlerFactory) {
        logger.debug("Thing handler factory '{}' added", thingHandlerFactory.getClass().getSimpleName());

        thingHandlerFactories.add(thingHandlerFactory);

        for (Thing thing : things) {
            if (thingHandlerFactory.supportsThingType(thing.getThingTypeUID())) {
                if (!isHandlerRegistered(thing)) {
                    registerAndInitializeHandler(thing, thingHandlerFactory);
                } else {
                    logger.warn("Thing handler for thing '{}' already registered", thing.getUID());
                }
            }
        }
    }

    private void registerAndInitializeHandler(final Thing thing, final ThingHandlerFactory thingHandlerFactory) {
        if (thingHandlerFactory != null) {
            try {
                SafeMethodCaller.call(new SafeMethodCaller.ActionWithException<Void>() {
                    @Override
                    public Void call() throws Exception {
                        registerHandler(thing, thingHandlerFactory);
                        initializeHandler(thing);
                        return null;
                    }
                });
            } catch (TimeoutException e) {
                logger.warn("Registering a handler for thing '{}' takes more than {}ms.", thing.getUID(),
                        SafeMethodCaller.DEFAULT_TIMEOUT);
            } catch (Exception ex) {
                ThingStatusInfo statusInfo = buildStatusInfo(ThingStatus.UNINITIALIZED,
                        ThingStatusDetail.HANDLER_REGISTERING_ERROR,
                        ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                setThingStatus(thing, statusInfo);
                logger.error("Exception occured while registering the handler for thing '{}' using factory '{}': {}",
                        thing.getUID(), thingHandlerFactory, ex.getMessage(), ex);
            }
        } else {
            logger.debug("Not registering a handler at this point since no handler factory for thing '{}' found.",
                    thing.getUID());
        }
    }

    private ThingHandlerFactory getThingHandlerFactory(Thing thing) {
        ThingHandlerFactory thingHandlerFactory = findThingHandlerFactory(thing.getThingTypeUID());
        if (thingHandlerFactory != null) {
            return thingHandlerFactory;
        }
        logger.debug("Not registering a handler at this point since no handler factory for thing '{}' found.",
                thing.getUID());
        return null;
    }

    protected void deactivate(ComponentContext componentContext) {
        this.thingRegistry.removeThingTracker(this);
    }

    protected void removeThingHandlerFactory(ThingHandlerFactory thingHandlerFactory) {
        logger.debug("Thing handler factory '{}' removed", thingHandlerFactory.getClass().getSimpleName());

        Set<ThingHandler> handlers = ImmutableSet.copyOf(thingHandlersByFactory.get(thingHandlerFactory));
        for (ThingHandler thingHandler : handlers) {
            Thing thing = thingHandler.getThing();
            if (thing != null && isHandlerRegistered(thing)) {
                unregisterHandler(thing, thingHandlerFactory);
                disposeHandler(thing, thingHandler);
            }
        }
        thingHandlersByFactory.removeAll(thingHandlerFactory);
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

    protected void setBundleProcessor(BundleProcessor bundleProcessor) {
        logger.trace("Added '{}'", bundleProcessor);
        bundleProcessors.add(bundleProcessor);
        bundleProcessor.registerListener(this);
    }

    protected void unsetBundleProcessor(BundleProcessor bundleProcessor) {
        bundleProcessor.unregisterListener(this);
        bundleProcessors.remove(bundleProcessor);
    }

}
