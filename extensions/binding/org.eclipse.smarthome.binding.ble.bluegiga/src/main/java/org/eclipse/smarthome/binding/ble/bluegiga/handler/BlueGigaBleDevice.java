/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.ble.bluegiga.handler;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.binding.ble.BleCharacteristic;
import org.eclipse.smarthome.binding.ble.BleCompletionStatus;
import org.eclipse.smarthome.binding.ble.BleDevice;
import org.eclipse.smarthome.binding.ble.BleService;
import org.eclipse.smarthome.binding.ble.BluetoothAddress;
import org.eclipse.smarthome.binding.ble.notification.BleConnectionStatusNotification;
import org.eclipse.smarthome.binding.ble.notification.BleScanNotification;
import org.eclipse.smarthome.binding.ble.notification.BleScanNotification.BleBeaconType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zsmartsystems.bluetooth.bluegiga.BlueGigaEventListener;
import com.zsmartsystems.bluetooth.bluegiga.BlueGigaResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaAttributeValueEvent;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaFindInformationFoundEvent;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaGroupFoundEvent;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaProcedureCompletedEvent;
import com.zsmartsystems.bluetooth.bluegiga.command.connection.BlueGigaConnectionStatusEvent;
import com.zsmartsystems.bluetooth.bluegiga.command.connection.BlueGigaDisconnectedEvent;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaScanResponseEvent;
import com.zsmartsystems.bluetooth.bluegiga.eir.EirDataType;
import com.zsmartsystems.bluetooth.bluegiga.eir.EirPacket;
import com.zsmartsystems.bluetooth.bluegiga.enumeration.BgApiResponse;
import com.zsmartsystems.bluetooth.bluegiga.enumeration.BluetoothAddressType;
import com.zsmartsystems.bluetooth.bluegiga.enumeration.ConnectionStatusFlag;
import com.zsmartsystems.bluetooth.bluegiga.enumeration.ScanResponseType;

/**
 * An extended {@link BleDevice} class to handle BlueGiga specific information
 *
 * @author Chris Jackson - Initial contribution
 */
public class BlueGigaBleDevice extends BleDevice implements BlueGigaEventListener {
    private final Logger logger = LoggerFactory.getLogger(BlueGigaBleDevice.class);

    // BlueGiga needs to know the address type when connecting
    private BluetoothAddressType addressType;

    // Used to correlate the scans so we get as much information as possible before calling the device "discovered"
    private Set<ScanResponseType> scanResponses = new HashSet<ScanResponseType>();

    // The dongle handler
    private BlueGigaBridgeHandler bgHandler;

    // An enum to use in the state machine for interacting with the device
    private enum BlueGigaProcedure {
        NONE,
        GET_SERVICES,
        GET_CHARACTERISTICS,
        CHARACTERISTIC_READ,
        CHARACTERISTIC_WRITE;
    }

    private BlueGigaProcedure procedureProgress = BlueGigaProcedure.NONE;

    // Somewhere to remember what characteristic we're working on
    private BleCharacteristic procedureCharacteristic = null;

    // The connection handle if the device is connected
    private int connection = -1;

    /**
     * Creates a new {@link BlueGigaBleDevice} which extends {@link BleDevice} for the BlueGiga implementation
     *
     * @param bgHandler the {@link BlueGigaBridgeHandler} that provides the link to the dongle
     * @param address the {@link BluetoothAddress} for this device
     * @param addressType the {@link BluetoothAddressType} of this device
     */
    public BlueGigaBleDevice(BlueGigaBridgeHandler bgHandler, BluetoothAddress address,
            BluetoothAddressType addressType) {
        super(address);

        logger.debug("Creating new BlueGiga device {}", address);

        this.bgHandler = bgHandler;
        this.addressType = addressType;

        bgHandler.addEventListener(this);
    }

