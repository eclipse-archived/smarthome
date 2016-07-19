/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.mqtt.internal;

import java.util.TimerTask;

/**
 * Connection helper which can be executed periodically to try to reconnect to a broker if the connection was previously
 * lost.
 *
 * @author Davy Vanherbergen
 */
public class MqttBrokerConnectionHelper extends TimerTask {

    private MqttBrokerConnection connection;

    /**
     * Create new connection helper to help reconnect the given connection.
     *
     * @param connection to reconnect.
     */
    public MqttBrokerConnectionHelper(MqttBrokerConnection connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        try {
            connection.start();
        } catch (Exception e) {
            // reconnect failed,
            // maybe we will have more luck next time...
        }
    }

}
