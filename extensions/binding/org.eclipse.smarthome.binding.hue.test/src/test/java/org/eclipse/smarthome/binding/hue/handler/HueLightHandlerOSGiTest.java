/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.hue.handler;

import static org.eclipse.smarthome.binding.hue.HueBindingConstants.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.smarthome.binding.hue.internal.MockedHttpClient;
import org.eclipse.smarthome.binding.hue.test.AbstractHueOSGiTest;
import org.eclipse.smarthome.binding.hue.test.HueLightState;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.test.AsyncResultWrapper;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Tests for {@link HueLightHandler}.
 *
 * @author Oliver Libutzki - Initial contribution
 * @author Michael Grammling - Initial contribution
 * @author Markus Mazurczak - Added test for OSRAM Par16 50 TW bulbs
 * @author Andre Fuechsel - modified tests after introducing the generic thing types
 * @author Denis Dudnik - switched to internally integrated source of Jue library
 */
public class HueLightHandlerOSGiTest extends AbstractHueOSGiTest {

    private static final int MIN_COLOR_TEMPERATURE = 153;
    private static final int MAX_COLOR_TEMPERATURE = 500;
    private static final int COLOR_TEMPERATURE_RANGE = MAX_COLOR_TEMPERATURE - MIN_COLOR_TEMPERATURE;

    private static final ThingTypeUID BRIDGE_THING_TYPE_UID = new ThingTypeUID("hue", "bridge");
    private static final ThingTypeUID COLOR_LIGHT_THING_TYPE_UID = new ThingTypeUID("hue", "0210");
    private static final ThingTypeUID LUX_LIGHT_THING_TYPE_UID = new ThingTypeUID("hue", "0100");
    private static final ThingTypeUID OSRAM_PAR16_LIGHT_THING_TYPE_UID = new ThingTypeUID("hue", "0220");
    private static final String OSRAM_MODEL_TYPE = "PAR16 50 TW";
    private static final String OSRAM_MODEL_TYPE_ID = "PAR16_50_TW";

    private ThingRegistry thingRegistry;
    private ItemChannelLinkRegistry linkRegistry;
    private ItemRegistry itemRegistry;

    @Before
    public void setUp() {
        registerVolatileStorageService();
        thingRegistry = getService(ThingRegistry.class, ThingRegistry.class);
        assertNotNull(thingRegistry);
        linkRegistry = getService(ItemChannelLinkRegistry.class, ItemChannelLinkRegistry.class);
        assertNotNull(linkRegistry);
        itemRegistry = getService(ItemRegistry.class, ItemRegistry.class);
        assertNotNull(itemRegistry);
    }

    private Bridge createBridge() {
        Configuration bridgeConfiguration = new Configuration();
        bridgeConfiguration.put(HOST, "1.2.3.4");
        bridgeConfiguration.put(USER_NAME, "testUserName");
        bridgeConfiguration.put(SERIAL_NUMBER, "testSerialNumber");

        Bridge hueBridge = (Bridge) thingRegistry.createThingOfType(BRIDGE_THING_TYPE_UID,
                new ThingUID(BRIDGE_THING_TYPE_UID, "testBridge"), null, "Bridge", bridgeConfiguration);

        assertNotNull(hueBridge);
        thingRegistry.add(hueBridge);

        return hueBridge;
    }

    private Thing createLight(Bridge hueBridge, ThingTypeUID lightUID) {
        Configuration lightConfiguration = new Configuration();
        lightConfiguration.put(LIGHT_ID, "1");

        Thing hueLight = thingRegistry.createThingOfType(lightUID, new ThingUID(lightUID, "Light1"), hueBridge.getUID(),
                "Light", lightConfiguration);

        assertNotNull(hueLight);
        thingRegistry.add(hueLight);

        for (Channel c : hueLight.getChannels()) {
            String item = hueLight.getUID().toString().replace(":", "_") + "_" + c.getUID().getId();
            if (linkRegistry.getBoundChannels(item).size() == 0) {
                linkRegistry.add(new ItemChannelLink(item, c.getUID()));
            }
        }

        return hueLight;
    }

