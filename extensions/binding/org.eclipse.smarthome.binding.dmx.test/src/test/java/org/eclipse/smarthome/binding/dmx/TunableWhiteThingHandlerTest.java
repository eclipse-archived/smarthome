/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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

import static org.eclipse.smarthome.binding.dmx.DmxBindingConstants.*;
import static org.eclipse.smarthome.binding.dmx.test.TestBridgeHandler.THING_TYPE_TEST_BRIDGE;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.binding.dmx.handler.DimmerThingHandler;
import org.eclipse.smarthome.binding.dmx.handler.TunableWhiteThingHandler;
import org.eclipse.smarthome.binding.dmx.test.TestBridgeHandler;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingProvider;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ManagedItemChannelLinkProvider;
import org.eclipse.smarthome.core.types.State;
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
public class TunableWhiteThingHandlerTest extends JavaOSGiTest {

    private static final String TEST_CHANNEL_CONFIG = "100/2";
    private static final int TEST_FADE_TIME = 1500;

    private static final String TEST_BRIGHTNESS_ITEM_NAME = "brightnessItem";
    private static final String TEST_BRIGHTNESS_CW_ITEM_NAME = "brightnessCWItem";
    private static final String TEST_BRIGHTNESS_WW_ITEM_NAME = "brightnessWWItem";
    private static final String TEST_COLOR_TEMPERATURE_ITEM_NAME = "colorTemperatureItem";

    private ManagedThingProvider managedThingProvider;
    private ItemRegistry itemRegistry;
    private final VolatileStorageService volatileStorageService = new VolatileStorageService();

    Map<String, Object> bridgeProperties;
    Map<String, Object> thingProperties;

    private Bridge bridge;
    private Thing dimmerThing;
    private GenericItem brightnessItem, brightnessCWItem, brightnessWWItem, colorTemperatureItem;

    private TestBridgeHandler dmxBridgeHandler;
    private TunableWhiteThingHandler dimmerThingHandler;

