/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.test

import static org.eclipse.smarthome.binding.hue.HueBindingConstants.*
import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*
import nl.q42.jue.MockedHttpClient
import nl.q42.jue.HttpClient.Result

import org.eclipse.smarthome.binding.hue.handler.HueBridgeHandler
import org.eclipse.smarthome.binding.hue.handler.HueLightHandler
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.events.EventPublisher
import org.eclipse.smarthome.core.items.events.ItemEventFactory
import org.eclipse.smarthome.core.library.types.HSBType
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType
import org.eclipse.smarthome.core.library.types.OnOffType
import org.eclipse.smarthome.core.library.types.PercentType
import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingProvider
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingStatusDetail
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder
import org.eclipse.smarthome.core.thing.setup.ThingSetupManager
import org.eclipse.smarthome.core.types.Command
import org.eclipse.smarthome.test.AsyncResultWrapper
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.storage.VolatileStorageService
import org.junit.Before
import org.junit.Test


/**
 * Tests for {@link HueLightHandler}.
 *
 * @author Oliver Libutzki - Initial contribution
 * @author Michael Grammling - Initial contribution
 */
class HueLightHandlerOSGiTest extends OSGiTest {

    final ThingTypeUID BRIDGE_THING_TYPE_UID = new ThingTypeUID("hue", "bridge")
    final ThingTypeUID COLOR_LIGHT_THING_TYPE_UID = new ThingTypeUID("hue", "LCT001")
    final ThingTypeUID LUX_LIGHT_THING_TYPE_UID = new ThingTypeUID("hue", "LWB004")

    ManagedThingProvider managedThingProvider
    VolatileStorageService volatileStorageService = new VolatileStorageService()


    @Before
    void setUp() {
        registerService(volatileStorageService)
        managedThingProvider = getService(ThingProvider, ManagedThingProvider)
        assertThat managedThingProvider, is(notNullValue())
    }

    Bridge createBridge() {
        Configuration bridgeConfiguration = new Configuration().with {
            put(HOST, "1.2.3.4")
            put(USER_NAME, "testUserName")
            put(SERIAL_NUMBER, "testSerialNumber")
            it
        }

        Bridge hueBridge = managedThingProvider.createThing(
                BRIDGE_THING_TYPE_UID,
                new ThingUID(BRIDGE_THING_TYPE_UID, "testBridge"),
                null, bridgeConfiguration)

        assertThat hueBridge, is(notNullValue())

        return hueBridge
    }

    Thing createLight(hueBridge, ThingTypeUID lightUID) {
        Configuration lightConfiguration = new Configuration().with {
            put(LIGHT_ID, "1")
            it
        }

        Thing hueLight = managedThingProvider.createThing(
                lightUID,
                new ThingUID(lightUID, "Light1"),
                hueBridge.getUID(), lightConfiguration)

        assertThat hueLight, is(notNullValue())

        return hueLight
    }

    @Test
    void 'assert that HueLightHandler is registered and unregistered'() {
        Bridge hueBridge = createBridge()

        HueLightHandler hueLightHandler = getService(ThingHandler, HueLightHandler)
        assertThat hueLightHandler, is(nullValue())

        Thing hueLight = createLight(hueBridge, COLOR_LIGHT_THING_TYPE_UID)

        // wait for HueLightHandler to be registered
        waitForAssert({
            hueLightHandler = getService(ThingHandler, HueLightHandler)
            assertThat hueLightHandler, is(notNullValue())
        }, 10000)

        managedThingProvider.remove(hueLight.getUID())

        // wait for HueLightHandler to be unregistered
        waitForAssert({
            hueLightHandler = getService(ThingHandler, HueLightHandler)
            assertThat hueLightHandler, is(nullValue())
        }, 10000)

        managedThingProvider.remove(hueBridge.getUID())
    }

    @Test
    void 'assert command for color channel: on'() {
        def expectedReply =
                """
                {
                    "on" : true
                }
                """
        assertSendCommandForColor(OnOffType.ON, new HueLightState(), expectedReply)
    }

    @Test
    void 'assert command for color temperature channel: on'() {
        def expectedReply =
                """
                {
                    "on" : true
                }
                """
        assertSendCommandForColorTemp(OnOffType.ON, new HueLightState(), expectedReply)
    }

    @Test
    void 'assert command for color channel: off'() {
        def expectedReply =
                """
                {
                    "on" : false
                }
                """
        assertSendCommandForColor(OnOffType.OFF, new HueLightState(), expectedReply)
    }

    @Test
    void 'assert command for color temperature channel: off'() {
        def expectedReply =
                """
                {
                    "on" : false
                }
                """
        assertSendCommandForColorTemp(OnOffType.OFF, new HueLightState(), expectedReply)
    }

