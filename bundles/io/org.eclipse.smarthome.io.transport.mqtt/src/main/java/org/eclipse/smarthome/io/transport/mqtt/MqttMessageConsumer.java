/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.mqtt;

import org.eclipse.smarthome.core.events.EventPublisher;

/**
 * All message consumers which want to register as a message consumer to a MqttBrokerConnection should implement this
 * interface.
 *
 * @author Davy Vanherbergen
 */
public interface MqttMessageConsumer {

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

    /**
     * Set Topic to subscribe to. May contain + or # wildcards
     *
     * @param topic to subscribe to.
     */
    public void setTopic(String topic);

    /**
     * Set the event publisher to use when broadcasting received messages onto the smarthome event bus.
     *
     * @param eventPublisher
     */
    public void setEventPublisher(EventPublisher eventPublisher);

}
