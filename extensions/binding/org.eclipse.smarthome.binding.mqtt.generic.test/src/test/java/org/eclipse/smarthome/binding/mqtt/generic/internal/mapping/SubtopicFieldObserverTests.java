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
package org.eclipse.smarthome.binding.mqtt.generic.internal.mapping;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.DeviceCallback;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.Node;
import org.eclipse.smarthome.binding.mqtt.generic.internal.handler.ThingChannelConstants;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;
import org.junit.Test;

/**
 * Tests cases for {@link SubtopicFieldObserver}.
 *
 * @author David Graeff - Initial contribution
 */
public class SubtopicFieldObserverTests {

    @Test
    public void testArrayToSubtopicCreateAndRemove() throws MqttException {
        // Create mocks
        MqttTopicClassMapper m = mock(MqttTopicClassMapper.class);
        DeviceCallback c = mock(DeviceCallback.class);

        doReturn(CompletableFuture.completedFuture(true)).when(m).unsubscribe(any());

        // A SubtopicFieldObserver always changes an external map. Create the map.
        Map<String, Node> nodes = new TreeMap<>();

        Function<String, Node> supplier = key -> {
            Node n = spy(new Node(key, ThingChannelConstants.testHomieThing, c));
            doReturn(CompletableFuture.completedFuture(true)).when(n).subscribe(any(), anyInt());
            return n;
        };

        SubtopicFieldObserver<Node> o = new SubtopicFieldObserver<Node>(m, c, nodes, supplier, 20);

        // Assign "abc,def" to the
        CompletableFuture<Boolean> future = new CompletableFuture<Boolean>();
        o.fieldChanged(future, "", "abc,def".split(","));

        assertThat(future.isDone(), is(true));
        assertThat(nodes.get("abc").nodeID, is("abc"));
        assertThat(nodes.get("def").nodeID, is("def"));

        Node soonToBeRemoved = nodes.get("def");
        o.fieldChanged(future, "", "abc,ghi".split(","));
        verify(c).subNodeRemoved(eq(soonToBeRemoved));
    }

}