    @Override
    public boolean connect() {
        if (connection != -1) {
            // We're already connected
            return false;
        }

        if (bgHandler.bgConnect(address, addressType)) {
            connectionState = ConnectionState.CONNECTING;
            return true;
        } else {
            connectionState = ConnectionState.DISCONNECTED;
            return false;
        }
    }

    @Override
    public boolean disconnect() {
        if (connection == -1) {
            // We're already disconnected
            return false;
        }

        return bgHandler.bgDisconnect(connection);
    }

    @Override
    public boolean discoverServices() {
        // Start by requesting all the services
        procedureProgress = BlueGigaProcedure.GET_SERVICES;
        return bgHandler.bgFindPrimaryServices(connection);
    }

    @Override
    public boolean readCharacteristic(BleCharacteristic characteristic) {
        if (characteristic == null || characteristic.getHandle() == 0) {
            return false;
        }

        if (!bgHandler.bgReadCharacteristic(connection, characteristic.getHandle())) {
            return false;
        }

        if (procedureProgress != BlueGigaProcedure.NONE) {
            return false;
        }

        procedureProgress = BlueGigaProcedure.CHARACTERISTIC_READ;
        procedureCharacteristic = characteristic;

        return true;
    }

    @Override
    public boolean writeCharacteristic(BleCharacteristic characteristic) {
        if (characteristic == null || characteristic.getHandle() == 0) {
            return false;
        }

        if (procedureProgress != BlueGigaProcedure.NONE) {
            return false;
        }

        if (!bgHandler.bgWriteCharacteristic(connection, characteristic.getHandle(), characteristic.getValue())) {
            return false;
        }

        procedureProgress = BlueGigaProcedure.CHARACTERISTIC_WRITE;
        procedureCharacteristic = characteristic;

        return true;
    }

