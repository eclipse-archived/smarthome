/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.lwm2m;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import javax.naming.ConfigurationException;

import org.eclipse.paho.client.lwm2mv3.ILwM2MActionListener;
import org.eclipse.paho.client.lwm2mv3.ILwM2MToken;
import org.eclipse.smarthome.io.transport.lwm2m.LwM2MBrokerConnection;
import org.eclipse.smarthome.io.transport.lwm2m.LwM2MConnectionObserver;
import org.eclipse.smarthome.io.transport.lwm2m.LwM2MConnectionState;
import org.eclipse.smarthome.io.transport.lwm2m.LwM2MException;
import org.eclipse.smarthome.io.transport.lwm2m.LwM2MMessageSubscriber;
import org.eclipse.smarthome.io.transport.lwm2m.LwM2MWillAndTestament;
import org.eclipse.smarthome.io.transport.lwm2m.org;
import org.eclipse.smarthome.io.transport.lwm2m.reconnect.AbstractReconnectStrategy;
import org.eclipse.smarthome.io.transport.lwm2m.reconnect.PeriodicReconnectStrategy;
import org.junit.Test;

/**
 * Tests the LwM2MBrokerConnection class
 *
 * @author David Graeff - Initial contribution
 */
public class LwM2MBrokerConnectionTests {
    @Test
    public void testConstructor() throws ConfigurationException {
        // Test tcp and ssl URLs
        LwM2MBrokerConnection a = new LwM2MBrokerConnection("name", "tcp://123.123.123.123", false);
        LwM2MBrokerConnection b = new LwM2MBrokerConnection("name", "ssl://123.123.123.123", true);
        assertFalse(a.isTextualConfiguredBroker());
        assertTrue(b.isTextualConfiguredBroker());
    }

    @Test(expected = ConfigurationException.class)
    public void testConstructorInvalidProtocol() throws ConfigurationException {
        new LwM2MBrokerConnection("name", "unsupported://123.123.123.123", false);
    }

    @Test(expected = ConfigurationException.class)
    public void testConstructorInvalidName() throws ConfigurationException, LwM2MException {
        new LwM2MBrokerConnection(" ", "tcp://123.123.123.123", false);
    }

    @Test
    public void messageConsumerTests() throws ConfigurationException, LwM2MException {
        final String url = "tcp://123.123.123.123";
        final String name = "TestName12@!";
        LwM2MBrokerConnection a = new LwM2MBrokerConnection(name, url, false);
        // Expect no consumers
        assertFalse(a.hasConsumers());

        // Add a consumer (expect consumers getTopic() to be called)
        LwM2MMessageSubscriber subscriber = mock(LwM2MMessageSubscriber.class);
        when(subscriber.getTopic()).thenAnswer(i -> "topic");
        a.addConsumer(subscriber);
        verify(subscriber).getTopic();
        assertTrue(a.hasConsumers());

        // Remove consumer
        a.removeConsumer(subscriber);
        assertFalse(a.hasConsumers());
    }

    @Test
    public void reconnectPolicyDefaultTest() throws ConfigurationException, LwM2MException, InterruptedException {
        final String url = "tcp://123.123.123.123";
        final String name = "TestName12@!";
        LwM2MBrokerConnection a = new LwM2MBrokerConnection(name, url, false);

        // Check if the default policy is set and that the broker within the policy is set.
        assertTrue(a.getReconnectStrategy() instanceof PeriodicReconnectStrategy);
        AbstractReconnectStrategy p = a.getReconnectStrategy();
        assertThat(p.getBrokerConnection(), is(a));
    }

