/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.ble.handler;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.binding.ble.BleBindingConstants;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGattCharacteristic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ParrotFlowerPowerThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * Note that the FlowerPower needs to be paired before communicating
 *
 * @author Chris Jackson - Initial Contribution
 */
public class ParrotFlowerPowerThingHandler extends BleBaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(ParrotFlowerPowerThingHandler.class);

    private final UUID UUID_PARROT_LIVE = UUID.fromString("39e1FA00-84a8-11e2-afba-0002a5d5c51b");

    private final UUID UUID_AIR_TEMPERATURE = UUID.fromString("39e1fa04-84a8-11e2-afba-0002a5d5c51b");
    private final UUID UUID_SOIL_FERTILISER = UUID.fromString("39e1fa05-84a8-11e2-afba-0002a5d5c51b");
    private final UUID UUID_SOIL_TEMPERATURE = UUID.fromString("39e1fa03-84a8-11e2-afba-0002a5d5c51b");
    private final UUID UUID_SOIL_MOISTURE = UUID.fromString("39e1fa02-84a8-11e2-afba-0002a5d5c51b");
    private final UUID UUID_LUMINANCE = UUID.fromString("39e1fa01-84a8-11e2-afba-0002a5d5c51b");
    private final UUID UUID_LED_STATE = UUID.fromString("39e1fa07-84a8-11e2-afba-0002a5d5c51b");

    private final BigDecimal CHARACTERISTIC_SCALE = new BigDecimal(0.033);

    private ScheduledFuture<?> pollingJob;
    boolean initialised = false;

    public ParrotFlowerPowerThingHandler(Thing thing) {
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

        characteristic = gattClient.getCharacteristic(UUID_PARROT_LIVE);
        if (characteristic == null) {
            logger.debug("Unable to find control characteristic!");
            return;
        }

        logger.debug("FlowerPower conversion: {} to \"{}\"", command, value);

        characteristic.setValue(value);
        gattClient.writeCharacteristic(characteristic);
    }

    @Override
    public void initialize() {
        super.initialize();

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public boolean handleReceivedCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid().equals(UUID_AIR_TEMPERATURE)) {
            DecimalType level = new DecimalType(
                    BigDecimal.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0))
                            .multiply(CHARACTERISTIC_SCALE));
            updateState(new ChannelUID(getThing().getUID(), BleBindingConstants.CHANNEL_AIR_TEMPERATURE), level);
        } else if (characteristic.getUuid().equals(UUID_SOIL_TEMPERATURE)) {
            DecimalType level = new DecimalType(
                    BigDecimal.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0))
                            .multiply(CHARACTERISTIC_SCALE));
            updateState(new ChannelUID(getThing().getUID(), BleBindingConstants.CHANNEL_SOIL_TEMPERATURE), level);
        } else if (characteristic.getUuid().equals(UUID_SOIL_FERTILISER)) {
            DecimalType level = new DecimalType(
                    BigDecimal.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0))
                            .multiply(CHARACTERISTIC_SCALE));
            updateState(new ChannelUID(getThing().getUID(), BleBindingConstants.CHANNEL_SOIL_FERTILISER), level);
        } else if (characteristic.getUuid().equals(UUID_SOIL_MOISTURE)) {
            DecimalType level = new DecimalType(
                    BigDecimal.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0))
                            .multiply(CHARACTERISTIC_SCALE));
            updateState(new ChannelUID(getThing().getUID(), BleBindingConstants.CHANNEL_SOIL_MOISTURE), level);
        } else if (characteristic.getUuid().equals(UUID_LUMINANCE)) {
            DecimalType level = new DecimalType(
                    BigDecimal.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0)));
            updateState(new ChannelUID(getThing().getUID(), BleBindingConstants.CHANNEL_LUMINANCE), level);
        } else if (characteristic.getUuid().equals(UUID_LED_STATE)) {
        }

        return true;
    }

    @Override
    protected void handleConnectionStateChange(int status, int newState) {
        if (initialised == true) {
            return;
        }

        logger.debug("Initialising Parrot FlowerPower");

        initialised = true;
        logger.debug("Initialising Parrot FlowerPower {}", device.getName());

        BluetoothGattCharacteristic characteristic;
        characteristic = gattClient.getCharacteristic(UUID_AIR_TEMPERATURE);
        if (characteristic != null) {
            gattClient.setCharacteristicNotification(characteristic, true);
        }
        characteristic = gattClient
                .getCharacteristic(BluetoothGattCharacteristic.GattCharacteristic.BATTERY_LEVEL.getUUID());
        if (characteristic != null) {
            gattClient.setCharacteristicNotification(characteristic, true);
        }
        characteristic = gattClient.getCharacteristic(UUID_SOIL_TEMPERATURE);
        if (characteristic != null) {
            gattClient.setCharacteristicNotification(characteristic, true);
        }
        characteristic = gattClient.getCharacteristic(UUID_SOIL_MOISTURE);
        if (characteristic != null) {
            gattClient.setCharacteristicNotification(characteristic, true);
        }
        characteristic = gattClient.getCharacteristic(UUID_SOIL_FERTILISER);
        if (characteristic != null) {
            gattClient.setCharacteristicNotification(characteristic, true);
        }
        characteristic = gattClient.getCharacteristic(UUID_LUMINANCE);
        if (characteristic != null) {
            gattClient.setCharacteristicNotification(characteristic, true);
        }
    }

    @Override
    protected void handleInitialisation() {
        gattClient.connect();

        // The polling task runs every minute (by default).
        // Some data doesn't update quickly, so to keep requests down, and improve battery
        // we increase the period between requests on these channels
        Runnable pollingRunnable = new Runnable() {
            private int counter = 9999;
            private int battery = 9999;

            @Override
            public void run() {
                BluetoothGattCharacteristic characteristic;
                characteristic = gattClient.getCharacteristic(UUID_AIR_TEMPERATURE);
                if (characteristic != null) {
                    gattClient.readCharacteristic(characteristic);
                }
                characteristic = gattClient.getCharacteristic(UUID_LUMINANCE);
                if (characteristic != null) {
                    gattClient.readCharacteristic(characteristic);
                }

                if (counter++ >= 10) {
                    characteristic = gattClient.getCharacteristic(UUID_SOIL_TEMPERATURE);
                    if (characteristic != null) {
                        gattClient.readCharacteristic(characteristic);
                    }
                    characteristic = gattClient.getCharacteristic(UUID_SOIL_MOISTURE);
                    if (characteristic != null) {
                        gattClient.readCharacteristic(characteristic);
                    }
                    characteristic = gattClient.getCharacteristic(UUID_SOIL_FERTILISER);
                    if (characteristic != null) {
                        gattClient.readCharacteristic(characteristic);
                    }

                    counter = 0;
                }

                if (battery++ >= 1440) {
                    characteristic = gattClient
                            .getCharacteristic(BluetoothGattCharacteristic.GattCharacteristic.BATTERY_LEVEL.getUUID());
                    if (characteristic != null) {
                        gattClient.readCharacteristic(characteristic);
                    }

                    battery = 0;
                }
            }
        };

        pollingJob = scheduler.scheduleAtFixedRate(pollingRunnable, 15, 60, TimeUnit.SECONDS);
    }

}
