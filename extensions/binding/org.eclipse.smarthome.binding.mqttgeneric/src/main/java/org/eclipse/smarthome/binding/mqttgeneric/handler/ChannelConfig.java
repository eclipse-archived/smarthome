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
package org.eclipse.smarthome.binding.mqttgeneric.handler;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.mqttgeneric.internal.AbstractMqttThingValue;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;
import org.eclipse.smarthome.io.transport.mqtt.MqttMessageSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains parsed channel configuration and runtime fields like the channel value.
 *
 * @author David Graeff - Initial contribution
 */
public class ChannelConfig implements MqttMessageSubscriber {
    private final Logger logger = LoggerFactory.getLogger(ChannelConfig.class);
    MqttBrokerConnection connection;

    String stateTopic;
    String commandTopic;
    String transformationPattern;

    BigDecimal min = BigDecimal.valueOf(0);
    BigDecimal max = BigDecimal.valueOf(100);
    BigDecimal step = BigDecimal.valueOf(1);
    Boolean isFloat = false;
    Boolean inverse = false;

    String on;
    String off;

    String transformationServiceName;

    // Runtime config
    AbstractMqttThingValue value;
    ChannelUID channelUID;
    TransformationServiceProvider transformationServiceProvider;
    private ChannelStateUpdateListener channelStateUpdateListener;

    public ChannelConfig() {
    }

    /**
     * Incoming message from the MqttBrokerConnection
     *
     * @param topic The topic. Should be the same as the field stateTopic.
     * @param payload The byte payload. Must be UTF8 encoded text.
     *            Some clients may decide to encode their own binary number or struct types.
     *            We do not and cannot support those here though.
     */
    @Override
    public void processMessage(String topic, byte[] payload) {
        if (!topic.equals(stateTopic)) {
            logger.trace("Received Mqtt data on {}. Does not match configured thing {}", topic, stateTopic);
            return;
        }

        String str = new String(payload, StandardCharsets.UTF_8);
        if (transformationPattern != null) {
            TransformationService service = transformationServiceProvider
                    .getTransformationService(transformationServiceName);
            if (service == null) {
                logger.warn("Transformation service '{}' not found", transformationServiceName);
                return;
            }
            try {
                str = service.transform(transformationPattern, str);
            } catch (TransformationException e) {
                logger.error("Error executing the transformation {}", str);
                return;
            }
        }

        try {
            channelStateUpdateListener.channelStateUpdated(channelUID, value.update(str));
        } catch (IllegalArgumentException e) {
            logger.warn("Incoming payload '{}' not supported by type '{}'", str, value.getClass().getSimpleName());
        }
    }

    /**
     * We register the whole object as a message consumer. The getTopic() method tells
     * the MqttBrokerConnection object in which topic we are interested in.
     */
    @Override
    public String getTopic() {
        return stateTopic;
    }

    void dispose() {
        if (stateTopic != null) {
            connection.removeConsumer(this);
        }
        connection = null;
        channelStateUpdateListener = null;
    }

    public void start(MqttBrokerConnection connection, ChannelStateUpdateListener channelStateUpdateListener)
            throws MqttException {
        this.connection = connection;
        this.channelStateUpdateListener = channelStateUpdateListener;
        if (StringUtils.isNotBlank(stateTopic)) {
            connection.addConsumer(this);
        }
    }
}