    @Test
    public void reconnectPolicyTests() throws ConfigurationException, LwM2MException, InterruptedException {
        final String url = "tcp://123.123.123.123";
        final String name = "TestName12@!";
        LwM2MBrokerConnection a = spy(new LwM2MBrokerConnection(name, url, false));

        // Check setter
        a.setReconnectStrategy(new PeriodicReconnectStrategy());
        assertThat(a.getReconnectStrategy().getBrokerConnection(), is(a));

        // Prepare a Mock to test if lostConnect is called and
        // if the PeriodicReconnectPolicy indeed calls start()
        PeriodicReconnectStrategy mockPolicy = spy(new PeriodicReconnectStrategy());
        doReturn(a).when(mockPolicy).getBrokerConnection();
        doReturn(0).when(mockPolicy).getFirstReconnectAfter();
        doReturn(10000).when(mockPolicy).getReconnectFrequency();
        doNothing().when(a).start();

        // Fake a disconnect
        a.setReconnectStrategy(mockPolicy);
        ILwM2MActionListener l = a.createConnectionListener();
        doReturn(false).when(a).isConnected();
        ILwM2MToken token = mock(ILwM2MToken.class);
        when(token.getException()).thenReturn(new org.eclipse.paho.client.lwm2mv3.LwM2MException(1));
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
    public void connectionObserverTests() throws ConfigurationException, LwM2MException {
        final String url = "tcp://123.123.123.123";
        final String name = "TestName12@!";
        LwM2MBrokerConnection a = spy(new LwM2MBrokerConnection(name, url, false));

        // Add an observer
        assertFalse(a.hasConnectionObservers());
        LwM2MConnectionObserver connectionObserver = mock(LwM2MConnectionObserver.class);
        a.addConnectionObserver(connectionObserver);
        assertTrue(a.hasConnectionObservers());

        // Adding a connection observer should not immediately call its connectionStateChanged() method.
        verify(connectionObserver, times(0)).connectionStateChanged(eq(LwM2MConnectionState.DISCONNECTED), anyObject());

        // Cause a success callback
        ILwM2MActionListener l = a.createConnectionListener();
        doReturn(true).when(a).isConnected();
        l.onSuccess(null);
        verify(connectionObserver, times(1)).connectionStateChanged(eq(LwM2MConnectionState.CONNECTED), anyObject());

        // Cause a failure callback with a mocked token
        ILwM2MToken token = mock(ILwM2MToken.class);
        org.eclipse.paho.client.lwm2mv3.LwM2MException testException = new org.eclipse.paho.client.lwm2mv3.LwM2MException(
                1);
        when(token.getException()).thenReturn(testException);

        doReturn(false).when(a).isConnected();
        l.onFailure(token, null);
        verify(connectionObserver, times(1)).connectionStateChanged(eq(LwM2MConnectionState.DISCONNECTED),
                eq(testException));

        // Remove observer
        a.removeConnectionObserver(connectionObserver);
        assertFalse(a.hasConnectionObservers());
    }

    @Test
    public void lastWillAndTestamentTests() throws ConfigurationException {
        final String url = "tcp://123.123.123.123";
        final String name = "TestName12@!";
        LwM2MBrokerConnection a = new LwM2MBrokerConnection(name, url, false);

        assertNull(a.getLastWill());
        assertNull(LwM2MWillAndTestament.fromString(""));
        a.setLastWill(LwM2MWillAndTestament.fromString("topic:message:1:true"));
        assertTrue(a.getLastWill().getTopic().equals("topic"));
        assertEquals(1, a.getLastWill().getQos());
        assertEquals(true, a.getLastWill().isRetain());
        byte b[] = { 'm', 'e', 's', 's', 'a', 'g', 'e' };
        assertTrue(Arrays.equals(a.getLastWill().getPayload(), b));
    }

    @Test(expected = IllegalArgumentException.class)
    public void lastWillAndTestamentConstructorTests() {
        new LwM2MWillAndTestament("", new byte[0], 0, false);
    }

    @Test
    public void setterGetterTests() throws ConfigurationException {
        final String url = "tcp://123.123.123.123";
        final String name = "TestName12@!";
        LwM2MBrokerConnection a = new LwM2MBrokerConnection(name, url, false);
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

        assertEquals(LwM2MBrokerConnection.DEFAULT_KEEPALIVE_INTERVAL, a.getKeepAliveInterval());
        a.setKeepAliveInterval(80);
        assertEquals(80, a.getKeepAliveInterval());

        assertFalse(a.isRetain());
        a.setRetain(true);
        assertTrue(a.isRetain());

        assertEquals(LwM2MBrokerConnection.DEFAULT_QOS, a.getQos());
        a.setQos(10);
        assertEquals(LwM2MBrokerConnection.DEFAULT_QOS, a.getQos());
        a.setQos(-10);
        assertEquals(LwM2MBrokerConnection.DEFAULT_QOS, a.getQos());
        a.setQos(2);
        assertEquals(2, a.getQos());
        a.setQos(1);
        assertEquals(1, a.getQos());

        // Check for default ssl context provider and reconnect policy
        assertNotNull(a.getSSLContextProvider());
        assertNotNull(a.getReconnectStrategy());

        assertFalse(a.isConnected());
    }
}
