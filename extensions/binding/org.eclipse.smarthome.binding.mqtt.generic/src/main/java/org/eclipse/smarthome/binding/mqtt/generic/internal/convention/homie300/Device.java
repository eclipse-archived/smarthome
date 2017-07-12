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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.DeviceAttributes.ReadyState;
import org.eclipse.smarthome.binding.mqtt.generic.internal.handler.HomieThingHandler;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.FieldChanged;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.MqttAttributeClass;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.MqttTopicClassMapper;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.SubtopicFieldObserver;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;

/**
 * Homie 3.x Device. This is also the base class to subscribe to and parse a homie MQTT topic tree.
 * First use {@link #subscribe(MqttTopicClassMapper)} to subscribe to the device/nodes/properties tree.
 * If everything has been received and parsed, call {@link #startChannels(MqttBrokerConnection, HomieThingHandler)}
 * to also subscribe to the property values. Usage:
 *
 * <pre>
 * Device device(thingUID, callback);
 * device.subscribe(topicMapper).thenRun(()-> {
 *   System.out.println("All attributes received. Device tree ready");
 *   device.startChannels(connection, handler);
 * });
 * </pre>
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class Device implements MqttAttributeClass {
    public final DeviceAttributes attributes = new DeviceAttributes();
    public final DeviceStatsAttributes stats = new DeviceStatsAttributes();
    public final Map<String, Node> nodes = new TreeMap<>();
    public final ThingUID thingUID;
    public final DeviceCallback callback;
    private boolean initializing = true;

    public Device(ThingUID thingUID, DeviceCallback callback) {
        this.thingUID = thingUID;
        this.callback = callback;
    }

    /**
     * Creates a new Homie Node, a child of this Homie Device.
     *
     * @param nodeID The node ID
     * @return A child node
     */
    public Node createNewNode(String nodeID) {
        return new Node(nodeID, thingUID, callback);
    }

    /**
     * Subscribe to all device attributes and device statistics. Parse the nodes
     * and subscribe to all node attributes. Parse node properties. This will not subscribe
     * to properties though. If subscribing to all necessary topics worked, the Device
     * will change from "initializing=true" to "initializing=false".
     *
     * Call {@link #startChannels(MqttBrokerConnection)} subsequently.
     *
     * @return A future that is complete as soon as all attributes, nodes and properties have been requested and have
     *         been subscribed to.
     * @throws MqttException
     */
    @Override
    public CompletableFuture<Void> subscribe(MqttTopicClassMapper topicMapper, int timeout) {
        Map<String, FieldChanged> m = new TreeMap<>();

        m.put("state", new FieldChanged() {
            @Override
            public void fieldChanged(CompletableFuture<Boolean> future, String fieldname, Object value) {
                future.complete(true);
                ReadyState state = (ReadyState) value;
                callback.readyStateChanged(state);
            }
        });

        m.put("nodes", new SubtopicFieldObserver<>(topicMapper, callback, nodes, key -> createNewNode(key), timeout));
        return topicMapper.subscribe("homie/" + thingUID.getId(), attributes, m, timeout)
                .thenRun(() -> initializing = false);
    }

    /**
     * Subscribe to all property state topics. The handler will receive an update call for each
     * received value. Therefore the thing channels should have been created before.
     *
     * @param connection A broker connection
     * @param handler The Homie handler, that receives property (channel) updates.
     * @return A future that is complete as soon as all properties have subscribed to their state topics.
     */
    public CompletableFuture<Void> startChannels(MqttBrokerConnection connection, HomieThingHandler handler) {
        if (initializing) {
            CompletableFuture<Void> c = new CompletableFuture<Void>();
            c.completeExceptionally(new Exception("Homie Device Tree not inialized yet."));
            return c;
        }
        List<CompletableFuture<Boolean>> futures = collectAllProperties().stream()
                .map(p -> p.startChannel(connection, handler)).collect(Collectors.toList());
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
    }

    /**
     * Get a homie property (which translates to an ESH channel).
     *
     * @param channelUID The group ID corresponds to the Homie Node, the channel ID (without group ID) corresponds to
     *            the Nodes Property.
     * @return A Homie Property, addressed by the given ChannelUID
     */
    @SuppressWarnings({ "null", "unused" })
    public @Nullable Property getProperty(ChannelUID channelUID) {
        Node node = nodes.get(channelUID.getGroupId());
        if (node == null) {
            return null;
        }
        return node.properties.get(channelUID.getIdWithoutGroup());
    }

    /**
     * Unsubscribe from everything.
     */
    @Override
    public CompletableFuture<Void> unsubscribe(MqttTopicClassMapper topicMapper) {
        List<CompletableFuture<Boolean>> futures = collectAllProperties().stream().map(p -> p.stopChannel())
                .collect(Collectors.toList());
        return CompletableFuture.allOf(CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])),
                topicMapper.unsubscribeAll());
    }

    /**
     * Return all homie nodes on this device
     */
    public Collection<Node> nodes() {
        return nodes.values();
    }

    /**
     * Collects all properties and returns them.
     */
    public Collection<Property> collectAllProperties() {
        return nodes.values().stream().flatMap(node -> node.properties.values().stream()).collect(Collectors.toList());
    }

    /**
     * @return Return true if this device is still initializing
     */
    public boolean isInitializing() {
        return initializing;
    }

}
