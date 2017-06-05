/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.bluetooth.le;

/**
 * Bluetooth LE scan settings.
 *
 * Settings are passed to startScan to define the parameters for the scan.
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class ScanSettings {
    public static final int CALLBACK_TYPE_ON_UPDATE = 0;
    public static final int CALLBACK_TYPE_ON_FOUND = 1;
    public static final int CALLBACK_TYPE_ON_LOST = 2;

    public static final int CALLBACK_TYPE_ALL_MATCHES = 0;
    public static final int CALLBACK_TYPE_FIRST_MATCH = 1;
    public static final int CALLBACK_TYPE_MATCH_LOST = 2;

    public static final int SCAN_MODE_LOW_POWER = 0;

    public static final int SCAN_RESULT_TYPE_FULL = 0;

    private int callbackType;
    private int scanMode;
    private int scanResultType;
    private long reportDelayNanos;

    private ScanSettings(int scanMode, int callbackType, int scanResultType, long reportDelayNanos) {
        this.scanMode = scanMode;
        this.callbackType = callbackType;
        this.scanResultType = scanResultType;
        this.reportDelayNanos = reportDelayNanos;
    }

    /**
     *
     * @return
     */
    public int getCallbackType() {
        return callbackType;
    }

    /**
     * Returns report delay timestamp based on the device clock.
     *
     * @return
     */
    public long getReportDelayMillis() {
        return reportDelayNanos;
    }

    /**
     *
     * @return
     */
    public int getScanMode() {
        return scanMode;
    }

    /**
     *
     * @return
     */
    public int getScanResultType() {
        return scanResultType;
    }

    class Builder {
        private int scanMode = SCAN_MODE_LOW_POWER;
        private int callbackType = CALLBACK_TYPE_ON_UPDATE;
        private int scanResultType = SCAN_RESULT_TYPE_FULL;
        private long reportDelayNanos = 0;

        public Builder setCallbackType(int callbackType) {
            if (callbackType < CALLBACK_TYPE_ON_UPDATE || callbackType > CALLBACK_TYPE_ON_LOST) {
                throw new IllegalArgumentException("Invalid callback type: " + callbackType);
            }
            this.callbackType = callbackType;
            return this;
        }

        public ScanSettings build() {
            return new ScanSettings(scanMode, callbackType, scanResultType, reportDelayNanos);
        }
    }
}
