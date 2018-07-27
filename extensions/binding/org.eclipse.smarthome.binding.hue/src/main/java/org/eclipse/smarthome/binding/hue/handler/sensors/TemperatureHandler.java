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
package org.eclipse.smarthome.binding.hue.handler.sensors;

import static org.eclipse.smarthome.binding.hue.HueBindingConstants.THING_TYPE_TEMPERATURE_SENSOR;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.hue.HueBindingConstants;
import org.eclipse.smarthome.binding.hue.handler.HueSensorHandler;
import org.eclipse.smarthome.binding.hue.internal.FullSensor;
import org.eclipse.smarthome.binding.hue.internal.HueBridge;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * Temperature Sensor
 *
 * @author Samuel Leisering - Added sensor support
 *
 */
public class TemperatureHandler extends HueSensorHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream.of(THING_TYPE_TEMPERATURE_SENSOR)
            .collect(Collectors.toSet());

    public TemperatureHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void doSensorStateChanged(@Nullable HueBridge bridge, @NonNull FullSensor sensor, Configuration config) {
        Object tmp = sensor.getState().get("temperature");
        System.out.println("SET TEMPERATURE " + tmp);
        BigDecimal value = new BigDecimal(String.valueOf(tmp));
        DecimalType temperature = new DecimalType(value.divide(new BigDecimal(100)));
        updateState(HueBindingConstants.CHANNEL_TEMPERATURE, temperature);
    }

    @Override
    protected String getVendor(String modelId) {
        return "Philips";
    }
}
