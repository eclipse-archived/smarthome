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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.onewire.internal.OwDynamicStateDescriptionProvider;
import org.eclipse.smarthome.binding.onewire.internal.OwException;
import org.eclipse.smarthome.binding.onewire.internal.device.AbstractDigitalOwDevice;
import org.eclipse.smarthome.binding.onewire.internal.device.DS2405;
import org.eclipse.smarthome.binding.onewire.internal.device.DS2406_DS2413;
import org.eclipse.smarthome.binding.onewire.internal.device.DS2408;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DigitalIOThingHandler} is responsible for handling the Digital I/O devices
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DigitalIOThingHandler extends OwBaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(
            Arrays.asList(THING_TYPE_DIGITALIO, THING_TYPE_DIGITALIO2, THING_TYPE_DIGITALIO8));

    private final Logger logger = LoggerFactory.getLogger(DigitalIOThingHandler.class);

    public DigitalIOThingHandler(Thing thing, OwDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        super(thing, dynamicStateDescriptionProvider);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType) {
            Integer ioChannel = Integer.valueOf(channelUID.getId().substring(channelUID.getId().length() - 1));
            if (ioChannel != null && ioChannel < ((AbstractDigitalOwDevice) sensors.get(0)).getChannelCount()) {
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

        if (this.thing.getThingTypeUID().equals(THING_TYPE_DIGITALIO)) {
            sensors.add(new DS2405(sensorIds.get(0), this));
        } else if (this.thing.getThingTypeUID().equals(THING_TYPE_DIGITALIO2)) {
            sensors.add(new DS2406_DS2413(sensorIds.get(0), this));
        } else if (this.thing.getThingTypeUID().equals(THING_TYPE_DIGITALIO8)) {
            sensors.add(new DS2408(sensorIds.get(0), this));
        }

        // sensor configuration
        try {
            sensors.get(0).configureChannels();
        } catch (OwException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        }

        validConfig = true;

        if (configuration.get(CONFIG_REFRESH) == null) {
            // override default of 300s from base thing handler if no user-defined value is present
            refreshInterval = 10 * 1000;
        }

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);
    }

    @Override
    public void refresh(OwBaseBridgeHandler bridgeHandler, long now) {
        try {
            Boolean forcedRefresh = lastRefresh == 0;

            if (now >= (lastRefresh + refreshInterval)) {
                logger.trace("refreshing {}", this.thing.getUID());
                lastRefresh = now;

                if (!sensors.get(0).checkPresence(bridgeHandler)) {
                    return;
                }

                sensors.get(0).refresh(bridgeHandler, forcedRefresh);
            }
        } catch (OwException e) {
            logger.debug("{}: refresh exception {}", this.thing.getUID(), e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "refresh exception");
            return;
        }
    }
}
