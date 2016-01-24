/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.bluetooth.bluez;

import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.io.transport.bluetooth.BluetoothDevice;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGatt;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGattCallback;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGattCharacteristic;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGattDescriptor;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGattService;
import org.eclipse.smarthome.io.transport.bluetooth.bluez.internal.dbus.ObjectManager;
import org.eclipse.smarthome.io.transport.bluetooth.bluez.internal.dbus.ObjectManager.InterfacesAdded;
import org.eclipse.smarthome.io.transport.bluetooth.bluez.internal.dbus.ObjectManager.InterfacesRemoved;
import org.eclipse.smarthome.io.transport.bluetooth.bluez.internal.dbus.Properties.PropertiesChanged;
import org.freedesktop.DBus;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.Path;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of BluetoothGatt for BlueZ
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class BluezBluetoothGatt extends BluetoothGatt implements DBusSigHandler {

    private static final Logger logger = LoggerFactory.getLogger(BluezBluetoothGatt.class);

    private DBusConnection connection;
    private BluezBluetoothDevice device;
    private String dbusPath;

    private DBus.Properties propertyReader;

    BluezBluetoothGatt(BluezBluetoothDevice device, boolean autoConnect, BluetoothGattCallback callback,
            int transport) {
        super(autoConnect, callback, transport);
        this.dbusPath = device.getDbusPath();
        this.device = device;

        // TODO: If the device isn't known by BlueZ yet, then???????

        // Whether to directly connect to the remote device (false) or to automatically connect as soon as
        // the remote device becomes available (true).
        if (autoConnect == false) {
        }

        // Set callback to null to stop calling the service update event while we're adding them
        this.callback = null;

        try {
            String dbusAddress = System.getProperty(BluezBluetoothConstants.BLUEZ_DBUS_CONFIGURATION);
            if (dbusAddress == null) {
                connection = DBusConnection.getConnection(DBusConnection.SYSTEM);
            } else {
                connection = DBusConnection.getConnection(dbusAddress);
            }

            // Get all the services we know about
            // Any services that get added later will be through the interface signal
            ObjectManager objectManager = connection.getRemoteObject(BluezBluetoothConstants.BLUEZ_DBUS_SERVICE, "/",
                    ObjectManager.class);
            Map<Path, Map<String, Map<String, Variant>>> managedObjects = objectManager.GetManagedObjects();
            if (managedObjects != null) {
                for (Path objectPath : managedObjects.keySet()) {
                    Map<String, Map<String, Variant>> managedObject = managedObjects.get(objectPath);
                    Map<String, Variant> serviceProperties = managedObject
                            .get(BluezBluetoothConstants.BLUEZ_DBUS_INTERFACE_GATTSERVICE1);

                    addService(objectPath.getPath(), serviceProperties);
                }
            }

            connection.addSigHandler(PropertiesChanged.class, this);
            connection.addSigHandler(InterfacesAdded.class, this);
            connection.addSigHandler(InterfacesRemoved.class, this);

            propertyReader = connection.getRemoteObject(BluezBluetoothConstants.BLUEZ_DBUS_SERVICE, dbusPath,
                    DBus.Properties.class);
            Map<String, Variant> properties = propertyReader
                    .GetAll(BluezBluetoothConstants.BLUEZ_DBUS_INTERFACE_DEVICE1);
            if (properties != null) {
                updateDeviceProperties(properties);
            }

            // Restore the callback!
            // Do this last to avoid any callbacks while we're initialising
            this.callback = callback;
        } catch (DBusException | DBusExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Restore the callback and add the event handlers
        this.callback = callback;
    }

    @Override
    public void finalize() {
        try {
            if (connection != null) {
                connection.removeSigHandler(PropertiesChanged.class, this);
                connection.removeSigHandler(InterfacesAdded.class, this);
                connection.removeSigHandler(InterfacesRemoved.class, this);

                connection.disconnect();
            }
        } catch (DBusException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public boolean connect() {
        super.connect();

        Boolean connectedProperty = propertyReader.Get(BluezBluetoothConstants.BLUEZ_DBUS_INTERFACE_DEVICE1,
                BluezBluetoothConstants.BLUEZ_DBUS_DEVICE_PROPERTY_CONNECTED);
        if (connectedProperty == null) {
            connectionState = STATE_DISCONNECTED;
        } else {
            if (connectedProperty == true) {
                connectionState = STATE_CONNECTED;
            } else {
                connectionState = STATE_DISCONNECTED;
            }
        }

        // If we're already connected, then notify the users
        // Otherwise we might never find out!
        if (connectionState == STATE_CONNECTED) {
            doConnectionStateChange(true);
            return true;
        }

        device.Connect();
        return true;
    }

    @Override
    public void disconnect() {
        super.disconnect();
        try {
            device.Disconnect();
        } catch (DBusExecutionException e) {
            logger.debug("Disconnect to {} failed: {} {}", device.getAddress(), e.getMessage(), e);
        }
    }

    @Override
    public void handle(DBusSignal signal) {
        try {
            if (signal instanceof PropertiesChanged) {
                if (signal.getPath().equals(dbusPath) == false) {
                    return;
                }

                PropertiesChanged propertiesChanged = (PropertiesChanged) signal;

                if (BluezBluetoothConstants.BLUEZ_DBUS_INTERFACE_DEVICE1
                        .equals(propertiesChanged.interface_name) == true) {
                    if (propertiesChanged.changed_properties.size() != 0) {
                        logger.debug("{}: Properties changed: {}", dbusPath, propertiesChanged.changed_properties);
                        updateDeviceProperties(propertiesChanged.changed_properties);
                    }
                    if (propertiesChanged.invalidated_properties.size() != 0) {
                        logger.debug("{}: Properties invalid: {}", dbusPath, propertiesChanged.invalidated_properties);
                        invalidateDeviceProperties(propertiesChanged.invalidated_properties);
                    }
                }
            } else if (signal instanceof InterfacesAdded) {
                InterfacesAdded interfacesAdded = (InterfacesAdded) signal;

                String objectSplit[] = interfacesAdded.object_path.toString().split(":");

                Map<String, Variant> properties = interfacesAdded.interfaces_and_properties
                        .get(BluezBluetoothConstants.BLUEZ_DBUS_INTERFACE_GATTSERVICE1);

                addService(objectSplit[2], properties);
            } else if (signal instanceof InterfacesRemoved) {
                // removeDevice((InterfacesRemoved) signal);
            }
        } catch (DBusExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public int getConnectionState(BluetoothDevice device) {
        // Not supported
        return STATE_DISCONNECTED;
    }

    @Override
    public void close() {
        try {
            if (connection != null) {
                connection.removeSigHandler(PropertiesChanged.class, this);
                connection.removeSigHandler(InterfacesAdded.class, this);
                connection.removeSigHandler(InterfacesRemoved.class, this);

                connection.disconnect();
            }
        } catch (DBusException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        super.close();
    }

    /**
     * Updates the gatt service configuration from the Bluez DBus properties
     *
     * @param changed_properties
     */
    private void updateDeviceProperties(Map<String, Variant> properties) {
        for (String property : properties.keySet()) {
            logger.debug("GATT '{}' updated property: {} to {}", dbusPath, property,
                    properties.get(property).getValue());
            switch (property) {
                // case BluezBluetoothConstants.BLUEZ_DBUS_DEVICE_PROPERTY_RSSI:
                // int rssi = (short) properties.get(BluezBluetoothConstants.BLUEZ_DBUS_DEVICE_PROPERTY_RSSI)
                // .getValue();
                // updateRSSI(rssi);
                // break;
                case BluezBluetoothConstants.BLUEZ_DBUS_DEVICE_PROPERTY_CONNECTED:
                    boolean connected = (boolean) properties
                            .get(BluezBluetoothConstants.BLUEZ_DBUS_DEVICE_PROPERTY_CONNECTED).getValue();
                    doConnectionStateChange(connected);
                    break;
                default:
                    break;
            }
        }
    }

    private void invalidateDeviceProperties(List<String> invalidated_properties) {
        for (String property : invalidated_properties) {
            logger.trace("GATT Service '{}' invalidated property: {}", dbusPath, property);
            switch (property) {
                case BluezBluetoothConstants.BLUEZ_DBUS_DEVICE_PROPERTY_CONNECTED:
                    doConnectionStateChange(false);
                    break;
                default:
                    break;
            }
        }
    }

    private boolean doAutoConnect() {
        if (connectionState == STATE_CONNECTED) {
            return true;
        }

        if (autoConnect == false) {
            return false;
        }

        connect();
        return true;
    }

    private void addService(String path, Map<String, Variant> serviceProperties) {
        if (serviceProperties == null) {
            return;
        }

        Variant devicePathVariant = serviceProperties
                .get(BluezBluetoothConstants.BLUEZ_DBUS_GATTSERVICE_PROPERTY_DEVICE);
        if (devicePathVariant == null) {
            return;
        }

        String devicePath = devicePathVariant.getValue().toString();
        if (dbusPath.equals(devicePath) == false) {
            return;
        }

        logger.debug("BlueZ GATT Service is {} :: {}", path, serviceProperties);
        BluetoothGattService service = new BluezBluetoothGattService(path);
        addService(service);
    }

    @Override
    public boolean readRemoteRssi() {
        super.readRemoteRssi();
        new RssiThread(this, callback).run();

        return true;
    }

    private class RssiThread extends Thread {
        private BluetoothGatt gatt;
        private BluetoothGattCallback callback;

        RssiThread(BluetoothGatt gatt, BluetoothGattCallback callback) {
            this.callback = callback;
            this.gatt = gatt;
        }

        @Override
        public void run() {
            try {
                Short remoteRssi = propertyReader.Get(BluezBluetoothConstants.BLUEZ_DBUS_INTERFACE_DEVICE1,
                        BluezBluetoothConstants.BLUEZ_DBUS_DEVICE_PROPERTY_RSSI);

                if (remoteRssi == null) {
                    callback.onReadRemoteRssi(gatt, 0, GATT_FAILURE);
                } else {
                    callback.onReadRemoteRssi(gatt, remoteRssi, GATT_SUCCESS);
                }
            } catch (DBusExecutionException e) {
                logger.debug("Error reading RSSI: {}", e.getMessage());
                callback.onReadRemoteRssi(gatt, 0, GATT_FAILURE);
            }
        }
    }

    @Override
    public boolean discoverServices() {
        // If we already have a list of services, call the callback in a few milliseconds
        // This ensures the asynchronousity
        if (gattServices.isEmpty() == false) {
            new DiscoverThread(this, callback).run();
        }
        return true;
    }

    /**
     * This class implements a delay before sending the onServicesDiscovered callback.
     * This is needed to allow the asynchronous request to return before we respond with the services.
     * This is used in instances where we already know the services when the connection is made.
     */
    private class DiscoverThread extends Thread {
        private BluetoothGatt gatt;
        private BluetoothGattCallback callback;

        DiscoverThread(BluetoothGatt gatt, BluetoothGattCallback callback) {
            this.callback = callback;
            this.gatt = gatt;
        }

        @Override
        public void run() {
            try {
                sleep(50);
                callback.onServicesDiscovered(gatt, GATT_SUCCESS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                callback.onServicesDiscovered(gatt, GATT_FAILURE);
            }
        }
    }

    /**
     * Handle connection state changes.
     * DISCONNECT is sent immediately. CONNECT is delayed.
     *
     * @param connected true if the device is connected
     */
    private void doConnectionStateChange(boolean connected) {
        if (callback == null) {
            setConnectedState(connected);

            return;
        }

        if (connected == false) {
            setConnectedState(false);

            return;
        }

        new ConnectStateThread(this, callback).run();
    }

    /**
     * This class implements a delay following a connect notification before notifying the callback.
     * Without this delay, a request sent immediately following a BlueZ connect update will fail with
     * a 'device not connected' notification.
     */
    private class ConnectStateThread extends Thread {
        private BluetoothGatt gatt;
        private BluetoothGattCallback callback;

        ConnectStateThread(BluetoothGatt gatt, BluetoothGattCallback callback) {
            this.callback = callback;
            this.gatt = gatt;
        }

        @Override
        public void run() {
            try {
                // TODO: Find out why this is required, or at least how long it needs to be!
                // 50ms is too short!
                sleep(100);
                setConnectedState(true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void sendReadDescriptor(BluetoothGattDescriptor descriptor) {
        ((BluezBluetoothGattDescriptor) descriptor).read(this, callback);
    }

    @Override
    protected void sendReadCharacteristic(BluetoothGattCharacteristic characteristic) {
        ((BluezBluetoothGattCharacteristic) characteristic).read(this, callback);
    }

    @Override
    protected void sendWriteDescriptor(BluetoothGattDescriptor descriptor) {
        ((BluezBluetoothGattDescriptor) descriptor).write(this, callback);
    }

    @Override
    protected void sendWriteCharacteristic(BluetoothGattCharacteristic characteristic) {
        ((BluezBluetoothGattCharacteristic) characteristic).write(this, callback);
    }
}
