/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
import org.eclipse.smarthome.core.common.SafeMethodCaller
import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.events.EventFilter
import org.eclipse.smarthome.core.events.EventPublisher
import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.i18n.TranslationProvider
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
import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUpdateBackgroundTransferHandler
import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUpdateHandler
import org.eclipse.smarthome.core.thing.binding.firmware.ProgressCallback
import org.eclipse.smarthome.core.thing.binding.firmware.ProgressStep
import org.eclipse.smarthome.core.thing.type.ThingTypeBuilder
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry
import org.eclipse.smarthome.core.types.Command
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.osgi.service.cm.ConfigurationAdmin
import org.osgi.service.component.ComponentContext

import com.google.common.collect.ImmutableSet

/**
 * Testing the {@link FirmwareUpdateService}.
 *
 * @author Thomas Höfer - Initial contribution
 */
final class FirmwareUpdateServiceOSGiTest extends OSGiTest {

    private final FirmwareStatusInfo unknownInfo = createUnknownInfo()
    private final FirmwareStatusInfo upToDateInfo = createUpToDateInfo()
    private final FirmwareStatusInfo updateAvailableInfo =  createUpdateAvailableInfo()
    private final FirmwareStatusInfo updateExecutableInfoFw112 = createUpdateExecutableInfo(FW112_EN.getUID())
    private final FirmwareStatusInfo updateExecutableInfoFw113 = createUpdateExecutableInfo(FW113_EN.getUID())
    private final FirmwareStatusInfo updateExecutableInfoFw120 = createUpdateExecutableInfo(FW120_EN.getUID())

