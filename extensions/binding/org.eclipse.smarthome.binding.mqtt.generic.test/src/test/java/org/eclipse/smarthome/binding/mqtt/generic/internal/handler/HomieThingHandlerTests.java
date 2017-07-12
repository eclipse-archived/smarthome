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

import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.naming.ConfigurationException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.binding.mqtt.generic.internal.ChannelState;
import org.eclipse.smarthome.binding.mqtt.generic.internal.ChannelStateHelper;
import org.eclipse.smarthome.binding.mqtt.generic.internal.MqttChannelTypeProvider;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.DeviceAttributes.ReadyState;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.Node;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.Property;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.PropertyAttributes.DataTypeEnum;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.MqttTopicClassMapper;
import org.eclipse.smarthome.binding.mqtt.handler.AbstractBrokerHandler;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests cases for {@link HomieThingHandler}.
 *
 * @author David Graeff - Initial contribution
 */
public class HomieThingHandlerTests {
    @Mock
    private ThingHandlerCallback callback;

    @Mock
    private Thing thing;

    @Mock
    private AbstractBrokerHandler bridgeHandler;

    @Mock
    private MqttBrokerConnection connection;

    @Mock
    private MqttTopicClassMapper topicMapper;

    private HomieThingHandler thingHandler;

    private final MqttChannelTypeProvider channelTypeProvider = new MqttChannelTypeProvider();

    @Before
    public void setUp() throws ConfigurationException, MqttException {
        ThingStatusInfo thingStatus = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);

        MockitoAnnotations.initMocks(this);
        // Mock the thing: We need the thingUID and the bridgeUID
        when(thing.getUID()).thenReturn(testHomieThing);
        when(thing.getStatusInfo()).thenReturn(thingStatus);
        when(thing.getConfiguration()).thenReturn(new Configuration());

        // Return the mocked connection object if the bridge handler is asked for it
        when(bridgeHandler.getConnection()).thenReturn(connection);

        doReturn(CompletableFuture.completedFuture(true)).when(connection).subscribe(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connection).publish(any(), any());

        thingHandler = spy(new HomieThingHandler(thing, channelTypeProvider, 50, 10));
        thingHandler.setCallback(callback);
        // Return the bridge handler if the thing handler asks for it
        doReturn(bridgeHandler).when(thingHandler).getBridgeHandler();

