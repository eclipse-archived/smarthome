/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.ble.notification;

/**
 * The {@link BleScanNotification} provides a notification of a received scan packet
 *
 * @author Chris Jackson - Initial contribution
 */
public class BleScanNotification extends BleNotification {
    /**
     * The receive signal strength for this beacon packet
     */
    private int rssi = Integer.MIN_VALUE;

    /**
     * The raw data
     */
    private int[] data = null;

    /**
     * The manufacturer specific data
     */
    private int[] manufacturerData = null;

    /**
     * The beacon type
     */
    private BleBeaconType beaconType = BleBeaconType.BEACON_UNKNOWN;

    /**
     * The device name
     */
    private String name = new String();

    /**
     * An enumeration of basic beacon types
     */
    public enum BleBeaconType {
        BEACON_UNKNOWN,
        BEACON_ADVERTISEMENT,
        BEACON_SCANRESPONSE
    }

    /**
     * Sets the receive signal strength RSSI value for the scan
     *
     * param rssi the RSSI value for the scan packet in dBm
     */
    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    /**
     * Gets the receive signal strength RSSI value for the scan
     *
     * @return the RSSI value for the scan packet in dBm or Integer.MIN_VALUE if no RSSI is available.
     */
    public int getRssi() {
        return rssi;
    }

    /**
     * Sets the scan packet data
     *
     * @param data an int array containing the raw packet data;
     */
    public void setData(int[] data) {
        this.data = data;
    }

    /**
     * Gets the scan packet data
     *
     * @return an int array containing the data or null if none is set
     */
    public int[] getData() {
        return data;
    }

    /**
     * Sets the scan packet manufacturer specific data
     *
     * @param manufacturerData an int array containing the manufacturer specific data
     */
    public void setManufacturerData(int[] manufacturerData) {
        this.manufacturerData = manufacturerData;
    }

    /**
     * Gets the scan packet manufacturer specific data
     *
     * @return an int array containing the manufacturer specific data or null if none is set
     */
    public int[] getManufacturerData() {
        return manufacturerData;
    }

    /**
     * Sets the beacon type for this packet
     *
     * @beaconType the {@link BleBeaconType} for this packet
     */
    public void setBeaconType(BleBeaconType beaconType) {
        this.beaconType = beaconType;
    }

    /**
     * Gets the beacon type for this packet
     *
     * @return the {@link BleBeaconType} for this packet
     */
    public BleBeaconType getBeaconType() {
        return beaconType;
    }

    /**
     * Sets the device name
     *
     * @param name {@link String} containing the device name
     */
    public void setDeviceName(String name) {
        this.name = name;
    }

    /**
     * Gets the device name
     *
     * @return {@link String} containing the device name
     */
    public String getDeviceName() {
        return name;
    }
}
