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
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory to create HomeAssistant MQTT components as specified on:
 * https://www.home-assistant.io/docs/mqtt/discovery/
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class CFactory {
    private static final Logger logger = LoggerFactory.getLogger(CFactory.class);

    public static @Nullable AbstractComponent createComponent(ThingUID thingUID, String component_id,
            String configJSON) {
        try {
            switch (component_id) {
                case "alarm_control_panel":
                    return new ComponentAlarmControlPanel(thingUID, component_id, configJSON);
                case "binary_sensor":
                    return new ComponentBinarySensor(thingUID, component_id, configJSON);
                case "camera":
                    return new ComponentCamera(thingUID, component_id, configJSON);
                case "cover":
                    return new ComponentCover(thingUID, component_id, configJSON);
                case "fan":
                    return new ComponentFan(thingUID, component_id, configJSON);
                case "climate":
                    return new ComponentClimate(thingUID, component_id, configJSON);
                case "light":
                    return new ComponentLight(thingUID, component_id, configJSON);
                case "lock":
                    return new ComponentLock(thingUID, component_id, configJSON);
                case "sensor":
                    return new ComponentSensor(thingUID, component_id, configJSON);
                case "switch":
                    return new ComponentSwitch(thingUID, component_id, configJSON);
            }
        } catch (UnsupportedOperationException e) {
            logger.warn("Not supported", e);
        }
        return null;
    }
}
