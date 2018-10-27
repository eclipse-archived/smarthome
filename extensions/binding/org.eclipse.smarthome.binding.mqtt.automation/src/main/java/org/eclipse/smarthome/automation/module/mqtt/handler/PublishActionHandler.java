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
package org.eclipse.smarthome.automation.module.mqtt.handler;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.handler.BaseModuleHandler;
import org.eclipse.smarthome.automation.module.mqtt.internal.MQTTModuleConstants;
import org.eclipse.smarthome.binding.mqtt.discovery.MQTTBrokerConnectionDiscoveryService;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Publishes a message to the configured broker connection or to the broker connection
 * that triggered the rule.
 *
 * @author David Graeff - Initial contribution
 *
 */
@NonNullByDefault
public class PublishActionHandler extends BaseModuleHandler<Action> implements ActionHandler {
    public static final String MODULE_TYPE_ID = "mqtt.publish";

    public static class Config {
        int timeout = 500;
        boolean retained = true;
        String topic = "";
        String message = "";
        @Nullable
        String mqttbroker;
    }

    private final Logger logger = LoggerFactory.getLogger(PublishActionHandler.class);
    private final MQTTBrokerConnectionDiscoveryService mqttService;
    // A weak reference cache for the broker connection
    private WeakReference<@Nullable MqttBrokerConnection> connection = new WeakReference<>(null);
    private final Config config;

    public PublishActionHandler(Action module, MQTTBrokerConnectionDiscoveryService mqttService) {
        super(module);
        this.mqttService = mqttService;
        config = module.getConfiguration().as(Config.class);
    }

    @SuppressWarnings({ "null", "unused" })
    @Override
    public @Nullable Map<String, Object> execute(Map<String, Object> context) {
        MqttBrokerConnection brokerConnection = null;
        final String mqttbroker = config.mqttbroker;
        if (mqttbroker != null) {
            // First try the weak-reference cache
            brokerConnection = connection.get();
            // Fetch from the discovery service if not in the cache
            if (brokerConnection == null) {
                brokerConnection = mqttService.getAllBrokerConnections().get(mqttbroker);
                connection = new WeakReference<>(brokerConnection);
            }
        }

        // If this action was triggered by the MQTT Trigger Handler, we know a broker connection already.
        if (brokerConnection == null) {
            brokerConnection = (MqttBrokerConnection) context.get(MQTTModuleConstants.INOUT_BROKER_CONNECTION);
        }

        if (brokerConnection == null) {
            logger.debug("No broker connection found for {}", config.topic);
            return null;
        }

        try {
            brokerConnection.publish(config.topic, config.message.getBytes(), 1, config.retained).get(config.timeout,
                    TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.warn("Failed to publish message {} to {}", config.message, config.topic, e);
        }
        return null;
    }
}
