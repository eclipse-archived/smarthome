/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.mqtt.reconnect;

import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;

/**
 * Implement this class and be notified of a lost broker connection.
 *
 * @author David Graeff
 */
public abstract class AbstractReconnectPolicy {
    protected MqttBrokerConnection brokerConnection;

    /**
     * Will be called by {@see MqttBrokerConnection.setReconnectPolicy()}.
     *
     * @param brokerConnection The broker connection
     */
    void setBrokerConnection(MqttBrokerConnection brokerConnection) {
        this.brokerConnection = brokerConnection;
    }

    public abstract void lostConnection();

    public abstract void connectionEstablished();
}
