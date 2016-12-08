/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.config.core.BundleProcessor
import org.eclipse.smarthome.config.core.ConfigDescription
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterBuilder
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.config.core.BundleProcessor.BundleProcessorListener
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener
import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.events.EventPublisher
import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.events.TopicEventFilter
import org.eclipse.smarthome.core.items.Item
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.items.events.ItemCommandEvent
import org.eclipse.smarthome.core.items.events.ItemEventFactory
import org.eclipse.smarthome.core.items.events.ItemStateEvent
import org.eclipse.smarthome.core.library.items.StringItem
import org.eclipse.smarthome.core.library.types.DecimalType
import org.eclipse.smarthome.core.library.types.StringType
import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.Channel
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingStatusDetail
import org.eclipse.smarthome.core.thing.ThingStatusInfo
import org.eclipse.smarthome.core.thing.ThingTypeMigrationService
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.BridgeHandler
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder
import org.eclipse.smarthome.core.thing.events.ThingEventFactory
import org.eclipse.smarthome.core.thing.events.ThingStatusInfoChangedEvent
import org.eclipse.smarthome.core.thing.events.ThingStatusInfoEvent
import org.eclipse.smarthome.core.thing.link.ItemChannelLink
import org.eclipse.smarthome.core.thing.link.ManagedItemChannelLinkProvider
import org.eclipse.smarthome.core.thing.link.ThingLinkManager
import org.eclipse.smarthome.core.thing.type.ThingType
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry
import org.eclipse.smarthome.core.types.State
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.framework.Bundle

import com.google.common.collect.Sets

/**
 * {@link ThingManagerOSGiTest} tests the {@link ThingManager}.
 */
class ThingManagerOSGiTest extends OSGiTest {

    ManagedThingProvider managedThingProvider
    ThingLinkManager thingLinkManager
    ItemRegistry itemRegistry

    ManagedItemChannelLinkProvider managedItemChannelLinkProvider

    def THING_TYPE_UID = new ThingTypeUID("binding:type")

    def THING_UID = new ThingUID(THING_TYPE_UID, "id")

    def CHANNEL_UID = new ChannelUID(THING_UID, "channel")

    Thing THING;

    EventPublisher eventPublisher

    @Before
    void setUp() {
        THING = ThingBuilder.create(THING_UID).withChannels([
            new Channel(CHANNEL_UID, "Switch")
        ]).build()
        registerVolatileStorageService()
        thingLinkManager = getService ThingLinkManager
        thingLinkManager.deactivate()
        managedItemChannelLinkProvider = getService(ManagedItemChannelLinkProvider)
        managedThingProvider = getService(ManagedThingProvider)
        eventPublisher = getService(EventPublisher)
        itemRegistry = getService(ItemRegistry)
        assertNotNull(itemRegistry)
    }

    @After
    void teardown() {
        managedThingProvider.getAll().each {
            managedThingProvider.remove(it.getUID())
        }
        thingLinkManager.activate(null)
    }

    @Test
    void 'ThingManager changes the thing type'() {
        registerThingTypeProvider()

        def thingHandlerFactory = [
            supportsThingType: { ThingTypeUID thingTypeUID -> true },
            registerHandler: { thing ->
                def thingHandler = [
                    setCallback: {},
                    initialize: {},
                    dispose: {},
                    getThing: { return THING }
                ] as ThingHandler
            },
            unregisterHandler: {},
            removeThing: {
            }
        ] as ThingHandlerFactory

        registerService(thingHandlerFactory)

        managedThingProvider.add(THING)

        assertThat THING.getThingTypeUID().getAsString(), is(equalTo(THING_TYPE_UID.getAsString()))

        def THING_TYPE_UID_NEW = new ThingTypeUID("binding:type2")

        def migrator = getService(ThingTypeMigrationService.class)
        assertThat migrator, is(not(null))

        migrator.migrateThingType(THING, THING_TYPE_UID_NEW, THING.getConfiguration())

        waitForAssert {assertThat THING.getThingTypeUID().getAsString(), is(equalTo(THING_TYPE_UID_NEW.getAsString()))}
    }

    @Test(expected=RuntimeException.class)
    void 'ThingManager does not change the thing type when new thing type is not registered'() {

        def thingHandlerFactory = [
            supportsThingType: { ThingTypeUID thingTypeUID -> true },
            registerHandler: { thing ->
                def thingHandler = [
                    setCallback: {},
                    initialize: {},
                    dispose: {},
                    getThing: { return THING }
                ] as ThingHandler
            },
            unregisterHandler: {},
            removeThing: {
            }
        ] as ThingHandlerFactory

        registerService(thingHandlerFactory)

        managedThingProvider.add(THING)

        assertThat THING.getThingTypeUID().getAsString(), is(equalTo(THING_TYPE_UID.getAsString()))

        def THING_TYPE_UID_NEW = new ThingTypeUID("binding:type2")

        def migrator = getService(ThingTypeMigrationService.class)
        assertThat migrator, is(not(null))

        migrator.migrateThingType(THING, THING_TYPE_UID_NEW, THING.getConfiguration())
    }

    @Test
    void 'ThingManager calls registerHandler for added Thing'() {

        def registerHandlerCalled = false

        def thingHandlerFactory = [
            supportsThingType: { ThingTypeUID thingTypeUID -> true },
            registerHandler: { thing ->
                registerHandlerCalled = true
                def thingHandler = [
                    setCallback: {},
                    initialize: {},
                    dispose: {},
                    getThing: { return THING }
                ] as ThingHandler
            },
            unregisterHandler: {},
            removeThing: {
            }
        ] as ThingHandlerFactory

        registerService(thingHandlerFactory)

        managedThingProvider.add(THING)

        waitForAssert {assertThat registerHandlerCalled, is(true)}
    }

    @Test
    void 'ThingManager calls unregisterHandler for removed Thing'() {

        def unregisterHandlerCalled = false
        def removeThingCalled = false

        def thingHandlerFactory = [
            supportsThingType: {thingTypeUID -> true },
            registerHandler: {thing ->
                def thingHandler = [
                    setCallback: {},
                    initialize: {},
                    dispose: {},
                    getThing: { return THING }
                ] as ThingHandler
            },
            unregisterHandler: { thing -> unregisterHandlerCalled = true },
            removeThing: { thingUID -> removeThingCalled = true }
        ] as ThingHandlerFactory

        registerService(thingHandlerFactory)

        managedThingProvider.add(THING)

        managedThingProvider.remove(THING.getUID())

        waitForAssert {assertThat removeThingCalled, is(true)}
        waitForAssert {assertThat unregisterHandlerCalled, is(true)}
    }

