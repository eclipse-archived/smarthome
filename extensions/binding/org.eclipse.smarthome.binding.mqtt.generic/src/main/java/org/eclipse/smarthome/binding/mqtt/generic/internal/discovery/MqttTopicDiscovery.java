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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.mqtt.handler.AbstractBrokerHandler;
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;
import org.eclipse.smarthome.io.transport.mqtt.MqttMessageSubscriber;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;
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
    private static class MessageSubscriber implements MqttMessageSubscriber {
        final MqttBrokerConnection connection;
        final MqttTopicDiscovery parent;
        final ThingUID thing;

        MessageSubscriber(MqttBrokerConnection connection, MqttTopicDiscovery parent, ThingUID thing)
                throws MqttException {
            this.connection = connection;
            this.parent = parent;
            this.thing = thing;
            connection.subscribe(parent.topic, this);
        }

        @Override
        public void processMessage(String topic, byte[] payload) {
            parent.topicDiscoveredObserver.topicDiscovered(thing, connection, topic,
                    new String(payload, StandardCharsets.UTF_8));
        }

        public void stop() {
            connection.unsubscribe(parent.topic, this);
        }
    }

    /**
     * Implement this interface to get notified of broker connections that support the given
     */
    public interface TopicDiscovered {
        void topicDiscovered(ThingUID connectionBridge, MqttBrokerConnection connection, String topic,
                String topicValue);

        void topicVanished(ThingUID connectionBridge, MqttBrokerConnection connection, String topic, String topicValue);
    }

    private final Logger logger = LoggerFactory.getLogger(MqttTopicDiscovery.class);
    protected final ThingRegistry thingRegistry;
    protected final Map<Thing, MessageSubscriber> observedConnections = new HashMap<>();
    protected final TopicDiscovered topicDiscoveredObserver;
    protected final String topic;

    /**
     * Creates a topic discovery object.
     *
     * @param mqttService The {@link MqttService} that allows to enumerate all known broker connections.
     * @param topicDiscoveredObserver A callback to get notified of results.
     * @param topic A topic, most likely with a wildcard like this: "house/+/main-light" to match
     *            "house/room1/main-light", "house/room2/main-light" etc.
     */
    MqttTopicDiscovery(ThingRegistry thingRegistry, TopicDiscovered topicDiscoveredObserver, String topic) {
        this.thingRegistry = thingRegistry;
        this.topicDiscoveredObserver = topicDiscoveredObserver;
        this.topic = topic;
    }

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
        observedConnections.forEach((c, m) -> m.stop());
        observedConnections.clear();
    }

    @Override
    public void added(Thing t) {
        if (!isMQTTconnectionBridge(t)) {
            return;
        }
        AbstractBrokerHandler h = (AbstractBrokerHandler) t.getHandler();
        if (h == null) {
            logger.trace("Found broker connection {}, but handler is not set!", t);
            return;
        }

        logger.trace("Found broker connection {}", t);
        try {
            MqttBrokerConnection connection = h.getConnection();
            if (observedConnections.containsKey(t) || connection == null) {
                return;
            }
            observedConnections.put(t, new MessageSubscriber(connection, this, t.getUID()));
        } catch (MqttException e) {
            logger.warn("Failed to subscribe to broker {} on topic {}", t, topic);
        }
    }

    @Override
    public void removed(Thing element) {
        MessageSubscriber messageSubscriber = observedConnections.remove(element);
        if (messageSubscriber != null) {
            messageSubscriber.stop();
        }
    }

    @Override
    public void updated(Thing oldElement, Thing element) {
        if (!isMQTTconnectionBridge(oldElement)) {
            return;
        }
        removed(oldElement);
        added(element);
    }
}
