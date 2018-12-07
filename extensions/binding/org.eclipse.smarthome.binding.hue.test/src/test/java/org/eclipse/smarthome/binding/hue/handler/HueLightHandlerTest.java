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
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.hue.internal.HueBridge;
import org.eclipse.smarthome.binding.hue.internal.HueLightStateExtended;
import org.eclipse.smarthome.binding.hue.internal.dto.Light;
import org.eclipse.smarthome.binding.hue.internal.dto.LightState.AlertMode;
import org.eclipse.smarthome.binding.hue.internal.dto.LightState.ColorMode;
import org.eclipse.smarthome.binding.hue.internal.dto.LightState.Effect;
import org.eclipse.smarthome.binding.hue.internal.dto.updates.LightStateUpdate;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.Command;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Tests for {@link HueLightHandler}. handleCommand() is checked with various payloads.
 *
 * @author Oliver Libutzki - Initial contribution
 * @author Michael Grammling - Initial contribution
 * @author Markus Mazurczak - Added test for OSRAM Par16 50 TW bulbs
 * @author Andre Fuechsel - modified tests after introducing the generic thing types
 * @author Denis Dudnik - switched to internally integrated source of Jue library
 * @author Simon Kaufmann - migrated to plain Java test
 * @author Christoph Weitkamp - Added support for bulbs using CIE XY colormode only
 * @author David Graeff - Finished migration to plain Java test
 */
public class HueLightHandlerTest {

    private static final int MIN_COLOR_TEMPERATURE = 153;
    private static final int MAX_COLOR_TEMPERATURE = 500;
    private static final int COLOR_TEMPERATURE_RANGE = MAX_COLOR_TEMPERATURE - MIN_COLOR_TEMPERATURE;

    private static final String OSRAM_MODEL_TYPE = "PAR16 50 TW";
    private static final String OSRAM_MODEL_TYPE_ID = "PAR16_50_TW";

    private JsonParser parser = new JsonParser();

    @Mock
    HueBridge mockClient;

    @Mock
    HueBridgeHandler handler;

    @Mock
    Bridge mockBridge;

    @Mock
    Thing mockThing;

    @Mock
    ThingHandlerCallback callback;

    HueLightStateExtended subject;

    Gson gson = new GsonBuilder().setDateFormat(HueBridge.DATE_FORMAT).create();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(handler.getHueBridge()).thenReturn(mockClient);
        when(mockBridge.getHandler()).thenReturn(handler);
        when(mockBridge.getStatusInfo())
                .thenReturn(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        when(mockThing.getConfiguration())
                .thenReturn(new Configuration(Collections.singletonMap(HueLightHandlerConfig.LIGHT_ID, "1")));
        subject = new HueLightStateExtended();
    }

    @Test
    public void assertCommandForOsramPar16_50ForColorTemperatureChannelOn() {
        String expectedReply = "{\"on\" : true, \"bri\" : 254}";
        assertSendCommandPar16(CHANNEL_COLORTEMPERATURE, OnOffType.ON, subject.withModel(OSRAM_MODEL_TYPE),
                expectedReply);
    }

    @Test
    public void assertCommandForOsramPar16_50ForColorTemperatureChannelOff() {
        String expectedReply = "{\"on\" : false, \"transitiontime\" : 0}";
        assertSendCommandPar16(CHANNEL_COLORTEMPERATURE, OnOffType.OFF, subject.withModel(OSRAM_MODEL_TYPE),
                expectedReply);
    }

    @Test
    public void assertCommandForOsramPar16_50ForBrightnessChannelOn() {
        String expectedReply = "{\"on\" : true, \"bri\" : 254}";
        assertSendCommandPar16(CHANNEL_BRIGHTNESS, OnOffType.ON, subject.withModel(OSRAM_MODEL_TYPE), expectedReply);
    }

