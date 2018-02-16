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
package org.eclipse.smarthome.binding.mqttgeneric.handler;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.HashMap;
import java.util.Map;

import javax.naming.ConfigurationException;

import org.eclipse.smarthome.binding.mqttgeneric.MqttBrokerBindingConstants;
import org.eclipse.smarthome.binding.mqttgeneric.internal.discovery.MqttServiceDiscoveryService;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

/**
 * Tests cases for {@link ThingHandler}.
 *
 * @author David Graeff - Initial contribution
 */
public class MqttBrokerConnectionHandlerTest {
    final String URL = "tcp://123.1.2.3";
    final String BROKERNAME = "testname";
    MqttBrokerConnectionHandler handler;

    @Mock
    private ThingHandlerCallback callback;

    @Mock
    private Bridge thing;

    @Mock
    MqttService service;

    @Mock
    MqttBrokerConnection connection;

    @Before
    public void setUp() {
        initMocks(this);
        when(thing.getUID()).thenReturn(MqttServiceDiscoveryService.makeThingUID(BROKERNAME));
        Map<String, Object> properties = new HashMap<>();
        properties.put(MqttBrokerBindingConstants.PARAM_BRIDGE_name, BROKERNAME);
        when(thing.getConfiguration()).thenReturn(new Configuration(properties));
        handler = new MqttBrokerConnectionHandler(thing, service);
        handler.setCallback(callback);
        handler.initialize();
    }

    @Test
    public void brokerAddedWrongID() throws ConfigurationException, MqttException {
        MqttBrokerConnection brokerConnection = mock(MqttBrokerConnection.class);
        when(brokerConnection.isConnected()).thenReturn(true);
        when(brokerConnection.getName()).thenReturn("something");
        handler.brokerAdded(brokerConnection);
        assertNull(handler.getConnection());

        // the invocation is from handler#initialize in setup:
        verify(callback, times(1)).statusUpdated(any(), any());
    }

    @Test
    public void brokerRemovedBroker() throws ConfigurationException, MqttException {
        MqttBrokerConnection brokerConnection = mock(MqttBrokerConnection.class);
        when(brokerConnection.isConnected()).thenReturn(true);
        when(brokerConnection.getName()).thenReturn(BROKERNAME);
        handler.brokerAdded(brokerConnection);
        assertThat(handler.getConnection(), is(brokerConnection));
        handler.brokerRemoved(brokerConnection);
        assertNull(handler.getConnection());
    }

    @Test
    public void brokerAddedConnectedBroker() throws ConfigurationException, MqttException {
        MqttBrokerConnection brokerConnection = mock(MqttBrokerConnection.class);
        when(brokerConnection.isConnected()).thenReturn(true);
        when(brokerConnection.getName()).thenReturn(BROKERNAME);
        handler.brokerAdded(brokerConnection);
        assertThat(handler.getConnection(), is(brokerConnection));

        verify(brokerConnection, times(0)).start();

        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);

        // 1st invocation is from handler#initialize in setup:
        verify(callback, times(2)).statusUpdated(eq(thing), statusInfoCaptor.capture());
        Assert.assertThat(statusInfoCaptor.getValue().getStatus(), is(ThingStatus.ONLINE));
    }

    @Test
    public void brokerAddedDisconnectedBroker() throws ConfigurationException, MqttException {
        MqttBrokerConnection brokerConnection = mock(MqttBrokerConnection.class);
        when(brokerConnection.isConnected()).thenReturn(false);
        when(brokerConnection.getName()).thenReturn(BROKERNAME);
        handler.brokerAdded(brokerConnection);
        assertThat(handler.getConnection(), is(brokerConnection));

        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);

        // 1st invocation is from handler#initialize in setup:
        verify(callback, times(2)).statusUpdated(eq(thing), statusInfoCaptor.capture());
        Assert.assertThat(statusInfoCaptor.getValue().getStatus(), is(ThingStatus.OFFLINE));
    }
}
