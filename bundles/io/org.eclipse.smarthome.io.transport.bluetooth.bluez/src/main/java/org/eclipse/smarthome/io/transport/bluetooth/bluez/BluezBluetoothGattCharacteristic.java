/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.bluetooth.bluez;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGatt;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGattCallback;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGattCharacteristic;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGattService;
import org.eclipse.smarthome.io.transport.bluetooth.bluez.internal.dbus.GattCharacteristic1;
import org.eclipse.smarthome.io.transport.bluetooth.bluez.internal.dbus.Properties.PropertiesChanged;
import org.freedesktop.DBus;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of BluetoothCharacteristic for BlueZ
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class BluezBluetoothGattCharacteristic extends BluetoothGattCharacteristic implements DBusSigHandler {
    private static final Logger logger = LoggerFactory.getLogger(BluetoothGattCharacteristic.class);

    private DBusConnection connection;
    private String dbusPath;
    private GattCharacteristic1 characteristic1;

    public BluezBluetoothGattCharacteristic(BluetoothGattService service, String characteristicPath) {
        super();

        this.service = service;

        // logger.debug("Creating BlueZ GATT characteristic at '{}'", characteristicPath);

        dbusPath = characteristicPath;

        try {
            String dbusAddress = System.getProperty(BluezBluetoothConstants.BLUEZ_DBUS_CONFIGURATION);
            if (dbusAddress == null) {
                connection = DBusConnection.getConnection(DBusConnection.SYSTEM);
            } else {
                connection = DBusConnection.getConnection(dbusAddress);
            }
            // logger.debug("BlueZ connection opened at {}", connection.getUniqueName());

            DBus.Properties propertyReader = connection.getRemoteObject(BluezBluetoothConstants.BLUEZ_DBUS_SERVICE,
                    dbusPath, DBus.Properties.class);
            Map<String, Variant> properties = propertyReader
                    .GetAll(BluezBluetoothConstants.BLUEZ_DBUS_INTERFACE_GATTCHARACTERISTIC1);
            updateProperties(properties);

            connection.addSigHandler(PropertiesChanged.class, this);

            characteristic1 = connection.getRemoteObject(BluezBluetoothConstants.BLUEZ_DBUS_SERVICE, dbusPath,
                    GattCharacteristic1.class);
        } catch (DBusException e) {
            // TODO Auto-generated catch block
            logger.debug("Error initialising GATT characteristic {} :: {}", dbusPath, e);
        }

    }

    @Override
    public void finalize() {
        try {
            if (connection != null) {
                connection.removeSigHandler(PropertiesChanged.class, this);
                connection.disconnect();
            }
        } catch (DBusException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void handle(DBusSignal signal) {
        try {
            if (signal instanceof PropertiesChanged) {
                // Make sure it's for us
                if (dbusPath.equals(signal.getPath()) == false) {
                    return;
                }

                PropertiesChanged propertiesChanged = (PropertiesChanged) signal;

                if (BluezBluetoothConstants.BLUEZ_DBUS_INTERFACE_GATTCHARACTERISTIC1
                        .equals(propertiesChanged.interface_name) == false) {
                    return;
                }
                if (propertiesChanged.changed_properties.size() != 0) {
                    logger.debug("{}: Properties changed: {}", dbusPath, propertiesChanged.changed_properties);
                    updateProperties(propertiesChanged.changed_properties);
                }
                if (propertiesChanged.invalidated_properties.size() != 0) {
                    logger.debug("{}: Properties invalid: {}", dbusPath, propertiesChanged.invalidated_properties);
                }
            }
        } catch (DBusExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Updates the gatt service configuration from the Bluez DBus properties
     *
     * @param changed_properties
     */
    private void updateProperties(Map<String, Variant> properties) {
        for (String property : properties.keySet()) {
            logger.debug("GATT Characteristic '{}' updated property: {} to {}", dbusPath, property,
                    properties.get(property).getValue());
            switch (property) {
                case BluezBluetoothConstants.BLUEZ_DBUS_GATTCHARACTERISTIC_PROPERTY_UUID:
                    uuid = UUID.fromString((String) properties
                            .get(BluezBluetoothConstants.BLUEZ_DBUS_GATTCHARACTERISTIC_PROPERTY_UUID).getValue());
                    break;
                case BluezBluetoothConstants.BLUEZ_DBUS_GATTCHARACTERISTIC_PROPERTY_VALUE:
                    setValue((byte[]) properties
                            .get(BluezBluetoothConstants.BLUEZ_DBUS_GATTCHARACTERISTIC_PROPERTY_VALUE).getValue());

                    // Notify that we've changed value so we can let the listeners know...
                    if (notifyGatt != null) {
                        notifyGatt.doCharacteristicNotification(this);
                    }
                    break;
                case BluezBluetoothConstants.BLUEZ_DBUS_GATTCHARACTERISTIC_PROPERTY_DESCRIPTORS:
                    gattDescriptors.clear();
                    Vector<Object> newDescriptors = (Vector<Object>) properties
                            .get(BluezBluetoothConstants.BLUEZ_DBUS_GATTCHARACTERISTIC_PROPERTY_DESCRIPTORS).getValue();
                    if (newDescriptors != null && newDescriptors.isEmpty() == false) {
                        logger.debug("Characteristics returned {}", newDescriptors);

                        for (Object descriptor : newDescriptors) {
                            addDescriptor(new BluezBluetoothGattDescriptor(this, descriptor.toString()));
                        }
                    }
                    break;
                case BluezBluetoothConstants.BLUEZ_DBUS_GATTCHARACTERISTIC_PROPERTY_FLAGS:
                    Vector<Object> flags = (Vector<Object>) properties
                            .get(BluezBluetoothConstants.BLUEZ_DBUS_GATTCHARACTERISTIC_PROPERTY_FLAGS).getValue();
                    if (flags.contains("read")) {
                        this.properties += PROPERTY_READ;
                    }
                    if (flags.contains("write")) {
                        this.properties += PROPERTY_WRITE;
                    }
                    if (flags.contains("notify")) {
                        this.properties += PROPERTY_NOTIFY;
                    }
                    // TODO: Other properties?
                    break;

                default:
                    break;
            }
        }
    }

    public void read(BluetoothGatt gatt, BluetoothGattCallback callback) {
        new ReadThread(gatt, callback, this).run();
    }

    private class ReadThread extends Thread {
        private BluetoothGattCallback callback;
        private BluetoothGattCharacteristic characteristic;
        private BluetoothGatt gatt;

        ReadThread(BluetoothGatt gatt, BluetoothGattCallback callback, BluetoothGattCharacteristic characteristic) {
            this.callback = callback;
            this.gatt = gatt;
            this.characteristic = characteristic;
        }

        @Override
        public void run() {
            boolean success;
            List<Byte> value = null;

            try {
                value = characteristic1.ReadValue();
                success = true;
            } catch (DBusExecutionException e) {
                logger.debug("Error in characteristic read of {} :: {}", characteristic.getUuid(), e.getMessage());
                success = false;
            }

            if (success == false) {
                callback.onCharacteristicRead(gatt, characteristic, BluetoothGatt.GATT_FAILURE);
                return;
            }

            byte[] newValue = new byte[value.size()];
            int cnt = 0;
            for (byte b : value) {
                newValue[cnt++] = b;
            }
            characteristic.setValue(newValue);

            gatt.processQueueResponse(characteristic.getUuid());
            callback.onCharacteristicRead(gatt, characteristic, BluetoothGatt.GATT_SUCCESS);
        }
    }

    public void write(BluetoothGatt gatt, BluetoothGattCallback callback) {
        new WriteThread(gatt, callback, this, this.value).run();
    }

    private class WriteThread extends Thread {
        private BluetoothGattCallback callback;
        private BluetoothGattCharacteristic characteristic;
        private BluetoothGatt gatt;
        private List<Byte> value;

        WriteThread(BluetoothGatt gatt, BluetoothGattCallback callback, BluetoothGattCharacteristic characteristic,
                byte[] value) {
            this.callback = callback;
            this.gatt = gatt;
            this.characteristic = characteristic;

            this.value = new ArrayList<Byte>(value.length);
            int cnt = 0;
            for (byte b : value) {
                this.value.add(value[cnt++]);
            }
        }

        @Override
        public void run() {
            boolean success;

            try {
                characteristic1.WriteValue(value);
                success = true;
            } catch (DBusExecutionException e) {
                logger.debug("Error in characteristic write {}", e.getMessage());
                success = false;
            }

            if (success == false) {
                callback.onCharacteristicWrite(gatt, characteristic, BluetoothGatt.GATT_FAILURE);
                return;
            }

            gatt.processQueueResponse(characteristic.getUuid());
            callback.onCharacteristicWrite(gatt, characteristic, BluetoothGatt.GATT_SUCCESS);
        }
    }

    @Override
    public boolean setNotification(BluetoothGatt gatt, boolean enable) {
        super.setNotification(gatt, enable);

        // TODO: Check if we're already notifying and return if the state is the same as requested

        boolean success;
        try {
            if (enable) {
                characteristic1.StartNotify();
            } else {
                characteristic1.StopNotify();
            }
            success = true;
        } catch (DBusExecutionException e) {
            logger.debug("Error in characteristic notify {}", e.getMessage());
            success = false;
        }

        return success;
    }
}
