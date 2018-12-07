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
package org.eclipse.smarthome.binding.hue.internal;

import static org.eclipse.smarthome.binding.hue.HueBindingConstants.THING_TYPE_EXTENDED_COLOR_LIGHT;
import static org.eclipse.smarthome.core.thing.Thing.PROPERTY_SERIAL_NUMBER;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.binding.hue.handler.HueBridgeHandler;
import org.eclipse.smarthome.binding.hue.handler.HueBridgeHandlerConfig;
import org.eclipse.smarthome.binding.hue.handler.HueBridgeHandlerHelper;
import org.eclipse.smarthome.binding.hue.handler.HueLightHandlerConfig;
import org.eclipse.smarthome.binding.hue.internal.discovery.HueLightDiscoveryService;
import org.eclipse.smarthome.binding.hue.internal.dto.HueConfig;
import org.eclipse.smarthome.binding.hue.internal.dto.Light;
import org.eclipse.smarthome.binding.hue.internal.utils.AsyncHttpClient;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalMatchers;
import org.osgi.framework.InvalidSyntaxException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Tests for {@link HueLightDiscoveryService}.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Andre Fuechsel - added test 'assert start search is called()'
 *         - modified tests after introducing the generic thing types
 * @author Denis Dudnik - switched to internally integrated source of Jue library
 * @author Markus Rathgeb - migrated to plain Java test
 */
public class HueLightDiscoveryServiceOSGiTest extends JavaOSGiTest {

    protected DiscoveryListener discoveryListener;
    protected ThingRegistry thingRegistry;
    protected Bridge hueBridge;
    protected HueBridgeHandler hueBridgeHandler;
    protected HueLightDiscoveryService discoveryService;

    protected final ThingTypeUID BRIDGE_THING_TYPE_UID = new ThingTypeUID("hue", "bridge");
    protected final ThingUID BRIDGE_THING_UID = new ThingUID(BRIDGE_THING_TYPE_UID, "testBridge");

    Gson gson = new GsonBuilder().setDateFormat(HueBridge.DATE_FORMAT).create();

    @Before
    public void setUp() throws InvalidSyntaxException {
        registerVolatileStorageService();

        thingRegistry = getService(ThingRegistry.class, ThingRegistry.class);
        assertThat(thingRegistry, is(notNullValue()));

        Configuration configuration = new Configuration();
        configuration.put(HueBridgeHandlerConfig.HOST, "1.2.3.4");
        configuration.put(HueBridgeHandlerConfig.USER_NAME, "testUserName");
        configuration.put(PROPERTY_SERIAL_NUMBER, "testSerialNumber");

        hueBridge = (Bridge) thingRegistry.createThingOfType(BRIDGE_THING_TYPE_UID, BRIDGE_THING_UID, null, "Bridge",
                configuration);

        assertThat(hueBridge, is(notNullValue()));
        thingRegistry.add(hueBridge);

        hueBridgeHandler = (HueBridgeHandler) hueBridge.getHandler();
        assertThat(hueBridgeHandler, is(notNullValue()));

        discoveryService = null;
        for (HueLightDiscoveryService service : getServices(DiscoveryService.class, HueLightDiscoveryService.class)) {
            if (service.getThingHandler() == hueBridgeHandler) {
                discoveryService = service;
            }
        }
        assertThat(discoveryService, is(notNullValue()));
    }

    @After
    public void cleanUp() {
        unregisterCurrentDiscoveryListener();
        thingRegistry.remove(BRIDGE_THING_UID);
        waitForAssert(() -> {
            assertThat(discoveryService.isInactive(), is(true));
        }, 500, 10);
        discoveryService = null;
    }

    private void registerDiscoveryListener(DiscoveryListener discoveryListener) {
        unregisterCurrentDiscoveryListener();
        this.discoveryListener = discoveryListener;
        discoveryService.addDiscoveryListener(this.discoveryListener);
    }

    private void unregisterCurrentDiscoveryListener() {
        if (this.discoveryListener != null) {
            discoveryService.removeDiscoveryListener(this.discoveryListener);
        }
    }

