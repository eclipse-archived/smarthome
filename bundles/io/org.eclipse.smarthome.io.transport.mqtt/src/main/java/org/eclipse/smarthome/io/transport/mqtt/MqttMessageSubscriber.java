/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.mqtt;

/**
 * Implement this interface and register on the {@see MqttBrokerConnection} to get notified
 * of incoming Mqtt messages on the given topic.
 *
 * @author David Graeff
 */
public interface MqttMessageSubscriber {
    /**
     * Process a received MQTT message.
     *
     * @param topic The mqtt topic on which the message was received.
     * @param payload content of the message.
     */
    public void processMessage(String topic, byte[] payload);

    /**
     * @return topic to subscribe to. May contain + or # wildcards
     */
    public String getTopic();
}
