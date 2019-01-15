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

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.onewire.internal.OwDynamicStateDescriptionProvider;
import org.eclipse.smarthome.binding.onewire.internal.OwException;
import org.eclipse.smarthome.binding.onewire.internal.OwPageBuffer;
import org.eclipse.smarthome.binding.onewire.internal.device.EDS006x;
import org.eclipse.smarthome.binding.onewire.internal.device.OwSensorType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link EDSSensorThingHandler} is responsible for handling EDS multisensors
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class EDSSensorThingHandler extends OwBaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_EDS_ENV);
    public static final Set<OwSensorType> SUPPORTED_SENSOR_TYPES = Collections
            .unmodifiableSet(Stream.of(OwSensorType.EDS0064, OwSensorType.EDS0065, OwSensorType.EDS0066,
                    OwSensorType.EDS0067, OwSensorType.EDS0068).collect(Collectors.toSet()));
    private static final Set<String> REQUIRED_PROPERTIES = Collections.singleton(PROPERTY_HW_REVISION);

    public EDSSensorThingHandler(Thing thing, OwDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        super(thing, dynamicStateDescriptionProvider, SUPPORTED_SENSOR_TYPES, REQUIRED_PROPERTIES);
    }

    @Override
    public void initialize() {
        if (!super.configureThingHandler()) {
            return;
        }

        // add sensors
        sensors.add(new EDS006x(sensorId, sensorType, this));

        scheduler.execute(() -> {
            configureThingChannels();
        });
    }

    @Override
    public Map<String, String> updateSensorProperties(OwBaseBridgeHandler bridgeHandler) throws OwException {
        Map<String, String> properties = new HashMap<String, String>();

        OwPageBuffer pages = bridgeHandler.readPages(sensorId);

        OwSensorType sensorType = OwSensorType.UNKNOWN;
        try {
            sensorType = OwSensorType.valueOf(new String(pages.getPage(0), 0, 7, StandardCharsets.US_ASCII));
        } catch (IllegalArgumentException e) {
        }

        if (!SUPPORTED_SENSOR_TYPES.contains(sensorType)) {
            throw new OwException("sensorType not supported for EDSSensorThing");
        }

        int fwRevisionLow = pages.getByte(3, 3);
        int fwRevisionHigh = pages.getByte(3, 4);
        String fwRevision = String.format("%d.%d", fwRevisionHigh, fwRevisionLow);

        properties.put(PROPERTY_MODELID, sensorType.name());
        properties.put(PROPERTY_VENDOR, "Embedded Data Systems");
        properties.put(PROPERTY_HW_REVISION, String.valueOf(fwRevision));

        return properties;
    }
}
