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
package org.eclipse.smarthome.binding.hue.handler;

import static org.eclipse.smarthome.core.thing.Thing.PROPERTY_SERIAL_NUMBER;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.hue.internal.HueBridge;
import org.eclipse.smarthome.binding.hue.internal.exceptions.ApiException;
import org.eclipse.smarthome.binding.hue.internal.exceptions.LinkButtonException;
import org.eclipse.smarthome.binding.hue.internal.utils.AsyncHttpClient;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Tests for {@link HueBridgeHandler}.
 *
 * @author Oliver Libutzki - Initial contribution
 * @author Michael Grammling - Initial contribution
 * @author Denis Dudnik - switched to internally integrated source of Jue library
 * @author David Graeff - migrated to plain Java test
 */
public class HueBridgeHandlerTest {

    private final ThingTypeUID BRIDGE_THING_TYPE_UID = new ThingTypeUID("hue", "bridge");
    private final ThingUID BRIDGE_THING_UID = new ThingUID(BRIDGE_THING_TYPE_UID, "testBridge");

    private static final String TEST_USER_NAME = "eshTestUser";
    private static final String DUMMY_HOST = "1.2.3.4";

    Gson gson = new GsonBuilder().setDateFormat(HueBridge.DATE_FORMAT).create();

    @Mock
    AsyncHttpClient asyncHttpClient;

    @Mock
    Bridge mockBridge;

    @Mock
    ThingHandlerCallback callback;

    protected HueBridgeHandler hueBridgeHandler;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        hueBridgeHandler = new HueBridgeHandler(mockBridge, asyncHttpClient);
        hueBridgeHandler.setCallback(callback);
        when(mockBridge.getHandler()).thenReturn(hueBridgeHandler);
        when(mockBridge.getStatusInfo())
                .thenReturn(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        // Mocked response for unauthorized config REST API endpoint
        AsyncHttpClient.Result r = new AsyncHttpClient.Result("{}", 200, "");
        when(asyncHttpClient.get(ArgumentMatchers.eq("http://1.2.3.4/api/config"))).thenReturn(r);
    }

    @After
    public void cleanUp() {
    }

    @Test
    public void assertThatANewUserIsAddedToConfigIfNotExistingYet() throws IOException {
        Configuration configuration = new Configuration();
        configuration.put(HueBridgeHandlerConfig.HOST, DUMMY_HOST);
        configuration.put(PROPERTY_SERIAL_NUMBER, "testSerialNumber");
        when(mockBridge.getConfiguration()).thenReturn(configuration);

        AsyncHttpClient.Result r = new AsyncHttpClient.Result("[{'success':{'username':'eshTestUser'}}]", 200, "");
        when(asyncHttpClient.post(ArgumentMatchers.eq("http://1.2.3.4/api"), anyString())).thenReturn(r);
        AsyncHttpClient.Result r2 = new AsyncHttpClient.Result("{}", 200, "");
        when(asyncHttpClient.get(ArgumentMatchers.eq("http://1.2.3.4/api/eshTestUser/config"))).thenReturn(r2);
        when(asyncHttpClient.get(ArgumentMatchers.eq("http://1.2.3.4/api/eshTestUser/lights"))).thenReturn(r2);

        hueBridgeHandler.hueBridge = new HueBridge(asyncHttpClient);
        hueBridgeHandler.initialize();
        HueBridgeHandlerHelper.cancelPollingJob(hueBridgeHandler);
        hueBridgeHandler.run();

        assertThat(configuration.get(HueBridgeHandlerConfig.USER_NAME), equalTo("eshTestUser"));
    }

    @Test
    public void goOfflineIfNoUserAndCreateUserFailed() throws IOException {
        Configuration configuration = new Configuration();
        configuration.put(HueBridgeHandlerConfig.HOST, DUMMY_HOST);
        configuration.put(PROPERTY_SERIAL_NUMBER, "testSerialNumber");
        when(mockBridge.getConfiguration()).thenReturn(configuration);

        AsyncHttpClient.Result r = new AsyncHttpClient.Result(
                "[{'error':{'type':101,'description':'link button not pressed'}}]", 403, "");
        when(asyncHttpClient.post(ArgumentMatchers.eq("http://1.2.3.4/api"), anyString())).thenReturn(r);

        hueBridgeHandler.hueBridge = new HueBridge(asyncHttpClient);
        hueBridgeHandler.initialize();
        HueBridgeHandlerHelper.cancelPollingJob(hueBridgeHandler);
        hueBridgeHandler.run();

        verify(callback).statusUpdated(any(), argThat(t -> t.getStatus().equals(ThingStatus.OFFLINE)
                && t.getStatusDetail().equals(ThingStatusDetail.CONFIGURATION_ERROR)));
    }

