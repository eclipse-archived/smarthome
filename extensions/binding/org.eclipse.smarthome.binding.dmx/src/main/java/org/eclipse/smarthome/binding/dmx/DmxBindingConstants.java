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

    public final static String BINDING_ID = "dmx";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_CHASER = new ThingTypeUID(BINDING_ID, "chaser");
    public final static ThingTypeUID THING_TYPE_DIMMER = new ThingTypeUID(BINDING_ID, "dimmer");
    public final static ThingTypeUID THING_TYPE_ARTNET_BRIDGE = new ThingTypeUID(BINDING_ID, "artnet-bridge");
    public final static ThingTypeUID THING_TYPE_LIB485_BRIDGE = new ThingTypeUID(BINDING_ID, "lib485-bridge");
    public final static ThingTypeUID THING_TYPE_SACN_BRIDGE = new ThingTypeUID(BINDING_ID, "sacn-bridge");

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = ImmutableSet.of(THING_TYPE_ARTNET_BRIDGE,
            THING_TYPE_LIB485_BRIDGE, THING_TYPE_SACN_BRIDGE, THING_TYPE_CHASER, THING_TYPE_DIMMER);

    // List of all config options
    public final static String CONFIG_UNIVERSE = "universe";
    public final static String CONFIG_DMX_ID = "dmxid";
    public final static String CONFIG_APPLY_CURVE = "applycurve";
    public final static String CONFIG_REFRESH_RATE = "refreshrate";

    public final static String CONFIG_SACN_MODE = "mode";
    public final static String CONFIG_ADDRESS = "address";
    public final static String CONFIG_LOCAL_ADDRESS = "localaddress";
    public final static String CONFIG_REFRESH_MODE = "refreshmode";

    public final static String CONFIG_DIMMER_FADE_TIME = "fadetime";
    public final static String CONFIG_DIMMER_DIM_TIME = "dimtime";
    public final static String CONFIG_DIMMER_TURNONVALUE = "turnonvalue";
    public final static String CONFIG_DIMMER_TURNOFFVALUE = "turnoffvalue";
    public final static String CONFIG_CHASER_STEPS = "steps";
    public final static String CONFIG_CHASER_RESUME_AFTER = "resumeafter";

    // List of all channels
    public final static String CHANNEL_BRIGHTNESS = "brightness";
    public final static String CHANNEL_COLOR = "color";
    public final static String CHANNEL_SWITCH = "switch";
    public final static String CHANNEL_CONTROL = "control";
    public final static String CHANNEL_MUTE = "mute";

    public final static ChannelTypeUID colorChannelTypeUID = new ChannelTypeUID(BINDING_ID, CHANNEL_COLOR);

    // Listener Type for channel updates
    public static enum ListenerType {
        ONOFF,
        VALUE,
        ACTION;
    }
}
