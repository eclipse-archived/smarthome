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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.internal.ChannelState;
import org.eclipse.smarthome.binding.mqtt.generic.internal.MqttChannelTypeProvider;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.Device;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.DeviceAttributes.ReadyState;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.DeviceCallback;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.DeviceStatsAttributes;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.Node;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.Property;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.MqttAttributeClass;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.MqttTopicClassMapper;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles Homie things.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HomieThingHandler extends AbstractMQTTThingHandler implements DeviceCallback {
    private final Logger logger = LoggerFactory.getLogger(HomieThingHandler.class);
    protected Device device;
    // The timeout per attribute field subscription
    private final int attributeReceiveTimeout;
    private @Nullable MqttTopicClassMapper topicMapper;

    /**
     * Create a new thing handler for homie discovered things. A channel type provider and a topic value receive timeout
     * must be provided.
     *
     * @param thing The thing of this handler
     * @param provider A channel type provider
     * @param homieReceiveTimeout Timeout for the entire tree parsing and subscription. In milliseconds.
     * @param attributeReceiveTimeout The timeout per attribute field subscription. In milliseconds.
     */
    public HomieThingHandler(Thing thing, MqttChannelTypeProvider provider, int subscribeTimeout,
            int attributeReceiveTimeout) {
        super(thing, provider, null, subscribeTimeout);
        this.attributeReceiveTimeout = attributeReceiveTimeout;
        device = new Device(this.thing.getUID(), this);
    }

    /**
     * Creates a {@link MqttTopicClassMapper} with the given connection.
     *
     * @param connection Broker connection
     * @return A topic mapper
     */
    protected MqttTopicClassMapper createTopicMapper(MqttBrokerConnection connection) {
        return new MqttTopicClassMapper(connection, scheduler);
    }

    @Override
    protected CompletableFuture<Void> start(MqttBrokerConnection connection) {
        // We have mostly retained messages for Homie. QoS 1 is required.
        connection.setRetain(true);
        connection.setQos(1);
        MqttTopicClassMapper topicMapper = createTopicMapper(connection);
        this.topicMapper = topicMapper;
        return device.subscribe(topicMapper, attributeReceiveTimeout)
                .thenRun(() -> readyStateChanged(device.attributes.state)).thenRun(this::propertiesChanged);
    }

    @Override
    protected void stop() {
        MqttTopicClassMapper topicMapper = this.topicMapper;
        if (topicMapper != null) {
            device.unsubscribe(topicMapper);
            this.topicMapper = null;
        }
    }

    @Override
    protected @Nullable ChannelState getChannelState(ChannelUID channelUID) {
        Property property = device.getProperty(channelUID);
        return property != null ? property.getChannelState() : null;
    }

    @Override
    public void readyStateChanged(ReadyState state) {
        switch (state) {
            case alert:
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_ERROR);
                break;
            case disconnected:
                updateStatus(ThingStatus.OFFLINE);
                break;
            case init:
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING);
                break;
            case lost:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                break;
            case ready:
                updateStatus(ThingStatus.ONLINE);
                break;
            case sleeping:
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.DUTY_CYCLE);
                break;
            case unknown:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Device did not publish a ready state");
                break;
            default:
                break;
        }
    }

    @Override
    public void statisticAttributesChanged(DeviceStatsAttributes stats) {
        Map<String, String> properties = new TreeMap<>();
        properties.put("interval", String.valueOf(stats.interval));
        updateProperties(properties);
    }

    @Override
    public void propertiesChanged() {
        if (device.isInitializing()) {
            return;
        }
        List<Channel> channels = new ArrayList<>();
        Collection<Node> nodes = device.nodes();
        for (Node node : nodes) {
            channelTypeProvider.addChannelGroupType(node.channelGroupTypeUID, node.nodeID);
            for (Property property : node.properties.values()) {
                ChannelState channelState = property.getChannelState();
                ChannelType channelType = property.getType();
                if (channelState == null || channelType == null) {
                    continue;
                }
                channelTypeProvider.addChannelType(property.channelTypeUID, channelType);
                channels.add(ChannelBuilder.create(property.channelUID, channelState.getItemType())
                        .withType(property.channelTypeUID).withKind(channelType.getKind())
                        .withLabel(property.attributes.name).build());
            }
        }

        updateThing(editThing().withChannels(channels).build());
        MqttBrokerConnection connection = this.connection;
        if (connection != null) {
            device.startChannels(connection, this).thenRun(() -> {
                logger.trace("Homie device {} fully attached", device.attributes.name);
            });
        }
    }

    @Override
    public void subNodeRemoved(MqttAttributeClass p) {
        if (p instanceof Node) {
            channelTypeProvider.removeChannelGroupType(((Node) p).channelGroupTypeUID);

        } else if (p instanceof Property) {
            channelTypeProvider.removeChannelType(((Property) p).channelTypeUID);

        }
    }

}
