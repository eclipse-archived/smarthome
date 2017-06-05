/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.bluetooth.bluez;

import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGattService;
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
 * Implementation of BluetoothGattService for BlueZ
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class BluezBluetoothGattService extends BluetoothGattService implements DBusSigHandler {
    private static final Logger logger = LoggerFactory.getLogger(BluetoothGattService.class);

    private DBusConnection connection;
    private String dbusPath;

    public BluezBluetoothGattService(String servicePath) {
        super();

        logger.debug("Creating BlueZ GATT service at '{}'", servicePath);

        dbusPath = servicePath;

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
                    .GetAll(BluezBluetoothConstants.BLUEZ_DBUS_INTERFACE_GATTSERVICE1);
            updateProperties(properties);

            connection.addSigHandler(PropertiesChanged.class, this);
        } catch (DBusException e) {
            // TODO Auto-generated catch block
            logger.debug("Error initialising GATT service {} :: {}", dbusPath, e);
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

                if (BluezBluetoothConstants.BLUEZ_DBUS_INTERFACE_GATTSERVICE1
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
            logger.trace("GATT Service '{}' updated property: {} to {}", dbusPath, property,
                    properties.get(property).getValue());
            switch (property) {
                case BluezBluetoothConstants.BLUEZ_DBUS_GATTSERVICE_PROPERTY_UUID:
                    uuid = UUID.fromString((String) properties
                            .get(BluezBluetoothConstants.BLUEZ_DBUS_GATTSERVICE_PROPERTY_UUID).getValue());
                    break;
                case BluezBluetoothConstants.BLUEZ_DBUS_GATTSERVICE_PROPERTY_PRIMARY:
                    serviceType = (Boolean) properties
                            .get(BluezBluetoothConstants.BLUEZ_DBUS_GATTSERVICE_PROPERTY_PRIMARY).getValue() == true
                                    ? SERVICE_TYPE_PRIMARY : SERVICE_TYPE_SECONDARY;
                    break;
                case BluezBluetoothConstants.BLUEZ_DBUS_GATTSERVICE_PROPERTY_CHARACTERISTICS:
                    gattCharacteristics.clear();
                    Vector<Object> newCharacteristics = (Vector<Object>) properties
                            .get(BluezBluetoothConstants.BLUEZ_DBUS_GATTSERVICE_PROPERTY_CHARACTERISTICS).getValue();
                    if (newCharacteristics != null && newCharacteristics.isEmpty() == false) {
                        // logger.debug("Characteristics returned {}", newCharacteristics);

                        for (Object characteristic : newCharacteristics) {
                            addCharacteristic(new BluezBluetoothGattCharacteristic(this, characteristic.toString()));
                        }
                    }
                    break;

                default:
                    break;
            }
        }
    }
}
