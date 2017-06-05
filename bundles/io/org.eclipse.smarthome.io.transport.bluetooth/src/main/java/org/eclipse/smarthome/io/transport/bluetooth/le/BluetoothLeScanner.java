/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.bluetooth.le;

import java.util.List;

/**
 * This calls provides methods to scan for Bluetooth LE devices.
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public abstract class BluetoothLeScanner {
    protected List<ScanFilter> filters;
    protected ScanSettings settings;
    protected ScanCallback callback;

    /**
     * Flush any pending batch scan results stored in Bluetooth controller.
     *
     * @param callback
     */
    public void flushPendingScanResults(ScanCallback callback) {
    }

    /**
     * Start Bluetooth LE scan
     *
     * @param filters
     * @param settings
     * @param callback
     */
    public void startScan(List<ScanFilter> filters, ScanSettings settings, ScanCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback can not be null");
        }
        this.filters = filters;
        this.settings = settings;
        this.callback = callback;
    }

    /**
     * Start Bluetooth LE scan
     *
     * @param callback
     */
    public void startScan(ScanCallback callback) {
        startScan(null, null, callback);
    }

    /**
     * Stops a Bluetooth LE scan
     *
     * @param callback
     */
    public void stopScan(ScanCallback callback) {
        this.callback = null;
    }
}
