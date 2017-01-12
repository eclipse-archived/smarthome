/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.yahooweather.internal.ExpiringCache;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusThingHandler;
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
 * @author Thomas Höfer - Added config status provider
 */
public class YahooWeatherHandler extends ConfigStatusThingHandler {

    private static final String LOCATION_PARAM = "location";

    private final Logger logger = LoggerFactory.getLogger(YahooWeatherHandler.class);

    private final int MAX_DATA_AGE = 3 * 60 * 60 * 1000; // 3h
    private final int CACHE_EXPIRY = 10 * 1000; // 10s
    private long lastUpdateTime;

    private BigDecimal location;
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

        location = (BigDecimal) config.get(LOCATION_PARAM);

        try {
            refresh = (BigDecimal) config.get("refresh");
        } catch (Exception e) {
            logger.debug("Cannot set refresh parameter.", e);
        }

        if (refresh == null) {
            // let's go for the default
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

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        Collection<ConfigStatusMessage> configStatus = new ArrayList<>();

        try {
            String locationData = getWeatherData(
                    "SELECT location FROM weather.forecast WHERE woeid = " + location.toPlainString());
            String city = getValue(locationData, "location", "city");
            if (city == null) {
                configStatus.add(ConfigStatusMessage.Builder.error(LOCATION_PARAM)
                        .withMessageKeySuffix("location-not-found").withArguments(location.toPlainString()).build());
            }
        } catch (IOException e) {
            logger.debug("Communication error occurred while getting Yahoo weather information.", e);
        }

        return configStatus;
    }

    private synchronized boolean updateWeatherData() {
        try {
            String data = getWeatherData(
                    "SELECT * FROM weather.forecast WHERE u = 'c' AND woeid = " + location.toPlainString());
            if (data != null) {
                if (data.contains("\"results\":null")) {
                    if (isCurrentDataExpired()) {
                        weatherData = null;
                        logger.trace(
                                "The Yahoo Weather API did not return any data. Omiting the old result because it became too old.");
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                                "The Yahoo Weather API did not return any data.");
                        return false;
                    } else {
                        // simply keep the old data
                        logger.trace("The Yahoo Weather API did not return any data. Keeping the old result.");
                        return false;
                    }
                } else {
                    lastUpdateTime = System.currentTimeMillis();
                    weatherData = data;
                }
                updateStatus(ThingStatus.ONLINE);
                return true;
            }
        } catch (IOException e) {
            logger.warn("Error accessing Yahoo weather: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
        weatherData = null;
        return false;
    }

    private boolean isCurrentDataExpired() {
        return lastUpdateTime + MAX_DATA_AGE < System.currentTimeMillis();
    }

    private final ExpiringCache<String, String> CACHE = new ExpiringCache<String, String>(CACHE_EXPIRY,
            new ExpiringCache.LoadAction<String, String>() {
                @Override
                public String load(String query) throws IOException {
                    try {
                        URL url = new URL("https://query.yahooapis.com/v1/public/yql?format=json&q="
                                + query.replaceAll(" ", "%20").replaceAll("'", "%27"));
                        URLConnection connection = url.openConnection();
                        return IOUtils.toString(connection.getInputStream());
                    } catch (MalformedURLException e) {
                        logger.debug("Constructed query url '{}' is not valid: {}", query, e.getMessage());
                        throw e;
                    }
                }
            });

    private String getWeatherData(String query) throws IOException {
        return CACHE.get(query);
    }

    private State getHumidity() {
        if (weatherData != null) {
            String humidity = getValue(weatherData, "atmosphere", "humidity");
            if (humidity != null) {
                return new DecimalType(humidity);
            }
        }
        return UnDefType.UNDEF;
    }

    private State getPressure() {
        if (weatherData != null) {
            String pressure = getValue(weatherData, "atmosphere", "pressure");
            if (pressure != null) {
                DecimalType ret = new DecimalType(pressure);
                if (ret.doubleValue() > 10000.0) {
                    // Unreasonably high, record so far was 1085,8 hPa
                    // The Yahoo API currently returns inHg values although it claims they are mbar - therefore convert
                    ret = new DecimalType(BigDecimal.valueOf((long) (ret.doubleValue() / 0.3386388158), 2));
                }
                return ret;
            }
        }
        return UnDefType.UNDEF;
    }

    private State getTemperature() {
        if (weatherData != null) {
            String temp = getValue(weatherData, "condition", "temp");
            if (temp != null) {
                return new DecimalType(temp);
            }
        }
        return UnDefType.UNDEF;
    }

    private String getValue(String data, String element, String param) {
        String tmp = StringUtils.substringAfter(data, element);
        if (tmp != null) {
            return StringUtils.substringBetween(tmp, param + "\":\"", "\"");
        }
        return null;
    }
}
