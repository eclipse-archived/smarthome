/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.ble.notification;

import org.eclipse.smarthome.binding.ble.BluetoothAddress;

/**
 * The {@link BleNotification} is the base class for BLE device notifications
 *
 * @author Chris Jackson - Initial contribution
 */
public abstract class BleNotification {
    protected BluetoothAddress address;

    /**
     * Returns the bluetooth address for this frame
     */
    public BluetoothAddress getAddress() {
        return address;
    }
}
