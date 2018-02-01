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
package org.eclipse.smarthome.binding.bluetooth.yeelightblue;

import org.eclipse.smarthome.binding.bluetooth.BluetoothBindingConstants;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link YeelightBlueBindingConstants.YeeLightBlueBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Chris Jackson - Initial contribution
 */
public class YeelightBlueBindingConstants {

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BLUE2 = new ThingTypeUID(BluetoothBindingConstants.BINDING_ID,
            "yeelight_blue2");

    // List of all Channel ids
    public final static String CHANNEL_SWITCH = "switch";
    public final static String CHANNEL_BRIGHTNESS = "brightness";
    public final static String CHANNEL_COLOR = "color";
    public final static String CHANNEL_RSSI = "rssi";

    public final static String YEELIGHT_NAME = "Yeelight Blue II";
}
