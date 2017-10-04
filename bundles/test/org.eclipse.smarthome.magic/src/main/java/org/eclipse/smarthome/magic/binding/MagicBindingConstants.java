/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.magic.binding;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link MagicBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Henning Treu - Initial contribution
 */
public class MagicBindingConstants {

    public static final String BINDING_ID = "magic";

    // List all Thing Type UIDs, related to the Hue Binding

    // generic thing types
    public static final ThingTypeUID THING_TYPE_EXTENSIBLE_THING = new ThingTypeUID(BINDING_ID, "extensible-thing");
    public static final ThingTypeUID THING_TYPE_ON_OFF_LIGHT = new ThingTypeUID(BINDING_ID, "onoff-light");
    public static final ThingTypeUID THING_TYPE_DIMMABLE_LIGHT = new ThingTypeUID(BINDING_ID, "dimmable-light");
    public static final ThingTypeUID THING_TYPE_COLOR_LIGHT = new ThingTypeUID(BINDING_ID, "color-light");
    public static final ThingTypeUID THING_TYPE_CONTACT_SENSOR = new ThingTypeUID(BINDING_ID, "contact-sensor");
    public static final ThingTypeUID THING_TYPE_CONFIG_THING = new ThingTypeUID(BINDING_ID, "configurable-thing");

    // List all channels
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_CONTACT = "contact";

}
