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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.internal.handler.HomieThingHandler;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.AbstractMqttAttributeClass;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.AbstractMqttAttributeClass.AttributeChanged;
import org.eclipse.smarthome.binding.mqtt.generic.internal.tools.ChildMap;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Homie 3.x Device. This is also the base class to subscribe to and parse a homie MQTT topic tree.
 * First use {@link #subscribe(AbstractMqttAttributeClass)} to subscribe to the device/nodes/properties tree.
 * If everything has been received and parsed, call {@link #startChannels(MqttBrokerConnection, HomieThingHandler)}
 * to also subscribe to the property values. Usage:
 *
 * <pre>
 * Device device(thingUID, callback);
 * device.subscribe(topicMapper,timeout).thenRun(()-> {
 *   System.out.println("All attributes received. Device tree ready");
 *   device.startChannels(connection, handler);
 * });
 * </pre>
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class Device implements AbstractMqttAttributeClass.AttributeChanged {
    private final Logger logger = LoggerFactory.getLogger(Device.class);
    // The device attributes, statistics and nodes of this device
    public final DeviceAttributes attributes;
    public final DeviceStatsAttributes stats;
    public final ChildMap<Node> nodes;

    // The corresponding ThingUID and callback of this device object
    public final ThingUID thingUID;
    private final DeviceCallback callback;

    // Unique identifier and topic
    private String topic = "";
    public String deviceID = "";
    private boolean initialized = false;
    private final AttributeChanged deviceStatisticsListener;

    /**
     * Creates a Homie Device structure. It consists of device attributes, device statistics and nodes.
     *
     * @param thingUID The thing UID
     * @param callback A callback, used to notify about new/removed nodes/properties and more.
     * @param attributes The device attributes object
     * @param stats The device statistics object
     */
    public Device(ThingUID thingUID, DeviceCallback callback, DeviceAttributes attributes,
            DeviceStatsAttributes stats) {
        this.thingUID = thingUID;
        this.callback = callback;
        this.attributes = attributes;
        this.stats = stats;
        this.nodes = new ChildMap<>();
        this.deviceStatisticsListener = createDeviceStatisticsListener(callback);
    }

    /**
     * Creates a Homie Device structure. It consists of device attributes, device statistics and nodes.
     *
     * @param thingUID The thing UID
     * @param callback A callback, used to notify about new/removed nodes/properties and more.
     * @param attributes The device attributes object
     * @param stats The device statistics object
     * @param nodes The nodes map
     * @param deviceStatisticsListener The AttributeChanged listener for the device statistics. Create the default one
     *            with {@link #createDeviceStatisticsListener(DeviceCallback)}.
     */
    public Device(ThingUID thingUID, DeviceCallback callback, DeviceAttributes attributes, DeviceStatsAttributes stats,
            ChildMap<Node> nodes, AttributeChanged deviceStatisticsListener) {
        this.thingUID = thingUID;
        this.callback = callback;
        this.attributes = attributes;
        this.stats = stats;
        this.nodes = nodes;
        this.deviceStatisticsListener = deviceStatisticsListener;
    }

    /**
     * Create the default listener for the device statistic attributes object.
     *
     * <p>
     * To be used for the
     * {@link #Device(ThingUID, DeviceCallback, DeviceAttributes, DeviceStatsAttributes, ChildMap, AttributeChanged)}
     * constructor.
     * </p>
     *
     * @param callback A device callback
     * @return The listener
     */
    public static AttributeChanged createDeviceStatisticsListener(DeviceCallback callback) {
        return (String name, Object value, MqttBrokerConnection connection, ScheduledExecutorService scheduler,
                boolean allMandatoryFieldsReceived) -> {
            if ("interval".equals(name)) {
                callback.heartbeatIntervalChanged((int) value);
            }
        };
    }

    /**
     * Subscribe to all device attributes and device statistics. Parse the nodes
     * and subscribe to all node attributes. Parse node properties. This will not subscribe
     * to properties though. If subscribing to all necessary topics worked {@link #isInitialized()} will return true.
     *
     * Call {@link #startChannels(MqttBrokerConnection)} subsequently.
     *
     * @param connection A broker connection
     * @param scheduler A scheduler to realize the timeout
     * @param timeout A timeout in milliseconds
     * @return A future that is complete as soon as all attributes, nodes and properties have been requested and have
     *         been subscribed to.
     */
    public CompletableFuture<@Nullable Void> subscribe(MqttBrokerConnection connection,
            ScheduledExecutorService scheduler, int timeout) {
        if (topic.isEmpty()) {
            throw new IllegalStateException("You must call initialize()!");
        }

        return attributes.subscribeAndReceive(connection, scheduler, topic, this, timeout)
                // On success, create all nodes and tell the handler about the ready state
                .thenCompose(b -> attributesReceived(connection, scheduler, timeout))
                // No matter if values have been received or not -> the subscriptions have been performed
                .whenComplete((r, e) -> {
                    initialized = true;
                });
    }

    public CompletableFuture<@Nullable Void> attributesReceived(MqttBrokerConnection connection,
            ScheduledExecutorService scheduler, int timeout) {
        callback.readyStateChanged(attributes.state);
        // Subscribe to statistics attributes
        stats.subscribeAndReceive(connection, scheduler, topic + "/$stats", deviceStatisticsListener, timeout)
                .exceptionally(e -> {
                    logger.warn("Did not receive all required device statistics attributes!");
                    // Default heartbeat interval assumed
                    callback.heartbeatIntervalChanged(stats.interval);
                    return null;
                });
        return applyNodes(connection, scheduler, timeout);
    }

    /**
     * Subscribe to all property state topics. The handler will receive an update call for each
     * received value. Therefore the thing channels should have been created before.
     *
     * @param connection A broker connection
     * @param scheduler A scheduler to realize the timeout
     * @param timeout A timeout in milliseconds. Can be 0 to disable the timeout and let the future return earlier.
     * @param handler The Homie handler, that receives property (channel) updates.
     * @return A future that is complete as soon as all properties have subscribed to their state topics.
     */
    public CompletableFuture<@Nullable Void> startChannels(MqttBrokerConnection connection,
            ScheduledExecutorService scheduler, int timeout, HomieThingHandler handler) {
        if (!isInitialized() || deviceID.isEmpty()) {
            CompletableFuture<@Nullable Void> c = new CompletableFuture<>();
            c.completeExceptionally(new Exception("Homie Device Tree not inialized yet."));
            return c;
        }

        return CompletableFuture.allOf(nodes.stream().flatMap(node -> node.properties.stream())
                .map(p -> p.startChannel(connection, scheduler, timeout)).toArray(CompletableFuture[]::new));
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
    public CompletableFuture<@Nullable Void> stop() {
        return attributes.unsubscribe().thenCompose(b -> stats.unsubscribe()).thenCompose(
                b -> CompletableFuture.allOf(nodes.stream().map(n -> n.stop()).toArray(CompletableFuture[]::new)));
    }

    /**
     * Return all homie nodes on this device
     */
    public ChildMap<Node> nodes() {
        return nodes;
    }

    /**
     * @return Return true if this device is initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Restore Nodes and Properties from Thing channels after handler initalization.
     *
     * @param channels
     */
    @SuppressWarnings({ "null", "unused" })
    public void initialize(String baseTopic, String deviceID, List<Channel> channels) {
        this.topic = baseTopic + "/" + deviceID;
        this.deviceID = deviceID;
        nodes.clear();
        for (Channel channel : channels) {
            final String nodeID = channel.getUID().getGroupId();
            final String propertyID = channel.getUID().getIdWithoutGroup();
            if (nodeID == null) {
                continue;
            }
            Node node = nodes.get(nodeID);
            if (node == null) {
                node = createNode(nodeID);
                node.nodeRestoredFromConfig();
                nodes.put(nodeID, node);
            }
            // Restores the properties attribute object via the channels configuration.
            Property property = node.createProperty(propertyID,
                    channel.getConfiguration().as(PropertyAttributes.class));
            property.createChannelFromAttribute();

            node.properties.put(propertyID, property);
        }
    }

    /**
     * Creates a new Homie Node, a child of this Homie Device.
     *
     * <p>
     * Implementation detail: Cannot be used for mocking or spying within tests.
     * </p>
     *
     * @param nodeID The node ID
     * @return A child node
     */
    public Node createNode(String nodeID) {
        return new Node(topic, nodeID, thingUID, callback, new NodeAttributes());
    }

    /**
     * Creates a new Homie Node, a child of this Homie Device.
     *
     * @param nodeID The node ID
     * @param attributes The node attributes object
     * @return A child node
     */
    public Node createNode(String nodeID, NodeAttributes attributes) {
        return new Node(topic, nodeID, thingUID, callback, attributes);
    }

    /**
     * <p>
     * The nodes of a device are determined by the device attribute "$nodes". If that attribute changes,
     * {@link #attributeChanged(CompletableFuture, String, Object, MqttBrokerConnection, ScheduledExecutorService)} is
     * called. The {@link #nodes} map will be synchronized and this method will be called for every removed node.
     * </p>
     *
     * <p>
     * This method will stop the node and will notify about the removed node all removed properties.
     * </p>
     *
     * @param node The removed node.
     */
    protected void notifyNodeRemoved(Node node) {
        node.stop();
        node.properties.stream().forEach(property -> node.notifyPropertyRemoved(property));
        callback.nodeRemoved(node);
    }

    CompletableFuture<@Nullable Void> applyNodes(MqttBrokerConnection connection, ScheduledExecutorService scheduler,
            int timeout) {
        return nodes.apply(attributes.nodes, node -> node.subscribe(connection, scheduler, timeout), this::createNode,
                this::notifyNodeRemoved).exceptionally(e -> {
                    logger.warn("Could not subscribe", e);
                    return null;
                });
    }

    @Override
    public void attributeChanged(String name, Object value, MqttBrokerConnection connection,
            ScheduledExecutorService scheduler, boolean allMandatoryFieldsReceived) {
        if (!initialized || !allMandatoryFieldsReceived) {
            return;
        }
        // Special case: Not all fields were known before
        if (!attributes.isComplete()) {
            attributesReceived(connection, scheduler, 500);
        } else {
            switch (name) {
                case "state": {
                    callback.readyStateChanged(attributes.state);
                    return;
                }
                case "nodes": {
                    applyNodes(connection, scheduler, 500);
                    return;
                }
            }
        }
    }
}
