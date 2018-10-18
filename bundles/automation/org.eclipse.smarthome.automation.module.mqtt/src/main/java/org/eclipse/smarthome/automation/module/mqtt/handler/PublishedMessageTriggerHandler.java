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
    public static final String CFG_TOPIC = "topic";

    private final MQTTTopicDiscoveryService discoveryService;

    public PublishedMessageTriggerHandler(Trigger module, MQTTTopicDiscoveryService discoveryService) {
        super(module);
        this.discoveryService = discoveryService;
    }

    @Override
    public void dispose() {
        discoveryService.unsubscribe(this);
        super.dispose();
    }

    @Override
    public void receivedMessage(ThingUID thingUID, MqttBrokerConnection connection, @NonNull String topic,
            byte @NonNull [] payload) {
        String receivedPayload = new String(payload);
        Map<String, Object> context = new TreeMap<>();
        context.put(MQTTModuleConstants.BROKER_TYPE, connection);
        context.put(MQTTModuleConstants.TOPIC_NAME_TYPE, topic);
        context.put(MQTTModuleConstants.TOPIC_VALUE_TYPE, receivedPayload);
        ((TriggerHandlerCallback) callback).triggered(module, context);
    }

    @Override
    public void topicVanished(ThingUID thingUID, MqttBrokerConnection connection, @NonNull String topic) {
        Map<String, Object> context = new TreeMap<>();
        context.put(MQTTModuleConstants.BROKER_TYPE, connection);
        context.put(MQTTModuleConstants.TOPIC_NAME_TYPE, topic);
        context.put(MQTTModuleConstants.TOPIC_VALUE_TYPE, "");
        ((TriggerHandlerCallback) callback).triggered(module, context);
    }

    @Override
    public synchronized void setCallback(ModuleHandlerCallback callback) {
        super.setCallback(callback);
        final String topic = (String) module.getConfiguration().get(CFG_TOPIC);
        discoveryService.subscribe(this, topic);
    }
}
