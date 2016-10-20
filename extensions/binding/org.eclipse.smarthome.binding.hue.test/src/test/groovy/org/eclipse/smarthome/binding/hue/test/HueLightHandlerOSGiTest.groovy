/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.items.events.ItemEventFactory
import org.eclipse.smarthome.core.library.types.HSBType
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType
import org.eclipse.smarthome.core.library.types.OnOffType
import org.eclipse.smarthome.core.library.types.PercentType
import org.eclipse.smarthome.core.library.types.StringType
import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.Channel
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingStatusDetail
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder
import org.eclipse.smarthome.core.thing.link.ItemChannelLink
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry
import org.eclipse.smarthome.core.types.Command
import org.eclipse.smarthome.test.AsyncResultWrapper
import org.eclipse.smarthome.test.storage.VolatileStorageService
import org.junit.Before
import org.junit.Test


/**
 * Tests for {@link HueLightHandler}.
 *
 * @author Oliver Libutzki - Initial contribution
 * @author Michael Grammling - Initial contribution
 * @author Markus Mazurczak - Added test for OSRAM Par16 50 TW bulbs
 * @author Andre Fuechsel - modified tests after introducing the generic thing types
 */
class HueLightHandlerOSGiTest extends AbstractHueOSGiTest {

    private static final int MIN_COLOR_TEMPERATURE = 153;
    private static final int MAX_COLOR_TEMPERATURE = 500;
    private static final int COLOR_TEMPERATURE_RANGE = MAX_COLOR_TEMPERATURE - MIN_COLOR_TEMPERATURE;

    final ThingTypeUID BRIDGE_THING_TYPE_UID = new ThingTypeUID("hue", "bridge")
    final ThingTypeUID COLOR_LIGHT_THING_TYPE_UID = new ThingTypeUID("hue", "0210")
    final ThingTypeUID LUX_LIGHT_THING_TYPE_UID = new ThingTypeUID("hue", "0100")
    final ThingTypeUID OSRAM_PAR16_LIGHT_THING_TYPE_UID = new ThingTypeUID("hue", "0220")
    final String OSRAM_MODEL_TYPE = "PAR16 50 TW"
    final String OSRAM_MODEL_TYPE_ID = "PAR16_50_TW"

    ThingRegistry thingRegistry
    ItemChannelLinkRegistry linkRegistry
    ItemRegistry itemRegistry
    VolatileStorageService volatileStorageService = new VolatileStorageService()

    @Before
    void setUp() {
        registerService(volatileStorageService)
        thingRegistry = getService(ThingRegistry, ThingRegistry)
        assertThat thingRegistry, is(notNullValue())
        linkRegistry = getService(ItemChannelLinkRegistry, ItemChannelLinkRegistry)
        assertThat linkRegistry, is(notNullValue())
        itemRegistry = getService(ItemRegistry, ItemRegistry)
        assertThat itemRegistry, is(notNullValue())
    }

    Bridge createBridge() {
        Configuration bridgeConfiguration = new Configuration().with {
            put(HOST, "1.2.3.4")
            put(USER_NAME, "testUserName")
            put(SERIAL_NUMBER, "testSerialNumber")
            it
        }

        Bridge hueBridge = thingRegistry.createThingOfType(
                BRIDGE_THING_TYPE_UID,
                new ThingUID(BRIDGE_THING_TYPE_UID, "testBridge"),
                null, "Bridge", bridgeConfiguration)

        assertThat hueBridge, is(notNullValue())
        thingRegistry.add(hueBridge)

        return hueBridge
    }

    Thing createLight(hueBridge, ThingTypeUID lightUID) {
        Configuration lightConfiguration = new Configuration().with {
            put(LIGHT_ID, "1")
            it
        }

        Thing hueLight = thingRegistry.createThingOfType(
                lightUID,
                new ThingUID(lightUID, "Light1"),
                hueBridge.getUID(), "Light", lightConfiguration)

        assertThat hueLight, is(notNullValue())
        thingRegistry.add(hueLight)

        for(Channel c : hueLight.getChannels()) {
            def item = hueLight.getUID().toString().replace(":", "_") + "_" + c.getUID().id
            if(linkRegistry.getBoundChannels(item).size()==0) {
                linkRegistry.add(new ItemChannelLink(item, c.getUID()))
            }
        }

        return hueLight
    }

