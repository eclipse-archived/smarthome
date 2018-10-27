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

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.automation.ModuleHandlerCallback;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseTriggerModuleHandler;
import org.eclipse.smarthome.automation.handler.TriggerHandlerCallback;
import org.eclipse.smarthome.automation.module.mqtt.internal.MQTTModuleConstants;
import org.eclipse.smarthome.binding.mqtt.discovery.MQTTTopicDiscoveryParticipant;
import org.eclipse.smarthome.binding.mqtt.discovery.MQTTTopicDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;

/**
 * Trigger a rule whenever a new value was received on the configured topic.
 *
 * @author David Graeff - Initial Contribution
 *
 */
@NonNullByDefault
public class PublishedMessageTriggerHandler extends BaseTriggerModuleHandler implements MQTTTopicDiscoveryParticipant {
    public static final String MODULE_TYPE_ID = "mqtt.topicTrigger";

    public static class Config {
        String topic = "";
        String message = "";
        @Nullable
        String mqttbroker;
    }

    private final MQTTTopicDiscoveryService discoveryService;
    private final Config config;

    public PublishedMessageTriggerHandler(Trigger module, MQTTTopicDiscoveryService discoveryService) {
        super(module);
        this.discoveryService = discoveryService;
        config = module.getConfiguration().as(Config.class);
    }

    @Override
    public void dispose() {
        discoveryService.unsubscribe(this);
        super.dispose();
    }

    @Override
    public void receivedMessage(ThingUID thingUID, MqttBrokerConnection connection, @NonNull String topic,
            byte @NonNull [] payload) {
        final String receivedPayload = new String(payload);
        if (config.message.isEmpty() || config.message.equals(receivedPayload)) {
            Map<String, Object> context = new TreeMap<>();
            context.put(MQTTModuleConstants.INOUT_BROKER_ID, connection);
            context.put(MQTTModuleConstants.INOUT_TOPIC_NAME, topic);
            context.put(MQTTModuleConstants.INOUT_TOPIC_VALUE, receivedPayload);
            ((TriggerHandlerCallback) callback).triggered(module, context);
        }
    }

    @Override
    public void topicVanished(ThingUID thingUID, MqttBrokerConnection connection, @NonNull String topic) {
        Map<String, Object> context = new TreeMap<>();
        context.put(MQTTModuleConstants.INOUT_BROKER_ID, connection);
        context.put(MQTTModuleConstants.INOUT_TOPIC_NAME, topic);
        context.put(MQTTModuleConstants.INOUT_TOPIC_VALUE, "");
        ((TriggerHandlerCallback) callback).triggered(module, context);
    }

    @Override
    public synchronized void setCallback(ModuleHandlerCallback callback) {
        super.setCallback(callback);
        final String topic = (String) module.getConfiguration().get(MQTTModuleConstants.CFG_TOPIC);
        discoveryService.subscribe(this, topic);
    }
}
