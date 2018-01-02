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
package org.eclipse.smarthome.binding.sonyaudio;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link SonyAudioBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David - Initial contribution
 */
public class SonyAudioBindingConstants {

    private static final String BINDING_ID = "sonyaudio";

    public static final String SONY_TYPE_STRDN1080 = "STR-DN1080";

    public static final String SONY_TYPE_HTCT800 = "HT-CT800";

    public static final String SONY_TYPE_SRSZR5 = "SRS-ZR5";

    public static final Set<String> SUPPORTED_DEVICE_MODELS = ImmutableSet.of(SONY_TYPE_STRDN1080, SONY_TYPE_HTCT800,
            SONY_TYPE_SRSZR5);

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_STRDN1080 = new ThingTypeUID(BINDING_ID, SONY_TYPE_STRDN1080);
    public static final ThingTypeUID THING_TYPE_HTCT800 = new ThingTypeUID(BINDING_ID, SONY_TYPE_HTCT800);
    public static final ThingTypeUID THING_TYPE_SRSZR5 = new ThingTypeUID(BINDING_ID, SONY_TYPE_SRSZR5);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_STRDN1080,
            THING_TYPE_HTCT800, THING_TYPE_SRSZR5);

    // List of thing parameters names
    public static final String HOST_PARAMETER = "ipAddress";
    public static final String SCALAR_PORT_PARAMETER = "port";
    public static final String SCALAR_PATH_PARAMETER = "path";
    public static final String REFRESHINTERVAL = "refreshInterval";

    // List of all Channel ids
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_INPUT = "input";
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_MUTE = "mute";
    public static final String CHANNEL_SOUND_FIELD = "soundField";
    public static final String CHANNEL_CLEAR_AUDIO = "clearAudio";

    public static final String CHANNEL_MASTER_POWER = "master#power";
    public static final String CHANNEL_MASTER_SOUND_FIELD = "master#soundField";
    public static final String CHANNEL_MASTER_PURE_DIRECT = "master#pureDirect";

    public static final String CHANNEL_ZONE1_POWER = "zone1#power";
    public static final String CHANNEL_ZONE1_INPUT = "zone1#input";
    public static final String CHANNEL_ZONE1_VOLUME = "zone1#volume";
    public static final String CHANNEL_ZONE1_MUTE = "zone1#mute";

    public static final String CHANNEL_ZONE2_POWER = "zone2#power";
    public static final String CHANNEL_ZONE2_INPUT = "zone2#input";
    public static final String CHANNEL_ZONE2_VOLUME = "zone2#volume";
    public static final String CHANNEL_ZONE2_MUTE = "zone2#mute";

    public static final String CHANNEL_ZONE3_POWER = "zone3#power";
    public static final String CHANNEL_ZONE3_INPUT = "zone3#input";
    public static final String CHANNEL_ZONE3_VOLUME = "zone3#volume";
    public static final String CHANNEL_ZONE3_MUTE = "zone3#mute";

    public static final String CHANNEL_ZONE4_POWER = "zone4#power";
    public static final String CHANNEL_ZONE4_INPUT = "zone4#input";
    public static final String CHANNEL_ZONE4_VOLUME = "zone4#volume";
    public static final String CHANNEL_ZONE4_MUTE = "zone4#mute";

    public static final String CHANNEL_RADIO_FREQ = "radio#broadcastFreq";
    public static final String CHANNEL_RADIO_STATION = "radio#broadcastStation";
    public static final String CHANNEL_RADIO_SEEK_STATION = "radio#broadcastSeekStation";

    // Used for Discovery service
    public static final String MANUFACTURER = "SONY";
    public static final String UPNP_DEVICE_TYPE = "MediaRenderer";
}
