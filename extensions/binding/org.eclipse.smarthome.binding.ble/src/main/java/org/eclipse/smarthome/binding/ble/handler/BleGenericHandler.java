/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.ble.handler;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.binding.ble.BleBindingConstants;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothAdapter;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothDevice;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGatt;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGattCharacteristic;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothGattService;
import org.eclipse.smarthome.io.transport.bluetooth.BluetoothManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BleGenericHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Chris Jackson - Initial Contribution
 */
public class BleGenericHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(BleGenericHandler.class);

    private BluetoothAdapter adapter;
    private BluetoothDevice device;
    private String address;

    // Move to handler
    private BluetoothGatt gattClient;
    private boolean stateDiscoveryComplete = false;
    // Map<UUID, Channel> channelMap = new HashMap<UUID, Channel>();
    // Map<ChannelUID, Map<DataType, BluetoothGattCharacteristic>> channelMap;

    public BleGenericHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNINITIALIZED);

        address = ((String) getConfig().get(BleBindingConstants.PROPERTY_ADDRESS));
        if (address == null) {
            logger.error("Property 'Address' is not set for {}", getThing().getUID());
            return;
        }

        adapter = BluetoothManager.getDefaultAdapter();
        if (adapter == null) {
            logger.error("Unable to get default Bluetooth adapter");
            return;
        }

        device = adapter.getRemoteDevice(address);
        if (device == null) {
            logger.error("Unable to get Bluetooth device for {}", address);
            return;
        }

        // Move...
        // gattClient = device.connectGatt(true, this);
        if (gattClient == null) {
            logger.error("Unable to connect to GATT device for {}", address);
            return;
        }

        // Request RSSI readings.
        // Note that we currently assume this enables continuous RSSI
        // This might be a wrong assumption - although Bluez does this (AFAICT)
        gattClient.readRemoteRssi();

        //
        //
        //
        /*
         * ThingBuilder thingBuilder = editThing();
         * Channel channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), "1"), "String").build();
         * thingBuilder.withChannel(channel);
         * updateThing(thingBuilder.build());
         *
         * updateStatus(ThingStatus.ONLINE);
         */
        //
        //
        //

        if (false) {// getThing().getChannels().size() == 0) {

            ThingBuilder thingBuilder = editThing();

            List<Channel> channels = new CopyOnWriteArrayList<>();
            ChannelTypeUID channelTypeUID = new ChannelTypeUID(BleBindingConstants.BINDING_ID, "rssi");
            Channel channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), "rssi"), "Number")
                    .withType(channelTypeUID).build();
            channels.add(channel);
            thingBuilder.withChannel(channel).withConfiguration(getConfig());
            updateThing(thingBuilder.build());

            // Thing me = getThing();
            // String id = me.getUID().getId();
            //
            // List<Channel> channels = new CopyOnWriteArrayList<>();
            // ChannelUID channelUID = new ChannelUID("bluetooth:bluetooth_generic:" + id);
            // ChannelUID channelUID = new ChannelUID(me.getUID(), "rssi");
            // Channel channel = ChannelBuilder.create(channelUID, "Number").build();

            // List<Channel> channels = new CopyOnWriteArrayList<>();
            // channels.add(channel);

            // Map<String, String> properties = new HashMap<>(2);
            // properties.put(BluetoothBindingConstants.PROPERTY_ADDRESS, address);

            // Thing newThing =
            // ThingBuilder.create(me.getUID()).withChannels(channels).withConfiguration(getConfig()).build();

            // updateThing(newThing);
        }

        // Do a scan of known services and add anything we know about to this things channels
        List<BluetoothGattService> gattServices = gattClient.getServices();
        for (BluetoothGattService service : gattServices) {

        }

        gattClient.connect();
    }

    @Override
    public void dispose() {
        if (gattClient != null) {
            gattClient.close();
        }
        // if(BluetoothAdapter != null) {
        // BluetoothAdapter
        // }
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        // Don't configure if we don't have the device open
        if (device == null) {
            return;
        }

        Configuration configuration = editConfiguration();
        for (Entry<String, Object> configurationParameter : configurationParameters.entrySet()) {
            if (configurationParameter.getKey().equals("pairing")) {
                doPairing();
            }
            if (configurationParameter.getKey().equals("discovery")) {
                doDiscovery();
            }
            if (configurationParameter.getKey().equals("pin")) {
                configuration.put(configurationParameter.getKey(), configurationParameter.getValue());
            }
        }
    }

    private void doPairing() {
        if (device.createBond() == false) {
            logger.debug("Pairing request for {} failed", address);
        }
    }

    private void doDiscovery() {
        // new BluetoothDeviceInitThread(device).run();
    }

    // **************************************************************

    private BluetoothGattCharacteristic findCharacteristic(UUID uuid) {
        for (BluetoothGattService service : gattClient.getServices()) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(uuid);
            if (characteristic != null) {
                return characteristic;
            }
        }

        return null;
    }

    /*
     * private void initialiseChannels() {
     * channelMap = new HashMap<ChannelUID, Map<DataType, BluetoothGattCharacteristic>>();
     *
     * for (Channel channel : getThing().getChannels()) {
     * // Create an entry in the channel map for this channel
     * if (channelMap.get(channel.getUID()) == null) {
     * channelMap.put(channel.getUID(), new HashMap<DataType, BluetoothGattCharacteristic>());
     * }
     *
     * Map<DataType, BluetoothGattCharacteristic> characteristicMap = channelMap.get(channel.getUID());
     *
     * // Process the channel properties
     * Map<String, String> properties = channel.getProperties();
     * for (String key : properties.keySet()) {
     * String[] bindingType = key.split(":");
     *
     * // Get the data type
     * DataType dataType = DataType.DecimalType;
     * try {
     * dataType = DataType.valueOf(bindingType[1]);
     * } catch (IllegalArgumentException e) {
     * logger.warn("{}: Invalid item type defined ({}). Assuming DecimalType", channel.getUID(),
     * bindingType[1]);
     * }
     *
     * UUID uuid = UUID.fromString(properties.get(key));
     *
     * BluetoothGattCharacteristic characteristic = findCharacteristic(uuid);
     * if (characteristic == null) {
     * logger.warn("GATT characteristic {} not found for {}", uuid, channel.getUID());
     * continue;
     * }
     *
     * characteristicMap.put(dataType, characteristic);
     * }
     * }
     * }
     */

}
