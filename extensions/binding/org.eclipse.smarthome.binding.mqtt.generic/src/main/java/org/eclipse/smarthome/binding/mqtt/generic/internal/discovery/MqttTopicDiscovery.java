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
package org.eclipse.smarthome.binding.mqtt.generic.internal.discovery;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.mqtt.handler.AbstractBrokerHandler;
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttMessageSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a base class for MQTT topic discoveries on all available broker connections. Naturally it only works for MQTT
 * conventions that requires messages to be retained.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class MqttTopicDiscovery implements RegistryChangeListener<Thing> {
    /**
     * Represents MQTT subscriptions for one specific {@link AbstractBrokerHandler} and adds itself to the
     * {@link MqttTopicDiscovery#observedBrokerHandlers} list. Start
     * subscribing to corresponding topics by calling {@link #start()} and stop all subscriptions by calling
     * {@link #stop()}.
     *
     * Internally this object will call {@link MqttTopicDiscovery#topicDiscoveredObserver} for each discovered topic.
     */
    private static class ObservedBrokerHandler implements MqttMessageSubscriber {
        final MqttBrokerConnection connection;
        final MqttTopicDiscovery parent;
        final ThingUID thing;

        ObservedBrokerHandler(MqttBrokerConnection connection, MqttTopicDiscovery parent, ThingUID thing) {
            this.connection = connection;
            this.parent = parent;
            this.thing = thing;
            parent.observedBrokerHandlers.put(thing, this);
        }

        @Override
        public void processMessage(String topic, byte[] payload) {
            parent.topicDiscoveredObserver.topicDiscovered(thing, connection, topic,
                    new String(payload, StandardCharsets.UTF_8));
        }

        public CompletableFuture<Boolean> start() {
            return connection.subscribe(parent.topic, this);
        }

        public void stop() {
            connection.unsubscribe(parent.topic, this);
        }
    }

    /**
     * Implement this interface to get notified of new popped up topics or vanished topics on the observed MQTT tree.
     */
    public interface TopicDiscovered {
        void topicDiscovered(ThingUID connectionBridge, MqttBrokerConnection connection, String topic,
                String topicValue);

        void topicVanished(ThingUID connectionBridge, MqttBrokerConnection connection, String topic, String topicValue);
    }

    private final Logger logger = LoggerFactory.getLogger(MqttTopicDiscovery.class);
    protected final ThingRegistry thingRegistry;
    protected final Map<ThingUID, ObservedBrokerHandler> observedBrokerHandlers = new HashMap<>();
    protected final TopicDiscovered topicDiscoveredObserver;
    protected final String topic;

    /**
     * Creates a topic discovery object.
     *
     * @param thingRegistry The frameworks Thing Registry. On each scan all things are enumerated to look for Bridges
     *            that have a handler that inherits from {@link AbstractBrokerHandler}.
     * @param topicDiscoveredObserver A callback to get notified of results.
     * @param topic A topic, most likely with a wildcard like this: "house/+/main-light" to match
     *            "house/room1/main-light", "house/room2/main-light" etc.
     */
    MqttTopicDiscovery(ThingRegistry thingRegistry, TopicDiscovered topicDiscoveredObserver, String topic) {
        this.thingRegistry = thingRegistry;
        this.topicDiscoveredObserver = topicDiscoveredObserver;
        this.topic = topic;
    }

    /**
     * Returns true if the given Thing has a handler that inherits from {@link AbstractBrokerHandler}.
     */
    protected static boolean isMQTTconnectionBridge(Thing thing) {
        return thing.getHandler() instanceof AbstractBrokerHandler;
    }

    /**
     * Perform a broker connection enumeration and subscribe to the destination topic on each connection.
     */
    protected void startScan() {
        thingRegistry.getAll().forEach(t -> added(t));
    }

    /**
     * Perform a broker connection enumeration and subscribe to the destination topic on each connection.
     * New connections will be handled automatically until {@link #stopBackgroundDiscovery()} is called.
     */
    protected void startBackgroundDiscovery() {
        thingRegistry.addRegistryChangeListener(this);
        thingRegistry.getAll().forEach(t -> added(t));
    }

    /**
     * Stops the background scanning on all new broker connections.
     */
    protected void stopBackgroundDiscovery() {
        thingRegistry.removeRegistryChangeListener(this);
        observedBrokerHandlers.forEach((c, m) -> m.stop());
        observedBrokerHandlers.clear();
    }

    /**
     * Add thing if it is a bridge and has a handler that inherits from {@link AbstractBrokerHandler}.
     */
    @Override
    public void added(Thing brokerHandlerBridge) {
        if (!isMQTTconnectionBridge(brokerHandlerBridge)
                || observedBrokerHandlers.containsKey(brokerHandlerBridge.getUID())) {
            return;
        }
        final AbstractBrokerHandler handler = (AbstractBrokerHandler) brokerHandlerBridge.getHandler();
        if (handler == null) {
            logger.trace("Found broker connection {}, but handler is not set!", brokerHandlerBridge);
            return;
        }

        final MqttBrokerConnection connection = handler.getConnection();
        if (connection == null) {
            return;
        }

        ObservedBrokerHandler o = new ObservedBrokerHandler(connection, this, brokerHandlerBridge.getUID());
        o.start().exceptionally(e -> {
            logger.warn("Failed to MQTT subscribe for {} on base topic {}", brokerHandlerBridge, topic);
            return false;
        }).thenRun(() -> {
            logger.trace("Found MQTT topic tree for {} on base topic {}", brokerHandlerBridge, topic);
        });
    }

    /**
     * Removes the thing from observed connections, if it exists in there, and stops any MQTT subscriptions.
     */
    @SuppressWarnings("null")
    @Override
    public void removed(Thing thing) {
        final ObservedBrokerHandler observedBrokerHandler = observedBrokerHandlers.remove(thing.getUID());
        if (observedBrokerHandler != null) {
            observedBrokerHandler.stop();
        }
    }

    @Override
    public void updated(Thing oldThing, Thing updatedThing) {
        if (!isMQTTconnectionBridge(oldThing)) {
            return;
        }
        removed(oldThing);
        added(updatedThing);
    }
}
