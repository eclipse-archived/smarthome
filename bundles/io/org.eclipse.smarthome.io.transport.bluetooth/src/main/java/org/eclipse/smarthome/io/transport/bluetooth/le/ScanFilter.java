/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.bluetooth.le;

import java.util.UUID;

/**
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class ScanFilter {
    /**
     *
     * @return
     */
    public String getDeviceAddress() {
        return null;
    }

    /**
     * Returns the filter set the device name field of Bluetooth advertisement data.
     *
     * @return
     */
    public String getDeviceName() {
        return null;
    }

    /**
     *
     * @return
     */
    public byte[] getManufacturerData() {
        return null;
    }

    /**
     *
     * @return
     */
    public byte[] getManufacturerDataMask() {
        return null;
    }

    /**
     * Returns the manufacturer id.
     *
     * @return
     */
    public int getManufacturerId() {
        return -1;
    }

    /**
     *
     * @return
     */
    public byte[] getServiceData() {
        return null;
    }

    /**
     *
     * @return
     */
    public byte[] getServiceDataMask() {
        return null;
    }

    /**
     *
     * @return
     */
    public UUID getServiceDataUuid() {
        return null;
    }

    /**
     *
     * @return
     */
    public UUID getServiceUuid() {
        return null;

    }

    /**
     * Returns the filter set on the service uuid.
     *
     * @return
     */
    public UUID getServiceUuidMask() {
        return null;
    }

    /**
     * Check if the scan filter matches a scanResult.
     *
     * @param scanResult
     * @return
     */
    public boolean matches(ScanResult scanResult) {
        return false;
    }
}