    @Test
    public void assertCommandForOsramPar16_50ForColorTemperatureChannelOn() {
        String expectedReply = "{\"on\" : true, \"bri\" : 254}";
        assertSendCommandForColorTempForPar16(OnOffType.ON, new HueLightState(OSRAM_MODEL_TYPE), expectedReply);
    }

    @Test
    public void assertCommandForOsramPar16_50ForColorTemperatureChannelOff() {
        String expectedReply = "{\"on\" : false, \"transitiontime\" : 0}";
        assertSendCommandForColorTempForPar16(OnOffType.OFF, new HueLightState(OSRAM_MODEL_TYPE), expectedReply);
    }

    @Test
    public void assertCommandForOsramPar16_50ForBrightnessChannelOn() {
        String expectedReply = "{\"on\" : true, \"bri\" : 254}";
        assertSendCommandForBrightnessForPar16(OnOffType.ON, new HueLightState(OSRAM_MODEL_TYPE), expectedReply);
    }

    @Test
    public void assertCommandForOsramPar16_50ForBrightnessChannelOff() {
        String expectedReply = "{\"on\" : false, \"transitiontime\" : 0}";
        assertSendCommandForBrightnessForPar16(OnOffType.OFF, new HueLightState(OSRAM_MODEL_TYPE), expectedReply);
    }

    @Test
    public void assertCommandForColorChannelOn() {
        String expectedReply = "{\"on\" : true}";
        assertSendCommandForColor(OnOffType.ON, new HueLightState(), expectedReply);
    }

    @Test
    public void assertCommandForColorTemperatureChannelOn() {
        String expectedReply = "{\"on\" : true}";
        assertSendCommandForColorTemp(OnOffType.ON, new HueLightState(), expectedReply);
    }

    @Test
    public void assertCommandForColorChannelOff() {
        String expectedReply = "{\"on\" : false}";
        assertSendCommandForColor(OnOffType.OFF, new HueLightState(), expectedReply);
    }

    @Test
    public void assertCommandForColorTemperatureChannelOff() {
        String expectedReply = "{\"on\" : false}";
        assertSendCommandForColorTemp(OnOffType.OFF, new HueLightState(), expectedReply);
    }

    @Test
    public void assertCommandForColorTemperatureChannel0Percent() {
        String expectedReply = "{\"ct\" : 153}";
        assertSendCommandForColorTemp(new PercentType(0), new HueLightState(), expectedReply);
    }

    @Test
    public void assertCommandForColorTemperatureChannel50Percent() {
        String expectedReply = "{\"ct\" : 327}";
        assertSendCommandForColorTemp(new PercentType(50), new HueLightState(), expectedReply);
    }

    @Test
    public void assertCommandForColorTemperatureChannel1000Percent() {
        String expectedReply = "{\"ct\" : 500}";
        assertSendCommandForColorTemp(new PercentType(100), new HueLightState(), expectedReply);
    }

    @Test
    public void assertPercentageValueOfColorTemperatureWhenCt153() {
        int expectedReply = 0;
        asserttoColorTemperaturePercentType(153, expectedReply);
    }

    @Test
    public void assertPercentageValueOfColorTemperatureWhenCt326() {
        int expectedReply = 50;
        asserttoColorTemperaturePercentType(326, expectedReply);
    }

    @Test
    public void assertPercentageValueOfColorTemperatureWhenCt500() {
        int expectedReply = 100;
        asserttoColorTemperaturePercentType(500, expectedReply);
    }

    @Test
    public void assertCommandForColorChannel0Percent() {
        String expectedReply = "{\"on\" : false}";
        assertSendCommandForColor(new PercentType(0), new HueLightState(), expectedReply);
    }

    @Test
    public void assertCommandForColorChannel50Percent() {
        String expectedReply = "{\"bri\" : 127, \"on\" : true}";
        assertSendCommandForColor(new PercentType(50), new HueLightState(), expectedReply);
    }

    @Test
    public void assertCommandForColorChannel100Percent() {
        String expectedReply = "{\"bri\" : 254, \"on\" : true}";
        assertSendCommandForColor(new PercentType(100), new HueLightState(), expectedReply);
    }

    @Test
    public void assertCommandForColorChannelBlack() {
        String expectedReply = "{\"on\" : false}";
        assertSendCommandForColor(HSBType.BLACK, new HueLightState(), expectedReply);
    }

