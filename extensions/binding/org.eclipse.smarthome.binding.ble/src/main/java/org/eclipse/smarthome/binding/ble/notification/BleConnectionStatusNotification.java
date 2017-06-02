/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.ble.notification;

import org.eclipse.smarthome.binding.ble.BleDevice.ConnectionState;

/**
 * The {@link BleConnectionStatusNotification} provides a notification of a change in the device connection state.
 *
 * @author Chris Jackson - Initial contribution
 */
public class BleConnectionStatusNotification extends BleNotification {
    private ConnectionState connectionState;

    public BleConnectionStatusNotification(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    /**
     * Returns the connection state for this notification
     * 
     * @return the {@link ConnectionState}
     */
    public ConnectionState getConnectionState() {
        return connectionState;
    };
}
