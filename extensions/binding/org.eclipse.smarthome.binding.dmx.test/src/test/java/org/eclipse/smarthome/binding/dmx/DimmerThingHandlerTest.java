/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.dmx;

import static org.eclipse.smarthome.binding.dmx.DmxBindingConstants.*;
import static org.eclipse.smarthome.binding.dmx.test.TestBridgeHandler.THING_TYPE_TEST_BRIDGE;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.binding.dmx.handler.DimmerThingHandler;
import org.eclipse.smarthome.binding.dmx.test.TestBridgeHandler;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingProvider;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests cases for {@link DimmerThingHandler}.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class DimmerThingHandlerTest extends JavaOSGiTest {

    private static final String TEST_ADDRESS = "localhost";
    private static final int TEST_UNIVERSE = 1;
    private static final String TEST_SINGLE_CHANNEL = "100";
    private static final String TEST_COLOR_CHANNEL = "200/3";
    private static final int TEST_FADE_TIME = 1000;

    private ManagedThingProvider managedThingProvider;
    private VolatileStorageService volatileStorageService = new VolatileStorageService();

    Map<String, Object> bridgeProperties;
    Map<String, Object> thingProperties;

    private Bridge bridge;
    private Thing dimmerThing;

    @Before
    public void setUp() {
        registerService(volatileStorageService);
        managedThingProvider = getService(ThingProvider.class, ManagedThingProvider.class);

        bridgeProperties = new HashMap<>();
        bridge = BridgeBuilder.create(THING_TYPE_TEST_BRIDGE, "testbridge").withLabel("Test Bridge")
                .withConfiguration(new Configuration(bridgeProperties)).build();

        thingProperties = new HashMap<>();
        thingProperties.put(CONFIG_DMX_ID, TEST_SINGLE_CHANNEL);
        thingProperties.put(CONFIG_DIMMER_FADE_TIME, TEST_FADE_TIME);
        dimmerThing = ThingBuilder.create(THING_TYPE_DIMMER, "testdimmer").withLabel("Dimmer Thing")
                .withBridge(bridge.getUID()).withConfiguration(new Configuration(thingProperties)).build();
    }

    @After
    public void tearDown() {
        managedThingProvider.remove(dimmerThing.getUID());
        managedThingProvider.remove(bridge.getUID());
        unregisterService(volatileStorageService);
    }

    @Test
    public void initializationOfDimmerThing() {
        assertThat(dimmerThing.getHandler(), is(nullValue()));
        managedThingProvider.add(bridge);
        waitForAssert(() -> assertThat(bridge.getHandler(), notNullValue()));
        TestBridgeHandler bridgeHandler = (TestBridgeHandler) bridge.getHandler();

        // check handler present
        managedThingProvider.add(dimmerThing);
        waitForAssert(() -> assertThat(dimmerThing.getHandler(), notNullValue()));

        // check that thing turns online id properly configured
        waitForAssert(() -> assertThat(dimmerThing.getStatus(), is(ThingStatus.ONLINE)));

        // check that thing properly follows bridge status
        bridgeHandler.updateBridgeStatus(ThingStatus.OFFLINE);
        waitForAssert(() -> assertThat(dimmerThing.getStatus(), is(ThingStatus.OFFLINE)));
        bridgeHandler.updateBridgeStatus(ThingStatus.ONLINE);
        waitForAssert(() -> assertThat(dimmerThing.getStatus(), is(ThingStatus.ONLINE)));

        // check that thing is offline if no bridge found
        managedThingProvider.remove(dimmerThing.getUID());
        assertThat(dimmerThing.getHandler(), is(nullValue()));
        dimmerThing = ThingBuilder.create(THING_TYPE_DIMMER, "testdimmer").withLabel("Dimmer Thing")
                .withConfiguration(new Configuration(thingProperties)).build();
        managedThingProvider.add(dimmerThing);
        waitForAssert(() -> assertThat(dimmerThing.getHandler(), notNullValue()));
        waitForAssert(() -> assertThat(dimmerThing.getStatus(), is(ThingStatus.OFFLINE)));
    }

    @Test
    public void creationAndDeletionOfColorChannel() {
        managedThingProvider.add(bridge);
        managedThingProvider.add(dimmerThing);
        DimmerThingHandler thingHandler = (DimmerThingHandler) dimmerThing.getHandler();
        waitForAssert(() -> assertThat(thingHandler, notNullValue()));

        // color channel not allowed in single channel dimmer
        waitForAssert(() -> assertThat(thingHandler.getThing().getChannel(CHANNEL_COLOR), nullValue()));

        // check that color channel is properly added
        thingProperties.replace(CONFIG_DMX_ID, TEST_COLOR_CHANNEL);
        thingHandler.handleConfigurationUpdate(thingProperties);
        waitForAssert(() -> assertThat(thingHandler.getThing().getChannel(CHANNEL_COLOR), notNullValue()));

        // check that color channel is properly added
        thingProperties.replace(CONFIG_DMX_ID, TEST_SINGLE_CHANNEL);
        thingHandler.handleConfigurationUpdate(thingProperties);
        waitForAssert(() -> assertThat(thingHandler.getThing().getChannel(CHANNEL_COLOR), nullValue()));
    }

    @Test
    public void testBrightnessChannel() {
        managedThingProvider.add(bridge);
        TestBridgeHandler bridgeHandler = (TestBridgeHandler) bridge.getHandler();
        waitForAssert(() -> assertThat(bridgeHandler, notNullValue()));

        managedThingProvider.add(dimmerThing);
        DimmerThingHandler thingHandler = (DimmerThingHandler) dimmerThing.getHandler();
        waitForAssert(() -> assertThat(thingHandler, notNullValue()));

        long startTime = System.currentTimeMillis();

        // on/off
        thingHandler.handleCommand(new ChannelUID(dimmerThing.getUID(), CHANNEL_BRIGHTNESS), OnOffType.ON);
        assertThat(bridgeHandler.getBufferValue(startTime, Integer.valueOf(TEST_SINGLE_CHANNEL)), is((byte) 0));
        assertThat(bridgeHandler.getBufferValue(startTime + TEST_FADE_TIME, Integer.valueOf(TEST_SINGLE_CHANNEL)),
                is((byte) 255));
        thingHandler.handleCommand(new ChannelUID(dimmerThing.getUID(), CHANNEL_BRIGHTNESS), OnOffType.OFF);
        assertThat(bridgeHandler.getBufferValue(startTime, Integer.valueOf(TEST_SINGLE_CHANNEL)), is((byte) 255));
        assertThat(bridgeHandler.getBufferValue(startTime + 2 * TEST_FADE_TIME, Integer.valueOf(TEST_SINGLE_CHANNEL)),
                is((byte) 0));

        // setting of values
        thingHandler.handleCommand(new ChannelUID(dimmerThing.getUID(), CHANNEL_BRIGHTNESS),
                DecimalType.valueOf("100"));
        assertThat(bridgeHandler.getBufferValue(startTime, Integer.valueOf(TEST_SINGLE_CHANNEL)), is((byte) 0));
        assertThat(bridgeHandler.getBufferValue(startTime + TEST_FADE_TIME, Integer.valueOf(TEST_SINGLE_CHANNEL)),
                is((byte) 100));

        thingHandler.handleCommand(new ChannelUID(dimmerThing.getUID(), CHANNEL_BRIGHTNESS), DecimalType.valueOf("0"));
        assertThat(bridgeHandler.getBufferValue(startTime, Integer.valueOf(TEST_SINGLE_CHANNEL)), is((byte) 100));
        assertThat(bridgeHandler.getBufferValue(startTime + TEST_FADE_TIME, Integer.valueOf(TEST_SINGLE_CHANNEL)),
                is((byte) 0));

        thingHandler.handleCommand(new ChannelUID(dimmerThing.getUID(), CHANNEL_BRIGHTNESS),
                DecimalType.valueOf("255"));
        assertThat(bridgeHandler.getBufferValue(startTime, Integer.valueOf(TEST_SINGLE_CHANNEL)), is((byte) 0));
        assertThat(bridgeHandler.getBufferValue(startTime + TEST_FADE_TIME, Integer.valueOf(TEST_SINGLE_CHANNEL)),
                is((byte) 255));

        // fading
        thingHandler.handleCommand(new ChannelUID(dimmerThing.getUID(), CHANNEL_BRIGHTNESS), DecimalType.valueOf("0"));
        assertThat(bridgeHandler.getBufferValue(startTime, Integer.valueOf(TEST_SINGLE_CHANNEL)), is((byte) 255));
        assertThat(bridgeHandler.getBufferValue(startTime + Math.round(0.33 * TEST_FADE_TIME),
                Integer.valueOf(TEST_SINGLE_CHANNEL)), is((byte) 170));
        assertThat(bridgeHandler.getBufferValue(startTime + Math.round(0.5 * TEST_FADE_TIME),
                Integer.valueOf(TEST_SINGLE_CHANNEL)), is((byte) 127));
        assertThat(bridgeHandler.getBufferValue(startTime + Math.round(0.75 * TEST_FADE_TIME),
                Integer.valueOf(TEST_SINGLE_CHANNEL)), is((byte) 63));
        assertThat(bridgeHandler.getBufferValue(startTime + TEST_FADE_TIME, Integer.valueOf(TEST_SINGLE_CHANNEL)),
                is((byte) 0));

    }

}
