/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.mqttgeneric.handler;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import javax.naming.ConfigurationException;

import org.eclipse.smarthome.binding.mqttgeneric.internal.discovery.MqttServiceDiscoveryService;
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
import org.mockito.MockitoAnnotations;

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
        MockitoAnnotations.initMocks(this);
        when(thing.getUID()).thenReturn(MqttServiceDiscoveryService.makeThingUID(BROKERNAME));
        handler = new MqttBrokerConnectionHandler(thing, service);
        handler.setCallback(callback);
    }

    @Test
    public void brokerAddedWrongID() throws ConfigurationException, MqttException {
        MqttBrokerConnection brokerConnection = mock(MqttBrokerConnection.class);
        when(brokerConnection.isConnected()).thenReturn(true);
        when(brokerConnection.getName()).thenReturn("something");
        handler.brokerAdded(brokerConnection);
        assertNull(handler.getConnection());
        verify(callback, times(0)).statusUpdated(anyObject(), anyObject());
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
        verify(callback).statusUpdated(eq(thing), statusInfoCaptor.capture());
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
        verify(callback).statusUpdated(eq(thing), statusInfoCaptor.capture());
        Assert.assertThat(statusInfoCaptor.getValue().getStatus(), is(ThingStatus.OFFLINE));
    }
}
