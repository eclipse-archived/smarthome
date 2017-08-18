/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.lwm2m;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.ConfigurationException;

import org.eclipse.smarthome.io.transport.lwm2m.LwM2MBrokerConnection;
import org.eclipse.smarthome.io.transport.lwm2m.LwM2MBrokersObserver;
import org.eclipse.smarthome.io.transport.lwm2m.LwM2MException;
import org.eclipse.smarthome.io.transport.lwm2m.LwM2MServerService;
import org.junit.Test;

/**
 * Tests the LwM2MService class
 *
 * @author David Graeff - Initial contribution
 */
public class LwM2MServiceTests {
    // Tests addBrokersListener/removeBrokersListener
    @Test
    public void brokerConnectionListenerTests() throws ConfigurationException {
        LwM2MServerService service = new LwM2MServerService();
        assertFalse(service.hasBrokerObservers());
        LwM2MBrokersObserver observer = mock(LwM2MBrokersObserver.class);

        service.addBrokersListener(observer);
        assertTrue(service.hasBrokerObservers());

        LwM2MBrokerConnection connection = new LwM2MBrokerConnection("name", "tcp://123.123.123.123", false);
        assertTrue(service.addBrokerConnection(connection));
        verify(observer).brokerAdded(connection);

        service.removeBrokerConnection(connection);
        verify(observer).brokerRemoved(connection);

        service.removeBrokersListener(observer);
        assertFalse(service.hasBrokerObservers());
    }

    // Tests extractBrokerConfigurations() and addBrokerConnection(map)
    @Test
    public void textualConfigurationTests() throws ConfigurationException, LwM2MException {
        LwM2MServerService service = new LwM2MServerService();

        Dictionary<String, String> properties = new Hashtable<>();
        properties.put("bam.name", "brokername");
        properties.put("bam.url", "tcp://123.123.123.123");
        Map<String, Map<String, String>> map = service.extractBrokerConfigurations(properties);
        assertEquals(map.size(), 1);
        Map<String, String> data = map.get("bam");
        assertNotNull(data);
        assertEquals("brokername", data.get("name"));
        assertEquals("tcp://123.123.123.123", data.get("url"));

        assertThat(service.getAllBrokerConnections(), hasSize(0));
        service.addBrokerConnection(data);
        assertThat(service.getAllBrokerConnections(), hasSize(1));
        assertNotNull(service.getBrokerConnection("brokername"));
    }

    @Test
    public void brokerConnectionAddRemoveEnumerateTests() {
        LwM2MServerService service = new LwM2MServerService();
        LwM2MBrokerConnection connection;
        try {
            connection = new LwM2MBrokerConnection("name", "tcp://123.123.123.123", false);
        } catch (ConfigurationException c) {
            fail("Couldn't create a LwM2MBrokerConnection object");
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