    @Test
    public void accessAPIwithWhitelistedUser() throws IOException {
        Configuration configuration = new Configuration();
        configuration.put(HueBridgeHandlerConfig.HOST, DUMMY_HOST);
        configuration.put(HueBridgeHandlerConfig.USER_NAME, TEST_USER_NAME);
        configuration.put(PROPERTY_SERIAL_NUMBER, "testSerialNumber");
        when(mockBridge.getConfiguration()).thenReturn(configuration);

        AsyncHttpClient.Result r = new AsyncHttpClient.Result("{}", 200, "");
        when(asyncHttpClient.get(ArgumentMatchers.eq("http://1.2.3.4/api/eshTestUser/config"))).thenReturn(r);
        when(asyncHttpClient.get(ArgumentMatchers.eq("http://1.2.3.4/api/eshTestUser/lights"))).thenReturn(r);

        hueBridgeHandler.hueBridge = new HueBridge(asyncHttpClient);
        hueBridgeHandler.initialize();
        HueBridgeHandlerHelper.cancelPollingJob(hueBridgeHandler);
        hueBridgeHandler.run();

        verify(callback).statusUpdated(any(), argThat(t -> t.getStatus().equals(ThingStatus.ONLINE)));

        // Test offline if connection lost as well in this test
        hueBridgeHandler.onConnectionLost();

        verify(callback).statusUpdated(any(), argThat(
                t -> t.getStatus().equals(ThingStatus.OFFLINE) && t.getStatusDetail().equals(ThingStatusDetail.NONE)));
    }

    @Test
    public void notAuthenticatedUserAndLinkButtonNotPressed() throws IOException {
        Configuration configuration = new Configuration();
        configuration.put(HueBridgeHandlerConfig.HOST, DUMMY_HOST);
        configuration.put(HueBridgeHandlerConfig.USER_NAME, "notAuthenticatedUser");
        configuration.put(PROPERTY_SERIAL_NUMBER, "testSerialNumber");
        when(mockBridge.getConfiguration()).thenReturn(configuration);

        AsyncHttpClient.Result r = new AsyncHttpClient.Result(
                "[{'error':{'type':101,'description':'link button not pressed'}}]", 403, "");
        when(asyncHttpClient.post(ArgumentMatchers.eq("http://1.2.3.4/api"), anyString())).thenReturn(r);

        when(asyncHttpClient.get(eq("http://1.2.3.4/api/notAuthenticatedUser/config"))).thenReturn(r);
        when(asyncHttpClient.get(eq("http://1.2.3.4/api/notAuthenticatedUser/lights"))).thenReturn(r);

        hueBridgeHandler.hueBridge = new HueBridge(asyncHttpClient);
        hueBridgeHandler.initialize();
        HueBridgeHandlerHelper.cancelPollingJob(hueBridgeHandler);
        hueBridgeHandler.run();

        verify(callback).statusUpdated(any(), argThat(t -> t.getStatus().equals(ThingStatus.OFFLINE)
                && t.getStatusDetail().equals(ThingStatusDetail.CONFIGURATION_ERROR)));
    }

    @Test
    public void notAuthenticatedUserAndLinkButtonNotPressed2() {
        Configuration configuration = new Configuration();
        configuration.put(HueBridgeHandlerConfig.HOST, DUMMY_HOST);
        configuration.put(PROPERTY_SERIAL_NUMBER, "testSerialNumber");
        when(mockBridge.getConfiguration()).thenReturn(configuration);

        hueBridgeHandler.hueBridge = new HueBridge(asyncHttpClient) {
            @Override
            public String createApiKey(@Nullable String username, String devicetype) throws IOException, ApiException {
                throw new LinkButtonException();
            };
        };

        hueBridgeHandler.createUser(null);

        verify(callback).statusUpdated(any(), argThat(t -> t.getStatus().equals(ThingStatus.OFFLINE)
                && t.getStatusDetail().equals(ThingStatusDetail.CONFIGURATION_ERROR)));
    }

    @Test
    public void assertThatAStatusConfigurationMessageForMissingBridgeIPIsProperlyReturnedIPIsNull() {
        Configuration configuration = new Configuration();
        configuration.put(HueBridgeHandlerConfig.HOST, null);
        configuration.put(PROPERTY_SERIAL_NUMBER, "testSerialNumber");
        when(mockBridge.getConfiguration()).thenReturn(configuration);

        hueBridgeHandler.hueBridge = new HueBridge(asyncHttpClient);
        hueBridgeHandler.initialize();
        HueBridgeHandlerHelper.cancelPollingJob(hueBridgeHandler);

        ConfigStatusMessage expected = ConfigStatusMessage.Builder.error(HueBridgeHandlerConfig.HOST)
                .withMessageKeySuffix(HueBridgeHandler.IP_ADDRESS_MISSING).withArguments(HueBridgeHandlerConfig.HOST)
                .build();

        assertEquals(expected, hueBridgeHandler.getConfigStatus().iterator().next());
    }

    @Test
    public void assertThatAStatusConfigurationMessageForMissingBridgeIPIsProperlyReturnedIPIsAnEmptyString() {
        Configuration configuration = new Configuration();
        configuration.put(HueBridgeHandlerConfig.HOST, "");
        configuration.put(PROPERTY_SERIAL_NUMBER, "testSerialNumber");
        when(mockBridge.getConfiguration()).thenReturn(configuration);

        hueBridgeHandler.hueBridge = new HueBridge(asyncHttpClient);
        hueBridgeHandler.initialize();
        HueBridgeHandlerHelper.cancelPollingJob(hueBridgeHandler);

        ConfigStatusMessage expected = ConfigStatusMessage.Builder.error(HueBridgeHandlerConfig.HOST)
                .withMessageKeySuffix(HueBridgeHandler.IP_ADDRESS_MISSING).withArguments(HueBridgeHandlerConfig.HOST)
                .build();

        assertEquals(expected, hueBridgeHandler.getConfigStatus().iterator().next());
    }
}
