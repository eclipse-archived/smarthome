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
package org.eclipse.smarthome.io.transport.mqtt;

import java.io.IOException;

/**
 * Callback interface for sending a message to the MqttBrokerConnection.
 *
 * @deprecated
 * @author Davy Vanherbergen
 */
@Deprecated
public interface MqttSenderChannel {
    /**
     * Send a message to the MQTT broker. Please do not use this interface anymore, but call
     * {@see MqttBrokerConnection.publish()} instead. You will not get a notification if your
     * message arrived the broker.
     *
     * @deprecated
     * @param topic Topic to publish the message to.
     * @param message message payload.
     * @throws IOException if there is no broker connection
     */
    @Deprecated
    public void publish(String topic, byte[] message) throws IOException;
}
