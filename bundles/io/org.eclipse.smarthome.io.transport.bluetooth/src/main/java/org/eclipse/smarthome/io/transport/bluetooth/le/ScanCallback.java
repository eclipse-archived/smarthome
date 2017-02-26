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
 * Bluetooth LE scan callback class
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class ScanCallback {
    public static final int SCAN_FAILED_ALREADY_STARTED = 1;
    public static final int SCAN_FAILED_APPLICATION_REGISTRATION_FAILED = 2;
    public static final int SCAN_FAILED_INTERNAL_ERROR = 3;
    public static final int SCAN_FAILED_FEATURE_UNSUPPORTED = 4;

    /**
     * Method when batch results are delivered.
     *
     * @param results
     */
    public void onBatchScanResults(List<ScanResult> results) {
    }

    /**
     * Method called when scan could not be started.
     *
     * @param errorCode
     */
    public void onScanFailed(int errorCode) {
    }

    /**
     * Method called when a BLE advertisement has been found.
     *
     * @param callbackType Defines how this callback was triggered.
     * @param result
     */
    public void onScanResult(int callbackType, ScanResult result) {
    }
}