    @Before
    public void setUp() {
        registerService(volatileStorageService);
        managedThingProvider = getService(ThingProvider.class, ManagedThingProvider.class);
        assertThat("Could not get ManagedThingProvider", managedThingProvider, is(notNullValue()));

        itemRegistry = getService(ItemRegistry.class);
        assertThat("Could not get ItemRegistry", itemRegistry, is(notNullValue()));

        bridgeProperties = new HashMap<>();
        bridge = BridgeBuilder.create(THING_TYPE_TEST_BRIDGE, "testbridge").withLabel("Test Bridge")
                .withConfiguration(new Configuration(bridgeProperties)).build();

        thingProperties = new HashMap<>();
        thingProperties.put(CONFIG_DMX_ID, TEST_CHANNEL_CONFIG);
        thingProperties.put(CONFIG_DIMMER_FADE_TIME, TEST_FADE_TIME);
        thingProperties.put(CONFIG_DIMMER_TURNONVALUE, "127,127");
        ThingUID thingUID = new ThingUID(THING_TYPE_TUNABLEWHITE, "testdimmer");
        dimmerThing = ThingBuilder.create(THING_TYPE_TUNABLEWHITE, "testdimmer").withLabel("Dimmer Thing")
                .withBridge(bridge.getUID()).withConfiguration(new Configuration(thingProperties))
                .withChannel(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_BRIGHTNESS), "Brightness")
                        .withType(BRIGHTNESS_CHANNEL_TYPEUID).build())
                .withChannel(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_BRIGHTNESS_CW), "Brightness CW")
                        .withType(BRIGHTNESS_CHANNEL_TYPEUID).build())
                .withChannel(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_BRIGHTNESS_WW), "Brightness WW")
                        .withType(BRIGHTNESS_CHANNEL_TYPEUID).build())
                .withChannel(
                        ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_COLOR_TEMPERATURE), "Color temperature")
                                .withType(COLOR_TEMPERATURE_CHANNEL_TYPEUID).build())
                .build();
    }

    @After
    public void tearDown() {
        managedThingProvider.remove(dimmerThing.getUID());
        managedThingProvider.remove(bridge.getUID());
        unregisterService(volatileStorageService);
    }

    @Test
    public void statusOfDimmerThing() {
        initialize();

        // check that thing turns online if properly configured
        waitForAssert(() -> assertThat(dimmerThing.getStatus(), is(ThingStatus.ONLINE)));

        // check that thing properly follows bridge status
        dmxBridgeHandler.updateBridgeStatus(ThingStatus.OFFLINE);
        waitForAssert(() -> assertThat("thing not OFFLINE after bridge OFFLINE", dimmerThing.getStatus(),
                is(ThingStatus.OFFLINE)));
        dmxBridgeHandler.updateBridgeStatus(ThingStatus.ONLINE);
        waitForAssert(() -> assertThat("thing not ONLINE after bridge ONLINE", dimmerThing.getStatus(),
                is(ThingStatus.ONLINE)));

        // check that thing is offline if no bridge found
        managedThingProvider.remove(dimmerThing.getUID());
        assertThat(dimmerThing.getHandler(), is(nullValue()));
        dimmerThing = ThingBuilder.create(THING_TYPE_DIMMER, "testdimmer").withLabel("Dimmer Thing")
                .withConfiguration(new Configuration(thingProperties)).build();
        managedThingProvider.add(dimmerThing);
        waitForAssert(() -> assertThat("bridgeless thing has no handler", dimmerThing.getHandler(), notNullValue()));
        waitForAssert(() -> assertThat("thing not OFFLINE if bridge is missing", dimmerThing.getStatus(),
                is(ThingStatus.OFFLINE)));
    }

    @Test
    public void testOnOffCommand() {
        initialize();

        // on
        long currentTime = System.currentTimeMillis();

        dimmerThingHandler.handleCommand(new ChannelUID(dimmerThing.getUID(), CHANNEL_BRIGHTNESS), OnOffType.ON);
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> assertThat("DimmerItem (brightness) is not updated to ON  after ON command",
                getItemOnOffType(brightnessItem), is(OnOffType.ON)));
        waitForAssert(() -> assertThat("DimmerItem (cool white) is not updated to 50% after ON command",
                ((PercentType) brightnessCWItem.getState()).doubleValue(), is(closeTo(50, 0.5))));
        waitForAssert(() -> assertThat("DimmerItem (warmw hite) is not updated to 50% after ON command%",
                ((PercentType) brightnessWWItem.getState()).doubleValue(), is(closeTo(50, 0.5))));

        // off
        dimmerThingHandler.handleCommand(new ChannelUID(dimmerThing.getUID(), CHANNEL_BRIGHTNESS), OnOffType.OFF);
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> assertThat("DimmerItem (brightness) is not updated to OFF after OFF command",
                getItemOnOffType(brightnessItem), is(OnOffType.OFF)));
        waitForAssert(() -> assertThat("DimmerItem (cool white) is not updated to 0% after OFF command",
                brightnessCWItem.getState(), is(PercentType.ZERO)));
        waitForAssert(() -> assertThat("DimmerItem (warm white) is not updated to 0% after OFF command",
                brightnessWWItem.getState(), is(PercentType.ZERO)));
    }

    @Test
    public void testPercentTypeCommand() {
        initialize();
        long currentTime = System.currentTimeMillis();

        // set 30%
        dimmerThingHandler.handleCommand(new ChannelUID(dimmerThing.getUID(), CHANNEL_BRIGHTNESS), new PercentType(30));
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> assertThat("DimmerItem is not updated to 30%",
                getItemPercentType(brightnessItem).doubleValue(), is(closeTo(30.0, 1.0))));

        // set 0%
        dimmerThingHandler.handleCommand(new ChannelUID(dimmerThing.getUID(), CHANNEL_BRIGHTNESS), PercentType.ZERO);
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> assertThat("DimmerItem is not updated to 0%", brightnessItem.getState(),
                is(equalTo(PercentType.ZERO))));

        // set 100%
        dimmerThingHandler.handleCommand(new ChannelUID(dimmerThing.getUID(), CHANNEL_BRIGHTNESS), PercentType.HUNDRED);
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> assertThat("DimmerItem is not updated to 100%",
                ((PercentType) brightnessItem.getState()).doubleValue(), is(closeTo(100.0, 0.5))));
    }

    @Test
    public void testColorTemperature() {
        initialize();
        long currentTime = System.currentTimeMillis();

        dimmerThingHandler.handleCommand(new ChannelUID(dimmerThing.getUID(), CHANNEL_BRIGHTNESS), OnOffType.ON);
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> assertThat("DimmerItem is not updated to 100%",
                getItemPercentType(brightnessItem).doubleValue(), is(closeTo(100.0, 0.5))));
        waitForAssert(() -> assertThat("ColorTemperatureItem is not 50%",
                getItemPercentType(colorTemperatureItem).doubleValue(), is(closeTo(50.0, 0.5))));

        // cool white
        dimmerThingHandler.handleCommand(new ChannelUID(dimmerThing.getUID(), CHANNEL_COLOR_TEMPERATURE),
                PercentType.ZERO);
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> assertThat("ColorTemperatureItem is not 0% after cool white",
                getItemPercentType(colorTemperatureItem).doubleValue(), is(closeTo(0.0, 0.1))));
        waitForAssert(() -> assertThat("DimmerItem is not updated to 100%",
                getItemPercentType(brightnessItem).doubleValue(), is(closeTo(100.0, 0.5))));
        waitForAssert(() -> assertThat("DimmerItem (cool white) is not updated to 100% after cool white command",
                getItemPercentType(brightnessCWItem).doubleValue(), is(closeTo(100.0, 0.5))));
        waitForAssert(() -> assertThat("DimmerItem (warm white) is not updated to 0% after cool white command",
                getItemPercentType(brightnessWWItem).doubleValue(), is(closeTo(0.0, 0.5))));

        // warm white
        dimmerThingHandler.handleCommand(new ChannelUID(dimmerThing.getUID(), CHANNEL_COLOR_TEMPERATURE),
                PercentType.HUNDRED);
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> assertThat("ColorTemperatureItem is not 100% after warm white command",
                getItemPercentType(colorTemperatureItem).doubleValue(), is(closeTo(100.0, 0.1))));
        waitForAssert(() -> assertThat("DimmerItem is not updated to 100%",
                getItemPercentType(brightnessItem).doubleValue(), is(closeTo(100.0, 0.5))));
        waitForAssert(() -> assertThat("DimmerItem (cool white) is not updated to 0% after warm white command",
                getItemPercentType(brightnessCWItem).doubleValue(), is(closeTo(0.0, 0.5))));
        waitForAssert(() -> assertThat("DimmerItem (warm white) is not updated to 100% after warm white command",
                getItemPercentType(brightnessWWItem).doubleValue(), is(closeTo(100.0, 0.5))));

        // intermediate white
        dimmerThingHandler.handleCommand(new ChannelUID(dimmerThing.getUID(), CHANNEL_COLOR_TEMPERATURE),
                new PercentType(75));
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> assertThat("ColorTemperatureItem is not 75% after intermediate white command",
                getItemPercentType(colorTemperatureItem).doubleValue(), is(closeTo(75.0, 0.1))));
        waitForAssert(() -> assertThat("DimmerItem is not updated to 100%",
                getItemPercentType(brightnessItem).doubleValue(), is(closeTo(100.0, 1.0))));
        waitForAssert(() -> assertThat("DimmerItem (cool white) is not updated to 0% after intermediate white command",
                getItemPercentType(brightnessCWItem).doubleValue(), is(closeTo(25.0, 0.5))));
        waitForAssert(
                () -> assertThat("DimmerItem (warm white) is not updated to 100% after intermediate white command",
                        getItemPercentType(brightnessWWItem).doubleValue(), is(closeTo(75.0, 0.5))));
    }

    private void initialize() {
        managedThingProvider.add(bridge);
        dmxBridgeHandler = (TestBridgeHandler) waitForAssert(() -> {
            final ThingHandler thingHandler = bridge.getHandler();
            assertThat("Bridge is null", thingHandler, notNullValue());
            return thingHandler;
        });

        managedThingProvider.add(dimmerThing);
        dimmerThing = managedThingProvider.get(dimmerThing.getUID());
        dimmerThingHandler = (TunableWhiteThingHandler) waitForAssert(() -> {
            final ThingHandler thingHandler = dimmerThing.getHandler();
            assertThat("dimmerThing is null", thingHandler, notNullValue());
            return thingHandler;
        });

        final ManagedItemChannelLinkProvider itemChannelLinkProvider = waitForAssert(() -> {
            final ManagedItemChannelLinkProvider tmp = getService(ManagedItemChannelLinkProvider.class);
            assertThat("Could not get ManagedItemChannelLinkProvider", tmp, is(notNullValue()));
            return tmp;
        });

        brightnessItem = new DimmerItem(TEST_BRIGHTNESS_ITEM_NAME);
        itemRegistry.add(brightnessItem);
        itemChannelLinkProvider.add(new ItemChannelLink(TEST_BRIGHTNESS_ITEM_NAME,
                new ChannelUID(dimmerThing.getUID(), CHANNEL_BRIGHTNESS)));

        brightnessCWItem = new DimmerItem(TEST_BRIGHTNESS_CW_ITEM_NAME);
        itemRegistry.add(brightnessCWItem);
        itemChannelLinkProvider.add(new ItemChannelLink(TEST_BRIGHTNESS_CW_ITEM_NAME,
                new ChannelUID(dimmerThing.getUID(), CHANNEL_BRIGHTNESS_CW)));

        brightnessWWItem = new DimmerItem(TEST_BRIGHTNESS_WW_ITEM_NAME);
        itemRegistry.add(brightnessWWItem);
        itemChannelLinkProvider.add(new ItemChannelLink(TEST_BRIGHTNESS_WW_ITEM_NAME,
                new ChannelUID(dimmerThing.getUID(), CHANNEL_BRIGHTNESS_WW)));

        colorTemperatureItem = new DimmerItem(TEST_COLOR_TEMPERATURE_ITEM_NAME);
        itemRegistry.add(colorTemperatureItem);
        itemChannelLinkProvider.add(new ItemChannelLink(TEST_COLOR_TEMPERATURE_ITEM_NAME,
                new ChannelUID(dimmerThing.getUID(), CHANNEL_COLOR_TEMPERATURE)));

    }

    private PercentType getItemPercentType(GenericItem item) {
        return (PercentType) waitForAssert(() -> {
            final State state = item.getStateAs(PercentType.class);
            assertThat("state is not PercentType", state, instanceOf(PercentType.class));
            return state;
        });
    }

    private OnOffType getItemOnOffType(GenericItem item) {
        return (OnOffType) waitForAssert(() -> {
            final State state = item.getStateAs(OnOffType.class);
            assertThat("state is not OnOffType", state, instanceOf(OnOffType.class));
            return state;
        });
    }
}