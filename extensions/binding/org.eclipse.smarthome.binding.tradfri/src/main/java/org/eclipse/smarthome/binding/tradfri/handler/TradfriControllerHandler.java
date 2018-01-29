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
package org.eclipse.smarthome.binding.tradfri.handler;

import static org.eclipse.smarthome.binding.tradfri.TradfriBindingConstants.*;

import org.eclipse.smarthome.binding.tradfri.internal.model.TradfriControllerData;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

/**
 * The {@link TradfriControllerHandler} is responsible for handling commands for individual controllers.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class TradfriControllerHandler extends TradfriThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TradfriControllerHandler.class);

    // keeps track of the current state for handling of increase/decrease
    private TradfriControllerData state;

    public TradfriControllerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void onUpdate(JsonElement data) {
        if (active && !(data.isJsonNull())) {
            state = new TradfriControllerData(data);
            updateStatus(state.getReachabilityStatus() ? ThingStatus.ONLINE : ThingStatus.OFFLINE);

            DecimalType batteryLevel = state.getBatteryLevel();
            if (batteryLevel != null) {
                updateState(CHANNEL_BATTERY_LEVEL, batteryLevel);
            }

            OnOffType batteryLow = state.getBatteryLow();
            if (batteryLow != null) {
                updateState(CHANNEL_BATTERY_LOW, batteryLow);
            }

            updateDeviceProperties(state);

            logger.debug(
                    "Updating thing for controllerId {} to state {batteryLevel: {}, batteryLow: {}, firmwareVersion: {}, modelId: {}, vendor: {}}",
                    state.getDeviceId(), batteryLevel, batteryLow, state.getFirmwareVersion(), state.getModelId(),
                    state.getVendor());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Refreshing channel {}", channelUID);
            coapClient.asyncGet(this);
            return;
        }

        logger.debug("The controller is a read-only device and cannot handle commands.");
    }
}
