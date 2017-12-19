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

/**
 * All message producers which want to register as a message producer to a MqttBrokerConnection should implement this
 * interface.
 *
 * This interface is deprecated. Use the {@see MqttBrokerConnection.publish()} method instead.
 *
 * @deprecated
 * @author Davy Vanherbergen
 */
@Deprecated
public interface MqttMessageProducer {

    /**
     * Set the sender channel which the message producer should use to publish any message.
     *
     * @param channel Sender Channel which will be set by the MqttBrokerConnection.
     */
    public void setSenderChannel(MqttSenderChannel channel);

}
