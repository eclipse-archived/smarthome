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
package org.eclipse.smarthome.binding.dmx;

import static org.eclipse.smarthome.binding.dmx.internal.DmxBindingConstants.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.dmx.handler.ColorThingHandler;
import org.eclipse.smarthome.binding.dmx.handler.DimmerThingHandler;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests cases for {@link DimmerThingHandler} in RGB mode.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class ColorThingHandlerTest extends AbstractDmxThingTest {

    private static final String TEST_CHANNEL_CONFIG = "100/3";
    private static final int TEST_FADE_TIME = 1500;
    private static final HSBType TEST_COLOR = new HSBType(new DecimalType(280), new PercentType(100),
            new PercentType(100));

    private Map<String, Object> thingProperties;
    private Thing dimmerThing;
    private ColorThingHandler dimmerThingHandler;

    private final ThingUID THING_UID_DIMMER = new ThingUID(THING_TYPE_COLOR, "testdimmer");
    private final ChannelUID CHANNEL_UID_COLOR = new ChannelUID(THING_UID_DIMMER, CHANNEL_COLOR);
    private final ChannelUID CHANNEL_UID_BRIGHTNESS_R = new ChannelUID(THING_UID_DIMMER, CHANNEL_BRIGHTNESS_R);
    private final ChannelUID CHANNEL_UID_BRIGHTNESS_G = new ChannelUID(THING_UID_DIMMER, CHANNEL_BRIGHTNESS_G);
    private final ChannelUID CHANNEL_UID_BRIGHTNESS_B = new ChannelUID(THING_UID_DIMMER, CHANNEL_BRIGHTNESS_B);

    @Before
    public void setUp() {
        super.setup();
        thingProperties = new HashMap<>();
        thingProperties.put(CONFIG_DMX_ID, TEST_CHANNEL_CONFIG);
        thingProperties.put(CONFIG_DIMMER_FADE_TIME, TEST_FADE_TIME);
        thingProperties.put(CONFIG_DIMMER_TURNONVALUE, "255,128,0");
        dimmerThing = ThingBuilder.create(THING_TYPE_COLOR, "testdimmer").withLabel("Dimmer Thing")
                .withBridge(bridge.getUID()).withConfiguration(new Configuration(thingProperties))
                .withChannel(ChannelBuilder.create(CHANNEL_UID_BRIGHTNESS_R, "Brightness R")
                        .withType(BRIGHTNESS_CHANNEL_TYPEUID).build())
                .withChannel(ChannelBuilder.create(CHANNEL_UID_BRIGHTNESS_G, "Brightness G")
                        .withType(BRIGHTNESS_CHANNEL_TYPEUID).build())
                .withChannel(ChannelBuilder.create(CHANNEL_UID_BRIGHTNESS_B, "Brightness B")
                        .withType(BRIGHTNESS_CHANNEL_TYPEUID).build())
                .withChannel(ChannelBuilder.create(CHANNEL_UID_COLOR, "Color").withType(COLOR_CHANNEL_TYPEUID).build())
                .build();
        dimmerThingHandler = new ColorThingHandler(dimmerThing) {
            @Override
            protected @Nullable Bridge getBridge() {
                return bridge;
            }
        };
        initializeHandler(dimmerThingHandler);
    }

    @Test
    public void testThingStatus() {
        assertThingStatus(dimmerThing);
    }

    @Test
    public void testThingStatus_noBridge() {
        // check that thing is offline if no bridge found
        ColorThingHandler dimmerHandlerWithoutBridge = new ColorThingHandler(dimmerThing) {
            @Override
            protected @Nullable Bridge getBridge() {
                return null;
            }
        };
        assertThingStatusWithoutBridge(dimmerHandlerWithoutBridge);
    }

    @Test
    public void testOnOffCommand() {
        // on
        long currentTime = System.currentTimeMillis();

        dimmerThingHandler.handleCommand(CHANNEL_UID_COLOR, OnOffType.ON);
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> {
            assertChannelStateUpdate(CHANNEL_UID_COLOR, state -> assertEquals(OnOffType.ON, state.as(OnOffType.class)));
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS_R, state -> assertEquals(PercentType.HUNDRED, state));
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS_G,
                    state -> assertThat(((PercentType) state).doubleValue(), is(closeTo(50.0, 0.5))));
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS_B, state -> assertEquals(PercentType.ZERO, state));
        });

        // off
        dimmerThingHandler.handleCommand(CHANNEL_UID_COLOR, OnOffType.OFF);
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> {
            assertChannelStateUpdate(CHANNEL_UID_COLOR,
                    state -> assertEquals(OnOffType.OFF, state.as(OnOffType.class)));
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS_R, state -> assertEquals(PercentType.ZERO, state));
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS_G, state -> assertEquals(PercentType.ZERO, state));
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS_B, state -> assertEquals(PercentType.ZERO, state));
        });
    }

    @Test
    public void testPercentTypeCommand() {
        assertPercentTypeCommands(dimmerThingHandler, CHANNEL_UID_COLOR, TEST_FADE_TIME);
    }

    @Test
    public void testColorCommand() {
        // setting of color
        long currentTime = System.currentTimeMillis();

        dimmerThingHandler.handleCommand(CHANNEL_UID_COLOR, TEST_COLOR);
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> {
            assertChannelStateUpdate(CHANNEL_UID_COLOR,
                    state -> assertThat(((HSBType) state).getHue().doubleValue(), is(closeTo(280, 1))));
            assertChannelStateUpdate(CHANNEL_UID_COLOR,
                    state -> assertThat(((HSBType) state).getSaturation().doubleValue(), is(closeTo(100.0, 0.5))));
            assertChannelStateUpdate(CHANNEL_UID_COLOR,
                    state -> assertThat(((HSBType) state).getBrightness().doubleValue(), is(closeTo(100.0, 0.5))));
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS_R,
                    state -> assertThat(((PercentType) state).doubleValue(), is(closeTo(66.5, 0.5))));
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS_G, state -> assertEquals(PercentType.ZERO, state));
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS_B, state -> assertEquals(PercentType.HUNDRED, state));
        });

        // color dimming
        dimmerThingHandler.handleCommand(CHANNEL_UID_COLOR, new PercentType(30));
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> {
            assertChannelStateUpdate(CHANNEL_UID_COLOR,
                    state -> assertThat(((HSBType) state).getHue().doubleValue(), is(closeTo(280, 2))));
            assertChannelStateUpdate(CHANNEL_UID_COLOR,
                    state -> assertThat(((HSBType) state).getSaturation().doubleValue(), is(closeTo(100.0, 1))));
            assertChannelStateUpdate(CHANNEL_UID_COLOR,
                    state -> assertThat(((HSBType) state).getBrightness().doubleValue(), is(closeTo(30.0, 1))));
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS_R,
                    state -> assertThat(((PercentType) state).doubleValue(), is(closeTo(19.2, 0.5))));
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS_G, state -> assertEquals(PercentType.ZERO, state));
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS_B,
                    state -> assertThat(((PercentType) state).doubleValue(), is(closeTo(29.8, 0.5))));
        });
    }

}
