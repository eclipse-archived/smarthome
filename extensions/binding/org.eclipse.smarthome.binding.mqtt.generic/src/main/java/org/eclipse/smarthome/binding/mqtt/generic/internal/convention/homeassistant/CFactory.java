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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.internal.generic.ChannelStateUpdateListener;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory to create HomeAssistant MQTT components. Those components are specified at:
 * https://www.home-assistant.io/docs/mqtt/discovery/
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class CFactory {
    private static final Logger logger = LoggerFactory.getLogger(CFactory.class);

    /**
     * Create a HA MQTT component. The configuration JSon string is required.
     *
     * @param thingUID The Thing UID that this component will belong to.
     * @param haID The location of this component. The HomeAssistant ID contains the object-id, node-id and
     *            component-id.
     * @param configJSON Most components expect a "name", a "state_topic" and "command_topic" like with
     *            "{name:'Name',state_topic:'homeassistant/switch/0/object/state',command_topic:'homeassistant/switch/0/object/set'".
     * @param channelStateUpdateListener A channel state update listener
     * @return A HA MQTT Component
     */
    public static @Nullable AbstractComponent createComponent(ThingUID thingUID, HaID haID, String configJSON,
            @Nullable ChannelStateUpdateListener channelStateUpdateListener) {
        try {
            switch (haID.component) {
                case "alarm_control_panel":
                    return new ComponentAlarmControlPanel(thingUID, haID, configJSON, channelStateUpdateListener);
                case "binary_sensor":
                    return new ComponentBinarySensor(thingUID, haID, configJSON, channelStateUpdateListener);
                case "camera":
                    return new ComponentCamera(thingUID, haID, configJSON, channelStateUpdateListener);
                case "cover":
                    return new ComponentCover(thingUID, haID, configJSON, channelStateUpdateListener);
                case "fan":
                    return new ComponentFan(thingUID, haID, configJSON, channelStateUpdateListener);
                case "climate":
                    return new ComponentClimate(thingUID, haID, configJSON, channelStateUpdateListener);
                case "light":
                    return new ComponentLight(thingUID, haID, configJSON, channelStateUpdateListener);
                case "lock":
                    return new ComponentLock(thingUID, haID, configJSON, channelStateUpdateListener);
                case "sensor":
                    return new ComponentSensor(thingUID, haID, configJSON, channelStateUpdateListener);
                case "switch":
                    return new ComponentSwitch(thingUID, haID, configJSON, channelStateUpdateListener);
            }
        } catch (UnsupportedOperationException e) {
            logger.warn("Not supported", e);
        }
        return null;
    }

    /**
     * Create a HA MQTT component by a given channel configuration.
     *
     * @param basetopic The MQTT base topic, usually "homeassistant"
     * @param channel A channel with the JSON configuration embedded as configuration (key: 'config')
     * @param channelStateUpdateListener A channel state update listener
     * @return A HA MQTT Component
     */
    public static @Nullable AbstractComponent createComponent(String basetopic, Channel channel,
            @Nullable ChannelStateUpdateListener channelStateUpdateListener) {
        HaID haID = new HaID(basetopic, channel.getUID());
        ThingUID thingUID = channel.getUID().getThingUID();
        String configJSON = (String) channel.getConfiguration().get("config");
        if (configJSON == null) {
            logger.warn("Provided channel does not have a 'config' configuration key!");
            return null;
        }
        try {
            switch (haID.component) {
                case "alarm_control_panel":
                    return new ComponentAlarmControlPanel(thingUID, haID, configJSON, channelStateUpdateListener);
                case "binary_sensor":
                    return new ComponentBinarySensor(thingUID, haID, configJSON, channelStateUpdateListener);
                case "camera":
                    return new ComponentCamera(thingUID, haID, configJSON, channelStateUpdateListener);
                case "cover":
                    return new ComponentCover(thingUID, haID, configJSON, channelStateUpdateListener);
                case "fan":
                    return new ComponentFan(thingUID, haID, configJSON, channelStateUpdateListener);
                case "climate":
                    return new ComponentClimate(thingUID, haID, configJSON, channelStateUpdateListener);
                case "light":
                    return new ComponentLight(thingUID, haID, configJSON, channelStateUpdateListener);
                case "lock":
                    return new ComponentLock(thingUID, haID, configJSON, channelStateUpdateListener);
                case "sensor":
                    return new ComponentSensor(thingUID, haID, configJSON, channelStateUpdateListener);
                case "switch":
                    return new ComponentSwitch(thingUID, haID, configJSON, channelStateUpdateListener);
            }
        } catch (UnsupportedOperationException e) {
            logger.warn("Not supported", e);
        }
        return null;
    }
}