    @Test
    void 'ThingManager handles thing handler lifecycle correctly'() {
        ThingHandlerCallback callback
        def registerHandlerCalled = false
        def unregisterHandlerCalled = false
        def initializeHandlerCalled = false
        def disposeHandlerCalled = false

        def thingHandler = [
            setCallback: { callbackArg -> callback = callbackArg },
            initialize: {
                initializeHandlerCalled = true
                callback.statusUpdated(THING, ThingStatusInfoBuilder.create(ThingStatus.ONLINE).build())
            },
            dispose: { disposeHandlerCalled = true },
            getThing: { return THING }
        ] as ThingHandler

        def thingHandlerFactory = [
            supportsThingType: { thingTypeUID -> true },
            registerHandler: { thing ->
                registerHandlerCalled = true
                thingHandler
            },
            unregisterHandler: { thing -> unregisterHandlerCalled = true },
            removeThing: {
            }
        ] as ThingHandlerFactory

        registerService(thingHandlerFactory)

        def statusInfo = ThingStatusInfoBuilder.create(ThingStatus.UNINITIALIZED, ThingStatusDetail.NONE).build()
        assertThat THING.getStatusInfo(), is(statusInfo)

        // add thing - provokes handler registration & initialization
        managedThingProvider.add(THING)
        waitForAssert {assertThat registerHandlerCalled, is(true)}
        waitForAssert {assertThat initializeHandlerCalled, is(true)}
        registerHandlerCalled = false
        initializeHandlerCalled = false
        statusInfo = ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build()
        waitForAssert { assertThat THING.getStatusInfo(), is(statusInfo) }

        // remove handler factory - provokes handler deregistration & disposal
        unregisterService(thingHandlerFactory)
        waitForAssert {assertThat unregisterHandlerCalled, is(true)}
        waitForAssert {assertThat disposeHandlerCalled, is(true)}
        unregisterHandlerCalled = false
        disposeHandlerCalled = false
        statusInfo = ThingStatusInfoBuilder.create(ThingStatus.UNINITIALIZED, ThingStatusDetail.HANDLER_MISSING_ERROR).build()
        waitForAssert { assertThat THING.getStatusInfo(), is(statusInfo) }

        // add handler factory - provokes handler registration & initialization
        registerService(thingHandlerFactory)
        waitForAssert {assertThat registerHandlerCalled, is(true)}
        waitForAssert {assertThat initializeHandlerCalled, is(true)}
        registerHandlerCalled = false
        initializeHandlerCalled = false
        statusInfo = ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build()
        waitForAssert { assertThat THING.getStatusInfo(), is(statusInfo) }

        // remove thing - provokes handler deregistration & disposal
        managedThingProvider.remove(THING.UID)
        waitForAssert {assertThat unregisterHandlerCalled, is(true)}
        waitForAssert {assertThat disposeHandlerCalled, is(true)}
        unregisterHandlerCalled = false
        disposeHandlerCalled = false
        statusInfo = ThingStatusInfoBuilder.create(ThingStatus.UNINITIALIZED, ThingStatusDetail.HANDLER_MISSING_ERROR).build()
        waitForAssert { assertThat THING.getStatusInfo(), is(statusInfo) }
    }

    @Test
    void 'ThingManager handles failing handler initialization correctly'() {
        ThingHandlerCallback callback
        Thing testThing = ThingBuilder.create(THING_TYPE_UID, THING_UID)
                .withConfiguration(new Configuration())
                .build()
        testThing.getConfiguration().put("shouldFail", true)

        def thingHandler = [
            setCallback: { callbackArg -> callback = callbackArg },
            initialize: {
                def shouldFail = testThing.getConfiguration().get("shouldFail") as boolean
                if(shouldFail) {
                    throw new Exception("Invalid config!")
                } else {
                    callback.statusUpdated(testThing, ThingStatusInfoBuilder.create(ThingStatus.ONLINE).build())
                }
            },
            dispose: {},
            getThing: { testThing }
        ] as ThingHandler

        def thingHandlerFactory = [
            supportsThingType: { thingTypeUID -> true },
            registerHandler: { thing -> thingHandler },
            unregisterHandler: { thing -> },
            removeThing: {
            }
        ] as ThingHandlerFactory
        registerService(thingHandlerFactory)

        def statusInfo = ThingStatusInfoBuilder.create(ThingStatus.UNINITIALIZED, ThingStatusDetail.NONE).build()
        assertThat testThing.getStatusInfo(), is(statusInfo)

        managedThingProvider.add(testThing)
        statusInfo = ThingStatusInfoBuilder.create(ThingStatus.UNINITIALIZED, ThingStatusDetail.HANDLER_INITIALIZING_ERROR).build()
        waitForAssert { assertThat testThing.getStatusInfo(), is(statusInfo) }

        testThing.getConfiguration().put("shouldFail", false)
        managedThingProvider.update(testThing)
        statusInfo = ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build()
        waitForAssert { assertThat testThing.getStatusInfo(), is(statusInfo) }
    }

