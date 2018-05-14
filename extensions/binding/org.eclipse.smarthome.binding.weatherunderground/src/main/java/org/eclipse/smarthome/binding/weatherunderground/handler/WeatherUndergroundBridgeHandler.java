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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.weatherunderground.WeatherUndergroundBindingConstants;
import org.eclipse.smarthome.binding.weatherunderground.internal.json.WeatherUndergroundJsonData;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link WeatherUndergroundBridgeHandler} is responsible for handling the
 * bridge things created to use the Weather Underground Service. This way, the
 * API key may be entered only once.
 *
 * @author Theo Giovanna - Initial Contribution
 */
@NonNullByDefault
public class WeatherUndergroundBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(WeatherUndergroundBridgeHandler.class);
    private final Gson gson;
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);
    private static final String URL = "http://api.wunderground.com/api/%APIKEY%/";
    private static final int FETCH_TIMEOUT_MS = 30000;

    private Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    String error = "";
    @Nullable
    String statusDescr = null;
    boolean validConfig = false;

    @Nullable
    private String apikey;

    public WeatherUndergroundBridgeHandler(Bridge bridge) {
        super(bridge);
        gson = new Gson();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing weatherunderground bridge handler.");
        Configuration config = getThing().getConfiguration();
        WeatherUndergroundJsonData result = null;
        String errorDetail = null;

        // Check if an api key has been provided during the bridge creation
        if (StringUtils.trimToNull((String) config.get(WeatherUndergroundBindingConstants.APIKEY)) == null) {
            error += " Parameter 'apikey' must be configured.";
            statusDescr = "@text/offline.conf-error-missing-apikey";
        } else {
            setApikey((String) config.get(WeatherUndergroundBindingConstants.APIKEY));
            // Check if the provided api key is valid for use with the weatherunderground service
            try {
                String urlStr = URL.replace("%APIKEY%", StringUtils.trimToEmpty(this.getApikey()));
                // Run the HTTP request and get the JSON response from Weather Underground
                String response = null;
                try {
                    response = HttpUtil.executeUrl("GET", urlStr, FETCH_TIMEOUT_MS);
                    logger.debug("apiResponse = {}", response);
                } catch (IllegalArgumentException e) {
                    // Catch Illegal character in path at index XX: http://api.wunderground.com/...
                    errorDetail = e.getMessage();
                    statusDescr = "@text/offline.uri-error";
                }
                // Map the JSON response to an object
                result = gson.fromJson(response, WeatherUndergroundJsonData.class);
                if (result.getResponse() == null) {
                    error = "missing response sub-object";
                } else if (result.getResponse().getErrorDescription() != null) {
                    if ("keynotfound".equals(result.getResponse().getErrorType())) {
                        error = "API key has to be fixed";
                        errorDetail = result.getResponse().getErrorDescription();
                        statusDescr = "@text/offline.comm-error-invalid-api-key";
                    } else if ("invalidquery".equals(result.getResponse().getErrorType())) {
                        // The API key provided is valid
                        validConfig = true;
                    } else {
                        errorDetail = result.getResponse().getErrorDescription();
                    }
                } else {
                    validConfig = true;
                }

            } catch (IOException e) {
                error = "Error running Weather Underground request";
                errorDetail = e.getMessage();
                statusDescr = "@text/offline.comm-error-running-request";
            } catch (JsonSyntaxException e) {
                error = "Error parsing Weather Underground response";
                errorDetail = e.getMessage();
                statusDescr = "@text/offline.comm-error-parsing-response";
            }
        }

        error = error.trim();

        // Updates the thing status accordingly
        if (validConfig) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            logger.debug("Disabling thing '{}': Error '{}': {}", getThing().getUID(), error, errorDetail);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, statusDescr);
        }

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // not needed
    }

    public @Nullable String getApikey() {
        return apikey;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    public Map<ThingUID, @Nullable ServiceRegistration<?>> getDiscoveryServiceRegs() {
        return discoveryServiceRegs;
    }

    public void setDiscoveryServiceRegs(Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegs) {
        this.discoveryServiceRegs = discoveryServiceRegs;
    }

    @Override
    public void handleRemoval() {
        // removes the old registration service associated to the bridge, if existing
        ServiceRegistration<?> dis = this.getDiscoveryServiceRegs().get(this.getThing().getUID());
        if (null != dis) {
            dis.unregister();
        }
        super.handleRemoval();
    }
}