    @Test
    public void assertCommandForOsramPar16_50ForBrightnessChannelOff() {
        String expectedReply = "{\"on\" : false, \"transitiontime\" : 0}";
        assertSendCommandPar16(CHANNEL_BRIGHTNESS, OnOffType.OFF, subject.withModel(OSRAM_MODEL_TYPE), expectedReply);
    }

    @Test
    public void assertCommandForColorChannelOn() {
        String expectedReply = "{\"on\" : true}";
        assertSendCommandLCT001(CHANNEL_COLOR, OnOffType.ON, subject, expectedReply);
    }

    @Test
    public void assertCommandForColorTemperatureChannelOn() {
        String expectedReply = "{\"on\" : true}";
        assertSendCommandLCT001(CHANNEL_COLORTEMPERATURE, OnOffType.ON, subject, expectedReply);
    }

    @Test
    public void assertCommandForColorChannelOff() {
        String expectedReply = "{\"on\" : false}";
        assertSendCommandLCT001(CHANNEL_COLOR, OnOffType.OFF, subject, expectedReply);
    }

    @Test
    public void assertCommandForColorTemperatureChannelOff() {
        String expectedReply = "{\"on\" : false}";
        assertSendCommandLCT001(CHANNEL_COLORTEMPERATURE, OnOffType.OFF, subject, expectedReply);
    }

    @Test
    public void assertCommandForColorTemperatureChannel0Percent() {
        String expectedReply = "{\"ct\" : 153}";
        assertSendCommandLCT001(CHANNEL_COLORTEMPERATURE, new PercentType(0), subject, expectedReply);
    }

    @Test
    public void assertCommandForColorTemperatureChannel50Percent() {
        String expectedReply = "{\"ct\" : 327}";
        assertSendCommandLCT001(CHANNEL_COLORTEMPERATURE, new PercentType(50), subject, expectedReply);
    }

    @Test
    public void assertCommandForColorTemperatureChannel1000Percent() {
        String expectedReply = "{\"ct\" : 500}";
        assertSendCommandLCT001(CHANNEL_COLORTEMPERATURE, new PercentType(100), subject, expectedReply);
    }

    @Test
    public void assertCommandForColorChannel0Percent() {
        String expectedReply = "{\"on\" : false}";
        assertSendCommandLCT001(CHANNEL_COLOR, new PercentType(0), new HueLightStateExtended(), expectedReply);
    }

    @Test
    public void assertCommandForColorChannel50Percent() {
        String expectedReply = "{\"bri\" : 127, \"on\" : true}";
        assertSendCommandLCT001(CHANNEL_COLOR, new PercentType(50), new HueLightStateExtended(), expectedReply);
    }

    @Test
    public void assertCommandForColorChannel100Percent() {
        String expectedReply = "{\"bri\" : 254, \"on\" : true}";
        assertSendCommandLCT001(CHANNEL_COLOR, new PercentType(100), new HueLightStateExtended(), expectedReply);
    }

    @Test
    public void assertCommandForColorChannelBlack() {
        String expectedReply = "{\"on\" : false}";
        assertSendCommandLCT001(CHANNEL_COLOR, HSBType.BLACK, new HueLightStateExtended(), expectedReply);
    }

    @Test
    public void assertCommandForColorChannelRed() {
        String expectedReply = "{\"bri\" : 254, \"sat\" : 254, \"hue\" : 0}";
        assertSendCommandLCT001(CHANNEL_COLOR, HSBType.RED, new HueLightStateExtended(), expectedReply);
    }

    @Test
    public void assertCommandForColorChannelBlue() {
        String expectedReply = "{\"bri\" : 254, \"sat\" : 254, \"hue\" : 43680}";
        assertSendCommandLCT001(CHANNEL_COLOR, HSBType.BLUE, new HueLightStateExtended(), expectedReply);
    }

    @Test
    public void assertCommandForColorChannelWhite() {
        String expectedReply = "{\"bri\" : 254, \"sat\" : 0, \"hue\" : 0}";
        assertSendCommandLCT001(CHANNEL_COLOR, HSBType.WHITE, new HueLightStateExtended(), expectedReply);
    }

