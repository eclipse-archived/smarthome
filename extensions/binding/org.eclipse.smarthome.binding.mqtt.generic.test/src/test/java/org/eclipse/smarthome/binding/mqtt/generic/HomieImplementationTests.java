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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.Device;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.DeviceAttributes.ReadyState;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.DeviceCallback;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.Node;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.Property;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.PropertyAttributes.DataTypeEnum;
import org.eclipse.smarthome.binding.mqtt.generic.internal.handler.ThingChannelConstants;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.MqttTopicClassMapper;
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
 * A full implementation test, that starts the embedded MQTT broker and publishes a homie device tree.
 *
 * @author David Graeff - Initial contribution
 */
public class HomieImplementationTests extends JavaOSGiTest {
    final Logger logger = LoggerFactory.getLogger(HomieImplementationTests.class);
    private MqttService mqttService;
    private MqttBrokerConnection embeddedConnection;
    private MqttBrokerConnection connection;
    private int registeredTopics = 100;

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
                embeddedConnection.isSecure(), "homie");
        connection.setQos(1);
        connection.start().get(200, TimeUnit.MILLISECONDS);
        assertThat(connection.connectionState(), is(MqttConnectionState.CONNECTED));
        // If the connection state changes in between -> fail
        connection.addConnectionObserver(failIfChange);

        embeddedConnection.setRetain(true);
        embeddedConnection.setQos(1);

        // Add homie device topics
        final String deviceID = "homie/" + ThingChannelConstants.testHomieThing.getId();
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        futures.add(embeddedConnection.publish(deviceID + "/$homie", "3.0".getBytes()));
        futures.add(embeddedConnection.publish(deviceID + "/$name", "Name".getBytes()));
        futures.add(embeddedConnection.publish(deviceID + "/$state", "ready".getBytes()));
        futures.add(embeddedConnection.publish(deviceID + "/$nodes", "testnode".getBytes()));

        // Add homie node topics
        final String testNode = deviceID + "/testnode";
        futures.add(embeddedConnection.publish(testNode + "/$name", "Testnode".getBytes()));
        futures.add(embeddedConnection.publish(testNode + "/$type", "Type".getBytes()));
        futures.add(embeddedConnection.publish(testNode + "/$properties", "temperature".getBytes()));

        // Add homie property topics
        final String property = testNode + "/temperature";
        futures.add(embeddedConnection.publish(property + "/$name", "Testprop".getBytes()));
        futures.add(embeddedConnection.publish(property + "/$settable", "true".getBytes()));
        futures.add(embeddedConnection.publish(property + "/$unit", "°C".getBytes()));
        futures.add(embeddedConnection.publish(property + "/$datatype", "float".getBytes()));
        futures.add(embeddedConnection.publish(property + "/$format", "-100:100".getBytes()));

        registeredTopics = futures.size();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).get(200, TimeUnit.MILLISECONDS);
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
                embeddedConnection.isSecure(), "homie");
        connection.start().get(2000, TimeUnit.MILLISECONDS);
        connection.stop().get(2000, TimeUnit.MILLISECONDS);
        connection = new MqttBrokerConnection(embeddedConnection.getHost(), embeddedConnection.getPort(),
                embeddedConnection.isSecure(), "homie");
        connection.start().get(2000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void retrieveAllTopics() throws MqttException, InterruptedException, ExecutionException, TimeoutException {
        CountDownLatch c = new CountDownLatch(registeredTopics);
        connection.subscribe("homie/" + ThingChannelConstants.testHomieThing.getId() + "/#",
                (topic, payload) -> c.countDown()).get(200, TimeUnit.MILLISECONDS);
        assertTrue("Connection " + connection.getClientId() + " not retrieving all topics",
                c.await(200, TimeUnit.MILLISECONDS));
    }

    @Test
    public void retrieveAttribute() throws MqttException, InterruptedException, ExecutionException, TimeoutException {
        Semaphore semaphore = new Semaphore(1);
        semaphore.acquire();
        connection.subscribe("homie/" + ThingChannelConstants.testHomieThing.getId() + "/$homie",
                (topic, payload) -> semaphore.release()).get(200, TimeUnit.MILLISECONDS);

        assertTrue("Connection " + connection.getClientId() + " not retrieving the $homie topic",
                semaphore.tryAcquire(200, TimeUnit.MILLISECONDS));
    }

    public Object answer(InvocationOnMock invocation) {
        Device device = (Device) invocation.getMock();
        return spy(new Node((String) invocation.getArguments()[0], device.thingUID, device.callback));
    }

    @SuppressWarnings("null")
    @Test
    public void parseHomieTree() throws MqttException, InterruptedException, ExecutionException, TimeoutException {
        DeviceCallback callback = mock(DeviceCallback.class);
        Device device = spy(new Device(ThingChannelConstants.testHomieThing, callback));
        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(4);
        MqttTopicClassMapper topicMapper = new MqttTopicClassMapper(connection, scheduler);

        doAnswer(this::answer).when(device).createNewNode(any());
        device.subscribe(topicMapper, 200).get(500, TimeUnit.MILLISECONDS);

        assertThat(device.isInitializing(), is(false));
        verify(callback).readyStateChanged(eq(ReadyState.ready));

        assertThat(device.attributes.homie, is("3.0"));
        assertThat(device.attributes.name, is("Name"));
        assertThat(device.attributes.state, is(ReadyState.ready));

        // Expect 1 node
        verify(device, times(1)).createNewNode(any());

        Node node = device.nodes.get("testnode");
        verify(node).subscribe(any(), anyInt());
        assertThat(node.attributes.type, is("Type"));
        assertThat(node.attributes.name, is("Testnode"));

        Property property = node.properties.get("temperature");
        assertThat(property.attributes.settable, is(true));
        assertThat(property.attributes.name, is("Testprop"));
        assertThat(property.attributes.unit, is("°C"));
        assertThat(property.attributes.datatype, is(DataTypeEnum.float_));
        assertThat(property.attributes.format, is("-100:100"));
        assertThat(property.getType().getState().getMinimum().intValue(), is(-100));
        assertThat(property.getType().getState().getMaximum().intValue(), is(100));

        // One for node parsing, one for each nodes property parsing
        verify(callback, times(2)).propertiesChanged();
    }
}
