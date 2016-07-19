/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.mqtt;

/**
 * All message producers which want to register as a message producer to a MqttBrokerConnection should implement this
 * interface.
 *
 * @author Davy Vanherbergen
 */
public interface MqttMessageProducer {

    /**
     * Set the sender channel which the message producer should use to publish any message.
     *
     * @param channel Sender Channel which will be set by the MqttBrokerConnection.
     */
    public void setSenderChannel(MqttSenderChannel channel);

}
