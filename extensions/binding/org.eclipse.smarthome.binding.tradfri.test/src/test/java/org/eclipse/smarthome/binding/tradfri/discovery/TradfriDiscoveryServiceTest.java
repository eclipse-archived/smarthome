/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.tradfri.discovery;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collection;

import org.eclipse.smarthome.binding.tradfri.DeviceConfig;
import org.eclipse.smarthome.binding.tradfri.TradfriBindingConstants;
import org.eclipse.smarthome.binding.tradfri.handler.TradfriGatewayHandler;
import org.eclipse.smarthome.binding.tradfri.internal.discovery.TradfriDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests for {@link TradfriDiscoveryService}.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class TradfriDiscoveryServiceTest {

    @Mock
    private TradfriGatewayHandler handler;

    private DiscoveryListener listener;
    private DiscoveryResult discoveryResult;

    private TradfriDiscoveryService discovery;

    @Before
    public void setUp() {
        initMocks(this);
        when(handler.getThing())
                .thenReturn(BridgeBuilder.create(TradfriBindingConstants.GATEWAY_TYPE_UID, "1").build());
        discovery = new TradfriDiscoveryService(handler);

        listener = new DiscoveryListener() {
            @Override
            public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
            }

            @Override
            public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
                discoveryResult = result;
            }

            @Override
            public Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
                    Collection<ThingTypeUID> thingTypeUIDs) {
                return null;
            }
        };
        discovery.addDiscoveryListener(listener);
    }

    @After
    public void cleanUp() {
        discoveryResult = null;
    }

    @Test
    public void correctSupportedTypes() {
        assertThat(discovery.getSupportedThingTypes().size(), is(3));
        assertTrue(discovery.getSupportedThingTypes().contains(TradfriBindingConstants.THING_TYPE_DIMMABLE_LIGHT));
        assertTrue(discovery.getSupportedThingTypes().contains(TradfriBindingConstants.THING_TYPE_COLOR_TEMP_LIGHT));
        assertTrue(discovery.getSupportedThingTypes().contains(TradfriBindingConstants.THING_TYPE_COLOR_LIGHT));
    }

    @Test
    public void validDiscoveryResult() {
        String json = "{\"9054\":0,\"9001\":\"LR\",\"5750\":2,\"9002\":1490983446,\"9020\":1491055861,\"9003\":65537,\"9019\":1,\"3\":{\"1\":\"TRADFRI bulb E27 WS opal 980lm\",\"0\":\"IKEA of Sweden\",\"2\":\"\",\"3\":\"1.1.1.1-5.7.2.0\",\"6\":1},\"3311\":[{\"5850\":1,\"5851\":254,\"5707\":0,\"5708\":0,\"5709\":33135,\"5710\":27211,\"9003\":0,\"5711\":0,\"5706\":\"efd275\"}]}";
        JsonObject data = new JsonParser().parse(json).getAsJsonObject();

        discovery.onUpdate("65537", data);

        assertNotNull(discoveryResult);
        assertThat(discoveryResult.getFlag(), is(DiscoveryResultFlag.NEW));
        assertThat(discoveryResult.getThingUID(), is(new ThingUID("tradfri:0220:1:65537")));
        assertThat(discoveryResult.getThingTypeUID(), is(TradfriBindingConstants.THING_TYPE_COLOR_TEMP_LIGHT));
        assertThat(discoveryResult.getBridgeUID(), is(new ThingUID("tradfri:gateway:1")));
        assertThat(discoveryResult.getProperties().get(DeviceConfig.ID), is(65537));
        assertThat(discoveryResult.getRepresentationProperty(), is(DeviceConfig.ID));
    }

    @Test
    public void validDiscoveryResultColorLightCWS() {
        String json = "{\"9001\":\"TRADFRI bulb E27 CWS opal 600lm\",\"9002\":1505151864,\"9020\":1505433527,\"9003\":65550,\"9019\":1,\"9054\":0,\"5750\":2,\"3\":{\"0\":\"IKEA of Sweden\",\"1\":\"TRADFRI bulb E27 CWS opal 600lm\",\"2\":\"\",\"3\":\"1.3.002\",\"6\":1},\"3311\":[{\"5850\":1,\"5708\":0,\"5851\":254,\"5707\":0,\"5709\":33137,\"5710\":27211,\"5711\":0,\"5706\":\"efd275\",\"9003\":0}]}";
        JsonObject data = new JsonParser().parse(json).getAsJsonObject();

        discovery.onUpdate("65550", data);

        assertNotNull(discoveryResult);
        assertThat(discoveryResult.getFlag(), is(DiscoveryResultFlag.NEW));
        assertThat(discoveryResult.getThingUID(), is(new ThingUID("tradfri:0210:1:65550")));
        assertThat(discoveryResult.getThingTypeUID(), is(TradfriBindingConstants.THING_TYPE_COLOR_LIGHT));
        assertThat(discoveryResult.getBridgeUID(), is(new ThingUID("tradfri:gateway:1")));
        assertThat(discoveryResult.getProperties().get(DeviceConfig.ID), is(65550));
        assertThat(discoveryResult.getRepresentationProperty(), is(DeviceConfig.ID));
    }

}
