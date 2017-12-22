/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.weatherunderground.handler;

import static tec.uom.se.unit.MetricPrefix.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.Unit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.binding.weatherunderground.internal.config.WeatherUndergroundConfiguration;
import org.eclipse.smarthome.binding.weatherunderground.internal.json.WeatherUndergroundJsonCurrent;
import org.eclipse.smarthome.binding.weatherunderground.internal.json.WeatherUndergroundJsonData;
import org.eclipse.smarthome.binding.weatherunderground.internal.json.WeatherUndergroundJsonForecast;
import org.eclipse.smarthome.binding.weatherunderground.internal.json.WeatherUndergroundJsonForecastDay;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.SmartHomeUnits;
import org.eclipse.smarthome.core.types.MeasurementSystem;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import tec.uom.se.unit.Units;

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

    private final LocaleProvider localeProvider;

    private WeatherUndergroundJsonData weatherData;

    private ScheduledFuture<?> refreshJob;

    private final Gson gson;

    private final Map<String, Integer> forecastMap;

    public WeatherUndergroundHandler(@NonNull Thing thing, LocaleProvider localeProvider) {
        super(thing);
        this.localeProvider = localeProvider;
        gson = new Gson();
        forecastMap = initForecastDayMap();
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
        if (isLinked(channelId)) {
            State state = null;
            if (channelId.startsWith("current")) {
                state = updateCurrentObservationChannel(channelId, weatherData.getCurrent());
            } else if (channelId.startsWith("forecast")) {
                state = updateForecastChannel(channelId, weatherData.getForecast());
            }

            logger.debug("Update channel {} with state {}", channelId, (state == null) ? "null" : state.toString());

            // Update the channel
            if (state != null) {
                updateState(channelId, state);
            }
        }
    }

    private State updateCurrentObservationChannel(String channelId, WeatherUndergroundJsonCurrent current) {
        String channelTypeId = getChannelTypeId(channelId);
        switch (channelTypeId) {
            case "location":
                return undefOrState(current.getLocation(), new StringType(current.getLocation()));
            case "stationId":
                return undefOrState(current.getStationId(), new StringType(current.getStationId()));
            case "observationTime":
                return undefOrState(current.getObservationTime(), new DateTimeType(current.getObservationTime()));
            case "conditions":
                return undefOrState(current.getConditions(), new StringType(current.getConditions()));
            case "temperature":
                return undefOrQuantity(current.getTemperatureC(), Units.CELSIUS, getTemperatureConversionMap());
            case "relativeHumidity":
                return undefOrDecimal(current.getRelativeHumidity());
            case "windDirection":
                return undefOrState(current.getWindDirection(), new StringType(current.getWindDirection()));
            case "windDirectionDegrees":
                return undefOrDecimal(current.getWindDirectionDegrees());
            case "windSpeed":
                return undefOrQuantity(current.getWindSpeedKmh(), Units.KILOMETRE_PER_HOUR, getSpeedConversionMap());
            case "windGust":
                return undefOrQuantity(current.getWindGustKmh(), Units.KILOMETRE_PER_HOUR, getSpeedConversionMap());
            case "pressure":
                return undefOrQuantity(current.getPressureHPa(), SmartHomeUnits.HECTO_PASCAL, getPressureConversionMap());
            case "dewPoint":
                return undefOrQuantity(current.getDewPointC(), Units.CELSIUS, getTemperatureConversionMap());
            case "heatIndex":
                return undefOrQuantity(current.getHeatIndexC(), Units.CELSIUS, getTemperatureConversionMap());
            case "windChill":
                return undefOrQuantity(current.getWindChillC(), Units.CELSIUS, getTemperatureConversionMap());
            case "feelingTemperature":
                return undefOrQuantity(current.getFeelingTemperatureC(), Units.CELSIUS, getTemperatureConversionMap());
            case "visibility":
                return undefOrQuantity(current.getVisibilityKm(), KILO(Units.METRE),
                        getConversionMap(KILO(Units.METRE), SmartHomeUnits.MILE));
            case "solarRadiation":
                return undefOrQuantity(current.getSolarRadiation(), SmartHomeUnits.IRRADIANCE, Collections.emptyMap());
            case "UVIndex":
                return undefOrDecimal(current.getUVIndex());
            case "precipitationDay":
                return undefOrQuantity(current.getPrecipitationDayMm(), MILLI(Units.METRE),
                        getConversionMap(MILLI(Units.METRE), SmartHomeUnits.INCH));
            case "precipitationHour":
                return undefOrQuantity(current.getPrecipitationHourMm(), MILLI(Units.METRE),
                        getConversionMap(MILLI(Units.METRE), SmartHomeUnits.INCH));
            case "icon":
                State icon = HttpUtil.downloadImage(current.getIcon().toExternalForm());
                if (icon == null) {
                    logger.debug("Failed to download the content of URL {}", current.getIcon().toExternalForm());
                    icon = UnDefType.UNDEF;
                }
                return icon;
            default:
                return null;
        }
    }

    private State updateForecastChannel(String channelId, WeatherUndergroundJsonForecast forecast) {
        int day = getDay(channelId);
        WeatherUndergroundJsonForecastDay dayForecast = forecast.getSimpleForecast(day);

        String channelTypeId = getChannelTypeId(channelId);
        switch (channelTypeId) {
            case "forecastTime":
                return undefOrState(dayForecast.getForecastTime(), new DateTimeType(dayForecast.getForecastTime()));
            case "conditions":
                return undefOrState(dayForecast.getConditions(), new StringType(dayForecast.getConditions()));
            case "minTemperature":
                return undefOrQuantity(dayForecast.getMinTemperatureC(), Units.CELSIUS, getTemperatureConversionMap());
            case "maxTemperature":
                return undefOrQuantity(dayForecast.getMaxTemperatureC(), Units.CELSIUS, getTemperatureConversionMap());
            case "relativeHumidity":
                return undefOrDecimal(dayForecast.getRelativeHumidity());
            case "probaPrecipitation":
                return undefOrDecimal(dayForecast.getProbaPrecipitation());
            case "precipitationDay":
                return undefOrQuantity(dayForecast.getPrecipitationDayMm(), MILLI(Units.METRE),
                        getConversionMap(MILLI(Units.METRE), SmartHomeUnits.INCH));
            case "snow":
                return undefOrQuantity(dayForecast.getSnowCm(), CENTI(Units.METRE),
                        getConversionMap(CENTI(Units.METRE), SmartHomeUnits.INCH));
            case "maxWindDirection":
                return undefOrState(dayForecast.getMaxWindDirection(),
                        new StringType(dayForecast.getMaxWindDirection()));
            case "maxWindDirectionDegrees":
                return undefOrDecimal(dayForecast.getMaxWindDirectionDegrees());
            case "maxWindSpeed":
                return undefOrQuantity(dayForecast.getMaxWindSpeedKmh(), Units.KILOMETRE_PER_HOUR,
                        getSpeedConversionMap());
            case "averageWindDirection":
                return undefOrState(dayForecast.getAverageWindDirection(),
                        new StringType(dayForecast.getAverageWindDirection()));
            case "averageWindDirectionDegrees":
                return undefOrDecimal(dayForecast.getAverageWindDirectionDegrees());
            case "averageWindSpeed":
                return undefOrQuantity(dayForecast.getAverageWindSpeedKmh(), Units.KILOMETRE_PER_HOUR,
                        getSpeedConversionMap());
            case "icon":
                State icon = HttpUtil.downloadImage(dayForecast.getIcon().toExternalForm());
                if (icon == null) {
                    logger.debug("Failed to download the content of URL {}", dayForecast.getIcon().toExternalForm());
                    icon = UnDefType.UNDEF;
                }
                return icon;
            default:
                return null;
        }
    }

    private State undefOrState(Object value, State state) {
        return value == null ? UnDefType.UNDEF : state;
    }

    private State undefOrQuantity(BigDecimal value, Unit<?> unit, Map<MeasurementSystem, Unit<?>> conversionMap) {
        return value == null ? UnDefType.UNDEF : new QuantityType(value.doubleValue(), unit, conversionMap);
    }

    private State undefOrDecimal(Number value) {
        return value == null ? UnDefType.UNDEF : new DecimalType(value.doubleValue());
    }

    private int getDay(String channelId) {
        String channel = channelId.split("#")[0];

        return forecastMap.get(channel);
    }

    private Map<MeasurementSystem, Unit<?>> getConversionMap(Unit<?> metricUnit, Unit<?> imperialUnit) {
        Map<MeasurementSystem, Unit<?>> conversionMap = new HashMap<>();
        conversionMap.put(MeasurementSystem.SI, metricUnit);
        conversionMap.put(MeasurementSystem.US, imperialUnit);

        return conversionMap;
    }

    private Map<MeasurementSystem, Unit<?>> getPressureConversionMap() {
        return getConversionMap(SmartHomeUnits.HECTO_PASCAL, SmartHomeUnits.INCH_OF_MERCURY);
    }

    private Map<MeasurementSystem, Unit<?>> getSpeedConversionMap() {
        return getConversionMap(Units.KILOMETRE_PER_HOUR, SmartHomeUnits.MILES_PER_HOUR);
    }

    private Map<MeasurementSystem, Unit<?>> getTemperatureConversionMap() {
        return getConversionMap(Units.CELSIUS, SmartHomeUnits.FAHRENHEIT);
    }

    private String getChannelTypeId(String channelId) {
        return channelId.substring(channelId.indexOf("#") + 1);
    }

    private Map<String, Integer> initForecastDayMap() {
        Map<String, Integer> forecastMap = new HashMap<>();
        forecastMap.put("forecastToday", Integer.valueOf(1));
        forecastMap.put("forecastTomorrow", Integer.valueOf(2));
        forecastMap.put("forecastDay2", Integer.valueOf(3));
        forecastMap.put("forecastDay3", Integer.valueOf(4));
        forecastMap.put("forecastDay4", Integer.valueOf(5));
        forecastMap.put("forecastDay5", Integer.valueOf(6));
        forecastMap.put("forecastDay6", Integer.valueOf(7));
        forecastMap.put("forecastDay7", Integer.valueOf(8));
        forecastMap.put("forecastDay8", Integer.valueOf(9));
        forecastMap.put("forecastDay9", Integer.valueOf(10));
        return forecastMap;
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
            result = gson.fromJson(response, WeatherUndergroundJsonData.class);
            if (result.getResponse() == null) {
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
