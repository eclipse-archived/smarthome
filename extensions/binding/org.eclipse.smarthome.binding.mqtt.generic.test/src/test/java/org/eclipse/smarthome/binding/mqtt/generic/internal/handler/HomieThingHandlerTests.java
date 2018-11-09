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

import static org.eclipse.smarthome.binding.mqtt.generic.internal.handler.ThingChannelConstants.testHomieThing;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.internal.MqttBindingConstants;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.Device;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.DeviceAttributes;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.DeviceAttributes.ReadyState;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.DeviceStatsAttributes;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.Node;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.NodeAttributes;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.Property;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.PropertyAttributes;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.PropertyAttributes.DataTypeEnum;
import org.eclipse.smarthome.binding.mqtt.generic.internal.generic.ChannelState;
import org.eclipse.smarthome.binding.mqtt.generic.internal.generic.ChannelStateHelper;
import org.eclipse.smarthome.binding.mqtt.generic.internal.generic.MqttChannelTypeProvider;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.AbstractMqttAttributeClass;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.SubscribeFieldToMQTTtopic;
import org.eclipse.smarthome.binding.mqtt.generic.internal.tools.ChildMap;
import org.eclipse.smarthome.binding.mqtt.generic.internal.tools.DelayedBatchProcessing;
import org.eclipse.smarthome.binding.mqtt.handler.AbstractBrokerHandler;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;

/**
 * Tests cases for {@link HomieThingHandler}.
 *
 * @author David Graeff - Initial contribution
 */
public class HomieThingHandlerTests {
    @Mock
    private ThingHandlerCallback callback;

    private Thing thing;

    @Mock
    private AbstractBrokerHandler bridgeHandler;

    @Mock
    private MqttBrokerConnection connection;

    @Mock
    private ScheduledExecutorService scheduler;

    @Mock
    private ScheduledFuture<?> scheduledFuture;

    private HomieThingHandler thingHandler;

    private final MqttChannelTypeProvider channelTypeProvider = new MqttChannelTypeProvider();

    private final String deviceID = ThingChannelConstants.testHomieThing.getId();
    private final String deviceTopic = "homie/" + deviceID;

    // A completed future is returned for a subscribe call to the attributes
    CompletableFuture<@Nullable Void> future = CompletableFuture.completedFuture(null);

    @Before
    public void setUp() {
        final ThingStatusInfo thingStatus = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);

        MockitoAnnotations.initMocks(this);

        final Configuration config = new Configuration();
        config.put("basetopic", "homie");
        config.put("deviceid", deviceID);

        thing = ThingBuilder.create(MqttBindingConstants.HOMIE300_MQTT_THING, testHomieThing.getId())
                .withConfiguration(config).build();
        thing.setStatusInfo(thingStatus);

        // Return the mocked connection object if the bridge handler is asked for it
        when(bridgeHandler.getConnectionAsync()).thenReturn(CompletableFuture.completedFuture(connection));

        doReturn(CompletableFuture.completedFuture(true)).when(connection).subscribe(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connection).unsubscribe(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connection).unsubscribeAll();
        doReturn(CompletableFuture.completedFuture(true)).when(connection).publish(any(), any(), anyInt(),
                anyBoolean());

