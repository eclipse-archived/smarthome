/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.ble.handler;

import java.util.UUID;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGattCharacteristic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WiTEnergyThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Chris Jackson - Initial Contribution
 */
public class WiTEnergyThingHandler extends BleBaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(WiTEnergyThingHandler.class);

    private final UUID UUID_WITECH_CONTROL = UUID.fromString("0000fee3-494c-4f47-4943-544543480000");
    private final UUID UUID_WITECH_STATUS = UUID.fromString("0000fee2-494c-4f47-4943-544543480000");

    public WiTEnergyThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        BluetoothGattCharacteristic characteristic = null;
        byte value[] = new byte[1];

        if (channelUID.getId().equals("switch-1")) {
            if (command instanceof OnOffType) {
                if (((OnOffType) command).equals(OnOffType.ON)) {
                    value[0] = -1;
                } else {
                    value[0] = 0;
                }
            }
        }

        characteristic = gattClient.getCharacteristic(UUID_WITECH_CONTROL);
        if (characteristic == null) {
            logger.debug("Unable to find control characteristic!");
            return;
        }

        logger.debug("WiT conversion: {} to \"{}\"", command, value);

        characteristic.setValue(value);
        gattClient.writeCharacteristic(characteristic);
    }

    @Override
    public void initialize() {
        super.initialize();
        updateStatus(ThingStatus.ONLINE);
    }
}