    @Test
    public void assertCommandForColorChannelRed() {
        String expectedReply = "{\"bri\" : 254, \"sat\" : 254, \"hue\" : 0}";
        assertSendCommandForColor(HSBType.RED, new HueLightState(), expectedReply);
    }

    @Test
    public void assertCommandForColorChannelBlue() {
        String expectedReply = "{\"bri\" : 254, \"sat\" : 254, \"hue\" : 43680}";
        assertSendCommandForColor(HSBType.BLUE, new HueLightState(), expectedReply);
    }

    @Test
    public void assertCommandForColorChannelWhite() {
        String expectedReply = "{\"bri\" : 254, \"sat\" : 0, \"hue\" : 0}";
        assertSendCommandForColor(HSBType.WHITE, new HueLightState(), expectedReply);
    }

    @Test
    public void asserCommandForColorChannelIncrease() {
        HueLightState currentState = new HueLightState().bri(1).on(false);
        String expectedReply = "{\"bri\" : 30, \"on\" : true}";
        assertSendCommandForColor(IncreaseDecreaseType.INCREASE, currentState, expectedReply);

        currentState.bri(200).on(true);
        expectedReply = "{\"bri\" : 230}";
        assertSendCommandForColor(IncreaseDecreaseType.INCREASE, currentState, expectedReply);

        currentState.bri(230);
        expectedReply = "{\"bri\" : 254}";
        assertSendCommandForColor(IncreaseDecreaseType.INCREASE, currentState, expectedReply);
    }

    @Test
    public void asserCommandForColorChannelDecrease() {
        HueLightState currentState = new HueLightState().bri(200);
        String expectedReply = "{\"bri\" : 170}";
        assertSendCommandForColor(IncreaseDecreaseType.DECREASE, currentState, expectedReply);

        currentState.bri(20);
        expectedReply = "{\"on\" : false}";
        assertSendCommandForColor(IncreaseDecreaseType.DECREASE, currentState, expectedReply);
    }

    @Test
    public void assertCommandForBrightnessChannel50Percent() {
        HueLightState currentState = new HueLightState();
        String expectedReply = "{\"bri\" : 127, \"on\" : true}";
        assertSendCommandForBrightness(new PercentType(50), currentState, expectedReply);
    }

    @Test
    public void assertCommandForBrightnessChannelIncrease() {
        HueLightState currentState = new HueLightState().bri(1).on(false);
        String expectedReply = "{\"bri\" : 30, \"on\" : true}";
        assertSendCommandForBrightness(IncreaseDecreaseType.INCREASE, currentState, expectedReply);

        currentState.bri(200).on(true);
        expectedReply = "{\"bri\" : 230}";
        assertSendCommandForBrightness(IncreaseDecreaseType.INCREASE, currentState, expectedReply);

        currentState.bri(230);
        expectedReply = "{\"bri\" : 254}";
        assertSendCommandForBrightness(IncreaseDecreaseType.INCREASE, currentState, expectedReply);
    }

    @Test
    public void assertCommandForBrightnessChannelDecrease() {
        HueLightState currentState = new HueLightState().bri(200);
        String expectedReply = "{\"bri\" : 170}";
        assertSendCommandForBrightness(IncreaseDecreaseType.DECREASE, currentState, expectedReply);

        currentState.bri(20);
        expectedReply = "{\"on\" : false}";
        assertSendCommandForBrightness(IncreaseDecreaseType.DECREASE, currentState, expectedReply);
    }

    @Test
    public void assertCommandForBrightnessChannelOff() {
        HueLightState currentState = new HueLightState();
        String expectedReply = "{\"on\" : false}";
        assertSendCommandForBrightness(OnOffType.OFF, currentState, expectedReply);
    }

    @Test
    public void assertCommandForBrightnessChannelOn() {
        HueLightState currentState = new HueLightState();
        String expectedReply = "{\"on\" : true}";
        assertSendCommandForBrightness(OnOffType.ON, currentState, expectedReply);
    }

