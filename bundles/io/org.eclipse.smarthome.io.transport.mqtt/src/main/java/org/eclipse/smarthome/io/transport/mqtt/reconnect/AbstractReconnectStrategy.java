/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.mqtt.reconnect;

import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionObserver;

/**
 * Implement this class to provide a strategy for (re)establishing a lost
 * broker connection.
 *
 * @author David Graeff - Initial contribution
 */
public abstract class AbstractReconnectStrategy {
    protected MqttBrokerConnection brokerConnection;

    /**
     * Will be called by {@see MqttBrokerConnection.setReconnectPolicy()}.
     *
     * @param brokerConnection The broker connection
     */
    public void setBrokerConnection(MqttBrokerConnection brokerConnection) {
        this.brokerConnection = brokerConnection;
    }

    /**
     * Return the brokerConnection object that this reconnect policy is assigned to.
     */
    public MqttBrokerConnection getBrokerConnection() {
        return brokerConnection;
    }

    /**
     * Return true if your implementation is trying to establish a connection, false otherwise.
     */
    public abstract boolean isReconnecting();

    /**
     * The {@link MqttConnectionObserver} will call this method if a broker connection has been lost
     * or couldn't be established. Your implementation should start trying to reestablish a connection.
     */
    public abstract void lostConnection();

    /**
     * The {@link MqttConnectionObserver} will call this method if a broker connection has been
     * successfully established. Your implementation should stop reconnection attempts and release
     * resources.
     */
    public abstract void connectionEstablished();

    /**
     * Start the reconnect strategy handling.
     */
    public abstract void start();

    /**
     * Stop the reconnect strategy handling.
     *
     * <p>
     * It must be possible to restart a reconnect strategy again after it has been stopped.
     */
    public abstract void stop();

}
