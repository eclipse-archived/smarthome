/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.weatherunderground.handler;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Calendar;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.weatherunderground.WeatherUndergroundBindingConstants;
import org.eclipse.smarthome.binding.weatherunderground.internal.config.WeatherUndergroundConfiguration;
import org.eclipse.smarthome.binding.weatherunderground.internal.json.WeatherUndergroundJsonData;
import org.eclipse.smarthome.binding.weatherunderground.internal.json.WeatherUndergroundJsonUtils;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link WeatherUndergroundHandler} is responsible for handling the
 * weather things created to use the Weather Underground Service.
 *
 * @author Laurent Garnier - Initial contribution
 */
public class WeatherUndergroundHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(WeatherUndergroundHandler.class);

    private static final String URL = "http://api.wunderground.com/api/%APIKEY%/%FEATURES%/%SETTINGS%/q/%QUERY%.json";
    private static final String FEATURE_CONDITIONS = "conditions";
    private static final String FEATURE_FORECAST10DAY = "forecast10day";
    private static final String FEATURE_GEOLOOKUP = "geolookup";
    private static final Set<String> USUAL_FEATURES = Stream.of(FEATURE_CONDITIONS, FEATURE_FORECAST10DAY)
            .collect(Collectors.toSet());

    private static final int DEFAULT_REFRESH_PERIOD = 30;

    private LocaleProvider localeProvider;

    private WeatherUndergroundJsonData weatherData;

    private ScheduledFuture<?> refreshJob;

    private Gson gson;

    public WeatherUndergroundHandler(Thing thing, LocaleProvider localeProvider) {
        super(thing);
        this.localeProvider = localeProvider;
        gson = new Gson();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing WeatherUnderground handler.");

        WeatherUndergroundConfiguration config = getConfigAs(WeatherUndergroundConfiguration.class);
        logger.debug("config apikey = {}", config.apikey);
        logger.debug("config location = {}", config.location);
        logger.debug("config language = {}", config.language);
        logger.debug("config refresh = {}", config.refresh);

        boolean validConfig = true;
        String errors = "";
        String statusDescr = null;

        if (StringUtils.trimToNull(config.apikey) == null) {
            errors += " Parameter 'apikey' must be configured.";
            statusDescr = "@text/offline.conf-error-missing-apikey";
            validConfig = false;
        }
        if (StringUtils.trimToNull(config.location) == null) {
            errors += " Parameter 'location' must be configured.";
            statusDescr = "@text/offline.conf-error-missing-location";
            validConfig = false;
        }
        if (config.language != null) {
            String lang = StringUtils.trimToEmpty(config.language);
            if (lang.length() != 2) {
                errors += " Parameter 'language' must be 2 letters.";
                statusDescr = "@text/offline.conf-error-syntax-language";
                validConfig = false;
            }
        }
        if (config.refresh != null && config.refresh < 5) {
            errors += " Parameter 'refresh' must be at least 5 minutes.";
            statusDescr = "@text/offline.conf-error-min-refresh";
            validConfig = false;
        }
        errors = errors.trim();

        if (validConfig) {
            startAutomaticRefresh();
        } else {
            logger.debug("Disabling thing '{}': {}", getThing().getUID(), errors);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, statusDescr);
        }
    }

    /**
     * Start the job refreshing the weather data
     */
    private void startAutomaticRefresh() {
        if (refreshJob == null || refreshJob.isCancelled()) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        // Request new weather data to the Weather Underground service
                        updateWeatherData();

                        // Update all channels from the updated weather data
                        for (Channel channel : getThing().getChannels()) {
                            updateChannel(channel.getUID().getId());
                        }
                    } catch (Exception e) {
                        logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                    }
                }
            };

            WeatherUndergroundConfiguration config = getConfigAs(WeatherUndergroundConfiguration.class);
            int period = (config.refresh != null) ? config.refresh.intValue() : DEFAULT_REFRESH_PERIOD;
            refreshJob = scheduler.scheduleWithFixedDelay(runnable, 0, period, TimeUnit.MINUTES);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing WeatherUnderground handler.");

        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannel(channelUID.getId());
        } else {
            logger.debug("The Weather Underground binding is a read-only binding and cannot handle command {}",
                    command);
        }
    }

    /**
     * Update the channel from the last Weather Underground data retrieved
     *
     * @param channelId the id identifying the channel to be updated
     */
    private void updateChannel(String channelId) {
        Channel channel = getThing().getChannel(channelId);
        if (channel != null && isLinked(channelId)) {
            // Get the source unit property from the channel when defined
            String sourceUnit = (String) channel.getConfiguration()
                    .get(WeatherUndergroundBindingConstants.PROPERTY_SOURCE_UNIT);
            if (sourceUnit == null) {
                sourceUnit = "";
            } else if (sourceUnit.length() > 0) {
                sourceUnit = sourceUnit.substring(0, 1).toUpperCase() + sourceUnit.substring(1);
            }

            // Get the value corresponding to the channel
            // from the last weather data retrieved
            Object value;
            try {
                value = WeatherUndergroundJsonUtils.getValue(channelId + sourceUnit, weatherData);
            } catch (Exception e) {
                logger.debug("Update channel {}: Can't get value: {}", channelId, e.getMessage());
                return;
            }

            // Build a State from this value
            State state = null;
            if (value == null) {
                state = UnDefType.UNDEF;
            } else if (value instanceof Calendar) {
                state = new DateTimeType((Calendar) value);
            } else if (value instanceof BigDecimal) {
                state = new DecimalType((BigDecimal) value);
            } else if (value instanceof Integer) {
                state = new DecimalType(BigDecimal.valueOf(((Integer) value).longValue()));
            } else if (value instanceof String) {
                state = new StringType(value.toString());
            } else if (value instanceof URL) {
                state = HttpUtil.downloadImage(((URL) value).toExternalForm());
                if (state == null) {
                    logger.debug("Failed to download the content of URL {}", ((URL) value).toExternalForm());
                    state = UnDefType.UNDEF;
                }
            } else {
                logger.debug("Update channel {}: Unsupported value type {}", channelId,
                        value.getClass().getSimpleName());
            }
            logger.debug("Update channel {} with state {} ({})", channelId, (state == null) ? "null" : state.toString(),
                    (value == null) ? "null" : value.getClass().getSimpleName());

            // Update the channel
            if (state != null) {
                updateState(channelId, state);
            }
        }
    }

    /**
     * Request new current conditions and forecast 10 days to the Weather Underground service
     * and store the data in weatherData
     *
     * @return true if success or false in case of error
     */
    private boolean updateWeatherData() {
        // Request new weather data to the Weather Underground service
        WeatherUndergroundConfiguration config = getConfigAs(WeatherUndergroundConfiguration.class);
        weatherData = getWeatherData(USUAL_FEATURES, StringUtils.trimToEmpty(config.location));
        return weatherData != null;
    }

    /**
     * Request a geolookup feature to the Weather Underground service
     * for a location given as parameter
     *
     * @param location the location to provide to the Weather Underground service
     *
     * @return true if success or false in case of error
     */
    private boolean checkWeatherLocation(String location) {
        WeatherUndergroundJsonData data = getWeatherData(Collections.singleton(FEATURE_GEOLOOKUP), location);
        return data != null;
    }

    /**
     * Request new weather data to the Weather Underground service
     *
     * @param features the list of features to be requested
     *
     * @return the weather data object mapping the JSON response or null in case of error
     */
    private WeatherUndergroundJsonData getWeatherData(Set<String> features, String location) {
        WeatherUndergroundJsonData result = null;
        boolean resultOk = false;
        String error = null;
        String errorDetail = null;
        String statusDescr = null;

        try {

            // Build a valid URL for the Weather Underground service using
            // the requested features and the thing configuration settings
            WeatherUndergroundConfiguration config = getConfigAs(WeatherUndergroundConfiguration.class);

            String urlStr = URL.replace("%APIKEY%", StringUtils.trimToEmpty(config.apikey));

            urlStr = urlStr.replace("%FEATURES%", String.join("/", features));

            String lang = StringUtils.trimToEmpty(config.language);
            if (lang.isEmpty()) {
                // If language is not set in the configuration, you try using the system language
                lang = localeProvider.getLocale().getLanguage();
                logger.debug("Use language from system locale: {}", lang);
            }
            if (lang.isEmpty()) {
                urlStr = urlStr.replace("%SETTINGS%", "");
            } else {
                urlStr = urlStr.replace("%SETTINGS%", "lang:" + lang.toUpperCase());
            }

            urlStr = urlStr.replace("%QUERY%", location);
            logger.debug("URL = {}", urlStr);

            // Run the HTTP request and get the JSON response from Weather Underground
            String response = null;
            try {
                response = HttpUtil.executeUrl("GET", urlStr, 30 * 1000);
                logger.debug("weatherData = {}", response);
            } catch (IllegalArgumentException e) {
                // catch Illegal character in path at index XX: http://api.wunderground.com/...
                error = "Error creating URI with location parameter: '" + location + "'";
                errorDetail = e.getMessage();
                statusDescr = "@text/offline.uri-error";
            }

            // Map the JSON response to an object
            if (response != null) {
                result = gson.fromJson(response, WeatherUndergroundJsonData.class);
                if (result == null) {
                    errorDetail = "no data returned";
                } else if (result.getResponse() == null) {
                    errorDetail = "missing response sub-object";
                } else if (result.getResponse().getErrorDescription() != null) {
                    if ("keynotfound".equals(result.getResponse().getErrorType())) {
                        error = "API key has to be fixed";
                        statusDescr = "@text/offline.comm-error-invalid-api-key";
                    }
                    errorDetail = result.getResponse().getErrorDescription();
                } else {
                    resultOk = true;
                    for (String feature : features) {
                        if (feature.equals(FEATURE_CONDITIONS) && result.getCurrent() == null) {
                            resultOk = false;
                            errorDetail = "missing current_observation sub-object";
                        } else if (feature.equals(FEATURE_FORECAST10DAY) && result.getForecast() == null) {
                            resultOk = false;
                            errorDetail = "missing forecast sub-object";
                        } else if (feature.equals(FEATURE_GEOLOOKUP) && result.getLocation() == null) {
                            resultOk = false;
                            errorDetail = "missing location sub-object";
                        }
                    }
                }
            }
            if (!resultOk && error == null) {
                error = "Error in Weather Underground response";
                statusDescr = "@text/offline.comm-error-response";
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

        // Update the thing status
        if (resultOk) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            logger.debug("{}: {}", error, errorDetail);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, statusDescr);
        }

        return resultOk ? result : null;
    }

}
