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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.onewire.internal.OwDynamicStateDescriptionProvider;
import org.eclipse.smarthome.binding.onewire.internal.device.AbstractDigitalOwDevice;
import org.eclipse.smarthome.binding.onewire.internal.device.DS18x20;
import org.eclipse.smarthome.binding.onewire.internal.device.DS2401;
import org.eclipse.smarthome.binding.onewire.internal.device.DS2405;
import org.eclipse.smarthome.binding.onewire.internal.device.DS2406_DS2413;
import org.eclipse.smarthome.binding.onewire.internal.device.DS2408;
import org.eclipse.smarthome.binding.onewire.internal.device.DS2423;
import org.eclipse.smarthome.binding.onewire.internal.device.OwSensorType;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GenericThingHandler} is responsible for handling counter sensors
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class GenericThingHandler extends OwBaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.unmodifiableSet(Stream
            .of(THING_TYPE_DIGITALIO, THING_TYPE_DIGITALIO2, THING_TYPE_DIGITALIO8, THING_TYPE_GENERIC,
                    THING_TYPE_TEMPERATURE, THING_TYPE_IBUTTON, THING_TYPE_COUNTER, THING_TYPE_COUNTER2)
            .collect(Collectors.toSet()));
    public static final Set<OwSensorType> SUPPORTED_SENSOR_TYPES = Collections
            .unmodifiableSet(Stream.of(OwSensorType.DS1420, OwSensorType.DS18B20, OwSensorType.DS18S20,
                    OwSensorType.DS1822, OwSensorType.DS2401, OwSensorType.DS2405, OwSensorType.DS2406,
                    OwSensorType.DS2408, OwSensorType.DS2413, OwSensorType.DS2423).collect(Collectors.toSet()));

    private final Logger logger = LoggerFactory.getLogger(GenericThingHandler.class);

    public GenericThingHandler(Thing thing, OwDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        super(thing, dynamicStateDescriptionProvider, SUPPORTED_SENSOR_TYPES);
    }

    @Override
    public void initialize() {
        // TODO: remove after 0.11.0 release
        if (!thing.getThingTypeUID().equals(THING_TYPE_GENERIC)) {
            changeThingType(THING_TYPE_GENERIC, getConfig());
        }
        Configuration configuration = getConfig();

        if (!super.configureThingHandler()) {
            return;
        }

        // add sensor
        switch (sensorType) {
            case DS18B20:
            case DS18S20:
            case DS1822:
                sensors.add(new DS18x20(sensorId, this));
                break;
            case DS1420:
            case DS2401:
                sensors.add(new DS2401(sensorId, this));
                refreshInterval = 10 * 1000;
                break;
            case DS2405:
                sensors.add(new DS2405(sensorId, this));
                refreshInterval = 10 * 1000;
                break;
            case DS2406:
            case DS2413:
                sensors.add(new DS2406_DS2413(sensorId, this));
                refreshInterval = 10 * 1000;
                break;
            case DS2408:
                sensors.add(new DS2408(sensorId, this));
                refreshInterval = 10 * 1000;
                break;
            case DS2423:
                sensors.add(new DS2423(sensorId, this));
                refreshInterval = 10 * 1000;
                break;
            default:
                throw new IllegalArgumentException(
                        "unsupported sensorType " + sensorType.name() + ", this should have been checked before!");
        }

        if (configuration.containsKey(CONFIG_REFRESH)) {
            // override default with configured value
            // TODO: remove default from OwBaseThingHandler
            refreshInterval = ((BigDecimal) configuration.get(CONFIG_REFRESH)).intValue() * 1000;
        }

        scheduler.execute(() -> {
            configureThingChannels();
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType) {
            if (channelUID.getId().startsWith(CHANNEL_DIGITAL) && thing.getChannel(channelUID.getId()) != null) {
                Integer ioChannel = Integer.valueOf(channelUID.getId().substring(channelUID.getId().length() - 1));
                Bridge bridge = getBridge();
                if (bridge != null) {
                    OwBaseBridgeHandler bridgeHandler = (OwBaseBridgeHandler) bridge.getHandler();
                    if (bridgeHandler != null) {
                        if (!((AbstractDigitalOwDevice) sensors.get(0)).writeChannel(bridgeHandler, ioChannel,
                                command)) {
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
        super.handleCommand(channelUID, command);
    }
}