    @Test
    void 'assert command for color temperature channel: 0%'() {
        def expectedReply =
                """
                {
                    "ct" : 153
                }
                """
        assertSendCommandForColorTemp(new PercentType(0), new HueLightState(), expectedReply)
    }

    @Test
    void 'assert command for color temperature channel: 50%'() {
        def expectedReply =
                """
                {
                    "ct" : 327
                }
                """
        assertSendCommandForColorTemp(new PercentType(50), new HueLightState(), expectedReply)
    }

    @Test
    void 'assert command for color temperature channel: 100%'() {
        def expectedReply =
                """
                {
                    "ct" : 500
                }
                """
        assertSendCommandForColorTemp(new PercentType(100), new HueLightState(), expectedReply)
    }

    @Test
    void 'assert command for color channel: 0%'() {
        def expectedReply =
                """
                {
                    "on" : false
                }
                """
        assertSendCommandForColor(new PercentType(0), new HueLightState(), expectedReply)
    }

    @Test
    void 'assert command for color channel: 50%'() {
        def expectedReply =
                """
                {
                    "bri" : 127,
                    "on" : true
                }
                """
        assertSendCommandForColor(new PercentType(50), new HueLightState(), expectedReply)
    }

    @Test
    void 'assert command for color channel: 100%'() {
        def expectedReply =
                """
                {
                    "bri" : 254,
                    "on" : true
                }
                """
        assertSendCommandForColor(new PercentType(100), new HueLightState(), expectedReply)
    }

    @Test
    void 'assert command for color channel: black'() {
        def expectedBody =
                """
                {
                    "on" : false
                }
                """
        assertSendCommandForColor(HSBType.BLACK, new HueLightState(), expectedBody)
    }

    @Test
    void 'assert command for color channel: red'() {
        def expectedReply =
                """
                {
                    "bri" : 254,
                    "sat" : 254,
                    "hue" : 0
                }
                """
        assertSendCommandForColor(HSBType.RED, new HueLightState(), expectedReply)
    }

    @Test
    void 'assert command for color channel: blue'() {
        def expectedReply =
                """
                {
                    "bri" : 254,
                    "sat" : 254,
                    "hue" : 43680
                }
                """
        assertSendCommandForColor(HSBType.BLUE, new HueLightState(), expectedReply)
    }

    @Test
    void 'assert command for color channel: white'() {
        def expectedBody =
                """
                {
                    "bri" : 254,
                    "sat" : 0,
                    "hue" : 0
                }
                """
        assertSendCommandForColor(HSBType.WHITE, new HueLightState(), expectedBody)
    }

    @Test
    void 'assert command for color channel: increase'() {
        def currentState = new HueLightState().bri(1).on(false)
        def expectedReply ='{"bri" : 30, "on" : true}'
        assertSendCommandForColor(IncreaseDecreaseType.INCREASE, currentState, expectedReply)

        currentState.bri(200).on(true)
        expectedReply ='{"bri" : 230}'
        assertSendCommandForColor(IncreaseDecreaseType.INCREASE, currentState, expectedReply)

        currentState.bri(230)
        expectedReply ='{"bri" : 254}'
        assertSendCommandForColor(IncreaseDecreaseType.INCREASE, currentState, expectedReply)
    }

    @Test
    void 'assert command for color channel: decrease'() {
        def currentState = new HueLightState().bri(200)
        def expectedReply ='{"bri" : 170}'
        assertSendCommandForColor(IncreaseDecreaseType.DECREASE, currentState, expectedReply)

        currentState.bri(20)
        expectedReply ='{"on" : false}'
        assertSendCommandForColor(IncreaseDecreaseType.DECREASE, currentState, expectedReply)
    }

    @Test
    void 'assert command for brightness channel: 50%'() {
        def currentState = new HueLightState()
        def expectedReply ='{"bri" : 127, "on" : true}'
        assertSendCommandForBrightness(new PercentType(50), currentState, expectedReply)
    }

    @Test
    void 'assert command for brightness channel: increase'() {
        def currentState = new HueLightState().bri(1).on(false)
        def expectedReply ='{"bri" : 30, "on" : true}'
        assertSendCommandForBrightness(IncreaseDecreaseType.INCREASE, currentState, expectedReply)

        currentState.bri(200).on(true)
        expectedReply ='{"bri" : 230}'
        assertSendCommandForBrightness(IncreaseDecreaseType.INCREASE, currentState, expectedReply)

        currentState.bri(230)
        expectedReply ='{"bri" : 254}'
        assertSendCommandForBrightness(IncreaseDecreaseType.INCREASE, currentState, expectedReply)
    }

