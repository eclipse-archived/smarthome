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

import org.eclipse.smarthome.binding.dmx.handler.ChaserThingHandler;
import org.eclipse.smarthome.binding.dmx.test.TestBridgeHandler;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
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
 * Tests cases for {@link ChaserThingHandler}.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class ChaserThingHandlerTest extends JavaOSGiTest {

    private static final String TEST_ADDRESS = "localhost";
    private static final int TEST_UNIVERSE = 1;
    private static final String TEST_CHANNEL = "100/3";
    private static final String TEST_STEPS = "0:100,150,250:-1";

    private ManagedThingProvider managedThingProvider;
    private VolatileStorageService volatileStorageService = new VolatileStorageService();

    Map<String, Object> bridgeProperties;
    Map<String, Object> thingProperties;

    private Bridge bridge;
    private Thing chaserThing;

    @Before
    public void setUp() {
        registerService(volatileStorageService);
        managedThingProvider = getService(ThingProvider.class, ManagedThingProvider.class);

        bridgeProperties = new HashMap<>();
        bridgeProperties.put(CONFIG_ADDRESS, TEST_ADDRESS);
        bridgeProperties.put(CONFIG_UNIVERSE, TEST_UNIVERSE);
        bridgeProperties = new HashMap<>();
        bridge = BridgeBuilder.create(THING_TYPE_TEST_BRIDGE, "testbridge").withLabel("Test Bridge")
                .withConfiguration(new Configuration(bridgeProperties)).build();
        thingProperties = new HashMap<>();
        thingProperties.put(CONFIG_DMX_ID, TEST_CHANNEL);
        thingProperties.put(CONFIG_CHASER_STEPS, TEST_STEPS);
        chaserThing = ThingBuilder.create(THING_TYPE_CHASER, "testchaser").withLabel("Chaser Thing")
                .withBridge(bridge.getUID()).withConfiguration(new Configuration(thingProperties)).build();
    }

    @After
    public void tearDown() {
        managedThingProvider.remove(chaserThing.getUID());
        managedThingProvider.remove(bridge.getUID());
        unregisterService(volatileStorageService);
    }

    @Test
    public void initializationOfChaserThing() {
        assertThat(chaserThing.getHandler(), is(nullValue()));
        managedThingProvider.add(bridge);
        waitForAssert(() -> assertThat(bridge.getHandler(), notNullValue()));
        TestBridgeHandler bridgeHandler = (TestBridgeHandler) bridge.getHandler();

        // check handler present
        managedThingProvider.add(chaserThing);
        waitForAssert(() -> assertThat(chaserThing.getHandler(), notNullValue()));

        // check that thing turns online id properly configured
        waitForAssert(() -> assertThat(chaserThing.getStatus(), is(ThingStatus.ONLINE)));

        // check that thing properly follows bridge status
        bridgeHandler.updateBridgeStatus(ThingStatus.OFFLINE);
        waitForAssert(() -> assertThat(chaserThing.getStatus(), is(ThingStatus.OFFLINE)));
        bridgeHandler.updateBridgeStatus(ThingStatus.ONLINE);
        waitForAssert(() -> assertThat(chaserThing.getStatus(), is(ThingStatus.ONLINE)));

        // check that thing is offline if no bridge found
        managedThingProvider.remove(chaserThing.getUID());
        assertThat(chaserThing.getHandler(), is(nullValue()));
        chaserThing = ThingBuilder.create(THING_TYPE_CHASER, "testchaser").withLabel("Chaser Thing")
                .withConfiguration(new Configuration(thingProperties)).build();
        managedThingProvider.add(chaserThing);
        waitForAssert(() -> assertThat(chaserThing.getHandler(), notNullValue()));
        waitForAssert(() -> assertThat(chaserThing.getStatus(), is(ThingStatus.OFFLINE)));
    }
}