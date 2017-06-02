/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.ble;

import org.eclipse.smarthome.binding.ble.notification.BleConnectionStatusNotification;
import org.eclipse.smarthome.binding.ble.notification.BleScanNotification;

/**
 * The {@link BleDeviceListener} class defines the a callback interface where devices are notified of updates to a
 * BLE device
 *
 * @author Chris Jackson - Initial contribution
 */
public interface BleDeviceListener {

    /**
     * Called when a scan record is received for the device
     *
     * @param scanNotification the {@link BleScanNotification} providing the scan packet information
     */
    void onScanRecordReceived(BleScanNotification scanNotification);

    /**
     * Called when the connection status changes
     *
     * @param connectionNotification the {@link BleConnectionStatusNotification} providing the updated connection
     *            information
     */
    void onConnectionStateChange(BleConnectionStatusNotification connectionNotification);

    /**
     * Called when a devices services and characteristics have been completely read
     */
    void onServicesDiscovered();

    /**
     * Called when a read request completes
     *
     * @param characteristic the {@link BleCharacteristic} that has completed the read request
     * @param status the {@link BleCompletionStatus} of the read request
     */
    void onCharacteristicReadComplete(BleCharacteristic characteristic, BleCompletionStatus status);

    /**
     * Called when a write request completes
     *
     * @param characteristic the {@link BleCharacteristic} that has completed the write request
     * @param status the {@link BleCompletionStatus} of the write request
     */
    void onCharacteristicWriteComplete(BleCharacteristic characteristic, BleCompletionStatus status);

    /**
     * Called when a characteristic value is received. Implementations should call this whenever a value
     * is received from the BLE device even if there is no change to the value.
     * 
     * @param characteristic the updated {@link BleCharacteristic}
     */
    void onCharacteristicUpdate(BleCharacteristic characteristic);
}
