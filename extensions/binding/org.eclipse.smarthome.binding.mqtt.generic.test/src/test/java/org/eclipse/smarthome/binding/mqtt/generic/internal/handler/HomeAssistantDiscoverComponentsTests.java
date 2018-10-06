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
package org.eclipse.smarthome.binding.mqtt.generic.internal.handler;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homeassistant.DiscoverComponents;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * Tests the {@link DiscoverComponents} class.
 *
 * @author David Graeff - Initial contribution
 */
public class HomeAssistantDiscoverComponentsTests extends JavaOSGiTest {
    Throwable failure = null;

    @Mock
    MqttBrokerConnection connection;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void checkForTopic() throws MqttException, InterruptedException, ExecutionException, TimeoutException {
        ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
        DiscoverComponents discover = spy(
                new DiscoverComponents(ThingChannelConstants.testHomeAssistantThing, executor));

        // Before calling startDiscovery, the topic needs to be set
        discover.startDiscovery(connection, 500).thenRun(() -> {
        }).exceptionally(e -> {
            failure = e;
            return null;
        }).get(500, TimeUnit.MILLISECONDS);

        assertNotNull(failure);
    }
}
