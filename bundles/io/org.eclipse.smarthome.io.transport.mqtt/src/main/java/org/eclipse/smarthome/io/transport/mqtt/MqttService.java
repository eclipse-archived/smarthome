/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.mqtt;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.io.transport.mqtt.internal.MqttBrokerConnection;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MQTT Service for creating new connections to MQTT brokers from the Smart Home configuration file and registering
 * message publishers and subscribers. This service is the main entry point for all bundles wanting to use the MQTT
 * transport.
 *
 * @author Davy Vanherbergen
 * @author Markus Rathgeb - Synchronize access to broker connections
 */
public class MqttService implements ManagedService {

    private final Logger logger = LoggerFactory.getLogger(MqttService.class);

    private final Map<String, MqttBrokerConnection> brokerConnections = new ConcurrentHashMap<String, MqttBrokerConnection>();

    private EventPublisher eventPublisher;

    @Override
    public void updated(Dictionary<String, ?> properties) throws ConfigurationException {

        // load broker configurations from configuration file
        if (properties == null || properties.isEmpty()) {
            return;
        }

        Enumeration<String> keys = properties.keys();
        while (keys.hasMoreElements()) {

            String key = keys.nextElement();

            if (key.equals("service.pid")) {
                // ignore the only non-broker property..
                continue;
            }

            String[] subkeys = key.split("\\.");
            if (subkeys.length != 2) {
                logger.debug("MQTT Broker property '{}' should have the format 'broker.propertykey'", key);
                continue;
            }

            String value = (String) properties.get(key);
            String name = subkeys[0].toLowerCase();
            String property = subkeys[1];

            if (StringUtils.isBlank(value)) {
                logger.trace("Property is empty: {}", key);
                continue;
            } else {
                logger.trace("Processing property: {} = {}", key, value);
            }

            final MqttBrokerConnection conn = getConnection(name);

            if (property.equals("url")) {
                conn.setUrl(value);
            } else if (property.equals("user")) {
                conn.setUser(value);
            } else if (property.equals("pwd")) {
                conn.setPassword(value);
            } else if (property.equals("qos")) {
                conn.setQos(Integer.parseInt(value));
            } else if (property.equals("retain")) {
                conn.setRetain(Boolean.parseBoolean(value));
            } else if (property.equals("async")) {
                conn.setAsync(Boolean.parseBoolean(value));
            } else if (property.equals("clientId")) {
                conn.setClientId(value);
            } else if (property.equals("lwt")) {
                MqttWillAndTestament will = MqttWillAndTestament.fromString(value);
                logger.debug("Setting last will: {}", will);
                conn.setLastWill(will);
            } else if (property.equals("keepAlive")) {
                conn.setKeepAliveInterval(Integer.parseInt(value));
            } else {
                logger.warn("Unrecognized property: {}", key);
            }
        }
        logger.info("MQTT Service initialization completed.");

        startAllBrokerConnections();
    }

    /**
     * Start service.
     */
    public void activate() {
        logger.debug("Starting MQTT Service...");
    }

    /**
     * Stop service.
     */
    public void deactivate() {
        logger.debug("Stopping MQTT Service...");
        stopAllBrokerConnections();
        logger.debug("MQTT Service stopped.");
    }

    private void startAllBrokerConnections() {
        /*
         * No need to synchronize the access to the broker connections.
         * We don't add or remove entries only use the existing ones and the map is a concurrent one.
         */
        for (final MqttBrokerConnection conn : brokerConnections.values()) {
            try {
                conn.start();
            } catch (final Exception e) {
                conn.connectionLost(e);
                logger.error("Error starting broker connection", e);
            }
        }
    }

    private void stopAllBrokerConnections() {
        /*
         * No need to synchronize the access to the broker connections.
         * We don't add or remove entries only use the existing ones and the map is a concurrent one.
         */
        for (final MqttBrokerConnection conn : brokerConnections.values()) {
            logger.info("Stopping broker connection '{}'", conn.getName());
            conn.close();
        }
    }

    /**
     * Lookup an broker connection by name.
     *
     * @param brokerName to look for.
     * @return existing connection or new one if it didn't exist yet.
     */
    private MqttBrokerConnection getConnection(String brokerName) {
        synchronized (brokerConnections) {
            MqttBrokerConnection conn = brokerConnections.get(brokerName.toLowerCase());
            if (conn == null) {
                conn = new MqttBrokerConnection(brokerName);
                brokerConnections.put(brokerName.toLowerCase(), conn);
            }
            return conn;
        }
    }

    /**
     * Register a new connection observer that could act on MQTT connection changes.
     *
     * @param brokerName Name of the broker that connection should be observed.
     * @param connectionObserver The connection observer that should be informed about connection changes.
     */
    public void registerConnectionObserver(String brokerName, MqttConnectionObserver connectionObserver) {
        getConnection(brokerName).addConnectionObserver(connectionObserver);
    }

    /**
     * Unregister an existing connection observer.
     *
     * @param brokerName Name of the broker that connection has been observed.
     * @param connectionObserver The connection observer that should not be informed anymore.
     */
    public void unregisterConnectionObserver(String brokerName, MqttConnectionObserver connectionObserver) {
        getConnection(brokerName).removeConnectionObserver(connectionObserver);
    }

    /**
     * Register a new message consumer which can process messages received on
     *
     * @param brokerName Name of the broker on which to listen for messages.
     * @param mqttMessageConsumer Consumer which will process any received message.
     */
    public void registerMessageConsumer(String brokerName, MqttMessageConsumer mqttMessageConsumer) {

        mqttMessageConsumer.setEventPublisher(eventPublisher);
        getConnection(brokerName).addConsumer(mqttMessageConsumer);
    }

    /**
     * Unregisters an existing message.
     *
     * @param mqttMessageConsumer Consumer which needs to be unregistered.
     */
    public void unregisterMessageConsumer(String brokerName, MqttMessageConsumer mqttMessageConsumer) {

        getConnection(brokerName).removeConsumer(mqttMessageConsumer);
    }

    public void registerMessageProducer(String brokerName, MqttMessageProducer commandPublisher) {

        getConnection(brokerName).addProducer(commandPublisher);
    }

    /**
     * Register a new message producer which can send messages to the given
     * broker.
     *
     * @param brokerName Name of the broker to which messages can be sent.
     * @param mqttMessageProducer Producer which generates the messages.
     */
    public void unregisterMessageProducer(String brokerName, MqttMessageProducer commandPublisher) {

        getConnection(brokerName).removeProducer(commandPublisher);
    }

    /**
     * Set the publisher to use for publishing SmartHome updates.
     *
     * @param eventPublisher EventPublisher
     */
    public void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Remove the publisher to use for publishing SmartHome updates.
     *
     * @param eventPublisher EventPublisher
     */
    public void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }
}
