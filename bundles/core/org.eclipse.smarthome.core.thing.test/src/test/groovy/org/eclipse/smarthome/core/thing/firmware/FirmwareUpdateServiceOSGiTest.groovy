/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.firmware

import static org.eclipse.smarthome.core.thing.firmware.Constants.*
import static org.eclipse.smarthome.core.thing.firmware.FirmwareStatusInfo.*
import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import java.util.concurrent.TimeUnit

import org.eclipse.smarthome.config.core.ConfigDescription
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterBuilder
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider
import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.events.EventFilter
import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.core.thing.binding.firmware.Firmware
import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUpdateBackgroundTransferHandler;
import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUpdateHandler
import org.eclipse.smarthome.core.thing.binding.firmware.ProgressCallback
import org.eclipse.smarthome.core.thing.binding.firmware.ProgressStep;
import org.eclipse.smarthome.core.thing.type.ThingType
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry
import org.eclipse.smarthome.core.types.Command
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.osgi.service.component.ComponentContext

import com.google.common.collect.ImmutableSet

/**
 * Testing the {@link FirmwareUpdateService}.
 *
 * @author Thomas Höfer - Initial contribution
 */
final class FirmwareUpdateServiceOSGiTest extends OSGiTest {

    private static final int WAIT = 250

    private final FirmwareStatusInfo unknownInfo = createUnknownInfo()
    private final FirmwareStatusInfo upToDateInfo = createUpToDateInfo()
    private final FirmwareStatusInfo updateAvailableInfo =  createUpdateAvailableInfo()
    private final FirmwareStatusInfo updateExecutableInfoFw112 = createUpdateExecutableInfo(FW112_EN.getUID())
    private final FirmwareStatusInfo updateExecutableInfoFw113 = createUpdateExecutableInfo(FW113_EN.getUID())
    private final FirmwareStatusInfo updateExecutableInfoFw120 = createUpdateExecutableInfo(FW120_EN.getUID())

    Locale defaultLocale

    @Rule
    public ExpectedException thrown = ExpectedException.none()

    private ManagedThingProvider managedThingProvider
    private ThingRegistry thingRegistry
    private FirmwareRegistry firmwareRegistry
    private FirmwareUpdateService firmwareUpdateService

    def thing1
    def thing2
    def thing3

    private def firmwareProvider = [
        getFirmware: { firmwareUID, locale ->
            if(firmwareUID.equals(FW009_EN.getUID())) {
                FW009_EN
            } else if(firmwareUID.equals(FW111_EN.getUID())) {
                FW111_EN
            } else if(firmwareUID.equals(FW112_EN.getUID())) {
                FW112_EN
            } else {
                null
            }
        },
        getFirmwares: { thingTypeUID, locale ->
            if(thingTypeUID.equals(THING_TYPE_UID_WITHOUT_FW) || thingTypeUID.equals(THING_TYPE_UID2) || thingTypeUID.equals(THING_TYPE_UID3)) {
                return [] as Set
            }
            [
                FW009_EN,
                FW111_EN,
                FW112_EN] as Set
        }] as FirmwareProvider

    private def firmwareStatusInfoEventSubscriber = new EventSubscriber() {

        private final def topics = ImmutableSet.of(FirmwareEventFactory.FIRMWARE_STATUS_TOPIC.replace(FirmwareEventFactory.THING_UID_TOPIC_KEY, THING1_UID.getAsString()),
        FirmwareEventFactory.FIRMWARE_STATUS_TOPIC.replace(FirmwareEventFactory.THING_UID_TOPIC_KEY, THING2_UID.getAsString()),
        FirmwareEventFactory.FIRMWARE_STATUS_TOPIC.replace(FirmwareEventFactory.THING_UID_TOPIC_KEY, THING3_UID.getAsString()),
        FirmwareEventFactory.FIRMWARE_STATUS_TOPIC.replace(FirmwareEventFactory.THING_UID_TOPIC_KEY, THING4_UID.getAsString()),
        FirmwareEventFactory.FIRMWARE_STATUS_TOPIC.replace(FirmwareEventFactory.THING_UID_TOPIC_KEY, UNKNOWN_THING_UID.getAsString()))

        def events = []

        @Override
        public Set<String> getSubscribedEventTypes() {
            return ImmutableSet.copyOf(FirmwareStatusInfoEvent.TYPE)
        }

        @Override
        public EventFilter getEventFilter() {
            return new EventFilter() {
                        @Override
                        boolean apply(Event event) {
                            topics.contains(event.getTopic())
                        };
                    };
        }

        @Override
        public void receive(Event event) {
            events.add(event);
        }
    } as EventSubscriber

