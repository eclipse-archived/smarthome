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
package org.eclipse.smarthome.binding.mqtt.generic.internal.homie300;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.mqtt.generic.MqttBindingConstants;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.FieldChanged;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.MqttTopicClassMapper;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;

/**
 * Homie 3.x Node
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class Node implements Subscribable {
    // Homie
    public final String nodeID;
    public NodeAttributes attributes = new NodeAttributes();
    public Map<Integer, NodeInstance> instances = new TreeMap<>();
    public Map<String, Property> properties = new TreeMap<>();
    public Map<String, PropertyAttributes> propertyAttributes = new TreeMap<>();
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
     * Parse node properties. This will not subscribe
     * to properties though. Call {@link Device#startChannels(MqttBrokerConnection)} as soon as the returned future has
     * completed.
     *
     * @throws MqttException
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
     */
    @Override
    public void unsubscribe(MqttTopicClassMapper topicMapper) {
        topicMapper.unsubscribe(attributes);
        properties.values().forEach(p -> p.unsubscribe(topicMapper));
    }
}
