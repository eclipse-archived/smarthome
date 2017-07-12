/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.mqtt.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import javax.naming.ConfigurationException;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionObserver;
import org.eclipse.smarthome.io.transport.mqtt.MqttMessageSubscriber;
import org.eclipse.smarthome.io.transport.mqtt.MqttWillAndTestament;
import org.junit.Test;

/**
 * Tests the MqttBrokerConnection class
 *
 * @author David Graeff - Initial contribution
 */
public class MqttBrokerConnectionTests {
    @Test
    public void testConstructor() throws ConfigurationException {
        // Test tcp and ssl URLs
        MqttBrokerConnection a = new MqttBrokerConnection("name", "tcp://123.123.123.123", false);
        MqttBrokerConnection b = new MqttBrokerConnection("name", "ssl://123.123.123.123", true);
        assertFalse(a.isTextualConfiguredBroker());
        assertTrue(b.isTextualConfiguredBroker());
    }

    @Test(expected = ConfigurationException.class)
    public void testConstructorInvalidProtocol() throws ConfigurationException {
        new MqttBrokerConnection("name", "unsupported://123.123.123.123", false);
    }

    @Test(expected = ConfigurationException.class)
    public void testConstructorInvalidName() throws ConfigurationException, MqttException {
        new MqttBrokerConnection(" ", "tcp://123.123.123.123", false);
    }

    @Test
    public void messageConsumerTests() throws ConfigurationException, MqttException {
        final String url = "tcp://123.123.123.123";
        final String name = "TestName12@!";
        MqttBrokerConnection a = new MqttBrokerConnection(name, url, false);
        assertFalse(a.isConsumers());
        MqttMessageSubscriber subscriber = mock(MqttMessageSubscriber.class);
        when(subscriber.getTopic()).thenAnswer(i -> "topic");
        a.addConsumer(subscriber);
        assertTrue(a.isConsumers());
        a.removeConsumer(subscriber);
        assertFalse(a.isConsumers());
    }

    @Test
    public void connectionObserverTests() throws ConfigurationException {
        final String url = "tcp://123.123.123.123";
        final String name = "TestName12@!";
        MqttBrokerConnection a = new MqttBrokerConnection(name, url, false);
        assertFalse(a.isConnectionObservers());
        MqttConnectionObserver connectionObserver = mock(MqttConnectionObserver.class);
        a.addConnectionObserver(connectionObserver);
        // Adding a connection observer should immediately call its setConnected() method.
        verify(connectionObserver).setConnected(false);

        assertTrue(a.isConnectionObservers());
        a.removeConnectionObserver(connectionObserver);
        assertFalse(a.isConnectionObservers());
    }

    @Test
    public void setterGetterTests() throws ConfigurationException {
        final String url = "tcp://123.123.123.123";
        final String name = "TestName12@!";
        MqttBrokerConnection a = new MqttBrokerConnection(name, url, false);
        assertEquals("URL getter", a.getUrl(), url);
        assertEquals("Name getter", a.getName(), name);

        a.setClientId("clientid");
        assertEquals("ClientID getter/setter", "clientid", a.getClientId());
        // client ids longer than 23 characters should be ignored
        a.setClientId("clientidclientidclientidclientid");
        assertEquals("ClientID too long check", "clientid", a.getClientId());

        a.setCredentials("user@!", "password123@^");
        assertEquals("User getter/setter", "user@!", a.getUser());
        assertEquals("Password getter/setter", "password123@^", a.getPassword());

        assertEquals(MqttBrokerConnection.DEFAULT_KEEPALIVE_INTERVAL, a.getKeepAliveInterval());
        a.setKeepAliveInterval(80);
        assertEquals(80, a.getKeepAliveInterval());

        assertFalse(a.isRetain());
        a.setRetain(true);
        assertTrue(a.isRetain());

        assertNull(a.getLastWill());
        assertNull(MqttWillAndTestament.fromString(""));
        a.setLastWill(MqttWillAndTestament.fromString("topic:message:1:true"));
        assertTrue(a.getLastWill().getTopic().equals("topic"));
        assertEquals(1, a.getLastWill().getQos());
        assertEquals(true, a.getLastWill().isRetain());
        byte b[] = { 'm', 'e', 's', 's', 'a', 'g', 'e' };
        assertTrue(Arrays.equals(a.getLastWill().getPayload(), b));

        assertEquals(MqttBrokerConnection.DEFAULT_QOS, a.getQos());
        a.setQos(10);
        assertEquals(MqttBrokerConnection.DEFAULT_QOS, a.getQos());
        a.setQos(-10);
        assertEquals(MqttBrokerConnection.DEFAULT_QOS, a.getQos());
        a.setQos(2);
        assertEquals(2, a.getQos());
        a.setQos(1);
        assertEquals(1, a.getQos());

        // Check for default ssl context provider and reconnect policy
        assertNotNull(a.getSSLContextProvider());
        assertNotNull(a.getReconnectPolicy());

        assertFalse(a.isConnected());
    }
}
