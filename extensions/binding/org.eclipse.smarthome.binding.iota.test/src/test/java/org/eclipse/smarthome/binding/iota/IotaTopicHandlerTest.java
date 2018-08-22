/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http:www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.iota;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.eclipse.smarthome.binding.iota.handler.ChannelConfig;
import org.eclipse.smarthome.binding.iota.handler.IotaTopicThingHandler;
import org.eclipse.smarthome.binding.iota.handler.TransformationServiceProvider;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.eclipse.smarthome.test.java.JavaTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The {@link IotaTopicHandlerTest} provides test cases for {@link IotaHandler}. The tests provide mocks for supporting
 * entities using Mockito.
 *
 * @author Theo Giovanna - Initial contribution
 */
public class IotaTopicHandlerTest extends JavaTest {

    private IotaTestMocks mocks;
    private IotaTopicThingHandler iotaTopicThingHandler;

    private Bridge bridge;
    private Thing thing;

    private JsonObject data;
    private JsonObject itemName;
    private JsonObject itemState;
    private ChannelConfig config;

    @Before
    public void setUp() {
        mocks = new IotaTestMocks();
        bridge = mocks.initializeBridge();
        thing = mocks.initializeThingTopic("", null, "", "");

        /**
         * Mimic json data retrieved through MAM
         */
        data = new Gson().fromJson("{\"Items\":[]}", JsonObject.class);
        itemName = new JsonObject();
        itemState = new JsonObject();
        itemState.addProperty("TOPIC", "TEMPERATURE");
        itemState.addProperty("STATE", "2.0 °C");
        itemState.addProperty("TIME", Instant.now().toString());
        itemName.addProperty("NAME", "item");
        itemName.add("STATUS", itemState);
        data.get("Items").getAsJsonArray().add(itemName);
    }

    @Test
    public void thingShouldInitialize() {
        thing = mocks.initializeThingTopic("", null, "", "");
        assertEquals(ThingStatus.ONLINE, thing.getStatus());
    }

    @Test
    public void bridgeShouldInitialize() {
        assertEquals(ThingStatus.ONLINE, bridge.getStatus());
    }

    @Test
    public void stateShouldBeUpdatedGivenJsonData() {
        TransformationServiceProvider mockTransformationServiceProvider = Mockito
                .mock(TransformationServiceProvider.class);
        when(mockTransformationServiceProvider.getTransformationService("JSONPATH"))
                .thenReturn(mocks.new MockTransformationService("2.0 °C"));
        // Initialize thing and set value to channel
        thing = mocks.initializeThingTopic("1.0 °C", mockTransformationServiceProvider, "JSONPATH",
                "$.[0].STATUS.STATE");
        config = mocks.getConfigTopic();
        iotaTopicThingHandler = mocks.getIotaTopicThingHandler();
        // Current state value should be 1.0 °C
        assertEquals("1.0 °C", config.getValue().getValue().toFullString());
        // Updating the channel value with the Json Data
        iotaTopicThingHandler.updateAllStates(data.get("Items").getAsJsonArray());
        // New state value should be 2.0 °C
        assertEquals("2.0 °C", config.getValue().getValue().toFullString());
    }

    @Test
    public void thingStatusShouldChangeIfBridgeStatusDoes() {
        // Check that the thing is online
        assertEquals(ThingStatus.ONLINE, thing.getStatus());
        // Check that thing properly follows bridge status
        ThingHandler handler = thing.getHandler();
        assertNotNull(handler);
        handler.bridgeStatusChanged(ThingStatusInfoBuilder.create(ThingStatus.OFFLINE).build());
        assertEquals(ThingStatus.OFFLINE, thing.getStatusInfo().getStatus());
        handler.bridgeStatusChanged(ThingStatusInfoBuilder.create(ThingStatus.ONLINE).build());
        assertEquals(ThingStatus.ONLINE, thing.getStatusInfo().getStatus());
    }

    @Test
    public void processingMessageShouldUpdateChannelValue() {
        thing = mocks.initializeThingTopic("1", null, "", "");
        config = mocks.getConfigTopic();
        assertEquals("1", config.getValue().getValue().toFullString());
        config.processMessage("2");
        assertEquals("2", config.getValue().getValue().toFullString());
    }

}
