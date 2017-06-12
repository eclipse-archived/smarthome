/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.ble;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.binding.ble.notification.BleConnectionStatusNotification;
import org.eclipse.smarthome.binding.ble.notification.BleScanNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BleDevice} class provides a base implementation of a Bluetooth Low Energy device
 *
 * @author Chris Jackson - Initial contribution
 */
public class BleDevice {
    private final Logger logger = LoggerFactory.getLogger(BleDevice.class);

    /**
     * Enumeration of BLE connection states
     *
     */
    public enum ConnectionState {
        /**
         * Device is still being discovered and is not available for use.
         */
        DISCOVERING,
        /**
         * Device has been discovered. This is used for the initial notification that the device is available.
         */
        DISCOVERED,
        /**
         * Device is disconnected.
         */
        DISCONNECTED,
        /**
         * A connection is in progress.
         */
        CONNECTING,
        /**
         * The device is connected.
         */
        CONNECTED,
        /**
         * A disconnection is in progress.
         */
        DISCONNECTING
    }

    protected enum BleEventType {
        CONNECTION_STATE,
        SCAN_RECORD,
        CHARACTERISTIC_READ_COMPLETE,
        CHARACTERISTIC_WRITE_COMPLETE,
        CHARACTERISTIC_UPDATED,
        SERVICES_DISCOVERED
    }

    /**
     * Current connection state
     */
    protected ConnectionState connectionState = ConnectionState.DISCOVERING;

    /**
     * Devices Bluetooth address
     */
    protected BluetoothAddress address;

    /**
     * Manufacturer id
     */
    protected int manufacturer = -1;

    /**
     * Manufacturer name
     */
    protected String manufacturerName = null;

    /**
     * Device name.
     * <p>
     * Uses the devices long name if known, otherwise the short name if known
     */
    protected String name;

    /**
     * List of supported services
     */
    protected final Map<UUID, BleService> supportedServices = new HashMap<UUID, BleService>();

    /**
     * Last known RSSI
     */
    protected int rssi = Integer.MIN_VALUE;

    /**
     * Last reported transmitter power
     */
    protected int txPower = Integer.MIN_VALUE;

    /**
     * The event listeners will be notified of device updates
     */
    private final List<BleDeviceListener> eventListeners = new CopyOnWriteArrayList<BleDeviceListener>();

    /**
     * Construct a BLE Device taking the Bluetooth address
     *
     * @param sender
     */
    public BleDevice(BluetoothAddress address) {
        this.address = address;
    }

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
    public BluetoothAddress getAddress() {
        return address;
    }

    /**
     * Sets the manufacturer id for the device
     *
     * @param manufacturer the manufacturer id
     */
    public void setManufacturerId(int manufacturer) {
        this.manufacturer = manufacturer;
        this.manufacturerName = BluetoothManufacturer.getManufacturer(manufacturer);
    }

    /**
     * Returns the manufacturer ID of the device
     *
     * @return an integer with manufacturer ID of the device, or -1 if not known
     */
    public int getManufacturerId() {
        return manufacturer;
    }

    /**
     * Returns the manufacturer name of the device
     *
     * @return an integer with manufacturer ID of the device, or -1 if not known
     */
    public String getManufacturerName() {
        return manufacturerName;
    }

    /**
     * Returns a {@link BleService} if the requested service is supported
     *
     * @return the {@link BleService} or null if the service is not supported.
     */
    public BleService getServices(UUID uuid) {
        return supportedServices.get(uuid);
    }

    /**
     * Returns a list of supported service UUIDs
     *
     * @return list of supported {@link BleService}s.
     */
    public Collection<BleService> getServices() {
        return supportedServices.values();
    }

    /**
     * Sets the device transmit power
     *
     * @param power the current transmitter power in dBm
     */
    public void setTxPower(int txPower) {
        this.txPower = txPower;
    }