    @Test
    public void assertCommandForAlertChannel() {
        HueLightState currentState = new HueLightState().alert("NONE");
        String expectedReply = "{\"alert\" : \"none\"}";
        assertSendCommandForAlert(new StringType("NONE"), currentState, expectedReply);

        currentState.alert("NONE");
        expectedReply = "{\"alert\" : \"select\"}";
        assertSendCommandForAlert(new StringType("SELECT"), currentState, expectedReply);

        currentState.alert("LSELECT");
        expectedReply = "{\"alert\" : \"lselect\"}";
        assertSendCommandForAlert(new StringType("LSELECT"), currentState, expectedReply);
    }

    @Test
    public void assertCommandForEffectChannel() {
        HueLightState currentState = new HueLightState().effect("ON");
        String expectedReply = "{\"effect\" : \"colorloop\"}";
        assertSendCommandForEffect(OnOffType.ON, currentState, expectedReply);

        currentState.effect("OFF");
        expectedReply = "{\"effect\" : \"none\"}";
        assertSendCommandForEffect(OnOffType.OFF, currentState, expectedReply);
    }

    private void assertSendCommandForColorTempForPar16(Command command, HueLightState currentState,
            String expectedReply) {
        assertSendCommand(CHANNEL_COLORTEMPERATURE, command, OSRAM_PAR16_LIGHT_THING_TYPE_UID, currentState,
                expectedReply, OSRAM_MODEL_TYPE_ID, "OSRAM");
    }

    private void assertSendCommandForBrightnessForPar16(Command command, HueLightState currentState,
            String expectedReply) {
        assertSendCommand(CHANNEL_BRIGHTNESS, command, OSRAM_PAR16_LIGHT_THING_TYPE_UID, currentState, expectedReply,
                OSRAM_MODEL_TYPE_ID, "OSRAM");
    }

    private void assertSendCommandForColor(Command command, HueLightState currentState, String expectedReply) {
        assertSendCommand(CHANNEL_COLOR, command, COLOR_LIGHT_THING_TYPE_UID, currentState, expectedReply);
    }

    private void assertSendCommandForColorTemp(Command command, HueLightState currentState, String expectedReply) {
        assertSendCommand(CHANNEL_COLORTEMPERATURE, command, COLOR_LIGHT_THING_TYPE_UID, currentState, expectedReply);
    }

    private void asserttoColorTemperaturePercentType(int ctValue, int expectedPercent) {
        int percent = (int) Math.round(((ctValue - MIN_COLOR_TEMPERATURE) * 100.0) / COLOR_TEMPERATURE_RANGE);
        assertEquals(percent, expectedPercent);
    }

    private void assertSendCommandForBrightness(Command command, HueLightState currentState, String expectedReply) {
        assertSendCommand(CHANNEL_BRIGHTNESS, command, LUX_LIGHT_THING_TYPE_UID, currentState, expectedReply);
    }

    private void assertSendCommandForAlert(Command command, HueLightState currentState, String expectedReply) {
        assertSendCommand(CHANNEL_ALERT, command, COLOR_LIGHT_THING_TYPE_UID, currentState, expectedReply);
    }

    private void assertSendCommandForEffect(Command command, HueLightState currentState, String expectedReply) {
        assertSendCommand(CHANNEL_EFFECT, command, COLOR_LIGHT_THING_TYPE_UID, currentState, expectedReply);
    }

    private void assertSendCommand(String channel, Command command, ThingTypeUID hueLightUID,
            HueLightState currentState, String expectedReply) {
        assertSendCommand(channel, command, hueLightUID, currentState, expectedReply, "LCT001", "Philips");
    }

