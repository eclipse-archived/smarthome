/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.io.transport.mqtt.internal;

import java.io.IOException;

import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;
import org.eclipse.smarthome.io.transport.mqtt.MqttSenderChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author David Gräff - Initial contribution
 */
@Deprecated
public class MqttSenderChannelImpl implements MqttSenderChannel {
    private final MqttBrokerConnection connection;
    private final Logger logger = LoggerFactory.getLogger(MqttSenderChannelImpl.class);

    public MqttSenderChannelImpl(MqttBrokerConnection connection) {
        this.connection = connection;
    }

    @Override
    public void publish(String topic, byte[] payload) throws IOException {
        if (!connection.isConnected()) {
            throw new IOException("No connection, can't publish messages");
        }

        try {
            connection.publish(topic, payload, null);
        } catch (MqttException e) {
            logger.error("Could not publish message to topic {}", topic, e);
        }
    }
}