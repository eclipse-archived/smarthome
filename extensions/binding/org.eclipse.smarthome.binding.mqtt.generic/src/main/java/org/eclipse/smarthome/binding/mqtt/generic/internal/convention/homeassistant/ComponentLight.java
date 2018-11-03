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
package org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homeassistant;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.internal.generic.ChannelStateUpdateListener;
import org.eclipse.smarthome.binding.mqtt.generic.internal.values.ColorValue;
import org.eclipse.smarthome.core.thing.ThingUID;

import com.google.gson.Gson;

/**
 * A MQTT light, following the https://www.home-assistant.io/components/light.mqtt/ specification.
 *
 * This is a reduced functionality implementation, which only allows to turn a light on and off.
 *
 * @author David Graeff - Initial contribution
 */
public class ComponentLight extends AbstractComponent {
    public static final String switchChannelID = "light"; // Randomly chosen channel "ID"
    public static final String brightnessChannelID = "brightness"; // Randomly chosen channel "ID"
    public static final String colorChannelID = "color"; // Randomly chosen channel "ID"

    /**
     * Configuration class for MQTT component
     */
    static class Config {
        protected String name = "MQTT Light";
        protected String icon = "";
        protected int qos = 1;
        protected boolean retain = true;
        protected @Nullable String unique_id;

        protected int brightness_scale = 255;
        protected boolean optimistic = false;
        protected List<String> effect_list;

        // Defines when on the payload_on is sent. Using last (the default) will send any style (brightness, color, etc)
        // topics first and then a payload_on to the command_topic. Using first will send the payload_on and then any
        // style topics. Using brightness will only send brightness commands instead of the payload_on to turn the light
        // on.
        protected String on_command_type = "last";

        protected @Nullable String state_topic;
        protected @Nullable String command_topic;
        protected @Nullable String state_value_template;

        protected @Nullable String brightness_state_topic;
        protected @Nullable String brightness_command_topic;
        protected @Nullable String brightness_value_template;

        protected @Nullable String color_temp_state_topic;
        protected @Nullable String color_temp_command_topic;
        protected @Nullable String color_temp_value_template;

        protected @Nullable String effect_command_topic;
        protected @Nullable String effect_state_topic;
        protected @Nullable String effect_value_template;

        protected @Nullable String rgb_command_topic;
        protected @Nullable String rgb_state_topic;
        protected @Nullable String rgb_value_template;
        protected @Nullable String rgb_command_template;

        protected @Nullable String white_value_command_topic;
        protected @Nullable String white_value_state_topic;
        protected @Nullable String white_value_template;

        protected @Nullable String xy_command_topic;
        protected @Nullable String xy_state_topic;
        protected @Nullable String xy_value_template;

        protected String payload_on = "ON";
        protected String payload_off = "OFF";

        protected @Nullable String availability_topic;
        protected String payload_available = "online";
        protected String payload_not_available = "offline";
    };

    protected Config config = new Config();

    public ComponentLight(ThingUID thing, HaID haID, String configJSON,
            @Nullable ChannelStateUpdateListener channelStateUpdateListener) {
        super(thing, haID, configJSON);
        config = new Gson().fromJson(configJSON, Config.class);

        channels.put(switchChannelID,
                new CChannel(this, switchChannelID, new ColorValue(true, config.payload_on, config.payload_off),
                        config.state_topic, config.command_topic, config.name, "", channelStateUpdateListener));
    }

    @Override
    public String name() {
        return config.name;
    }
}
