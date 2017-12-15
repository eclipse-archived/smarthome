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

import org.eclipse.smarthome.binding.dmx.handler.ColorThingHandler;
import org.eclipse.smarthome.binding.dmx.handler.DimmerThingHandler;
import org.eclipse.smarthome.binding.dmx.test.TestBridgeHandler;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
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
 * Tests cases for {@link DimmerThingHandler} in RGB mode.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class ColorThingHandlerTest extends JavaOSGiTest {

    private static final String TEST_CHANNEL_CONFIG = "100/3";
    private static final int TEST_FADE_TIME = 1500;
    private static final HSBType TEST_COLOR = new HSBType(new DecimalType(280), new PercentType(100),
            new PercentType(100));

    private static final String TEST_BRIGHTNESS_R_ITEM_NAME = "brightnessRItem";
    private static final String TEST_BRIGHTNESS_G_ITEM_NAME = "brightnessGItem";
    private static final String TEST_BRIGHTNESS_B_ITEM_NAME = "brightnessBItem";
    private static final String TEST_DIMMER_ITEM_NAME = "dimmerItem";
    private static final String TEST_COLOR_ITEM_NAME = "colorItem";

    private ManagedThingProvider managedThingProvider;
    private ItemRegistry itemRegistry;
    private final VolatileStorageService volatileStorageService = new VolatileStorageService();

    Map<String, Object> bridgeProperties;
    Map<String, Object> thingProperties;

    private Bridge bridge;
    private Thing dimmerThing;
    private GenericItem dimmerItem, brightnessRItem, brightnessGItem, brightnessBItem, colorItem;

    private TestBridgeHandler dmxBridgeHandler;
    private ColorThingHandler dimmerThingHandler;

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
        thingProperties.put(CONFIG_DIMMER_TURNONVALUE, "255,128,0");
        ThingUID thingUID = new ThingUID(THING_TYPE_COLOR, "testdimmer");
        dimmerThing = ThingBuilder.create(THING_TYPE_COLOR, "testdimmer").withLabel("Dimmer Thing")
                .withBridge(bridge.getUID()).withConfiguration(new Configuration(thingProperties))
                .withChannel(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_BRIGHTNESS_R), "Brightness R")
                        .withType(BRIGHTNESS_CHANNEL_TYPEUID).build())
                .withChannel(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_BRIGHTNESS_G), "Brightness G")
                        .withType(BRIGHTNESS_CHANNEL_TYPEUID).build())
                .withChannel(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_BRIGHTNESS_B), "Brightness B")
                        .withType(BRIGHTNESS_CHANNEL_TYPEUID).build())
                .withChannel(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_COLOR), "Color")
                        .withType(COLOR_CHANNEL_TYPEUID).build())
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

        dimmerThingHandler.handleCommand(new ChannelUID(dimmerThing.getUID(), CHANNEL_COLOR), OnOffType.ON);
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> assertThat("color is not updated to ON after ON command", getItemOnOffType(colorItem),
                is(OnOffType.ON)));
        waitForAssert(() -> assertThat("DimmerItem (red) is not updated to 100% after ON command",
                brightnessRItem.getState(), is(PercentType.HUNDRED)));
        waitForAssert(() -> assertThat("DimmerItem (green) is not updated to 50% after ON command",
                getItemPercentType(brightnessGItem).doubleValue(), is(closeTo(50, 0.5))));
        waitForAssert(() -> assertThat("DimmerItem (blue) is not updated to 0%  after ON command",
                brightnessBItem.getState(), is(PercentType.ZERO)));

        // off
        dimmerThingHandler.handleCommand(new ChannelUID(dimmerThing.getUID(), CHANNEL_COLOR), OnOffType.OFF);
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> assertThat("color is not updated to OFF after OFF command", getItemOnOffType(colorItem),
                is(OnOffType.OFF)));
        waitForAssert(() -> assertThat("DimmerItem (red) is not updated to 0% after OFF command",
                brightnessRItem.getState(), is(PercentType.ZERO)));
        waitForAssert(() -> assertThat("DimmerItem (green) is not updated to 0% after OFF command",
                brightnessGItem.getState(), is(PercentType.ZERO)));
        waitForAssert(() -> assertThat("DimmerItem (blue) is not updated to 0% after OFF command",
                brightnessBItem.getState(), is(PercentType.ZERO)));
    }

    @Test
    public void testPercentTypeCommand() {
        initialize();
        long currentTime = System.currentTimeMillis();

        // set 50%
        dimmerThingHandler.handleCommand(new ChannelUID(dimmerThing.getUID(), CHANNEL_COLOR), new PercentType(30));
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);
        waitForAssert(() -> assertThat("DimmerItem is not updated to 30%", getItemPercentType(dimmerItem).doubleValue(),
                is(closeTo(30.0, 1.0))));

        // set 0%
        dimmerThingHandler.handleCommand(new ChannelUID(dimmerThing.getUID(), CHANNEL_COLOR), PercentType.ZERO);
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> assertThat("DimmerItem is not updated to 0%", (PercentType) dimmerItem.getState(),
                is(equalTo(PercentType.ZERO))));

        // set 100%
        dimmerThingHandler.handleCommand(new ChannelUID(dimmerThing.getUID(), CHANNEL_COLOR), PercentType.HUNDRED);
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> assertThat("DimmerItem is not updated to 100%", (PercentType) dimmerItem.getState(),
                is(equalTo(PercentType.HUNDRED))));
    }

    @Test
    public void testColorCommand() {
        initialize();

        // setting of color
        long currentTime = System.currentTimeMillis();
        dimmerThingHandler.handleCommand(new ChannelUID(dimmerThing.getUID(), CHANNEL_COLOR), TEST_COLOR);
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> assertThat("ColorItem is not HSBType", colorItem.getState(), instanceOf(HSBType.class)));

        waitForAssert(() -> assertThat("ColorItem (hue) is not updated correctly after setting color",
                ((HSBType) colorItem.getState()).getHue().doubleValue(), is(closeTo(280, 1))));
        waitForAssert(() -> assertThat("ColorItem (saturation) is not updated correctly after setting color",
                ((HSBType) colorItem.getState()).getSaturation().doubleValue(), is(closeTo(100, 0.5))));
        waitForAssert(() -> assertThat("ColorItem (brightness) is not updated correctly after setting color",
                ((HSBType) colorItem.getState()).getBrightness().doubleValue(), is(closeTo(100, 0.5))));

        waitForAssert(() -> assertThat("DimmerItem (red) is not updated to 66% after setting color",
                getItemPercentType(brightnessRItem).doubleValue(), is(closeTo(66.5, 0.5))));
        waitForAssert(() -> assertThat("DimmerItem (green) is not updated to 0% after setting color",
                brightnessGItem.getState(), is(PercentType.ZERO)));
        waitForAssert(() -> assertThat("DimmerItem (blue) is not updated to 100% after setting color",
                brightnessBItem.getState(), is(PercentType.HUNDRED)));

        // color dimming
        dimmerThingHandler.handleCommand(new ChannelUID(dimmerThing.getUID(), CHANNEL_COLOR), new PercentType(30));
        currentTime = dmxBridgeHandler.calcBuffer(currentTime, TEST_FADE_TIME);

        waitForAssert(() -> assertThat("ColorItem (hue) is not updated correctly after dimming",
                ((HSBType) colorItem.getState()).getHue().doubleValue(), is(closeTo(280, 2))));
        waitForAssert(() -> assertThat("ColorItem (saturation) is not updated correctly after dimming",
                ((HSBType) colorItem.getState()).getSaturation().doubleValue(), is(closeTo(100, 1))));
        waitForAssert(() -> assertThat("ColorItem (brightness) is not updated correctly after dimming",
                ((HSBType) colorItem.getState()).getBrightness().doubleValue(), is(closeTo(30, 1))));

        waitForAssert(() -> assertThat("DimmerItem (red) is not updated to 19% after dimming color",
                ((PercentType) brightnessRItem.getState()).doubleValue(), is(closeTo(19.2, 0.5))));
        waitForAssert(() -> assertThat("DimmerItem (green) is not updated to 0% after dimming color",
                brightnessGItem.getState(), is(PercentType.ZERO)));
        waitForAssert(() -> assertThat("DimmerItem (blue) is not updated to 100% after dimming color",
                ((PercentType) brightnessBItem.getState()).doubleValue(), is(closeTo(29.8, 0.5))));
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
        dimmerThingHandler = (ColorThingHandler) waitForAssert(() -> {
            final ThingHandler thingHandler = dimmerThing.getHandler();
            assertThat("dimmerThing is null", thingHandler, notNullValue());
            return thingHandler;
        });

        final ManagedItemChannelLinkProvider itemChannelLinkProvider = waitForAssert(() -> {
            final ManagedItemChannelLinkProvider tmp = getService(ManagedItemChannelLinkProvider.class);
            assertThat("Could not get ManagedItemChannelLinkProvider", tmp, is(notNullValue()));
            return tmp;
        });

        brightnessRItem = new DimmerItem(TEST_BRIGHTNESS_R_ITEM_NAME);
        itemRegistry.add(brightnessRItem);
        itemChannelLinkProvider.add(new ItemChannelLink(TEST_BRIGHTNESS_R_ITEM_NAME,
                new ChannelUID(dimmerThing.getUID(), CHANNEL_BRIGHTNESS_R)));

        brightnessGItem = new DimmerItem(TEST_BRIGHTNESS_G_ITEM_NAME);
        itemRegistry.add(brightnessGItem);
        itemChannelLinkProvider.add(new ItemChannelLink(TEST_BRIGHTNESS_G_ITEM_NAME,
                new ChannelUID(dimmerThing.getUID(), CHANNEL_BRIGHTNESS_G)));

        brightnessBItem = new DimmerItem(TEST_BRIGHTNESS_B_ITEM_NAME);
        itemRegistry.add(brightnessBItem);
        itemChannelLinkProvider.add(new ItemChannelLink(TEST_BRIGHTNESS_B_ITEM_NAME,
                new ChannelUID(dimmerThing.getUID(), CHANNEL_BRIGHTNESS_B)));

        dimmerItem = new DimmerItem(TEST_DIMMER_ITEM_NAME);
        itemRegistry.add(dimmerItem);
        itemChannelLinkProvider
                .add(new ItemChannelLink(TEST_DIMMER_ITEM_NAME, new ChannelUID(dimmerThing.getUID(), CHANNEL_COLOR)));

        colorItem = new ColorItem(TEST_COLOR_ITEM_NAME);
        itemRegistry.add(colorItem);
        itemChannelLinkProvider
                .add(new ItemChannelLink(TEST_COLOR_ITEM_NAME, new ChannelUID(dimmerThing.getUID(), CHANNEL_COLOR)));
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
