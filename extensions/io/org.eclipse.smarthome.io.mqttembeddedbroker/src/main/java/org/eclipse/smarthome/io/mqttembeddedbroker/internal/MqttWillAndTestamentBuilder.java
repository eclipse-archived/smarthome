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
package org.eclipse.smarthome.io.mqttembeddedbroker.internal;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.io.transport.mqtt.MqttWillAndTestament;

/**
 * Builds a {@link MqttWillAndTestament}.
 *
 * @author David Graeff - Initial contribution
 */
public class MqttWillAndTestamentBuilder {
    String topic;
    byte[] message;
    int qos;
    boolean retain;

    public static MqttWillAndTestamentBuilder create(String topic) {
        if (StringUtils.isBlank(topic)) {
            throw new IllegalArgumentException("Topic must not be null");
        }
        MqttWillAndTestamentBuilder b = new MqttWillAndTestamentBuilder();
        b.topic = topic;
        return b;
    }

    public MqttWillAndTestamentBuilder qos(Integer qos) {
        if (qos != null) {
            if (qos < 0 || qos > 2) {
                throw new IllegalArgumentException("QOS value invalid");
            }
            this.qos = qos;
        }
        return this;
    }

    public MqttWillAndTestamentBuilder retain(Boolean retain) {
        if (retain != null) {
            this.retain = retain;
        }
        return this;
    }

    public MqttWillAndTestamentBuilder payload(String payload) {
        if (payload != null) {
            this.message = payload.getBytes();
        }
        return this;
    }

    public MqttWillAndTestament build() {
        try {
            return new MqttWillAndTestament(topic, message, qos, retain);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
