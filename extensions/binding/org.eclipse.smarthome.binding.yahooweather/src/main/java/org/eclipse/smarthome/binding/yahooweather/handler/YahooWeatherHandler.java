/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.yahooweather.handler;

import static org.eclipse.smarthome.binding.yahooweather.YahooWeatherBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link YahooWeatherHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Stefan Bußweiler - Integrate new thing status handling
 */
public class YahooWeatherHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(YahooWeatherHandler.class);

    private String location;
    private BigDecimal refresh;

    private String weatherData = null;

    ScheduledFuture<?> refreshJob;

    public YahooWeatherHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing YahooWeather handler.");
        super.initialize();

        Configuration config = getThing().getConfiguration();

        location = (String) config.get("location");

        try {
            refresh = (BigDecimal) config.get("refresh");
        } catch (Exception e) {
            // let's ignore it and go for the default
            refresh = new BigDecimal(60);
        }

        startAutomaticRefresh();
    }

    @Override
    public void dispose() {
        refreshJob.cancel(true);
    }

    private void startAutomaticRefresh() {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    boolean success = updateWeatherData();
                    if (success) {
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE), getTemperature());
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_HUMIDITY), getHumidity());
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_PRESSURE), getPressure());
                    }
                } catch (Exception e) {
                    logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                }
            }
        };

        refreshJob = scheduler.scheduleAtFixedRate(runnable, 0, refresh.intValue(), TimeUnit.SECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            boolean success = updateWeatherData();
            if (success) {
                switch (channelUID.getId()) {
                    case CHANNEL_TEMPERATURE:
                        updateState(channelUID, getTemperature());
                        break;
                    case CHANNEL_HUMIDITY:
                        updateState(channelUID, getHumidity());
                        break;
                    case CHANNEL_PRESSURE:
                        updateState(channelUID, getPressure());
                        break;
                    default:
                        logger.debug("Command received for an unknown channel: {}", channelUID.getId());
                        break;
                }
            }
        } else {
            logger.debug("Command {} is not supported for channel: {}", command, channelUID.getId());
        }
    }

    private synchronized boolean updateWeatherData() {
        String urlString = "http://weather.yahooapis.com/forecastrss?w=" + location + "&u=c";
        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            weatherData = IOUtils.toString(connection.getInputStream());
            updateStatus(ThingStatus.ONLINE);
            return true;
        } catch (MalformedURLException e) {
            logger.debug("Constructed url '{}' is not valid: {}", urlString, e.getMessage());
            return false;
        } catch (IOException e) {
            logger.warn("Error accessing Yahoo weather: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            return false;
        }
    }

    private State getHumidity() {
        if (weatherData != null) {
            String humidity = StringUtils.substringAfter(weatherData, "yweather:atmosphere");
            humidity = StringUtils.substringBetween(humidity, "humidity=\"", "\"");
            if (humidity != null) {
                return new DecimalType(humidity);
            }
        }
        return UnDefType.UNDEF;
    }

    private State getPressure() {
        if (weatherData != null) {
            String pressure = StringUtils.substringAfter(weatherData, "yweather:atmosphere");
            pressure = StringUtils.substringBetween(pressure, "pressure=\"", "\"");
            if (pressure != null) {
                return new DecimalType(pressure);
            }
        }
        return UnDefType.UNDEF;
    }

    private State getTemperature() {
        if (weatherData != null) {
            String temp = StringUtils.substringAfter(weatherData, "yweather:condition");
            temp = StringUtils.substringBetween(temp, "temp=\"", "\"");
            if (temp != null) {
                return new DecimalType(temp);
            }
        }
        return UnDefType.UNDEF;
    }
}
