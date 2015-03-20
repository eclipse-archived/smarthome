/**
w * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.mqtt;

/**
 * All MQTT connection observers which want to register as a connection observer to a MqttBrokerConnection should
 * implement this interface.
 *
 * @author Markus Rathgeb - Initial contribution and API
 */

public interface MqttConnectionObserver {

    /**
     * Inform the observer if the connection is active or disconnected.
     *
     * This callback is called after a observer is registered to a MqttBrokerConnection or if the connection state
     * changed.
     *
     * @param connected true if the connection is established, false if disconnected.
     */
    public void setConnected(boolean connected);

}
