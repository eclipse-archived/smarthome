/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.fsinternetradio;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * This {@link FSInternetRadioBindingConstants} interface defines common constants, which are
 * used across the whole binding.
 *
 * @author Patrick Koenemann - Initial contribution
 */
public interface FSInternetRadioBindingConstants {

    String BINDING_ID = "fsinternetradio";

    // List of all Thing Type UIDs
    ThingTypeUID THING_TYPE_RADIO = new ThingTypeUID(BINDING_ID, "radio");

    // List of all Channel ids
    String CHANNEL_POWER = "power";
    String CHANNEL_PRESET = "preset";
    String CHANNEL_VOLUME_PERCENT = "volume-percent";
    String CHANNEL_VOLUME_ABSOLUTE = "volume-absolute";
    String CHANNEL_MUTE = "mute";
    String CHANNEL_PLAY_INFO_NAME = "play-info-name";
    String CHANNEL_PLAY_INFO_TEXT = "play-info-text";
    String CHANNEL_MODE = "mode";

    // config properties
    String CONFIG_PROPERTY_IP = "ip";
    String CONFIG_PROPERTY_PIN = "pin";
    String CONFIG_PROPERTY_PORT = "port";
    String CONFIG_PROPERTY_REFRESH = "refresh";

    // further properties
    String PROPERTY_MANUFACTURER = "manufacturer";
    String PROPERTY_MODEL = "model";
}
