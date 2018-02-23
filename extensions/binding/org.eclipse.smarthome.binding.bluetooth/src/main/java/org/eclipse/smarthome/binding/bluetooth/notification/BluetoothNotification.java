/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.bluetooth.notification;

import org.eclipse.smarthome.binding.bluetooth.BluetoothAddress;

/**
 * The {@link BluetoothNotification} is the base class for Bluetooth device notifications
 *
 * @author Chris Jackson - Initial contribution
 */
public abstract class BluetoothNotification {
    protected BluetoothAddress address;

    /**
     * Returns the bluetooth address for this frame
     */
    public BluetoothAddress getAddress() {
        return address;
    }
}
