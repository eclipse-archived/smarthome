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
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.binding.dmx.handler.ArtnetBridgeHandler;
import org.eclipse.smarthome.binding.dmx.internal.DmxBridgeHandler;
import org.eclipse.smarthome.binding.dmx.internal.multiverse.BaseDmxChannel;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingProvider;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests cases for {@link ArtnetBridgeHandler}.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class ArtnetBridgeHandlerTest extends JavaOSGiTest {

    private static final String TEST_ADDRESS = "localhost";
    private static final int TEST_UNIVERSE = 1;
    private static final int TEST_CHANNEL = 100;

    private ManagedThingProvider managedThingProvider;
    private VolatileStorageService volatileStorageService = new VolatileStorageService();

    Map<String, Object> bridgeProperties;
    Map<String, Object> thingProperties;

    private Bridge bridge;
    private Thing thing;

    @Before
    public void setUp() {
        registerService(volatileStorageService);
        managedThingProvider = getService(ThingProvider.class, ManagedThingProvider.class);

        bridgeProperties = new HashMap<>();
        bridgeProperties.put(CONFIG_ADDRESS, TEST_ADDRESS);
        bridgeProperties.put(CONFIG_UNIVERSE, TEST_UNIVERSE);
        bridge = BridgeBuilder.create(THING_TYPE_ARTNET_BRIDGE, "artnetbridge").withLabel("Artnet Bridge")
                .withConfiguration(new Configuration(bridgeProperties)).build();

        thingProperties = new HashMap<>();
        thingProperties.put(CONFIG_DMX_ID, String.format("%d", TEST_CHANNEL));
        thing = ThingBuilder.create(THING_TYPE_DIMMER, "testdimmer").withLabel("Dimmer Thing")
                .withBridge(bridge.getUID()).withConfiguration(new Configuration(thingProperties)).build();
    }

    @After
    public void tearDown() {
        managedThingProvider.remove(thing.getUID());
        managedThingProvider.remove(bridge.getUID());
        unregisterService(volatileStorageService);
    }

    @Test
    public void initializationOfBridgeHandler() {
        assertThat(bridge.getHandler(), is(nullValue()));
        managedThingProvider.add(bridge);
        BridgeHandler bridgeHandler = bridge.getHandler();

        waitForAssert(() -> assertThat(bridgeHandler, notNullValue()));
        waitForAssert(() -> assertThat(bridge.getStatus(), is(ThingStatus.ONLINE)));
    }

    @Test
    public void renamingOfUniverses() {
        managedThingProvider.add(bridge);
        DmxBridgeHandler bridgeHandler = (DmxBridgeHandler) bridge.getHandler();
        waitForAssert(() -> assertThat(bridgeHandler.getUniverseId(), is(TEST_UNIVERSE)));

        bridgeProperties.replace(CONFIG_UNIVERSE, 2);
        bridgeHandler.handleConfigurationUpdate(bridgeProperties);
        waitForAssert(() -> assertThat(bridgeHandler.getUniverseId(), is(2)));

        bridgeProperties.replace(CONFIG_UNIVERSE, TEST_UNIVERSE);
        bridgeHandler.handleConfigurationUpdate(bridgeProperties);
        waitForAssert(() -> assertThat(bridgeHandler.getUniverseId(), is(TEST_UNIVERSE)));
    }

    @Test
    public void initializationOfDimmerThing() {
        assertThat(thing.getHandler(), is(nullValue()));
        managedThingProvider.add(bridge);
        waitForAssert(() -> assertThat(bridge.getHandler(), notNullValue()));
        managedThingProvider.add(thing);

        waitForAssert(() -> assertThat(thing.getHandler(), notNullValue()));
    }

    public void retrievingOfChannels() {
        managedThingProvider.add(bridge);
        waitForAssert(() -> assertThat(bridge.getHandler(), notNullValue()));
        managedThingProvider.add(thing);
        DmxBridgeHandler bridgeHandler = (DmxBridgeHandler) bridge.getHandler();

        BaseDmxChannel channel = new BaseDmxChannel(TEST_UNIVERSE, TEST_CHANNEL);
        BaseDmxChannel returnedChannel = bridgeHandler.getDmxChannel(channel, thing);

        Integer channelId = returnedChannel.getChannelId();
        assertThat(channelId, is(TEST_CHANNEL));
    }
}
