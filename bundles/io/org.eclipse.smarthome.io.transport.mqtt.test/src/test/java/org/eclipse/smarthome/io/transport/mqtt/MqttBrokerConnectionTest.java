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
package org.eclipse.smarthome.io.transport.mqtt;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.smarthome.io.transport.mqtt.reconnect.AbstractReconnectStrategy;
import org.eclipse.smarthome.io.transport.mqtt.reconnect.PeriodicReconnectStrategy;
import org.junit.Test;
import org.osgi.service.cm.ConfigurationException;

/**
 * Tests the MqttBrokerConnection class
 *
 * @author David Graeff - Initial contribution
 */
public class MqttBrokerConnectionTest {
    @Test
    public void messageConsumerTests() throws ConfigurationException, MqttException {
        MqttBrokerConnection a = new MqttBrokerConnection("123.123.123.123", null, false, null);
        // Expect no consumers
        assertFalse(a.hasConsumers());

        // Add a consumer (expect consumers getTopic() to be called)
        MqttMessageSubscriber subscriber = mock(MqttMessageSubscriber.class);
        when(subscriber.getTopic()).thenAnswer(i -> "topic");
        a.addConsumer(subscriber);
        verify(subscriber).getTopic();
        assertTrue(a.hasConsumers());

        // Remove consumer
        a.removeConsumer(subscriber);
        assertFalse(a.hasConsumers());
    }

    @Test
    public void reconnectPolicyDefaultTest() throws ConfigurationException, MqttException, InterruptedException {
        MqttBrokerConnection a = new MqttBrokerConnection("123.123.123.123", null, false, null);

        // Check if the default policy is set and that the broker within the policy is set.
        assertTrue(a.getReconnectStrategy() instanceof PeriodicReconnectStrategy);
        AbstractReconnectStrategy p = a.getReconnectStrategy();
        assertThat(p.getBrokerConnection(), equalTo(a));
    }

    @Test
    public void reconnectPolicyTests() throws ConfigurationException, MqttException, InterruptedException {
        MqttBrokerConnection a = spy(new MqttBrokerConnection("123.123.123.123", null, false, null));

        // Check setter
        a.setReconnectStrategy(new PeriodicReconnectStrategy());
        assertThat(a.getReconnectStrategy().getBrokerConnection(), equalTo(a));

        // Prepare a Mock to test if lostConnect is called and
        // if the PeriodicReconnectPolicy indeed calls start()
        PeriodicReconnectStrategy mockPolicy = spy(new PeriodicReconnectStrategy(10000, 0));
        doNothing().when(a).start();
        mockPolicy.start();
        a.isConnecting = true;

        // Fake a disconnect
        a.setReconnectStrategy(mockPolicy);
        IMqttActionListener l = a.createConnectionListener();
        doReturn(MqttConnectionState.DISCONNECTED).when(a).connectionState();
        IMqttToken token = mock(IMqttToken.class);
        when(token.getException()).thenReturn(new org.eclipse.paho.client.mqttv3.MqttException(1));
        l.onFailure(token, null);

        // Check lostConnect
        verify(mockPolicy).lostConnection();
        Thread.sleep(10);
        verify(a).start();
        assertTrue(mockPolicy.isReconnecting());

        // Fake connection established
        l.onSuccess(token);
        assertFalse(mockPolicy.isReconnecting());
    }

