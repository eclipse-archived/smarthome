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
import org.eclipse.smarthome.binding.hue.internal.SensorConfigUpdate;
import org.eclipse.smarthome.binding.hue.internal.TemperatureConfigUpdate;
import org.eclipse.smarthome.binding.hue.internal.handler.HueSensorHandler;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * Temperature Sensor
 *
 * @author Samuel Leisering - Initial contribution
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class TemperatureHandler extends HueSensorHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_TEMPERATURE_SENSOR);

    public TemperatureHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected SensorConfigUpdate doConfigurationUpdate(Map<String, Object> configurationParameters) {
        TemperatureConfigUpdate configUpdate = new TemperatureConfigUpdate();
        if (configurationParameters.containsKey(CONFIG_LED_INDICATION)) {
            configUpdate.setLedIndication(Boolean.TRUE.equals(configurationParameters.get(CONFIG_LED_INDICATION)));
        }
        return configUpdate;
    }

    @Override
    protected void doSensorStateChanged(@Nullable HueBridge bridge, FullSensor sensor, Configuration config) {
        Object temperature = sensor.getState().get(STATE_TEMPERATURE);
        if (temperature != null) {
            BigDecimal value = new BigDecimal(String.valueOf(temperature));
            updateState(CHANNEL_TEMPERATURE, new QuantityType<>(value.divide(new BigDecimal(100)), SIUnits.CELSIUS));
        }

        if (sensor.getConfig().containsKey(CONFIG_LED_INDICATION)) {
            config.put(CONFIG_LED_INDICATION, sensor.getConfig().get(CONFIG_LIGHT_LEVEL_THRESHOLD_DARK));
        }
    }
}
