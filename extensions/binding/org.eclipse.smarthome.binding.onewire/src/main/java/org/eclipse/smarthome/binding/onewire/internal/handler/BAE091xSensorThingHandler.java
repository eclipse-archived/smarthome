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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.onewire.internal.OwDynamicStateDescriptionProvider;
import org.eclipse.smarthome.binding.onewire.internal.config.BAE091xHandlerConfiguration;
import org.eclipse.smarthome.binding.onewire.internal.device.BAE0910;
import org.eclipse.smarthome.binding.onewire.internal.device.OwSensorType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BAE091xSensorThingHandler} is responsible for handling BAE0910 based multisensors
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class BAE091xSensorThingHandler extends OwBaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BAE091X);

    private final Logger logger = LoggerFactory.getLogger(BAE091xSensorThingHandler.class);
    private OwSensorType sensorType = OwSensorType.UNKNOWN;

    public BAE091xSensorThingHandler(Thing thing, OwDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        super(thing, dynamicStateDescriptionProvider);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType) {
            if (channelUID.getId().startsWith(CHANNEL_DIGITAL)) {
                Bridge bridge = getBridge();
                if (bridge != null) {
                    OwBaseBridgeHandler bridgeHandler = (OwBaseBridgeHandler) bridge.getHandler();
                    if (bridgeHandler != null) {
                        if (!((BAE0910) sensors.get(0)).writeChannel(bridgeHandler, channelUID.getId(), command)) {
                            logger.debug("writing to channel {} in thing {} not permitted (input channel)", channelUID,
                                    this.thing.getUID());
                        }
                    } else {
                        logger.warn("bridge handler not found");
                    }
                } else {
                    logger.warn("bridge not found");
                }
            }
        }
        // TODO: PWM channels
        super.handleCommand(channelUID, command);
    }

    @Override
    public void initialize() {
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
            scheduler.execute(() -> {
                updateSensorProperties();
            });
            return;
        }
        sensorType = OwSensorType.valueOf(properties.get(PROPERTY_MODELID));

        // add sensors
        if (sensorType == OwSensorType.BAE0910) {
            sensors.add(new BAE0910(sensorIds.get(0), this));
        } else {
            logger.info("BAE0911 sensors are currently not supported");
        }

        scheduler.execute(() -> {
            configureThingChannels();
        });
    }

    private void configureThingChannels() {
        BAE091xHandlerConfiguration configuration = getConfig().as(BAE091xHandlerConfiguration.class);
        ThingBuilder thingBuilder = editThing();
        boolean isEdited = false;
        boolean hasPWM13 = false;
        boolean hasPWM24 = false;

        ThingUID thingUID = getThing().getUID();
        logger.debug("configuring sensors for {}", thingUID);

        BAE0910 sensor = (BAE0910) sensors.get(0);
        // Pin1:
        switch (configuration.pin1) {
            case CONFIG_BAE_PIN_DISABLED:
                if (thing.getChannel(CHANNEL_COUNTER) != null) {
                    thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_COUNTER));
                    isEdited = true;
                }
                break;
            case CONFIG_BAE_PIN_COUNTER:
                if (thing.getChannel(CHANNEL_COUNTER) == null) {
                    thingBuilder.withChannel(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_COUNTER), "Number")
                            .withLabel("Counter").withType(new ChannelTypeUID(BINDING_ID, "bae-counter")).build());
                    sensor.enableChannel(CHANNEL_COUNTER);
                    isEdited = true;
                }
                break;
            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "unknown configuration option for pin 1");
                return;
        }

        // Pin2:
        switch (configuration.pin2) {
            case CONFIG_BAE_PIN_DISABLED:
                if (thing.getChannel(CHANNEL_DIGITAL2) != null) {
                    thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_DIGITAL2));
                    isEdited = true;
                }
                if (thing.getChannel(CHANNEL_PWM_DUTY3) != null) {
                    thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_PWM_DUTY3));
                    isEdited = true;
                }
                break;
            case CONFIG_BAE_PIN_OUT:
                if (thing.getChannel(CHANNEL_DIGITAL2) == null) {
                    thingBuilder.withChannel(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_DIGITAL2), "Switch")
                            .withLabel("Digital Out Pin 2").withType(new ChannelTypeUID(BINDING_ID, "bae-do")).build());
                    isEdited = true;
                }
                if (thing.getChannel(CHANNEL_PWM_DUTY3) != null) {
                    thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_PWM_DUTY3));
                    isEdited = true;
                }
                sensor.enableChannel(CHANNEL_DIGITAL2);
                break;
            case CONFIG_BAE_PIN_PWM:
                hasPWM13 = true;
                if (thing.getChannel(CHANNEL_PWM_DUTY3) == null) {
                    thingBuilder.withChannel(
                            ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_PWM_DUTY3), "Number:Dimensionless")
                                    .withLabel("Duty Cycle PWM 3").withType(CHANNEL_TYPE_UID_BAE_PWM_DUTY).build());
                    isEdited = true;
                }
                if (thing.getChannel(CHANNEL_DIGITAL2) != null) {
                    thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_DIGITAL2));
                    isEdited = true;
                }
                sensor.enableChannel(CHANNEL_PWM_DUTY3);
                break;
            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "unknown configuration option for pin 2");
                return;
        }

        // Pin6:
        switch (configuration.pin6) {
            case CONFIG_BAE_PIN_DISABLED:
                if (thing.getChannel(CHANNEL_DIGITAL6) != null) {
                    thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_DIGITAL6));
                    isEdited = true;
                }
                if (thing.getChannel(CHANNEL_PWM_DUTY4) != null) {
                    thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_PWM_DUTY4));
                    isEdited = true;
                }
                break;
            case CONFIG_BAE_PIN_PIO:
                if (thing.getChannel(CHANNEL_DIGITAL6) == null) {
                    thingBuilder.withChannel(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_DIGITAL6), "Switch")
                            .withLabel("PIO Pin 6").withType(new ChannelTypeUID(BINDING_ID, "bae-pio")).build());
                    isEdited = true;
                }
                if (thing.getChannel(CHANNEL_PWM_DUTY4) != null) {
                    thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_PWM_DUTY4));
                    isEdited = true;
                }
                sensor.enableChannel(CHANNEL_DIGITAL6);
                break;
            case CONFIG_BAE_PIN_PWM:
                hasPWM24 = true;
                if (thing.getChannel(CHANNEL_PWM_DUTY4) == null) {
                    thingBuilder.withChannel(
                            ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_PWM_DUTY4), "Number:Dimensionless")
                                    .withLabel("Duty Cycle PWM 4").withType(CHANNEL_TYPE_UID_BAE_PWM_DUTY).build());
                    isEdited = true;
                }
                if (thing.getChannel(CHANNEL_DIGITAL6) != null) {
                    thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_DIGITAL6));
                    isEdited = true;
                }
                sensor.enableChannel(CHANNEL_PWM_DUTY4);
                break;
            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "unknown configuration option for pin 6");
                return;
        }

        // Pin7:
        switch (configuration.pin7) {
            case CONFIG_BAE_PIN_DISABLED:
                if (thing.getChannel(CHANNEL_DIGITAL7) != null) {
                    thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_DIGITAL7));
                    isEdited = true;
                }
                if (thing.getChannel(CHANNEL_VOLTAGE) != null) {
                    thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_VOLTAGE));
                    isEdited = true;
                }
                if (thing.getChannel(CHANNEL_PWM_DUTY2) != null) {
                    thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_PWM_DUTY2));
                    isEdited = true;
                }
                break;
            case CONFIG_BAE_PIN_ANALOG:
                if (thing.getChannel(CHANNEL_VOLTAGE) == null) {
                    thingBuilder.withChannel(ChannelBuilder
                            .create(new ChannelUID(thing.getUID(), CHANNEL_VOLTAGE), "Number:Voltage")
                            .withLabel("Analog Input").withType(new ChannelTypeUID(BINDING_ID, "bae-analog")).build());
                    isEdited = true;
                }
                if (thing.getChannel(CHANNEL_DIGITAL7) != null) {
                    thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_DIGITAL7));
                    isEdited = true;
                }
                if (thing.getChannel(CHANNEL_PWM_DUTY2) != null) {
                    thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_PWM_DUTY2));
                    isEdited = true;
                }
                sensor.enableChannel(CHANNEL_VOLTAGE);
                break;
            case CONFIG_BAE_PIN_OUT:
                if (thing.getChannel(CHANNEL_DIGITAL7) == null) {
                    thingBuilder.withChannel(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_DIGITAL7), "Switch")
                            .withLabel("Digital Out Pin 7").withType(new ChannelTypeUID(BINDING_ID, "bae-do")).build());
                    isEdited = true;
                }
                if (thing.getChannel(CHANNEL_VOLTAGE) != null) {
                    thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_VOLTAGE));
                    isEdited = true;
                }
                if (thing.getChannel(CHANNEL_PWM_DUTY2) != null) {
                    thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_PWM_DUTY2));
                    isEdited = true;
                }
                sensor.enableChannel(CHANNEL_DIGITAL7);
                break;
            case CONFIG_BAE_PIN_PWM:
                hasPWM24 = true;
                if (thing.getChannel(CHANNEL_PWM_DUTY2) == null) {
                    thingBuilder.withChannel(
                            ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_PWM_DUTY2), "Number:Dimensionless")
                                    .withLabel("Duty Cycle PWM 2").withType(CHANNEL_TYPE_UID_BAE_PWM_DUTY).build());
                    isEdited = true;
                }
                if (thing.getChannel(CHANNEL_DIGITAL7) != null) {
                    thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_DIGITAL7));
                    isEdited = true;
                }
                if (thing.getChannel(CHANNEL_VOLTAGE) != null) {
                    thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_VOLTAGE));
                    isEdited = true;
                }
                sensor.enableChannel(CHANNEL_PWM_DUTY2);
                break;
            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "unknown configuration option for pin 2");
                return;
        }

        // Pin8:
        Channel channel8 = thing.getChannel(CHANNEL_DIGITAL8);
        switch (configuration.pin8) {
            case CONFIG_BAE_PIN_DISABLED:
                if (thing.getChannel(CHANNEL_DIGITAL8) != null) {
                    thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_DIGITAL8));
                    isEdited = true;
                }
                if (thing.getChannel(CHANNEL_PWM_DUTY1) != null) {
                    thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_PWM_DUTY1));
                    isEdited = true;
                }
                break;
            case CONFIG_BAE_PIN_IN:
                if ((channel8 == null)
                        || !((new ChannelTypeUID(BINDING_ID, "bae-in")).equals(channel8.getChannelTypeUID()))) {
                    if (channel8 != null) {
                        thingBuilder.withoutChannels(channel8);
                    }
                    thingBuilder.withChannel(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_DIGITAL8), "Switch")
                            .withLabel("Digital In Pin 8").withType(new ChannelTypeUID(BINDING_ID, "bae-in")).build());
                    isEdited = true;
                }
                if (thing.getChannel(CHANNEL_PWM_DUTY1) != null) {
                    thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_PWM_DUTY1));
                    isEdited = true;
                }
                sensor.enableChannel(CHANNEL_DIGITAL8);
                break;
            case CONFIG_BAE_PIN_OUT:
                if ((channel8 == null)
                        || !((new ChannelTypeUID(BINDING_ID, "bae-out")).equals(channel8.getChannelTypeUID()))) {
                    if (channel8 != null) {
                        thingBuilder.withoutChannels(channel8);
                    }

                    thingBuilder.withChannel(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_DIGITAL8), "Switch")
                            .withLabel("Digital Out Pin 8").withType(new ChannelTypeUID(BINDING_ID, "bae-out"))
                            .build());
                    isEdited = true;
                }
                if (thing.getChannel(CHANNEL_PWM_DUTY1) != null) {
                    thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_PWM_DUTY1));
                    isEdited = true;
                }
                sensor.enableChannel(CHANNEL_DIGITAL8);
                break;
            case CONFIG_BAE_PIN_PWM:
                hasPWM24 = true;
                if (thing.getChannel(CHANNEL_PWM_DUTY1) == null) {
                    thingBuilder.withChannel(
                            ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_PWM_DUTY1), "Number:Dimensionless")
                                    .withLabel("Duty Cycle PWM 1").withType(CHANNEL_TYPE_UID_BAE_PWM_DUTY).build());
                    isEdited = true;
                }
                if (thing.getChannel(CHANNEL_DIGITAL8) != null) {
                    thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_DIGITAL8));
                    isEdited = true;
                }
                sensor.enableChannel(CHANNEL_PWM_DUTY1);
                break;
            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "unknown configuration option for pin 8");
                return;
        }

        if (hasPWM13) {
            if (thing.getChannel(CHANNEL_PWM_FREQ1) == null) {
                thingBuilder.withChannel(ChannelBuilder
                        .create(new ChannelUID(thingUID, CHANNEL_PWM_FREQ1), "Number:Frequency").withLabel("Frequency")
                        .withType(new ChannelTypeUID(BINDING_ID, "bae-pwm-frequency")).build());
                isEdited = true;
            }
            sensor.enableChannel(CHANNEL_PWM_FREQ1);
        } else {
            if (thing.getChannel(CHANNEL_PWM_FREQ1) != null) {
                thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_PWM_FREQ1));
                isEdited = true;

            }
        }

        if (hasPWM24) {
            if (thing.getChannel(CHANNEL_PWM_FREQ2) == null) {

                thingBuilder.withChannel(ChannelBuilder
                        .create(new ChannelUID(thingUID, CHANNEL_PWM_FREQ2), "Number:Frequency").withLabel("Frequency")
                        .withType(new ChannelTypeUID(BINDING_ID, "bae-pwm-frequency")).build());
                isEdited = true;
            }
            sensor.enableChannel(CHANNEL_PWM_FREQ2);
        } else {
            if (thing.getChannel(CHANNEL_PWM_FREQ2) != null) {
                thingBuilder.withoutChannel(new ChannelUID(thingUID, CHANNEL_PWM_FREQ2));
                isEdited = true;
            }
        }

        if (isEdited) {
            updateThing(thingBuilder.build());
        }

        validConfig = true;

        updatePresenceStatus(UnDefType.UNDEF);
    }

    @Override
    public Map<String, String> updateSensorProperties(OwBaseBridgeHandler bridgeHandler) {
        Map<String, String> properties = new HashMap<>();

        // TODO: re-enable
        // sensorType = BAE0910.getDeviceSubType(bridgeHandler, sensorIds.get(0));
        sensorType = OwSensorType.BAE0910;

        properties.put(PROPERTY_MODELID, sensorType.toString());
        properties.put(PROPERTY_VENDOR, "Brain4home");

        return properties;
    }
}
