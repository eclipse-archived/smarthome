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

import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGatt;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGattCallback;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGattCharacteristic;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGattDescriptor;
import org.eclipse.smarthome.io.transport.bluetooth.bluez.internal.dbus.GattDescriptor1;
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
 * Implementation of BluetoothGattDescriptor for BlueZ
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class BluezBluetoothGattDescriptor extends BluetoothGattDescriptor implements DBusSigHandler {
    private static final Logger logger = LoggerFactory.getLogger(BluetoothGattCharacteristic.class);

    private DBusConnection connection;
    private String dbusPath;
    private GattDescriptor1 descriptor1;

    public BluezBluetoothGattDescriptor(BluetoothGattCharacteristic characteristic, String descriptorPath) {
        super();

        this.characteristic = characteristic;

        // logger.debug("Creating BlueZ GATT characteristic at '{}'", descriptorPath);

        dbusPath = descriptorPath;

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
                    .GetAll(BluezBluetoothConstants.BLUEZ_DBUS_INTERFACE_GATTDESCRIPTOR1);
            updateProperties(properties);

            connection.addSigHandler(PropertiesChanged.class, this);

            descriptor1 = connection.getRemoteObject(BluezBluetoothConstants.BLUEZ_DBUS_SERVICE, dbusPath,
                    GattDescriptor1.class);

        } catch (DBusException e) {
            // TODO Auto-generated catch block
            logger.debug("Error initialising GATT descriptor {} :: {}", dbusPath, e);
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

                if (BluezBluetoothConstants.BLUEZ_DBUS_INTERFACE_GATTDESCRIPTOR1
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
            logger.trace("GATT Descriptor '{}' updated property: {} to {}", dbusPath, property,
                    properties.get(property).getValue());
            switch (property) {
                case BluezBluetoothConstants.BLUEZ_DBUS_GATTDESCRIPTOR_PROPERTY_UUID:
                    uuid = UUID.fromString((String) properties
                            .get(BluezBluetoothConstants.BLUEZ_DBUS_GATTCHARACTERISTIC_PROPERTY_UUID).getValue());
                    break;
                case BluezBluetoothConstants.BLUEZ_DBUS_GATTCHARACTERISTIC_PROPERTY_VALUE:
                    // logger.debug("Descriptor {} updated to {}")
                    // setValue((byte[]) properties
                    // .get(BluezBluetoothConstants.BLUEZ_DBUS_GATTCHARACTERISTIC_PROPERTY_VALUE).getValue());
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public boolean setValue(byte[] newValue) {
        value = newValue;

        return true;
    }

    public void read(BluetoothGatt gatt, BluetoothGattCallback callback) {
        ReadThread reader = new ReadThread(gatt, callback, this);

        reader.run();
    }

    private class ReadThread extends Thread {
        private BluetoothGattCallback callback;
        private BluetoothGattDescriptor descriptor;
        private BluetoothGatt gatt;

        ReadThread(BluetoothGatt gatt, BluetoothGattCallback callback, BluetoothGattDescriptor descriptor) {
            this.callback = callback;
            this.gatt = gatt;
            this.descriptor = descriptor;
        }

        @Override
        public void run() {
            boolean success;
            List<Byte> value = null;

            try {
                value = descriptor1.ReadValue();
                success = true;
            } catch (DBusExecutionException e) {
                logger.debug("Error in descriptor read {}", e);
                success = false;
            }

            if (success == false) {
                callback.onDescriptorRead(gatt, descriptor, BluetoothGatt.GATT_FAILURE);
                return;
            }

            byte[] newValue = new byte[value.size()];
            int cnt = 0;
            for (byte b : value) {
                newValue[cnt++] = b;
            }
            descriptor.setValue(newValue);

            gatt.processQueueResponse(descriptor.getUuid());
            callback.onDescriptorRead(gatt, descriptor, BluetoothGatt.GATT_SUCCESS);
        }
    }

    public void write(BluetoothGatt gatt, BluetoothGattCallback callback) {
        WriteThread reader = new WriteThread(gatt, callback, this, this.value);

        reader.run();
    }

    private class WriteThread extends Thread {
        private BluetoothGattCallback callback;
        private BluetoothGattDescriptor descriptor;
        private BluetoothGatt gatt;
        private List<Byte> value;

        WriteThread(BluetoothGatt gatt, BluetoothGattCallback callback, BluetoothGattDescriptor descriptor,
                byte[] value) {
            this.callback = callback;
            this.gatt = gatt;
            this.descriptor = descriptor;

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
                descriptor1.WriteValue(value);
                success = true;
            } catch (DBusExecutionException e) {
                logger.debug("Error in descriptor write {}", e);
                success = false;
            }

            if (success == false) {
                callback.onDescriptorWrite(gatt, descriptor, BluetoothGatt.GATT_FAILURE);
                return;
            }

            gatt.processQueueResponse(characteristic.getUuid());
            callback.onDescriptorWrite(gatt, descriptor, BluetoothGatt.GATT_SUCCESS);
        }
    }

}
