/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.bluetooth.bluez;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import org.eclipse.smarthome.io.transport.bluetooth.BluetoothClass;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothDevice;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGatt;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGattCallback;
import org.eclipse.smarthome.io.transport.bluetooth.bluez.internal.dbus.Device1;
import org.eclipse.smarthome.io.transport.bluetooth.bluez.internal.dbus.Properties.PropertiesChanged;
import org.eclipse.smarthome.io.transport.bluetooth.events.BluetoothDeviceBondingEvent;
import org.freedesktop.DBus;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.UInt16;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of BluetoothDevice for BlueZ
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class BluezBluetoothDevice extends BluetoothDevice implements DBusSigHandler {
    private BluezBluetoothAdapter adapter;
    private DBusConnection connection;
    private Device1 device1;
    private String dbusPath;
    DBus.Properties propertyReader;

    private static final Logger logger = LoggerFactory.getLogger(BluezBluetoothDevice.class);

    public BluezBluetoothDevice(BluezBluetoothAdapter adapter, String address, String name) {
        dbusPath = adapter.getDbusPath() + "/dev_" + address.replaceAll(":", "_");
        this.address = address;
        this.name = name;
        logger.debug("Creating BlueZ device at '{}'", dbusPath);

        try {
            String dbusAddress = System.getProperty(BluezBluetoothConstants.BLUEZ_DBUS_CONFIGURATION);
            if (dbusAddress == null) {
                connection = DBusConnection.getConnection(DBusConnection.SYSTEM);
            } else {
                connection = DBusConnection.getConnection(dbusAddress);
            }
            logger.debug("BlueZ connection opened at {}", connection.getUniqueName());

            device1 = connection.getRemoteObject(BluezBluetoothConstants.BLUEZ_DBUS_SERVICE, dbusPath, Device1.class);

            connection.addSigHandler(PropertiesChanged.class, this);
            propertyReader = connection.getRemoteObject(BluezBluetoothConstants.BLUEZ_DBUS_SERVICE, dbusPath,
                    DBus.Properties.class);
            Map<String, Variant> properties = propertyReader
                    .GetAll(BluezBluetoothConstants.BLUEZ_DBUS_INTERFACE_DEVICE1);
            if (properties != null) {
                updateProperties(properties);
            }
        } catch (DBusException | DBusExecutionException e1) {
            e1.printStackTrace();
        }
    }

    public BluezBluetoothDevice(BluezBluetoothAdapter adapter, Map<String, Variant> properties) {
        // manufacturerCode
        this(adapter, properties.get(BluezBluetoothConstants.BLUEZ_DBUS_DEVICE_PROPERTY_ADDRESS).getValue().toString(),
                properties.get(BluezBluetoothConstants.BLUEZ_DBUS_DEVICE_PROPERTY_ALIAS).getValue().toString());

        Vector<String> x = (Vector<String>) properties.get(BluezBluetoothConstants.BLUEZ_DBUS_DEVICE_PROPERTY_UUID)
                .getValue();
        for (String el : x) {
            try {
                UUID uuid = UUID.fromString(el);
                supportedUuid.add(uuid);
            } catch (IllegalArgumentException e) {
                logger.debug("Illegal UUID {}", el);
                continue;
            }
        }
    }

    @Override
    public void finalize() {
        try {
            if (connection != null) {
                connection.removeSigHandler(PropertiesChanged.class, this);
                // connection.removeSigHandler(InterfacesAdded.class, this);
                // connection.removeSigHandler(InterfacesRemoved.class, this);

                connection.disconnect();
            }
        } catch (DBusException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public BluetoothGatt connectGatt(boolean autoConnect, BluetoothGattCallback callback, int transport) {
        // If the device supports the GENERIC_ATTRIBUTE profile, then create a GATT class
        // if (!uuid.contains(BluezBluetoothConstants.BLUEZ_PROFILE_GATT)) {
        // logger.debug("{} attempted to connect to GATT when profile isn't supported", dbusPath);
        // return null;
        // }

        return new BluezBluetoothGatt(this, autoConnect, callback, transport);
    }

    @Override
    public boolean createBond() {
        if (bondState == BOND_BONDED) {
            return false;
        }

        try {
            device1.Pair();
        } catch (DBusExecutionException e) {
            return false;
        }

        bondState = BOND_BONDING;
        adapter.notifyEventListeners(new BluetoothDeviceBondingEvent(this, bondState));

        return true;
    }

    @Override
    public void handle(DBusSignal signal) {
        // Make sure it's for us
        if (dbusPath.equals(signal.getPath()) == false) {
            return;
        }

        try {
            if (signal instanceof PropertiesChanged) {
                PropertiesChanged propertiesChanged = (PropertiesChanged) signal;

                if (BluezBluetoothConstants.BLUEZ_DBUS_INTERFACE_DEVICE1
                        .equals(propertiesChanged.interface_name) == false) {
                    return;
                }
                if (propertiesChanged.changed_properties.size() != 0) {
                    // logger.debug("{}: Properties changed: {}", dbusPath, propertiesChanged.changed_properties);
                    updateProperties(propertiesChanged.changed_properties);
                }
                if (propertiesChanged.invalidated_properties.size() != 0) {
                    logger.debug("{}: Properties invalid: {}", dbusPath, propertiesChanged.invalidated_properties);
                }
            } else {
                logger.info("Unknown signal!!! {}", signal.getClass());
            }
        } catch (DBusExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Updates the device configuration from the Bluez DBus properties
     *
     * @param changed_properties
     */
    private void updateProperties(Map<String, Variant> properties) {
        for (String property : properties.keySet()) {
            logger.trace("Device '{}' updated property: {} to {}", dbusPath, property,
                    properties.get(property).getValue());
            switch (property) {
                case BluezBluetoothConstants.BLUEZ_DBUS_DEVICE_PROPERTY_NAME:
                    // Name can't change, so if it's already set, then ignore
                    // This allows the name to be used if the alias isn't set
                    name = (String) properties.get(BluezBluetoothConstants.BLUEZ_DBUS_DEVICE_PROPERTY_NAME).getValue();
                    break;
                case BluezBluetoothConstants.BLUEZ_DBUS_DEVICE_PROPERTY_ALIAS:
                    name = (String) properties.get(BluezBluetoothConstants.BLUEZ_DBUS_DEVICE_PROPERTY_ALIAS).getValue();
                    break;
                case BluezBluetoothConstants.BLUEZ_DBUS_DEVICE_PROPERTY_ADDRESS:
                    address = (String) properties.get(BluezBluetoothConstants.BLUEZ_DBUS_DEVICE_PROPERTY_ADDRESS)
                            .getValue();
                    break;
                case BluezBluetoothConstants.BLUEZ_DBUS_DEVICE_PROPERTY_CLASS:
                    Variant variant;
                    variant = properties.get(BluezBluetoothConstants.BLUEZ_DBUS_DEVICE_PROPERTY_CLASS);
                    if (variant != null) {
                        bluetoothClass = new BluetoothClass(Integer.parseInt(variant.getValue().toString()));
                    }
                    break;
                case BluezBluetoothConstants.BLUEZ_DBUS_DEVICE_PROPERTY_PAIRED:
                    bondState = ((Boolean) properties.get(BluezBluetoothConstants.BLUEZ_DBUS_DEVICE_PROPERTY_PAIRED)
                            .getValue()) ? BOND_BONDED : BOND_NONE;
                    break;
                case BluezBluetoothConstants.BLUEZ_DBUS_DEVICE_PROPERTY_MANUFACTURER:
                    variant = properties.get(BluezBluetoothConstants.BLUEZ_DBUS_DEVICE_PROPERTY_MANUFACTURER);
                    if (variant != null) {
                        Set keys = ((Map) variant.getValue()).keySet();
                        if (keys != null) {
                            UInt16 x = (UInt16) keys.toArray()[0];
                            manufacturer = x.intValue();
                        }
                    }
                default:
                    break;
            }
        }
    }

    protected void Connect() {
        try {
            device1.Connect();
        } catch (DBusExecutionException e) {
            logger.debug("Error connecting to {}: ", address, e.getMessage());
        }
    }

    protected void Disconnect() {
        try {
            device1.Disconnect();
        } catch (DBusExecutionException e) {
            logger.debug("Error connecting to {}: ", address, e.getMessage());
        }
    }

    public String getDbusPath() {
        return dbusPath;
    }
}
