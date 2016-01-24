/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.bluetooth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.io.transport.bluetooth.events.BluetoothEvent;
import org.eclipse.smarthome.io.transport.bluetooth.le.BluetoothLeScanner;

/**
 * BluetoothAdapter represents a physical Bluetooth adapter
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public abstract class BluetoothAdapter {
    public static final int STATE_OFF = 10;
    public static final int STATE_ON = 12;

    protected static Map<String, BluetoothDevice> bluetoothDevices = new HashMap<String, BluetoothDevice>();

    private ArrayList<BluetoothEventListener> eventListeners = new ArrayList<BluetoothEventListener>();
    protected int state = STATE_OFF;
    protected String name;
    protected String address;

    protected boolean leReady = false;

    /**
     *
     * @return
     */
    public String getAddress() {
        return address;
    }

    /**
     * Return true if the adapter has discovery enabled
     *
     */
    public boolean isDiscovering() {
        return false;
    }

    /**
     * Return the power / enable status of the adapter
     *
     * @return true if adapter is enabled, false otherwise
     */
    public boolean isEnabled() {
        return false;
    }

    /**
     * Return true if the adapter supports Bluetooth LE.
     *
     * @return true if the adapter supports Bluetooth LE
     */
    public boolean isLeReady() {
        return false;
    }

    /**
     * Enable the Bluetooth adapter. This may power the adapter on.
     */
    public void enable() {
    }

    /**
     * Disable the Bluetooth adapter. This may power the adapter off
     */
    public void disable() {
    }

    /**
     * Returns a BluetoothLeScanner object which provides the Bluetooth LE scan function.
     *
     * @return
     */
    public BluetoothLeScanner getBluetoothLeScanner() {
        return null;
    }

    /**
     * Return the set of BluetoothDevices that are paired to the adapter.
     *
     * @return
     */
    public Set<BluetoothDevice> getBondedDevices() {
        return null;
    }

    /**
     * Start the device discovery.
     *
     * @return true if the discovery started ok
     */
    public boolean startDiscovery() {
        return false;
    }

    /**
     * Stops the device discovery.
     *
     * @return
     */
    public boolean cancelDiscovery() {
        return false;
    }

    /**
     * Get a remote Bluetooth device based on its hardware address.
     * This method will always return a device object even if the device doesn't exist on the network.
     *
     * @param address Hardware address of remote device
     * @return BluetoothDevice
     */
    public BluetoothDevice getRemoteDevice(String address) {
        if (bluetoothDevices.containsKey(address)) {
            return bluetoothDevices.get(address);
        }
        return null;
    }

    /**
     * Start Beacon advertising for the given interface.
     *
     */
    public void startBeaconAdvertising() {
    }

    /**
     * Stop Beacon advertising.
     *
     */
    public void stopBeaconAdvertising() {
    }

    /**
     * Set the Beacon advertising interval.
     *
     * @param min Minimum time interval between advertisements
     * @param max Maximum time interval between advertisements
     *
     */
    public void setBeaconAdvertisingInterval(Integer min, Integer max) {
    }

    /**
     * Set the data in to the Beacon advertising packet for the given interface.
     *
     * @param uuid Proximity UUID
     * @param major Groups beacons with the same proximity UUID
     * @param minor Differentiates beacons with the same proximity UUID and major value
     * @param txPower Transmitting power \@1m
     * @param companyCode Specifies the manufacturer
     * @param LELimited Specifies the LE Limited Discoverable Mode (the device advertises for 30.72s only)
     * @param LEGeneral Specifies the LE General Discoverable Mode (the device advertises indefinitely)
     * @param BR_EDRSupported Sets support for BR/EDR
     * @param LE_BRController Specifies whether LE and BR/EDR Controller operates simultaneously
     * @param LE_BRHost Specifies whether LE and BR/EDR Host operates simultaneously
     *
     */
    public void setBeaconAdvertisingData(String uuid, Integer major, Integer minor, String companyCode, Integer txPower,
            boolean LELimited, boolean LEGeneral, boolean BR_EDRSupported, boolean LE_BRController, boolean LE_BRHost) {
    }

    /**
     * Get the Bluetooth name of the local Bluetooth adapter.
     * This name is visible to remote Bluetooth devices and can be set by the user.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the adapter
     *
     * @param name
     * @return
     */
    public boolean setName(String name) {
        this.name = name;
        return true;
    }

    /**
     *
     * @param eventListener
     */
    public void addEventListener(BluetoothEventListener eventListener) {
        synchronized (this.eventListeners) {
            this.eventListeners.add(eventListener);
        }
    }

    /**
     *
     * @param eventListener
     */
    public void removeEventListener(BluetoothEventListener eventListener) {
        synchronized (this.eventListeners) {
            this.eventListeners.remove(eventListener);
        }
    }

    /**
     *
     * @param event
     */
    public void notifyEventListeners(BluetoothEvent event) {
        ArrayList<BluetoothEventListener> copy = new ArrayList<BluetoothEventListener>(this.eventListeners);
        for (BluetoothEventListener listener : copy) {
            listener.handleBluetoothEvent(event);
        }
    }

    /**
     * Validate a String Bluetooth address, such as "DE:AD:00:00:BE:EF"
     * Alphabetic characters must be uppercase to be valid.
     *
     * @param address
     * @return
     */
    public static boolean checkBluetoothAddress(String address) {
        return true;
    }

    /**
     *
     * @return
     */
    public int getState() {
        return state;
    }

}