    @Test
    void 'ThingManager handles bridge-thing handler life cycle correctly'() {
        def initCalledCounter = 0
        def disposedCalledCounter = 0
        ThingHandlerCallback callback

        def bridge = BridgeBuilder.create(new ThingTypeUID("binding:test"), new ThingUID("binding:test:someBridgeUID-1")).build()
        def bridgeInitCalled = false
        def bridgeInitCalledOrder = 0
        def bridgeDisposedCalled = false
        def bridgeDisposedCalledOrder = 0
        def bridgeHandler = [
            setCallback: { callbackArg -> callback = callbackArg },
            initialize: {
                bridgeInitCalled = true
                bridgeInitCalledOrder = ++initCalledCounter
                callback.statusUpdated(bridge, ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build())
            },
            dispose: {
                bridgeDisposedCalled = true
                bridgeDisposedCalledOrder = ++disposedCalledCounter
            },
            thingDisposed: { thing -> },
            childHandlerInitialized: { thingHandler, thing -> },
            childHandlerDisposed: { thingHandler, thing -> },
            getThing: { bridge },
            handleRemoval: {
                callback.statusUpdated(bridge, ThingStatusInfoBuilder.create(ThingStatus.REMOVED).build())
            }
        ] as BridgeHandler

        def thing = ThingBuilder.create(new ThingTypeUID("binding:test"), new ThingUID("binding:test:someThingUID-1")).withBridge(bridge.getUID()).build()
        def thingInitCalled = false
        def thingInitCalledOrder = 0
        def thingDisposedCalled = false
        def thingDisposedCalledOrder = 0
        def thingHandler = [
            setCallback: {},
            initialize: {
                thingInitCalled = true
                thingInitCalledOrder = ++initCalledCounter
                callback.statusUpdated(thing, ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build())
            },
            dispose: {
                thingDisposedCalled = true
                thingDisposedCalledOrder = ++disposedCalledCounter
            },
            bridgeStatusChanged: { },
            getThing: { thing },
            handleRemoval: {
                callback.statusUpdated(thing, ThingStatusInfoBuilder.create(ThingStatus.REMOVED).build())
            }
        ] as ThingHandler

        def thingHandlerFactory = [
            supportsThingType: { ThingTypeUID thingTypeUID -> true },
            registerHandler: { thingArg ->
                if (thingArg instanceof Bridge) {
                    return bridgeHandler
                } else if (thingArg instanceof Thing) {
                    return thingHandler
                }
            },
            unregisterHandler: {},
            removeThing: {
            }
        ] as ThingHandlerFactory
        registerService(thingHandlerFactory)

        def statusInfo = ThingStatusInfoBuilder.create(ThingStatus.UNINITIALIZED, ThingStatusDetail.NONE).build()
        assertThat thing.getStatusInfo(), is(statusInfo)
        assertThat bridge.getStatusInfo(), is(statusInfo)
        assertThat bridgeInitCalled, is(false)
        assertThat bridgeInitCalledOrder, is(0)
        assertThat bridgeDisposedCalled, is(false)
        assertThat bridgeDisposedCalledOrder, is(0)
        assertThat thingInitCalled, is(false)
        assertThat thingInitCalledOrder, is(0)
        assertThat thingDisposedCalled, is(false)
        assertThat thingDisposedCalled, is(false)
        assertThat thingDisposedCalledOrder, is(0)

        ThingRegistry thingRegistry = getService(ThingRegistry)
        assertThat thingRegistry, not(null)

        // add thing - no thing initialization, because bridge is not available
        thingRegistry.add(thing)
        waitForAssert ({ assertThat thingInitCalled, is(false) })
        statusInfo = ThingStatusInfoBuilder.create(ThingStatus.UNINITIALIZED, ThingStatusDetail.HANDLER_MISSING_ERROR).build()
        waitForAssert ({ assertThat thing.getStatusInfo(), is(statusInfo) })

        // add bridge - provokes bridge & thing initialization
        thingRegistry.add(bridge)
        waitForAssert ({ assertThat bridgeInitCalled, is(true) })
        waitForAssert ({ assertThat bridgeInitCalledOrder, is(1) })
        waitForAssert ({ assertThat thingInitCalled, is(true) })
        waitForAssert ({ assertThat thingInitCalledOrder, is(2) })
        bridgeInitCalled = false
        thingInitCalled = false
        statusInfo = ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build()
        waitForAssert ({ assertThat thing.getStatusInfo(), is(statusInfo) })
        waitForAssert ({ assertThat bridge.getStatusInfo(), is(statusInfo) })

        // remove thing - provokes thing disposal
        thingRegistry.remove(thing.getUID())
        waitForAssert ({ assertThat thingDisposedCalled, is(true) })
        waitForAssert ({ assertThat thingDisposedCalledOrder, is(1) })
        thingDisposedCalled = false
        waitForAssert ({ assertThat bridge.getStatusInfo(), is(statusInfo) })
        statusInfo = ThingStatusInfoBuilder.create(ThingStatus.UNINITIALIZED, ThingStatusDetail.HANDLER_MISSING_ERROR).build()
        waitForAssert ({ assertThat thing.getStatusInfo(), is(statusInfo) })

        // add thing again - provokes thing initialization
        thingRegistry.add(thing)
        waitForAssert ({ assertThat thingInitCalled, is(true) })
        waitForAssert ({ assertThat thingInitCalledOrder, is(3) })
        thingInitCalled = false
        statusInfo = ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build()
        waitForAssert ({ assertThat thing.getStatusInfo(), is(statusInfo) })

        // remove bridge - provokes thing & bridge disposal
        thingRegistry.remove(bridge.getUID())
        waitForAssert ({ assertThat thingDisposedCalled, is(true) })
        waitForAssert ({ assertThat thingDisposedCalledOrder, is(2) })
        waitForAssert ({ assertThat bridgeDisposedCalled, is(true) })
        waitForAssert ({ assertThat bridgeDisposedCalledOrder, is(3) })
        thingDisposedCalled = false
        bridgeDisposedCalled = false
        statusInfo = ThingStatusInfoBuilder.create(ThingStatus.UNINITIALIZED, ThingStatusDetail.HANDLER_MISSING_ERROR).build()
        waitForAssert ({ assertThat thing.getStatusInfo(), is(statusInfo) })
        waitForAssert ({ assertThat bridge.getStatusInfo(), is(statusInfo) })

        // add bridge again
        thingRegistry.add(bridge)
        waitForAssert ({ assertThat bridgeInitCalled, is(true) })
        waitForAssert ({ assertThat bridgeInitCalledOrder, is(4) })
        waitForAssert ({ assertThat thingInitCalled, is(true) })
        waitForAssert ({ assertThat thingInitCalledOrder, is(5) })
        bridgeInitCalled = false
        thingInitCalled = false
        statusInfo = ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build()
        waitForAssert ({ assertThat thing.getStatusInfo(), is(statusInfo) })
        waitForAssert ({ assertThat bridge.getStatusInfo(), is(statusInfo) })

        // unregister factory
        unregisterService(thingHandlerFactory)
        waitForAssert ({ assertThat thingDisposedCalled, is(true) })
        waitForAssert ({ assertThat thingDisposedCalledOrder, is(4) })
        waitForAssert ({ assertThat bridgeDisposedCalled, is(true) })
        waitForAssert ({ assertThat bridgeDisposedCalledOrder, is(5) })
        statusInfo = ThingStatusInfoBuilder.create(ThingStatus.UNINITIALIZED, ThingStatusDetail.HANDLER_MISSING_ERROR).build()
        waitForAssert ({ assertThat thing.getStatusInfo(), is(statusInfo) })
        waitForAssert ({ assertThat bridge.getStatusInfo(), is(statusInfo) })
    }

