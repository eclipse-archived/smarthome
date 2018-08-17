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
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
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
    private static final String URL_QUERY = "http://api.wunderground.com/api/%APIKEY%/%FEATURES%/%SETTINGS%/q/%QUERY%.json";
    private static final int FETCH_TIMEOUT_MS = 30000;
    private static final String FEATURE_CONDITIONS = "conditions";
    private static final String FEATURE_FORECAST10DAY = "forecast10day";
    private static final String FEATURE_GEOLOOKUP = "geolookup";

    private static final Map<String, String> LANG_ISO_TO_WU_CODES = new HashMap<String, String>();
    // Codes from https://www.wunderground.com/weather/api/d/docs?d=resources/country-to-iso-matching&MR=1
    static {
        // Column 1 of the table
        LANG_ISO_TO_WU_CODES.put("AF", "AH");
        LANG_ISO_TO_WU_CODES.put("AL", "AB");
        LANG_ISO_TO_WU_CODES.put("DZ", "AL");
        LANG_ISO_TO_WU_CODES.put("AS", "AS");
        LANG_ISO_TO_WU_CODES.put("AD", "AD");
        LANG_ISO_TO_WU_CODES.put("AO", "AN");
        LANG_ISO_TO_WU_CODES.put("AI", "A1");
        LANG_ISO_TO_WU_CODES.put("AQ", "AA");
        LANG_ISO_TO_WU_CODES.put("AG", "AT");
        LANG_ISO_TO_WU_CODES.put("AR", "AG");
        LANG_ISO_TO_WU_CODES.put("AM", "AM");
        LANG_ISO_TO_WU_CODES.put("AW", "AW");
        LANG_ISO_TO_WU_CODES.put("AU", "AU");
        // AU duplicated
        LANG_ISO_TO_WU_CODES.put("AT", "OS");
        LANG_ISO_TO_WU_CODES.put("AZ", "A2");
        LANG_ISO_TO_WU_CODES.put("BS", "BS");
        LANG_ISO_TO_WU_CODES.put("BH", "BN");
        LANG_ISO_TO_WU_CODES.put("BD", "BW");
        LANG_ISO_TO_WU_CODES.put("BB", "BR");
        LANG_ISO_TO_WU_CODES.put("BY", "BY");
        LANG_ISO_TO_WU_CODES.put("BE", "BX");
        LANG_ISO_TO_WU_CODES.put("BZ", "BH");
        LANG_ISO_TO_WU_CODES.put("BJ", "BJ");
        LANG_ISO_TO_WU_CODES.put("BM", "BE");
        LANG_ISO_TO_WU_CODES.put("BT", "B2");
        LANG_ISO_TO_WU_CODES.put("BO", "BO");
        LANG_ISO_TO_WU_CODES.put("BA", "BA");
        LANG_ISO_TO_WU_CODES.put("BW", "BC");
        LANG_ISO_TO_WU_CODES.put("BV", "BV");
        LANG_ISO_TO_WU_CODES.put("BR", "BZ");
        LANG_ISO_TO_WU_CODES.put("IO", "BT");
        LANG_ISO_TO_WU_CODES.put("BN", "BF");
        LANG_ISO_TO_WU_CODES.put("BG", "BU");
        LANG_ISO_TO_WU_CODES.put("BF", "HV");
        LANG_ISO_TO_WU_CODES.put("BI", "BI");
        LANG_ISO_TO_WU_CODES.put("KH", "KH");
        LANG_ISO_TO_WU_CODES.put("CM", "CM");
        LANG_ISO_TO_WU_CODES.put("CA", "CA");
        LANG_ISO_TO_WU_CODES.put("CV", "CV");
        LANG_ISO_TO_WU_CODES.put("KY", "GC");
        LANG_ISO_TO_WU_CODES.put("CF", "CE");
        LANG_ISO_TO_WU_CODES.put("TD", "CD");
        LANG_ISO_TO_WU_CODES.put("CL", "CH");
        LANG_ISO_TO_WU_CODES.put("CN", "CI");
        LANG_ISO_TO_WU_CODES.put("CX", "CX");
        LANG_ISO_TO_WU_CODES.put("CC", "CC");
        LANG_ISO_TO_WU_CODES.put("CO", "CO");
        LANG_ISO_TO_WU_CODES.put("KM", "IC");
        LANG_ISO_TO_WU_CODES.put("CG", "CG");
        LANG_ISO_TO_WU_CODES.put("CD", "CD");
        LANG_ISO_TO_WU_CODES.put("CK", "KU");
        LANG_ISO_TO_WU_CODES.put("CR", "CS");
        LANG_ISO_TO_WU_CODES.put("CI", "IV");
        LANG_ISO_TO_WU_CODES.put("HR", "RH");
        LANG_ISO_TO_WU_CODES.put("CU", "CU");
        LANG_ISO_TO_WU_CODES.put("CY", "CY");
        LANG_ISO_TO_WU_CODES.put("CZ", "CZ");
        LANG_ISO_TO_WU_CODES.put("DK", "DN");
        LANG_ISO_TO_WU_CODES.put("DJ", "DJ");
        LANG_ISO_TO_WU_CODES.put("DM", "DO");
        LANG_ISO_TO_WU_CODES.put("DO", "DR");
        LANG_ISO_TO_WU_CODES.put("EC", "EQ");
        LANG_ISO_TO_WU_CODES.put("EG", "EG");
        LANG_ISO_TO_WU_CODES.put("SV", "ES");
        LANG_ISO_TO_WU_CODES.put("GQ", "GQ");
        LANG_ISO_TO_WU_CODES.put("ER", "E1");
        LANG_ISO_TO_WU_CODES.put("EE", "EE");
        LANG_ISO_TO_WU_CODES.put("ET", "ET");
        LANG_ISO_TO_WU_CODES.put("FK", "FK");
        LANG_ISO_TO_WU_CODES.put("FO", "FA");
        LANG_ISO_TO_WU_CODES.put("FJ", "FJ");
        LANG_ISO_TO_WU_CODES.put("FI", "FI");
        LANG_ISO_TO_WU_CODES.put("FR", "FR");
        LANG_ISO_TO_WU_CODES.put("GF", "FG");
        LANG_ISO_TO_WU_CODES.put("PF", "PF");
        LANG_ISO_TO_WU_CODES.put("TF", "TF");
        LANG_ISO_TO_WU_CODES.put("GA", "GO");
        LANG_ISO_TO_WU_CODES.put("GM", "GB");
        LANG_ISO_TO_WU_CODES.put("GE", "GE");

        // Column 2 of the table
        LANG_ISO_TO_WU_CODES.put("DE", "DL");
        LANG_ISO_TO_WU_CODES.put("GH", "GH");
        LANG_ISO_TO_WU_CODES.put("GI", "GI");
        LANG_ISO_TO_WU_CODES.put("GR", "GR");
        LANG_ISO_TO_WU_CODES.put("GL", "GL");
        LANG_ISO_TO_WU_CODES.put("GD", "GD");
        LANG_ISO_TO_WU_CODES.put("GP", "GP");
        LANG_ISO_TO_WU_CODES.put("GU", "GU");
        LANG_ISO_TO_WU_CODES.put("GT", "GU");
        LANG_ISO_TO_WU_CODES.put("GN", "GN");
        LANG_ISO_TO_WU_CODES.put("GW", "GW");
        LANG_ISO_TO_WU_CODES.put("GY", "GY");
        LANG_ISO_TO_WU_CODES.put("HT", "HA");
        LANG_ISO_TO_WU_CODES.put("HM", "HM");
        LANG_ISO_TO_WU_CODES.put("VA", "VA");
        LANG_ISO_TO_WU_CODES.put("HN", "HO");
        LANG_ISO_TO_WU_CODES.put("HK", "HK");
        LANG_ISO_TO_WU_CODES.put("HU", "HU");
        LANG_ISO_TO_WU_CODES.put("IS", "IL");
        LANG_ISO_TO_WU_CODES.put("IN", "IN");
        LANG_ISO_TO_WU_CODES.put("ID", "ID");
        LANG_ISO_TO_WU_CODES.put("IR", "IR");
        LANG_ISO_TO_WU_CODES.put("IQ", "IQ");
        LANG_ISO_TO_WU_CODES.put("IE", "IE");
        LANG_ISO_TO_WU_CODES.put("IL", "IS");
        LANG_ISO_TO_WU_CODES.put("IT", "IY");
        LANG_ISO_TO_WU_CODES.put("JM", "JM");
        LANG_ISO_TO_WU_CODES.put("JP", "JP");
        LANG_ISO_TO_WU_CODES.put("JO", "JD");
        LANG_ISO_TO_WU_CODES.put("KZ", "KZ");
        LANG_ISO_TO_WU_CODES.put("KE", "KN");
        LANG_ISO_TO_WU_CODES.put("KI", "KB");
        LANG_ISO_TO_WU_CODES.put("KP", "KR");
        LANG_ISO_TO_WU_CODES.put("KR", "KO");
        LANG_ISO_TO_WU_CODES.put("KW", "KW");
        LANG_ISO_TO_WU_CODES.put("KG", "KG");
        LANG_ISO_TO_WU_CODES.put("LA", "LA");
        LANG_ISO_TO_WU_CODES.put("LV", "LV");
        LANG_ISO_TO_WU_CODES.put("LB", "LB");
        LANG_ISO_TO_WU_CODES.put("LS", "LS");
        LANG_ISO_TO_WU_CODES.put("LR", "LI");
        LANG_ISO_TO_WU_CODES.put("LY", "LY");
        LANG_ISO_TO_WU_CODES.put("LI", "LT");
        LANG_ISO_TO_WU_CODES.put("LT", "L1");
        LANG_ISO_TO_WU_CODES.put("LU", "LU");
        LANG_ISO_TO_WU_CODES.put("MO", "MU");
        LANG_ISO_TO_WU_CODES.put("MK", "MK");
        LANG_ISO_TO_WU_CODES.put("MG", "MG");
        LANG_ISO_TO_WU_CODES.put("MW", "MW");
        LANG_ISO_TO_WU_CODES.put("MY", "MS");
        LANG_ISO_TO_WU_CODES.put("MV", "MV");
        LANG_ISO_TO_WU_CODES.put("ML", "MI");
        LANG_ISO_TO_WU_CODES.put("MT", "ML");
        LANG_ISO_TO_WU_CODES.put("MH", "MH");
        LANG_ISO_TO_WU_CODES.put("MP", "MP");
        LANG_ISO_TO_WU_CODES.put("MQ", "MR");
        LANG_ISO_TO_WU_CODES.put("MR", "MT");
        LANG_ISO_TO_WU_CODES.put("MU", "MA");
        LANG_ISO_TO_WU_CODES.put("YT", "YT");
        LANG_ISO_TO_WU_CODES.put("MX", "MX");
        LANG_ISO_TO_WU_CODES.put("FM", "US_FM");
        LANG_ISO_TO_WU_CODES.put("MD", "M1");
        LANG_ISO_TO_WU_CODES.put("MC", "M3");
        LANG_ISO_TO_WU_CODES.put("MN", "MO");
        LANG_ISO_TO_WU_CODES.put("MS", "M2");
        LANG_ISO_TO_WU_CODES.put("MA", "MC");
        LANG_ISO_TO_WU_CODES.put("MZ", "MZ");
        LANG_ISO_TO_WU_CODES.put("MM", "BM");
        LANG_ISO_TO_WU_CODES.put("NA", "NM");
        LANG_ISO_TO_WU_CODES.put("NR", "NW");
        LANG_ISO_TO_WU_CODES.put("NP", "NP");
        LANG_ISO_TO_WU_CODES.put("NL", "NL");
        LANG_ISO_TO_WU_CODES.put("AN", "AN");
        LANG_ISO_TO_WU_CODES.put("NZ", "NZ");
        LANG_ISO_TO_WU_CODES.put("NC", "NC");
        LANG_ISO_TO_WU_CODES.put("NI", "NK");
        LANG_ISO_TO_WU_CODES.put("NE", "NR");
        LANG_ISO_TO_WU_CODES.put("NG", "NI");
        LANG_ISO_TO_WU_CODES.put("NU", "N1");
        LANG_ISO_TO_WU_CODES.put("NF", "XX_NF");
        LANG_ISO_TO_WU_CODES.put("MP", "US_MP");
        LANG_ISO_TO_WU_CODES.put("NO", "NO");
        LANG_ISO_TO_WU_CODES.put("OM", "OM");

        // Column 3 of the table
        LANG_ISO_TO_WU_CODES.put("PK", "PK");
        LANG_ISO_TO_WU_CODES.put("PW", "PW");
        LANG_ISO_TO_WU_CODES.put("PS", "PS");
        LANG_ISO_TO_WU_CODES.put("PA", "PM");
        LANG_ISO_TO_WU_CODES.put("PG", "NG");
        LANG_ISO_TO_WU_CODES.put("PY", "PY");
        LANG_ISO_TO_WU_CODES.put("PE", "PR");
        LANG_ISO_TO_WU_CODES.put("PH", "PH");
        LANG_ISO_TO_WU_CODES.put("PN", "P2");
        LANG_ISO_TO_WU_CODES.put("PL", "PL");
        LANG_ISO_TO_WU_CODES.put("PT", "PO");
        LANG_ISO_TO_WU_CODES.put("PR", "PR");
        LANG_ISO_TO_WU_CODES.put("QA", "QT");
        LANG_ISO_TO_WU_CODES.put("RE", "RE");
        LANG_ISO_TO_WU_CODES.put("RO", "RO");
        LANG_ISO_TO_WU_CODES.put("RU", "RS");
        LANG_ISO_TO_WU_CODES.put("RW", "RW");
        LANG_ISO_TO_WU_CODES.put("SH", "HE");
        LANG_ISO_TO_WU_CODES.put("KN", "K1");
        LANG_ISO_TO_WU_CODES.put("LC", "LC");
        LANG_ISO_TO_WU_CODES.put("PM", "P1");
        LANG_ISO_TO_WU_CODES.put("VC", "VC");
        LANG_ISO_TO_WU_CODES.put("WS", "ZM");
        LANG_ISO_TO_WU_CODES.put("SM", "SM");
        LANG_ISO_TO_WU_CODES.put("ST", "TP");
        LANG_ISO_TO_WU_CODES.put("SA", "SD");
        LANG_ISO_TO_WU_CODES.put("SN", "SG");
        LANG_ISO_TO_WU_CODES.put("SC", "SC");
        LANG_ISO_TO_WU_CODES.put("SL", "SL");
        LANG_ISO_TO_WU_CODES.put("SG", "SR");
        LANG_ISO_TO_WU_CODES.put("SK", "S1");
        LANG_ISO_TO_WU_CODES.put("SI", "LJ");
        LANG_ISO_TO_WU_CODES.put("SB", "SO");
        LANG_ISO_TO_WU_CODES.put("SO", "SI");
        LANG_ISO_TO_WU_CODES.put("ZA", "ZA");
        LANG_ISO_TO_WU_CODES.put("GS", "GS");
        LANG_ISO_TO_WU_CODES.put("ES", "SP");
        LANG_ISO_TO_WU_CODES.put("LK", "SB");
        LANG_ISO_TO_WU_CODES.put("SD", "SU");
        LANG_ISO_TO_WU_CODES.put("SR", "SM");
        LANG_ISO_TO_WU_CODES.put("SJ", "SJ");
        LANG_ISO_TO_WU_CODES.put("SZ", "SV");
        LANG_ISO_TO_WU_CODES.put("SE", "SN");
        LANG_ISO_TO_WU_CODES.put("CH", "SW");
        LANG_ISO_TO_WU_CODES.put("SY", "SY");
        LANG_ISO_TO_WU_CODES.put("TW", "TW");
        LANG_ISO_TO_WU_CODES.put("TJ", "TJ");
        LANG_ISO_TO_WU_CODES.put("TZ", "TN");
        LANG_ISO_TO_WU_CODES.put("TH", "TH");
        LANG_ISO_TO_WU_CODES.put("TL", "EA");
        LANG_ISO_TO_WU_CODES.put("TG", "TG");
        LANG_ISO_TO_WU_CODES.put("TK", "TK");
        LANG_ISO_TO_WU_CODES.put("TO", "TO");
        LANG_ISO_TO_WU_CODES.put("TT", "TD");
        LANG_ISO_TO_WU_CODES.put("TN", "TS");
        LANG_ISO_TO_WU_CODES.put("TR", "TU");
        LANG_ISO_TO_WU_CODES.put("TM", "TM");
        LANG_ISO_TO_WU_CODES.put("TC", "TI");
        LANG_ISO_TO_WU_CODES.put("TV", "TV");
        LANG_ISO_TO_WU_CODES.put("TB", "TB");
        LANG_ISO_TO_WU_CODES.put("UG", "UG");
        LANG_ISO_TO_WU_CODES.put("UA", "UR");
        LANG_ISO_TO_WU_CODES.put("AE", "ER");
        LANG_ISO_TO_WU_CODES.put("GB", "UK");
        LANG_ISO_TO_WU_CODES.put("US", "US");
        LANG_ISO_TO_WU_CODES.put("UM", "US_UM");
        LANG_ISO_TO_WU_CODES.put("UY", "UY");
        LANG_ISO_TO_WU_CODES.put("UZ", "UZ");
        LANG_ISO_TO_WU_CODES.put("VU", "NH");
        LANG_ISO_TO_WU_CODES.put("VE", "VN");
        LANG_ISO_TO_WU_CODES.put("VN", "VS");
        LANG_ISO_TO_WU_CODES.put("VG", "VG");
        LANG_ISO_TO_WU_CODES.put("VI", "VI");
        LANG_ISO_TO_WU_CODES.put("WF", "FW");
        LANG_ISO_TO_WU_CODES.put("EH", "EH");
        LANG_ISO_TO_WU_CODES.put("YE", "YE");
        LANG_ISO_TO_WU_CODES.put("RS", "RB");
        LANG_ISO_TO_WU_CODES.put("KV", "KV");
        LANG_ISO_TO_WU_CODES.put("ME", "M4");
        LANG_ISO_TO_WU_CODES.put("ZM", "ZB");
        LANG_ISO_TO_WU_CODES.put("ZW", "ZW");
    }

    @Nullable
    private ServiceRegistration<?> discoveryService;

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

    @Override
    public void handleRemoval() {
        // removes the old registration service associated to the bridge, if existing
        if (discoveryService != null) {
            discoveryService.unregister();
        }
        super.handleRemoval();
    }

    /**
     * Request new weather data to the Weather Underground service
     *
     * @param features the list of features to be requested
     * @return the weather data object mapping the JSON response or null in case of error
     */
    protected @Nullable WeatherUndergroundJsonData getWeatherData(Set<String> features, String location,
            String language, LocaleProvider localeProvider, Gson gsonWeather) {
        WeatherUndergroundJsonData result = null;
        boolean resultOk = false;
        String error = null;
        String errorDetail = null;
        String statusDescr = null;

        try {

            String urlStr = URL_QUERY.replace("%APIKEY%", StringUtils.trimToEmpty(getApikey()));

            urlStr = urlStr.replace("%FEATURES%", String.join("/", features));

            String lang = StringUtils.trimToEmpty(language);
            if (lang.isEmpty()) {
                // If language is not set in the configuration, you try deducing it from the system language
                lang = LANG_ISO_TO_WU_CODES.get(localeProvider.getLocale().getLanguage().toUpperCase());
                logger.debug("Use language deduced from system locale {}: {}", localeProvider.getLocale().getLanguage(),
                        lang);
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
                response = HttpUtil.executeUrl("GET", urlStr, FETCH_TIMEOUT_MS);
                logger.debug("weatherData = {}", response);
            } catch (IllegalArgumentException e) {
                // catch Illegal character in path at index XX: http://api.wunderground.com/...
                error = "Error creating URI with location parameter: '" + location + "'";
                errorDetail = e.getMessage();
                statusDescr = "@text/offline.uri-error";
            }

            // Map the JSON response to an object
            result = gsonWeather.fromJson(response, WeatherUndergroundJsonData.class);
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
        if (!resultOk) {
            logger.debug("{}: {}", error, errorDetail);
            logger.debug("Could not initialize thing. Error: {}", statusDescr);
        }

        return resultOk ? result : null;
    }

    public void setDiscoveryService(ServiceRegistration<?> discoveryService) {
        this.discoveryService = discoveryService;
    }
}
