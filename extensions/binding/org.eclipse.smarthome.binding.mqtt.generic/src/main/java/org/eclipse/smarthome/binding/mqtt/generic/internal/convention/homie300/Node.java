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
package org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.mqtt.generic.internal.MqttBindingConstants;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.FieldChanged;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.MqttAttributeClass;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.MqttTopicClassMapper;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.SubtopicFieldObserver;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;

/**
 * Homie 3.x Node.
 *
 * A Homie Node contains Homie Properties ({@link Property}) but can also have attributes ({@link NodeAttributes}).
 * It corresponds to an ESH ChannelGroup.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class Node implements MqttAttributeClass {
    // Homie
    public final String nodeID;
    public NodeAttributes attributes = new NodeAttributes();
    public Map<Integer, NodeInstance> instances = new TreeMap<>();
    public Map<String, Property> properties = new TreeMap<>();
    // Runtime
    public final DeviceCallback callback;
    // ESH
    public final ThingUID thingUID;
    public final ChannelGroupTypeUID channelGroupTypeUID;

    public Node(String nodeID, ThingUID thingUID, DeviceCallback callback) {
        this.nodeID = nodeID;
        this.thingUID = thingUID;
        this.callback = callback;
        channelGroupTypeUID = new ChannelGroupTypeUID(MqttBindingConstants.BINDING_ID, thingUID.getId() + "_" + nodeID);
    }

    /**
     * Parse node properties. This will not subscribe to properties though. Call
     * {@link Device#startChannels(MqttBrokerConnection)} as soon as the returned future has
     * completed.
     */
    @Override
    public CompletableFuture<Void> subscribe(MqttTopicClassMapper topicMapper, int timeout) {
        Map<String, FieldChanged> m = new TreeMap<>();
        m.put("properties", new SubtopicFieldObserver<>(topicMapper, callback, properties,
                key -> new Property(this, key, thingUID), timeout));
        return topicMapper.subscribe("homie/" + thingUID.getId() + "/" + nodeID, attributes, m, timeout);
    }

    /**
     * Unsubscribe from all node attribute and also all property attributes within this node.
     *
     * @param connection A broker connection
     * @return Returns a future that completes as soon as all unsubscriptions have been performed.
     */
    @Override
    public CompletableFuture<Void> unsubscribe(MqttTopicClassMapper topicMapper) {
        List<CompletableFuture<Void>> futures = properties.values().stream().map(p -> p.unsubscribe(topicMapper))
                .collect(Collectors.toList());
        futures.add(topicMapper.unsubscribe(attributes));
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
    }
}
