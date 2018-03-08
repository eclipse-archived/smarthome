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
package org.eclipse.smarthome.binding.bluetooth.blukii;

import org.eclipse.smarthome.binding.bluetooth.BluetoothBindingConstants;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link BlukiiBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class BlukiiBindingConstants {

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BEACON = new ThingTypeUID(BluetoothBindingConstants.BINDING_ID,
            "blukii_beacon");

    public final static String BLUKII_PREFIX = "blukii ";

    // Channel IDs
    public final static String CHANNEL_ID_BATTERY = "battery";
    public final static String CHANNEL_ID_TEMPERATURE = "temperature";
    public final static String CHANNEL_ID_HUMIDITY = "humidity";
    public final static String CHANNEL_ID_PRESSURE = "pressure";
    public final static String CHANNEL_ID_LUMINANCE = "luminance";

    public final static String CHANNEL_ID_TILTX = "tiltx";
    public final static String CHANNEL_ID_TILTY = "tilty";
    public final static String CHANNEL_ID_TILTZ = "tiltz";

    public final static String CHANNEL_ID_SWITCH = "switch";

}