    @Test
    void 'ThingManager does not delegate update events to its source'() {
        registerThingTypeProvider()

        def itemName = "name"
        def handleUpdateWasCalled = false
        def callback

        managedThingProvider.add(THING)
        managedItemChannelLinkProvider.add(new ItemChannelLink(itemName, CHANNEL_UID))
        def thingHandler = [
            handleUpdate: { ChannelUID channelUID, State newState -> handleUpdateWasCalled = true },
            setCallback: { callbackArg -> callback = callbackArg },
            initialize: {},
            dispose: {},
            channelLinked: {},
            getThing: { return THING }
        ] as ThingHandler

        def thingHandlerFactory = [
            supportsThingType: { thingTypeUID -> true },
            registerHandler: { thing -> thingHandler },
            unregisterHandler: { thing -> },
            removeThing: { thingUID ->
            }
        ] as ThingHandlerFactory
        registerService(thingHandlerFactory)

        callback.statusUpdated(THING, ThingStatusInfoBuilder.create(ThingStatus.ONLINE).build())

        // event should be delivered
        eventPublisher.post(ItemEventFactory.createStateEvent(itemName, new DecimalType(10)))
        waitForAssert { assertThat handleUpdateWasCalled, is(true) }

        handleUpdateWasCalled = false

        // event should not be delivered, because the source is the same
        eventPublisher.post(ItemEventFactory.createStateEvent(itemName, new DecimalType(10), CHANNEL_UID.toString()))
        waitFor({handleUpdateWasCalled == true}, 1000)
        assertThat handleUpdateWasCalled, is(false)
    }

    @Test
    void 'ThingManager handles state updates correctly'() {
        registerThingTypeProvider()

        def itemName = "name"
        def thingUpdatedWasCalled = false
        def callback;

        // Create item
        Item item = new StringItem(itemName)
        itemRegistry.add(item)

        managedThingProvider.add(THING)
        managedItemChannelLinkProvider.add(new ItemChannelLink(itemName, CHANNEL_UID))
        def thingHandler = [
            thingUpdated: { thingUpdatedWasCalled = true },
            setCallback: {callbackArg -> callback = callbackArg },
            initialize: {},
            dispose: {},
            channelLinked: {},
            getThing: {return THING}
        ] as ThingHandler

        def thingHandlerFactory = [
            supportsThingType: { thingTypeUID -> true},
            registerHandler: {thing -> thingHandler },
            unregisterHandler: {thing -> },
            removeThing: {thingUID ->
            }
        ] as ThingHandlerFactory
        registerService(thingHandlerFactory)

        callback.statusUpdated(THING, ThingStatusInfoBuilder.create(ThingStatus.ONLINE).build())

        Event receivedEvent = null
        def itemUpdateEventSubscriber = [
            receive: { event -> receivedEvent = event },
            getSubscribedEventTypes: { Sets.newHashSet(ItemStateEvent.TYPE) },
            getEventFilter: { new TopicEventFilter("smarthome/items/.*/state") },
        ] as EventSubscriber
        registerService(itemUpdateEventSubscriber)

        // thing manager posts the update to the event bus via EventPublisher
        callback.stateUpdated(CHANNEL_UID, new StringType("Value"))
        waitForAssert { assertThat receivedEvent, not(null) }
        assertThat receivedEvent, is(instanceOf(ItemStateEvent))
        ItemStateEvent itemUpdateEvent = receivedEvent as ItemStateEvent
        assertThat itemUpdateEvent.getTopic(), is("smarthome/items/name/state")
        assertThat itemUpdateEvent.getItemName(), is(itemName)
        assertThat itemUpdateEvent.getSource(), is(CHANNEL_UID.toString())
        assertThat itemUpdateEvent.getItemState(), is(instanceOf(StringType))
        assertThat itemUpdateEvent.getItemState(), is("Value")

        receivedEvent = null
        itemUpdateEvent = null
        def thing = ThingBuilder.create(THING_UID).withChannels([
            new Channel(CHANNEL_UID, "Switch")
        ]).build()
        managedThingProvider.update(thing)

        callback.stateUpdated(CHANNEL_UID, new StringType("Value"))
        waitForAssert { assertThat receivedEvent, not(null) }
        assertThat receivedEvent, is(instanceOf(ItemStateEvent))
        itemUpdateEvent = receivedEvent as ItemStateEvent
        assertThat itemUpdateEvent.getTopic(), is("smarthome/items/name/state")
        assertThat itemUpdateEvent.getItemName(), is(itemName)
        assertThat itemUpdateEvent.getSource(), is(CHANNEL_UID.toString())
        assertThat itemUpdateEvent.getItemState(), is(instanceOf(StringType))
        assertThat itemUpdateEvent.getItemState(), is("Value")
        waitForAssert { assertThat thingUpdatedWasCalled, is(true) }
    }

    @Test
    void 'ThingManager handles post command correctly'() {

        def itemName = "name"
        def callback;

        // Create item
        Item item = new StringItem(itemName)
        itemRegistry.add(item)

        managedThingProvider.add(THING)
        managedItemChannelLinkProvider.add(new ItemChannelLink(itemName, CHANNEL_UID))
        def thingHandler = [
            setCallback: {callbackArg -> callback = callbackArg },
            initialize: {},
            dispose: {},
            getThing: {return THING}
        ] as ThingHandler

        def thingHandlerFactory = [
            supportsThingType: { thingTypeUID -> true},
            registerHandler: {thing -> thingHandler },
            unregisterHandler: {thing -> },
            removeThing: {thingUID ->
            }
        ] as ThingHandlerFactory
        registerService(thingHandlerFactory)

        Event receivedEvent = null
        def itemCommandEventSubscriber = [
            receive: { event -> receivedEvent = event },
            getSubscribedEventTypes: { Sets.newHashSet(ItemCommandEvent.TYPE) },
            getEventFilter: { new TopicEventFilter("smarthome/items/.*/command") },
        ] as EventSubscriber
        registerService(itemCommandEventSubscriber)

        // thing manager posts the command to the event bus via EventPublisher
        callback.postCommand(CHANNEL_UID, new StringType("Value"))
        waitForAssert { assertThat receivedEvent, not(null) }
        assertThat receivedEvent, is(instanceOf(ItemCommandEvent))
        ItemCommandEvent itemCommandEvent = receivedEvent as ItemCommandEvent
        assertThat itemCommandEvent.getTopic(), is("smarthome/items/name/command")
        assertThat itemCommandEvent.getItemName(), is(itemName)
        assertThat itemCommandEvent.getSource(), is(CHANNEL_UID.toString())
        assertThat itemCommandEvent.getItemCommand(), is(instanceOf(StringType))
        assertThat itemCommandEvent.getItemCommand(), is("Value")
    }

