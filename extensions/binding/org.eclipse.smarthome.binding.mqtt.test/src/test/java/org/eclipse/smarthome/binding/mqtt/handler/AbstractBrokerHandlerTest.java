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
package org.eclipse.smarthome.binding.mqtt.handler;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.eclipse.smarthome.binding.mqtt.internal.MqttThingID;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionState;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.service.cm.ConfigurationException;

/**
 * Tests cases for {@link AbstractBrokerHandler}.
 *
 * @author David Graeff - Initial contribution
 */
public class AbstractBrokerHandlerTest {
    private final String HOST = "tcp://123.1.2.3";
    private final int PORT = 80;
    private SystemBrokerHandler handler;

    @Mock
    private ThingHandlerCallback callback;

    @Mock
    private Bridge thing;

    @Mock
    private MqttService service;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        doReturn(MqttThingID.getThingUID(HOST, PORT)).when(thing).getUID();
        handler = new SystemBrokerHandler(thing, service);
        handler.setCallback(callback);
        assertThat(handler.brokerID, is(MqttThingID.getThingID(HOST, PORT)));
    }

    @Test
    public void brokerAddedWrongID() throws ConfigurationException, MqttException {
        MqttBrokerConnection brokerConnection = mock(MqttBrokerConnection.class);
        when(brokerConnection.connectionState()).thenReturn(MqttConnectionState.CONNECTED);
        handler.brokerAdded("nonsense_id", brokerConnection);
        assertNull(handler.connection);
        // We do not expect a status change, because brokerAdded will do nothing with invalid connections.
        verify(callback, times(0)).statusUpdated(anyObject(), anyObject());
    }

    @Test
    public void brokerRemovedBroker() throws ConfigurationException, MqttException {
        MqttBrokerConnection brokerConnection = mock(MqttBrokerConnection.class);
        when(brokerConnection.connectionState()).thenReturn(MqttConnectionState.CONNECTED);
        handler.brokerAdded(handler.brokerID, brokerConnection);
        assertThat(handler.connection, is(brokerConnection));
        handler.brokerRemoved("something", brokerConnection);
        assertNull(handler.connection);
    }

    @Test
    public void brokerAddedConnectedBroker() throws ConfigurationException, MqttException {
        MqttBrokerConnection brokerConnection = mock(MqttBrokerConnection.class);
        when(brokerConnection.connectionState()).thenReturn(MqttConnectionState.CONNECTED);
        handler.brokerAdded(handler.brokerID, brokerConnection);
        assertThat(handler.connection, is(brokerConnection));

        // We do not expect a call to start because the broker connection is already "connected".
        verify(brokerConnection, times(0)).start();

        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback).statusUpdated(eq(thing), statusInfoCaptor.capture());
        Assert.assertThat(statusInfoCaptor.getValue().getStatus(), is(ThingStatus.ONLINE));
    }

    @Test
    public void brokerAddedDisconnectedBroker() throws ConfigurationException, MqttException {
        MqttBrokerConnection brokerConnection = mock(MqttBrokerConnection.class);
        when(brokerConnection.connectionState()).thenReturn(MqttConnectionState.DISCONNECTED);
        handler.brokerAdded(handler.brokerID, brokerConnection);
        assertThat(handler.connection, is(brokerConnection));

        // We expect a call to start because the broker connection is not connected.
        verify(brokerConnection).start();

        // We do not expect a synchronous status update. The online/offline status will be updated
        // as soon as the connection is established or a timeout happened.
        verify(callback, times(0)).statusUpdated(any(), any());
    }
}
