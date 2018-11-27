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
package org.eclipse.smarthome.binding.hue.internal.handler.sensors;

import static org.eclipse.smarthome.binding.hue.internal.FullSensor.*;
import static org.eclipse.smarthome.binding.hue.internal.HueBindingConstants.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.hue.internal.FullSensor;
import org.eclipse.smarthome.binding.hue.internal.HueBridge;
import org.eclipse.smarthome.binding.hue.internal.LightLevelConfigUpdate;
import org.eclipse.smarthome.binding.hue.internal.SensorConfigUpdate;
import org.eclipse.smarthome.binding.hue.internal.handler.HueSensorHandler;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * Light Level Sensor
 *
 * @author Samuel Leisering - Initial contribution
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class LightLevelHandler extends HueSensorHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_LIGHT_LEVEL_SENSOR);

    public LightLevelHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected SensorConfigUpdate doConfigurationUpdate(Map<String, Object> configurationParameters) {
        LightLevelConfigUpdate configUpdate = new LightLevelConfigUpdate();
        if (configurationParameters.containsKey(CONFIG_LED_INDICATION)) {
            configUpdate.setLedIndication(Boolean.TRUE.equals(configurationParameters.get(CONFIG_LED_INDICATION)));
        }
        if (configurationParameters.containsKey(CONFIG_LIGHT_LEVEL_THRESHOLD_DARK)) {
            configUpdate.setThresholdDark(
                    Integer.parseInt(String.valueOf(configurationParameters.get(CONFIG_LIGHT_LEVEL_THRESHOLD_DARK))));
        }
        if (configurationParameters.containsKey(CONFIG_LIGHT_LEVEL_THRESHOLD_OFFSET)) {
            configUpdate.setThresholdOffset(
                    Integer.parseInt(String.valueOf(configurationParameters.get(CONFIG_LIGHT_LEVEL_THRESHOLD_OFFSET))));
        }
        return configUpdate;
    }

    @Override
    protected void doSensorStateChanged(@Nullable HueBridge bridge, FullSensor sensor, Configuration config) {
        Object lightLevel = sensor.getState().get(STATE_LIGHT_LEVEL);
        if (lightLevel != null) {
            BigDecimal value = new BigDecimal(String.valueOf(lightLevel));
            updateState(CHANNEL_LIGHT_LEVEL, new DecimalType(value));

            // calculate lux, according to
            // https://developers.meethue.com/documentation/supported-sensors#clip_zll_lightlevel
            double lux = Math.pow(10, (value.subtract(BigDecimal.ONE).divide(new BigDecimal(10000))).doubleValue());
            updateState(CHANNEL_ILLUMINANCE, new QuantityType<>(lux, SmartHomeUnits.LUX));
        }

        Object dark = sensor.getState().get(STATE_DARK);
        if (dark != null) {
            boolean value = Boolean.parseBoolean(String.valueOf(dark));
            updateState(CHANNEL_DARK, value ? OnOffType.ON : OnOffType.OFF);
        }

        Object daylight = sensor.getState().get(STATE_DAYLIGHT);
        if (daylight != null) {
            boolean value = Boolean.parseBoolean(String.valueOf(daylight));
            updateState(CHANNEL_DAYLIGHT, value ? OnOffType.ON : OnOffType.OFF);
        }

        if (sensor.getConfig().containsKey(CONFIG_LED_INDICATION)) {
            config.put(CONFIG_LED_INDICATION, sensor.getConfig().get(CONFIG_LIGHT_LEVEL_THRESHOLD_DARK));
        }
        if (sensor.getConfig().containsKey(CONFIG_LIGHT_LEVEL_THRESHOLD_DARK)) {
            config.put(CONFIG_LIGHT_LEVEL_THRESHOLD_DARK, sensor.getConfig().get(CONFIG_LIGHT_LEVEL_THRESHOLD_DARK));
        }
        if (sensor.getConfig().containsKey(CONFIG_LIGHT_LEVEL_THRESHOLD_OFFSET)) {
            config.put(CONFIG_LIGHT_LEVEL_THRESHOLD_OFFSET,
                    sensor.getConfig().get(CONFIG_LIGHT_LEVEL_THRESHOLD_OFFSET));
        }
    }
}
