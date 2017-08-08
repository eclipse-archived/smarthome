/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal;

import static org.eclipse.smarthome.binding.hue.HueBindingConstants.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.smarthome.binding.hue.handler.HueBridgeHandler;
import org.eclipse.smarthome.binding.hue.internal.discovery.HueLightDiscoveryService;
import org.eclipse.smarthome.binding.hue.test.AbstractHueOSGiTest;
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
import org.eclipse.smarthome.test.AsyncResultWrapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link HueLightDiscoveryService}.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Andre Fuechsel - added test 'assert start search is called()'
 *         - modified tests after introducing the generic thing types
 * @author Denis Dudnik - switched to internally integrated source of Jue library
 * @author Markus Rathgeb - migrated to plain Java test
 */
public class HueLightDiscoveryServiceOSGiTest extends AbstractHueOSGiTest {

    protected HueThingHandlerFactory hueThingHandlerFactory;
    protected DiscoveryListener discoveryListener;
    protected ThingRegistry thingRegistry;
    protected Bridge hueBridge;
    protected HueBridgeHandler hueBridgeHandler;
    protected HueLightDiscoveryService discoveryService;

    protected final ThingTypeUID BRIDGE_THING_TYPE_UID = new ThingTypeUID("hue", "bridge");
    protected final ThingUID BRIDGE_THING_UID = new ThingUID(BRIDGE_THING_TYPE_UID, "testBridge");

    @Before
    public void setUp() {
        registerVolatileStorageService();

        thingRegistry = getService(ThingRegistry.class, ThingRegistry.class);
        assertThat(thingRegistry, is(notNullValue()));

        Configuration configuration = new Configuration();
        configuration.put(HOST, "1.2.3.4");
        configuration.put(USER_NAME, "testUserName");
        configuration.put(SERIAL_NUMBER, "testSerialNumber");

        hueBridge = (Bridge) thingRegistry.createThingOfType(BRIDGE_THING_TYPE_UID, BRIDGE_THING_UID, null, "Bridge",
                configuration);

        assertThat(hueBridge, is(notNullValue()));
        thingRegistry.add(hueBridge);

        hueBridgeHandler = getThingHandler(hueBridge, HueBridgeHandler.class);
        assertThat(hueBridgeHandler, is(notNullValue()));

        discoveryService = getService(DiscoveryService.class, HueLightDiscoveryService.class);
        assertThat(discoveryService, is(notNullValue()));
    }

    @After
    public void cleanUp() {
        thingRegistry.remove(BRIDGE_THING_UID);
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
        FullLight light = new FullLight();
        light.setId("1");
        light.setModelID("LCT001");
        light.setType("Extended color light");

        AsyncResultWrapper<DiscoveryResult> resultWrapper = new AsyncResultWrapper<DiscoveryResult>();

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
                    Collection<ThingTypeUID> thingTypeUIDs) {
                return null;
            }
        });

        discoveryService.onLightAdded(null, light);
        waitForAssert(() -> {
            assertTrue(resultWrapper.isSet());
        });

        final DiscoveryResult result = resultWrapper.getWrappedObject();
        assertThat(result.getFlag(), is(DiscoveryResultFlag.NEW));
        assertThat(result.getThingUID().toString(), is("hue:0210:testBridge:" + light.getId()));
        assertThat(result.getThingTypeUID(), is(THING_TYPE_EXTENDED_COLOR_LIGHT));
        assertThat(result.getBridgeUID(), is(hueBridge.getUID()));
        assertThat(result.getProperties().get(LIGHT_ID), is(light.getId()));
    }

    @Test
    public void startSearchIsCalled() {
        final AtomicBoolean searchHasBeenTriggered = new AtomicBoolean(false);
        AsyncResultWrapper<String> addressWrapper = new AsyncResultWrapper<String>();
        AsyncResultWrapper<String> bodyWrapper = new AsyncResultWrapper<String>();

        MockedHttpClient mockedHttpClient = new MockedHttpClient() {

            @Override
            public Result put(String address, String body) throws IOException {
                addressWrapper.set(address);
                bodyWrapper.set(body);
                return new Result("", 200);
            }

            @Override
            public Result get(String address) throws IOException {
                if (address.endsWith("testUserName/")) {
                    String body = "{\"lights\":{}}";
                    return new Result(body, 200);
                } else {
                    return null;
                }
            }

            @Override
            public Result post(String address, String body) throws IOException {
                if (address.endsWith("lights")) {
                    String bodyReturn = "{\"success\": {\"/lights\": \"Searching for new devices\"}}";
                    searchHasBeenTriggered.set(true);
                    return new Result(bodyReturn, 200);
                } else {
                    return null;
                }
            }
        };

        installHttpClientMock(hueBridgeHandler, mockedHttpClient);

        ThingStatusInfo online = ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build();
        waitForAssert(() -> {
            assertThat(hueBridge.getStatusInfo(), is(online));
        });

        discoveryService.startScan();
        waitForAssert(() -> {
            assertTrue(searchHasBeenTriggered.get());
        });
    }

    private void installHttpClientMock(HueBridgeHandler hueBridgeHandler, MockedHttpClient mockedHttpClient) {
        waitForAssert(() -> {
            try {
                // mock HttpClient
                final Field hueBridgeField = HueBridgeHandler.class.getDeclaredField("hueBridge");
                hueBridgeField.setAccessible(true);
                final Object hueBridgeValue = hueBridgeField.get(hueBridgeHandler);
                assertThat(hueBridgeValue, is(notNullValue()));

                final Field httpClientField = HueBridge.class.getDeclaredField("http");
                httpClientField.setAccessible(true);
                httpClientField.set(hueBridgeValue, mockedHttpClient);

                final Field usernameField = HueBridge.class.getDeclaredField("username");
                usernameField.setAccessible(true);
                usernameField.set(hueBridgeValue, hueBridgeHandler.getThing().getConfiguration().get(USER_NAME));
            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ex) {
                Assert.fail("Reflection usage error");
            }
        });
        hueBridgeHandler.initialize();
    }

}