    @Test
    void 'ThingManager handles thing status updates online and offline correctly'() {
        ThingHandlerCallback callback;

        managedThingProvider.add(THING)
        def thingHandler = [
            setCallback: {callbackArg -> callback = callbackArg },
            initialize: {},
            dispose: {
            },
            getThing: {return THING}
        ] as ThingHandler

        def thingHandlerFactory = [
            supportsThingType: { thingTypeUID -> true},
            registerHandler: {thing -> thingHandler },
            unregisterHandler: {thing -> },
            removeThing: {thingUID ->
            }
        ] as ThingHandlerFactory
        registerService(thingHandlerFactory)

        def statusInfo = ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build()
        callback.statusUpdated(THING, statusInfo)
        assertThat THING.statusInfo, is(statusInfo)

        statusInfo = ThingStatusInfoBuilder.create(ThingStatus.OFFLINE, ThingStatusDetail.NONE).build()
        callback.statusUpdated(THING, statusInfo)
        assertThat THING.statusInfo, is(statusInfo)
    }

    @Test
    void 'ThingManager handles thing status updates uninitialized and initializing correctly'() {
        registerThingTypeProvider()

        def thingHandler = [
            setCallback: {},
            initialize: {},
            dispose: {},
            getThing: {return THING}
        ] as ThingHandler

        def thingHandlerFactory = [
            supportsThingType: { thingTypeUID -> true},
            registerHandler: {thing -> thingHandler },
            unregisterHandler: {thing -> },
            removeThing: {thingUID ->
            }
        ] as ThingHandlerFactory

        registerService(thingHandlerFactory)

        def statusInfo = ThingStatusInfoBuilder.create(ThingStatus.UNINITIALIZED, ThingStatusDetail.NONE).build()
        assertThat THING.statusInfo, is(statusInfo)

        managedThingProvider.add(THING)
        waitForAssert({
            statusInfo = ThingStatusInfoBuilder.create(ThingStatus.INITIALIZING, ThingStatusDetail.NONE).build()
            assertThat THING.statusInfo, is(statusInfo)
        })

        unregisterService(thingHandlerFactory)
        waitForAssert({
            statusInfo = ThingStatusInfoBuilder.create(ThingStatus.UNINITIALIZED, ThingStatusDetail.HANDLER_MISSING_ERROR).build()
            assertThat THING.statusInfo, is(statusInfo)
        })
    }

    @Test
    void 'ThingManager handles thing status update uninitialized with an exception correctly'() {
        def exception = "Some runtime exception occurred!"

        def thingHandler = [
            setCallback: {},
            initialize: {},
            dispose: {},
            getThing: {return THING}
        ] as ThingHandler

        def thingHandlerFactory = [
            supportsThingType: { thingTypeUID -> true},
            registerHandler: { thing -> throw new RuntimeException(exception) }
        ] as ThingHandlerFactory

        registerService(thingHandlerFactory)

        def statusInfo = ThingStatusInfoBuilder.create(ThingStatus.UNINITIALIZED,
                ThingStatusDetail.HANDLER_REGISTERING_ERROR).withDescription(exception).build()
        managedThingProvider.add(THING)
        assertThat THING.statusInfo, is(statusInfo)
    }

    @Test
    void 'ThingManager handles thing updates correctly'() {

        def itemName = "name"
        ThingHandlerCallback callback;

        managedThingProvider.add(THING)
        managedItemChannelLinkProvider.add(new ItemChannelLink(itemName, CHANNEL_UID))
        def thingHandler = [
            setCallback: {callbackArg -> callback = callbackArg },
            thingUpdated: {},
            initialize: {},
            dispose: {},
            getThing: {return THING}
        ] as ThingHandler

        def thingHandlerFactory = [
            supportsThingType: {thingTypeUID -> true},
            registerHandler: {thing -> thingHandler },
            unregisterHandler: {thing -> },
            removeThing: {thingUID ->
            }
        ] as ThingHandlerFactory
        registerService(thingHandlerFactory)

        boolean thingUpdated = false

        ThingRegistry thingRegistry = getService(ThingRegistry)
        def registryChangeListener = [ updated: {old, updated -> thingUpdated = true} ] as RegistryChangeListener

        try {
            thingRegistry.addRegistryChangeListener(registryChangeListener)
            callback.thingUpdated(THING)
            assertThat thingUpdated, is(true)
        } finally {
            thingRegistry.removeRegistryChangeListener(registryChangeListener)
        }
    }

    @Test(expected=IllegalStateException.class)
    void 'ThingManager complains if the managed thing provider cannot handle thing updates'() {

        def itemName = "name"
        ThingHandlerCallback callback;

        managedThingProvider.add(THING)
        managedItemChannelLinkProvider.add(new ItemChannelLink(itemName, CHANNEL_UID))
        def thingHandler = [
            setCallback: {callbackArg -> callback = callbackArg },
            initialize: {},
            dispose: {},
            getThing: { return THING },
            thingUpdated: {
            }
        ] as ThingHandler

        def thingHandlerFactory = [
            supportsThingType: {ThingTypeUID thingTypeUID -> true},
            registerHandler: {thing -> thingHandler },
            unregisterHandler: {thing -> },
            removeThing: {thingUID ->
            }
        ] as ThingHandlerFactory
        registerService(thingHandlerFactory)

        boolean thingUpdated = false

        ThingRegistry thingRegistry = getService(ThingRegistry)
        def registryChangeListener = [ updated: {old, updated -> thingUpdated = true} ] as RegistryChangeListener

        ThingHandlerCallback storedCallback = callback
        managedThingProvider.remove(THING.getUID())

        try {
            thingRegistry.addRegistryChangeListener(registryChangeListener)
            storedCallback.thingUpdated(THING)
            assertThat thingUpdated, is(true)
        } finally {
            thingRegistry.removeRegistryChangeListener(registryChangeListener)
        }
    }

