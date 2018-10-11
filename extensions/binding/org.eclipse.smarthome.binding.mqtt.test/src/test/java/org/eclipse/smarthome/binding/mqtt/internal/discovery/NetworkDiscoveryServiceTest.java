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
package org.eclipse.smarthome.binding.mqtt.internal.discovery;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.binding.mqtt.MqttBindingConstants;
import org.eclipse.smarthome.binding.mqtt.handler.MqttBrokerConnectionEx;
import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.ScanListener;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.service.cm.ConfigurationException;

/**
 * Tests cases for {@link NetworkDiscoveryService}.
 *
 * @author David Graeff - Initial contribution
 */
public class NetworkDiscoveryServiceTest {
    public static class NetworkDiscoveryServiceEx extends NetworkDiscoveryService {
        public int savedFoundBrokers = 0;

        @Override
        protected synchronized void finished() {
            savedFoundBrokers = foundBrokers;
            super.finished();
        }

        @Override
        protected List<String> getScannableIPs() {
            List<String> mockedIPS = new ArrayList<>();
            mockedIPS.add("129.1.2.3");
            return mockedIPS;
        }

        @Override
        public int getScanTimeout() {
            return 0;
        }

        // Mock createTestConnections(). We return MqttBrokerConnection mocks.
        // Half of them return isConnected()==true
        @Override
        protected MqttBrokerConnection[] createTestConnections(List<String> networkIPs) throws ConfigurationException {
            assertThat(networkIPs.size(), is(2));
            MqttBrokerConnection[] connections = new MqttBrokerConnection[networkIPs.size() * 2];
            for (int i = 0; i < networkIPs.size(); ++i) {
                MqttBrokerConnectionEx c = new MqttBrokerConnectionEx(networkIPs.get(i), 80, false, null);
                connections[i * 2] = c;
                c = new MqttBrokerConnectionEx(networkIPs.get(i), 81, true, null);
                c.connectSuccess = false;
                connections[i * 2 + 1] = c;
            }
            return connections;
        }
    }

    @Mock
    private DiscoveryListener discoverListener;

    @Mock
    private ScanListener scanListener;

    private final NetworkDiscoveryServiceEx subject = new NetworkDiscoveryServiceEx();

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @SuppressWarnings("null")
    @Test
    public void testDiscovery() throws ConfigurationException, MqttException, InterruptedException {
        subject.addDiscoveryListener(discoverListener);

        subject.startScan(scanListener);

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            return;
        }

        assertTrue(subject.testConnections != null);
        assertThat(subject.testConnections.length, is(4));

        // only half of the test connections "connect"
        assertThat(subject.savedFoundBrokers, is(2));

        verify(scanListener).onFinished();

        // We expect 2 discoveries
        ArgumentCaptor<DiscoveryResult> discoveryCapture = ArgumentCaptor.forClass(DiscoveryResult.class);
        verify(discoverListener, times(2)).thingDiscovered(eq(subject), discoveryCapture.capture());
        List<DiscoveryResult> discoveryResults = discoveryCapture.getAllValues();
        assertThat(discoveryResults.size(), is(2));
        assertThat(discoveryResults.get(0).getThingTypeUID(), is(MqttBindingConstants.BRIDGE_TYPE_BROKER));
        assertThat(discoveryResults.get(1).getThingTypeUID(), is(MqttBindingConstants.BRIDGE_TYPE_BROKER));
    }
}
