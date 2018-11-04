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
package org.eclipse.smarthome.binding.onewire.internal.handler;

import static org.eclipse.smarthome.binding.onewire.internal.OwBindingConstants.*;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.onewire.internal.OwDynamicStateDescriptionProvider;
import org.eclipse.smarthome.binding.onewire.internal.OwException;
import org.eclipse.smarthome.binding.onewire.internal.Util;
import org.eclipse.smarthome.binding.onewire.internal.device.DS18x20;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;

/**
 * The {@link TemperatureSensorThingHandler} is responsible for handling temperature sensors
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class TemperatureSensorThingHandler extends OwBaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_TEMPERATURE);

    public TemperatureSensorThingHandler(Thing thing,
            OwDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        super(thing, dynamicStateDescriptionProvider);
    }

    @Override
    public void initialize() {
        Map<String, String> properties = editProperties();

        if (!super.configure()) {
            return;
        }

        if (getThing().getStatus() == ThingStatus.OFFLINE) {
            return;
        }

        sensors.add(new DS18x20(sensorIds.get(0), this));

        if (!properties.containsKey(PROPERTY_MODELID)) {
            scheduler.execute(() -> {
                updateSensorProperties();
            });
        } else {
            scheduler.execute(() -> {
                configureThingChannels();
            });
        }
    }

    private void configureThingChannels() {
        Channel tempChannel = thing.getChannel(CHANNEL_TEMPERATURE);
        if (tempChannel == null) {
            Map<String, String> properties = editProperties();
            ThingBuilder thingBuilder = editThing();
            if (properties.get(PROPERTY_MODELID).equals("DS18B20")
                    || properties.get(PROPERTY_MODELID).equals("DS1822")) {
                thingBuilder.withChannel(
                        Util.buildTemperatureChannel(thing.getUID(), CHANNEL_TYPE_UID_TEMPERATURE_POR_RES));
            } else {
                thingBuilder
                        .withChannel(Util.buildTemperatureChannel(thing.getUID(), CHANNEL_TYPE_UID_TEMPERATURE_POR));
            }
            updateThing(thingBuilder.build());
        }

        try {
            sensors.get(0).configureChannels();
            sensors.get(0).enableChannel(CHANNEL_TEMPERATURE);
        } catch (OwException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        }

        validConfig = true;

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);
    }
}