        doReturn(false).when(scheduledFuture).isDone();
        doReturn(scheduledFuture).when(scheduler).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));

        final HomieThingHandler handler = new HomieThingHandler(thing, channelTypeProvider, 30, 5);
        thingHandler = spy(handler);
        thingHandler.setCallback(callback);
        final Device device = new Device(thing.getUID(), thingHandler, spy(new DeviceAttributes()),
                spy(new DeviceStatsAttributes()), new ChildMap<>(),
                Device.createDeviceStatisticsListener(thingHandler));
        thingHandler.setInternalObjects(spy(device),
                spy(new DelayedBatchProcessing<Object>(500, thingHandler, scheduler)));

        // Return the bridge handler if the thing handler asks for it
        doReturn(bridgeHandler).when(thingHandler).getBridgeHandler();

        // We are by default online
        doReturn(thingStatus).when(thingHandler).getBridgeStatus();

    }

    @Test
    public void initialize() {
        assertThat(thingHandler.device.isInitialized(), is(false));
        // // A completed future is returned for a subscribe call to the attributes
        doReturn(future).when(thingHandler.device.attributes).subscribeAndReceive(any(), any(), anyString(), any(),
                anyInt());
        doReturn(future).when(thingHandler.device.attributes).unsubscribe();
        // Prevent a call to accept, that would update our thing.
        doNothing().when(thingHandler).accept(any());
        // Pretend that a device state change arrived.
        thingHandler.device.attributes.state = ReadyState.ready;

        verify(callback, times(0)).statusUpdated(eq(thing), any());

        thingHandler.initialize();

        // Expect a call to the bridge status changed, the start, the propertiesChanged method
        verify(thingHandler).bridgeStatusChanged(any());
        verify(thingHandler).start(any());
        verify(thingHandler).readyStateChanged(any());
        verify(thingHandler.device.attributes).subscribeAndReceive(any(), any(),
                argThat(arg -> deviceTopic.equals(arg)), any(), anyInt());

        assertThat(thingHandler.device.isInitialized(), is(true));

        verify(callback).statusUpdated(eq(thing), argThat((arg) -> arg.getStatus().equals(ThingStatus.ONLINE)
                && arg.getStatusDetail().equals(ThingStatusDetail.NONE)));
    }

    @Test
    public void initializeGeneralTimeout() throws InterruptedException {
        // A non completed future is returned for a subscribe call to the attributes
        doReturn(future).when(thingHandler.device.attributes).subscribeAndReceive(any(), any(), anyString(), any(),
                anyInt());
        doReturn(future).when(thingHandler.device.attributes).unsubscribe();

        // Prevent a call to accept, that would update our thing.
        doNothing().when(thingHandler).accept(any());

        thingHandler.initialize();

        verify(callback).statusUpdated(eq(thing), argThat((arg) -> arg.getStatus().equals(ThingStatus.OFFLINE)
                && arg.getStatusDetail().equals(ThingStatusDetail.COMMUNICATION_ERROR)));
    }

    @Test
    public void initializeNoStateReceived() throws InterruptedException {
        // A completed future is returned for a subscribe call to the attributes
        doReturn(future).when(thingHandler.device.attributes).subscribeAndReceive(any(), any(), anyString(), any(),
                anyInt());
        doReturn(future).when(thingHandler.device.attributes).unsubscribe();

        // Prevent a call to accept, that would update our thing.
        doNothing().when(thingHandler).accept(any());

        thingHandler.initialize();
        assertThat(thingHandler.device.isInitialized(), is(true));

        verify(callback).statusUpdated(eq(thing), argThat((arg) -> arg.getStatus().equals(ThingStatus.OFFLINE)
                && arg.getStatusDetail().equals(ThingStatusDetail.GONE)));
    }

    @SuppressWarnings("null")
    @Test
    public void handleCommandRefresh() {
        // Create mocked homie device tree with one node and one read-only property
        Node node = thingHandler.device.createNode("node", spy(new NodeAttributes()));
        doReturn(future).when(node.attributes).subscribeAndReceive(any(), any(), anyString(), any(), anyInt());
        doReturn(future).when(node.attributes).unsubscribe();
        node.attributes.name = "testnode";

        Property property = node.createProperty("property", spy(new PropertyAttributes()));
        doReturn(future).when(property.attributes).subscribeAndReceive(any(), any(), anyString(), any(), anyInt());
        doReturn(future).when(property.attributes).unsubscribe();
        property.attributes.name = "testprop";
        property.attributes.datatype = DataTypeEnum.string_;
        property.attributes.settable = false;
        property.attributesReceived();
        node.properties.put(property.propertyID, property);
        thingHandler.device.nodes.put(node.nodeID, node);

        thingHandler.connection = connection;
        thingHandler.handleCommand(property.channelUID, RefreshType.REFRESH);

        verify(callback).stateUpdated(argThat(arg -> property.channelUID.equals(arg)),
                argThat(arg -> property.getChannelState().getValue().getValue().equals(arg)));
    }

    @SuppressWarnings("null")
    @Test
    public void handleCommandUpdate() {
        // Create mocked homie device tree with one node and one writable property
        Node node = thingHandler.device.createNode("node", spy(new NodeAttributes()));
        doReturn(future).when(node.attributes).subscribeAndReceive(any(), any(), anyString(), any(), anyInt());
        doReturn(future).when(node.attributes).unsubscribe();
        node.attributes.name = "testnode";

        Property property = node.createProperty("property", spy(new PropertyAttributes()));
        doReturn(future).when(property.attributes).subscribeAndReceive(any(), any(), anyString(), any(), anyInt());
        doReturn(future).when(property.attributes).unsubscribe();
        property.attributes.name = "testprop";
        property.attributes.datatype = DataTypeEnum.string_;
        property.attributes.settable = true;
        property.attributesReceived();
        node.properties.put(property.propertyID, property);
        thingHandler.device.nodes.put(node.nodeID, node);

        ChannelState channelState = property.getChannelState();
        assertNotNull(channelState);
        ChannelStateHelper.setConnection(channelState, connection);// Pretend we called start()
        thingHandler.connection = connection;

        StringType updateValue = new StringType("UPDATE");
        thingHandler.handleCommand(property.channelUID, updateValue);

        assertThat(property.getChannelState().getValue().getValue().toString(), is("UPDATE"));
        verify(connection, times(1)).publish(any(), any(), anyInt(), anyBoolean());

        // Check non writable property
        property.attributes.settable = false;
        property.attributesReceived();
        // Assign old value
        property.getChannelState().getValue().update("OLDVALUE");
        // Try to update with new value
        updateValue = new StringType("SOMETHINGNEW");
        thingHandler.handleCommand(property.channelUID, updateValue);
        // Expect old value and no MQTT publish
        assertThat(property.getChannelState().getValue().getValue().toString(), is("OLDVALUE"));
        verify(connection, times(1)).publish(any(), any(), anyInt(), anyBoolean());
    }

    public Object createSubscriberAnswer(InvocationOnMock invocation) {
        final AbstractMqttAttributeClass attributes = (AbstractMqttAttributeClass) invocation.getMock();
        final ScheduledExecutorService scheduler = (ScheduledExecutorService) invocation.getArguments()[0];
        final Field field = (Field) invocation.getArguments()[1];
        final String topic = (String) invocation.getArguments()[2];
        final boolean mandatory = (boolean) invocation.getArguments()[3];
        final SubscribeFieldToMQTTtopic s = spy(
                new SubscribeFieldToMQTTtopic(scheduler, field, attributes, topic, mandatory));
        doReturn(CompletableFuture.completedFuture(true)).when(s).subscribeAndReceive(any(), anyInt());
        return s;
    }

    public Property createSpyProperty(String propertyID, Node node) {
        // Create a property with the same ID and insert it instead
        Property property = spy(node.createProperty(propertyID, spy(new PropertyAttributes())));
        doAnswer(this::createSubscriberAnswer).when(property.attributes).createSubscriber(any(), any(), any(),
                anyBoolean());
        property.attributes.name = "testprop";
        property.attributes.datatype = DataTypeEnum.string_;

        return property;
    }

    public Node createSpyNode(String propertyID, Device device) {
        // Create the node
        Node node = spy(device.createNode("node", spy(new NodeAttributes())));
        doReturn(future).when(node.attributes).subscribeAndReceive(any(), any(), anyString(), any(), anyInt());
        doReturn(future).when(node.attributes).unsubscribe();
        node.attributes.name = "testnode";
        node.attributes.properties = new String[] { "property" };
        doAnswer(this::createSubscriberAnswer).when(node.attributes).createSubscriber(any(), any(), any(),
                anyBoolean());

        // Intercept creating a property in the next call and inject a spy'ed property.
        doAnswer(i -> createSpyProperty("property", node)).when(node).createProperty(any());

        return node;
    }

    @Test
    public void propertiesChanged() throws InterruptedException, ExecutionException {
        thingHandler.device.initialize("homie", "device", new ArrayList<Channel>());
        thingHandler.connection = connection;

        // Create mocked homie device tree with one node and one property
        doAnswer(this::createSubscriberAnswer).when(thingHandler.device.attributes).createSubscriber(any(), any(),
                any(), anyBoolean());

        thingHandler.device.attributes.state = ReadyState.ready;
        thingHandler.device.attributes.name = "device";
        thingHandler.device.attributes.homie = "3.0";
        thingHandler.device.attributes.nodes = new String[] { "node" };

        // Intercept creating a node in initialize()->start() and inject a spy'ed node.
        doAnswer(i -> createSpyNode("node", thingHandler.device)).when(thingHandler.device).createNode(any());

        verify(thingHandler, times(0)).nodeAddedOrChanged(any());
        verify(thingHandler, times(0)).propertyAddedOrChanged(any());

        thingHandler.initialize();

        assertThat(thingHandler.device.isInitialized(), is(true));

        verify(thingHandler).propertyAddedOrChanged(any());
        verify(thingHandler).nodeAddedOrChanged(any());

        verify(thingHandler.device).subscribe(any(), any(), anyInt());
        verify(thingHandler.device).attributesReceived(any(), any(), anyInt());

        assertNotNull(thingHandler.device.nodes.get("node").properties.get("property"));

        assertTrue(thingHandler.delayedProcessing.isArmed());

        // Simulate waiting for the delayed processor
        thingHandler.delayedProcessing.forceProcessNow();

        // Called for the updated property + for the new channels
        verify(callback, atLeast(2)).thingUpdated(any());

        final List<@NonNull Channel> channels = thingHandler.getThing().getChannels();
        assertThat(channels.size(), is(1));
        assertThat(channels.get(0).getLabel(), is("testprop"));
        assertThat(channels.get(0).getKind(), is(ChannelKind.STATE));

        final Map<@NonNull String, @NonNull String> properties = thingHandler.getThing().getProperties();
        assertThat(properties.get(MqttBindingConstants.HOMIE_PROPERTY_VERSION), is("3.0"));
        assertThat(properties.size(), is(1));
    }

    @Test
    public void heartBeatInterval()
            throws InterruptedException, ExecutionException, NoSuchFieldException, SecurityException {
        thingHandler.device.initialize("homie", "device", new ArrayList<Channel>());
        thingHandler.connection = connection;

        // Inject spy'ed subscriber object
        doAnswer(this::createSubscriberAnswer).when(thingHandler.device.stats).createSubscriber(any(), any(), any(),
                anyBoolean());
        doAnswer(this::createSubscriberAnswer).when(thingHandler.device.attributes).createSubscriber(any(), any(),
                any(), anyBoolean());

        thingHandler.device.attributes.state = ReadyState.ready;
        thingHandler.device.attributes.name = "device";
        thingHandler.device.attributes.homie = "3.0";
        thingHandler.device.attributes.nodes = new String[] {};

        thingHandler.initialize();

        assertThat(thingHandler.device.isInitialized(), is(true));

        verify(thingHandler.device).attributesReceived(any(), any(), anyInt());
        verify(thingHandler.device.stats).subscribeAndReceive(any(), any(), anyString(), any(), anyInt());

        // Emulate a received value for the "interval" topic
        thingHandler.device.stats.fieldChanged(DeviceStatsAttributes.class.getDeclaredField("interval"), 60);

        verify(thingHandler).heartbeatIntervalChanged(anyInt());
        verify(callback).thingUpdated(any());
    }
}
