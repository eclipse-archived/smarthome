/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.yahooweather.discovery;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.binding.yahooweather.YahooWeatherBindingConstants;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * The {@link YahooWeatherDiscoveryService} is responsible for discovering a weather Thing
 * of the local city from the IP address
 *
 * @author Marcel Verpaalen - Initial contribution
 * @author Jochen Hiller - Removed dependency to Apache HttpClient 3.x
 * @author Andre Fuechsel - Added call of removeOlderResults
 * @author Thomas Höfer - Added representation
 */

public class YahooWeatherDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(YahooWeatherDiscoveryService.class);

    public YahooWeatherDiscoveryService() {
        super(YahooWeatherBindingConstants.SUPPORTED_THING_TYPES_UIDS, 10);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return YahooWeatherBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    /**
     * Download url content to a String
     * 
     * @param url as String
     * @return String with body
     */
    private String downloadData(String urlString) {
        String downloadDataString = null;
        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            downloadDataString = IOUtils.toString(connection.getInputStream());
        } catch (MalformedURLException e) {
            logger.debug("Constructed url '{}' is not valid: {}", urlString, e.getMessage());
        } catch (IOException e) {
            logger.error("Error accessing url '{}' : {} ", urlString, e.getMessage());
        }

        return downloadDataString;
    }

    /**
     * Retrieves an element from Json based on the path
     * 
     * @param Json
     * @param path to find
     * @return JsonElement from the path
     */
    private static JsonElement getAtPath(JsonElement e, String path) {
        JsonElement current = e;
        String ss[] = path.split("/");
        for (int i = 0; i < ss.length; i++) {
            current = current.getAsJsonObject().get(ss[i]);
        }
        return current;
    }

    /**
     * Retrieves the woeid (Where On Earth IDentifier) used for determining the location
     * used in the Yahoo Weather interface
     * 
     * @param Coordinate in form latitude,longitude as String
     * @return Json text from woeid service as String
     */
    private String getWoeidData(String coordinate) {
        String query = "SELECT * FROM geo.placefinder WHERE text='" + coordinate + "' and gflags='R'";
        String url = null;
        try {
            url = "https://query.yahooapis.com/v1/public/yql";
            url += "?q=" + URLEncoder.encode(query, "UTF-8") + "&format=json";
        } catch (Exception e) {
            logger.debug("Error while getting location ID: {}", e.getMessage());
        }
        return downloadData(url);
    }

    /**
     * Retrieves the location based on the IP address
     * 
     * @return Json text as String with location data
     */
    private String getGeoFromIpData() {
        String url = "http://freegeoip.net/json/";
        return downloadData(url);
    }

    /**
     * Discover the location and the woeid then submits the result
     */
    private void discoverLocation() {
        String locationName = null;
        String locationWoeid = null;
        try {
            JsonElement locationData = new JsonParser().parse(getGeoFromIpData());
            locationName = getAtPath(locationData, "city").getAsString() + ", "
                    + getAtPath(locationData, "country_name").getAsString();
            String locationCoords = getAtPath(locationData, "latitude").getAsString() + ", "
                    + getAtPath(locationData, "longitude").getAsString();
            logger.debug("Location from IP: '{}' coordinates: '{}'", locationName, locationCoords);

            JsonElement woeidData = new JsonParser().parse(getWoeidData(locationCoords));
            locationName = getAtPath(woeidData, "query/results/Result/city").getAsString() + ", "
                    + getAtPath(woeidData, "query/results/Result/country").getAsString();
            locationWoeid = getAtPath(woeidData, "query/results/Result/woeid").getAsString();
            logger.debug("Location from locationID: '{}' is: '{}'", locationWoeid, locationName);
        } catch (Exception e) {
            logger.debug("Error while getting location ID: {}", e.getMessage());
        }

        if (locationWoeid != null) {
            submitDiscoveryResults(locationName, locationWoeid);
        }
    }

    /**
     * Submit the discovered location to the Smarthome inbox
     * 
     * @param locationName
     * @param locationWoeid
     */
    private void submitDiscoveryResults(String locationName, String locationWoeid) {
        ThingUID uid = new ThingUID(YahooWeatherBindingConstants.THING_TYPE_WEATHER, locationWoeid);
        if (uid != null) {
            Map<String, Object> properties = new HashMap<>(1);
            properties.put("location", locationWoeid);
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withRepresentationProperty("location").withLabel("Yahoo weather " + locationName).build();
            thingDiscovered(result);
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                discoverLocation();
            }
        }).start();
    }

    @Override
    protected void startScan() {
        discoverLocation();
        removeOlderResults(getTimestampOfLastScan());
    }

}
