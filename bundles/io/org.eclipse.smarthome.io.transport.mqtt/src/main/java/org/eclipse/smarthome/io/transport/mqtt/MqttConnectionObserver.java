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
 * @author David Graeff - Rewritten
 * @author Markus Rathgeb - Initial contribution and API
 */
public interface MqttConnectionObserver {
    /**
     * Inform the observer if a connection could be established or if a connection
     * is lost. This will be issued in the context of the Mqtt client thread and
     * requires that the control is returned quickly to not stall the Mqtt thread.
     *
     * @param state The new connection state
     * @param error An exception object (might be a MqttException) with the reason why
     *            a connection failed.
     */
    public void connectionStateChanged(MqttConnectionState state, Throwable error);
}
