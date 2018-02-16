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
import static org.mockito.MockitoAnnotations.initMocks;

import javax.naming.ConfigurationException;

import org.eclipse.smarthome.binding.mqttgeneric.internal.TextValue;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

/**
 * Tests cases for {@link ThingHandler} to test the json transformation.
 *
 * @author David Graeff - Initial contribution
 */
public class ThingHandlerTransformationPatternTest {

    @Mock
    private TransformationService jsonPathService;

    @Mock
    private TransformationServiceProvider transformationServiceProvider;

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
        initMocks(this);

        ThingStatusInfo thingStatus = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);

        // Mock the thing: We need the thingUID and the bridgeUID
        when(thing.getUID()).thenReturn(testThing);
        when(thing.getBridgeUID()).thenReturn(bridgeThing);
        when(thing.getChannels()).thenReturn(thingChannelListWithJson);
        when(thing.getStatusInfo()).thenReturn(thingStatus);
        when(thing.getConfiguration()).thenReturn(new Configuration());

        // Return the mocked connection object if the bridge handler is asked for it
        when(bridgeHandler.getConnection()).thenReturn(connection);

        thingHandler = spy(new MqttThingHandler(thing, transformationServiceProvider));
        when(transformationServiceProvider.getTransformationService(anyString())).thenReturn(jsonPathService);

        thingHandler.setCallback(callback);
        // Return the bridge handler if the thing handler asks for it
        doReturn(bridgeHandler).when(thingHandler).getBridgeHandler();

        // We are by default online
        doReturn(true).when(connection).isConnected();
        doReturn(thingStatus).when(thingHandler).getBridgeStatus();
    }

    @Test
    public void initialize() throws MqttException {
        when(thing.getChannels()).thenReturn(thingChannelListWithJson);

        thingHandler.initialize();
        ChannelConfig channelConfig = thingHandler.channelDataByChannelUID.get(textChannelUID);
        assertThat(channelConfig.transformationPattern, is(jsonPathPattern));
    }

    @Test
    public void processMessageWithJSONPath() throws Exception {
        when(jsonPathService.transform(jsonPathPattern, jsonPathJSON)).thenReturn("23.2");

        thingHandler.initialize();
        ChannelConfig channelConfig = thingHandler.channelDataByChannelUID.get(textChannelUID);
        byte payload[] = jsonPathJSON.getBytes();
        channelConfig.stateTopic = "test/state";
        channelConfig.transformationPattern = jsonPathPattern;
        channelConfig.value = new TextValue("TEST");
        // Test process message
        channelConfig.processMessage("test/state", payload);

        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(callback).stateUpdated(eq(textChannelUID), stateCaptor.capture());
        assertThat(stateCaptor.getValue().toString(), is("23.2"));
        assertThat(channelConfig.value.getValue().toString(), is("23.2"));
    }
}
