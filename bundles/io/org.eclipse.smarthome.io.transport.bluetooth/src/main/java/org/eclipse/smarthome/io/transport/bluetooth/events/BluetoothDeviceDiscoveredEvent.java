/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.bluetooth.events;

import org.eclipse.smarthome.io.transport.bluetooth.BluetoothDevice;

/**
 *
 * @author Chris Jackson - Initial Implementation
 *
 */
public class BluetoothDeviceDiscoveredEvent extends BluetoothEvent {
    private BluetoothDevice device;

    public BluetoothDeviceDiscoveredEvent(BluetoothDevice device) {
        this.device = device;
    }

    public BluetoothDevice getDevice() {
        return device;
    }
}
