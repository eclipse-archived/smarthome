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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.onewire.internal.OwDynamicStateDescriptionProvider;
import org.eclipse.smarthome.binding.onewire.internal.OwException;
import org.eclipse.smarthome.binding.onewire.internal.OwPageBuffer;
import org.eclipse.smarthome.binding.onewire.internal.device.DS18x20;
import org.eclipse.smarthome.binding.onewire.internal.device.DS2406_DS2413;
import org.eclipse.smarthome.binding.onewire.internal.device.DS2438;
import org.eclipse.smarthome.binding.onewire.internal.device.DS2438.LightSensorType;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
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
 * The {@link MultisensorThingHandler} is responsible for handling multisensors
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class MultisensorThingHandler extends OwBaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(
            Arrays.asList(THING_TYPE_MS_TH, THING_TYPE_MS_THS, THING_TYPE_MS_TV, THING_TYPE_AMS, THING_TYPE_BMS));

    private final Logger logger = LoggerFactory.getLogger(MultisensorThingHandler.class);

    private final ThingTypeUID thingType = this.thing.getThingTypeUID();
    private String prodDate = "";
    private int hwRevision = 0;

    private int digitalRefreshInterval = 10 * 1000;
    private long digitalLastRefresh = 0;

    public MultisensorThingHandler(Thing thing, OwDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
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

        if (configuration.get(CONFIG_DIGITALREFRESH) != null) {
            digitalRefreshInterval = ((BigDecimal) configuration.get(CONFIG_DIGITALREFRESH)).intValue() * 1000;
        } else {
            digitalRefreshInterval = 10 * 1000;
        }
        digitalLastRefresh = 0;

        // add sensors
        sensors.add(new DS2438(sensorIds.get(0), this));
        if (THING_TYPE_BMS.equals(thingType)) {
            sensors.add(new DS18x20(sensorIds.get(1), this));
        } else if (THING_TYPE_AMS.equals(thingType)) {
            sensors.add(new DS18x20(sensorIds.get(1), this));
            sensors.add(new DS2438(sensorIds.get(2), this));
            sensors.add(new DS2406_DS2413(sensorIds.get(3), this));
        }

        // AMS/BMS properties
        if (thingType == THING_TYPE_BMS || thingType == THING_TYPE_AMS) {
            if ((properties.get(PROPERTY_PROD_DATE) == null) || (properties.get(PROPERTY_HW_REVISION) == null)) {
                scheduler.execute(() -> {
                    updateSensorProperties();
                });
                return;
            } else {
                hwRevision = Integer.valueOf(properties.get(PROPERTY_HW_REVISION));
            }
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

    private void configureThingChannels() {
        Configuration configuration = getConfig();
        ThingBuilder thingBuilder = editThing();

        logger.debug("configuring sensors for {}", this.thing.getLabel());

        if (thing.getChannel(CHANNEL_SUPPLYVOLTAGE) != null) {
            sensors.get(0).enableChannel(CHANNEL_SUPPLYVOLTAGE);
        }

        // temperature channel
        if ((configuration.get(CONFIG_TEMPERATURESENSOR) != null)
                && configuration.get(CONFIG_TEMPERATURESENSOR).equals("DS18B20") && sensorCount > 1) {
            sensors.get(1).enableChannel(CHANNEL_TEMPERATURE);
        } else {
            sensors.get(0).enableChannel(CHANNEL_TEMPERATURE);
        }

        // first AI channel
        if (THING_TYPE_MS_TV.equals(thingType)) {
            sensors.get(0).enableChannel(CHANNEL_VOLTAGE);
        } else if (THING_TYPE_AMS.equals(thingType) || THING_TYPE_BMS.equals(thingType)
                || THING_TYPE_MS_TH.equals(thingType) || THING_TYPE_MS_THS.equals(thingType)) {
            sensors.get(0).enableChannel(CHANNEL_HUMIDITY);
            if (thing.getChannel(CHANNEL_ABSOLUTE_HUMIDITY) != null) {
                sensors.get(0).enableChannel(CHANNEL_ABSOLUTE_HUMIDITY);
            }
            if (thing.getChannel(CHANNEL_DEWPOINT) != null) {
                sensors.get(0).enableChannel(CHANNEL_DEWPOINT);
            }
        }

        // light/current sensor
        if ((thingType == THING_TYPE_AMS) || (thingType == THING_TYPE_BMS)) {
            if ((configuration.get(CONFIG_LIGHTSENSOR) != null) && ((Boolean) configuration.get(CONFIG_LIGHTSENSOR))) {
                sensors.get(0).enableChannel(CHANNEL_LIGHT);
                if (thing.getChannel(CHANNEL_CURRENT) != null) {
                    thingBuilder.withoutChannel(new ChannelUID(getThing().getUID(), CHANNEL_CURRENT));
                }
                if (thing.getChannel(CHANNEL_LIGHT) == null) {
                    thingBuilder.withChannel(ChannelBuilder
                            .create(new ChannelUID(getThing().getUID(), CHANNEL_LIGHT), "Number:Illuminance")
                            .withLabel("Light").withType(new ChannelTypeUID(BINDING_ID, "light")).build());
                }
                if (hwRevision <= 13) {
                    ((DS2438) sensors.get(0)).setLightSensorType(LightSensorType.ElabNetV1);
                } else {
                    ((DS2438) sensors.get(0)).setLightSensorType(LightSensorType.ElabNetV2);
                }
            } else {
                sensors.get(0).enableChannel(CHANNEL_CURRENT);
                if (thing.getChannel(CHANNEL_LIGHT) != null) {
                    thingBuilder.withoutChannel(new ChannelUID(getThing().getUID(), CHANNEL_LIGHT));
                }
                if (thing.getChannel(CHANNEL_CURRENT) == null) {
                    thingBuilder.withChannel(ChannelBuilder
                            .create(new ChannelUID(getThing().getUID(), CHANNEL_CURRENT), "Number:Current")
                            .withLabel("Current").withType(new ChannelTypeUID(BINDING_ID, "current")).build());
                }
            }
        }

        // second AI channel
        if (THING_TYPE_AMS.equals(thingType)) {
            sensors.get(2).enableChannel(CHANNEL_VOLTAGE);
        }

        // digital channels
        if (THING_TYPE_AMS.equals(thingType)) {
            if (configuration.get(CONFIG_DIGITALREFRESH) != null) {
                digitalRefreshInterval = ((BigDecimal) configuration.get(CONFIG_DIGITALREFRESH)).intValue() * 1000;
            } else {
                // default 10ms
                digitalRefreshInterval = 10 * 1000;
            }
        }

        updateThing(thingBuilder.build());

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
    protected void updateSensorProperties() {
        Map<String, String> properties = editProperties();

        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("updating thing properties failed, no bridge available");
            scheduler.schedule(() -> {
                updateSensorProperties();
            }, 5000, TimeUnit.MILLISECONDS);
            return;
        }

        OwBaseBridgeHandler bridgeHandler = (OwBaseBridgeHandler) bridge.getHandler();
        try {
            if (bridgeHandler == null) {
                throw new OwException("no bridge handler available");
            }
            OwPageBuffer pages = bridgeHandler.readPages(sensorIds.get(0));
            prodDate = String.format("%d/%d", pages.getByte(5, 0), 256 * pages.getByte(5, 1) + pages.getByte(5, 2));
            hwRevision = pages.getByte(5, 3);
            properties.put(PROPERTY_PROD_DATE, prodDate);
            properties.put(PROPERTY_HW_REVISION, String.valueOf(hwRevision));
            logger.trace("set production date {}, revision {}", prodDate, hwRevision);
        } catch (OwException e) {
            logger.info("updating thing properties failed: {}", e.getMessage());
            scheduler.schedule(() -> {
                updateSensorProperties();
            }, 5000, TimeUnit.MILLISECONDS);
            return;
        }

        updateProperties(properties);
    }
}
