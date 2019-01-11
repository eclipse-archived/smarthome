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
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
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

        ThingStatusInfo statusInfo = getThing().getStatusInfo();
        if (statusInfo.getStatus() == ThingStatus.OFFLINE
                && statusInfo.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR) {
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

        // temperature channel (present on all devices)
        sensors.get(0).enableChannel(CHANNEL_TEMPERATURE);

        // supply voltage (all sensors, except DS1923)
        if (sensorType == OwSensorType.DS1923) {
            removeChannelIfExisting(thingBuilder, CHANNEL_SUPPLYVOLTAGE);
        } else {
            addChannelIfMissing(thingBuilder, CHANNEL_SUPPLYVOLTAGE, CHANNEL_TYPE_UID_VOLTAGE, "Supply Voltage");
            sensors.get(0).enableChannel(CHANNEL_SUPPLYVOLTAGE);
        }

        // analog channel
        switch (sensorType) {
            case DS2438:
                addChannelIfMissing(thingBuilder, CHANNEL_VOLTAGE, CHANNEL_TYPE_UID_VOLTAGE);
                addChannelIfMissing(thingBuilder, CHANNEL_CURRENT, CHANNEL_TYPE_UID_CURRENT);
                removeChannelIfExisting(thingBuilder, CHANNEL_HUMIDITY);
                removeChannelIfExisting(thingBuilder, CHANNEL_ABSOLUTE_HUMIDITY);
                removeChannelIfExisting(thingBuilder, CHANNEL_DEWPOINT);
                removeChannelIfExisting(thingBuilder, CHANNEL_LIGHT);
                ((DS2438) sensors.get(0)).setCurrentSensorType(CurrentSensorType.INTERNAL);
                sensors.get(0).enableChannel(CHANNEL_VOLTAGE);
                sensors.get(0).enableChannel(CHANNEL_CURRENT);
                break;
            case DS1923:
                // DS1923 has fixed humidity sensor on-board
                addChannelIfMissing(thingBuilder, CHANNEL_HUMIDITY, CHANNEL_TYPE_UID_HUMIDITY);
                addChannelIfMissing(thingBuilder, CHANNEL_ABSOLUTE_HUMIDITY, CHANNEL_TYPE_UID_ABSHUMIDITY);
                addChannelIfMissing(thingBuilder, CHANNEL_DEWPOINT, CHANNEL_TYPE_UID_DEWPOINT);
                removeChannelIfExisting(thingBuilder, CHANNEL_LIGHT);
                removeChannelIfExisting(thingBuilder, CHANNEL_CURRENT);
                removeChannelIfExisting(thingBuilder, CHANNEL_VOLTAGE);
                sensors.get(0).enableChannel(CHANNEL_HUMIDITY);
                sensors.get(0).enableChannel(CHANNEL_ABSOLUTE_HUMIDITY);
                sensors.get(0).enableChannel(CHANNEL_DEWPOINT);

                break;
            case MS_TC:
                addChannelIfMissing(thingBuilder, CHANNEL_CURRENT, CHANNEL_TYPE_UID_CURRENT);
                removeChannelIfExisting(thingBuilder, CHANNEL_HUMIDITY);
                removeChannelIfExisting(thingBuilder, CHANNEL_ABSOLUTE_HUMIDITY);
                removeChannelIfExisting(thingBuilder, CHANNEL_DEWPOINT);
                removeChannelIfExisting(thingBuilder, CHANNEL_LIGHT);
                removeChannelIfExisting(thingBuilder, CHANNEL_VOLTAGE);
                ((DS2438) sensors.get(0)).setCurrentSensorType(CurrentSensorType.IBUTTONLINK);
                sensors.get(0).enableChannel(CHANNEL_CURRENT);
                break;
            case MS_TH:
                // DS2438 can have different sensors
                addChannelIfMissing(thingBuilder, CHANNEL_HUMIDITY, CHANNEL_TYPE_UID_HUMIDITYCONF);
                addChannelIfMissing(thingBuilder, CHANNEL_ABSOLUTE_HUMIDITY, CHANNEL_TYPE_UID_ABSHUMIDITY);
                addChannelIfMissing(thingBuilder, CHANNEL_DEWPOINT, CHANNEL_TYPE_UID_DEWPOINT);
                removeChannelIfExisting(thingBuilder, CHANNEL_LIGHT);
                removeChannelIfExisting(thingBuilder, CHANNEL_VOLTAGE);
                removeChannelIfExisting(thingBuilder, CHANNEL_CURRENT);
                sensors.get(0).enableChannel(CHANNEL_HUMIDITY);
                sensors.get(0).enableChannel(CHANNEL_ABSOLUTE_HUMIDITY);
                sensors.get(0).enableChannel(CHANNEL_DEWPOINT);
                break;
            case MS_TL:
                addChannelIfMissing(thingBuilder, CHANNEL_LIGHT, CHANNEL_TYPE_UID_LIGHT);
                removeChannelIfExisting(thingBuilder, CHANNEL_HUMIDITY);
                removeChannelIfExisting(thingBuilder, CHANNEL_ABSOLUTE_HUMIDITY);
                removeChannelIfExisting(thingBuilder, CHANNEL_DEWPOINT);
                removeChannelIfExisting(thingBuilder, CHANNEL_CURRENT);
                removeChannelIfExisting(thingBuilder, CHANNEL_VOLTAGE);
                sensors.get(0).enableChannel(CHANNEL_LIGHT);
                ((DS2438) sensors.get(0)).setLightSensorType(LightSensorType.IBUTTONLINK);
                break;
            default:
                // use voltage channel as default
                addChannelIfMissing(thingBuilder, CHANNEL_VOLTAGE, CHANNEL_TYPE_UID_VOLTAGE);
                removeChannelIfExisting(thingBuilder, CHANNEL_HUMIDITY);
                removeChannelIfExisting(thingBuilder, CHANNEL_ABSOLUTE_HUMIDITY);
                removeChannelIfExisting(thingBuilder, CHANNEL_DEWPOINT);
                removeChannelIfExisting(thingBuilder, CHANNEL_CURRENT);
                removeChannelIfExisting(thingBuilder, CHANNEL_LIGHT);
                sensors.get(0).enableChannel(CHANNEL_VOLTAGE);
        }

        updateThing(thingBuilder.build());

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