        // We are by default online
        doReturn(thingStatus).when(thingHandler).getBridgeStatus();
    }

    @Test
    public void initialize() throws MqttException {
        CompletableFuture<Boolean> future = CompletableFuture.completedFuture(true);

        assertThat(thingHandler.device.isInitializing(), is(true));
        doReturn(topicMapper).when(thingHandler).createTopicMapper(any());
        // A completed future is returned for a subscribe call to the topic mapper
        doReturn(future).when(topicMapper).subscribe(any(), any(), any(), anyInt());
        doReturn(future).when(topicMapper).unsubscribe(any());
        doReturn(future).when(topicMapper).unsubscribeAll();
        // Prevent a call to propertiesChanged, that would update our thing.
        doNothing().when(thingHandler).propertiesChanged();
        // Pretend that a device state change arrived.
        thingHandler.device.attributes.state = ReadyState.ready;

        {
            ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
            verify(callback, times(0)).statusUpdated(eq(thing), statusInfoCaptor.capture());
        }

        final Logger logger = LoggerFactory.getLogger(HomieThingHandlerTests.class);
        thingHandler.initialize();

        // Expect a call to the bridge status changed, the start, the propertiesChanged method and to the topic mapper
        // subscribe
        verify(thingHandler).bridgeStatusChanged(any());
        verify(thingHandler).start(any());
        verify(thingHandler).readyStateChanged(any());
        verify(thingHandler).propertiesChanged();

        ArgumentCaptor<String> baseTopicCaptor = ArgumentCaptor.forClass(String.class);
        verify(topicMapper).subscribe(baseTopicCaptor.capture(), eq(thingHandler.device.attributes), any(), anyInt());
        assertThat(baseTopicCaptor.getValue(), is("homie/device123"));

        // Because the mocked topic mapper future is completed, initializing should be set to false now
        assertThat(thingHandler.device.isInitializing(), is(false));

        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback).statusUpdated(eq(thing), statusInfoCaptor.capture());
        assertThat(statusInfoCaptor.getValue().getStatus(), is(ThingStatus.ONLINE));
        assertThat(statusInfoCaptor.getValue().getStatusDetail(), is(ThingStatusDetail.NONE));
    }

    @Test
    public void initializeGeneralTimeout() throws MqttException, InterruptedException {
        CompletableFuture<Boolean> future = new CompletableFuture<Boolean>();
        doReturn(topicMapper).when(thingHandler).createTopicMapper(any());
        // A non completed future is returned for a subscribe call to the topic mapper
        doReturn(future).when(topicMapper).subscribe(any(), any(), any(), anyInt());
        doReturn(future).when(topicMapper).unsubscribe(any());
        doReturn(future).when(topicMapper).unsubscribeAll();

        // Prevent a call to propertiesChanged, that would update our thing.
        doNothing().when(thingHandler).propertiesChanged();

        thingHandler.initialize();

        Thread.sleep(100);

        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback).statusUpdated(eq(thing), statusInfoCaptor.capture());
        assertThat(statusInfoCaptor.getValue().getStatus(), is(ThingStatus.OFFLINE));
        assertThat(statusInfoCaptor.getValue().getStatusDetail(), is(ThingStatusDetail.COMMUNICATION_ERROR));
    }

    @Test
    public void initializeNoStateReceived() throws MqttException, InterruptedException {
        CompletableFuture<Boolean> future = CompletableFuture.completedFuture(true);
        doReturn(topicMapper).when(thingHandler).createTopicMapper(any());
        // A completed future is returned for a subscribe call to the topic mapper
        doReturn(future).when(topicMapper).subscribe(any(), any(), any(), anyInt());
        doReturn(future).when(topicMapper).unsubscribe(any());
        doReturn(future).when(topicMapper).unsubscribeAll();

        // Prevent a call to propertiesChanged, that would update our thing.
        doNothing().when(thingHandler).propertiesChanged();

        thingHandler.initialize();
        assertThat(thingHandler.device.isInitializing(), is(false));

        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback).statusUpdated(eq(thing), statusInfoCaptor.capture());
        assertThat(statusInfoCaptor.getValue().getStatus(), is(ThingStatus.OFFLINE));
        assertThat(statusInfoCaptor.getValue().getStatusDetail(), is(ThingStatusDetail.COMMUNICATION_ERROR));
    }

    @SuppressWarnings("null")
    @Test
    public void handleCommandRefresh() {
        // Create mocked homie device tree with one node and one read-only property
        Node node = new Node("node", thing.getUID(), thingHandler);
        node.attributes.name = "testnode";
        Property property = new Property(node, "property", thing.getUID());
        property.attributes.name = "testprop";
        property.attributes.datatype = DataTypeEnum.string_;
        property.attributes.settable = false;
        property.attributesReceived();
        node.properties.put(property.propertyID, property);
        thingHandler.device.nodes.put(node.nodeID, node);

        thingHandler.connection = connection;
        thingHandler.handleCommand(property.channelUID, RefreshType.REFRESH);

        ArgumentCaptor<ChannelUID> channelUIDCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(callback).stateUpdated(channelUIDCaptor.capture(), stateCaptor.capture());
        assertThat(channelUIDCaptor.getValue(), is(property.channelUID));
        assertThat(stateCaptor.getValue(), is(property.getChannelState().getValue().getValue()));
    }

    @SuppressWarnings("null")
    @Test
    public void handleCommandUpdate() throws MqttException {
        // Create mocked homie device tree with one node and one writable property
        Node node = new Node("node", thing.getUID(), thingHandler);
        node.attributes.name = "testnode";
        Property property = new Property(node, "property", thing.getUID());
        property.attributes.name = "testprop";
        property.attributes.datatype = DataTypeEnum.string_;
        property.attributes.settable = true;
        property.attributesReceived();
        node.properties.put(property.propertyID, property);
        thingHandler.device.nodes.put(node.nodeID, node);

        ChannelState channelState = property.getChannelState();
        assertTrue(channelState != null);
        ChannelStateHelper.setConnection(channelState, connection);// Pretend we called start()
        thingHandler.connection = connection;

        StringType updateValue = new StringType("UPDATE");
        thingHandler.handleCommand(property.channelUID, updateValue);

        assertThat(property.getChannelState().getValue().getValue().toString(), is("UPDATE"));
        verify(connection, times(1)).publish(any(), any());

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
        verify(connection, times(1)).publish(any(), any());
    }

    @Test
    public void propertiesChanged() throws MqttException {
        // Create mocked homie device tree with one node and one property
        Node node = new Node("node", thing.getUID(), thingHandler);
        node.attributes.name = "testnode";
        Property property = new Property(node, "property", thing.getUID());
        property.attributes.name = "testprop";
        property.attributes.datatype = DataTypeEnum.string_;
        property.attributesReceived();
        node.properties.put(property.propertyID, property);
        thingHandler.device.nodes.put(node.nodeID, node);

        CompletableFuture<Boolean> future = CompletableFuture.completedFuture(true);
        doReturn(topicMapper).when(thingHandler).createTopicMapper(any());
        doReturn(future).when(topicMapper).subscribe(any(), any(), any(), anyInt());
        doReturn(future).when(topicMapper).unsubscribe(any());
        doReturn(future).when(topicMapper).unsubscribeAll();

        thingHandler.connection = connection;
        thingHandler.initialize();

        verify(thingHandler).propertiesChanged();

        List<@NonNull Channel> channels = thingHandler.getThing().getChannels();
        assertThat(channels.size(), is(1));
        assertThat(channels.get(0).getChannelTypeUID(), is(property.channelTypeUID));
        assertThat(channels.get(0).getLabel(), is(property.attributes.name));
        assertThat(channels.get(0).getKind(), is(ChannelKind.STATE));
    }
}