    @Test
    public void assertXYCommandForColorChannelBlack() {
        String expectedReply = "{\"on\" : false}";
        subject.colormode = ColorMode.xy;
        assertSendCommandLCT001(CHANNEL_COLOR, HSBType.BLACK, subject, expectedReply);
    }

    @Test
    public void assertXYCommandForColorChannelWhite() {
        String expectedReply = "{\"bri\" : 254,\"xy\" : [ 0.31271592 , 0.32900152 ]}";
        subject.colormode = ColorMode.xy;
        assertSendCommandLCT001(CHANNEL_COLOR, HSBType.WHITE, subject, expectedReply);
    }

    @Test
    public void assertXYCommandForColorChannelColorful() {
        String expectedReply = "{\"bri\" : 127,\"xy\" : [ 0.16969365 , 0.12379659 ]}";
        subject.colormode = ColorMode.xy;
        assertSendCommandLCT001(CHANNEL_COLOR, new HSBType("220,90,50"), subject, expectedReply);
    }

    @Test
    public void asserCommandForColorChannelIncrease() {
        subject.bri = 1;
        subject.on = false;
        String expectedReply = "{\"bri\" : 31, \"on\" : true}";
        assertSendCommandLCT001(CHANNEL_COLOR, IncreaseDecreaseType.INCREASE, subject, expectedReply);

        subject.bri = 200;
        subject.on = true;
        expectedReply = "{\"bri\" : 230}";
        assertSendCommandLCT001(CHANNEL_COLOR, IncreaseDecreaseType.INCREASE, subject, expectedReply);

        subject.bri = 230;
        expectedReply = "{\"bri\" : 254}";
        assertSendCommandLCT001(CHANNEL_COLOR, IncreaseDecreaseType.INCREASE, subject, expectedReply);
    }

    @Test
    public void asserCommandForColorChannelDecrease() {
        subject.bri = 200;
        subject.on = true;
        String expectedReply = "{\"bri\" : 170}";
        assertSendCommandLCT001(CHANNEL_COLOR, IncreaseDecreaseType.DECREASE, subject, expectedReply);

        subject.bri = 20;
        expectedReply = "{\"on\" : false}";
        assertSendCommandLCT001(CHANNEL_COLOR, IncreaseDecreaseType.DECREASE, subject, expectedReply);
    }

    @Test
    public void assertCommandForBrightnessChannel50Percent() {
        String expectedReply = "{\"bri\" : 127, \"on\" : true}";
        assertSendCommandLCT001(CHANNEL_BRIGHTNESS, new PercentType(50), subject, expectedReply);
    }

    @Test
    public void assertCommandForBrightnessChannelIncrease() {
        subject.bri = 1;
        subject.on = false;
        String expectedReply = "{\"bri\" : 31, \"on\" : true}";
        assertSendCommandLCT001(CHANNEL_BRIGHTNESS, IncreaseDecreaseType.INCREASE, subject, expectedReply);

        subject.bri = 200;
        subject.on = true;
        expectedReply = "{\"bri\" : 230}";
        assertSendCommandLCT001(CHANNEL_BRIGHTNESS, IncreaseDecreaseType.INCREASE, subject, expectedReply);

        subject.bri = 230;
        expectedReply = "{\"bri\" : 254}";
        assertSendCommandLCT001(CHANNEL_BRIGHTNESS, IncreaseDecreaseType.INCREASE, subject, expectedReply);
    }

    @Test
    public void assertCommandForBrightnessChannelDecrease() {
        subject.bri = 200;
        subject.on = true;
        String expectedReply = "{\"bri\" : 170}";
        assertSendCommandLCT001(CHANNEL_BRIGHTNESS, IncreaseDecreaseType.DECREASE, subject, expectedReply);

        subject.bri = 20;
        expectedReply = "{\"on\" : false}";
        assertSendCommandLCT001(CHANNEL_BRIGHTNESS, IncreaseDecreaseType.DECREASE, subject, expectedReply);
    }

