/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.bluetooth.bluez.le;

import java.util.List;

import org.eclipse.smarthome.io.transport.bluetooth.BluetoothDevice;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothEventListener;
import org.eclipse.smarthome.io.transport.bluetooth.bluez.BluezBluetoothAdapter;
import org.eclipse.smarthome.io.transport.bluetooth.events.BluetoothDeviceDiscoveredEvent;
import org.eclipse.smarthome.io.transport.bluetooth.events.BluetoothEvent;
import org.eclipse.smarthome.io.transport.bluetooth.le.BluetoothLeScanner;
import org.eclipse.smarthome.io.transport.bluetooth.le.ScanCallback;
import org.eclipse.smarthome.io.transport.bluetooth.le.ScanFilter;
import org.eclipse.smarthome.io.transport.bluetooth.le.ScanResult;
import org.eclipse.smarthome.io.transport.bluetooth.le.ScanSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of BluetoothLeScanner for BlueZ
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class BluezBluetoothLeScanner extends BluetoothLeScanner implements BluetoothEventListener {
    private static final Logger logger = LoggerFactory.getLogger(BluetoothLeScanner.class);

    private BluezBluetoothAdapter adapter;

    public BluezBluetoothLeScanner(BluezBluetoothAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * Start Bluetooth LE scan.
     *
     * @param filters
     * @param settings
     * @param callback
     */
    @Override
    public void startScan(List<ScanFilter> filters, ScanSettings settings, ScanCallback callback) {
        super.startScan(filters, settings, callback);

        if (adapter != null) {
            adapter.addEventListener(this);
            adapter.startDiscovery();
        }
    }

    /**
     * Stops an ongoing Bluetooth LE scan.
     *
     * @param callback
     */
    @Override
    public void stopScan(ScanCallback callback) {
        super.stopScan(callback);
        if (adapter != null) {
            adapter.cancelDiscovery();
            adapter.removeEventListener(this);
        }
    }

    @Override
    public void handleBluetoothEvent(BluetoothEvent event) {
        if (event instanceof BluetoothDeviceDiscoveredEvent) {
            BluetoothDeviceDiscoveredEvent discoveryEvent = (BluetoothDeviceDiscoveredEvent) event;

            if (filters != null && filters.size() != 0) {
                // TODO: Handle filters
            }

            BluetoothDevice device = discoveryEvent.getDevice();

            // Check if we support the GATT profile.
            // (This is not a reliable method so remove for now!)
            // boolean supportsGatt = false;
            // UUID[] uuids = device.getUuids();
            // for (UUID uuid : uuids) {
            // if (BluezBluetoothConstants.BLUEZ_PROFILE_GATT.toString().equals(uuid.toString())) {
            // supportsGatt = true;
            // break;
            // }
            // }

            // if (supportsGatt == false) {
            // logger.debug("BLE Scanner: Device found that doesn't support BLE {} {}", device.getAddress(),
            // device.getName());
            // return;
            // }

            int callbackType = ScanSettings.CALLBACK_TYPE_ALL_MATCHES;

            ScanResult result = new ScanResult(discoveryEvent.getDevice(), null, 0, 0);

            logger.debug("BLE Scanner: New BLE device {} {}", device.getAddress(), device.getName());
            callback.onScanResult(callbackType, result);
        }
    }
}
