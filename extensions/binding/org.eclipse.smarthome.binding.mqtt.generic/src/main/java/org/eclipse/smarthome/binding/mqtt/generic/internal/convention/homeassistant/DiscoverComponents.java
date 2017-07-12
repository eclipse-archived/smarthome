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
package org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homeassistant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.internal.ChannelStateUpdateListener;
import org.eclipse.smarthome.binding.mqtt.generic.internal.MqttChannelTypeProvider;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttMessageSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for subscribing to the HomeAssistant MQTT components wildcard topic
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class DiscoverComponents implements MqttMessageSubscriber {
    private final Logger logger = LoggerFactory.getLogger(DiscoverComponents.class);
    private final Map<String, AbstractComponent> haComponentsDiscoverProcess = new HashMap<String, AbstractComponent>();
    private final ThingUID thingUID;
    protected @Nullable String configTopic;
    private final ScheduledExecutorService scheduler;
    private @Nullable ScheduledFuture<?> scheduledFuture;
    protected final CompletableFuture<Boolean> discoverFinishedFuture = new CompletableFuture<>();

    public DiscoverComponents(ThingUID thingUID, ScheduledExecutorService scheduler) {
        this.thingUID = thingUID;
        this.scheduler = scheduler;
    }

    @Override
    public void processMessage(String topic, byte[] payload) {
        if (!topic.endsWith("/config")) {
            return;
        }
        TopicToID topicParts = new TopicToID(topic);
        if (haComponentsDiscoverProcess.containsKey(topicParts.component)) {
            return;
        }
        logger.trace("Added thing {} component {}", topicParts.objectID, topicParts.component);
        AbstractComponent component = CFactory.createComponent(thingUID, topicParts.component, new String(payload));
        if (component != null) {
            haComponentsDiscoverProcess.put(topicParts.component, component);
        }
    }

    /**
     * Start a components discovery. You need to have set the wildcard MQTT topic before with
     * {@link #setDiscoveryTopic(String, String, String)}.
     *
     * @param connection A MQTT broker connection
     * @param discoverTime The time in milliseconds for the discovery to run
     * @return A future that completes normally after the given time in milliseconds or exceptionally on any error.
     */
    public CompletableFuture<Boolean> startDiscovery(MqttBrokerConnection connection, int discoverTime) {
        haComponentsDiscoverProcess.clear();
        String configTopic = this.configTopic;
        if (configTopic == null) {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("No configuration topic set"));
            return future;
        }

        // Subscribe to the wildcard topic and start receive MQTT retained topics
        connection.subscribe(configTopic, this).exceptionally(e -> {
            final ScheduledFuture<?> scheduledFuture = this.scheduledFuture;
            if (scheduledFuture != null) { // Cancel timeout
                scheduledFuture.cancel(false);
                this.scheduledFuture = null;
            }
            discoverFinishedFuture.completeExceptionally(e);
            return false;
        });

        // Set up a scheduled future that will stop the discovery after the given time
        this.scheduledFuture = scheduler.schedule(() -> {
            this.scheduledFuture = null;
            connection.unsubscribe(configTopic, this);
            discoverFinishedFuture.complete(true);
        }, discoverTime, TimeUnit.MILLISECONDS);
        return discoverFinishedFuture;
    }

    /**
     * Stops an ongoing discovery or do nothing if no discovery is running.
     *
     * @param connection A MQTT broker connection
     */
    public void stopDiscovery(MqttBrokerConnection connection) {
        String configTopic = this.configTopic;
        final ScheduledFuture<?> scheduledFuture = this.scheduledFuture;
        if (configTopic == null || scheduledFuture == null) {
            return;
        }
        scheduledFuture.cancel(false);
        connection.unsubscribe(configTopic, this);
    }

    /**
     * Clear discovery results.
     */
    public void clearResults() {
        haComponentsDiscoverProcess.clear();
    }

    /**
     * Clear the components of the given map and apply the newly discovered ones.
     *
     * Remove the old components from the channel type provider and apply the new components
     * and component channels to it.
     *
     * @param haComponents The destination map of HA MQTT components
     * @param channelTypeProvider A channel type provider
     * @return A list of discovered ESH channels
     */
    public List<Channel> applyDiscoveryResult(Map<String, AbstractComponent> haComponents,
            MqttChannelTypeProvider channelTypeProvider) {
        // Remove channel types of the current components discovery
        haComponents.values().stream().forEach(e -> {
            channelTypeProvider.removeChannelGroupType(e.groupTypeUID());
            for (CChannel entry : e.channelTypes().values()) {
                channelTypeProvider.removeChannelType(entry.channelTypeUID);
            }
        });
        haComponents.clear();
        haComponents.putAll(haComponentsDiscoverProcess);
        // Add channel types of the new components discovery
        List<Channel> channels = new ArrayList<>();
        haComponents.values().stream().forEach(e -> {
            channelTypeProvider.addChannelGroupType(e.groupTypeUID(), e.name());
            for (CChannel entry : e.channelTypes().values()) {
                channelTypeProvider.addChannelType(entry.channelTypeUID, entry.type);
                channels.add(entry.channel);
            }
        });
        return channels;
    }

    /**
     * Subscribe to all component channel state topics.
     *
     * @param haComponents HomeAssistant MQTT components
     * @param connection A MQTT broker connection
     * @param channelStateUpdateListener State update listener
     * @return
     */
    public static CompletableFuture<Boolean> startComponents(Map<String, AbstractComponent> haComponents,
            MqttBrokerConnection connection, ChannelStateUpdateListener channelStateUpdateListener) {
        return haComponents.values().stream().map(e -> e.start(connection, channelStateUpdateListener))
                .reduce(CompletableFuture.completedFuture(true), (a, v) -> a.thenCompose(b -> v));
    }

    /**
     * Setup the MQTT discovery wildcard topic.
     *
     * @param baseTopic The base topic. Usually "homeassistant".
     * @param objectID The object id (=device id)
     * @param nodeID The node id. Can be empty.
     */
    public void setDiscoveryTopic(String baseTopic, String objectID, @Nullable String nodeID) {
        if (nodeID == null || nodeID.isEmpty()) {
            configTopic = baseTopic + "/+/" + objectID + "/config";
        } else {
            configTopic = baseTopic + "/+/" + nodeID + "/" + objectID + "/config";
        }

    }

}