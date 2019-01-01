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

import java.math.BigDecimal;
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
import org.eclipse.smarthome.binding.onewire.internal.Util;
import org.eclipse.smarthome.binding.onewire.internal.device.DS18x20;
import org.eclipse.smarthome.binding.onewire.internal.device.DS2406_DS2413;
import org.eclipse.smarthome.binding.onewire.internal.device.DS2438;
import org.eclipse.smarthome.binding.onewire.internal.device.DS2438.LightSensorType;
import org.eclipse.smarthome.binding.onewire.internal.device.OwSensorType;
import org.eclipse.smarthome.config.core.Configuration;
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
 * The {@link AdvancedMultisensorThingHandler} is responsible for handling DS2438 based multisensors (modules)
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class AdvancedMultisensorThingHandler extends OwBaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(
            Arrays.asList(THING_TYPE_AMS, THING_TYPE_BMS));

    private final Logger logger = LoggerFactory.getLogger(AdvancedMultisensorThingHandler.class);

    private final ThingTypeUID thingType = this.thing.getThingTypeUID();
    private int hwRevision = 0;
    private OwSensorType sensorType = OwSensorType.UNKNOWN;

    private int digitalRefreshInterval = 10 * 1000;
    private long digitalLastRefresh = 0;

    public AdvancedMultisensorThingHandler(Thing thing,
            OwDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
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

        if (configuration.containsKey(CONFIG_DIGITALREFRESH)) {
            digitalRefreshInterval = ((BigDecimal) configuration.get(CONFIG_DIGITALREFRESH)).intValue() * 1000;
        } else {
            digitalRefreshInterval = 10 * 1000;
        }
        digitalLastRefresh = 0;

        if (!properties.containsKey(PROPERTY_MODELID) || !properties.containsKey(PROPERTY_PROD_DATE)
                || !properties.containsKey(PROPERTY_HW_REVISION)) {
            updateSensorProperties();
            return;
        } else {
            sensorType = OwSensorType.valueOf(properties.get(PROPERTY_MODELID));
            hwRevision = Integer.valueOf(properties.get(PROPERTY_HW_REVISION));
        }

        // add sensors

        sensors.add(new DS2438(sensorIds.get(0), this));
        sensors.add(new DS18x20(sensorIds.get(1), this));

        if (THING_TYPE_AMS.equals(thingType)) {
            sensors.add(new DS2438(sensorIds.get(2), this));
            sensors.add(new DS2406_DS2413(sensorIds.get(3), this));
        }

        scheduler.execute(() -> {
            configureThingChannels();
        });
    }

    @Override
    public void refresh(OwBaseBridgeHandler bridgeHandler, long now) {
        try {
            if ((now >= (digitalLastRefresh + digitalRefreshInterval)) && (thingType == THING_TYPE_AMS)) {
                logger.trace("refreshing digital {}", this.thing.getUID());

                Boolean forcedRefresh = digitalLastRefresh == 0;
                digitalLastRefresh = now;

                if (!sensors.get(3).checkPresence(bridgeHandler)) {
                    return;
                }

                sensors.get(3).refresh(bridgeHandler, forcedRefresh);
            }

            if (now >= (lastRefresh + refreshInterval)) {
                if (!sensors.get(0).checkPresence(bridgeHandler)) {
                    return;
                }

                logger.trace("refreshing analog {}", this.thing.getUID());

                Boolean forcedRefresh = lastRefresh == 0;
                lastRefresh = now;

                if (thingType.equals(THING_TYPE_AMS)) {
                    for (int i = 0; i < sensorCount - 1; i++) {
                        sensors.get(i).refresh(bridgeHandler, forcedRefresh);
                    }
                } else {
                    for (int i = 0; i < sensorCount; i++) {
                        sensors.get(i).refresh(bridgeHandler, forcedRefresh);
                    }
                }
            }
        } catch (OwException e) {
            logger.debug("{}: refresh exception '{}'", this.thing.getUID(), e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "refresh exception");
        }
    }

    private boolean configureSupplyVoltageChannel(ThingBuilder thingBuilder) {
        Channel supplyVoltageChannel = thing.getChannel(CHANNEL_SUPPLYVOLTAGE);

        // not present in DS1923
        if (sensorType == OwSensorType.DS1923) {
            if (supplyVoltageChannel != null) {
                thingBuilder.withoutChannel(supplyVoltageChannel.getUID());
                return true;
            }
        } else if (supplyVoltageChannel == null) {
            thingBuilder.withChannel(ChannelBuilder
                    .create(new ChannelUID(getThing().getUID(), CHANNEL_SUPPLYVOLTAGE), "Number:ElectricPotential")
                    .withLabel("Supply Voltage").withType(new ChannelTypeUID(BINDING_ID, "voltage")).build());
            sensors.get(0).enableChannel(CHANNEL_SUPPLYVOLTAGE);
            return true;
        }
        return false;
    }

    private void configureThingChannels() {
        Configuration configuration = getConfig();
        ThingBuilder thingBuilder = editThing();
        boolean isEdited = false;

        // temperature channel
        Channel temperatureChannel = thing.getChannel(CHANNEL_TEMPERATURE);
        if (temperatureChannel == null) {
            // this is strange - there should always be to be a temperature channel
            temperatureChannel = Util.buildTemperatureChannel(thing.getUID(), CHANNEL_TYPE_UID_TEMPERATURE,
                    new Configuration());
            thingBuilder.withChannel(temperatureChannel);
            isEdited = true;
        }

        // always use HIH-4000 on ElabNet sensors.
        Channel humidityChannel = thing.getChannel(CHANNEL_HUMIDITY);
        if (humidityChannel != null && !humidityChannel.getConfiguration().containsKey(CONFIG_HUMIDITY)) {
            thingBuilder.withoutChannel(humidityChannel.getUID());
            thingBuilder.withChannel(ChannelBuilder.create(humidityChannel.getUID(), "Number:Dimensionless")
                    .withLabel("Humidity").withType(new ChannelTypeUID(BINDING_ID, "humidity"))
                    .withConfiguration(new Configuration(new HashMap<String, Object>() {
                        {
                            put(CONFIG_HUMIDITY, "/HIH4000/humidity");
                        }
                    })).build());
        }

        if (configuration.containsKey(CONFIG_TEMPERATURESENSOR)
                && configuration.get(CONFIG_TEMPERATURESENSOR).equals("DS18B20")) {
            // use DS18B20 for temperature
            sensors.get(1).enableChannel(CHANNEL_TEMPERATURE);
            if (!CHANNEL_TYPE_UID_TEMPERATURE_POR_RES.equals(temperatureChannel.getChannelTypeUID())) {
                thingBuilder.withoutChannel(temperatureChannel.getUID());
                thingBuilder.withChannel(Util.buildTemperatureChannel(thing.getUID(),
                        CHANNEL_TYPE_UID_TEMPERATURE_POR_RES, temperatureChannel.getConfiguration()));
                isEdited = true;
            }
        } else {
            // use standard temperature channel
            sensors.get(0).enableChannel(CHANNEL_TEMPERATURE);
            if (!CHANNEL_TYPE_UID_TEMPERATURE.equals(temperatureChannel.getChannelTypeUID())) {
                thingBuilder.withoutChannel(temperatureChannel.getUID());
                thingBuilder.withChannel(Util.buildTemperatureChannel(thing.getUID(), CHANNEL_TYPE_UID_TEMPERATURE,
                        temperatureChannel.getConfiguration()));
                isEdited = true;
            }
        }

        // standard channels on all AMS/BMS
        sensors.get(0).enableChannel(CHANNEL_HUMIDITY);
        sensors.get(0).enableChannel(CHANNEL_ABSOLUTE_HUMIDITY);
        sensors.get(0).enableChannel(CHANNEL_DEWPOINT);
        sensors.get(0).enableChannel(CHANNEL_SUPPLYVOLTAGE);

        // light/current sensor
        if (configuration.containsKey(CONFIG_LIGHTSENSOR) && ((Boolean) configuration.get(CONFIG_LIGHTSENSOR))) {
            sensors.get(0).enableChannel(CHANNEL_LIGHT);
            if (thing.getChannel(CHANNEL_CURRENT) != null) {
                thingBuilder.withoutChannel(new ChannelUID(getThing().getUID(), CHANNEL_CURRENT));
                isEdited = true;
            }
            if (thing.getChannel(CHANNEL_LIGHT) == null) {
                thingBuilder.withChannel(
                        ChannelBuilder.create(new ChannelUID(getThing().getUID(), CHANNEL_LIGHT), "Number:Illuminance")
                                .withLabel("Light").withType(new ChannelTypeUID(BINDING_ID, "light")).build());
                isEdited = true;
            }
            if (hwRevision <= 13) {
                ((DS2438) sensors.get(0)).setLightSensorType(LightSensorType.ELABNET_V1);
            } else {
                ((DS2438) sensors.get(0)).setLightSensorType(LightSensorType.ELABNET_V2);
            }
        } else {
            sensors.get(0).enableChannel(CHANNEL_CURRENT);
            if (thing.getChannel(CHANNEL_LIGHT) != null) {
                thingBuilder.withoutChannel(new ChannelUID(getThing().getUID(), CHANNEL_LIGHT));
                isEdited = true;
            }
            if (thing.getChannel(CHANNEL_CURRENT) == null) {
                thingBuilder.withChannel(
                        ChannelBuilder.create(new ChannelUID(getThing().getUID(), CHANNEL_CURRENT), "Number:Current")
                                .withLabel("Current").withType(new ChannelTypeUID(BINDING_ID, "current")).build());
                isEdited = true;
            }
        }

        // additional sensors on AMS
        if (THING_TYPE_AMS.equals(thingType)) {
            sensors.get(2).enableChannel(CHANNEL_VOLTAGE);

            if (configuration.containsKey(CONFIG_DIGITALREFRESH)) {
                digitalRefreshInterval = ((BigDecimal) configuration.get(CONFIG_DIGITALREFRESH)).intValue() * 1000;
            } else {
                // default 10ms
                digitalRefreshInterval = 10 * 1000;
            }
        }

        if (isEdited) {
            updateThing(thingBuilder.build());
        }

        try {
            for (int i = 0; i < sensorCount; i++) {
                sensors.get(i).configureChannels();
            }
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
        OwPageBuffer pages = bridgeHandler.readPages(sensorIds.get(0));
        DS2438Configuration ds2438configuration = new DS2438Configuration(pages);

        sensorType = DS2438Configuration.getMultisensorType(ds2438configuration.getSensorSubType(),
                ds2438configuration.getAssociatedSensorTypes());
        properties.put(PROPERTY_MODELID, sensorType.toString());
        properties.put(PROPERTY_VENDOR, ds2438configuration.getVendor());

        properties.put(PROPERTY_PROD_DATE, ds2438configuration.getProductionDate());
        properties.put(PROPERTY_HW_REVISION, ds2438configuration.getHardwareRevision());

        return properties;
    }
}
