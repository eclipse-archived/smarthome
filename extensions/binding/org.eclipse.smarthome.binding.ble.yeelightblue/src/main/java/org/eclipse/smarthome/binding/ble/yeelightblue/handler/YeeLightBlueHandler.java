/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.ble.yeelightblue.handler;

import java.util.UUID;

import org.eclipse.smarthome.binding.ble.BleBindingConstants;
import org.eclipse.smarthome.binding.ble.BleBridgeApi;
import org.eclipse.smarthome.binding.ble.BleCharacteristic;
import org.eclipse.smarthome.binding.ble.BleCompletionStatus;
import org.eclipse.smarthome.binding.ble.BleDevice;
import org.eclipse.smarthome.binding.ble.BleDeviceListener;
import org.eclipse.smarthome.binding.ble.BluetoothAddress;
import org.eclipse.smarthome.binding.ble.notification.BleConnectionStatusNotification;
import org.eclipse.smarthome.binding.ble.notification.BleScanNotification;
import org.eclipse.smarthome.binding.ble.yeelightblue.YeeLightBlueBindingConstants;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link YeeLightBlueHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Chris Jackson - Initial contribution
 */
public class YeeLightBlueHandler extends BaseThingHandler implements BleDeviceListener {

    private final Logger logger = LoggerFactory.getLogger(YeeLightBlueHandler.class);

    private final UUID UUID_YEELIGHT_CONTROL = UUID.fromString("0000fff1-0000-0000-0000-000000000000");
    private final UUID UUID_YEELIGHT_STATUS_REQUEST = UUID.fromString("0000fff5-0000-0000-0000-000000000000");
    private final UUID UUID_YEELIGHT_STATUS_RESPONSE = UUID.fromString("0000fff6-0000-0000-0000-000000000000");

    // Remember the bridge - it's our link to the world
    private BleBridgeApi bleBridge = null;

    // Our BLE address
    private BluetoothAddress address;

    // Our device
    private BleDevice device = null;

    // The characteristics we regularly use
    private BleCharacteristic characteristicControl = null;
    private BleCharacteristic characteristicRequest = null;

