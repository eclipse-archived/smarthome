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
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EDSSensorThingHandler} is responsible for handling EDS multisensors
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class EDSSensorThingHandler extends OwBaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_EDS_ENV);
    private static final Set<OwSensorType> SUPPORTED_SENSOR_TYPES = Collections
            .unmodifiableSet(Stream.of(OwSensorType.EDS0064, OwSensorType.EDS0065, OwSensorType.EDS0066,
                    OwSensorType.EDS0067, OwSensorType.EDS0068).collect(Collectors.toSet()));

    private final Logger logger = LoggerFactory.getLogger(EDSSensorThingHandler.class);

    private OwSensorType sensorType = OwSensorType.UNKNOWN;

    public EDSSensorThingHandler(Thing thing, OwDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
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

        // add sensors
        sensors.add(new EDS006x(sensorIds.get(0), this));

        // check sensors
        if (!properties.containsKey(PROPERTY_MODELID)) {
            updateSensorProperties();
            return;
        } else {
            sensorType = OwSensorType.valueOf(properties.get(PROPERTY_MODELID));
            ((EDS006x) sensors.get(0)).configureChannels(sensorType);
        }

        scheduler.execute(() -> {
            configureThingChannels();
        });
    }

    private void configureThingChannels() {
        ThingBuilder thingBuilder = editThing();

        logger.debug("configuring sensors for {}", this.thing.getLabel());

        try {
            EDS006x sensor = (EDS006x) sensors.get(0);
            sensor.configureChannels(sensorType);

            switch (sensorType) {
                case EDS0064:
                    sensor.enableChannel(CHANNEL_TEMPERATURE);
                    break;
                case EDS0065:
                    sensor.enableChannel(CHANNEL_TEMPERATURE);
                    sensor.enableChannel(CHANNEL_HUMIDITY);
                    sensor.enableChannel(CHANNEL_ABSOLUTE_HUMIDITY);
                    sensor.enableChannel(CHANNEL_DEWPOINT);
                    break;
                case EDS0066:
                    sensor.enableChannel(CHANNEL_TEMPERATURE);
                    sensor.enableChannel(CHANNEL_PRESSURE);
                    break;
                case EDS0067:
                    sensor.enableChannel(CHANNEL_TEMPERATURE);
                    sensor.enableChannel(CHANNEL_LIGHT);
                    break;
                case EDS0068:
                    sensor.enableChannel(CHANNEL_TEMPERATURE);
                    sensor.enableChannel(CHANNEL_HUMIDITY);
                    sensor.enableChannel(CHANNEL_ABSOLUTE_HUMIDITY);
                    sensor.enableChannel(CHANNEL_DEWPOINT);
                    sensor.enableChannel(CHANNEL_PRESSURE);
                    sensor.enableChannel(CHANNEL_LIGHT);
                    break;
                default:
                    throw new OwException("sensor not supported");
            }
        } catch (OwException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        }

        // humidity sensor
        if ((sensorType == OwSensorType.EDS0065) || (sensorType == OwSensorType.EDS0068)) {
            if (thing.getChannel(CHANNEL_HUMIDITY) == null) {
                thingBuilder.withChannel(ChannelBuilder
                        .create(new ChannelUID(getThing().getUID(), CHANNEL_HUMIDITY), "Number:Dimensionless")
                        .withLabel("Humidity").withType(new ChannelTypeUID(BINDING_ID, "humidity")).build());
            }
            if (thing.getChannel(CHANNEL_DEWPOINT) == null) {
                thingBuilder.withChannel(ChannelBuilder
                        .create(new ChannelUID(getThing().getUID(), CHANNEL_DEWPOINT), "Number:Temperature")
                        .withLabel("Dewpoint").withType(new ChannelTypeUID(BINDING_ID, "dewpoint")).build());
            }
            if (thing.getChannel(CHANNEL_ABSOLUTE_HUMIDITY) == null) {
                thingBuilder.withChannel(ChannelBuilder
                        .create(new ChannelUID(getThing().getUID(), CHANNEL_ABSOLUTE_HUMIDITY), "Number:Density")
                        .withLabel("Abs. Humidity").withType(new ChannelTypeUID(BINDING_ID, "absolutehumidity"))
                        .build());
            }
        } else {
            if (thing.getChannel(CHANNEL_HUMIDITY) != null) {
                thingBuilder.withoutChannel(new ChannelUID(getThing().getUID(), CHANNEL_HUMIDITY));
            }
            if (thing.getChannel(CHANNEL_DEWPOINT) != null) {
                thingBuilder.withoutChannel(new ChannelUID(getThing().getUID(), CHANNEL_DEWPOINT));
            }
            if (thing.getChannel(CHANNEL_ABSOLUTE_HUMIDITY) != null) {
                thingBuilder.withoutChannel(new ChannelUID(getThing().getUID(), CHANNEL_ABSOLUTE_HUMIDITY));
            }
        }

        // pressure sensor
        if ((sensorType == OwSensorType.EDS0066) || (sensorType == OwSensorType.EDS0068)) {
            if (thing.getChannel(CHANNEL_PRESSURE) == null) {
                thingBuilder.withChannel(
                        ChannelBuilder.create(new ChannelUID(getThing().getUID(), CHANNEL_PRESSURE), "Number:Pressure")
                                .withLabel("Pressure").withType(new ChannelTypeUID(BINDING_ID, "pressure")).build());
            }
        } else {
            if (thing.getChannel(CHANNEL_PRESSURE) != null) {
                thingBuilder.withoutChannel(new ChannelUID(getThing().getUID(), CHANNEL_PRESSURE));
            }
        }

        // light sensor
        if ((sensorType == OwSensorType.EDS0067) || (sensorType == OwSensorType.EDS0068)) {
            if (thing.getChannel(CHANNEL_LIGHT) == null) {
                thingBuilder.withChannel(
                        ChannelBuilder.create(new ChannelUID(getThing().getUID(), CHANNEL_LIGHT), "Number:Illuminance")
                                .withLabel("Illuminance").withType(new ChannelTypeUID(BINDING_ID, "light")).build());
            }
        } else {
            if (thing.getChannel(CHANNEL_LIGHT) != null) {
                thingBuilder.withoutChannel(new ChannelUID(getThing().getUID(), CHANNEL_LIGHT));
            }
        }

        updateThing(thingBuilder.build());

        validConfig = true;
        updatePresenceStatus(UnDefType.UNDEF);
    }

    @Override
    public Map<String, String> updateSensorProperties(OwBaseBridgeHandler bridgeHandler) throws OwException {
        Map<String, String> properties = new HashMap<String, String>();

        OwPageBuffer pages = bridgeHandler.readPages(sensorIds.get(0));

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
