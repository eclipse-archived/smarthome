/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.bluetooth;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a remote Bluetooth device.
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public abstract class BluetoothDevice {
    protected String name;
    protected String address;
    protected int type;
    protected int manufacturer = -1;
    protected BluetoothClass bluetoothClass;
    protected int bondState = BOND_NONE;
    protected List<UUID> supportedUuid = new ArrayList<UUID>();

    public static final int DEVICE_TYPE_DUAL = 0x003;
    public static final int DEVICE_TYPE_LE = 0x002;
    public static final int DEVICE_TYPE_UNKNOWN = 0x000;

    public static final int BOND_NONE = 0;
    public static final int BOND_BONDING = 1;
    public static final int BOND_BONDED = 2;

    public static final int TRANSPORT_AUTO = 0;
    public static final int TRANSPORT_BREDR = 1;
    public static final int TRANSPORT_LE = 2;

    /**
     * Returns the the name of the Bluetooth device.
     *
     * @return The devices name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the physical address of the device.
     *
     * @return The physical address of the device
     */
    public String getAddress() {
        return address;
    }

    /**
     * The type of devices, name whether the device supports
     * Bluetooth LE or not.
     *
     * @return The device type
     */
    public int getType() {
        return type;
    }

    /**
     * Connect to GATT Server hosted by this device.
     *
     * @param autoConnect Whether to directly connect to the remote device (false) or to automatically connect as soon
     *            as the remote device becomes available (true).
     * @param callback GATT callback handler that will receive asynchronous callbacks.
     * @return
     */
    public BluetoothGatt connectGatt(boolean autoConnect, BluetoothGattCallback callback) {
        return connectGatt(autoConnect, callback, TRANSPORT_AUTO);
    }

    /**
     * Connect to GATT Server hosted by this device.
     *
     * @param autoConnect Whether to directly connect to the remote device (false) or to automatically connect as soon
     *            as the remote device becomes available (true).
     * @param callback GATT callback handler that will receive asynchronous callbacks.
     * @param preferred transport for GATT connections to remote dual-mode devices TRANSPORT_AUTO or TRANSPORT_BREDR or
     *            TRANSPORT_LE
     * @return
     */
    abstract public BluetoothGatt connectGatt(boolean autoConnect, BluetoothGattCallback callback, int transport);

    /**
     * Start the pairing process with the remote device.
     * This is an asynchronous call, it will return immediately - an event will be sent when the state changes.
     *
     * @return true if bonding will start
     */
    abstract public boolean createBond();

    /**
     * Create an RFCOMM BluetoothSocket socket ready to start an insecure outgoing connection to the remote device
     * using SDP lookup of UUID.
     *
     * @param uuid
     * @return
     */
    // public BluetoothSocket createInsecureRfcommSocketToServiceRecord(UUID uuid)

    /**
     * Create an RFCOMM BluetoothSocket ready to start a secure outgoing connection to this remote device using SDP
     * lookup of UUID.
     *
     * @param uuid
     * @return
     */
    // public BluetoothSocket createRfcommSocketToServiceRecord(UUID uuid)

    /**
     * Confirm passkey for PAIRING_VARIANT_PASSKEY_CONFIRMATION pairing.
     *
     * @param confirm
     * @return
     */
    public boolean setPairingConfirmation(boolean confirm) {
        return confirm;

    }

    /**
     * Set the pin during pairing when the pairing method is PAIRING_VARIANT_PIN
     *
     * @param pin
     * @return
     */
    public boolean setPin(byte[] pin) {
        return false;
    }

    /**
     * Perform a service discovery on the remote device to get the UUIDs supported.
     *
     * @return
     */
    // abstract public boolean fetchUuidsWithSdp();

    /**
     * Get the Bluetooth class of the remote device.
     *
     * @return
     */
    public BluetoothClass getBluetoothClass() {
        return bluetoothClass;
    }

    /**
     * Get the bond (paired) state of the remote device.
     *
     * Possible values for the bond state are: BOND_NONE, BOND_BONDING, BOND_BONDED.
     *
     * @return
     */
    public int getBondState() {
        return bondState;
    }

    /**
     * Returns the supported features (UUIDs) of the remote device.
     *
     * @return
     */
    public UUID[] getUuids() {
        return supportedUuid.toArray(new UUID[supportedUuid.size()]);
    }

    /**
     * Returns the manufacturer ID of the device
     *
     * @return
     */
    public int getManufacturer() {
        return manufacturer;
    }

}