    @Test
    public void connectionObserverTests() throws ConfigurationException, MqttException {
        MqttBrokerConnection a = spy(new MqttBrokerConnection("123.123.123.123", null, false, null));

        // Add an observer
        assertFalse(a.hasConnectionObservers());
        MqttConnectionObserver connectionObserver = mock(MqttConnectionObserver.class);
        a.addConnectionObserver(connectionObserver);
        assertTrue(a.hasConnectionObservers());

        // Adding a connection observer should not immediately call its connectionStateChanged() method.
        verify(connectionObserver, times(0)).connectionStateChanged(eq(MqttConnectionState.DISCONNECTED), anyObject());

        // Cause a success callback
        IMqttActionListener l = a.createConnectionListener();
        doReturn(MqttConnectionState.CONNECTED).when(a).connectionState();
        l.onSuccess(null);
        verify(connectionObserver, times(1)).connectionStateChanged(eq(MqttConnectionState.CONNECTED), anyObject());

        // Cause a failure callback with a mocked token
        IMqttToken token = mock(IMqttToken.class);
        org.eclipse.paho.client.mqttv3.MqttException testException = new org.eclipse.paho.client.mqttv3.MqttException(
                1);
        when(token.getException()).thenReturn(testException);

        doReturn(MqttConnectionState.DISCONNECTED).when(a).connectionState();
        l.onFailure(token, null);
        verify(connectionObserver, times(1)).connectionStateChanged(eq(MqttConnectionState.DISCONNECTED),
                eq(testException));

        // Remove observer
        a.removeConnectionObserver(connectionObserver);
        assertFalse(a.hasConnectionObservers());
    }

    @Test
    public void lastWillAndTestamentTests() throws ConfigurationException {
        MqttBrokerConnection a = new MqttBrokerConnection("123.123.123.123", null, false, null);

        assertNull(a.getLastWill());
        assertNull(MqttWillAndTestament.fromString(""));
        a.setLastWill(MqttWillAndTestament.fromString("topic:message:1:true"));
        assertTrue(a.getLastWill().getTopic().equals("topic"));
        assertEquals(1, a.getLastWill().getQos());
        assertEquals(true, a.getLastWill().isRetain());
        byte b[] = { 'm', 'e', 's', 's', 'a', 'g', 'e' };
        assertTrue(Arrays.equals(a.getLastWill().getPayload(), b));
    }

    @Test(expected = IllegalArgumentException.class)
    public void lastWillAndTestamentConstructorTests() {
        new MqttWillAndTestament("", new byte[0], 0, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooLongClientID() throws ConfigurationException {
        // client ids longer than 23 characters should throw
        MqttBrokerConnection a = new MqttBrokerConnection("123.123.123.123", null, false,
                "clientidclientidclientidclientid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void qosInvalid() throws ConfigurationException {
        MqttBrokerConnection a = new MqttBrokerConnection("123.123.123.123", null, false, null);
        a.setQos(10);
    }

    @Test
    public void setterGetterTests() {
        MqttBrokerConnection a = new MqttBrokerConnection("123.123.123.123", null, false, "clientid");
        assertEquals("URL getter", a.getHost(), "123.123.123.123");
        assertEquals("Name getter", a.getPort(), 1883); // Check for non-secure port
        assertEquals("Secure getter", a.isSecure(), false);

        assertEquals("ClientID getter/setter", "clientid", a.getClientId());

        a.setCredentials("user@!", "password123@^");
        assertEquals("User getter/setter", "user@!", a.getUser());
        assertEquals("Password getter/setter", "password123@^", a.getPassword());

        assertEquals(MqttBrokerConnection.DEFAULT_KEEPALIVE_INTERVAL, a.getKeepAliveInterval());
        a.setKeepAliveInterval(80);
        assertEquals(80, a.getKeepAliveInterval());

        assertFalse(a.isRetain());
        a.setRetain(true);
        assertTrue(a.isRetain());

        assertEquals(MqttBrokerConnection.DEFAULT_QOS, a.getQos());
        a.setQos(2);
        assertEquals(2, a.getQos());
        a.setQos(1);
        assertEquals(1, a.getQos());

        // Check for default ssl context provider and reconnect policy
        assertNotNull(a.getSSLContextProvider());
        assertNotNull(a.getReconnectStrategy());

        assertThat(a.connectionState(), equalTo(MqttConnectionState.DISCONNECTED));
    }
}
