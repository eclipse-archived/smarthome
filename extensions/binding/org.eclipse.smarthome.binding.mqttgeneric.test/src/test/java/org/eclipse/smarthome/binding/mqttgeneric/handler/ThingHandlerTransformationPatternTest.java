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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.nio.file.InvalidPathException;

import javax.naming.ConfigurationException;

import org.eclipse.smarthome.binding.mqttgeneric.internal.TextValue;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

/**
 * Tests cases for {@link ThingHandler} to test the json transformation.
 *
 * @author David Graeff - Initial contribution
 */
public class ThingHandlerTransformationPatternTest {
    // JSonpathservice implementation. Unfortunately, we can't use the real class, because
    // it is not exported. An OSGI test is to heavy and unnecessary to just have those few lines
    // of code for a jsonPath transformation.
    TransformationService jsonPathService = (jsonPathExpression, source) -> {
        if (jsonPathExpression == null || source == null) {
            throw new TransformationException("the given parameters 'JSonPath' and 'source' must not be null");
        }
        try {
            Object transformationResult = JsonPath.read(source, jsonPathExpression);
            return (transformationResult != null) ? transformationResult.toString() : null;
        } catch (PathNotFoundException e1) {
            return null;
        } catch (InvalidPathException e2) {
            throw new TransformationException("An error occurred while transforming JSON expression.", e2);
        }
    };
    TransformationServiceProvider transformationServiceProvider = type -> jsonPathService;

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
        when(thing.getChannels()).thenReturn(thingChannelListWithJson);
        when(thing.getStatusInfo()).thenReturn(thingStatus);
        when(thing.getConfiguration()).thenReturn(new Configuration());

        // Return the mocked connection object if the bridge handler is asked for it
        when(bridgeHandler.getConnection()).thenReturn(connection);

        subject = spy(new MqttThingHandler(thing, transformationServiceProvider));
        subject.setCallback(callback);
        // Return the bridge handler if the thing handler asks for it
        doReturn(bridgeHandler).when(subject).getBridgeHandler();

        // We are by default online
        doReturn(true).when(connection).isConnected();
        doReturn(thingStatus).when(subject).getBridgeStatus();
    }

    @Test
    public void initialize() throws MqttException {
        when(thing.getChannels()).thenReturn(thingChannelListWithJson);

        subject.initialize();
        ChannelConfig c = subject.channelDataByChannelUID.get(textChannelUID);
        assertThat(c.transformationPattern, is(jsonPathPattern));
    }

    @Test
    public void processMessageWithJSONPath() {
        subject.initialize();
        ChannelConfig c = subject.channelDataByChannelUID.get(textChannelUID);
        byte payload[] = jsonPathJSON.getBytes();
        c.stateTopic = "test/state";
        c.transformationPattern = jsonPathPattern;
        c.value = new TextValue("TEST");
        // Test process message
        c.processMessage("test/state", payload);

        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(callback).stateUpdated(eq(textChannelUID), stateCaptor.capture());
        assertThat(stateCaptor.getValue().toString(), is("23.2"));
        assertThat(c.value.getValue().toString(), is("23.2"));
    }
}
