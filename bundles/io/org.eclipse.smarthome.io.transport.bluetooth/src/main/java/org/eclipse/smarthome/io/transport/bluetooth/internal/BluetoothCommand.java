/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.bluetooth.internal;

import java.util.Comparator;
import java.util.UUID;

import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGattCharacteristic;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGattDescriptor;

/**
 * This class is used to store bluetooth GATT requests in a queue.
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class BluetoothCommand {
    // Command types.
    // Note that these are in priority order
    public enum CommandType {
        CHARACTERISTIC_WRITE,
        CHARACTERISTIC_READ,
        DESCRIPTOR_WRITE,
        DESCRIPTOR_READ
    }

    private CommandType commandType;
    private BluetoothGattDescriptor descriptor;
    private BluetoothGattCharacteristic characteristic;

    public BluetoothCommand(CommandType commandType, BluetoothGattDescriptor descriptor) {
        this.commandType = commandType;
        this.descriptor = descriptor;
    }

    public BluetoothCommand(CommandType commandType, BluetoothGattCharacteristic characteristic) {
        this.commandType = commandType;
        this.characteristic = characteristic;
    }

    public static class PriorityComparator implements Comparator<BluetoothCommand> {

        /**
         * This method ensures that the CHARACTERISTIC_WRITE messages have highest priority
         */
        @Override
        public int compare(BluetoothCommand arg0, BluetoothCommand arg1) {
            return arg0.commandType.compareTo(arg1.commandType);
        }

    }

    public CommandType getMessageType() {
        return commandType;
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return characteristic;
    }

    public BluetoothGattDescriptor getDescriptor() {
        return descriptor;
    }

    public UUID getUuid() {
        if (characteristic != null) {
            return characteristic.getUuid();
        } else {
            return descriptor.getUuid();
        }
    }

    @Override
    public boolean equals(Object object) {
        BluetoothCommand cmd = (BluetoothCommand) object;

        if (this.commandType != cmd.commandType) {
            return false;
        }

        if (this.getUuid().equals(cmd.getUuid())) {
            return true;
        }

        return false;
    }

}