    Locale _defaultLocale

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
            } else if(firmwareUID.equals(FWALPHA_EN.getUID())) {
                FWALPHA_EN
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
        _defaultLocale = Locale.getDefault()
        setDefaultLocale(Locale.ENGLISH)

        registerVolatileStorageService()
        waitForAssert {
            managedThingProvider = getService ManagedThingProvider
            assertThat managedThingProvider, is(notNullValue())
        }

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

        def expectedInitializedFirmwareStatusInfoEvents = 3
        waitForAssert {
            assertThat firmwareStatusInfoEventSubscriber.events.size(), is(expectedInitializedFirmwareStatusInfoEvents)
        }
    }

    @After
    void teardown() {
        managedThingProvider.getAll().each {
            managedThingProvider.remove(it.getUID())
        }
        unregisterMocks()
        setDefaultLocale(_defaultLocale)
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
                [FWALPHA_EN] as Set
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
        assertThatNoFirmwareStatusInfoEventWasPropagated(THING1_UID)
    }

    @Test
    void 'assert that firmware status is propagated regularly through job'() {
        assertThat thing1.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V111)
        assertThat thing2.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V112)

        def originalPeriod = firmwareUpdateService.firmwareStatusInfoJobPeriod

        updateConfig(1, 1, TimeUnit.SECONDS)

        final int expectedEventsBecauseOfInitialPropagation = 3

        waitForAssert {
            assertThat firmwareStatusInfoEventSubscriber.events.size(), is(expectedEventsBecauseOfInitialPropagation)
            assertThat firmwareStatusInfoEventSubscriber.events, hasItem(FirmwareEventFactory.createFirmwareStatusInfoEvent(updateExecutableInfoFw112, THING1_UID))
            assertThat firmwareStatusInfoEventSubscriber.events, hasItem(FirmwareEventFactory.createFirmwareStatusInfoEvent(upToDateInfo, THING2_UID))
            assertThat firmwareStatusInfoEventSubscriber.events, hasItem(FirmwareEventFactory.createFirmwareStatusInfoEvent(unknownInfo, THING3_UID))
        }
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

        final int expectedEventsBecauseOfStatusChange = 2

        waitForAssert {
            assertThat firmwareStatusInfoEventSubscriber.events.size(), is(expectedEventsBecauseOfStatusChange)
            assertThat firmwareStatusInfoEventSubscriber.events, hasItem(FirmwareEventFactory.createFirmwareStatusInfoEvent(updateExecutableInfoFw113, THING1_UID))
            assertThat firmwareStatusInfoEventSubscriber.events, hasItem(FirmwareEventFactory.createFirmwareStatusInfoEvent(updateExecutableInfoFw113, THING2_UID))
        }
        firmwareStatusInfoEventSubscriber.events = []

        unregisterService(firmwareProvider2)

        waitForAssert {
            assertThat firmwareStatusInfoEventSubscriber.events.size(), is(expectedEventsBecauseOfStatusChange)
            assertThat firmwareStatusInfoEventSubscriber.events, hasItem(FirmwareEventFactory.createFirmwareStatusInfoEvent(updateExecutableInfoFw112, THING1_UID))
            assertThat firmwareStatusInfoEventSubscriber.events, hasItem(FirmwareEventFactory.createFirmwareStatusInfoEvent(upToDateInfo, THING2_UID))
        }

        updateConfig(originalPeriod, 1, TimeUnit.SECONDS)

        waitForAssert {
            assertThat firmwareUpdateService.firmwareStatusInfoJobPeriod, is(originalPeriod)
        }

        firmwareRegistry.firmwareProviders.clear()
    }

    @Test
    void 'assert that firmware upgrade works'() {
        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw112)
        assertFirmwareStatusInfoEvent(THING1_UID, updateExecutableInfoFw112)

        firmwareUpdateService.updateFirmware(THING1_UID, FW112_EN.getUID(), null)

        waitForAssert {
            assertThat thing1.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V112.toString())
        }

        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(upToDateInfo)
        assertFirmwareStatusInfoEvent(THING1_UID, upToDateInfo)
    }

    @Test(expected=IllegalArgumentException)
    void 'assert that cancel throws IllegalArgumentException if there is no firmware update handler for thing'(){
        firmwareUpdateService.cancelFirmwareUpdate(new ThingUID("dummy:thing:withoutHandler"))
    }

    @Test(expected=NullPointerException)
    void 'assert that cancel throws NullPointerExceptions if the given thing id is null'(){
        firmwareUpdateService.cancelFirmwareUpdate(null)
    }

    @Test(expected=IllegalStateException)
    void 'assert that cancel throws IllegalStateException if update was not started before'(){
        firmwareUpdateService.cancelFirmwareUpdate(THING3_UID)
    }

    @Test
    void 'assert that cancel cancels the firmware update using the correct FirmwareUpdateHandler'(){
        firmwareUpdateService.updateFirmware(THING1_UID, FW111_EN.getUID(), _defaultLocale)
        firmwareUpdateService.updateFirmware(THING2_UID, FW111_EN.getUID(), _defaultLocale)
        firmwareUpdateService.updateFirmware(THING3_UID, FWALPHA_EN.getUID(), _defaultLocale)

        firmwareUpdateService.cancelFirmwareUpdate(THING3_UID)

        waitForAssert{
            assertThat thing1.getHandler().cancelCalled, is(false)
            assertThat thing2.getHandler().cancelCalled, is(false)
            assertThat thing3.getHandler().cancelCalled, is(true)
        }
    }

    @Test
    void 'assert that cancelFirmwareUpdate sets internalFailed on ProgressCallback if an exception occur'(){
        def exception = new NullPointerException()
        def postedEvent
        def expectedEnglishMessage = "An unexpected error occurred during canceling of firmware update."
        def firmwareUpdateHandler = [
            cancel:{ throw exception },
            isUpdateExecutable:{
            },
            updateFirmware:{ fw, callback ->
            },
            getThing: {
                return ThingBuilder.create(THING_TYPE_UID1, THING4_UID).build()
            }
        ] as FirmwareUpdateHandler

        def publisher = [
            post : { event -> postedEvent = event }
        ] as EventPublisher
        def i18nProvider = getService(TranslationProvider)
        assertThat i18nProvider, is(notNullValue())
        registerService(firmwareUpdateHandler)
        def service = getService(FirmwareUpdateHandler) {
            bundleContext.getService(it).equals(firmwareUpdateHandler)
        }
        assertThat service, is(not(null))

        //locale null
        def callback = new ProgressCallbackImpl(firmwareUpdateHandler, publisher, i18nProvider, THING4_UID, FW111_EN.getUID(), null)
        firmwareUpdateService.progressCallbackMap.put(THING4_UID, callback)
        firmwareUpdateService.cancelFirmwareUpdate(THING4_UID)
        waitForAssert{
            assertThat postedEvent, is(notNullValue())
            assertThat postedEvent, is(instanceOf(FirmwareUpdateResultInfoEvent))
            FirmwareUpdateResultInfoEvent resultEvent = postedEvent as FirmwareUpdateResultInfoEvent
            assertThat resultEvent.getThingUID(), is(THING4_UID)
            assertThat resultEvent.firmwareUpdateResultInfo.result, is(FirmwareUpdateResult.ERROR)
            assertThat resultEvent.firmwareUpdateResultInfo.errorMessage, is(expectedEnglishMessage)
        }
        postedEvent = null

        //locale EN
        callback = new ProgressCallbackImpl(firmwareUpdateHandler, publisher, i18nProvider, THING4_UID, FW111_EN.getUID(), Locale.ENGLISH)
        firmwareUpdateService.progressCallbackMap.put(THING4_UID, callback)
        firmwareUpdateService.cancelFirmwareUpdate(THING4_UID)
        waitForAssert{
            assertThat postedEvent, is(notNullValue())
            assertThat postedEvent, is(instanceOf(FirmwareUpdateResultInfoEvent))
            FirmwareUpdateResultInfoEvent resultEvent = postedEvent as FirmwareUpdateResultInfoEvent
            assertThat resultEvent.getThingUID(), is(THING4_UID)
            assertThat resultEvent.firmwareUpdateResultInfo.result, is(FirmwareUpdateResult.ERROR)
            assertThat resultEvent.firmwareUpdateResultInfo.errorMessage, is(expectedEnglishMessage)
        }
        postedEvent = null

        //locale DE
        callback = new ProgressCallbackImpl(firmwareUpdateHandler, publisher, i18nProvider, THING4_UID, FW111_EN.getUID(), Locale.GERMANY)
        firmwareUpdateService.progressCallbackMap.put(THING4_UID, callback)
        firmwareUpdateService.cancelFirmwareUpdate(THING4_UID)
        waitForAssert{
            assertThat postedEvent, is(notNullValue())
            assertThat postedEvent, is(instanceOf(FirmwareUpdateResultInfoEvent))
            FirmwareUpdateResultInfoEvent resultEvent = postedEvent as FirmwareUpdateResultInfoEvent
            assertThat resultEvent.getThingUID(), is(THING4_UID)
            assertThat resultEvent.firmwareUpdateResultInfo.result, is(FirmwareUpdateResult.ERROR)
            assertThat resultEvent.firmwareUpdateResultInfo.errorMessage, is("Es ist ein unerwarteter Fehler während des Abbruchs eines Firmware-Updates aufgetreten.")
        }
    }

    @Test
    void 'assert that cancelFirmwareUpdate sets internalFailed on ProgressCallback if the operation took to long'(){
        def expectedEnglishMessage = "A timeout occurred during canceling of firmware update."
        def postedEvent
        def firmwareUpdateHandler = [
            cancel:{
                Thread.sleep(SafeMethodCaller.DEFAULT_TIMEOUT+1000)
            },
            isUpdateExecutable:{
            },
            updateFirmware:{ fw, callback ->
            },
            getThing: {
                return ThingBuilder.create(THING_TYPE_UID1, THING4_UID).build()
            }
        ] as FirmwareUpdateHandler
        def publisher = [
            post : { event -> postedEvent = event }
        ] as EventPublisher
        def i18nProvider = getService(TranslationProvider)
        assertThat i18nProvider, is(notNullValue())

        registerService(firmwareUpdateHandler)
        def service = getService(FirmwareUpdateHandler) {
            bundleContext.getService(it).equals(firmwareUpdateHandler)
        }
        assertThat service, is(not(null))

        //locale null
        def callback = new ProgressCallbackImpl(firmwareUpdateHandler, publisher, i18nProvider, THING4_UID, FW111_EN.getUID(), null)
        firmwareUpdateService.progressCallbackMap.put(THING4_UID, callback)
        firmwareUpdateService.cancelFirmwareUpdate(THING4_UID)
        waitForAssert{
            assertThat postedEvent, is(notNullValue())
            assertThat postedEvent, is(instanceOf(FirmwareUpdateResultInfoEvent))
            FirmwareUpdateResultInfoEvent resultEvent = postedEvent as FirmwareUpdateResultInfoEvent
            assertThat resultEvent.getThingUID(), is(THING4_UID)
            assertThat resultEvent.firmwareUpdateResultInfo.result, is(FirmwareUpdateResult.ERROR)
            assertThat resultEvent.firmwareUpdateResultInfo.errorMessage, is(expectedEnglishMessage)
        }
        postedEvent = null

        //locale EN
        callback = new ProgressCallbackImpl(firmwareUpdateHandler, publisher, i18nProvider, THING4_UID, FW111_EN.getUID(), Locale.ENGLISH)
        firmwareUpdateService.progressCallbackMap.put(THING4_UID, callback)
        firmwareUpdateService.cancelFirmwareUpdate(THING4_UID)
        waitForAssert{
            assertThat postedEvent, is(notNullValue())
            assertThat postedEvent, is(instanceOf(FirmwareUpdateResultInfoEvent))
            FirmwareUpdateResultInfoEvent resultEvent = postedEvent as FirmwareUpdateResultInfoEvent
            assertThat resultEvent.getThingUID(), is(THING4_UID)
            assertThat resultEvent.firmwareUpdateResultInfo.result, is(FirmwareUpdateResult.ERROR)
            assertThat resultEvent.firmwareUpdateResultInfo.errorMessage, is(expectedEnglishMessage)
        }
        postedEvent = null

        //locale DE
        callback = new ProgressCallbackImpl(firmwareUpdateHandler, publisher, i18nProvider, THING4_UID, FW111_EN.getUID(), Locale.GERMANY)
        firmwareUpdateService.progressCallbackMap.put(THING4_UID, callback)
        firmwareUpdateService.cancelFirmwareUpdate(THING4_UID)
        waitForAssert{
            assertThat postedEvent, is(notNullValue())
            assertThat postedEvent, is(instanceOf(FirmwareUpdateResultInfoEvent))
            FirmwareUpdateResultInfoEvent resultEvent = postedEvent as FirmwareUpdateResultInfoEvent
            assertThat resultEvent.getThingUID(), is(THING4_UID)
            assertThat resultEvent.firmwareUpdateResultInfo.result, is(FirmwareUpdateResult.ERROR)
            assertThat resultEvent.firmwareUpdateResultInfo.errorMessage, is("Das Abbrechen des Firmware-Updates ist aufgrund einer Zeitüberschreitung fehlgeschlagen.")
        }
    }

    @Test
    void 'assert that firmware downgrade works'() {
        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING2_UID), is(upToDateInfo)
        assertFirmwareStatusInfoEvent(THING2_UID, upToDateInfo)

        firmwareUpdateService.updateFirmware(THING2_UID, FW111_EN.getUID(), null)

        waitForAssert {
            assertThat thing2.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V111.toString())
        }

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

        thrown.expect(IllegalArgumentException.class)
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
                [FW111_FIX_EN, FW113_EN] as Set
            }] as FirmwareProvider

        registerService(firmwareProvider2, FirmwareProvider.class.getName())

        assertFirmwareStatusInfoEvent(THING2_UID, upToDateInfo)

        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw113)
        assertFirmwareStatusInfoEvent(THING1_UID, updateExecutableInfoFw112, updateExecutableInfoFw113)

        try {
            firmwareUpdateService.updateFirmware(THING1_UID, FW113_EN.getUID(), null)
            fail "Expeced an IllegalArgumentException, but it was not thrown."
        } catch (IllegalArgumentException expected) {
            assertThat expected.getMessage(), is(String.format(
                    "Firmware with UID %s requires at least firmware version %s to get installed. But the current firmware version of the thing with UID %s is %s.",
                    FW113_EN.getUID(), FW113_EN.getPrerequisiteVersion(), THING1_UID, V111))
        }

        firmwareUpdateService.updateFirmware(THING1_UID, FW111_FIX_EN.getUID(), null)

        waitForAssert {
            assertThat thing1.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V111_FIX)
        }

        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw113)
        assertThatNoFirmwareStatusInfoEventWasPropagated(THING1_UID)

        firmwareUpdateService.updateFirmware(THING1_UID, FW113_EN.getUID(), null)

        waitForAssert {
            assertThat thing1.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V113)
        }

        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(upToDateInfo)
        assertFirmwareStatusInfoEvent(THING1_UID, upToDateInfo)

        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING2_UID), is(updateExecutableInfoFw113)
        assertFirmwareStatusInfoEvent(THING2_UID, updateExecutableInfoFw113)

        firmwareUpdateService.updateFirmware(THING2_UID, FW113_EN.getUID(), null)

        waitForAssert {
            assertThat thing2.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V113)
        }

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

        waitForAssert {
            assertThat thing1.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V112)
        }

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

        waitForAssert {
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
        }
        progressInfoEventSubscriber.events.clear()

        waitForAssert {
            assertThat firmwareUpdateResultInfoEventSubscriber.events.size(), is(1)
            FirmwareUpdateResultInfoEvent firmwareUpdateResultInfoEvent = (FirmwareUpdateResultInfoEvent) firmwareUpdateResultInfoEventSubscriber.events [0]
            assertThat firmwareUpdateResultInfoEvent.getTopic(), containsString(THING1_UID.getAsString())
            assertThat firmwareUpdateResultInfoEvent.getThingUID(), is(THING1_UID)
            assertThat firmwareUpdateResultInfoEvent.getFirmwareUpdateResultInfo().getResult(), is(FirmwareUpdateResult.SUCCESS)
            assertThat firmwareUpdateResultInfoEvent.getFirmwareUpdateResultInfo().getErrorMessage(), is(null)
        }
        firmwareUpdateResultInfoEventSubscriber.events.clear()

        waitForAssert {
            assertThat thing1.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V112.toString())
        }

        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(upToDateInfo)
        assertFirmwareStatusInfoEvent(THING1_UID, upToDateInfo)
    }

    @Test
    void 'assert that update firmware handles time out'() {
        registerService(firmwareUpdateResultInfoEventSubscriber, EventSubscriber.class.getName())

        def timeout = firmwareUpdateService.timeout
        firmwareUpdateService.timeout = 250

        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw112)
        assertFirmwareStatusInfoEvent(THING1_UID, updateExecutableInfoFw112)

        def longwait = 10000
        thing1.getHandler().wait = longwait

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
        assertFailedFirmwareUpdate(enMsg)

        firmwareUpdateService.updateFirmware(THING1_UID, FW112_EN.getUID(), Locale.ENGLISH)
        assertFailedFirmwareUpdate(enMsg)

        firmwareUpdateService.updateFirmware(THING1_UID, FW112_EN.getUID(), Locale.GERMAN)
        assertFailedFirmwareUpdate("Es ist ein unerwarteter Fehler während des Firmware-Updates aufgetreten.")

        assertThat thing1.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V111.toString())
        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw112)
        assertThatNoFirmwareStatusInfoEventWasPropagated(THING1_UID)
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

        waitForAssert {
            assertThat thing1.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V111.toString())
        }

        assertThat thing1.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V111.toString())
        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw112)
        assertThatNoFirmwareStatusInfoEventWasPropagated(THING1_UID)
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

        waitForAssert {
            assertThat firmwareUpdateService.getFirmwareStatusInfo(THING4_UID), is(updateExecutableInfoFw120)
            assertFirmwareStatusInfoEvent(THING4_UID, updateExecutableInfoFw120)
        }

        firmwareUpdateService.updateFirmware(THING4_UID, FW120_EN.getUID(), null)

        waitForAssert {
            assertThat thing4.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V120)
        }

        assertThat firmwareUpdateService.getFirmwareStatusInfo(THING4_UID), is(upToDateInfo)
        assertFirmwareStatusInfoEvent(THING4_UID, upToDateInfo)

        assertThat thing4.getHandler().isUpdateExecutable(), is(false)
    }

    @Test
    void 'assert that invalid config values are rejected'() {
        def originalPeriod = firmwareUpdateService.firmwareStatusInfoJobPeriod
        def originalDelay = firmwareUpdateService.firmwareStatusInfoJobDelay
        def originalTimeUnit = firmwareUpdateService.firmwareStatusInfoJobTimeUnit

        updateInvalidConfigAndAssert(0, 0, TimeUnit.SECONDS, originalPeriod, originalDelay, originalTimeUnit)
        updateInvalidConfigAndAssert(1, -1, TimeUnit.SECONDS, originalPeriod, originalDelay, originalTimeUnit)
        updateInvalidConfigAndAssert(1, 0, TimeUnit.NANOSECONDS, originalPeriod, originalDelay, originalTimeUnit)
    }

    private void registerThingTypeProvider() {
        def thingType = ThingTypeBuilder.instance(THING_TYPE_UID1, "label").withConfigDescriptionURI(CONFIG_URI).build();
        def thingTypeWithoutFW = ThingTypeBuilder.instance(THING_TYPE_UID_WITHOUT_FW, "label").withConfigDescriptionURI(CONFIG_URI).build();

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

    private void assertThatNoFirmwareStatusInfoEventWasPropagated(ThingUID thingUID) {
        def infoEvents = new ArrayList(firmwareStatusInfoEventSubscriber.events)
        assertThat infoEvents.findAll {
            it.thingUID.equals(thingUID)
        }.size(), is(0)
    }

    private void assertFirmwareStatusInfoEvent(ThingUID thingUID, FirmwareStatusInfo... expected) {
        def expectedList = expected as List

        def infoEvents = []
        waitForAssert {
            infoEvents = new ArrayList(firmwareStatusInfoEventSubscriber.events)
            infoEvents = infoEvents.findAll {
                it.thingUID.equals(thingUID)
            }

            assertThat infoEvents.size(), is(expected.size())
            infoEvents.each {
                assertThat it.getTopic(), containsString(thingUID.getAsString())
                assertThat it.getThingUID(), is(thingUID)
                assertThat expectedList, hasItem(it.getFirmwareStatusInfo())
            }
        }

        infoEvents.each {
            firmwareStatusInfoEventSubscriber.events.remove(it)
        }
    }

    private void assertFailedFirmwareUpdate(def expectedErrorMessage) {
        waitForAssert {
            assertThat firmwareUpdateResultInfoEventSubscriber.events.size(), is(1)
            FirmwareUpdateResultInfoEvent firmwareUpdateResultInfoEvent = (FirmwareUpdateResultInfoEvent) firmwareUpdateResultInfoEventSubscriber.events.first()
            assertThat firmwareUpdateResultInfoEvent.getTopic(), containsString(THING1_UID.getAsString())
            assertThat firmwareUpdateResultInfoEvent.getThingUID(), is(THING1_UID)
            assertThat firmwareUpdateResultInfoEvent.getFirmwareUpdateResultInfo().getResult(), is(FirmwareUpdateResult.ERROR)
            assertThat firmwareUpdateResultInfoEvent.getFirmwareUpdateResultInfo().getErrorMessage(), is(expectedErrorMessage)
        }

        firmwareUpdateResultInfoEventSubscriber.events.clear()
    }

    def updateInvalidConfigAndAssert(def period, def delay, def timeUnit, def expectedPeriod, def expectedDelay, def expectedTimeUnit) {
        updateConfig(period, delay, timeUnit)
        waitForAssert {
            assertThat firmwareUpdateService.firmwareStatusInfoJobPeriod, is(expectedPeriod)
            assertThat firmwareUpdateService.firmwareStatusInfoJobDelay, is(expectedDelay)
            assertThat firmwareUpdateService.firmwareStatusInfoJobTimeUnit, is(expectedTimeUnit)
        }
    }

    def updateConfig(def period, def delay, def timeUnit) {
        def config = getConfig()
        def properties = config.getProperties()
        if (properties == null) {
            properties = new Hashtable()
        }
        properties.put(FirmwareUpdateService.PERIOD_CONFIG_KEY, period)
        properties.put(FirmwareUpdateService.DELAY_CONFIG_KEY, delay)
        properties.put(FirmwareUpdateService.TIME_UNIT_CONFIG_KEY, timeUnit.name())
        config.update(properties)
    }

    def getConfig() {
        def configAdmin = getService(ConfigurationAdmin)
        assertThat configAdmin, is(notNullValue())

        def config = configAdmin.getConfiguration("org.eclipse.smarthome.core.thing.firmware.FirmwareUpdateService")
        assertThat config, is(notNullValue())

        return config
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

        int wait = 25
        boolean updateExecutable = true
        boolean exception = false
        boolean fail = false
        boolean cancelCalled = false

        FirmwareUpdateThingHandler(Thing thing) {
            super(thing)
        }

        @Override
        public void initialize() {
            sleep wait
            updateStatus(ThingStatus.ONLINE)
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

            sleep wait

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

        @Override
        public void cancel() {
            cancelCalled = true
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

        int wait = 25
        boolean updateExecutable = false
        boolean transferred = false

        FirmwareUpdateBackgroundTransferThingHandler(Thing thing) {
            super(thing)
        }

        @Override
        public void initialize() {
            sleep wait
            updateStatus(ThingStatus.ONLINE)
        }

        @Override
        public void handleCommand(ChannelUID channelUID, Command command) {
        }

        @Override
        public void updateFirmware(Firmware firmware, ProgressCallback progressCallback) {
            sleep wait
            updateProperty(Thing.PROPERTY_FIRMWARE_VERSION, firmware.getVersion())
            updateExecutable = false;
        }

        @Override
        public boolean isUpdateExecutable() {
            return updateExecutable;
        }

        void transferFirmware(Firmware firmware) {
            sleep wait
            updateExecutable = true
        }

        @Override
        public void cancel() {
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

        @Override
        public void initialize() {
            updateStatus(ThingStatus.ONLINE)
        }
    }
}
