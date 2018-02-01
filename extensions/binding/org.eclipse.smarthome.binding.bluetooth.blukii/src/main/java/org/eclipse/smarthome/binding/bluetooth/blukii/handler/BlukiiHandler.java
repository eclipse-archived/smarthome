/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.binding.bluetooth.blukii.handler;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.binding.bluetooth.BluetoothCharacteristic;
import org.eclipse.smarthome.binding.bluetooth.BluetoothDeviceListener;
import org.eclipse.smarthome.binding.bluetooth.ConnectedBluetoothHandler;
import org.eclipse.smarthome.binding.bluetooth.blukii.BlukiiBindingConstants;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BlukiiHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public class BlukiiHandler extends ConnectedBluetoothHandler implements BluetoothDeviceListener {

    private final Logger logger = LoggerFactory.getLogger(BlukiiHandler.class);

    public BlukiiHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        super.handleCommand(channelUID, command);
        if (channelUID.getId().equals(BlukiiBindingConstants.CHANNEL_ID_ACCELREPORT) && command instanceof OnOffType) {
            BluetoothCharacteristic characteristic = device.getCharacteristic(BlukiiBindingConstants.CHAR_ACCEL_REPORT);
            int[] value = command == OnOffType.ON ? new int[] { 1 } : new int[] { 0 };
            characteristic.setValue(value);
            device.writeCharacteristic(characteristic);
        }
    }

    @Override
    public void onServicesDiscovered() {
        super.onServicesDiscovered();
        BluetoothCharacteristic xAccel = device.getCharacteristic(BlukiiBindingConstants.CHAR_ACCEL_X);
        BluetoothCharacteristic yAccel = device.getCharacteristic(BlukiiBindingConstants.CHAR_ACCEL_Y);
        BluetoothCharacteristic zAccel = device.getCharacteristic(BlukiiBindingConstants.CHAR_ACCEL_Z);
        activateChannel(xAccel, BlukiiBindingConstants.CHANNEL_TYPE_UID_ACCEL, BlukiiBindingConstants.CHANNEL_ID_X);
        activateChannel(yAccel, BlukiiBindingConstants.CHANNEL_TYPE_UID_ACCEL, BlukiiBindingConstants.CHANNEL_ID_Y);
        activateChannel(zAccel, BlukiiBindingConstants.CHANNEL_TYPE_UID_ACCEL, BlukiiBindingConstants.CHANNEL_ID_Z);
    }

    @Override
    public void onCharacteristicUpdate(@NonNull BluetoothCharacteristic characteristic) {
        super.onCharacteristicUpdate(characteristic);
        if (characteristic.getUuid().equals(BlukiiBindingConstants.CHAR_ACCEL_REPORT)) {
            OnOffType enabled = characteristic.getValue()[0] == 1 ? OnOffType.ON : OnOffType.OFF;
            updateState(BlukiiBindingConstants.CHANNEL_ID_ACCELREPORT, enabled);
        }
        if (characteristic.getUuid().equals(BlukiiBindingConstants.CHAR_ACCEL_X)) {
            Integer x = characteristic.getIntValue(BluetoothCharacteristic.FORMAT_SINT16, 0);
            updateState(BlukiiBindingConstants.CHANNEL_ID_X, new DecimalType(x));
        }
        if (characteristic.getUuid().equals(BlukiiBindingConstants.CHAR_ACCEL_Y)) {
            Integer y = characteristic.getIntValue(BluetoothCharacteristic.FORMAT_SINT16, 0);
            updateState(BlukiiBindingConstants.CHANNEL_ID_Y, new DecimalType(y));
        }
        if (characteristic.getUuid().equals(BlukiiBindingConstants.CHAR_ACCEL_Z)) {
            Integer z = characteristic.getIntValue(BluetoothCharacteristic.FORMAT_SINT16, 0);
            updateState(BlukiiBindingConstants.CHANNEL_ID_Z, new DecimalType(z));
        }
    }
}
