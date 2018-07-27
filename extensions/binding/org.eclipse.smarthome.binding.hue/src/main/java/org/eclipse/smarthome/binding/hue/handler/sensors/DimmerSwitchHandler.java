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

import static org.eclipse.smarthome.binding.hue.HueBindingConstants.THING_TYPE_DIMMER_SWITCH;

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
 * Hue Dimmer Switch
 *
 * @author Samuel Leisering - Added sensor support
 *
 */
public class DimmerSwitchHandler extends HueSensorHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream.of(THING_TYPE_DIMMER_SWITCH)
            .collect(Collectors.toSet());

    public DimmerSwitchHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void doSensorStateChanged(@Nullable HueBridge bridge, @NonNull FullSensor sensor, Configuration config) {
        Object btn = sensor.getState().get("buttonevent");

        DecimalType dt = new DecimalType(String.valueOf(btn));
        updateState(HueBindingConstants.CHANNEL_DIMMER_SWITCH, dt);
    }

    @Override
    protected String getVendor(String modelId) {
        return "Philips";
    }

}
