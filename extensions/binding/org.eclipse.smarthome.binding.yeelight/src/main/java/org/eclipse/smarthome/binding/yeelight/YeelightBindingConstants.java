/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.yeelight;

import java.util.UUID;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link BluetoothBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Chris Jackson - Initial contribution
 */
public class YeelightBindingConstants {

    public static final String BINDING_ID = "yeelight";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_YEELIGHT_BLUE = new ThingTypeUID(BINDING_ID, "yeelight_blue");

    public final static String PROPERTY_ADDRESS = "address";
    public final static String PROPERTY_MANUFACTURER = "manufacturer";

    public final static String THING_NAME_GENERIC = "Bluetooth Device";

    public final static String CHANNEL_CFG_CHARACTERISTIC = "characteristic";

    public final static String CHANNEL_SWITCH = "switch";
    public final static String CHANNEL_BRIGHTNESS = "brightness";
    public final static String CHANNEL_RSSI = "rssi";
    public final static String CHANNEL_COLOR = "color";

    public final static String YEELIGHT_NAME = "Yeelight Blue II";

    public static final long bleUuid = 0x800000805f9b34fbL;

    public final static UUID CHARACTERISTIC_RSSI = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");
}
