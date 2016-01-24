/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.ble.handler;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.binding.ble.BleBindingConstants;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothAdapter;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothDevice;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGatt;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGattCallback;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGattCharacteristic;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGattDescriptor;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGattService;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothManager;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothManufacturer;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BleBaseThingHandler} is a base implementation for bluetooth devices.
 *
 * @author Chris Jackson - Initial Contribution
 */
public abstract class BleBaseThingHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(BleBaseThingHandler.class);

    protected BluetoothAdapter adapter;
    protected BluetoothDevice device;
    protected String address;

    protected BluetoothGatt gattClient;
    protected BluetoothGattCallback gattCallback;

    private boolean servicesDiscoveryRequested = false;
    private boolean characteristicDiscoveryRequested = false;
    private boolean initialisationCalled = false;

    private ScheduledFuture<?> rssiPollingJob;

    public BleBaseThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNINITIALIZED);

        address = ((String) getConfig().get(BleBindingConstants.PROPERTY_ADDRESS));
        if (address == null) {
            logger.error("Property 'Address' is not set for {}", getThing().getUID());
            return;
        }

        // Open the adapter
        adapter = BluetoothManager.getDefaultAdapter();
        if (adapter == null) {
            logger.error("Unable to get default Bluetooth adapter");
            return;
        }

        device = adapter.getRemoteDevice(address);
        if (device == null) {
            logger.error("Unable to get Bluetooth device {}", address);
            return;
        }

        logger.debug("Bluetooth BLE device initialised\n" + dumpServices());

        gattCallback = new GattCallback();
        gattClient = device.connectGatt(true, gattCallback);
        if (gattClient == null) {
            logger.error("Unable to connect to GATT device {}", address);
            return;
        }

        logger.debug("Bluetooth BLE GATT initialised\n{}", dumpServices());

        if (gattClient.connect() == false) {
            logger.debug("Error connecting to {}", address);
        }

        Runnable pollingRunnable = new Runnable() {
            @Override
            public void run() {
                gattClient.readRemoteRssi();
            }
        };

        rssiPollingJob = scheduler.scheduleAtFixedRate(pollingRunnable, 15, 30, TimeUnit.SECONDS);
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        Configuration configuration = editConfiguration();
        for (Entry<String, Object> configurationParameter : configurationParameters.entrySet()) {
            if (configurationParameter.getKey().equals("pairing")) {
            }
            if (configurationParameter.getKey().equals("discovery")) {
            }
            if (configurationParameter.getKey().equals("pin")) {
                configuration.put(configurationParameter.getKey(), configurationParameter.getValue());
            }
        }
    }

    private String makePrintable(byte[] value) {
        String stringValue = "";
        if (value != null) {
            boolean printable = true;
            int len = value.length;
            int cnt = 0;
            for (int val : value) {
                if (val < 0) {
                    val &= 0xff;
                } else if (val < 16) {
                    stringValue += "0";
                }
                stringValue += Integer.toHexString(val);
                stringValue += " ";

                cnt++;
                if ((val < 32 || val > 127) && (val != 0 && cnt != len)) {
                    printable = false;
                }
            }

            stringValue = stringValue.toUpperCase();

            if (printable == true) {
                stringValue += "  ==  \"" + new String(value, StandardCharsets.UTF_8) + "\"";
            }
        }
        return stringValue;
    }

    private String dumpServices() {
        // Dump a list of services and characteristics
        StringBuilder strOutput = new StringBuilder();
        strOutput.append("Address      : " + device.getAddress() + "\n");
        strOutput.append("Name         : " + device.getName());
        BluetoothManufacturer manufacturer = BluetoothManufacturer.getManufacturer(device.getManufacturer());
        if (manufacturer != null) {
            strOutput.append("  " + manufacturer.getLabel());
        }
        strOutput.append("\n");
        strOutput.append("Manufacturer : " + device.getManufacturer() + "\n");
        strOutput.append("Type         : " + device.getType() + "\n");

        if (gattClient == null) {
            return strOutput.toString();
        }

        strOutput.append("Services     : " + gattClient.getServices().size() + "\n");

        BluetoothGattService.GattService typeService;
        BluetoothGattCharacteristic.GattCharacteristic typeCharacteristic;
        BluetoothGattDescriptor.GattDescriptor typeDescriptor;
        String svcString;
        String stringValue;
        String strProperties;
        for (BluetoothGattService service : gattClient.getServices()) {
            typeService = service.getService();
            svcString = "";
            if (typeService != null) {
                svcString = typeService.toString();
            }
            strOutput.append("   | Service UUID : " + service.getUuid() + "  " + svcString);
            strOutput.append("   | Service Type : "
                    + (service.getType() == BluetoothGattService.SERVICE_TYPE_PRIMARY ? "Primary" : "Secondary")
                    + "\n");
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                typeCharacteristic = characteristic.getCharacteristic();
                svcString = "";
                if (typeCharacteristic != null) {
                    svcString = typeCharacteristic.toString();
                }
                stringValue = makePrintable(characteristic.getValue());

                strProperties = "";
                if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
                    strProperties += "read ";
                }
                if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
                    strProperties += "write ";
                }
                if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                    strProperties += "notify ";
                }
                if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                    strProperties += "indicate ";
                }

                strOutput.append(
                        "      | Characteristic UUID     : " + characteristic.getUuid() + "  " + svcString + "\n");
                strOutput.append("      | Characteristic Property : " + strProperties + "\n");
                strOutput.append("      | Characteristic Value    : " + stringValue + "\n");

                for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                    typeDescriptor = descriptor.getDescriptor();
                    svcString = "";
                    if (typeDescriptor != null) {
                        svcString = typeDescriptor.toString();
                    }

                    stringValue = makePrintable(descriptor.getValue());

                    strOutput
                            .append("         | Descriptor UUID  : " + descriptor.getUuid() + "   " + svcString + "\n");
                    strOutput.append("         | Descriptor Value : " + stringValue + "\n");
                }
            }
        }

        return strOutput.toString();
    }

    private void processStandardCharacteristics(BluetoothGattCharacteristic characteristic) {
        // Process any common characteristics
        if (characteristic.getUuid().equals(BluetoothGattCharacteristic.GattCharacteristic.BATTERY_LEVEL.getUUID())) {
            DecimalType level = new DecimalType(
                    characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
            updateState(new ChannelUID(getThing().getUID(), BleBindingConstants.CHANNEL_BATTERYLEVEL), level);
        }
    }

    class GattCallback extends BluetoothGattCallback {
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                return;
            }

            updateState(new ChannelUID(getThing().getUID(), BleBindingConstants.CHANNEL_RSSI),
                    new DecimalType(new BigDecimal(rssi)));
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                logger.debug("Connected to {}", address);
                if (servicesDiscoveryRequested == false && gattClient.discoverServices() == false) {
                    logger.error("Unable to start GATT discovery for {}", address);
                    return;
                }
                servicesDiscoveryRequested = true;
            } else {
                logger.debug("Disconnected from {}", address);
            }

            handleConnectionStateChange(status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                return;
            }

            // Only do this discovery search once!
            if (characteristicDiscoveryRequested == false) {
                characteristicDiscoveryRequested = true;

                logger.debug("Bluetooth BLE device services discovered for {}\n{}", address, dumpServices());

                // Request all characteristics and descriptors
                for (BluetoothGattService service : gattClient.getServices()) {
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
                            logger.debug("Requesting CHARACTERISTIC {} {}", address, characteristic.getUuid());
                            gatt.readCharacteristic(characteristic);
                        }
                        for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                            logger.debug("Requesting DESCRIPTOR {} {}", address, descriptor.getUuid());
                            gatt.readDescriptor(descriptor);
                        }
                    }
                }
            }

            handleServicesDiscovered(status);

            checkInitialisation();
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            // Process any common characteristics
            processStandardCharacteristics(characteristic);

            if (handleReceivedCharacteristic(characteristic) == false) {
                handleCharacteristicChanged(characteristic);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                logger.debug("Characteristic {} from {} has been read - value {}", characteristic.getUuid(), address,
                        characteristic.getValue());
            } else {
                logger.debug("Characteristic {} from {} has been read - ERROR", characteristic.getUuid(), address);
                return;
            }

            processStandardCharacteristics(characteristic);

            if (handleReceivedCharacteristic(characteristic) == false) {
                handleCharacteristicRead(characteristic, status);
            }

            checkInitialisation();
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            handleCharacteristicWrite(characteristic, status);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                logger.debug("Descriptor {} from {} has been read - value {}", descriptor.getUuid(), address,
                        descriptor.getValue());
            } else {
                logger.debug("Descriptor {} from {} has been read - ERROR", descriptor.getUuid(), address);
            }

            if (gatt.getQueueLength() == 0) {
                logger.debug("Queue is empty.\n{}", dumpServices());
            }

            handleDescriptorRead(descriptor, status);

            checkInitialisation();
        }
    }

    private void checkInitialisation() {
        if (characteristicDiscoveryRequested == false || initialisationCalled == true) {
            return;
        }

        if (gattClient.getQueueLength() != 0) {
            return;
        }

        logger.debug("Discovery Search Complete.\n{}", dumpServices());

        initialisationCalled = true;
        handleInitialisation();
    }

    // The following methods are called by the base class and should be overridden by classes extending the base class.

    /**
     *
     * @param status
     * @param newState
     */
    protected void handleConnectionStateChange(int status, int newState) {
    }

    protected void handleServicesDiscovered(int status) {
    }

    protected void handleCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
    }

    protected void handleCharacteristicRead(BluetoothGattCharacteristic characteristic, int status) {
    }

    protected void handleCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status) {
    }

    protected void handleDescriptorRead(BluetoothGattDescriptor descriptor, int status) {
    }

    /**
     * Called when characteristics are updated. The class should override this to process received characteristics
     * and update channels.
     *
     * @param characteristic
     * @return true if the class process this
     */
    protected boolean handleReceivedCharacteristic(BluetoothGattCharacteristic characteristic) {
        return false;
    }

    /**
     * Called after the base class has initialised the adapter etc. The class should perform any device initialisation
     * at this point.
     * This could include setting notifications etc.
     */
    protected void handleInitialisation() {
    }

}
