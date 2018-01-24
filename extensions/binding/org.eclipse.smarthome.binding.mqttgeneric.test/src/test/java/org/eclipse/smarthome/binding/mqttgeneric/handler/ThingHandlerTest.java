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
package org.eclipse.smarthome.binding.mqttgeneric.handler;

import static org.eclipse.smarthome.binding.mqttgeneric.handler.ThingChannelConstants.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import javax.naming.ConfigurationException;

import org.eclipse.smarthome.binding.mqttgeneric.internal.TextValue;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests cases for {@link ThingHandler}.
 *
 * @author David Graeff - Initial contribution
 */
public class ThingHandlerTest {
    @Mock
    private ThingHandlerCallback callback;

    @Mock
    private Thing thing;

    @Mock
    MqttBrokerConnectionHandler bridgeHandler;

    @Mock
    MqttBrokerConnection connection;

    MqttThingHandler subject;

    @Before
    public void setUp() throws ConfigurationException, MqttException {
        ThingStatusInfo thingStatus = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);

        MockitoAnnotations.initMocks(this);
        // Mock the thing: We need the thingUID and the bridgeUID
        when(thing.getUID()).thenReturn(testThing);
        when(thing.getBridgeUID()).thenReturn(bridgeThing);
        when(thing.getChannels()).thenReturn(thingChannelList);
        when(thing.getStatusInfo()).thenReturn(thingStatus);
        when(thing.getConfiguration()).thenReturn(new Configuration());

        // Return the mocked connection object if the bridge handler is asked for it
        when(bridgeHandler.getConnection()).thenReturn(connection);

        subject = spy(new MqttThingHandler(thing, null));
        subject.setCallback(callback);
        // Return the bridge handler if the thing handler asks for it
        doReturn(bridgeHandler).when(subject).getBridgeHandler();

        // We are by default online
        doReturn(true).when(connection).isConnected();
        doReturn(thingStatus).when(subject).getBridgeStatus();
    }

    @Test(expected = IllegalArgumentException.class)
    public void initializeWithUnknownThingUID() {
        List<Channel> l = new ArrayList<>();
        l.add(cb("test", "TextItemType", textConfiguration(), unknownChannel));
        when(thing.getChannels()).thenReturn(l);
        subject.initialize();
    }

    @Test
    public void initialize() throws MqttException {
        subject.initialize();
        verify(subject).bridgeStatusChanged(anyObject());

        ChannelConfig c = subject.channelDataByChannelUID.get(textChannelUID);
        assertThat(c.stateTopic, is("test/state"));
        assertThat(c.commandTopic, is("test/command"));

        verify(connection).addConsumer(eq(c));

        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback).statusUpdated(eq(thing), statusInfoCaptor.capture());
        assertThat(statusInfoCaptor.getValue().getStatus(), is(ThingStatus.ONLINE));
        assertThat(statusInfoCaptor.getValue().getStatusDetail(), is(ThingStatusDetail.NONE));

    }

    @Test
    public void handleCommandRefresh() {
        subject.initialize();
        ChannelConfig c = subject.channelDataByChannelUID.get(textChannelUID);
        // Mock value
        TextValue value = spy(new TextValue("TEST"));
        c.value = value;
        c.connection = connection;
        subject.connection = connection;

        subject.handleCommand(textChannelUID, RefreshType.REFRESH);
        verify(value).getValue();
    }

    @Test
    public void handleCommandUpdate() {
        subject.initialize();
        // Mock value
        TextValue value = spy(new TextValue("TEST"));
        ChannelConfig c = subject.channelDataByChannelUID.get(textChannelUID);
        c.value = value;
        c.connection = connection;
        subject.connection = connection;

        StringType updateValue = new StringType("UPDATE");
        subject.handleCommand(textChannelUID, updateValue);
        verify(value).update(eq(updateValue));
        assertThat(c.value.getValue().toString(), is("UPDATE"));
    }

    @Test
    public void processMessage() {
        subject.initialize();
        byte payload[] = "UPDATE".getBytes();
        ChannelConfig c = subject.channelDataByChannelUID.get(textChannelUID);
        c.stateTopic = "test/state";
        c.value = new TextValue("TEST");
        // Test process message
        c.processMessage("test/state", payload);

        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(callback).stateUpdated(eq(textChannelUID), stateCaptor.capture());
        assertThat(stateCaptor.getValue().toString(), is("UPDATE"));
        assertThat(c.value.getValue().toString(), is("UPDATE"));
    }

    @Test
    public void processMessageNotMatching() {
        subject.initialize();
        byte payload[] = "UPDATE".getBytes();
        ChannelConfig c = subject.channelDataByChannelUID.get(textChannelUID);
        c.stateTopic = "test/state";
        c.value = new TextValue("TEST");
        // Test process message
        c.processMessage("test/state2", payload);

        verify(callback, times(0)).stateUpdated(eq(textChannelUID), anyObject());
        assertThat(c.value.getValue().toString(), is("TEST"));
    }
}