    @Test
    void 'assert command for brightness channel: decrease'() {
        def currentState = new HueLightState().bri(200)
        def expectedReply ='{"bri" : 170}'
        assertSendCommandForBrightness(IncreaseDecreaseType.DECREASE, currentState, expectedReply)

        currentState.bri(20)
        expectedReply ='{"on" : false}'
        assertSendCommandForBrightness(IncreaseDecreaseType.DECREASE, currentState, expectedReply)
    }


    private void assertSendCommandForColor(Command command, HueLightState currentState, String expectedReply) {
        assertSendCommand(CHANNEL_COLOR, command, COLOR_LIGHT_THING_TYPE_UID, currentState, expectedReply)
    }

    private void assertSendCommandForColorTemp(Command command, HueLightState currentState, String expectedReply) {
        assertSendCommand(CHANNEL_COLORTEMPERATURE, command, COLOR_LIGHT_THING_TYPE_UID, currentState, expectedReply)
    }

    private void assertSendCommandForBrightness(Command command, HueLightState currentState, String expectedReply) {
        assertSendCommand(CHANNEL_BRIGHTNESS, command, LUX_LIGHT_THING_TYPE_UID, currentState, expectedReply)
    }

    private void assertSendCommand(String channel, Command command, ThingTypeUID hueLightUID, HueLightState currentState, String expectedReply) {
        Bridge hueBridge = createBridge()

        HueLightHandler hueLightHandler = getService(ThingHandler, HueLightHandler)
        assertThat hueLightHandler, is(nullValue())

        Thing hueLight = createLight(hueBridge, hueLightUID)

        try {
            // wait for HueLightHandler to be registered
            waitForAssert({
                hueLightHandler = getService(ThingHandler, HueLightHandler)
                assertThat hueLightHandler, is(notNullValue())
            }, 10000)

            def AsyncResultWrapper<String> addressWrapper = new AsyncResultWrapper<String>()
            def AsyncResultWrapper<String> bodyWrapper = new AsyncResultWrapper<String>()

            MockedHttpClient mockedHttpClient =  [
                put: { String address, String body ->
                    addressWrapper.set(address)
                    bodyWrapper.set(body)
                    new Result("", 200)
                },
                get: { String address ->
                    if (address.endsWith("testUserName/")) {
                        new Result(currentState.toString(), 200)
                    }
                }
            ] as MockedHttpClient

            installHttpClientMock(hueLightHandler.getHueBridgeHandler(), mockedHttpClient)

            assertBridgeOnline(hueLightHandler.getBridge())
            enableHueLightChannels(hueLight)
            postCommand(hueLight, channel, command)

            waitForAssert({assertTrue addressWrapper.isSet}, 10000)
            waitForAssert({assertTrue bodyWrapper.isSet}, 10000)

            assertThat addressWrapper.wrappedObject, is("http://1.2.3.4/api/testUserName/lights/1/state")
            assertJson(expectedReply, bodyWrapper.wrappedObject)
        } finally {
            managedThingProvider.remove(hueLight.getUID())
            managedThingProvider.remove(hueBridge.getUID())
        }
    }

    private assertBridgeOnline(Bridge bridge){
        def online = ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build()
        waitForAssert({
            assertThat bridge.getStatusInfo(), is(online)
        }, 10000)
    }

    private enableHueLightChannels(Thing hueLight){
        ThingSetupManager thingSetupManager = getService(ThingSetupManager)

        hueLight.getChannels().each {
            thingSetupManager.enableChannel(it.UID)
        }
    }

    private postCommand(Thing hueLight, String channel, Command command){

        def item = hueLight.getUID().toString().replace(":", "_") + "_" + channel

        EventPublisher eventPublisher = getService(EventPublisher)
        assertThat eventPublisher, is(notNullValue())

        eventPublisher.post(ItemEventFactory.createCommandEvent(item, command))
    }

    private void assertJson(String expected, String actual) {
        def jsonSlurper = Class.forName("groovy.json.JsonSlurper").newInstance()
        def actualResult = jsonSlurper.parseText(actual)
        def expectedResult = jsonSlurper.parseText(expected)

        assertThat actualResult, is(expectedResult)
    }

    private void installHttpClientMock(HueBridgeHandler hueBridgeHandler,
            MockedHttpClient mockedHttpClient) {

        // mock HttpClient
        def hueBridgeField = hueBridgeHandler.getClass().getDeclaredField("bridge")
        hueBridgeField.accessible = true
        def hueBridgeValue = hueBridgeField.get(hueBridgeHandler)

        def httpClientField = hueBridgeValue.getClass().getDeclaredField("http")
        httpClientField.accessible = true
        httpClientField.set(hueBridgeValue, mockedHttpClient)

        def usernameField = hueBridgeValue.getClass().getDeclaredField("username")
        usernameField.accessible = true
        usernameField.set(hueBridgeValue, hueBridgeHandler.config.get(USER_NAME))

        hueBridgeHandler.initialize()
    }

}
