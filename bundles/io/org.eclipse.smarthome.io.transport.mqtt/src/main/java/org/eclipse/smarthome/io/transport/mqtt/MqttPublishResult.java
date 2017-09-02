/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.mqtt;

/**
 * Represents a publish result asynchronously provided by the {@link MqttPublishCallback}
 * after a call to {@link MqttBrokerConnection}.publish().
 *
 * @author David Graeff - Initial contribution
 */
public class MqttPublishResult {
    final int messageID;
    String topic;

    /**
     * Package local and only to be used by {@link MqttBrokerConnection}.publish() and tests.
     *
     * @param messageID
     * @param topic
     */
    MqttPublishResult(int messageID, String topic) {
        this.messageID = messageID;
        this.topic = topic;
    }

    /**
     * Return the topic, that the publish was targeted on.
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Return the messageID that was used to send the message to the broker.
     */
    public int getMessageID() {
        return messageID;
    }
}