    @Test
    public void assertCommandForBrightnessChannelOff() {
        HueLightStateExtended currentState = new HueLightStateExtended();
        String expectedReply = "{\"on\" : false}";
        assertSendCommandLCT001(CHANNEL_BRIGHTNESS, OnOffType.OFF, currentState, expectedReply);
    }

    @Test
    public void assertCommandForBrightnessChannelOn() {
        HueLightStateExtended currentState = new HueLightStateExtended();
        String expectedReply = "{\"on\" : true}";
        assertSendCommandLCT001(CHANNEL_BRIGHTNESS, OnOffType.ON, currentState, expectedReply);
    }

    @Test
    public void assertCommandForAlertChannel() {
        subject.alert = AlertMode.none;
        String expectedReply = "{\"alert\" : \"none\"}";
        assertSendCommandLCT001(CHANNEL_ALERT, new StringType("NONE"), subject, expectedReply);

        subject.alert = AlertMode.select;
        expectedReply = "{\"alert\" : \"select\"}";
        assertSendCommandLCT001(CHANNEL_ALERT, new StringType("SELECT"), subject, expectedReply);

        subject.alert = AlertMode.lselect;
        expectedReply = "{\"alert\" : \"lselect\"}";
        assertSendCommandLCT001(CHANNEL_ALERT, new StringType("LSELECT"), subject, expectedReply);
    }

    @Test
    public void assertCommandForEffectChannel() {
        subject.effect = Effect.colorloop;
        String expectedReply = "{\"effect\" : \"colorloop\"}";
        assertSendCommandLCT001(CHANNEL_EFFECT, OnOffType.ON, subject, expectedReply);

        subject.effect = Effect.none;
        expectedReply = "{\"effect\" : \"none\"}";
        assertSendCommandLCT001(CHANNEL_EFFECT, OnOffType.OFF, subject, expectedReply);
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

    private void asserttoColorTemperaturePercentType(int ctValue, int expectedPercent) {
        int percent = (int) Math.round(((ctValue - MIN_COLOR_TEMPERATURE) * 100.0) / COLOR_TEMPERATURE_RANGE);
        assertEquals(percent, expectedPercent);
    }

    private void assertSendCommandPar16(String channel, Command command, HueLightStateExtended currentState,
            String expectedReply) {
        assertSendCommand(channel, command, currentState, parser.parse(expectedReply), OSRAM_MODEL_TYPE_ID, "OSRAM");
    }

    private void assertSendCommandLCT001(String channel, Command command, HueLightStateExtended currentState,
            String expectedReply) {
        assertSendCommand(channel, command, currentState, parser.parse(expectedReply), "LCT001", "Philips");
    }

    private void assertSendCommand(String channel, Command command, HueLightStateExtended currentState,
            JsonElement expectedReply, String expectedModel, String expectedVendor) {

        final Light lightWithState = new Light();
        lightWithState.state = currentState;
        lightWithState.modelid = currentState.model;

        when(mockClient.getLightById(eq("1"))).thenReturn(lightWithState);
        when(mockClient.setLightState(any(), any())).thenReturn(CompletableFuture.completedFuture(null));

        HueLightHandler hueLightHandler = new HueLightHandler(mockThing) {
            @Override
            protected @Nullable Bridge getBridge() {
                return mockBridge;
            }

            @Override
            public void updateLightState(@NonNull LightStateUpdate LightStateUpdate) {
                assertThat(parser.parse(gson.toJson(LightStateUpdate)), is(expectedReply));
            }
        };
        hueLightHandler.setCallback(callback);
        hueLightHandler.initialize();

        verify(mockClient, atLeast(1)).registerLightStatusListener(any());
        verify(mockThing, atLeast(4)).setProperty(any(), any());

        hueLightHandler.handleCommand(new ChannelUID(new ThingUID("hue::test"), channel), command);
    }
}
