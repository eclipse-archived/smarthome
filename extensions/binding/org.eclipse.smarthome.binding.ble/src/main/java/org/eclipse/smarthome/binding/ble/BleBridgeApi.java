/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.ble;

/**
 * The {@link BleBridgeApi} class defines the standard bridge API that must be implemented by Thing implemented in the
 * BLE host.
 * <p>
 * <b>Scanning</b>
 * The API assumes that the bridge is "always" scanning to enable beacons to be received.
 * The bridge must decide to enable and disable scanning as it needs. This design choice avoids interaction between
 * higher layers where a binding may want to enable scanning while another needs to disable scanning for a specific
 * function (e.g. to connect to a device). The bridge should disable scanning only for the period that is needed.
 *
 * @author Chris Jackson - Initial contribution
 */
public interface BleBridgeApi {

    /**
     * Starts an active scan on the Bluetooth interface.
     *
     * @return true if the scan was started
     */
    boolean scanStart();

    /**
     * Stops an active scan on the Bluetooth interface
     */
    void scanStop();

    /**
     * Gets the {@link BluetoothAddress} of the bridge
     *
     * @return the {@link BluetoothAddress} of the bridge
     */
    BluetoothAddress getAddress();

    /**
     * Gets the {@link BleDevice} given the {@link BluetoothAddress}.
     * A {@link BleDevice} will always be returned for a valid hardware address, even if this adapter has never seen
     * that device.
     *
     * @param address the {@link BluetoothAddress} to retrieve
     * @return the {@link BleDevice}
     */
    BleDevice getDevice(BluetoothAddress address);

}
