/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.binding.onewire4j.handler;

import static org.eclipse.smarthome.binding.onewire4j.OneWire4JBindingConstants.*;

import java.io.IOException;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ibapl.onewire4j.OneWireAdapter;
import de.ibapl.onewire4j.container.ENotProperlyConvertedException;
import de.ibapl.onewire4j.container.OneWireDevice;
import de.ibapl.onewire4j.container.ReadScratchpadRequest;
import de.ibapl.onewire4j.container.TemperatureContainer;

/**
 * The {@link TemperatureHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author aploese@gmx.de - Initial contribution
 */
public class TemperatureHandler extends BaseThingHandler {
    protected ThingStatusDetail owHandlerStatus = ThingStatusDetail.HANDLER_CONFIGURATION_PENDING;

    public TemperatureContainer temperatureContainer;

    private final Logger logger = LoggerFactory.getLogger(TemperatureHandler.class);

    public TemperatureHandler(Thing thing) {
        super(thing);
    }

    public void updateTemperature(double temperature) {
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE), new DecimalType(temperature));
    }

    public void updateMinTemperature(double temperature) {
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_MIN_TEMPERATURE), new DecimalType(temperature));
    }

    public void updateMaxTemperature(double temperature) {
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_MAX_TEMPERATURE), new DecimalType(temperature));
    }

    public void tempReadFailed() {
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_TEMPERATURE:
                if (command instanceof RefreshType) {
                    // updateTemperature(++lastValue);
                }
                break;
            case CHANNEL_MIN_TEMPERATURE:
                if (command instanceof RefreshType) {
                    // updateMinTemperature(0);
                }
                break;
            case CHANNEL_MAX_TEMPERATURE:
                if (command instanceof RefreshType) {
                    // updateMaxTemperature(100);
                }
                break;
            default:

        }
    }

    @Override
    public void initialize() {
        logger.debug("thing {} is initializing", this.thing.getUID());
        Configuration configuration = getConfig();
        long deviceId;
        try {
            deviceId = Long.parseUnsignedLong((String) configuration.get("deviceId"), 16);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Can't parse DeviceId");
            return;
        }
        temperatureContainer = (TemperatureContainer) OneWireDevice.fromAdress(deviceId, true);
        if (temperatureContainer == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "No container Found");
            return;
        }
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "no bridge assigned");
            owHandlerStatus = ThingStatusDetail.CONFIGURATION_ERROR;
            return;
        } else {
            if (bridge.getStatus().equals(ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.ONLINE);
                owHandlerStatus = ThingStatusDetail.NONE;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        }
    }

    @Override
    public void dispose() {
    }

    public void readDevice(OneWireAdapter oneWireAdapter) throws IOException {
        try {
            ReadScratchpadRequest request = new ReadScratchpadRequest();
            temperatureContainer.readScratchpad(oneWireAdapter, request);
            final double temp = temperatureContainer.getTemperature(request);
            updateTemperature(temp);
        } catch (ENotProperlyConvertedException e) {
            try {
                final double temp = temperatureContainer.convertAndReadTemperature(oneWireAdapter);
                updateTemperature(temp);
            } catch (ENotProperlyConvertedException e1) {
                tempReadFailed();
            }
        }
    }

}