    @Override
    public void bluegigaEventReceived(BlueGigaResponse event) {
        if (event instanceof BlueGigaScanResponseEvent) {
            BlueGigaScanResponseEvent scanEvent = (BlueGigaScanResponseEvent) event;

            // Check if this is addressed to this device
            if (!address.equals(new BluetoothAddress(scanEvent.getSender()))) {
                return;
            }

            // Set device properties
            rssi = scanEvent.getRssi();
            addressType = scanEvent.getAddressType();

            int[] manufacturerData = null;

            // If the packet contains data, then process it and add anything relevant to the device...
            if (scanEvent.getData() != null) {
                EirPacket eir = new EirPacket(scanEvent.getData());
                for (EirDataType record : eir.getRecords().keySet()) {
                    switch (record) {
                        case EIR_FLAGS:
                            break;
                        case EIR_MANUFACTURER_SPECIFIC:
                            manufacturerData = (int[]) eir.getRecord(EirDataType.EIR_MANUFACTURER_SPECIFIC);
                            if (manufacturerData.length > 2) {
                                int id = manufacturerData[0] + (manufacturerData[1] << 8);
                                manufacturer = id;
                            }
                            break;
                        case EIR_NAME_LONG:
                        case EIR_NAME_SHORT:
                            name = (String) eir.getRecord(record);
                            break;
                        case EIR_SLAVEINTERVALRANGE:
                            break;
                        case EIR_SVC_DATA_UUID128:
                            break;
                        case EIR_SVC_DATA_UUID16:
                            break;
                        case EIR_SVC_DATA_UUID32:
                            break;
                        case EIR_SVC_UUID128_INCOMPLETE:
                        case EIR_SVC_UUID16_COMPLETE:
                        case EIR_SVC_UUID16_INCOMPLETE:
                        case EIR_SVC_UUID32_COMPLETE:
                        case EIR_SVC_UUID32_INCOMPLETE:
                        case EIR_SVC_UUID128_COMPLETE:
                            // addServices((List<UUID>) eir.getRecord(record));
                            break;
                        case EIR_TXPOWER:
                            txPower = (int) eir.getRecord(EirDataType.EIR_TXPOWER);
                            break;
                        default:
                            break;
                    }
                }
            }

            if (connectionState == ConnectionState.DISCOVERING) {
                // We want to wait for an advertisement and a scan response before we call this discovered.
                // The intention is to gather a reasonable amount of data about the device given devices send
                // different data in different packets...
                // Note that this is possible a bit arbitrary and may be refined later.
                scanResponses.add(scanEvent.getPacketType());

                if ((scanResponses.contains(ScanResponseType.CONNECTABLE_ADVERTISEMENT)
                        || scanResponses.contains(ScanResponseType.DISCOVERABLE_ADVERTISEMENT)
                        || scanResponses.contains(ScanResponseType.NON_CONNECTABLE_ADVERTISEMENT))
                        && scanResponses.contains(ScanResponseType.SCAN_RESPONSE)) {
                    // Set our state to disconnected
                    connectionState = ConnectionState.DISCONNECTED;
                    connection = -1;

                    // But notify listeners that the state is now DISCOVERED
                    notifyListeners(BleEventType.CONNECTION_STATE,
                            new BleConnectionStatusNotification(ConnectionState.DISCOVERED));

                    // Notify the bridge - for inbox notifications
                    bgHandler.deviceDiscovered(this);
                }
            }

            // Notify listeners of all scan records - for RSSI, beacon processing (etc)
            BleScanNotification scanNotification = new BleScanNotification();
            scanNotification.setRssi(scanEvent.getRssi());

            switch (scanEvent.getPacketType()) {
                case CONNECTABLE_ADVERTISEMENT:
                case DISCOVERABLE_ADVERTISEMENT:
                case NON_CONNECTABLE_ADVERTISEMENT:
                    scanNotification.setBeaconType(BleBeaconType.BEACON_ADVERTISEMENT);
                    break;
                case SCAN_RESPONSE:
                    scanNotification.setBeaconType(BleBeaconType.BEACON_SCANRESPONSE);
                    break;
                default:
                    break;
            }

            if (manufacturerData != null) {
                scanNotification.setManufacturerData(manufacturerData);
            }

            notifyListeners(BleEventType.SCAN_RECORD, scanNotification);

            return;
        }

        if (event instanceof BlueGigaGroupFoundEvent) {
            // A Service has been discovered
            BlueGigaGroupFoundEvent serviceEvent = (BlueGigaGroupFoundEvent) event;

            // If this is not our connection handle then ignore.
            if (connection != serviceEvent.getConnection()) {
                return;
            }

            logger.debug("BlueGiga Group: {} svcs={}", this, supportedServices);

            BleService service = new BleService(serviceEvent.getUuid(), true, serviceEvent.getStart(),
                    serviceEvent.getEnd());
            addService(service);

            return;
        }

        if (event instanceof BlueGigaFindInformationFoundEvent) {
            // A Characteristic has been discovered
            BlueGigaFindInformationFoundEvent infoEvent = (BlueGigaFindInformationFoundEvent) event;

            // If this is not our connection handle then ignore.
            if (connection != infoEvent.getConnection()) {
                return;
            }

            logger.debug("BlueGiga FindInfo: {} svcs={}", this, supportedServices);

            BleCharacteristic characteristic = new BleCharacteristic(infoEvent.getUuid(), infoEvent.getChrHandle());

            BleService service = getServiceByHandle(characteristic.getHandle());
            if (service == null) {
                logger.debug("BlueGiga: Unable to find service for handle {}", characteristic.getHandle());
                return;
            }
            characteristic.setService(service);
            service.addCharacteristic(characteristic);

            return;
        }

        if (event instanceof BlueGigaProcedureCompletedEvent) {
            BlueGigaProcedureCompletedEvent completedEvent = (BlueGigaProcedureCompletedEvent) event;

            // If this is not our connection handle then ignore.
            if (connection != completedEvent.getConnection()) {
                return;
            }

            if (procedureProgress == null) {
                logger.debug("BlueGiga procedure completed but procedure is null with connection {}, address {}",
                        connection, address);
                return;
            }

            // The current procedure is now complete - move on...
            switch (procedureProgress) {
                case GET_SERVICES:
                    // We've downloaded all services, now get the characteristics
                    procedureProgress = BlueGigaProcedure.GET_CHARACTERISTICS;
                    bgHandler.bgFindCharacteristics(connection);
                    break;
                case GET_CHARACTERISTICS:
                    // We've downloaded all characteristics
                    procedureProgress = BlueGigaProcedure.NONE;
                    notifyListeners(BleEventType.SERVICES_DISCOVERED);
                    break;
                case CHARACTERISTIC_READ:
                    // The read failed
                    notifyListeners(BleEventType.CHARACTERISTIC_READ_COMPLETE, procedureCharacteristic,
                            BleCompletionStatus.ERROR);
                    procedureProgress = BlueGigaProcedure.NONE;
                    procedureCharacteristic = null;
                    break;
                case CHARACTERISTIC_WRITE:
                    // The write completed - failure or success
                    BleCompletionStatus result = completedEvent.getResult() == BgApiResponse.SUCCESS
                            ? BleCompletionStatus.SUCCESS : BleCompletionStatus.ERROR;
                    notifyListeners(BleEventType.CHARACTERISTIC_WRITE_COMPLETE, procedureCharacteristic, result);
                    procedureProgress = BlueGigaProcedure.NONE;
                    procedureCharacteristic = null;
                    break;
                default:
                    break;
            }

            return;
        }

        if (event instanceof BlueGigaConnectionStatusEvent) {
            BlueGigaConnectionStatusEvent connectionEvent = (BlueGigaConnectionStatusEvent) event;

            // Check if this is addressed to this device
            if (!address.equals(new BluetoothAddress(connectionEvent.getAddress()))) {
                return;
            }

            // If we're connected, then remember the connection handle
            if (connectionEvent.getFlags().contains(ConnectionStatusFlag.CONNECTION_CONNECTED)) {
                connectionState = ConnectionState.CONNECTED;
                connection = connectionEvent.getConnection();
            }

            if (connectionEvent.getFlags().contains(ConnectionStatusFlag.CONNECTION_CONNECTED)) {
                notifyListeners(BleEventType.CONNECTION_STATE, new BleConnectionStatusNotification(connectionState));
            }

            return;
        }

        if (event instanceof BlueGigaDisconnectedEvent) {
            BlueGigaDisconnectedEvent disconnectedEvent = (BlueGigaDisconnectedEvent) event;

            // If this is not our connection handle then ignore.
            if (connection != disconnectedEvent.getConnection()) {
                return;
            }

            connectionState = ConnectionState.DISCONNECTED;
            connection = -1;
            notifyListeners(BleEventType.CONNECTION_STATE, new BleConnectionStatusNotification(connectionState));

            return;
        }

        if (event instanceof BlueGigaAttributeValueEvent) {
            // A read request has completed - update the characteristic
            BlueGigaAttributeValueEvent valueEvent = (BlueGigaAttributeValueEvent) event;

            BleCharacteristic characteristic = getCharacteristicByHandle(valueEvent.getAttHandle());
            if (characteristic == null) {
                logger.debug("BlueGiga didn't find characteristic for event {}", event);
            }

            // If this is the characteristic we were reading, then send a read completion
            if (procedureProgress == BlueGigaProcedure.CHARACTERISTIC_READ && procedureCharacteristic != null
                    && procedureCharacteristic.getHandle() == valueEvent.getAttHandle()) {
                procedureProgress = BlueGigaProcedure.NONE;
                procedureCharacteristic = null;
                notifyListeners(BleEventType.CHARACTERISTIC_READ_COMPLETE, procedureCharacteristic,
                        BleCompletionStatus.SUCCESS);
            }

            // Notify the user of the updated value
            if (characteristic != null) {
                notifyListeners(BleEventType.CHARACTERISTIC_UPDATED, procedureCharacteristic);
            }
        }
    }
}
