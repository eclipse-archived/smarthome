/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.mqtt;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.ConfigurationException;

import org.junit.Test;

/**
 * Tests the MqttService class
 *
 * @author David Graeff - Initial contribution
 */
public class MqttServiceTests {
    // Tests addBrokersListener/removeBrokersListener
    @Test
    public void brokerConnectionListenerTests() throws ConfigurationException {
        MqttService service = new MqttService();
        assertFalse(service.hasBrokerObservers());
        MqttBrokersObserver observer = mock(MqttBrokersObserver.class);

        service.addBrokersListener(observer);
        assertTrue(service.hasBrokerObservers());

        MqttBrokerConnection connection = new MqttBrokerConnection("name", "tcp://123.123.123.123", false);
        assertTrue(service.addBrokerConnection(connection));
        verify(observer).brokerAdded(connection);

        service.removeBrokerConnection(connection);
        verify(observer).brokerRemoved(connection);

        service.removeBrokersListener(observer);
        assertFalse(service.hasBrokerObservers());
    }

    // Tests extractBrokerConfigurations() and addBrokerConnection(map)
    @Test
    public void extractBrokerConfigurationsTests() throws ConfigurationException, MqttException {
        MqttService service = new MqttService();

        Map<String, Object> properties = new Hashtable<>();
        properties.put("bam.name", "brokername");
        properties.put("bam.url", "tcp://123.123.123.123");
        Map<String, Map<String, String>> map = service.extractBrokerConfigurations(properties);
        assertEquals(map.size(), 1);
        Map<String, String> data = map.get("bam");
        assertNotNull(data);
        assertEquals("brokername", data.get("name"));
        assertEquals("tcp://123.123.123.123", data.get("url"));
    }

    // Tests if updates to the textual configuration are processed correctly
    @Test
    public void textualConfigurationTests() throws ConfigurationException, MqttException {
        MqttService service = new MqttService();

        Map<String, Object> properties = new Hashtable<>();
        properties.put("bam.name", "brokername");
        properties.put("bam.url", "tcp://123.123.123.123");

        // Test activate
        service.activate(properties);
        assertThat(service.getAllBrokerConnections(), hasSize(1));
        assertNotNull(service.getBrokerConnection("brokername"));

        Map<String, Object> properties2 = new Hashtable<>();
        properties2.put("bam2.name", "brokername2");
        properties2.put("bam2.url", "tcp://123.123.123.123");

        // Test configuration change
        service.modified(properties2);
        assertThat(service.getAllBrokerConnections(), hasSize(1));
        assertNull(service.getBrokerConnection("brokername"));
        assertNotNull(service.getBrokerConnection("brokername2"));

        // Test if old broker connections are freed correctly
        service.modified(Collections.emptyMap());
        assertThat(service.getAllBrokerConnections(), hasSize(0));
    }

    @Test
    public void brokerConnectionAddRemoveEnumerateTests() {
        MqttService service = new MqttService();
        MqttBrokerConnection connection;
        try {
            connection = new MqttBrokerConnection("name", "tcp://123.123.123.123", false);
        } catch (ConfigurationException c) {
            fail("Couldn't create a MqttBrokerConnection object");
            return;
        }
        // Add
        assertThat(service.getAllBrokerConnections(), hasSize(0));
        assertTrue(service.addBrokerConnection(connection));
        assertFalse(service.addBrokerConnection(connection));

        // Get/Enumerate
        assertNotNull(service.getBrokerConnection("name"));
        assertThat(service.getAllBrokerConnections(), hasSize(1));

        // Remove
        service.removeBrokerConnection(connection);
        assertThat(service.getAllBrokerConnections(), hasSize(0));
    }
}
