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
import org.eclipse.smarthome.binding.onewire.internal.device.DS2423;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link CounterSensorThingHandler} is responsible for handling counter sensors
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class CounterSensorThingHandler extends OwBaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_COUNTER2);

    public CounterSensorThingHandler(Thing thing, OwDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        super(thing, dynamicStateDescriptionProvider);
    }

    @Override
    public void initialize() {
        Configuration configuration = getConfig();
        Map<String, String> properties = editProperties();

        if (!super.configure()) {
            return;
        }

        if (getThing().getStatus() == ThingStatus.OFFLINE) {
            return;
        }

        if (!properties.containsKey(PROPERTY_MODELID)) {
            scheduler.execute(() -> {
                updateSensorProperties();
            });
        }

        sensors.add(new DS2423(sensorIds.get(0), this));

        try {
            sensors.get(0).configureChannels();
        } catch (OwException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        }

        validConfig = true;

        if (!configuration.containsKey(CONFIG_REFRESH)) {
            // override default of 300s from base thing handler if no user-defined value is present
            refreshInterval = 10 * 1000;
        }

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);
    }
}