    /**
     * Returns the last Transmit Power value or Integer.MIN_VALUE if no transmit power has been received
     *
     * @return the last reported transmitter power value in dBm
     */
    public int getTxPower() {
        return txPower;
    }

    /**
     * Sets the current Receive Signal Strength Indicator (RSSI) value
     *
     * @param rssi the current RSSI value in dBm
     * @return true if the RSSI has changed, false if it was the same as previous
     */
    public boolean setRssi(int rssi) {
        boolean changed = this.rssi != rssi;
        this.rssi = rssi;

        return changed;
    }

    /**
     * Returns the last Receive Signal Strength Indicator (RSSI) value or Integer.MIN_VALUE if no RSSI has been received
     *
     * @return the last RSSI value in dBm
     */
    public int getRssi() {
        return rssi;
    }

    /**
     * Set the name of the device
     *
     * @param name a {@link String} defining the device name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Check if the device supports the specified service
     *
     * @param uuid the service {@link UUID}
     * @return true if the service is supported
     */
    public boolean supportsService(UUID uuid) {
        return supportedServices.containsKey(uuid);
    }

    /**
     * Get the current connection state for this device
     *
     * @return the current {@link ConnectionState}
     */
    public void getConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    /**
     * Connects to a device. This is an asynchronous method. Once the connection state is updated, the
     * {@link BleDeviceListener.onConnectionState} method will be called with the connection state.
     * <p>
     * If the device is already connected, this will return false.
     *
     * @return true if the connection process is started successfully
     */
    public boolean connect() {
        return false;
    }

    /**
     * Disconnects from a device. Once the connection state is updated, the {@link BleDeviceListener.onConnectionState}
     * method will be called with the connection state.
     * <p>
     * If the device is not currently connected, this will return false.
     *
     * @return true if the disconnection process is started successfully
     */
    public boolean disconnect() {
        return false;
    }

    /**
     * Starts a discovery on a device. This will iterate through all services and characteristics to build up a view of
     * the device.
     * <p>
     * This method should be called before attempting to read or write characteristics.
     *
     * @return true if the discovery process is started successfully
     */
    public boolean discoverServices() {
        return false;
    }

    /**
     * Gets a BLE characteristic if it is known.
     * <p>
     * Note that this method will not search for a characteristic in the remote device if it is not known.
     * You must have previously connected to the device so that the device services and characteristics can
     * be retrieved.
     *
     * @param uuid the {@link UUID} of the characteristic to return
     * @return the {@link BleCharacteristic} or null if the characteristic is not found in the device
     */
    public BleCharacteristic getCharacteristic(UUID uuid) {
        for (BleService service : supportedServices.values()) {
            if (service.providesCharacteristic(uuid)) {
                return service.getCharacteristic(uuid);
            }
        }
        return null;
    }

    /**
     * Reads a characteristic. Only a single read or write operation can be requested at once. Attempting to perform an
     * operation when one is already in progress will result in subsequent calls returning false.
     * <p>
     * This is an asynchronous method. Once the read is complete {@link BleDeviceListener.onCharacteristicReadComplete}
     * method will be called with the completion state.
     * <p>
     * Note that {@link BleDeviceListener.onCharacteristicUpdate} will be called when the read value is received.
     *
     * @param characteristic the {@link BleCharacteristic} to read.
     * @return true if the characteristic read is started successfully
     */
    public boolean readCharacteristic(BleCharacteristic characteristic) {
        return false;
    }

    /**
     * Writes a characteristic. Only a single read or write operation can be requested at once. Attempting to perform an
     * operation when one is already in progress will result in subsequent calls returning false.
     * <p>
     * This is an asynchronous method. Once the write is complete
     * {@link BleDeviceListener.onCharacteristicWriteComplete} method will be called with the completion state.
     *
     * @param characteristic the {@link BleCharacteristic} to read.
     * @return true if the characteristic write is started successfully
     */
    public boolean writeCharacteristic(BleCharacteristic characteristic) {
        return false;
    }