    @Test
    void 'ThingManager posts thing status events if the status of a thing is updated'() {
        registerThingTypeProvider()

        ThingHandlerCallback callback
        ThingStatusInfoEvent receivedEvent

        def thingHandler = [
            setCallback: {callbackArg -> callback = callbackArg },
            initialize: {},
            dispose: {},
            getThing: {return THING}
        ] as ThingHandler

        def thingHandlerFactory = [
            supportsThingType: {thingTypeUID -> true},
            registerHandler: {thing -> thingHandler },
            unregisterHandler: {thing -> },
            removeThing: {thingUID ->
            }
        ] as ThingHandlerFactory
        registerService(thingHandlerFactory)

        def thingStatusEventSubscriber = [
            receive: { event -> receivedEvent = event as ThingStatusInfoEvent },
            getSubscribedEventTypes: { Sets.newHashSet(ThingStatusInfoEvent.TYPE) },
            getEventFilter: { null },
        ] as EventSubscriber
        registerService(thingStatusEventSubscriber)

        // set status to INITIALIZING
        def statusInfo = ThingStatusInfoBuilder.create(ThingStatus.INITIALIZING, ThingStatusDetail.NONE).build()
        ThingStatusInfoEvent event = ThingEventFactory.createStatusInfoEvent(THING.getUID(), statusInfo)
        managedThingProvider.add(THING)

        waitForAssert {assertThat receivedEvent, not(null)}
        assertThat receivedEvent.getType(), is(event.getType())
        assertThat receivedEvent.getPayload(), is(event.getPayload())
        assertThat receivedEvent.getTopic(), is(event.getTopic())
        receivedEvent = null

        // set status to ONLINE
        statusInfo = ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build()
        event = ThingEventFactory.createStatusInfoEvent(THING.getUID(), statusInfo)
        callback.statusUpdated(THING, statusInfo)

        waitForAssert {assertThat receivedEvent, not(null)}
        assertThat receivedEvent.getType(), is(event.getType())
        assertThat receivedEvent.getPayload(), is(event.getPayload())
        assertThat receivedEvent.getTopic(), is(event.getTopic())
        receivedEvent = null

        // set status to OFFLINE
        statusInfo = ThingStatusInfoBuilder.create(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR).build()
        event = ThingEventFactory.createStatusInfoEvent(THING.getUID(), statusInfo)
        callback.statusUpdated(THING, statusInfo)

        waitForAssert {assertThat receivedEvent, not(null)}
        assertThat receivedEvent.getType(), is(event.getType())
        assertThat receivedEvent.getPayload(), is(event.getPayload())
        assertThat receivedEvent.getTopic(), is(event.getTopic())
        receivedEvent = null

        // set status to UNINITIALIZED
        statusInfo = ThingStatusInfoBuilder.create(ThingStatus.UNINITIALIZED, ThingStatusDetail.HANDLER_MISSING_ERROR).build()
        event = ThingEventFactory.createStatusInfoEvent(THING.getUID(), statusInfo)
        unregisterService(thingHandlerFactory)

        waitForAssert {assertThat receivedEvent, not(null)}
        assertThat receivedEvent.getType(), is(event.getType())
        assertThat receivedEvent.getPayload(), is(event.getPayload())
        assertThat receivedEvent.getTopic(), is(event.getTopic())
    }

    @Test
    void 'ThingManager posts thing status changed events if the status of a thing is changed'() {
        ThingHandlerCallback callback
        ThingStatusInfoChangedEvent receivedEvent

        registerThingTypeProvider()

        def thingHandler = [
            setCallback: {callbackArg -> callback = callbackArg },
            initialize: {},
            dispose: {},
            getThing: {THING}
        ] as ThingHandler

        def thingHandlerFactory = [
            supportsThingType: {thingTypeUID -> true},
            registerHandler: {thing -> thingHandler },
            unregisterHandler: {thing -> },
            removeThing: {thingUID ->
            }
        ] as ThingHandlerFactory
        registerService(thingHandlerFactory)

        def thingStatusEventSubscriber = [
            receive: { event -> receivedEvent = event as ThingStatusInfoChangedEvent },
            getSubscribedEventTypes: { Sets.newHashSet(ThingStatusInfoChangedEvent.TYPE) },
            getEventFilter: { null },
        ] as EventSubscriber
        registerService(thingStatusEventSubscriber)

        // add thing (UNINITIALIZED -> INITIALIZING)
        managedThingProvider.add(THING)

        waitForAssert {assertThat receivedEvent, not(null)}
        assertThat receivedEvent.getType(), is(ThingStatusInfoChangedEvent.TYPE)
        assertThat receivedEvent.getTopic(), is("smarthome/things/binding:type:id/statuschanged")
        assertThat receivedEvent.getStatusInfo().getStatus(), is(ThingStatus.INITIALIZING)
        assertThat receivedEvent.getOldStatusInfo().getStatus(), is(ThingStatus.UNINITIALIZED)
        receivedEvent = null

        // set status to ONLINE (INITIALIZING -> ONLINE)
        ThingStatusInfo statusInfo = ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build()
        callback.statusUpdated(THING, statusInfo)

        waitForAssert {assertThat receivedEvent, not(null)}
        assertThat receivedEvent.getType(), is(ThingStatusInfoChangedEvent.TYPE)
        assertThat receivedEvent.getTopic(), is("smarthome/things/binding:type:id/statuschanged")
        assertThat receivedEvent.getStatusInfo().getStatus(), is(ThingStatus.ONLINE)
        assertThat receivedEvent.getOldStatusInfo().getStatus(), is(ThingStatus.INITIALIZING)
        receivedEvent = null

        // set status to ONLINE again
        statusInfo = ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build()
        callback.statusUpdated(THING, statusInfo)

        // make sure no event has been sent
        Thread.sleep(100)
        assertThat receivedEvent, is(null)
    }

