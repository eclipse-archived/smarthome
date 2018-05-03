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
package org.eclipse.smarthome.io.transport.mqtt;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.io.transport.mqtt.internal.MqttBrokerConnectionServiceInstance;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service allows you to enumerate system-wide configured Mqtt broker connections. You do not need this service
 * if you want to manage own/local Mqtt broker connections. If you add a broker connection, it will be
 * available immediately for all MqttService users. A removed broker connection may still be in use by consuming
 * services.
 *
 * Added/removed connections are not permanent. If the service is shutdown/restored it will
 * contain the system-wide configured connections again (usually via text files).
 *
 * System broker connections are not configured via this service. See {@link MqttBrokerConnectionServiceInstance}
 * instead.
 *
 * @author David Graeff - Added/Removed observer interface, Add/Remove/Enumerate broker connections.
 * @author Davy Vanherbergen
 * @author Markus Rathgeb - Synchronize access to broker connections
 */
@Component(immediate = true, service = {
        MqttService.class }, configurationPid = "org.eclipse.smarthome.mqtt", property = {
                Constants.SERVICE_PID + "=org.eclipse.smarthome.mqtt" })
@NonNullByDefault
public class MqttService {
    private final Logger logger = LoggerFactory.getLogger(MqttService.class);
    private final Map<String, MqttBrokerConnection> brokerConnections = new ConcurrentHashMap<String, MqttBrokerConnection>();
    private final List<MqttServiceObserver> brokersObservers = new CopyOnWriteArrayList<>();

    /**
     * Add a listener to get notified of new/removed brokers.
     *
     * @param observer The observer
     */
    public void addBrokersListener(MqttServiceObserver observer) {
        brokersObservers.add(observer);
    }

    /**
     * Remove a listener and don't get notified of new/removed brokers anymore.
     *
     * @param observer The observer
     */
    public void removeBrokersListener(MqttServiceObserver observer) {
        brokersObservers.remove(observer);
    }

    /**
     * Return true if a broker listener has been added via addBrokersListener().
     */
    public boolean hasBrokerObservers() {
        return !brokersObservers.isEmpty();
    }

    /**
     * Lookup an broker connection by name.
     *
     * @param brokerName to look for.
     * @return existing connection or null
     */
    public @Nullable MqttBrokerConnection getBrokerConnection(String brokerName) {
        synchronized (brokerConnections) {
            return brokerConnections.get(brokerName);
        }
    }

    /**
     * Adds a broker connection to the service.
     * The broker connection state will not be altered (started/stopped).
     *
     * It is your responsibility to remove the broker connection again by calling
     * removeBrokerConnection(brokerID).
     *
     * @param brokerID The broker connection will be identified by this ID. The ID must be unique within the service.
     * @param connection The broker connection object
     * @return Return true if the connection could be added successfully, return false if there is already
     *         an existing connection with the same name.
     */
    public boolean addBrokerConnection(String brokerID, MqttBrokerConnection connection) {
        synchronized (brokerConnections) {
            if (brokerConnections.containsKey(brokerID)) {
                return false;
            }
            brokerConnections.put(brokerID, connection);
        }
        brokersObservers.forEach(o -> o.brokerAdded(brokerID, connection));
        return true;
    }

    /**
     * Add a broker by a configuration key-value map. You need to provide at least a "host".
     *
     * @param config The configuration instance.
     * @return Returns the created broker connection or null if there is already a connection with the same name.
     * @throws ConfigurationException Most likely your provided host is invalid.
     * @throws MqttException
     */
    public @Nullable MqttBrokerConnection addBrokerConnection(String brokerID, MqttBrokerConnectionConfig config)
            throws ConfigurationException, MqttException {
        MqttBrokerConnection connection;
        synchronized (brokerConnections) {
            if (brokerConnections.containsKey(brokerID)) {
                return null;
            }
            String host = config.host;
            if (StringUtils.isNotBlank(host) && host != null) {
                connection = new MqttBrokerConnection(host, config.port, config.secure, config.clientID);
                brokerConnections.put(brokerID, connection);
            } else {
                throw new ConfigurationException("host", "You need to provide a hostname/IP!");
            }
        }

        // Extract further configurations
        connection.setCredentials(config.username, config.password);
        if (config.keepAlive != null) {
            connection.setKeepAliveInterval(config.keepAlive.intValue());
        }

        connection.setQos(config.qos.intValue());
        connection.setRetain(config.retainMessages);
        if (config.lwtTopic != null) {
            String topic = config.lwtTopic;
            MqttWillAndTestament will = new MqttWillAndTestament(topic,
                    config.lwtMessage != null ? config.lwtMessage.getBytes() : null, config.lwtQos, config.lwtRetain);
            logger.debug("Setting last will: {}", will);
            connection.setLastWill(will);
        }

        brokersObservers.forEach(o -> o.brokerAdded(brokerID, connection));
        return connection;
    }

    /**
     * Remove a broker connection by name
     *
     * @param brokerName The broker ID
     * @return Returns the removed broker connection, or null if there was none with the given name.
     */
    public @Nullable MqttBrokerConnection removeBrokerConnection(String brokerID) {
        synchronized (brokerConnections) {
            final @Nullable MqttBrokerConnection connection = brokerConnections.remove(brokerID);
            if (connection != null) {
                brokersObservers.forEach(o -> o.brokerRemoved(brokerID, connection));
            }
            return connection;
        }
    }

    /**
     * Returns an unmodifiable map with all configured brokers of this service and the broker ID as keys.
     */
    public Map<String, MqttBrokerConnection> getAllBrokerConnections() {
        synchronized (brokerConnections) {
            return Collections.unmodifiableMap(brokerConnections);
        }
    }
}
