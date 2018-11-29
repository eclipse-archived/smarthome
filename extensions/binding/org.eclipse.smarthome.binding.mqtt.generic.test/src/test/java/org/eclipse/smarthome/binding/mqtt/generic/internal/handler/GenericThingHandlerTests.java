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

import static org.eclipse.smarthome.binding.mqtt.generic.internal.handler.ThingChannelConstants.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.binding.mqtt.generic.internal.generic.ChannelConfig;
import org.eclipse.smarthome.binding.mqtt.generic.internal.generic.ChannelConfigBuilder;
import org.eclipse.smarthome.binding.mqtt.generic.internal.generic.ChannelState;
import org.eclipse.smarthome.binding.mqtt.generic.internal.generic.MqttChannelStateDescriptionProvider;
import org.eclipse.smarthome.binding.mqtt.generic.internal.generic.TransformationServiceProvider;
import org.eclipse.smarthome.binding.mqtt.generic.internal.values.OnOffValue;
import org.eclipse.smarthome.binding.mqtt.generic.internal.values.TextValue;
import org.eclipse.smarthome.binding.mqtt.generic.internal.values.ValueFactory;
import org.eclipse.smarthome.binding.mqtt.handler.AbstractBrokerHandler;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests cases for {@link GenericThingHandler}.
 *
 * @author David Graeff - Initial contribution
 */
public class GenericThingHandlerTests {
    @Mock
    private ThingHandlerCallback callback;

    @Mock
    private Thing thing;

    @Mock
    private AbstractBrokerHandler bridgeHandler;

    @Mock
    private MqttBrokerConnection connection;

    private GenericThingHandler thingHandler;

    @Before
    public void setUp() {
        ThingStatusInfo thingStatus = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);

        MockitoAnnotations.initMocks(this);
        // Mock the thing: We need the thingUID and the bridgeUID
        when(thing.getUID()).thenReturn(testGenericThing);
        when(thing.getChannels()).thenReturn(thingChannelList);
        when(thing.getStatusInfo()).thenReturn(thingStatus);
        when(thing.getConfiguration()).thenReturn(new Configuration());

        // Return the mocked connection object if the bridge handler is asked for it
        when(bridgeHandler.getConnectionAsync()).thenReturn(CompletableFuture.completedFuture(connection));

        CompletableFuture<Void> voidFutureComplete = new CompletableFuture<Void>();
        voidFutureComplete.complete(null);
        doReturn(voidFutureComplete).when(connection).unsubscribeAll();
        doReturn(CompletableFuture.completedFuture(true)).when(connection).subscribe(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connection).unsubscribe(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connection).publish(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connection).publish(any(), any(), anyInt(),
                anyBoolean());

        thingHandler = spy(new GenericThingHandler(thing, mock(MqttChannelStateDescriptionProvider.class),
                mock(TransformationServiceProvider.class), 1500));
        thingHandler.setCallback(callback);

        // Return the bridge handler if the thing handler asks for it
        doReturn(bridgeHandler).when(thingHandler).getBridgeHandler();

        // The broker connection bridge is by default online
        doReturn(thingStatus).when(thingHandler).getBridgeStatus();
    }

    @Test(expected = IllegalArgumentException.class)
    public void initializeWithUnknownThingUID() {
        ChannelConfig config = textConfiguration().as(ChannelConfig.class);
        thingHandler.createChannelState(config, new ChannelUID(testGenericThing, "test"),
                ValueFactory.createValueState(config, unknownChannel.getId()));
    }

    @Test
    public void initialize() {
        thingHandler.initialize();
        verify(thingHandler).bridgeStatusChanged(any());
        verify(thingHandler).start(any());
        assertThat(thingHandler.connection, is(connection));

        ChannelState channelConfig = thingHandler.channelStateByChannelUID.get(textChannelUID);
        assertThat(channelConfig.getStateTopic(), is("test/state"));
        assertThat(channelConfig.getCommandTopic(), is("test/command"));

        verify(connection).subscribe(eq(channelConfig.getStateTopic()), eq(channelConfig));

        verify(callback).statusUpdated(eq(thing), argThat((arg) -> arg.getStatus().equals(ThingStatus.ONLINE)
                && arg.getStatusDetail().equals(ThingStatusDetail.NONE)));
    }

    @Test
    public void handleCommandRefresh() {
        ChannelState channelConfig = mock(ChannelState.class);
        doReturn(CompletableFuture.completedFuture(true)).when(channelConfig).start(any(), any(), anyInt());
        doReturn(CompletableFuture.completedFuture(true)).when(channelConfig).stop();
        doReturn(channelConfig).when(thingHandler).createChannelState(any(), any(), any());
        thingHandler.initialize();

        TextValue value = spy(new TextValue());
        doReturn(value).when(channelConfig).getValue();
        thingHandler.connection = connection;

        thingHandler.handleCommand(textChannelUID, RefreshType.REFRESH);
        verify(value).getValue();
    }

    @Test
    public void handleCommandUpdateString() {
        TextValue value = spy(new TextValue());
        ChannelState channelConfig = spy(
                new ChannelState(ChannelConfigBuilder.create("stateTopic", "commandTopic").build(), textChannelUID,
                        value, thingHandler));
        doReturn(channelConfig).when(thingHandler).createChannelState(any(), any(), any());
        thingHandler.initialize();
        thingHandler.connection = connection;

        StringType updateValue = new StringType("UPDATE");
        thingHandler.handleCommand(textChannelUID, updateValue);
        verify(value).update(eq(updateValue));
        assertThat(channelConfig.getValue().getValue().toString(), is("UPDATE"));
    }

    @Test
    public void handleCommandUpdateBoolean() {
        OnOffValue value = spy(new OnOffValue("ON", "OFF"));
        ChannelState channelConfig = spy(
                new ChannelState(ChannelConfigBuilder.create("stateTopic", "commandTopic").build(), textChannelUID,
                        value, thingHandler));
        doReturn(channelConfig).when(thingHandler).createChannelState(any(), any(), any());
        thingHandler.initialize();
        thingHandler.connection = connection;

        StringType updateValue = new StringType("ON");
        thingHandler.handleCommand(textChannelUID, updateValue);

        verify(value).update(eq(updateValue));
        assertThat(channelConfig.getValue().getValue(), is(OnOffType.ON));
    }

    @Test
    public void processMessage() {
        TextValue textValue = new TextValue();
        ChannelState channelConfig = spy(
                new ChannelState(ChannelConfigBuilder.create("test/state", "test/state/set").build(), textChannelUID,
                        textValue, thingHandler));
        doReturn(channelConfig).when(thingHandler).createChannelState(any(), any(), any());
        thingHandler.initialize();
        byte payload[] = "UPDATE".getBytes();
        // Test process message
        channelConfig.processMessage("test/state", payload);

        verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));

        verify(callback).stateUpdated(eq(textChannelUID), argThat(arg -> "UPDATE".equals(arg.toString())));
        assertThat(textValue.getValue().toString(), is("UPDATE"));
    }
}