    private def progressInfoEventSubscriber = new EventSubscriber() {

        private final def topics = ImmutableSet.of(FirmwareEventFactory.FIRMWARE_UPDATE_PROGRESS_TOPIC.replace(FirmwareEventFactory.THING_UID_TOPIC_KEY, THING1_UID.getAsString()))

        def events = []

        @Override
        public Set<String> getSubscribedEventTypes() {
            return ImmutableSet.copyOf(FirmwareUpdateProgressInfoEvent.TYPE)
        }

        @Override
        public EventFilter getEventFilter() {
            return new EventFilter() {
                        @Override
                        boolean apply(Event event) {
                            topics.contains(event.getTopic())
                        };
                    };
        }

        @Override
        public void receive(Event event) {
            events.add(event);
        }
    } as EventSubscriber

    private def firmwareUpdateResultInfoEventSubscriber = new EventSubscriber() {

        private final def topics = ImmutableSet.of(FirmwareEventFactory.FIRMWARE_UPDATE_RESULT_TOPIC.replace(FirmwareEventFactory.THING_UID_TOPIC_KEY, THING1_UID.getAsString()))

        def events = []

        @Override
        public Set<String> getSubscribedEventTypes() {
            return ImmutableSet.copyOf(FirmwareUpdateResultInfoEvent.TYPE)
        }

        @Override
        public EventFilter getEventFilter() {
            return new EventFilter() {
                        @Override
                        boolean apply(Event event) {
                            topics.contains(event.getTopic())
                        };
                    };
        }

        @Override
        public void receive(Event event) {
            events.add(event);
        }
    } as EventSubscriber

    @Before
    void setup() {
        defaultLocale = Locale.getDefault()
        Locale.setDefault(Locale.ENGLISH)

        registerVolatileStorageService()
        managedThingProvider = getService ManagedThingProvider
        assertThat managedThingProvider, is(notNullValue())
        thingRegistry = getService(ThingRegistry)
        assertThat thingRegistry, is(notNullValue())

        registerThingTypeProvider()
        registerConfigDescriptionProvider()

        firmwareRegistry = getService(FirmwareRegistry)
        assertThat firmwareRegistry, is(notNullValue())

        firmwareUpdateService = getService(FirmwareUpdateService)
        assertThat firmwareUpdateService, is(notNullValue())

        registerService(firmwareProvider, FirmwareProvider.class.getName())
        registerService(firmwareStatusInfoEventSubscriber, EventSubscriber.class.getName())

        def firmwareUpdateThingHandlerFactory = new FirmwareUpdateThingHandlerFactory()
        firmwareUpdateThingHandlerFactory.activate([getBundleContext: { bundleContext }] as ComponentContext)
        registerService(firmwareUpdateThingHandlerFactory, ThingHandlerFactory.class.name)

        thing1 = ThingBuilder.create(THING_TYPE_UID1, THING1_ID).withProperties([(Thing.PROPERTY_FIRMWARE_VERSION) : V111]).build()
        thing2 = ThingBuilder.create(THING_TYPE_UID1, THING2_ID).withProperties([(Thing.PROPERTY_FIRMWARE_VERSION) : V112]).build()
        thing3 = ThingBuilder.create(THING_TYPE_UID2, THING3_ID).withProperties([(Thing.PROPERTY_FIRMWARE_VERSION) : VALPHA]).build()

        managedThingProvider.add(thing1)
        managedThingProvider.add(thing2)
        managedThingProvider.add(thing3)

        waitForAssert {
            assertThat thing1.getStatus(), is(ThingStatus.ONLINE)
            assertThat thing2.getStatus(), is(ThingStatus.ONLINE)
            assertThat thing3.getStatus(), is(ThingStatus.ONLINE)
        }
    }

    @After
    void teardown() {
        Locale.setDefault(defaultLocale)
        managedThingProvider.getAll().each {
            managedThingProvider.remove(it.getUID())
        }
        unregisterMocks()
    }

    @Test
    void 'assert that BaseThingHandlerFactory registers firmware update handler'() {
        def handler = thing1.getHandler()
        assertThat handler, is(not(null))

        def firmwareUpdateHandler = getService(FirmwareUpdateHandler)
        assertThat firmwareUpdateHandler, is(handler)
    }

