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
import org.eclipse.smarthome.binding.dmx.handler.DimmerThingHandler;
import org.eclipse.smarthome.config.core.Configuration;
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
 * Tests cases for {@link DimmerThingHandler} in normal mode.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class DimmerThingHandlerTest extends AbstractDmxThingTest {
    private static final String TEST_CHANNEL_CONFIG = "100";
    private static final int TEST_FADE_TIME = 1500;

    private Map<String, Object> thingProperties;
    private Thing dimmerThing;
    private DimmerThingHandler dimmerThingHandler;

    private final ThingUID THING_UID_DIMMER = new ThingUID(THING_TYPE_DIMMER, "testdimmer");
    private final ChannelUID CHANNEL_UID_BRIGHTNESS = new ChannelUID(THING_UID_DIMMER, CHANNEL_BRIGHTNESS);

    @Before
    public void setUp() {
        super.setup();

        thingProperties = new HashMap<>();
        thingProperties.put(CONFIG_DMX_ID, TEST_CHANNEL_CONFIG);
        thingProperties.put(CONFIG_DIMMER_FADE_TIME, TEST_FADE_TIME);
        thingProperties.put(CONFIG_DIMMER_DYNAMICTURNONVALUE, true);

        dimmerThing = ThingBuilder
                .create(THING_TYPE_DIMMER, "testdimmer").withLabel("Dimmer Thing").withBridge(bridge.getUID())
                .withConfiguration(new Configuration(thingProperties)).withChannel(ChannelBuilder
                        .create(CHANNEL_UID_BRIGHTNESS, "Brightness").withType(BRIGHTNESS_CHANNEL_TYPEUID).build())
                .build();

        dimmerThingHandler = new DimmerThingHandler(dimmerThing) {
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
        DimmerThingHandler dimmerHandlerWithoutBridge = new DimmerThingHandler(dimmerThing) {
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

        dimmerThingHandler.handleCommand(CHANNEL_UID_BRIGHTNESS, OnOffType.ON);
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> {
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS,
                    state -> assertEquals(OnOffType.ON, state.as(OnOffType.class)));
        });

        // off
        dimmerThingHandler.handleCommand(CHANNEL_UID_BRIGHTNESS, OnOffType.OFF);
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> {
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS,
                    state -> assertEquals(OnOffType.OFF, state.as(OnOffType.class)));
        });
    }

    @Test
    public void testDynamicTurnOnValue() {
        long currentTime = System.currentTimeMillis();
        int testValue = 75;

        // turn on with arbitrary value
        dimmerThingHandler.handleCommand(CHANNEL_UID_BRIGHTNESS, new PercentType(testValue));
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> {
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS,
                    state -> assertThat(((PercentType) state).doubleValue(), is(closeTo(testValue, 1.0))));
        });

        // turn off and hopefully store last value
        dimmerThingHandler.handleCommand(CHANNEL_UID_BRIGHTNESS, OnOffType.OFF);
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> {
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS,
                    state -> assertEquals(OnOffType.OFF, state.as(OnOffType.class)));
        });

        // turn on and restore value
        dimmerThingHandler.handleCommand(CHANNEL_UID_BRIGHTNESS, OnOffType.ON);
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> {
            assertChannelStateUpdate(CHANNEL_UID_BRIGHTNESS,
                    state -> assertThat(((PercentType) state).doubleValue(), is(closeTo(testValue, 1.0))));
        });
    }

    @Test
    public void testPercentTypeCommand() {
        assertPercentTypeCommands(dimmerThingHandler, CHANNEL_UID_BRIGHTNESS, TEST_FADE_TIME);
    }

}
