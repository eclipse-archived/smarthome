/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.ble.yeelightblue;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link Ble.YeeLightBlueBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Chris Jackson - Initial contribution
 */
public class YeeLightBlueBindingConstants {

    private static final String BINDING_ID = "ble";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BLUE2 = new ThingTypeUID(BINDING_ID, "yeelight_blue2");

    // List of all Channel ids
    public final static String CHANNEL_SWITCH = "switch";
    public final static String CHANNEL_BRIGHTNESS = "brightness";
    public final static String CHANNEL_COLOR = "color";
    public final static String CHANNEL_RSSI = "rssi";

    public final static String YEELIGHT_NAME = "Yeelight Blue II";
}