    @Test
    void 'assert that correct firmware status is provided'() {
        assertThat thing1.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V111)
        assertThat thing2.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V112)

        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw112)
        assertFirmwareStatusInfoEvent(THING1_UID, updateExecutableInfoFw112)

        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING2_UID), is(upToDateInfo)
        assertFirmwareStatusInfoEvent(THING2_UID, upToDateInfo)

        thing2.getHandler().updateProperty(Thing.PROPERTY_FIRMWARE_VERSION, null)
        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING2_UID), is(unknownInfo)
        assertFirmwareStatusInfoEvent(THING2_UID, unknownInfo)
    }

    @Test
    void 'assert that firmware status is unknown if no firmware provider is available'() {
        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING3_UID), is(unknownInfo)
        assertFirmwareStatusInfoEvent(THING3_UID, unknownInfo)
    }

    @Test
    void 'assert that firmware status is changed once a firmware provider is available'() {
        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING3_UID), is(unknownInfo)
        assertFirmwareStatusInfoEvent(THING3_UID, unknownInfo)

        def firmwareProvider2 = [
            getFirmware: { firmwareUID, locale ->
                if(firmwareUID.equals(FWALPHA_EN.getUID())) {
                    FWALPHA_EN
                } else {
                    null
                }
            },
            getFirmwares: { thingTypeUID, locale ->
                if(thingTypeUID.equals(THING_TYPE_UID_WITHOUT_FW) || thingTypeUID.equals(THING_TYPE_UID1)) {
                    return [] as Set
                }
                [
                    FWALPHA_EN] as Set
            }] as FirmwareProvider

        registerService(firmwareProvider2, FirmwareProvider.class.getName())

        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING3_UID), is(upToDateInfo)
        assertFirmwareStatusInfoEvent(THING3_UID, upToDateInfo)
    }

    @Test
    void 'assert that firmware status change is only propagated once'() {
        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw112)
        assertFirmwareStatusInfoEvent(THING1_UID, updateExecutableInfoFw112)

        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw112)
        assertThatNoFirmwareStatusInfoEventWasPropagated()
    }

    @Test
    void 'assert that firmware status is propagated regularly through job'() {
        assertThat thing1.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V111)
        assertThat thing2.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V112)

        firmwareUpdateService.deactivate()
        firmwareUpdateService.firmwareStatusInfoJobTimeUnit = TimeUnit.SECONDS
        final int waitForNextJobExecution = 1000
        firmwareUpdateService.activate()

        final int expectedEventsBecauseOfInitialPropagation = 3

        waitForAssert({
            assertThat firmwareStatusInfoEventSubscriber.events.size(), is(expectedEventsBecauseOfInitialPropagation)
            assertThat firmwareStatusInfoEventSubscriber.events, hasItem(FirmwareEventFactory.createFirmwareStatusInfoEvent(updateExecutableInfoFw112, THING1_UID))
            assertThat firmwareStatusInfoEventSubscriber.events, hasItem(FirmwareEventFactory.createFirmwareStatusInfoEvent(upToDateInfo, THING2_UID))
            assertThat firmwareStatusInfoEventSubscriber.events, hasItem(FirmwareEventFactory.createFirmwareStatusInfoEvent(unknownInfo, THING3_UID))
        }, WAIT)
        firmwareStatusInfoEventSubscriber.events = []

        def firmwareProvider2 = [
            getFirmware: { firmwareUID, locale -> null },
            getFirmwares: { thingTypeUID, locale ->
                if(thingTypeUID.equals(THING_TYPE_UID_WITHOUT_FW) || thingTypeUID.equals(THING_TYPE_UID2)) {
                    return [] as Set
                }
                [FW113_EN] as Set
            }] as FirmwareProvider

        registerService(firmwareProvider2, FirmwareProvider.class.getName())

        Thread.sleep(waitForNextJobExecution)

        final int expectedEventsBecauseOfStatusChange = 2

        waitForAssert({
            assertThat firmwareStatusInfoEventSubscriber.events.size(), is(expectedEventsBecauseOfStatusChange)
            assertThat firmwareStatusInfoEventSubscriber.events, hasItem(FirmwareEventFactory.createFirmwareStatusInfoEvent(updateExecutableInfoFw113, THING1_UID))
            assertThat firmwareStatusInfoEventSubscriber.events, hasItem(FirmwareEventFactory.createFirmwareStatusInfoEvent(updateExecutableInfoFw113, THING2_UID))
        }, WAIT)
        firmwareStatusInfoEventSubscriber.events = []

        unregisterService(firmwareProvider2)

        Thread.sleep(waitForNextJobExecution)

        waitForAssert({
            assertThat firmwareStatusInfoEventSubscriber.events.size(), is(expectedEventsBecauseOfStatusChange)
            assertThat firmwareStatusInfoEventSubscriber.events, hasItem(FirmwareEventFactory.createFirmwareStatusInfoEvent(updateExecutableInfoFw112, THING1_UID))
            assertThat firmwareStatusInfoEventSubscriber.events, hasItem(FirmwareEventFactory.createFirmwareStatusInfoEvent(upToDateInfo, THING2_UID))
        }, WAIT)

        firmwareUpdateService.deactivate()
        firmwareRegistry.firmwareProviders.clear()
    }

    @Test
    void 'assert that firmware upgrade works'() {
        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw112)
        assertFirmwareStatusInfoEvent(THING1_UID, updateExecutableInfoFw112)

        firmwareUpdateService.updateFirmware(THING1_UID, FW112_EN.getUID(), null)

        waitForAssert({
            assertThat thing1.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V112.toString())
        }, WAIT)

        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(upToDateInfo)
        assertFirmwareStatusInfoEvent(THING1_UID, upToDateInfo)
    }

    @Test
    void 'assert that firmware downgrade works'() {
        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING2_UID), is(upToDateInfo)
        assertFirmwareStatusInfoEvent(THING2_UID, upToDateInfo)

        firmwareUpdateService.updateFirmware(THING2_UID, FW111_EN.getUID(), null)

        waitForAssert({
            assertThat thing2.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V111.toString())
        }, WAIT)

        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING2_UID), is(updateExecutableInfoFw112)
        assertFirmwareStatusInfoEvent(THING2_UID, updateExecutableInfoFw112)
    }

    @Test(expected=NullPointerException)
    void 'assert that given thing uid is checked for getFirmwareStatus operation'() {
        firmwareUpdateService.getFirmwareStatusInfo(null)
    }

    @Test(expected=NullPointerException)
    void 'assert that given thing uid is checked for updateFirmware operation'() {
        firmwareUpdateService.updateFirmware(null, FW009_EN.getUID(), null)
    }

    @Test(expected=NullPointerException)
    void 'assert that given firmware uid is checked for updateFirmware operation'() {
        firmwareUpdateService.updateFirmware(THING1_UID, null, null)
    }

    @Test
    void 'assert that get firmware status is null for things whose handler is not a firmware update handler'() {
        assertThat firmwareUpdateService.getFirmwareStatusInfo(UNKNOWN_THING_UID), is(null)
    }

    @Test
    void 'assert that firmware update is rejected if firmware does not exist'() {
        thrown.expect(IllegalArgumentException.class)
        thrown.expectMessage(is(String.format("Firmware with UID %s was not found.", UNKNOWN_FIRMWARE_UID)))

        firmwareUpdateService.updateFirmware(THING1_UID, UNKNOWN_FIRMWARE_UID, null)
    }

    @Test
    void 'assert that firmware update is rejected for things without a firmware update handler'() {
        def thingHandlerFactory2 = new NonFirmwareUpdateThingHandlerFactory()
        thingHandlerFactory2.activate([getBundleContext: { bundleContext }] as ComponentContext)
        registerService(thingHandlerFactory2, ThingHandlerFactory.class.name)

        def thing4 = ThingBuilder.create(THING_TYPE_UID_WITHOUT_FW, THING5_ID).build()

        managedThingProvider.add(thing4)

        assertThat thing4.getHandler(), is(notNullValue())

        thrown.expect(IllegalStateException.class)
        thrown.expectMessage(is(String.format("There is no firmware update handler for thing with UID %s.", thing4.getUID())))

        firmwareUpdateService.updateFirmware(thing4.getUID(), FW009_EN.getUID(), null)
    }

    @Test
    void 'assert that firmware update is rejected if update is not executable'() {
        thing1.getHandler().updateExecutable = false

        thrown.expect(IllegalStateException.class)
        thrown.expectMessage(is(String.format("The firmware update of thing with UID %s is not executable.", THING1_UID)))

        firmwareUpdateService.updateFirmware(THING1_UID, FW112_EN.getUID(), null)
    }

    @Test
    void 'assert that prerequisite version is checked'() {
        def firmwareProvider2 = [
            getFirmware: { firmwareUID, locale ->
                if(firmwareUID.equals(FW111_FIX_EN.getUID())) {
                    FW111_FIX_EN
                } else if(firmwareUID.equals(FW113_EN.getUID())) {
                    FW113_EN
                } else {
                    null
                }
            },
            getFirmwares: { thingTypeUID, locale ->
                if(thingTypeUID.equals(THING_TYPE_UID_WITHOUT_FW) || thingTypeUID.equals(THING_TYPE_UID2)) {
                    return [] as Set
                }
                [
                    FW111_FIX_EN,
                    FW113_EN] as Set
            }] as FirmwareProvider

        registerService(firmwareProvider2, FirmwareProvider.class.getName())

        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw113)
        assertFirmwareStatusInfoEvent(THING1_UID, updateExecutableInfoFw113)

        try {
            firmwareUpdateService.updateFirmware(THING1_UID, FW113_EN.getUID(), null)
            fail "Expeced an IllegalArgumentException, but it was not thrown."
        } catch (IllegalArgumentException expected) {
            assertThat expected.getMessage(), is(String.format(
                    "Firmware with UID %s requires at least firmware version %s to get installed. But the current firmware version of the thing with UID %s is %s.",
                    FW113_EN.getUID(), FW113_EN.getPrerequisiteVersion(), THING1_UID, V111))
        }

        firmwareUpdateService.updateFirmware(THING1_UID, FW111_FIX_EN.getUID(), null)

        waitForAssert({
            assertThat thing1.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V111_FIX)
        }, WAIT)

        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw113)
        assertThatNoFirmwareStatusInfoEventWasPropagated()

        firmwareUpdateService.updateFirmware(THING1_UID, FW113_EN.getUID(), null)

        waitForAssert({
            assertThat thing1.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V113)
        }, WAIT)

        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(upToDateInfo)
        assertFirmwareStatusInfoEvent(THING1_UID, upToDateInfo)

        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING2_UID), is(updateExecutableInfoFw113)
        assertFirmwareStatusInfoEvent(THING2_UID, updateExecutableInfoFw113)

        firmwareUpdateService.updateFirmware(THING2_UID, FW113_EN.getUID(), null)

        waitForAssert({
            assertThat thing2.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V113)
        }, WAIT)

        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING2_UID), is(upToDateInfo)
        assertFirmwareStatusInfoEvent(THING2_UID, upToDateInfo)
    }

    @Test
    void 'assert that firmware status is available instead of executable if update is not executable'() {
        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw112)
        assertFirmwareStatusInfoEvent(THING1_UID, updateExecutableInfoFw112)

        thing1.getHandler().updateExecutable = false;
        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateAvailableInfo)
        assertFirmwareStatusInfoEvent(THING1_UID, updateAvailableInfo)

        thing1.getHandler().updateExecutable = true;
        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw112)
        assertFirmwareStatusInfoEvent(THING1_UID, updateExecutableInfoFw112)

        firmwareUpdateService.updateFirmware(THING1_UID, FW112_EN.getUID(), null)

        waitForAssert({
            assertThat thing1.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V112)
        }, WAIT)

        assertThat thing1.getHandler().isUpdateExecutable(), is(true)
    }

    @Test
    void 'assert that firmware update is rejected if firmware is not suitable for thing'() {
        def firmwareProvider2 = [
            getFirmware: { firmwareUID, locale ->
                if(firmwareUID.equals(FWALPHA_EN.getUID())) {
                    FWALPHA_EN
                } else {
                    null
                }
            },
            getFirmwares: { thingTypeUID, locale ->
                if(thingTypeUID.equals(THING_TYPE_UID_WITHOUT_FW) || thingTypeUID.equals(THING_TYPE_UID1)) {
                    return [] as Set
                }
                [FWALPHA_EN] as Set
            }] as FirmwareProvider

        registerService(firmwareProvider2, FirmwareProvider.class.getName())

        thrown.expect(IllegalArgumentException.class)
        thrown.expectMessage(is(String.format("Firmware with UID %s is not suitable for thing with UID %s.", FWALPHA_EN.getUID(),
                THING1_UID)))

        firmwareUpdateService.updateFirmware(THING1_UID, FWALPHA_EN.getUID(), null)
    }

    @Test
    void 'assert that progress info and result events are sent'() {
        registerService(progressInfoEventSubscriber, EventSubscriber.class.getName())
        registerService(firmwareUpdateResultInfoEventSubscriber, EventSubscriber.class.getName())

        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw112)
        assertFirmwareStatusInfoEvent(THING1_UID, updateExecutableInfoFw112)

        firmwareUpdateService.updateFirmware(THING1_UID, FW112_EN.getUID(), null)

        waitForAssert({
            assertThat progressInfoEventSubscriber.events.size(), is(thing1.getHandler().sequence.length)
            def seqList = thing1.getHandler().sequence as List
            int index = 0
            for(FirmwareUpdateProgressInfoEvent progressInfoEvent in progressInfoEventSubscriber.events) {
                assertThat progressInfoEvent.getTopic(), containsString(THING1_UID.getAsString())
                assertThat progressInfoEvent.getThingUID(), is(THING1_UID)
                assertThat progressInfoEvent.getProgressInfo().getProgressStep(), is(thing1.getHandler().sequence[index])
                assertThat progressInfoEvent.getProgressInfo().getSequence(), is(seqList)
                index++
            }
        }, WAIT)
        progressInfoEventSubscriber.events.clear()

        waitForAssert({
            assertThat firmwareUpdateResultInfoEventSubscriber.events.size(), is(1)
            FirmwareUpdateResultInfoEvent firmwareUpdateResultInfoEvent = (FirmwareUpdateResultInfoEvent) firmwareUpdateResultInfoEventSubscriber.events [0]
            assertThat firmwareUpdateResultInfoEvent.getTopic(), containsString(THING1_UID.getAsString())
            assertThat firmwareUpdateResultInfoEvent.getThingUID(), is(THING1_UID)
            assertThat firmwareUpdateResultInfoEvent.getFirmwareUpdateResultInfo().getResult(), is(FirmwareUpdateResult.SUCCESS)
            assertThat firmwareUpdateResultInfoEvent.getFirmwareUpdateResultInfo().getErrorMessage(), is(null)
        }, WAIT)
        firmwareUpdateResultInfoEventSubscriber.events.clear()

        waitForAssert({
            assertThat thing1.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V112.toString())
        }, WAIT)

        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(upToDateInfo)
        assertFirmwareStatusInfoEvent(THING1_UID, upToDateInfo)
    }

    @Test
    void 'assert that update firmware handles time out'() {
        registerService(firmwareUpdateResultInfoEventSubscriber, EventSubscriber.class.getName())

        def timeout = firmwareUpdateService.timeout
        firmwareUpdateService.timeout = WAIT

        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw112)
        assertFirmwareStatusInfoEvent(THING1_UID, updateExecutableInfoFw112)

        def longwait = 10000
        thing1.getHandler().sleep = longwait

        def enMsg = "A timeout occurred during firmware update."

        firmwareUpdateService.updateFirmware(THING1_UID, FW112_EN.getUID(), null)
        assertFailedFirmwareUpdate(enMsg)

        firmwareUpdateService.updateFirmware(THING1_UID, FW112_EN.getUID(), Locale.ENGLISH)
        assertFailedFirmwareUpdate(enMsg)

        firmwareUpdateService.updateFirmware(THING1_UID, FW112_EN.getUID(), Locale.GERMAN)
        assertFailedFirmwareUpdate("Das Firmware-Update ist aufgrund einer Zeitüberschreitung fehlgeschlagen.")

        firmwareUpdateService.timeout = timeout
    }

    @Test
    void 'assert that update firmware handles general handler error'() {
        registerService(firmwareUpdateResultInfoEventSubscriber, EventSubscriber.class.getName())

        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw112)
        assertFirmwareStatusInfoEvent(THING1_UID, updateExecutableInfoFw112)

        thing1.getHandler().exception = true

        def enMsg = "An unexpected error occurred during firmware update."

        firmwareUpdateService.updateFirmware(THING1_UID, FW112_EN.getUID(), null)
        assertFailedFirmwareUpdate("An unexpected error occurred during firmware update.")

        firmwareUpdateService.updateFirmware(THING1_UID, FW112_EN.getUID(), Locale.ENGLISH)
        assertFailedFirmwareUpdate("An unexpected error occurred during firmware update.")

        firmwareUpdateService.updateFirmware(THING1_UID, FW112_EN.getUID(), Locale.GERMAN)
        assertFailedFirmwareUpdate("Es ist ein unerwarteter Fehler während des Firmware-Updates aufgetreten.")

        assertThat thing1.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V111.toString())
        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw112)
        assertThatNoFirmwareStatusInfoEventWasPropagated()
    }

    @Test
    void 'assert that handler can set error message for failed firmware update'() {
        registerService(firmwareUpdateResultInfoEventSubscriber, EventSubscriber.class.getName())

        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw112)
        assertFirmwareStatusInfoEvent(THING1_UID, updateExecutableInfoFw112)

        thing1.getHandler().fail = true

        firmwareUpdateService.updateFirmware(THING1_UID, FW112_EN.getUID(), null)
        assertFailedFirmwareUpdate("Error")

        firmwareUpdateService.updateFirmware(THING1_UID, FW112_EN.getUID(), Locale.ENGLISH)
        assertFailedFirmwareUpdate("Error")

        firmwareUpdateService.updateFirmware(THING1_UID, FW112_EN.getUID(), Locale.GERMAN)
        assertFailedFirmwareUpdate("Fehler")

        waitForAssert({
            assertThat thing1.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V111.toString())
        }, WAIT)

        assertThat thing1.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V111.toString())
        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw112)
        assertThatNoFirmwareStatusInfoEventWasPropagated()
    }

    @Test
    void 'assert that firmware update handlers can be used that are not thing handlers'() {
        def expectedFirmwareUpdateHandlers = 3

        assertThat firmwareUpdateService.firmwareUpdateHandlers.size(), is(expectedFirmwareUpdateHandlers)

        def firmwareUpdateHandler = [
            getThing: {
                [ getUID: { UNKNOWN_THING_UID },
                    getThingTypeUID: { UNKNOWN_THING_TYPE_UID },
                    getProperties: { [:] }] as Thing
            },
            isUpdatable: { false } ] as FirmwareUpdateHandler

        registerService(firmwareUpdateHandler)

        def service = getService(FirmwareUpdateHandler) {
            bundleContext.getService(it).equals(firmwareUpdateHandler)
        }

        assertThat service, is(notNullValue())
        assertThat firmwareUpdateService.firmwareUpdateHandlers.size(), is(expectedFirmwareUpdateHandlers + 1)

        assertThat firmwareUpdateService.getFirmwareStatusInfo(UNKNOWN_THING_UID), is(unknownInfo)

        unregisterService(firmwareUpdateHandler)
    }

    @Test
    void 'assert that background transfer is triggered'() {
        def firmwareUpdateBackgroundTransferThingHandlerFactory = new FirmwareUpdateBackgroundTransferThingHandlerFactory()
        firmwareUpdateBackgroundTransferThingHandlerFactory.activate([getBundleContext: { bundleContext }] as ComponentContext)
        registerService(firmwareUpdateBackgroundTransferThingHandlerFactory, ThingHandlerFactory.class.name)

        def thing4 = ThingBuilder.create(THING_TYPE_UID3, THING4_ID).withProperties([(Thing.PROPERTY_FIRMWARE_VERSION) : V111]).build()

        managedThingProvider.add(thing4)

        waitForAssert {
            assertThat thing4.getStatus(), is(ThingStatus.ONLINE)
        }

        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING4_UID), is(unknownInfo)
        assertFirmwareStatusInfoEvent(THING4_UID, unknownInfo)

        def firmwareProvider2 = [
            getFirmware: { firmwareUID, locale ->
                if(firmwareUID.equals(FW120_EN.getUID())) {
                    FW120_EN
                } else {
                    null
                }
            },
            getFirmwares: { thingTypeUID, locale ->
                if(!thingTypeUID.equals(THING_TYPE_UID3)) {
                    return [] as Set
                }
                [FW120_EN] as Set
            }] as FirmwareProvider

        registerService(firmwareProvider2, FirmwareProvider.class.getName())

        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING4_UID), is(updateAvailableInfo)
        assertFirmwareStatusInfoEvent(THING4_UID, updateAvailableInfo)

        waitForAssert ({
            assertThat firmwareUpdateService.getFirmwareStatusInfo(THING4_UID), is(updateExecutableInfoFw120)
            assertFirmwareStatusInfoEvent(THING4_UID, updateExecutableInfoFw120)
        }, WAIT)

        firmwareUpdateService.updateFirmware(THING4_UID, FW120_EN.getUID(), null)

        waitForAssert({
            assertThat thing4.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V120)
        }, WAIT)

        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING4_UID), is(upToDateInfo)
        assertFirmwareStatusInfoEvent(THING4_UID, upToDateInfo)

        assertThat thing4.getHandler().isUpdateExecutable(), is(false)
    }

    private void registerThingTypeProvider() {
        def thingType = new ThingType(THING_TYPE_UID1, null, "label", null, null, null, null, CONFIG_URI)
        def thingTypeWithoutFW = new ThingType(THING_TYPE_UID_WITHOUT_FW, null, "label", null, null, null, null, CONFIG_URI)

        registerService([
            getThingType: { thingTypeUID,locale ->
                thingTypeUID.equals(THING_TYPE_UID1) ? thingType : thingTypeWithoutFW
            }
        ] as ThingTypeProvider)

        registerService([
            getThingType:{ thingTypeUID ->
                thingTypeUID.equals(THING_TYPE_UID1) ? thingType : thingTypeWithoutFW
            }
        ] as ThingTypeRegistry)
    }

    private void registerConfigDescriptionProvider() {
        def configDescription = new ConfigDescription(CONFIG_URI, [
            ConfigDescriptionParameterBuilder.create("parameter", ConfigDescriptionParameter.Type.TEXT).build()] as List);

        registerService([
            getConfigDescription: { uri, locale -> configDescription }
        ] as ConfigDescriptionProvider)
    }

    private void assertThatNoFirmwareStatusInfoEventWasPropagated() {
        waitForAssert({
            assertThat firmwareStatusInfoEventSubscriber.events.size(), is(0)
        }, WAIT)
    }

    private void assertFirmwareStatusInfoEvent(ThingUID thingUID, FirmwareStatusInfo expected) {
        final int onlyOneEventExpected = 1

        waitForAssert({
            assertThat firmwareStatusInfoEventSubscriber.events.size(), is(onlyOneEventExpected)
            FirmwareStatusInfoEvent firmwareStatusInfoEvent = (FirmwareStatusInfoEvent) firmwareStatusInfoEventSubscriber.events [0]
            assertThat firmwareStatusInfoEvent.getTopic(), containsString(thingUID.getAsString())
            assertThat firmwareStatusInfoEvent.getThingUID(), is(thingUID)
            assertThat firmwareStatusInfoEvent.getFirmwareStatusInfo(), is(expected)
        }, WAIT)

        firmwareStatusInfoEventSubscriber.events.clear()
    }

    def assertFailedFirmwareUpdate(def expectedErrorMessage) {
        waitForAssert({
            assertThat firmwareUpdateResultInfoEventSubscriber.events.size(), is(1)
            FirmwareUpdateResultInfoEvent firmwareUpdateResultInfoEvent = (FirmwareUpdateResultInfoEvent) firmwareUpdateResultInfoEventSubscriber.events [0]
            assertThat firmwareUpdateResultInfoEvent.getTopic(), containsString(THING1_UID.getAsString())
            assertThat firmwareUpdateResultInfoEvent.getThingUID(), is(THING1_UID)
            assertThat firmwareUpdateResultInfoEvent.getFirmwareUpdateResultInfo().getResult(), is(FirmwareUpdateResult.ERROR)
            assertThat firmwareUpdateResultInfoEvent.getFirmwareUpdateResultInfo().getErrorMessage(), is(expectedErrorMessage)
        })

        firmwareUpdateResultInfoEventSubscriber.events.clear()
    }

    final class FirmwareUpdateThingHandlerFactory extends BaseThingHandlerFactory {

        @Override
        public boolean supportsThingType(ThingTypeUID thingTypeUID) {
            thingTypeUID.equals(THING_TYPE_UID1) || thingTypeUID.equals(THING_TYPE_UID2)
        }

        @Override
        protected ThingHandler createHandler(Thing thing) {
            return new FirmwareUpdateThingHandler(thing)
        }
    }

    final class FirmwareUpdateThingHandler extends BaseThingHandler implements FirmwareUpdateHandler {

        def sequence = [
            ProgressStep.REBOOTING,
            ProgressStep.DOWNLOADING,
            ProgressStep.TRANSFERRING,
            ProgressStep.UPDATING
        ].toArray(new ProgressStep[0])

        int sleep = 25
        boolean updateExecutable = true
        boolean exception = false
        boolean fail = false

        FirmwareUpdateThingHandler(Thing thing) {
            super(thing)
        }

        @Override
        public void handleCommand(ChannelUID channelUID, Command command) {
        }

        @Override
        public void updateFirmware(Firmware firmware, ProgressCallback progressCallback) {
            progressCallback.defineSequence(sequence)

            if(fail) {
                progressCallback.failed("error")
                return
            }

            Thread.sleep(sleep)

            progressCallback.next()
            progressCallback.next()
            progressCallback.next()
            progressCallback.next()

            if(exception) {
                try {
                    progressCallback.next()
                } catch (NoSuchElementException e) {
                    fail "Unexcepted exception thrown"
                }
            }

            updateProperty(Thing.PROPERTY_FIRMWARE_VERSION, firmware.getVersion())

            progressCallback.success()
        }

        @Override
        public boolean isUpdateExecutable() {
            return updateExecutable;
        }
    }

    final class FirmwareUpdateBackgroundTransferThingHandlerFactory extends BaseThingHandlerFactory {

        @Override
        public boolean supportsThingType(ThingTypeUID thingTypeUID) {
            thingTypeUID.equals(THING_TYPE_UID3)
        }

        @Override
        protected ThingHandler createHandler(Thing thing) {
            return new FirmwareUpdateBackgroundTransferThingHandler(thing)
        }
    }

    final class FirmwareUpdateBackgroundTransferThingHandler extends BaseThingHandler implements FirmwareUpdateBackgroundTransferHandler {

        int sleep = 25
        boolean updateExecutable = false
        boolean transferred = false

        FirmwareUpdateBackgroundTransferThingHandler(Thing thing) {
            super(thing)
        }

        @Override
        public void handleCommand(ChannelUID channelUID, Command command) {
        }

        @Override
        public void updateFirmware(Firmware firmware, ProgressCallback progressCallback) {
            Thread.sleep(sleep)
            updateProperty(Thing.PROPERTY_FIRMWARE_VERSION, firmware.getVersion())
            updateExecutable = false;
        }

        @Override
        public boolean isUpdateExecutable() {
            return updateExecutable;
        }

        void transferFirmware(Firmware firmware) {
            Thread.sleep(sleep)
            updateExecutable = true
        }
    }

    final class NonFirmwareUpdateThingHandlerFactory extends BaseThingHandlerFactory {

        @Override
        public boolean supportsThingType(ThingTypeUID thingTypeUID) {
            thingTypeUID.equals(THING_TYPE_UID_WITHOUT_FW)
        }

        @Override
        protected ThingHandler createHandler(Thing thing) {
            return new NonFirmwareUpdateThingHandler(thing)
        }
    }

    final class NonFirmwareUpdateThingHandler extends BaseThingHandler {

        NonFirmwareUpdateThingHandler(Thing thing) {
            super(thing)
        }

        @Override
        public void handleCommand(ChannelUID channelUID, Command command) {
        }
    }
}