    public YeeLightBlueHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        try {
            address = new BluetoothAddress((String) getConfig().get(BleBindingConstants.CONFIGURATION_ADDRESS));
        } catch (IllegalArgumentException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getLocalizedMessage());
            return;
        }

        logger.error("YeeLightBlue: Creating handler at address {}.", address);

        updateStatus(ThingStatus.ONLINE);

        Bridge bridge = getBridge();
        if (bridge != null) {
            bridgeStatusChanged(bridge.getStatusInfo());
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            // Bridge is offline
            return;
        }

        if (bleBridge != null) {
            return;
        }

        // Remember the bridge - it's our link to the BLE world
        bleBridge = (BleBridgeApi) getBridge().getHandler();

        device = bleBridge.getDevice(address);
        if (device == null) {
            logger.error("YeeLightBlue: device not found at address {}.", address);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }

        device.addListener(this);

        // if (device.connect() == false) {
        // logger.error("Error attempting to start connection to {}", device);
        // }
    }

    @Override
    public void dispose() {
        device.removeListener(this);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String value = null;

        if (command instanceof HSBType) {
            HSBType hsb = (HSBType) command;
            double r = hsb.getRed().doubleValue() * 2.55;
            double g = hsb.getGreen().doubleValue() * 2.55;
            double b = hsb.getBlue().doubleValue() * 2.55;
            double a = hsb.getSaturation().doubleValue();
            value = String.format("%.0f,%.0f,%.0f,%.0f", r, g, b, a);
        }

        else if (command instanceof PercentType) {
            value = ",,," + ((PercentType) command).intValue() + "";
        }

        else if (command instanceof OnOffType) {
            value = ",,," + ((OnOffType) command == OnOffType.ON ? 100 : 0) + "";
        }

        if (value == null) {
            logger.debug("Unable to convert value!");
            return;
        }

        if (characteristicControl == null) {
            logger.debug("YeeLightBlue: Unable to find control characteristic!");
            return;
        }

        // Terminate the value string with commas - up to 18 characters long
        for (int cnt = value.length(); cnt < 18; cnt++) {
            value += ",";
        }
        logger.debug("YeelightBlue conversion: {} to \"{}\"", command, value);

        characteristicControl.setValue(value);
        device.writeCharacteristic(characteristicControl);
    }

    @Override
    public void onScanRecordReceived(BleScanNotification scanNotification) {
        updateState(new ChannelUID(getThing().getUID(), BleBindingConstants.BLE_CHANNEL_RSSI),
                new DecimalType(scanNotification.getRssi()));
    }

    @Override
    public void onConnectionStateChange(BleConnectionStatusNotification connectionNotification) {
        logger.debug("YeeLightBlue: state changed to {}", connectionNotification.getConnectionState());

        switch (connectionNotification.getConnectionState()) {
            case DISCOVERED:
                // The device is now known on the BLE network, so we can do something...
                if (device.connect() == false) {
                    logger.debug("YeeLightBlue: Error attempting to connect after discovery");
                }
                break;
            case CONNECTED:
                if (device.discoverServices() == false) {
                    logger.debug("YeeLightBlue: Error attempting to discover services");
                }
                break;
            case DISCONNECTED:
                break;
            default:
                break;
        }
    }

    @Override
    public void onServicesDiscovered() {
        // Everything is initialised now - get the characteristics we want to use
        characteristicControl = device.getCharacteristic(UUID_YEELIGHT_CONTROL);
        if (characteristicControl == null) {
            logger.debug("YeeLightBlue control characteristic not known after service discovery!");
        }
        characteristicRequest = device.getCharacteristic(UUID_YEELIGHT_STATUS_REQUEST);
        if (characteristicRequest == null) {
            logger.debug("YeeLightBlue status characteristic not known after service discovery!");
        }

        // Read the current value so we can update the UI
        readStatus();
    }

    @Override
    public void onCharacteristicWriteComplete(BleCharacteristic characteristic, BleCompletionStatus status) {
        // If this was a write to the control, then read back the state
        if (characteristic.getUuid().equals(UUID_YEELIGHT_CONTROL) == true) {
            readStatus();
        }
    }

    @Override
    public void onCharacteristicReadComplete(BleCharacteristic characteristic, BleCompletionStatus status) {
        if (status == BleCompletionStatus.SUCCESS) {
            logger.debug("Characteristic {} from {} has been read - value {}", characteristic.getUuid(), address,
                    characteristic.getValue());
        } else {
            logger.debug("Characteristic {} from {} has been read - ERROR", characteristic.getUuid(), address);
            return;
        }
    }

    @Override
    public void onCharacteristicUpdate(BleCharacteristic characteristic) {
        if (characteristic.getUuid().equals(UUID_YEELIGHT_STATUS_RESPONSE)) {
            String value = characteristic.getStringValue(0);
            logger.debug("Yeelight status update is \"{}\"", value);

            String[] elements = value.split(",");

            float[] hsb = new float[3];
            int red, green, blue, light;
            try {
                red = Integer.parseInt(elements[0]);
            } catch (NumberFormatException e) {
                red = 0;
            }
            try {
                green = Integer.parseInt(elements[1]);
            } catch (NumberFormatException e) {
                green = 0;
            }
            try {
                blue = Integer.parseInt(elements[2]);
            } catch (NumberFormatException e) {
                blue = 0;
            }
            try {
                light = Integer.parseInt(elements[3]);
            } catch (NumberFormatException e) {
                light = 0;
            }

            HSBType hsbState = HSBType.fromRGB(red, green, blue);

            updateState(new ChannelUID(getThing().getUID(), YeeLightBlueBindingConstants.CHANNEL_COLOR), hsbState);
            updateState(new ChannelUID(getThing().getUID(), YeeLightBlueBindingConstants.CHANNEL_SWITCH),
                    light == 0 ? OnOffType.OFF : OnOffType.ON);
            updateState(new ChannelUID(getThing().getUID(), YeeLightBlueBindingConstants.CHANNEL_BRIGHTNESS),
                    new PercentType(light));
        }
    }

    private void readStatus() {
        if (characteristicRequest == null) {
            logger.debug("YeeLightBlue status characteristic not known");
            return;
        }

        characteristicRequest.setValue("S");
        device.writeCharacteristic(characteristicRequest);
    }
}
