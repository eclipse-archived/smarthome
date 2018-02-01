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

import java.util.UUID;

import org.eclipse.smarthome.binding.bluetooth.BluetoothBindingConstants;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * The {@link BlukiiBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class BlukiiBindingConstants {

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BUTTON = new ThingTypeUID(BluetoothBindingConstants.BINDING_ID,
            "blukii_bts");

    public final static String BLUKII_PREFIX = "blukii";

    // Channel IDs
    public final static String CHANNEL_ID_ACCELREPORT = "accelReport";
    public final static String CHANNEL_ID_X = "accelX";
    public final static String CHANNEL_ID_Y = "accelY";
    public final static String CHANNEL_ID_Z = "accelZ";

    // Channel types UIDs
    public final static ChannelTypeUID CHANNEL_TYPE_UID_ACCEL_REPORT = new ChannelTypeUID(
            BluetoothBindingConstants.BINDING_ID, "blukii_accel_report");
    public final static ChannelTypeUID CHANNEL_TYPE_UID_ACCEL = new ChannelTypeUID(BluetoothBindingConstants.BINDING_ID,
            "blukii_accel");

    // Characteristics
    public final static UUID CHAR_ACCEL_REPORT = UUID.fromString("0000feb1-0000-1000-8000-00805f9b34fb");
    public final static UUID CHAR_ACCEL_X = UUID.fromString("0000feb3-0000-1000-8000-00805f9b34fb");
    public final static UUID CHAR_ACCEL_Y = UUID.fromString("0000feb4-0000-1000-8000-00805f9b34fb");
    public final static UUID CHAR_ACCEL_Z = UUID.fromString("0000feb5-0000-1000-8000-00805f9b34fb");
}