    @Test
    void 'ThingManager calls initialize for added Thing correctly'() {
        // register ThingTypeProvider & ConfigurationDescriptionProvider with 'required' parameter
        registerThingTypeProvider()
        registerConfigDescriptionProvider(true)

        ThingHandlerCallback callback;
        def initializedCalled = false;
        def thing = ThingBuilder.create(new ThingUID("binding:type:thingId")).build()
        def thingHandler = [
            setCallback: { callbackArg -> callback = callbackArg },
            initialize: { initializedCalled = true},
            dispose: {},
            getThing: {-> return thing}
        ] as ThingHandler

        def thingHandlerFactory = [
            supportsThingType: {thingTypeUID -> true},
            registerHandler: {thingArg -> thingHandler},
            unregisterHandler: {},
            removeThing: {
            }
        ] as ThingHandlerFactory
        registerService(thingHandlerFactory)

        def statusInfo = ThingStatusInfoBuilder.create(ThingStatus.UNINITIALIZED, ThingStatusDetail.NONE).build()
        assertThat thing.getStatusInfo(), is(statusInfo)

        // add thing with empty configuration
        managedThingProvider.add(thing)

        // ThingHandler.initialize() not called, thing status is UNINITIALIZED.HANDLER_CONFIGURATION_PENDING
        statusInfo = ThingStatusInfoBuilder.create(ThingStatus.UNINITIALIZED, ThingStatusDetail.HANDLER_CONFIGURATION_PENDING).build()
        assertThat initializedCalled, is(false)
        assertThat thing.getStatusInfo(), is(statusInfo)

        // set required configuration parameter
        thing.configuration = [parameter: "value"] as Map
        statusInfo = ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build()
        callback.configurationUpdated(thing);
        callback.statusUpdated(thing, statusInfo)

        // ThingHandler.initialize() called, thing status is ONLINE.NONE
        waitForAssert({
            assertThat initializedCalled, is(true)
            assertThat thing.getStatusInfo(), is(statusInfo)
        }, 4000)
    }

    @Test
    void 'ThingManager waits with initialize until bundle processing is finished'() {
        registerThingTypeProvider()

        ThingHandlerCallback callback;
        def initializedCalled = false;
        def thing = ThingBuilder.create(new ThingUID("binding:type:thingId")).build()
        def thingHandler = [
            setCallback: { callbackArg -> callback = callbackArg },
            initialize: { initializedCalled = true},
            dispose: {},
            getThing: {-> return thing},
        ] as ThingHandler

        def thingHandlerFactory = [
            supportsThingType: {thingTypeUID -> true},
            registerHandler: {thingArg -> thingHandler},
            unregisterHandler: {},
            removeThing: {
            }
        ] as ThingHandlerFactory
        registerService(thingHandlerFactory)

        boolean finished = false;
        Bundle bundle = null;
        BundleProcessorListener listener = null;
        def bundleProcessor = [
            "hasFinishedLoading": { object -> bundle = object; return finished },
            "registerListener": {l -> listener = l},
            "unregisterListener": {l -> listener = null}
        ] as BundleProcessor
        registerService(bundleProcessor)

        def statusInfo = ThingStatusInfoBuilder.create(ThingStatus.UNINITIALIZED, ThingStatusDetail.NONE).build()
        assertThat thing.getStatusInfo(), is(statusInfo)

        managedThingProvider.add(thing)

        assertThat initializedCalled, is(false)
        assertThat thing.getStatusInfo(), is(statusInfo)

        finished = true;
        listener.bundleFinished(bundleProcessor, bundle)

        // ThingHandler.initialize() called, thing status is INITIALIZING.NONE
        statusInfo = ThingStatusInfoBuilder.create(ThingStatus.INITIALIZING, ThingStatusDetail.NONE).build()
        waitForAssert({
            assertThat initializedCalled, is(true)
            assertThat thing.getStatusInfo(), is(statusInfo)
        }, 4000)
    }

    @Test
    void 'ThingManager calls bridgeStatusChanged on ThingHandler correctly'() {
        ThingHandlerCallback bridgeCallback;
        ThingHandlerCallback thingCallback;

        def bridge = BridgeBuilder.create(new ThingTypeUID("binding:type"), new ThingUID("binding:type:bridgeUID-1")).build()
        def bridgeHandler = [
            setCallback: {callbackArg -> bridgeCallback = callbackArg },
            initialize: {
                bridgeCallback.statusUpdated(bridge, ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build())
            },
            dispose: {},
            childHandlerInitialized: {handler, thing ->},
            childHandlerDisposed: {handler, thing ->},
            getThing: {-> return bridge}
        ] as BridgeHandler

        def bridgeStatusChangedCalled = false
        def thing = ThingBuilder.create(new ThingTypeUID("binding:type"), new ThingUID("binding:type:thingUID-1")).withBridge(bridge.getUID()).build()
        def thingHandler = [
            setCallback: {callbackArg -> thingCallback = callbackArg },
            initialize: {
                thingCallback.statusUpdated(thing, ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build())
            },
            dispose: {},
            bridgeStatusChanged: {bridgeStatusChangedCalled = true},
            getThing: {-> return thing},
        ] as ThingHandler

        def thingHandlerFactory = [
            supportsThingType: {thingTypeUID -> true},
            registerHandler: {thingArg -> (thingArg instanceof Bridge) ? bridgeHandler : thingHandler },
            unregisterHandler: {thingArg -> },
            removeThing: {thingUID ->
            }
        ] as ThingHandlerFactory
        registerService(thingHandlerFactory)

        managedThingProvider.add(bridge)
        managedThingProvider.add(thing)

        waitForAssert({assertThat bridge.getStatus(), is(ThingStatus.ONLINE)})
        waitForAssert({assertThat thing.getStatus(), is(ThingStatus.ONLINE)})

        // initial bridge initialization is not reported as status change
        waitForAssert({ assertThat bridgeStatusChangedCalled, is(false)})

        // the same status is also not reported, because it's not a change
        def statusInfo = ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build()
        bridgeCallback.statusUpdated(bridge, statusInfo)
        waitForAssert({assertThat bridgeStatusChangedCalled, is(false)})

        // report a change to OFFLINE
        statusInfo = ThingStatusInfoBuilder.create(ThingStatus.OFFLINE, ThingStatusDetail.NONE).build()
        bridgeCallback.statusUpdated(bridge, statusInfo)
        waitForAssert({assertThat bridgeStatusChangedCalled, is(true)})
        bridgeStatusChangedCalled = false;

        // report a change to ONLINE
        statusInfo = ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build()
        bridgeCallback.statusUpdated(bridge, statusInfo)
        waitForAssert({assertThat bridgeStatusChangedCalled, is(true)})
        bridgeStatusChangedCalled = false;

        statusInfo = ThingStatusInfoBuilder.create(ThingStatus.REMOVED, ThingStatusDetail.NONE).build()
        bridgeCallback.statusUpdated(bridge, statusInfo)
        waitForAssert({assertThat bridgeStatusChangedCalled, is(false)})
    }

