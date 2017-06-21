/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.mqtt;

/**
 * Implement this interface to get notified of connection state changes.
 * Register this observer at {@see MqttBrokerConnection}.
 *
 * @author Markus Rathgeb - Initial contribution and API
 */
public interface MqttConnectionObserver {
    /**
     * Inform the observer if the connection is active or disconnected.
     *
     * @param connected true if the connection is established, false if disconnected.
     */
    public void setConnected(boolean connected);

}