    /**
     * Adds a service to the device.
     *
     * @param service the new {@link BleService} to add
     * @return true if the service was added or false if the service was already supported
     */
    protected boolean addService(BleService service) {
        if (supportedServices.containsKey(service.getUuid())) {
            return false;
        }

        logger.debug("BLE adding new service to device {}: {}", address, service);

        supportedServices.put(service.getUuid(), service);
        return true;
    }

    /**
     * Adds a list of services to the device
     *
     * @param uuids
     */
    protected void addServices(List<UUID> uuids) {
        for (UUID uuid : uuids) {
            // Check if we already know about this service
            if (supportsService(uuid)) {
                continue;
            }

            // Create a new service and add it to the device
            addService(new BleService(uuid));
        }
    }

    /**
     * Gets a service based on the handle.
     * This will return a service if the handle falls within the start and end handles for the service.
     *
     * @param handle the handle for the service
     * @return the {@link BleService} or null if the service was not found
     */
    protected BleService getServiceByHandle(int handle) {
        synchronized (supportedServices) {
            for (BleService service : supportedServices.values()) {
                if (service.getHandleStart() <= handle && service.getHandleEnd() >= handle) {
                    return service;
                }
            }
        }
        return null;
    }

    /**
     * Gets a characteristic based on the handle.
     *
     * @param handle the handle for the characteristic
     * @return the {@link BleCharacteristic} or null if the characteristic was not found
     */
    protected BleCharacteristic getCharacteristicByHandle(int handle) {
        BleService service = getServiceByHandle(handle);
        if (service != null) {
            return service.getCharacteristicByHandle(handle);
        }

        return null;
    }

    /**
     * Adds a device listener
     *
     * @param listener the {@link BleDeviceListener} to add
     */
    public void addListener(BleDeviceListener listener) {
        if (listener == null) {
            return;
        }
        eventListeners.add(listener);
    }

    /**
     * Removes a device listener
     *
     * @param listener the {@link BleDeviceListener} to remove
     */
    public void removeListener(BleDeviceListener listener) {
        eventListeners.remove(listener);
    }

    /**
     * Notify the listeners of an event
     *
     * @param event the {@link BleEventType} of this event
     * @param args an array of arguments to pass to the callback
     */
    protected void notifyListeners(BleEventType event, Object... args) {
        for (BleDeviceListener listener : eventListeners) {
            try {
                switch (event) {
                    case SCAN_RECORD:
                        listener.onScanRecordReceived((BleScanNotification) args[0]);
                        break;
                    case CONNECTION_STATE:
                        listener.onConnectionStateChange((BleConnectionStatusNotification) args[0]);
                        break;
                    case SERVICES_DISCOVERED:
                        listener.onServicesDiscovered();
                        break;
                    case CHARACTERISTIC_READ_COMPLETE:
                        listener.onCharacteristicReadComplete((BleCharacteristic) args[0],
                                (BleCompletionStatus) args[1]);
                        break;
                    case CHARACTERISTIC_WRITE_COMPLETE:
                        listener.onCharacteristicWriteComplete((BleCharacteristic) args[0],
                                (BleCompletionStatus) args[1]);
                        break;
                    case CHARACTERISTIC_UPDATED:
                        listener.onCharacteristicUpdate((BleCharacteristic) args[0]);
                        break;
                }

            } catch (Throwable throwable) {
                logger.error("BLE could not inform the listener '" + listener + "' : " + throwable.getMessage(),
                        throwable);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BleDevice [address=");
        builder.append(address);
        builder.append(", name=");
        builder.append(name);
        builder.append(", rssi=");
        builder.append(rssi);
        builder.append(", manufacturer=");
        if (manufacturerName != null) {
            builder.append('(');
            builder.append(manufacturerName);
            builder.append(')');
        }
        builder.append(manufacturer);
        builder.append(']');
        return builder.toString();
    }
}
