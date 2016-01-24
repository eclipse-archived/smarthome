/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.ble;

import java.util.UUID;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link BluetoothBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Chris Jackson - Initial contribution
 */
public class BleBindingConstants {

    public static final String BINDING_ID = "ble";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_GENERIC = new ThingTypeUID(BINDING_ID, "generic");
    public final static ThingTypeUID THING_TYPE_PARROT_FLOWERPOWER = new ThingTypeUID(BINDING_ID, "parrot_flowerpower");
    public final static ThingTypeUID THING_TYPE_WIT_ENERGY = new ThingTypeUID(BINDING_ID, "wit_energy");
    public final static ThingTypeUID THING_TYPE_YEELIGHT_BLUE = new ThingTypeUID(BINDING_ID, "yeelight_blue");

    public final static String PROPERTY_ADDRESS = "address";
    public final static String PROPERTY_MANUFACTURER = "manufacturer";

    public final static String THING_NAME_GENERIC = "Bluetooth Device";

    public final static String CHANNEL_CFG_CHARACTERISTIC = "characteristic";

    public final static String CHANNEL_BATTERYLEVEL = "battery-level";
    public final static String CHANNEL_SWITCH = "switch";
    public final static String CHANNEL_BRIGHTNESS = "brightness";
    public final static String CHANNEL_RSSI = "rssi";
    public final static String CHANNEL_COLOR = "color";

    public final static String CHANNEL_AIR_TEMPERATURE = "air-temperature";
    public final static String CHANNEL_SOIL_TEMPERATURE = "soil-temperature";
    public final static String CHANNEL_SOIL_MOISTURE = "soil-moisture";
    public final static String CHANNEL_SOIL_FERTILISER = "soil-fertiliser";
    public final static String CHANNEL_LUMINANCE = "luminance";
    public final static String CHANNEL_LED_STATE = "led-state";

    public static final long bleUuid = 0x800000805f9b34fbL;

    public final static UUID CHARACTERISTIC_RSSI = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");
}
