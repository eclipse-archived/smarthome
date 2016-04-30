/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.bluetooth.le;

import org.eclipse.smarthome.io.transport.bluetooth.BluetoothDevice;

/**
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class ScanResult {
    BluetoothDevice device;

    public ScanResult(BluetoothDevice device, ScanRecord scanRecord, int rssi, long timestampNanos) {
        this.device = device;
    }

    /**
     * Returns a remote bluetooth device identified by the bluetooth device address.
     *
     * @return
     */
    public BluetoothDevice getDevice() {
        return device;
    }

    /**
     * Returns the received signal strength in dBm.
     *
     * @return
     */
    public int getRssi() {
        return 0;
    }

    /**
     * Returns the scan record, which is a combination of advertisement and scan response.
     *
     * @return
     */
    public ScanRecord getScanRecord() {
        return null;
    }

    /**
     * Returns timestamp since boot when the scan record was observed.
     *
     * @return
     */
    public long getTimestampNanos() {
        return 0;
    }

    @Override

    /**
     * Returns an integer hash code for this object.
     */
    public int hashCode() {
        return 0;
    }

    /**
     * Returns a string containing a concise, human-readable description of this object.
     */
    @Override
    public String toString() {
        return null;
    }
}
