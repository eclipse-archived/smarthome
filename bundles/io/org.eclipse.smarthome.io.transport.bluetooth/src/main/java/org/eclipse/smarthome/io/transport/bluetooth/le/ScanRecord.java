/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.bluetooth.le;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class ScanRecord {
    /**
     * Returns the advertising flags indicating the discoverable mode and capability of the device.
     *
     * @return
     */
    int getAdvertiseFlags() {
        return 0;
    }

    /**
     * Returns raw bytes of scan record.
     *
     * @return
     */
    byte[] getBytes() {
        return null;
    }

    /**
     * Returns the local name of the BLE device.
     *
     * @return
     */
    String getDeviceName() {
        return null;
    }

    /**
     * Returns a sparse array of manufacturer identifier and its corresponding manufacturer specific data.
     *
     * @return
     */
    // Map<byte[]> getManufacturerSpecificData() {
    // return null;
    // }

    /**
     * Returns the manufacturer specific data associated with the manufacturer id.
     *
     * @param manufacturerId
     * @return
     */
    byte[] getManufacturerSpecificData(int manufacturerId) {
        return null;
    }

    /**
     * Returns the service data byte array associated with the serviceUuid.
     *
     * @param serviceDataUuid
     * @return
     */
    byte[] getServiceData(UUID serviceDataUuid) {
        return null;
    }

    /**
     * Returns a map of service UUID and its corresponding service data.
     *
     * @return
     */
    Map<UUID, byte[]> getServiceData() {
        return null;
    }

    /**
     * Returns a list of service UUIDs within the advertisement that are used to identify the bluetooth GATT services.
     *
     * @return
     */
    List<UUID> getServiceUuids() {
        return null;
    }

    /**
     * Returns the transmission power level of the packet in dBm.
     *
     * @return
     */
    int getTxPowerLevel() {
        return 0;
    }
}
