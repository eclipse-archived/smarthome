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
package org.eclipse.smarthome.binding.bluetooth;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.eclipse.smarthome.binding.bluetooth.notification.BluetoothScanNotification;

/**
 * The {@link BluetoothDeviceListener} class defines the a callback interface where devices are notified of updates to a
 * BLE device
 *
 * @author Chris Jackson - Initial contribution
 * @author Kai Kreuzer - Added descriptor updates
 */
@NonNullByDefault
public interface BluetoothDeviceListener {

    /**
     * Called when a scan record is received for the device
     *
     * @param scanNotification the {@link BluetoothScanNotification} providing the scan packet information
     */
    void onScanRecordReceived(BluetoothScanNotification scanNotification);

    /**
     * Called when the connection status changes
     *
     * @param connectionNotification the {@link BluetoothConnectionStatusNotification} providing the updated connection
     *            information
     */
    void onConnectionStateChange(BluetoothConnectionStatusNotification connectionNotification);

    /**
     * Called when a devices services and characteristics have been completely read
     */
    void onServicesDiscovered();

    /**
     * Called when a read request completes
     *
     * @param characteristic the {@link BluetoothCharacteristic} that has completed the read request
     * @param status the {@link BluetoothCompletionStatus} of the read request
     */
    void onCharacteristicReadComplete(BluetoothCharacteristic characteristic, BluetoothCompletionStatus status);

    /**
     * Called when a write request completes
     *
     * @param characteristic the {@link BluetoothCharacteristic} that has completed the write request
     * @param status the {@link BluetoothCompletionStatus} of the write request
     */
    void onCharacteristicWriteComplete(BluetoothCharacteristic characteristic, BluetoothCompletionStatus status);

    /**
     * Called when a characteristic value is received. Implementations should call this whenever a value
     * is received from the BLE device even if there is no change to the value.
     *
     * @param characteristic the updated {@link BluetoothCharacteristic}
     */
    void onCharacteristicUpdate(BluetoothCharacteristic characteristic);

    /**
     * Called when a descriptor value is received. Implementations should call this whenever a value
     * is received from the BLE device even if there is no change to the value.
     *
     * @param characteristic the updated {@link BluetoothCharacteristic}
     */
    void onDescriptorUpdate(BluetoothDescriptor bluetoothDescriptor);
}
