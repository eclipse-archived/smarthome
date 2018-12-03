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
package org.eclipse.smarthome.binding.openweathermap.internal.handler;

import static org.eclipse.smarthome.binding.openweathermap.internal.OpenWeatherMapBindingConstants.*;
import static org.eclipse.smarthome.core.library.unit.MetricPrefix.*;
import static org.eclipse.smarthome.core.library.unit.SIUnits.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.smarthome.binding.openweathermap.internal.config.OpenWeatherMapWeatherAndForecastConfiguration;
import org.eclipse.smarthome.binding.openweathermap.internal.connection.OpenWeatherMapCommunicationException;
import org.eclipse.smarthome.binding.openweathermap.internal.connection.OpenWeatherMapConfigurationException;
import org.eclipse.smarthome.binding.openweathermap.internal.connection.OpenWeatherMapConnection;
import org.eclipse.smarthome.binding.openweathermap.internal.model.OpenWeatherMapJsonDailyForecastData;
import org.eclipse.smarthome.binding.openweathermap.internal.model.OpenWeatherMapJsonHourlyForecastData;
import org.eclipse.smarthome.binding.openweathermap.internal.model.OpenWeatherMapJsonWeatherData;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link OpenWeatherMapWeatherAndForecastHandler} is responsible for handling commands, which are sent to one of
 * the channels.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class OpenWeatherMapWeatherAndForecastHandler extends AbstractOpenWeatherMapHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenWeatherMapWeatherAndForecastHandler.class);

    private static final String CHANNEL_GROUP_HOURLY_FORECAST_PREFIX = "forecastHours";
    private static final String CHANNEL_GROUP_DAILY_FORECAST_PREFIX = "forecastDay";
    private static final Pattern CHANNEL_GROUP_HOURLY_FORECAST_PREFIX_PATTERN = Pattern
            .compile(CHANNEL_GROUP_HOURLY_FORECAST_PREFIX + "([0-9]*)");
    private static final Pattern CHANNEL_GROUP_DAILY_FORECAST_PREFIX_PATTERN = Pattern
            .compile(CHANNEL_GROUP_DAILY_FORECAST_PREFIX + "([0-9]*)");

    // keeps track of the parsed counts
    private int forecastHours = 24;
    private int forecastDays = 6;

    private @Nullable OpenWeatherMapJsonWeatherData weatherData;
    private @Nullable OpenWeatherMapJsonHourlyForecastData hourlyForecastData;
    private @Nullable OpenWeatherMapJsonDailyForecastData dailyForecastData;

    public OpenWeatherMapWeatherAndForecastHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        logger.debug("Initialize OpenWeatherMapWeatherAndForecastHandler handler '{}'.", getThing().getUID());
        OpenWeatherMapWeatherAndForecastConfiguration config = getConfigAs(
                OpenWeatherMapWeatherAndForecastConfiguration.class);

        boolean configValid = true;
        int newForecastHours = config.getForecastHours();
        if (newForecastHours < 0 || newForecastHours > 120 || newForecastHours % 3 != 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-not-supported-number-of-hours");
            configValid = false;
        }
        int newForecastDays = config.getForecastDays();
        if (newForecastDays < 0 || newForecastDays > 16) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-not-supported-number-of-days");
            configValid = false;
        }

        if (configValid) {
            logger.debug("Rebuilding thing '{}'.", getThing().getUID());
            List<Channel> toBeAddedChannels = new ArrayList<>();
            List<Channel> toBeRemovedChannels = new ArrayList<>();
            if (forecastHours != newForecastHours) {
                logger.debug("Rebuilding hourly forecast channel groups.");
                if (forecastHours > newForecastHours) {
                    for (int i = newForecastHours + 3; i <= forecastHours; i += 3) {
                        toBeRemovedChannels.addAll(removeChannelsOfGroup(
                                CHANNEL_GROUP_HOURLY_FORECAST_PREFIX + ((i < 10) ? "0" : "") + Integer.toString(i)));
                    }
                } else {
                    for (int i = forecastHours + 3; i <= newForecastHours; i += 3) {
                        toBeAddedChannels.addAll(createChannelsForGroup(
                                CHANNEL_GROUP_HOURLY_FORECAST_PREFIX + ((i < 10) ? "0" : "") + Integer.toString(i),
                                CHANNEL_GROUP_TYPE_HOURLY_FORECAST));
                    }
                }
                forecastHours = newForecastHours;
            }
            if (forecastDays != newForecastDays) {
                logger.debug("Rebuilding daily forecast channel groups.");
                if (forecastDays > newForecastDays) {
                    if (newForecastDays < 1) {
                        toBeRemovedChannels.addAll(removeChannelsOfGroup(CHANNEL_GROUP_FORECAST_TODAY));
                    }
                    if (newForecastDays < 2) {
                        toBeRemovedChannels.addAll(removeChannelsOfGroup(CHANNEL_GROUP_FORECAST_TOMORROW));
                    }
                    for (int i = newForecastDays; i < forecastDays; ++i) {
                        toBeRemovedChannels.addAll(
                                removeChannelsOfGroup(CHANNEL_GROUP_DAILY_FORECAST_PREFIX + Integer.toString(i)));
                    }
                } else {
                    if (forecastDays == 0 && newForecastDays > 0) {
                        toBeAddedChannels.addAll(createChannelsForGroup(CHANNEL_GROUP_FORECAST_TODAY,
                                CHANNEL_GROUP_TYPE_DAILY_FORECAST));
                    }
                    if (forecastDays <= 1 && newForecastDays > 1) {
                        toBeAddedChannels.addAll(createChannelsForGroup(CHANNEL_GROUP_FORECAST_TOMORROW,
                                CHANNEL_GROUP_TYPE_DAILY_FORECAST));
                    }
                    for (int i = (forecastDays < 2) ? 2 : forecastDays; i < newForecastDays; ++i) {
                        toBeAddedChannels.addAll(
                                createChannelsForGroup(CHANNEL_GROUP_DAILY_FORECAST_PREFIX + Integer.toString(i),
                                        CHANNEL_GROUP_TYPE_DAILY_FORECAST));
                    }
                }
                forecastDays = newForecastDays;
            }
            ThingBuilder builder = editThing().withoutChannels(toBeRemovedChannels);
            for (Channel channel : toBeAddedChannels) {
                builder.withChannel(channel);
            }
            updateThing(builder.build());
        }
    }

    @Override
    protected boolean requestData(OpenWeatherMapConnection connection)
            throws OpenWeatherMapCommunicationException, OpenWeatherMapConfigurationException {
        logger.debug("Update weather and forecast data of thing '{}'.", getThing().getUID());
        try {
            weatherData = connection.getWeatherData(location);
            if (forecastHours > 0) {
                hourlyForecastData = connection.getHourlyForecastData(location, forecastHours / 3);
            }
            if (forecastDays > 0) {
                try {
                    dailyForecastData = connection.getDailyForecastData(location, forecastDays);
                } catch (OpenWeatherMapConfigurationException e) {
                    if (e.getCause() instanceof HttpResponseException) {
                        logger.warn(e.getLocalizedMessage());
                        forecastDays = 0;
                        Configuration editConfig = editConfiguration();
                        editConfig.put(CONFIG_FORECAST_DAYS, 0);
                        updateConfiguration(editConfig);
                        logger.debug("Removing daily forecast channel groups.");
                        List<Channel> channels = getThing().getChannels().stream()
                                .filter(c -> CHANNEL_GROUP_FORECAST_TODAY.equals(c.getUID().getGroupId())
                                        || CHANNEL_GROUP_FORECAST_TOMORROW.equals(c.getUID().getGroupId())
                                        || c.getUID().getGroupId().startsWith(CHANNEL_GROUP_DAILY_FORECAST_PREFIX))
                                .collect(Collectors.toList());
                        updateThing(editThing().withoutChannels(channels).build());
                    } else {
                        throw e;
                    }
                }
            }
            return true;
        } catch (JsonSyntaxException e) {
            logger.debug("JsonSyntaxException occurred during execution: {}", e.getLocalizedMessage(), e);
            return false;
        }
    }

    @Override
    protected void updateChannel(ChannelUID channelUID) {
        String channelGroupId = channelUID.getGroupId();
        switch (channelGroupId) {
            case CHANNEL_GROUP_STATION:
            case CHANNEL_GROUP_CURRENT_WEATHER:
                updateCurrentChannel(channelUID);
                break;
            case CHANNEL_GROUP_FORECAST_TODAY:
                updateDailyForecastChannel(channelUID, 0);
                break;
            case CHANNEL_GROUP_FORECAST_TOMORROW:
                updateDailyForecastChannel(channelUID, 1);
                break;
            default:
                int i;
                Matcher hourlyForecastMatcher = CHANNEL_GROUP_HOURLY_FORECAST_PREFIX_PATTERN.matcher(channelGroupId);
                if (hourlyForecastMatcher.find() && (i = Integer.parseInt(hourlyForecastMatcher.group(1))) >= 3
                        && i <= 120) {
                    updateHourlyForecastChannel(channelUID, (i / 3) - 1);
                    break;
                }
                Matcher dailyForecastMatcher = CHANNEL_GROUP_DAILY_FORECAST_PREFIX_PATTERN.matcher(channelGroupId);
                if (dailyForecastMatcher.find() && (i = Integer.parseInt(dailyForecastMatcher.group(1))) > 1
                        && i <= 16) {
                    updateDailyForecastChannel(channelUID, i);
                    break;
                }
                break;
        }
    }

    /**
     * Update the channel from the last OpenWeatherMap data retrieved.
     *
     * @param channelUID the id identifying the channel to be updated
     */
    private void updateCurrentChannel(ChannelUID channelUID) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        if (weatherData != null) {
            State state = UnDefType.UNDEF;
            switch (channelId) {
                case CHANNEL_STATION_ID:
                    state = getStringTypeState(weatherData.getId().toString());
                    break;
                case CHANNEL_STATION_NAME:
                    state = getStringTypeState(weatherData.getName());
                    break;
                case CHANNEL_STATION_LOCATION:
                    state = getPointTypeState(weatherData.getCoord().getLat(), weatherData.getCoord().getLon());
                    break;
                case CHANNEL_TIME_STAMP:
                    state = getDateTimeTypeState(weatherData.getDt());
                    break;
                case CHANNEL_CONDITION:
                    state = getStringTypeState(weatherData.getWeather().get(0).getDescription());
                    break;
                case CHANNEL_CONDITION_ID:
                    state = getStringTypeState(weatherData.getWeather().get(0).getId().toString());
                    break;
                case CHANNEL_CONDITION_ICON:
                    state = getRawTypeState(
                            OpenWeatherMapConnection.getWeatherIcon(weatherData.getWeather().get(0).getIcon()));
                    break;
                case CHANNEL_TEMPERATURE:
                    state = getQuantityTypeState(weatherData.getMain().getTemp(), CELSIUS);
                    break;
                case CHANNEL_PRESSURE:
                    state = getQuantityTypeState(weatherData.getMain().getPressure(), HECTO(PASCAL));
                    break;
                case CHANNEL_HUMIDITY:
                    state = getQuantityTypeState(weatherData.getMain().getHumidity(), SmartHomeUnits.PERCENT);
                    break;
                case CHANNEL_WIND_SPEED:
                    state = getQuantityTypeState(weatherData.getWind().getSpeed(), SmartHomeUnits.METRE_PER_SECOND);
                    break;
                case CHANNEL_WIND_DIRECTION:
                    state = getQuantityTypeState(weatherData.getWind().getDeg(), SmartHomeUnits.DEGREE_ANGLE);
                    break;
                case CHANNEL_GUST_SPEED:
                    state = getQuantityTypeState(weatherData.getWind().getGust(), SmartHomeUnits.METRE_PER_SECOND);
                    break;
                case CHANNEL_CLOUDINESS:
                    state = getQuantityTypeState(weatherData.getClouds().getAll(), SmartHomeUnits.PERCENT);
                    break;
                case CHANNEL_RAIN:
                    state = getQuantityTypeState(
                            weatherData.getRain() == null || weatherData.getRain().get3h() == null ? 0
                                    : weatherData.getRain().get3h(),
                            MILLI(METRE));
                    break;
                case CHANNEL_SNOW:
                    state = getQuantityTypeState(
                            weatherData.getSnow() == null || weatherData.getSnow().get3h() == null ? 0
                                    : weatherData.getSnow().get3h(),
                            MILLI(METRE));
                    break;
            }
            logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
            updateState(channelUID, state);
        } else {
            logger.debug("No weather data available to update channel '{}' of group '{}'.", channelId, channelGroupId);
        }
    }

    /**
     * Update the channel from the last OpenWeatherMap data retrieved.
     *
     * @param channelUID the id identifying the channel to be updated
     * @param count
     */
    private void updateHourlyForecastChannel(ChannelUID channelUID, int count) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        if (hourlyForecastData != null && hourlyForecastData.getList().size() > count) {
            org.eclipse.smarthome.binding.openweathermap.internal.model.forecast.hourly.List forecastData = hourlyForecastData
                    .getList().get(count);
            State state = UnDefType.UNDEF;
            switch (channelId) {
                case CHANNEL_TIME_STAMP:
                    state = getDateTimeTypeState(forecastData.getDt());
                    break;
                case CHANNEL_CONDITION:
                    state = getStringTypeState(forecastData.getWeather().get(0).getDescription());
                    break;
                case CHANNEL_CONDITION_ID:
                    state = getStringTypeState(forecastData.getWeather().get(0).getId().toString());
                    break;
                case CHANNEL_CONDITION_ICON:
                    state = getRawTypeState(
                            OpenWeatherMapConnection.getWeatherIcon(forecastData.getWeather().get(0).getIcon()));
                    break;
                case CHANNEL_TEMPERATURE:
                    state = getQuantityTypeState(forecastData.getMain().getTemp(), CELSIUS);
                    break;
                case CHANNEL_PRESSURE:
                    state = getQuantityTypeState(forecastData.getMain().getPressure(), HECTO(PASCAL));
                    break;
                case CHANNEL_HUMIDITY:
                    state = getQuantityTypeState(forecastData.getMain().getHumidity(), SmartHomeUnits.PERCENT);
                    break;
                case CHANNEL_WIND_SPEED:
                    state = getQuantityTypeState(forecastData.getWind().getSpeed(), SmartHomeUnits.METRE_PER_SECOND);
                    break;
                case CHANNEL_WIND_DIRECTION:
                    state = getQuantityTypeState(forecastData.getWind().getDeg(), SmartHomeUnits.DEGREE_ANGLE);
                    break;
                case CHANNEL_GUST_SPEED:
                    state = getQuantityTypeState(forecastData.getWind().getGust(), SmartHomeUnits.METRE_PER_SECOND);
                    break;
                case CHANNEL_CLOUDINESS:
                    state = getQuantityTypeState(forecastData.getClouds().getAll(), SmartHomeUnits.PERCENT);
                    break;
                case CHANNEL_RAIN:
                    state = getQuantityTypeState(
                            forecastData.getRain() == null || forecastData.getRain().get3h() == null ? 0
                                    : forecastData.getRain().get3h(),
                            MILLI(METRE));
                    break;
                case CHANNEL_SNOW:
                    state = getQuantityTypeState(
                            forecastData.getSnow() == null || forecastData.getSnow().get3h() == null ? 0
                                    : forecastData.getSnow().get3h(),
                            MILLI(METRE));
                    break;
            }
            logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
            updateState(channelUID, state);
        } else {
            logger.debug("No weather data available to update channel '{}' of group '{}'.", channelId, channelGroupId);
        }
    }

    /**
     * Update the channel from the last OpenWeatherMap data retrieved.
     *
     * @param channelUID the id identifying the channel to be updated
     * @param count
     */
    private void updateDailyForecastChannel(ChannelUID channelUID, int count) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        if (dailyForecastData != null && dailyForecastData.getList().size() > count) {
            org.eclipse.smarthome.binding.openweathermap.internal.model.forecast.daily.List forecastData = dailyForecastData
                    .getList().get(count);
            State state = UnDefType.UNDEF;
            switch (channelId) {
                case CHANNEL_TIME_STAMP:
                    state = getDateTimeTypeState(forecastData.getDt());
                    break;
                case CHANNEL_CONDITION:
                    state = getStringTypeState(forecastData.getWeather().get(0).getDescription());
                    break;
                case CHANNEL_CONDITION_ID:
                    state = getStringTypeState(forecastData.getWeather().get(0).getId().toString());
                    break;
                case CHANNEL_CONDITION_ICON:
                    state = getRawTypeState(
                            OpenWeatherMapConnection.getWeatherIcon(forecastData.getWeather().get(0).getIcon()));
                    break;
                case CHANNEL_MIN_TEMPERATURE:
                    state = getQuantityTypeState(forecastData.getTemp().getMin(), CELSIUS);
                    break;
                case CHANNEL_MAX_TEMPERATURE:
                    state = getQuantityTypeState(forecastData.getTemp().getMax(), CELSIUS);
                    break;
                case CHANNEL_PRESSURE:
                    state = getQuantityTypeState(forecastData.getPressure(), HECTO(PASCAL));
                    break;
                case CHANNEL_HUMIDITY:
                    state = getQuantityTypeState(forecastData.getHumidity(), SmartHomeUnits.PERCENT);
                    break;
                case CHANNEL_WIND_SPEED:
                    state = getQuantityTypeState(forecastData.getSpeed(), SmartHomeUnits.METRE_PER_SECOND);
                    break;
                case CHANNEL_WIND_DIRECTION:
                    state = getQuantityTypeState(forecastData.getDeg(), SmartHomeUnits.DEGREE_ANGLE);
                    break;
                case CHANNEL_GUST_SPEED:
                    state = getQuantityTypeState(forecastData.getGust(), SmartHomeUnits.METRE_PER_SECOND);
                    break;
                case CHANNEL_CLOUDINESS:
                    state = getQuantityTypeState(forecastData.getClouds(), SmartHomeUnits.PERCENT);
                    break;
                case CHANNEL_RAIN:
                    state = getQuantityTypeState(forecastData.getRain() == null ? 0 : forecastData.getRain(),
                            MILLI(METRE));
                    break;
                case CHANNEL_SNOW:
                    state = getQuantityTypeState(forecastData.getSnow() == null ? 0 : forecastData.getSnow(),
                            MILLI(METRE));
                    break;
            }
            logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
            updateState(channelUID, state);
        } else {
            logger.debug("No weather data available to update channel '{}' of group '{}'.", channelId, channelGroupId);
        }
    }
}
