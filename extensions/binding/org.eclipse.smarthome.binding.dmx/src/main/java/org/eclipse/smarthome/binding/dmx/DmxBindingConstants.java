/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.dmx;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link DmxBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class DmxBindingConstants {

    public static final String BINDING_ID = "dmx";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CHASER = new ThingTypeUID(BINDING_ID, "chaser");
    public static final ThingTypeUID THING_TYPE_DIMMER = new ThingTypeUID(BINDING_ID, "dimmer");
    public static final ThingTypeUID THING_TYPE_ARTNET_BRIDGE = new ThingTypeUID(BINDING_ID, "artnet-bridge");
    public static final ThingTypeUID THING_TYPE_LIB485_BRIDGE = new ThingTypeUID(BINDING_ID, "lib485-bridge");
    public static final ThingTypeUID THING_TYPE_SACN_BRIDGE = new ThingTypeUID(BINDING_ID, "sacn-bridge");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = ImmutableSet.of(THING_TYPE_ARTNET_BRIDGE,
            THING_TYPE_LIB485_BRIDGE, THING_TYPE_SACN_BRIDGE, THING_TYPE_CHASER, THING_TYPE_DIMMER);

    // List of all config options
    public static final String CONFIG_UNIVERSE = "universe";
    public static final String CONFIG_DMX_ID = "dmxid";
    public static final String CONFIG_APPLY_CURVE = "applycurve";
    public static final String CONFIG_REFRESH_RATE = "refreshrate";

    public static final String CONFIG_SACN_MODE = "mode";
    public static final String CONFIG_ADDRESS = "address";
    public static final String CONFIG_LOCAL_ADDRESS = "localaddress";
    public static final String CONFIG_REFRESH_MODE = "refreshmode";

    public static final String CONFIG_DIMMER_FADE_TIME = "fadetime";
    public static final String CONFIG_DIMMER_DIM_TIME = "dimtime";
    public static final String CONFIG_DIMMER_TURNONVALUE = "turnonvalue";
    public static final String CONFIG_DIMMER_TURNOFFVALUE = "turnoffvalue";
    public static final String CONFIG_CHASER_STEPS = "steps";
    public static final String CONFIG_CHASER_RESUME_AFTER = "resumeafter";

    // List of all channels
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_CONTROL = "control";
    public static final String CHANNEL_MUTE = "mute";

    public static final ChannelTypeUID colorChannelTypeUID = new ChannelTypeUID(BINDING_ID, CHANNEL_COLOR);

    // Listener Type for channel updates
    public static enum ListenerType {
        ONOFF,
        VALUE,
        ACTION;
    }
}