    @Test
    void 'assert that HueLightHandler status detail is set to bridge offline when the bridge is offline'() {
        Bridge hueBridge = createBridge()
        simulateBridgeInitialization()
        Thing hueLight = createLight(hueBridge, COLOR_LIGHT_THING_TYPE_UID)

        try {
            HueLightHandler hueLightHandler
            waitForAssert {
                hueLightHandler = getThingHandler(HueLightHandler)
                assertThat hueLightHandler, is(notNullValue())
            }

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
                        new Result(new HueLightState().toString(), 200)
                    }
                }
            ] as MockedHttpClient

            HueBridgeHandler hueBridgeHandler = hueLightHandler.getHueBridgeHandler()
            installHttpClientMock(hueBridgeHandler, mockedHttpClient)

            assertBridgeOnline(hueLightHandler.getBridge())
            hueLightHandler.initialize()

            waitForAssert({
                assertThat(hueLight.getStatus(), is(ThingStatus.ONLINE))
            }, 10000)

            hueBridgeHandler.onConnectionLost(hueBridgeHandler.bridge)

            assertThat(hueBridge.getStatus(), is(ThingStatus.OFFLINE))
            assertThat(hueBridge.getStatusInfo().getStatusDetail(), is(not(ThingStatusDetail.BRIDGE_OFFLINE)))
            waitForAssert({
                assertThat(hueLight.getStatus(),  is(ThingStatus.OFFLINE))
            }, 10000)
            waitForAssert({
                assertThat(hueLight.getStatusInfo().getStatusDetail(), is(ThingStatusDetail.BRIDGE_OFFLINE))
            }, 10000)
        } finally {
            thingRegistry.forceRemove(hueLight.getUID())
            thingRegistry.forceRemove(hueBridge.getUID())
            waitForAssert({
                assertThat thingRegistry.get(hueLight.getUID()), is(nullValue())
                assertThat thingRegistry.get(hueBridge.getUID()), is(nullValue())
            }, 10000)
        }
    }

    @Test
    void 'assert command for osram par16 50 for color temperature channel: on'() {
        def expectedReply = '{"on" : true, "bri" : 254}'
        assertSendCommandForColorTempForPar16(OnOffType.ON, new HueLightState(OSRAM_MODEL_TYPE), expectedReply)
    }

    @Test
    void 'assert command for osram par16 50 for color temperature channel: off'() {
        def expectedReply = '{"on" : false, "transitiontime" : 0}'
        assertSendCommandForColorTempForPar16(OnOffType.OFF, new HueLightState(OSRAM_MODEL_TYPE), expectedReply)
    }

    @Test
    void 'assert command for osram par16 50 for brightness channel: on'() {
        def expectedReply = '{"on" : true, "bri" : 254}'
        assertSendCommandForBrightnessForPar16(OnOffType.ON, new HueLightState(OSRAM_MODEL_TYPE), expectedReply)
    }

    @Test
    void 'assert command for osram par16 50 for brightness channel: off'() {
        def expectedReply = '{"on" : false, "transitiontime" : 0}'
        assertSendCommandForBrightnessForPar16(OnOffType.OFF, new HueLightState(OSRAM_MODEL_TYPE), expectedReply)
    }

    @Test
    void 'assert command for color channel: on'() {
        def expectedReply = '{"on" : true}'
        assertSendCommandForColor(OnOffType.ON, new HueLightState(), expectedReply)
    }

    @Test
    void 'assert command for color temperature channel: on'() {
        def expectedReply = '{"on" : true}'
        assertSendCommandForColorTemp(OnOffType.ON, new HueLightState(), expectedReply)
    }

    @Test
    void 'assert command for color channel: off'() {
        def expectedReply = '{"on" : false}'
        assertSendCommandForColor(OnOffType.OFF, new HueLightState(), expectedReply)
    }

    @Test
    void 'assert command for color temperature channel: off'() {
        def expectedReply = '{"on" : false}'
        assertSendCommandForColorTemp(OnOffType.OFF, new HueLightState(), expectedReply)
    }

    @Test
    void 'assert command for color temperature channel: 0%'() {
        def expectedReply = '{"ct" : 153}'
        assertSendCommandForColorTemp(new PercentType(0), new HueLightState(), expectedReply)
    }

    @Test
    void 'assert command for color temperature channel: 50%'() {
        def expectedReply = '{"ct" : 327}'
        assertSendCommandForColorTemp(new PercentType(50), new HueLightState(), expectedReply)
    }

    @Test
    void 'assert command for color temperature channel: 100%'() {
        def expectedReply = '{"ct" : 500}'
        assertSendCommandForColorTemp(new PercentType(100), new HueLightState(), expectedReply)
    }

    @Test
    void 'assert percentage value of color temperature when ct: 153'() {
        def expectedReply = 0
        asserttoColorTemperaturePercentType(153, expectedReply)
    }

    @Test
    void 'assert percentage value of color temperature when ct: 326'() {
        def expectedReply = 50
        asserttoColorTemperaturePercentType(326, expectedReply)
    }

    @Test
    void 'assert percentage value of color temperature when ct: 500'() {
        def expectedReply = 100
        asserttoColorTemperaturePercentType(500, expectedReply)
    }

    @Test
    void 'assert command for color channel: 0%'() {
        def expectedReply = '{"on" : false}'
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
        def expectedReply = '{"on" : false}'
        assertSendCommandForColor(HSBType.BLACK, new HueLightState(), expectedReply)
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

    @Test
    void 'assert command for brightness channel: off'() {
        def currentState = new HueLightState()
        def expectedReply ='{"on" : false}'
        assertSendCommandForBrightness(OnOffType.OFF, currentState, expectedReply)
    }

    @Test
    void 'assert command for brightness channel: on'() {
        def currentState = new HueLightState()
        def expectedReply ='{"on" : true}'
        assertSendCommandForBrightness(OnOffType.ON, currentState, expectedReply)
    }

    @Test
    void 'assert command for alert channel'() {
        def currentState = new HueLightState().alert('NONE')
        def expectedReply ='{"alert" : "none"}'
        assertSendCommandForAlert(new StringType("NONE"), currentState, expectedReply)

        currentState.alert("NONE")
        expectedReply ='{"alert" : "select"}'
        assertSendCommandForAlert(new StringType("SELECT"), currentState, expectedReply)

        currentState.alert("LSELECT")
        expectedReply ='{"alert" : "lselect"}'
        assertSendCommandForAlert(new StringType("LSELECT"), currentState, expectedReply)
    }

    @Test
    void 'assert command for effect channel'() {
        def currentState = new HueLightState().effect('ON')
        def expectedReply ='{"effect" : "colorloop"}'
        assertSendCommandForEffect(OnOffType.ON, currentState, expectedReply)

        currentState.effect('OFF')
        expectedReply ='{"effect" : "none"}'
        assertSendCommandForEffect(OnOffType.OFF, currentState, expectedReply)
    }

    private void assertSendCommandForColorTempForPar16(Command command, HueLightState currentState, String expectedReply) {
        assertSendCommand(CHANNEL_COLORTEMPERATURE, command, OSRAM_PAR16_LIGHT_THING_TYPE_UID, currentState, expectedReply, OSRAM_MODEL_TYPE_ID, "OSRAM")
    }

    private void assertSendCommandForBrightnessForPar16(Command command, HueLightState currentState, String expectedReply) {
        assertSendCommand(CHANNEL_BRIGHTNESS, command, OSRAM_PAR16_LIGHT_THING_TYPE_UID, currentState, expectedReply, OSRAM_MODEL_TYPE_ID, "OSRAM")
    }

    private void assertSendCommandForColor(Command command, HueLightState currentState, String expectedReply) {
        assertSendCommand(CHANNEL_COLOR, command, COLOR_LIGHT_THING_TYPE_UID, currentState, expectedReply)
    }

    private void assertSendCommandForColorTemp(Command command, HueLightState currentState, String expectedReply) {
        assertSendCommand(CHANNEL_COLORTEMPERATURE, command, COLOR_LIGHT_THING_TYPE_UID, currentState, expectedReply)
    }

    private void asserttoColorTemperaturePercentType(int ctValue, int expectedPercent) {
        int percent = (int) Math.round(((ctValue - MIN_COLOR_TEMPERATURE) * 100.0 )/ COLOR_TEMPERATURE_RANGE);
        assertThat expectedPercent, is(percent)
    }

    private void assertSendCommandForBrightness(Command command, HueLightState currentState, String expectedReply) {
        assertSendCommand(CHANNEL_BRIGHTNESS, command, LUX_LIGHT_THING_TYPE_UID, currentState, expectedReply)
    }

    private void assertSendCommandForAlert(Command command, HueLightState currentState, String expectedReply){
        assertSendCommand(CHANNEL_ALERT, command, COLOR_LIGHT_THING_TYPE_UID, currentState, expectedReply)
    }

    private void assertSendCommandForEffect(Command command, HueLightState currentState, String expectedReply){
        assertSendCommand(CHANNEL_EFFECT, command, COLOR_LIGHT_THING_TYPE_UID, currentState, expectedReply)
    }

    private void assertSendCommand(String channel, Command command, ThingTypeUID hueLightUID, HueLightState currentState, String expectedReply, String expectedModel = "LCT001", String expectedVendor = "Philips") {
        Bridge hueBridge = createBridge()
        simulateBridgeInitialization()

        Thing hueLight = createLight(hueBridge, hueLightUID)

        try {
            HueLightHandler hueLightHandler
            waitForAssert {
                hueLightHandler = getThingHandler(HueLightHandler)
                assertThat hueLightHandler, is(notNullValue())
            }

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
            hueLightHandler.initialize()

            waitForAssert({
                hueLight.with {
                    assertThat properties.get(Thing.PROPERTY_MODEL_ID), is(expectedModel)
                    assertThat properties.get(Thing.PROPERTY_VENDOR), is(expectedVendor)
                }
            })

            postCommand(hueLight, channel, command)

            waitForAssert({assertTrue addressWrapper.isSet}, 10000)
            waitForAssert({assertTrue bodyWrapper.isSet}, 10000)

            assertThat addressWrapper.wrappedObject, is("http://1.2.3.4/api/testUserName/lights/1/state")
            assertJson(expectedReply, bodyWrapper.wrappedObject)
        } finally {
            thingRegistry.forceRemove(hueLight.getUID())
            thingRegistry.forceRemove(hueBridge.getUID())
            waitForAssert({
                assertThat thingRegistry.get(hueLight.getUID()), is(nullValue())
                assertThat thingRegistry.get(hueBridge.getUID()), is(nullValue())
            }, 10000)
        }
    }

    private void simulateBridgeInitialization() {
        HueBridgeHandler.metaClass.initialize = { updateStatus(ThingStatus.ONLINE) }
        HueBridgeHandler bridgeHandler
        waitForAssert {
            bridgeHandler = getThingHandler(HueBridgeHandler)
            assertThat bridgeHandler, is(notNullValue())
        }
        bridgeHandler.metaClass.initialize = { updateStatus(ThingStatus.ONLINE) }
        bridgeHandler.initialize()
    }

    private assertBridgeOnline(Bridge bridge){
        def online = ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build()
        waitForAssert({
            assertThat bridge.getStatusInfo(), is(online)
        }, 10000)
    }

    private postCommand(Thing hueLight, String channel, Command command){

        def item = hueLight.getUID().toString().replace(":", "_") + "_" + channel

        waitForAssert {
            assertThat itemRegistry.get(item), is(notNullValue())
        }

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
        def hueBridgeValue = null

        waitForAssert({
            hueBridgeValue = hueBridgeField.get(hueBridgeHandler)
            assertThat hueBridgeValue, is(notNullValue())
        }, 10000, 100)

        def httpClientField = hueBridgeValue.getClass().getDeclaredField("http")
        httpClientField.accessible = true
        httpClientField.set(hueBridgeValue, mockedHttpClient)

        def usernameField = hueBridgeValue.getClass().getDeclaredField("username")
        usernameField.accessible = true
        usernameField.set(hueBridgeValue, hueBridgeHandler.config.get(USER_NAME))

        hueBridgeHandler.initialize()
    }

}
