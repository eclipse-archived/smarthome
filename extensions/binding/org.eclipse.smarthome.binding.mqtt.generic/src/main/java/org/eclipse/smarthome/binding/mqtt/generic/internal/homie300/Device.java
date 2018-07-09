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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.handler.HomieThingHandler;
import org.eclipse.smarthome.binding.mqtt.generic.internal.homie300.DeviceAttributes.ReadyState;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.FieldChanged;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.MqttTopicClassMapper;
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
public class Device implements Subscribable {
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

    public Node createNewNode(String nodeID) {
        return new Node(nodeID, thingUID, callback);
    }

    /**
     * Subscribe to all device attributes and device statistics. Parse the nodes
     * and subscribe to all node attributes. Parse node properties. This will not subscribe
     * to properties though. Call {@link #startChannels(MqttBrokerConnection)} subsequently.
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
     * @throws MqttException
     */
    public CompletableFuture<Void> startChannels(MqttBrokerConnection connection, HomieThingHandler handler)
            throws MqttException {
        List<CompletableFuture<Boolean>> futures = new ArrayList<CompletableFuture<Boolean>>();
        for (Property p : collectAllProperties()) {
            futures.add(p.startChannel(connection, handler));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
    }

    /**
     * Get a homie property (which translates to a ESH channel).
     *
     * @param channelUID
     * @return
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
    public void unsubscribe(MqttTopicClassMapper topicMapper) {
        topicMapper.unsubscribeAll();
        collectAllProperties().forEach(p -> p.stopChannel());
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
     * @return Is initializing
     */
    public boolean isInitializing() {
        return initializing;
    }

}