    private void assertSendCommand(String channel, Command command, ThingTypeUID hueLightUID,
            HueLightState currentState, String expectedReply, String expectedModel, String expectedVendor) {
        Bridge hueBridge = null;
        Thing hueLight = null;
        try {
            hueBridge = createBridge();

            AsyncResultWrapper<String> addressWrapper = new AsyncResultWrapper<String>();
            AsyncResultWrapper<String> bodyWrapper = new AsyncResultWrapper<String>();

            MockedHttpClient mockedHttpClient = new MockedHttpClient() {
                @Override
                public Result put(String address, String body) throws IOException {
                    addressWrapper.set(address);
                    bodyWrapper.set(body);
                    return new Result("", 200);
                }

                @Override
                public Result get(String address) throws IOException {
                    if (address.endsWith("testUserName")) {
                        return new Result(currentState.toString(), 200);
                    }
                    return new Result("", 404);
                }

            };

            installHttpClientMock((HueBridgeHandler) hueBridge.getHandler(), mockedHttpClient);
            simulateBridgeInitialization(hueBridge);

            hueLight = createLight(hueBridge, hueLightUID);
            HueLightHandler hueLightHandler = getThingHandler(hueLight, HueLightHandler.class);

            assertBridgeOnline(getBridge(hueLightHandler));
            hueLightHandler.initialize();
            Thing light = hueLight;
            waitForAssert(() -> {
                assertEquals(expectedModel, light.getProperties().get(Thing.PROPERTY_MODEL_ID));
                assertEquals(expectedVendor, light.getProperties().get(Thing.PROPERTY_VENDOR));
            });

            postCommand(hueLight, channel, command);

            waitForAssert(() -> assertTrue(addressWrapper.isSet()));
            waitForAssert(() -> assertTrue(bodyWrapper.isSet()));

            assertEquals("http://1.2.3.4/api/testUserName/lights/1/state", addressWrapper.getWrappedObject());
            assertJson(expectedReply, bodyWrapper.getWrappedObject());

        } finally {
            if (hueLight != null) {
                ThingUID uid = hueLight.getUID();
                thingRegistry.forceRemove(uid);
                waitForAssert(() -> assertNull(thingRegistry.get(uid)));
            }
            if (hueBridge != null) {
                ThingUID uid = hueBridge.getUID();
                thingRegistry.forceRemove(uid);
                waitForAssert(() -> assertNull(thingRegistry.get(uid)));
            }
        }
    }

    private void simulateBridgeInitialization(Bridge bridge) {
        try {
            HueBridgeHandler bridgeHandler;
            bridgeHandler = getThingHandler(bridge, HueBridgeHandler.class);
            assertNotNull(bridgeHandler);
            Method method = BaseThingHandler.class.getDeclaredMethod("updateStatus", ThingStatus.class);
            method.setAccessible(true);
            method.invoke((BaseThingHandler) bridgeHandler, ThingStatus.ONLINE);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private void assertBridgeOnline(Bridge bridge) {
        ThingStatusInfo online = ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build();
        waitForAssert(() -> assertEquals(online, bridge.getStatusInfo()));
    }

    private void postCommand(Thing hueLight, String channel, Command command) {
        String item = hueLight.getUID().toString().replace(":", "_") + "_" + channel;
        waitForAssert(() -> assertNotNull(itemRegistry.get(item)));

        EventPublisher eventPublisher = getService(EventPublisher.class);
        assertNotNull(eventPublisher);

        eventPublisher.post(ItemEventFactory.createCommandEvent(item, command));
    }

    private void assertJson(String expected, String actual) {
        JsonParser parser = new JsonParser();
        JsonElement jsonExpected = parser.parse(expected);
        JsonElement jsonActual = parser.parse(actual);
        assertEquals(jsonExpected, jsonActual);
    }

    private void installHttpClientMock(HueBridgeHandler hueBridgeHandler, MockedHttpClient mockedHttpClient) {
        try {
            // mock HttpClient
            Field hueBridgeField = hueBridgeHandler.getClass().getDeclaredField("hueBridge");
            hueBridgeField.setAccessible(true);

            waitForAssert(() -> {
                try {
                    assertNotNull(hueBridgeField.get(hueBridgeHandler));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new AssertionError(e);
                }
            });
            Object hueBridgeValue = hueBridgeField.get(hueBridgeHandler);

            Field httpClientField = hueBridgeValue.getClass().getDeclaredField("http");
            httpClientField.setAccessible(true);
            httpClientField.set(hueBridgeValue, mockedHttpClient);

            Field usernameField = hueBridgeValue.getClass().getDeclaredField("username");
            usernameField.setAccessible(true);
            usernameField.set(hueBridgeValue, hueBridgeHandler.getThing().getConfiguration().get(USER_NAME));

            hueBridgeHandler.initialize();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Bridge getBridge(ThingHandler handler) {
        ThingUID bridgeUID = handler.getThing().getBridgeUID();
        if (bridgeUID != null && thingRegistry != null) {
            return (Bridge) thingRegistry.get(bridgeUID);
        } else {
            return null;
        }
    }

}
