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
package org.eclipse.smarthome.binding.openweathermap.internal.connection;

import static java.util.stream.Collectors.joining;
import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.eclipse.jetty.http.HttpStatus.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.smarthome.binding.openweathermap.internal.config.OpenWeatherMapAPIConfiguration;
import org.eclipse.smarthome.binding.openweathermap.internal.handler.OpenWeatherMapAPIHandler;
import org.eclipse.smarthome.binding.openweathermap.internal.model.OpenWeatherMapJsonDailyForecastData;
import org.eclipse.smarthome.binding.openweathermap.internal.model.OpenWeatherMapJsonHourlyForecastData;
import org.eclipse.smarthome.binding.openweathermap.internal.model.OpenWeatherMapJsonWeatherData;
import org.eclipse.smarthome.binding.openweathermap.internal.utils.ByteArrayFileCache;
import org.eclipse.smarthome.core.cache.ExpiringCacheMap;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link OpenWeatherMapConnection} is responsible for handling the connections to OpenWeatherMap API.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class OpenWeatherMapConnection {

    private final Logger logger = LoggerFactory.getLogger(OpenWeatherMapConnection.class);

    private static final String PROPERTY_MESSAGE = "message";

    private static final String PNG_CONTENT_TYPE = "image/png";

    private static final String PARAM_APPID = "appid";
    private static final String PARAM_UNITS = "units";
    private static final String PARAM_LAT = "lat";
    private static final String PARAM_LON = "lon";
    private static final String PARAM_LANG = "lang";
    private static final String PARAM_FORECAST_CNT = "cnt";

    // Current weather data (see https://openweathermap.org/current)
    private static final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";
    // 5 day / 3 hour forecast (see https://openweathermap.org/forecast5)
    private static final String THREE_HOUR_FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast";
    // 16 day / daily forecast (see https://openweathermap.org/forecast16)
    private static final String DAILY_FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast/daily";
    // Weather icons (see https://openweathermap.org/weather-conditions)
    private static final String ICON_URL = "https://openweathermap.org/img/w/%s.png";

    private final OpenWeatherMapAPIHandler handler;
    private final HttpClient httpClient;

    private static final ByteArrayFileCache IMAGE_CACHE = new ByteArrayFileCache(
            "org.eclipse.smarthome.binding.openweathermap");
    private final ExpiringCacheMap<String, String> cache;

    private final JsonParser parser = new JsonParser();
    private final Gson gson = new Gson();

    public OpenWeatherMapConnection(OpenWeatherMapAPIHandler handler, HttpClient httpClient) {
        this.handler = handler;
        this.httpClient = httpClient;

        OpenWeatherMapAPIConfiguration config = handler.getOpenWeatherMapAPIConfig();
        cache = new ExpiringCacheMap<>(TimeUnit.MINUTES.toMillis(config.getRefreshInterval()));
    }

    /**
     * Requests the current weather data for the given location (see https://openweathermap.org/current).
     *
     * @param location location represented as {@link PointType}
     * @return the current weather data
     * @throws JsonSyntaxException
     * @throws OpenWeatherMapCommunicationException
     * @throws OpenWeatherMapConfigurationException
     */
    public synchronized @Nullable OpenWeatherMapJsonWeatherData getWeatherData(@Nullable PointType location)
            throws JsonSyntaxException, OpenWeatherMapCommunicationException, OpenWeatherMapConfigurationException {
        return gson.fromJson(
                getResponseFromCache(
                        buildURL(WEATHER_URL, getRequestParams(handler.getOpenWeatherMapAPIConfig(), location))),
                OpenWeatherMapJsonWeatherData.class);
    }

    /**
     * Requests the hourly forecast data for the given location (see https://openweathermap.org/forecast5).
     *
     * @param location location represented as {@link PointType}
     * @param count number of hours
     * @return the hourly forecast data
     * @throws JsonSyntaxException
     * @throws OpenWeatherMapCommunicationException
     * @throws OpenWeatherMapConfigurationException
     */
    public synchronized @Nullable OpenWeatherMapJsonHourlyForecastData getHourlyForecastData(
            @Nullable PointType location, int count)
            throws JsonSyntaxException, OpenWeatherMapCommunicationException, OpenWeatherMapConfigurationException {
        if (count <= 0) {
            throw new OpenWeatherMapConfigurationException("@text/offline.conf-error-not-supported-number-of-hours");
        }

        Map<String, String> params = getRequestParams(handler.getOpenWeatherMapAPIConfig(), location);
        params.put(PARAM_FORECAST_CNT, Integer.toString(count));

        return gson.fromJson(getResponseFromCache(buildURL(THREE_HOUR_FORECAST_URL, params)),
                OpenWeatherMapJsonHourlyForecastData.class);
    }

    /**
     * Requests the daily forecast data for the given location (see https://openweathermap.org/forecast16).
     *
     * @param location location represented as {@link PointType}
     * @param count number of days
     * @return the daily forecast data
     * @throws JsonSyntaxException
     * @throws OpenWeatherMapCommunicationException
     * @throws OpenWeatherMapConfigurationException
     */
    public synchronized @Nullable OpenWeatherMapJsonDailyForecastData getDailyForecastData(@Nullable PointType location,
            int count)
            throws JsonSyntaxException, OpenWeatherMapCommunicationException, OpenWeatherMapConfigurationException {
        if (count <= 0) {
            throw new OpenWeatherMapConfigurationException("@text/offline.conf-error-not-supported-number-of-days");
        }

        Map<String, String> params = getRequestParams(handler.getOpenWeatherMapAPIConfig(), location);
        params.put(PARAM_FORECAST_CNT, Integer.toString(count));

        return gson.fromJson(getResponseFromCache(buildURL(DAILY_FORECAST_URL, params)),
                OpenWeatherMapJsonDailyForecastData.class);
    }

    /**
     * Downloads the icon for the given icon id (see https://openweathermap.org/weather-conditions).
     *
     * @param iconId the id of the icon
     * @return the weather icon as {@link RawType}
     */
    public static @Nullable RawType getWeatherIcon(String iconId) {
        if (StringUtils.isEmpty(iconId)) {
            throw new IllegalArgumentException("Cannot download weather icon as icon id is null.");
        }

        return downloadWeatherIconFromCache(String.format(ICON_URL, iconId));
    }

    private static @Nullable RawType downloadWeatherIconFromCache(String url) {
        if (IMAGE_CACHE.containsKey(url)) {
            return new RawType(IMAGE_CACHE.get(url), PNG_CONTENT_TYPE);
        } else {
            RawType image = downloadWeatherIcon(url);
            if (image != null) {
                IMAGE_CACHE.put(url, image.getBytes());
                return image;
            }
        }
        return null;
    }

    private static @Nullable RawType downloadWeatherIcon(String url) {
        return HttpUtil.downloadImage(url);
    }

    private Map<String, String> getRequestParams(OpenWeatherMapAPIConfiguration config, @Nullable PointType location) {
        if (location == null) {
            throw new OpenWeatherMapConfigurationException("@text/offline.conf-error-missing-location");
        }

        Map<String, String> params = new HashMap<>();
        // API key (see http://openweathermap.org/appid)
        params.put(PARAM_APPID, StringUtils.trimToEmpty(config.getApikey()));

        // Units format (see https://openweathermap.org/current#data)
        params.put(PARAM_UNITS, "metric");

        // By geographic coordinates (see https://openweathermap.org/current#geo)
        params.put(PARAM_LAT, location.getLatitude().toString());
        params.put(PARAM_LON, location.getLongitude().toString());

        // Multilingual support (see https://openweathermap.org/current#multi)
        String language = StringUtils.trimToEmpty(config.getLanguage());
        if (!language.isEmpty()) {
            params.put(PARAM_LANG, language.toLowerCase());
        }
        return params;
    }

    private String buildURL(String url, Map<String, String> requestParams) {
        return requestParams.keySet().stream().map(key -> key + "=" + encodeParam(requestParams.get(key)))
                .collect(joining("&", url + "?", StringUtils.EMPTY));
    }

    private String encodeParam(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            logger.debug("UnsupportedEncodingException occurred during execution: {}", e.getLocalizedMessage(), e);
            return StringUtils.EMPTY;
        }
    }

    private @Nullable String getResponseFromCache(String url) {
        return cache.putIfAbsentAndGet(url, () -> getResponse(url));
    }

    private String getResponse(String url) {
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("OpenWeatherMap request: URL = '{}'", uglifyApikey(url));
            }
            ContentResponse contentResponse = httpClient.newRequest(url).method(GET).timeout(10, TimeUnit.SECONDS)
                    .send();
            int httpStatus = contentResponse.getStatus();
            String content = contentResponse.getContentAsString();
            String errorMessage = StringUtils.EMPTY;
            logger.trace("OpenWeatherMap response: status = {}, content = '{}'", httpStatus, content);
            switch (httpStatus) {
                case OK_200:
                    return content;
                case BAD_REQUEST_400:
                case UNAUTHORIZED_401:
                case NOT_FOUND_404:
                    errorMessage = getErrorMessage(content);
                    logger.debug("OpenWeatherMap server responded with status code {}: {}", httpStatus, errorMessage);
                    throw new OpenWeatherMapConfigurationException(errorMessage);
                case TOO_MANY_REQUESTS_429:
                    // TODO disable refresh job temporarily (see https://openweathermap.org/appid#Accesslimitation)
                default:
                    errorMessage = getErrorMessage(content);
                    logger.debug("OpenWeatherMap server responded with status code {}: {}", httpStatus, errorMessage);
                    throw new OpenWeatherMapCommunicationException(errorMessage);
            }
        } catch (ExecutionException e) {
            String errorMessage = e.getLocalizedMessage();
            logger.trace("Exception occurred during execution: {}", errorMessage, e);
            if (e.getCause() instanceof HttpResponseException) {
                logger.debug("OpenWeatherMap server responded with status code {}: Invalid API key.", UNAUTHORIZED_401);
                throw new OpenWeatherMapConfigurationException("@text/offline.conf-error-invalid-apikey", e.getCause());
            } else {
                throw new OpenWeatherMapCommunicationException(errorMessage, e.getCause());
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Exception occurred during execution: {}", e.getLocalizedMessage(), e);
            throw new OpenWeatherMapCommunicationException(e.getLocalizedMessage(), e.getCause());
        }
    }

    private String uglifyApikey(String url) {
        return url.replaceAll("(appid=)+\\w+", "appid=*****");
    }

    private String getErrorMessage(String response) {
        JsonObject jsonResponse = parser.parse(response).getAsJsonObject();
        if (jsonResponse.has(PROPERTY_MESSAGE)) {
            return jsonResponse.get(PROPERTY_MESSAGE).getAsString();
        }
        return response;
    }
}
