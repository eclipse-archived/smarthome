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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.onewire.internal.DS2438Configuration;
import org.eclipse.smarthome.binding.onewire.internal.OwDynamicStateDescriptionProvider;
import org.eclipse.smarthome.binding.onewire.internal.OwException;
import org.eclipse.smarthome.binding.onewire.internal.OwPageBuffer;
import org.eclipse.smarthome.binding.onewire.internal.device.DS1923;
import org.eclipse.smarthome.binding.onewire.internal.device.DS2438;
import org.eclipse.smarthome.binding.onewire.internal.device.DS2438.CurrentSensorType;
import org.eclipse.smarthome.binding.onewire.internal.device.DS2438.LightSensorType;
import org.eclipse.smarthome.binding.onewire.internal.device.OwSensorType;
import org.eclipse.smarthome.core.thing.Channel;
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
 * The {@link BasicMultisensorThingHandler} is responsible for handling DS2438/DS1923 based multisensors (single
 * sensors)
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class BasicMultisensorThingHandler extends OwBaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(
            Arrays.asList(THING_TYPE_MS_TX, THING_TYPE_MS_TH, THING_TYPE_MS_TV));

    private final Logger logger = LoggerFactory.getLogger(BasicMultisensorThingHandler.class);
    private OwSensorType sensorType = OwSensorType.UNKNOWN;

    public BasicMultisensorThingHandler(Thing thing,
            OwDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        super(thing, dynamicStateDescriptionProvider);
    }

    @Override
    public void initialize() {
        if (!thing.getThingTypeUID().equals(THING_TYPE_MS_TX)) {
            changeThingType(THING_TYPE_MS_TX, getConfig());
        }

        Map<String, String> properties = editProperties();

        if (!super.configure()) {
            return;
        }

        if (getThing().getStatus() == ThingStatus.OFFLINE) {
            return;
        }

        if (!properties.containsKey(PROPERTY_MODELID)) {
            updateSensorProperties();
            return;
        }
        sensorType = OwSensorType.valueOf(properties.get(PROPERTY_MODELID));

        // add sensors
        if (sensorType == OwSensorType.DS1923) {
            sensors.add(new DS1923(sensorIds.get(0), this));
        } else {
            sensors.add(new DS2438(sensorIds.get(0), this));
        }

        scheduler.execute(() -> {
            configureThingChannels();
        });
    }

    private void configureThingChannels() {
        ThingBuilder thingBuilder = editThing();
        boolean isEdited = false;

        // temperature channel (present on all devices)
        sensors.get(0).enableChannel(CHANNEL_TEMPERATURE);

        // supply voltage (all sensors, except DS1923)
        Channel supplyVoltageChannel = thing.getChannel(CHANNEL_SUPPLYVOLTAGE);
        if (sensorType == OwSensorType.DS1923) {
            if (supplyVoltageChannel != null) {
                thingBuilder.withoutChannel(supplyVoltageChannel.getUID());
                isEdited = true;
            }
        } else {
            sensors.get(0).enableChannel(CHANNEL_SUPPLYVOLTAGE);
        }

        // analog channel
        switch (sensorType) {
            case MS_T:
                // no other channels
                break;
            case DS1923:
                // DS1923 has fixed humidity sensor on-board
                if (thing.getChannel(CHANNEL_HUMIDITY) == null) {
                    thingBuilder.withChannel(ChannelBuilder
                            .create(new ChannelUID(thing.getUID(), CHANNEL_HUMIDITY), "Number:Dimensionless")
                            .withLabel("Humidity").withType(new ChannelTypeUID(BINDING_ID, "humidity")).build());
                    isEdited = true;
                }
                sensors.get(0).enableChannel(CHANNEL_HUMIDITY);
                break;
            case MS_TC:
                if (thing.getChannel(CHANNEL_CURRENT) == null) {
                    thingBuilder.withChannel(
                            ChannelBuilder.create(new ChannelUID(thing.getUID(), CHANNEL_LIGHT), "Number:Current")
                                    .withLabel("Current").withType(new ChannelTypeUID(BINDING_ID, "current")).build());
                    isEdited = true;
                    sensors.get(0).enableChannel(CHANNEL_CURRENT);
                    ((DS2438) sensors.get(0)).setCurrentSensorType(CurrentSensorType.IBUTTONLINK);
                }
                sensors.get(0).enableChannel(CHANNEL_LIGHT);
            case MS_TH:
                // DS2438 can have different sensors
                if (thing.getChannel(CHANNEL_HUMIDITY) == null) {
                    thingBuilder.withChannel(ChannelBuilder
                            .create(new ChannelUID(thing.getUID(), CHANNEL_HUMIDITY), "Number:Dimensionless")
                            .withLabel("Humidity").withType(new ChannelTypeUID(BINDING_ID, "humidityconf")).build());
                    isEdited = true;
                }
                sensors.get(0).enableChannel(CHANNEL_HUMIDITY);
                break;
            case MS_TL:
                if (thing.getChannel(CHANNEL_LIGHT) == null) {
                    thingBuilder.withChannel(
                            ChannelBuilder.create(new ChannelUID(thing.getUID(), CHANNEL_LIGHT), "Number:Illuminance")
                                    .withLabel("Light").withType(new ChannelTypeUID(BINDING_ID, "light")).build());
                    isEdited = true;
                }
                sensors.get(0).enableChannel(CHANNEL_LIGHT);
                ((DS2438) sensors.get(0)).setLightSensorType(LightSensorType.IBUTTONLINK);
                break;
            default:
                // use voltage channel as default
                if (thing.getChannel(CHANNEL_VOLTAGE) == null) {
                    thingBuilder.withChannel(ChannelBuilder
                            .create(new ChannelUID(thing.getUID(), CHANNEL_VOLTAGE), "Number:ElectricPotential")
                            .withLabel("Voltage").withType(new ChannelTypeUID(BINDING_ID, "voltage")).build());
                    isEdited = true;
                }
                sensors.get(0).enableChannel(CHANNEL_VOLTAGE);
        }

        // if humidity sensor, add additional channels
        if (sensorType == OwSensorType.DS1923 || sensorType == OwSensorType.MS_TH) {
            if (thing.getChannel(CHANNEL_ABSOLUTE_HUMIDITY) == null) {
                thingBuilder.withChannel(ChannelBuilder
                        .create(new ChannelUID(thing.getUID(), CHANNEL_ABSOLUTE_HUMIDITY), "Number:Density")
                        .withLabel("Abs. Humidity").withType(new ChannelTypeUID(BINDING_ID, "abshumidity")).build());
                isEdited = true;
            }
            sensors.get(0).enableChannel(CHANNEL_ABSOLUTE_HUMIDITY);
            if (thing.getChannel(CHANNEL_DEWPOINT) == null) {
                thingBuilder.withChannel(
                        ChannelBuilder.create(new ChannelUID(thing.getUID(), CHANNEL_DEWPOINT), "Number:Temperature")
                                .withLabel("Dewpoint").withType(new ChannelTypeUID(BINDING_ID, "dewpoint")).build());
                isEdited = true;
            }
            sensors.get(0).enableChannel(CHANNEL_DEWPOINT);
        }

        if (isEdited) {
            updateThing(thingBuilder.build());
        }

        try {
            sensors.get(0).configureChannels();
        } catch (OwException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        }

        validConfig = true;
        updatePresenceStatus(UnDefType.UNDEF);
    }

    @Override
    public Map<String, String> updateSensorProperties(OwBaseBridgeHandler bridgeHandler) throws OwException {
        Map<String, String> properties = new HashMap<String, String>();
        sensorType = bridgeHandler.getType(sensorIds.get(0));

        if (sensorType == OwSensorType.DS1923) {
            properties.put(PROPERTY_MODELID, sensorType.toString());
            properties.put(PROPERTY_VENDOR, "Dallas/Maxim");
        } else {
            OwPageBuffer pages = bridgeHandler.readPages(sensorIds.get(0));
            DS2438Configuration ds2438configuration = new DS2438Configuration(pages);

            sensorType = ds2438configuration.getSensorSubType();
            properties.put(PROPERTY_MODELID, sensorType.toString());

            String vendor = ds2438configuration.getVendor();
            properties.put(PROPERTY_VENDOR, vendor);
        }

        return properties;
    }
}