    @Test
    public void hueLightRegistration() {
        Light light = new Light();
        light.id = "1";
        light.modelid = "LCT001";
        light.type = "Extended color light";

        AtomicReference<DiscoveryResult> resultWrapper = new AtomicReference<>();

        registerDiscoveryListener(new DiscoveryListener() {
            @Override
            public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
                resultWrapper.set(result);
            }

            @Override
            public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
            }

            @Override
            public Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
                    Collection<ThingTypeUID> thingTypeUIDs, ThingUID bridgeUID) {
                return null;
            }
        });

        discoveryService.onLightAdded(light);
        waitForAssert(() -> {
            assertTrue(resultWrapper.get() != null);
        }, 1000, 50);

        final DiscoveryResult result = resultWrapper.get();
        assertThat(result.getFlag(), is(DiscoveryResultFlag.NEW));
        assertThat(result.getThingUID().toString(), is("hue:0210:testBridge:" + light.id));
        assertThat(result.getThingTypeUID(), is(THING_TYPE_EXTENDED_COLOR_LIGHT));
        assertThat(result.getBridgeUID(), is(hueBridge.getUID()));
        assertThat(result.getProperties().get(HueLightHandlerConfig.LIGHT_ID), is(light.id));
    }

    @Test
    public void startSearchIsCalled() throws IOException {

        final AtomicBoolean searchHasBeenTriggered = new AtomicBoolean(false);

        AsyncHttpClient client = mock(AsyncHttpClient.class);
        doReturn(new AsyncHttpClient.Result("", 200, "")).when(client).put(anyString(), anyString());
        doReturn(new AsyncHttpClient.Result("{\"lights\":{}}", 200, "")).when(client).get(eq("testUserName"));
        doReturn(new AsyncHttpClient.Result("", 404, "")).when(client).get(AdditionalMatchers.not(eq("testUserName")));
        doAnswer(a -> {
            String address = (String) a.getArgument(0);
            if (address.endsWith("lights")) {
                searchHasBeenTriggered.set(true);
                return new AsyncHttpClient.Result("{\"success\": {\"/lights\": \"Searching for new devices\"}}", 200,
                        "");
            } else {
                return new AsyncHttpClient.Result("", 404, "");
            }
        }).when(client).post(anyString(), anyString());

        HttpClient httpClient = mock(HttpClient.class);
        AsyncHttpClient mockedHttpClient = new AsyncHttpClient(httpClient, 5000) {

            @Override
            public Result put(String address, String body) throws IOException {
                return new Result("", 200, "");
            }

            @Override
            public Result get(String address) throws IOException {
                if (address.endsWith("/config")) {
                    HueConfig config = new HueConfig();
                    config.name = "testname";
                    config.bridgeid = "bridgeid";
                    return new AsyncHttpClient.Result(gson.toJson(config), 200, "");
                } else if (address.endsWith("/lights")) {
                    return new AsyncHttpClient.Result("{}", 200, "");
                } else if (address.endsWith("testUserName")) {
                    return new AsyncHttpClient.Result("{\"lights\":{}}", 200, "");
                } else {
                    return new AsyncHttpClient.Result("", 404, "");
                }
            }

            @Override
            public Result post(String address, String body) throws IOException {
                if (address.endsWith("lights")) {
                    searchHasBeenTriggered.set(true);
                    return new AsyncHttpClient.Result("{\"success\": {\"/lights\": \"Searching for new devices\"}}",
                            200, "");
                } else {
                    return new AsyncHttpClient.Result("", 404, "");
                }
            }
        };

        assertThat(hueBridgeHandler.getHueBridge(), is(notNullValue()));
        hueBridgeHandler.getHueBridge().http = mockedHttpClient;
        HueBridgeHelper.setUsername(hueBridgeHandler.getHueBridge(),
                (String) hueBridgeHandler.getThing().getConfiguration().get(HueBridgeHandlerConfig.USER_NAME));

        hueBridgeHandler.initialize();
        HueBridgeHandlerHelper.cancelPollingJob(hueBridgeHandler);
        hueBridgeHandler.run();

        ThingStatusInfo online = ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build();
        waitForAssert(() -> {
            assertThat(hueBridge.getStatusInfo(), is(online));
        }, 1000, 50);

        discoveryService.startScan();
        waitForAssert(() -> {
            assertTrue(searchHasBeenTriggered.get());
        }, 1000, 50);
    }
}
