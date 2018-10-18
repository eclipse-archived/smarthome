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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.handler.BaseModuleHandler;
import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.eclipse.smarthome.binding.mqtt.discovery.MQTTTopicDiscoveryParticipant;
import org.eclipse.smarthome.binding.mqtt.discovery.MQTTTopicDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Observes a MQTT topic. Checks if the current topic value matches a configured one.
 *
 * @author David Graeff - Initial Contribution
 *
 */
@NonNullByDefault
public class PublishedMessageConditionHandler extends BaseModuleHandler<Condition>
        implements ConditionHandler, MQTTTopicDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(PublishedMessageConditionHandler.class);

    public static final String MODULE_TYPE_ID = "mqtt.PublishedMessage";
    public static final String CFG_TOPIC = "topic";
    public static final String CFG_PAYLOAD = "payload";

    private final MQTTTopicDiscoveryService discoveryService;
    private final String expectedPayload;
    private String receivedPayload = "";

    public PublishedMessageConditionHandler(Condition module, MQTTTopicDiscoveryService discoveryService) {
        super(module);
        this.discoveryService = discoveryService;
        expectedPayload = (String) module.getConfiguration().get(CFG_PAYLOAD);
        final String topic = (String) module.getConfiguration().get(CFG_TOPIC);
        discoveryService.subscribe(this, topic);
    }

    @Override
    public void dispose() {
        discoveryService.unsubscribe(this);
        super.dispose();
    }

    @Override
    public void receivedMessage(ThingUID thingUID, MqttBrokerConnection connection, @NonNull String topic,
            byte @NonNull [] payload) {
        receivedPayload = new String(payload);
        logger.trace("Payload changed: {}", receivedPayload);
    }

    @Override
    public void topicVanished(ThingUID thingUID, MqttBrokerConnection connection, @NonNull String topic) {
        receivedPayload = "";
    }

    @NonNullByDefault({}) // TODO Until the ConditionHandler interface is fixed
    @Override
    public boolean isSatisfied(Map<String, Object> context) {
        return expectedPayload.equals(receivedPayload);
    }

}
