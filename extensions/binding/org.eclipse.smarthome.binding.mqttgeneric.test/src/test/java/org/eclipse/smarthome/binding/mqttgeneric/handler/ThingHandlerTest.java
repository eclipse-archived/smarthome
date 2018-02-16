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
    private MqttBrokerConnectionHandler bridgeHandler;

    @Mock
    private MqttBrokerConnection connection;

    private MqttThingHandler thingHandler;

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

        thingHandler = spy(new MqttThingHandler(thing, null));
        thingHandler.setCallback(callback);
        // Return the bridge handler if the thing handler asks for it
        doReturn(bridgeHandler).when(thingHandler).getBridgeHandler();

        // We are by default online
        doReturn(true).when(connection).isConnected();
        doReturn(thingStatus).when(thingHandler).getBridgeStatus();
    }

    @Test(expected = IllegalArgumentException.class)
    public void initializeWithUnknownThingUID() {
        List<Channel> l = new ArrayList<>();
        l.add(cb("test", "TextItemType", textConfiguration(), unknownChannel));
        when(thing.getChannels()).thenReturn(l);
        thingHandler.initialize();
    }

    @Test
    public void initialize() throws MqttException {
        thingHandler.initialize();
        verify(thingHandler).bridgeStatusChanged(any());

        ChannelConfig channelConfig = thingHandler.channelDataByChannelUID.get(textChannelUID);
        assertThat(channelConfig.stateTopic, is("test/state"));
        assertThat(channelConfig.commandTopic, is("test/command"));

        verify(connection).addConsumer(eq(channelConfig));

        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback).statusUpdated(eq(thing), statusInfoCaptor.capture());
        assertThat(statusInfoCaptor.getValue().getStatus(), is(ThingStatus.ONLINE));
        assertThat(statusInfoCaptor.getValue().getStatusDetail(), is(ThingStatusDetail.NONE));

    }

    @Test
    public void handleCommandRefresh() {
        thingHandler.initialize();
        ChannelConfig channelConfig = thingHandler.channelDataByChannelUID.get(textChannelUID);
        // Mock value
        TextValue value = spy(new TextValue("TEST"));
        channelConfig.value = value;
        channelConfig.connection = connection;
        thingHandler.connection = connection;

        thingHandler.handleCommand(textChannelUID, RefreshType.REFRESH);
        verify(value).getValue();
    }

    @Test
    public void handleCommandUpdate() {
        thingHandler.initialize();
        // Mock value
        TextValue value = spy(new TextValue("TEST"));
        ChannelConfig channelConfig = thingHandler.channelDataByChannelUID.get(textChannelUID);
        channelConfig.value = value;
        channelConfig.connection = connection;
        thingHandler.connection = connection;

        StringType updateValue = new StringType("UPDATE");
        thingHandler.handleCommand(textChannelUID, updateValue);
        verify(value).update(eq(updateValue));
        assertThat(channelConfig.value.getValue().toString(), is("UPDATE"));
    }

    @Test
    public void processMessage() {
        thingHandler.initialize();
        byte payload[] = "UPDATE".getBytes();
        ChannelConfig channelConfig = thingHandler.channelDataByChannelUID.get(textChannelUID);
        channelConfig.stateTopic = "test/state";
        channelConfig.value = new TextValue("TEST");
        // Test process message
        channelConfig.processMessage("test/state", payload);

        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(callback).stateUpdated(eq(textChannelUID), stateCaptor.capture());
        assertThat(stateCaptor.getValue().toString(), is("UPDATE"));
        assertThat(channelConfig.value.getValue().toString(), is("UPDATE"));
    }

    @Test
    public void processMessageNotMatching() {
        thingHandler.initialize();
        byte payload[] = "UPDATE".getBytes();
        ChannelConfig channelConfig = thingHandler.channelDataByChannelUID.get(textChannelUID);
        channelConfig.stateTopic = "test/state";
        channelConfig.value = new TextValue("TEST");
        // Test process message
        channelConfig.processMessage("test/state2", payload);

        verify(callback, times(0)).stateUpdated(eq(textChannelUID), any());
        assertThat(channelConfig.value.getValue().toString(), is("TEST"));
    }
}
