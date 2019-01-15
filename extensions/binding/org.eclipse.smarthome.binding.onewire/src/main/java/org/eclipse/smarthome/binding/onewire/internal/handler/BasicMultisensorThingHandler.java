/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.onewire.internal.DS2438Configuration;
import org.eclipse.smarthome.binding.onewire.internal.OwDynamicStateDescriptionProvider;
import org.eclipse.smarthome.binding.onewire.internal.OwException;
import org.eclipse.smarthome.binding.onewire.internal.device.DS1923;
import org.eclipse.smarthome.binding.onewire.internal.device.DS2438;
import org.eclipse.smarthome.binding.onewire.internal.device.DS2438.CurrentSensorType;
import org.eclipse.smarthome.binding.onewire.internal.device.DS2438.LightSensorType;
import org.eclipse.smarthome.binding.onewire.internal.device.OwSensorType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link BasicMultisensorThingHandler} is responsible for handling DS2438/DS1923 based multisensors (single
 * sensors)
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class BasicMultisensorThingHandler extends OwBaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.unmodifiableSet(
            Stream.of(THING_TYPE_MS_TX, THING_TYPE_MS_TH, THING_TYPE_MS_TV).collect(Collectors.toSet()));
    public static final Set<OwSensorType> SUPPORTED_SENSOR_TYPES = Collections
            .unmodifiableSet(Stream.of(OwSensorType.MS_TH, OwSensorType.MS_TC, OwSensorType.MS_TL, OwSensorType.MS_TV,
                    OwSensorType.DS1923, OwSensorType.DS2438).collect(Collectors.toSet()));

    public BasicMultisensorThingHandler(Thing thing,
            OwDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        super(thing, dynamicStateDescriptionProvider, SUPPORTED_SENSOR_TYPES);
    }

    @Override
    public void initialize() {
        // TODO: remove after 0.11.0 release
        if (!thing.getThingTypeUID().equals(THING_TYPE_MS_TX)) {
            changeThingType(THING_TYPE_MS_TX, getConfig());
        }

        if (!super.configureThingHandler()) {
            return;
        }

        // add sensors
        if (sensorType == OwSensorType.DS1923) {
            sensors.add(new DS1923(sensorId, this));
        } else {
            sensors.add(new DS2438(sensorId, this));
        }

        scheduler.execute(() -> {
            configureThingChannels();
        });
    }

    @Override
    protected void configureThingChannels() {
        switch (sensorType) {
            case DS2438:
                ((DS2438) sensors.get(0)).setCurrentSensorType(CurrentSensorType.INTERNAL);
                break;
            case MS_TC:
                ((DS2438) sensors.get(0)).setCurrentSensorType(CurrentSensorType.IBUTTONLINK);
                break;
            case MS_TL:
                ((DS2438) sensors.get(0)).setLightSensorType(LightSensorType.IBUTTONLINK);
                break;
            default:
        }

        super.configureThingChannels();
    }

    @Override
    public Map<String, String> updateSensorProperties(OwBaseBridgeHandler bridgeHandler) throws OwException {
        Map<String, String> properties = new HashMap<String, String>();
        sensorType = bridgeHandler.getType(sensorId);

        if (sensorType == OwSensorType.DS1923) {
            properties.put(PROPERTY_MODELID, sensorType.toString());
            properties.put(PROPERTY_VENDOR, "Dallas/Maxim");
        } else {
            DS2438Configuration ds2438configuration = new DS2438Configuration(bridgeHandler, sensorId);

            sensorType = ds2438configuration.getSensorSubType();
            properties.put(PROPERTY_MODELID, sensorType.toString());

            String vendor = ds2438configuration.getVendor();
            properties.put(PROPERTY_VENDOR, vendor);
        }

        return properties;
    }
}
