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

package org.eclipse.smarthome.binding.weatherunderground.handler;

import static org.eclipse.smarthome.binding.weatherunderground.WeatherUndergroundBindingConstants.THING_TYPE_BRIDGE;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.binding.weatherunderground.WeatherUndergroundBindingConstants;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.LoggerFactory;

/**
 * The {@link WeatherUndergroundBridgeHandler} is responsible for handling the
 * bridge things created to use the Weather Underground Service. This way, the
 * API key may be entered only once.
 *
 * @author Theo Giovanna - Initial Contribution
 */

public class WeatherUndergroundBridgeHandler extends BaseBridgeHandler {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(WeatherUndergroundBridgeHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);

    private String apikey;

    public WeatherUndergroundBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing weatherunderground bridge handler.");
        Configuration config = getThing().getConfiguration();
        setApikey((String) config.get(WeatherUndergroundBindingConstants.APIKEY));
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        // not needed
    }

    public String getApikey() {
        return apikey;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }
}