/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.bluetooth.yeelightblue.handler;

import java.util.UUID;

import org.eclipse.smarthome.binding.bluetooth.BluetoothCharacteristic;
import org.eclipse.smarthome.binding.bluetooth.BluetoothCompletionStatus;
import org.eclipse.smarthome.binding.bluetooth.BluetoothDeviceListener;
import org.eclipse.smarthome.binding.bluetooth.ConnectedBluetoothHandler;
import org.eclipse.smarthome.binding.bluetooth.yeelightblue.YeelightBlueBindingConstants;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link YeelightBlueHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Chris Jackson - Initial contribution
 * @author Kai Kreuzer - Refactored to extend {@link ConnectedBluetoothHandler}.
 */
public class YeelightBlueHandler extends ConnectedBluetoothHandler implements BluetoothDeviceListener {

    private final Logger logger = LoggerFactory.getLogger(YeelightBlueHandler.class);

    private final UUID UUID_YEELIGHT_CONTROL = UUID.fromString("0000fff1-0000-0000-0000-000000000000");
    private final UUID UUID_YEELIGHT_STATUS_REQUEST = UUID.fromString("0000fff5-0000-0000-0000-000000000000");
    private final UUID UUID_YEELIGHT_STATUS_RESPONSE = UUID.fromString("0000fff6-0000-0000-0000-000000000000");

    // The characteristics we regularly use
    private BluetoothCharacteristic characteristicControl = null;
    private BluetoothCharacteristic characteristicRequest = null;

    public YeelightBlueHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        device.connect();
        device.discoverServices();
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
    public void onCharacteristicWriteComplete(BluetoothCharacteristic characteristic,
            BluetoothCompletionStatus status) {
        // If this was a write to the control, then read back the state
        if (characteristic.getUuid().equals(UUID_YEELIGHT_CONTROL)) {
            readStatus();
        }
    }

    @Override
    public void onCharacteristicUpdate(BluetoothCharacteristic characteristic) {
        if (characteristic.getUuid().equals(UUID_YEELIGHT_STATUS_RESPONSE)) {
            String value = characteristic.getStringValue(0);
            logger.debug("Yeelight status update is \"{}\"", value);

            String[] elements = value.split(",");

            int red, green, blue;
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

            HSBType hsbState = HSBType.fromRGB(red, green, blue);

            updateState(new ChannelUID(getThing().getUID(), YeelightBlueBindingConstants.CHANNEL_COLOR), hsbState);
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
