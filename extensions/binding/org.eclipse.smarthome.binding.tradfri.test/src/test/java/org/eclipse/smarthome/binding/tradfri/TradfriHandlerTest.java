/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.tradfri;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.binding.tradfri.handler.TradfriGatewayHandler;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingProvider;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests cases for {@link TradfriGatewayHandler}.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class TradfriHandlerTest extends JavaOSGiTest {

    private ManagedThingProvider managedThingProvider;
    private VolatileStorageService volatileStorageService = new VolatileStorageService();
    private Bridge bridge;
    private Thing thing;

    @Before
    public void setUp() {
        registerService(volatileStorageService);
        managedThingProvider = getService(ThingProvider.class, ManagedThingProvider.class);

        Map<String, Object> properties = new HashMap<>();
        properties.put(GatewayConfig.HOST, "1.2.3.4");
        properties.put(GatewayConfig.CODE, "abc");
        bridge = BridgeBuilder.create(TradfriBindingConstants.GATEWAY_TYPE_UID, "1").withLabel("My Gateway")
                .withConfiguration(new Configuration(properties)).build();
        properties = new HashMap<>();
        properties.put(DeviceConfig.ID, "65537");
        thing = ThingBuilder.create(TradfriBindingConstants.THING_TYPE_DIMMABLE_LIGHT, "1").withLabel("My Bulb")
                .withBridge(bridge.getUID()).withConfiguration(new Configuration(properties)).build();
    }

    @After
    public void tearDown() {
        managedThingProvider.remove(thing.getUID());
        managedThingProvider.remove(bridge.getUID());
        unregisterService(volatileStorageService);
    }

    @Test
    public void creationOfTradfriGatewayHandler() {
        assertThat(bridge.getHandler(), is(nullValue()));
        managedThingProvider.add(bridge);
        waitForAssert(() -> assertThat(bridge.getHandler(), notNullValue()));
    }

    @Test
    public void creationOfTradfriLightHandler() {
        assertThat(thing.getHandler(), is(nullValue()));
        managedThingProvider.add(bridge);
        managedThingProvider.add(thing);
        waitForAssert(() -> assertThat(thing.getHandler(), notNullValue()));
    }
}
