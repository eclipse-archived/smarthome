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
package org.eclipse.smarthome.binding.mqtt.generic;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.internal.ChannelStateUpdateListener;
import org.eclipse.smarthome.binding.mqtt.generic.internal.MqttChannelTypeProvider;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homeassistant.AbstractComponent;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homeassistant.ComponentSwitch;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homeassistant.DiscoverComponents;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.Device;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.Node;
import org.eclipse.smarthome.binding.mqtt.generic.internal.handler.ThingChannelConstants;
import org.eclipse.smarthome.binding.mqtt.generic.internal.values.OnOffValue;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionObserver;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionState;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A full implementation test, that starts the embedded MQTT broker and publishes a homeassistant MQTT discovery device
 * tree.
 *
 * @author David Graeff - Initial contribution
 */
public class HomeAssistantMQTTImplementationTests extends JavaOSGiTest {
    final Logger logger = LoggerFactory.getLogger(HomeAssistantMQTTImplementationTests.class);
    private MqttService mqttService;
    private MqttBrokerConnection embeddedConnection;
    private MqttBrokerConnection connection;
    private int registeredTopics = 100;
    Throwable failure = null;

    /**
     * Create an observer that fails the test as soon as the broker client connection changes its connection state
     * to something else then CONNECTED.
     */
    MqttConnectionObserver failIfChange = new MqttConnectionObserver() {
        @Override
        public void connectionStateChanged(@NonNull MqttConnectionState state, @Nullable Throwable error) {
            assertThat(state, is(MqttConnectionState.CONNECTED));
        }
    };

    @Before
    public void setUp()
            throws InterruptedException, MqttException, ConfigurationException, ExecutionException, TimeoutException {
        registerVolatileStorageService();
        initMocks(this);
        mqttService = getService(MqttService.class);

        // Wait for the EmbeddedBrokerService internal connection to be connected
        embeddedConnection = new EmbeddedBrokerTools().waitForConnection(mqttService);

        connection = new MqttBrokerConnection(embeddedConnection.getHost(), embeddedConnection.getPort(),
                embeddedConnection.isSecure(), "ha_mqtt");
        connection.setQos(1);
        connection.start().get(200, TimeUnit.MILLISECONDS);
        assertThat(connection.connectionState(), is(MqttConnectionState.CONNECTED));
        // If the connection state changes in between -> fail
        connection.addConnectionObserver(failIfChange);

        embeddedConnection.setRetain(true);
        embeddedConnection.setQos(1);

        // Add component configurations
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        final String testObject = "homeassistant/switch/node/" + ThingChannelConstants.testHomeAssistantThing.getId();
        final String str = "{'name':'testname','state_topic':'" + testObject + "/state','command_topic':'" + testObject
                + "/set'}";
        futures.add(embeddedConnection.publish(testObject + "/config", str.getBytes()));

        // Add component states
        futures.add(embeddedConnection.publish(testObject + "/state", "true".getBytes()));

        registeredTopics = futures.size();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).get(200, TimeUnit.MILLISECONDS);

        failure = null;
    }

    @After
    public void tearDown() throws InterruptedException, ExecutionException, TimeoutException {
        if (connection != null) {
            connection.removeConnectionObserver(failIfChange);
            connection.stop().get(500, TimeUnit.MILLISECONDS);
        }
    }

    @Test
    public void reconnectTest()
            throws InterruptedException, ExecutionException, TimeoutException, MqttException, ConfigurationException {
        connection.removeConnectionObserver(failIfChange);
        connection.stop().get(2000, TimeUnit.MILLISECONDS);
        connection = new MqttBrokerConnection(embeddedConnection.getHost(), embeddedConnection.getPort(),
                embeddedConnection.isSecure(), "ha_mqtt");
        connection.start().get(2000, TimeUnit.MILLISECONDS);
        connection.stop().get(2000, TimeUnit.MILLISECONDS);
        connection = new MqttBrokerConnection(embeddedConnection.getHost(), embeddedConnection.getPort(),
                embeddedConnection.isSecure(), "ha_mqtt");
        connection.start().get(2000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void retrieveAllTopics() throws MqttException, InterruptedException, ExecutionException, TimeoutException {
        CountDownLatch c = new CountDownLatch(registeredTopics);
        connection.subscribe("homeassistant/+/+/" + ThingChannelConstants.testHomeAssistantThing.getId() + "/#",
                (topic, payload) -> c.countDown()).get(200, TimeUnit.MILLISECONDS);
        assertTrue("Connection " + connection.getClientId() + " not retrieving all topics",
                c.await(200, TimeUnit.MILLISECONDS));
    }

    public Object answer(InvocationOnMock invocation) {
        Device device = (Device) invocation.getMock();
        return spy(new Node((String) invocation.getArguments()[0], device.thingUID, device.callback));
    }

    @Test
    public void parseHATree() throws MqttException, InterruptedException, ExecutionException, TimeoutException {
        MqttChannelTypeProvider channelTypeProvider = mock(MqttChannelTypeProvider.class);

        Map<String, AbstractComponent> haComponents = new HashMap<String, AbstractComponent>();

        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(4);
        DiscoverComponents discover = spy(
                new DiscoverComponents(ThingChannelConstants.testHomeAssistantThing, scheduler));

        discover.setDiscoveryTopic("homeassistant", ThingChannelConstants.testHomeAssistantThing.getId(), "node");
        discover.startDiscovery(connection, 500).thenRun(() -> {
            discover.applyDiscoveryResult(haComponents, channelTypeProvider);
        }).exceptionally(e -> {
            failure = e;
            return null;
        }).get(700, TimeUnit.MILLISECONDS);

        assertNull(failure);
        assertThat(haComponents.size(), is(1));

        // For the switch component we should have one channel group type and one channel type
        verify(channelTypeProvider, times(1)).addChannelGroupType(any(), any(String.class));
        verify(channelTypeProvider, times(1)).addChannelType(any(), any());

        // We expect a switch component with an OnOff channel with the initial value OFF:
        OnOffValue v = (OnOffValue) haComponents.get("switch").channelTypes()
                .get(ComponentSwitch.switchChannelID).channelState.getValue();
        assertThat(v.getValue(), is(OnOffType.OFF));

        // Subscribe to channel states
        ChannelStateUpdateListener channelStateUpdateListener = mock(ChannelStateUpdateListener.class);
        DiscoverComponents.startComponents(haComponents, connection, channelStateUpdateListener).thenRun(() -> {
        }).exceptionally(e -> {
            failure = e;
            return null;
        }).get(1000, TimeUnit.MILLISECONDS);

        verify(channelStateUpdateListener, times(1)).updateChannelState(any(), any());

        v = (OnOffValue) haComponents.get("switch").channelTypes().get(ComponentSwitch.switchChannelID).channelState
                .getValue();
        assertThat(v.getValue(), is(OnOffType.ON));

    }
}
