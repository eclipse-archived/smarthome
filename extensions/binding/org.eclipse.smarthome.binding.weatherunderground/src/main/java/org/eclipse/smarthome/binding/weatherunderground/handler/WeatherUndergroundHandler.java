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

import static org.eclipse.smarthome.core.library.unit.MetricPrefix.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.binding.weatherunderground.internal.config.WeatherUndergroundConfiguration;
import org.eclipse.smarthome.binding.weatherunderground.internal.json.WeatherUndergroundJsonCurrent;
import org.eclipse.smarthome.binding.weatherunderground.internal.json.WeatherUndergroundJsonData;
import org.eclipse.smarthome.binding.weatherunderground.internal.json.WeatherUndergroundJsonForecast;
import org.eclipse.smarthome.binding.weatherunderground.internal.json.WeatherUndergroundJsonForecastDay;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.UnitProvider;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Bridge;
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

/**
 * The {@link WeatherUndergroundHandler} is responsible for handling the
 * weather things created to use the Weather Underground Service.
 *
 * @author Laurent Garnier - Initial contribution
 * @author Theo Giovanna - Added a bridge for the API key
 */
public class WeatherUndergroundHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(WeatherUndergroundHandler.class);

    private static final int DEFAULT_REFRESH_PERIOD = 30;
    private static final String FEATURE_CONDITIONS = "conditions";
    private static final String FEATURE_FORECAST10DAY = "forecast10day";
    private static final Set<String> USUAL_FEATURES = Stream.of(FEATURE_CONDITIONS, FEATURE_FORECAST10DAY)
            .collect(Collectors.toSet());

    private final LocaleProvider localeProvider;
    private final UnitProvider unitProvider;
    private WeatherUndergroundJsonData weatherData;

    private ScheduledFuture<?> refreshJob;
    private final Gson gson;
    private final Map<String, Integer> forecastMap;

    public WeatherUndergroundHandler(@NonNull Thing thing, LocaleProvider localeProvider, UnitProvider unitProvider) {
        super(thing);
        this.localeProvider = localeProvider;
        this.unitProvider = unitProvider;
        gson = new Gson();
        forecastMap = initForecastDayMap();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing WeatherUnderground handler.");

        WeatherUndergroundConfiguration config = getConfigAs(WeatherUndergroundConfiguration.class);

        logger.debug("config location = {}", config.location);
        logger.debug("config language = {}", config.language);
        logger.debug("config refresh = {}", config.refresh);

        boolean validConfig = true;
        String errors = "";
        String statusDescr = null;

        if (this.getBridge() == null) {
            logger.error("Error: you must configure a bridge");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge uninitialized");
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
            if (weatherData != null) {
                if (channelId.startsWith("current")) {
                    state = updateCurrentObservationChannel(channelId, weatherData.getCurrent());
                } else if (channelId.startsWith("forecast")) {
                    state = updateForecastChannel(channelId, weatherData.getForecast());
                }
            }

            logger.debug("Update channel {} with state {}", channelId, (state == null) ? "null" : state.toString());

            // Update the channel
            if (state != null) {
                updateState(channelId, state);
            } else {
                updateState(channelId, UnDefType.NULL);
            }
        }
    }

    private State updateCurrentObservationChannel(String channelId, WeatherUndergroundJsonCurrent current) {
        WUQuantity quantity;
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
                quantity = getTemperature(current.getTemperatureC(), current.getTemperatureF());
                return undefOrQuantity(quantity);
            case "relativeHumidity":
                return undefOrState(current.getRelativeHumidity(),
                        new QuantityType<>(current.getRelativeHumidity(), SmartHomeUnits.PERCENT));
            case "windDirection":
                return undefOrState(current.getWindDirection(), new StringType(current.getWindDirection()));
            case "windDirectionDegrees":
                return undefOrState(current.getWindDirectionDegrees(),
                        new QuantityType<>(current.getWindDirectionDegrees(), SmartHomeUnits.DEGREE_ANGLE));
            case "windSpeed":
                quantity = getSpeed(current.getWindSpeedKmh(), current.getWindSpeedMph());
                return undefOrQuantity(quantity);
            case "windGust":
                quantity = getSpeed(current.getWindGustKmh(), current.getWindGustMph());
                return undefOrQuantity(quantity);
            case "pressure":
                quantity = getPressure(current.getPressureHPa(), current.getPressureInHg());
                return undefOrQuantity(quantity);
            case "pressureTrend":
                return undefOrState(current.getPressureTrend(), new StringType(current.getPressureTrend()));
            case "dewPoint":
                quantity = getTemperature(current.getDewPointC(), current.getDewPointF());
                return undefOrQuantity(quantity);
            case "heatIndex":
                quantity = getTemperature(current.getHeatIndexC(), current.getHeatIndexF());
                return undefOrQuantity(quantity);
            case "windChill":
                quantity = getTemperature(current.getWindChillC(), current.getWindChillF());
                return undefOrQuantity(quantity);
            case "feelingTemperature":
                quantity = getTemperature(current.getFeelingTemperatureC(), current.getFeelingTemperatureF());
                return undefOrQuantity(quantity);
            case "visibility":
                quantity = getWUQuantity(KILO(SIUnits.METRE), ImperialUnits.MILE, current.getVisibilityKm(),
                        current.getVisibilityMi());
                return undefOrQuantity(quantity);
            case "solarRadiation":
                return undefOrQuantity(new WUQuantity(current.getSolarRadiation(), SmartHomeUnits.IRRADIANCE));
            case "UVIndex":
                return undefOrDecimal(current.getUVIndex());
            case "precipitationDay":
                quantity = getPrecipitation(current.getPrecipitationDayMm(), current.getPrecipitationDayIn());
                return undefOrQuantity(quantity);
            case "precipitationHour":
                quantity = getPrecipitation(current.getPrecipitationHourMm(), current.getPrecipitationHourIn());
                return undefOrQuantity(quantity);
            case "iconKey":
                return undefOrState(current.getIconKey(), new StringType(current.getIconKey()));
            case "icon":
                State icon = HttpUtil.downloadImage(current.getIcon().toExternalForm());
                if (icon == null) {
                    logger.debug("Failed to download the content of URL {}", current.getIcon().toExternalForm());
                    return null;
                }
                return icon;
            default:
                return null;
        }
    }

    private State updateForecastChannel(String channelId, WeatherUndergroundJsonForecast forecast) {
        WUQuantity quantity;
        int day = getDay(channelId);
        WeatherUndergroundJsonForecastDay dayForecast = forecast.getSimpleForecast(day);

        String channelTypeId = getChannelTypeId(channelId);
        switch (channelTypeId) {
            case "forecastTime":
                return undefOrState(dayForecast.getForecastTime(), new DateTimeType(dayForecast.getForecastTime()));
            case "conditions":
                return undefOrState(dayForecast.getConditions(), new StringType(dayForecast.getConditions()));
            case "minTemperature":
                quantity = getTemperature(dayForecast.getMinTemperatureC(), dayForecast.getMinTemperatureF());
                return undefOrQuantity(quantity);
            case "maxTemperature":
                quantity = getTemperature(dayForecast.getMaxTemperatureC(), dayForecast.getMaxTemperatureF());
                return undefOrQuantity(quantity);
            case "relativeHumidity":
                return undefOrState(dayForecast.getRelativeHumidity(),
                        new QuantityType<>(dayForecast.getRelativeHumidity(), SmartHomeUnits.PERCENT));
            case "probaPrecipitation":
                return undefOrState(dayForecast.getProbaPrecipitation(),
                        new QuantityType<>(dayForecast.getProbaPrecipitation(), SmartHomeUnits.PERCENT));
            case "precipitationDay":
                quantity = getPrecipitation(dayForecast.getPrecipitationDayMm(), dayForecast.getPrecipitationDayIn());
                return undefOrQuantity(quantity);
            case "snow":
                quantity = getWUQuantity(CENTI(SIUnits.METRE), ImperialUnits.INCH, dayForecast.getSnowCm(),
                        dayForecast.getSnowIn());
                return undefOrQuantity(quantity);
            case "maxWindDirection":
                return undefOrState(dayForecast.getMaxWindDirection(),
                        new StringType(dayForecast.getMaxWindDirection()));
            case "maxWindDirectionDegrees":
                return undefOrState(dayForecast.getMaxWindDirectionDegrees(),
                        new QuantityType<>(dayForecast.getMaxWindDirectionDegrees(), SmartHomeUnits.DEGREE_ANGLE));
            case "maxWindSpeed":
                quantity = getSpeed(dayForecast.getMaxWindSpeedKmh(), dayForecast.getMaxWindSpeedMph());
                return undefOrQuantity(quantity);
            case "averageWindDirection":
                return undefOrState(dayForecast.getAverageWindDirection(),
                        new StringType(dayForecast.getAverageWindDirection()));
            case "averageWindDirectionDegrees":
                return undefOrState(dayForecast.getAverageWindDirectionDegrees(),
                        new QuantityType<>(dayForecast.getAverageWindDirectionDegrees(), SmartHomeUnits.DEGREE_ANGLE));
            case "averageWindSpeed":
                quantity = getSpeed(dayForecast.getAverageWindSpeedKmh(), dayForecast.getAverageWindSpeedMph());
                return undefOrQuantity(quantity);
            case "iconKey":
                return undefOrState(dayForecast.getIconKey(), new StringType(dayForecast.getIconKey()));
            case "icon":
                State icon = HttpUtil.downloadImage(dayForecast.getIcon().toExternalForm());
                if (icon == null) {
                    logger.debug("Failed to download the content of URL {}", dayForecast.getIcon().toExternalForm());
                    return null;
                }
                return icon;
            default:
                return null;
        }
    }

    private State undefOrState(Object value, State state) {
        return value == null ? null : state;
    }

    private <T extends Quantity<T>> State undefOrQuantity(WUQuantity quantity) {
        return quantity.value == null ? null : new QuantityType<>(quantity.value, quantity.unit);
    }

    private State undefOrDecimal(Number value) {
        return value == null ? null : new DecimalType(value.doubleValue());
    }

    private int getDay(String channelId) {
        String channel = channelId.split("#")[0];

        return forecastMap.get(channel);
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
        WeatherUndergroundBridgeHandler handler = null;
        Bridge bridge = this.getBridge();
        if (bridge != null) {
            handler = (WeatherUndergroundBridgeHandler) bridge.getHandler();
        }
        if (handler != null) {
            weatherData = handler.getWeatherData(USUAL_FEATURES, StringUtils.trimToEmpty(config.location),
                    this.localeProvider, this.gson);

            if (weatherData != null) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR);
            }
        }
        return weatherData != null;
    }

    private WUQuantity getTemperature(BigDecimal siValue, BigDecimal imperialValue) {
        return getWUQuantity(SIUnits.CELSIUS, ImperialUnits.FAHRENHEIT, siValue, imperialValue);
    }

    private WUQuantity getSpeed(BigDecimal siValue, BigDecimal imperialValue) {
        return getWUQuantity(SIUnits.KILOMETRE_PER_HOUR, ImperialUnits.MILES_PER_HOUR, siValue, imperialValue);
    }

    private WUQuantity getPressure(BigDecimal siValue, BigDecimal imperialValue) {
        return getWUQuantity(HECTO(SIUnits.PASCAL), ImperialUnits.INCH_OF_MERCURY, siValue, imperialValue);
    }

    private WUQuantity getPrecipitation(BigDecimal siValue, BigDecimal imperialValue) {
        return getWUQuantity(MILLI(SIUnits.METRE), ImperialUnits.INCH, siValue, imperialValue);
    }

    private <T extends Quantity<T>> WUQuantity getWUQuantity(Unit<T> siUnit, Unit<T> imperialUnit, BigDecimal siValue,
            BigDecimal imperialValue) {
        boolean isSI = unitProvider.getMeasurementSystem().equals(SIUnits.getInstance());
        return new WUQuantity(isSI ? siValue : imperialValue, isSI ? siUnit : imperialUnit);
    }

    private class WUQuantity {
        private WUQuantity(BigDecimal value, Unit<?> unit) {
            this.value = value;
            this.unit = unit;
        }

        private final Unit<?> unit;
        private final BigDecimal value;
    }
}