    @Test
    void 'ThingManager calls childHandlerInitialized and childHandlerDisposed on BridgeHandler correctly'() {
        ThingHandlerCallback callback

        def childHandlerInitializedCalled = false
        def initializedHandler = null
        def initializedThing = null
        def childHandlerDisposedCalled = false
        def disposedHandler = null
        def disposedThing = null

        def bridge = BridgeBuilder.create(new ThingTypeUID("binding:type"), new ThingUID("binding:type:bridgeUID-1")).build()
        def bridgeHandler = [
            setCallback: { callbackArg -> callback = callbackArg },
            initialize: {
                callback.statusUpdated(bridge, ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build())
            },
            dispose: {},
            childHandlerInitialized: { thingHandler, thing ->
                childHandlerInitializedCalled = true
                initializedThing = thing
                initializedHandler = thingHandler
            },
            childHandlerDisposed: { thingHandler, thing ->
                childHandlerDisposedCalled = true
                disposedThing = thing
                disposedHandler = thingHandler
            },
            getThing: {-> return bridge}
        ] as BridgeHandler

        def thing = ThingBuilder.create(new ThingTypeUID("binding:type"), new ThingUID("binding:type:thingUID-1")).withBridge(bridge.getUID()).build()
        def thingHandler = [
            setCallback: { callbackArg -> callback = callbackArg },
            initialize: {
                callback.statusUpdated(thing, ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build())
            },
            dispose: {},
            bridgeStatusChanged: {},
            getThing: {-> return thing},
        ] as ThingHandler

        def thingHandlerFactory = [
            supportsThingType: {thingTypeUID -> true},
            registerHandler: {thingArg -> (thingArg instanceof Bridge) ? bridgeHandler : thingHandler },
            unregisterHandler: {thingArg -> },
            removeThing: {thingUID ->
            }
        ] as ThingHandlerFactory
        registerService(thingHandlerFactory)

        managedThingProvider.add(bridge)

        assertThat childHandlerInitializedCalled, is(false)
        assertThat childHandlerDisposedCalled, is(false)

        managedThingProvider.add(thing)
        waitForAssert({assertThat childHandlerInitializedCalled, is(true)})
        assertThat initializedThing, is(thing)
        assertThat initializedHandler, is(thingHandler)

        managedThingProvider.remove(thing.getUID())
        waitForAssert({assertThat childHandlerDisposedCalled, is(true)})
        assertThat disposedThing, is(thing)
        assertThat disposedHandler, is(thingHandler)
    }

    @Test
    void 'ThingManager calls childHandlerInitialized and childHandlerDisposed on BridgeHandler correctly even if child registration takes too long'() {
        ThingHandlerCallback callback

        def childHandlerInitializedCalled = false
        def initializedHandler = null
        def initializedThing = null
        def childHandlerDisposedCalled = false
        def disposedHandler = null
        def disposedThing = null

        def bridge = BridgeBuilder.create(new ThingTypeUID("binding:type"), new ThingUID("binding:type:bridgeUID-1")).build()
        def bridgeHandler = [
            setCallback: { callbackArg -> callback = callbackArg },
            initialize: {
                callback.statusUpdated(bridge, ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build())
            },
            dispose: {},
            childHandlerInitialized: { thingHandler, thing ->
                childHandlerInitializedCalled = true
                initializedThing = thing
                initializedHandler = thingHandler
            },
            childHandlerDisposed: { thingHandler, thing ->
                childHandlerDisposedCalled = true
                disposedThing = thing
                disposedHandler = thingHandler
            },
            getThing: {-> return bridge}
        ] as BridgeHandler

        def thing = ThingBuilder.create(new ThingTypeUID("binding:type"), new ThingUID("binding:type:thingUID-1")).withBridge(bridge.getUID()).build()
        def thingHandler = [
            setCallback: { callbackArg -> callback = callbackArg },
            initialize: {
                callback.statusUpdated(thing, ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build())
            },
            dispose: {},
            bridgeStatusChanged: {},
            getThing: {-> return thing},
        ] as ThingHandler

        def thingHandlerFactory = [
            supportsThingType: {thingTypeUID -> true},
            registerHandler: {thingArg ->
                Thread.sleep(6000) // Wait longer than the SafeMethodCaller timeout
                (thingArg instanceof Bridge) ? bridgeHandler : thingHandler },
            unregisterHandler: {thingArg -> },
            removeThing: {thingUID ->
            }
        ] as ThingHandlerFactory
        registerService(thingHandlerFactory)

        managedThingProvider.add(bridge)

        assertThat childHandlerInitializedCalled, is(false)
        assertThat childHandlerDisposedCalled, is(false)

        managedThingProvider.add(thing)
        waitForAssert({assertThat childHandlerInitializedCalled, is(true)})
        assertThat initializedThing, is(thing)
        assertThat initializedHandler, is(thingHandler)

        managedThingProvider.remove(thing.getUID())
        waitForAssert({assertThat childHandlerDisposedCalled, is(true)})
        assertThat disposedThing, is(thing)
        assertThat disposedHandler, is(thingHandler)
    }

    private void registerThingTypeProvider() {
        def URI configDescriptionUri = new URI("test:test");
        def thingType = new ThingType(new ThingTypeUID("binding", "type"), null, "label", null, null, null, null, configDescriptionUri)

        registerService([
            getThingType: {thingTypeUID,locale -> thingType }
        ] as ThingTypeProvider)

        registerService([
            getThingType:{thingTypeUID -> thingType}
        ] as ThingTypeRegistry)
    }

    private void registerConfigDescriptionProvider(boolean withRequiredParameter = false) {
        def URI configDescriptionUri = new URI("test:test");
        def configDescription = new ConfigDescription(configDescriptionUri, [
            ConfigDescriptionParameterBuilder.create("parameter", ConfigDescriptionParameter.Type.TEXT).withRequired(withRequiredParameter).build()] as List);

        registerService([
            getConfigDescription: {uri, locale -> configDescription}
        ] as ConfigDescriptionProvider)
    }

}
