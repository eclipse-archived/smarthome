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

import static org.eclipse.smarthome.binding.hue.internal.HueBindingConstants.*;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.hue.internal.FullSensor;
import org.eclipse.smarthome.binding.hue.internal.HueBridge;
import org.eclipse.smarthome.binding.hue.internal.SensorConfigUpdate;
import org.eclipse.smarthome.binding.hue.internal.handler.HueSensorHandler;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * Hue Dimmer Switch
 *
 * @author Samuel Leisering - Initial contribution
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class DimmerSwitchHandler extends HueSensorHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_DIMMER_SWITCH);

    public DimmerSwitchHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected SensorConfigUpdate doConfigurationUpdate(Map<String, Object> configurationParameters) {
        return new SensorConfigUpdate();
    }

    @Override
    protected void doSensorStateChanged(@Nullable HueBridge bridge, FullSensor sensor, Configuration config) {
        Object buttonState = sensor.getState().get(FullSensor.STATE_BUTTON_EVENT);
        if (buttonState != null) {
            String value = String.valueOf(buttonState);
            updateState(CHANNEL_DIMMER_SWITCH, new DecimalType(value));
            triggerChannel(EVENT_DIMMER_SWITCH, value);
        }
    }
}
