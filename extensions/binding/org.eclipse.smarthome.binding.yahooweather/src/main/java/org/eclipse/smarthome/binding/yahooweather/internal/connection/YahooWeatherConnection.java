/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.yahooweather.internal.connection;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation for Yahoo Weather url connection.
 * 
 * @author Christoph Weitkamp - Changed use of caching utils to ESH ExpiringCacheMap
 * 
 */
public class YahooWeatherConnection {

    private final Logger logger = LoggerFactory.getLogger(YahooWeatherConnection.class);

    private final String WEBSERVICE_URL = "https://query.yahooapis.com/v1/public/yql?format=json";

    private final int WEBSERVICE_TIMEOUT = 10 * 1000; // 10s

    public String getResponseFromQuery(String query) {
        try {
            return HttpUtil.executeUrl("GET",
                    WEBSERVICE_URL + "&q=" + URLEncoder.encode(query, StandardCharsets.UTF_8.toString()),
                    WEBSERVICE_TIMEOUT);
        } catch (IOException e) {
            logger.warn("Communication error occurred while getting Yahoo weather information: {}", e.getMessage());
        }
        return null;
    }
}
