/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.weatherunderground.internal.json;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

/**
 * The {@link WeatherUndergroundJsonUtils} class contains utilities methods.
 *
 * @author Laurent Garnier - Initial contribution
 */
public class WeatherUndergroundJsonUtils {

    /**
     * Returns the field value from the object data, nested fields are possible.
     * If the fieldName is for example current#humidity, the methods getCurrent().getHumidity() are called.
     */
    public static Object getValue(String channelId, Object data) throws Exception {
        String[] fields = StringUtils.split(channelId, "#");
        return getValue(data, fields, 0);
    }

    /**
     * Iterates through the fields and returns the getter value.
     */
    @SuppressWarnings("all")
    private static Object getValue(Object data, String[] fields, int index) throws Exception {
        if (data == null) {
            return null;
        }
        String fieldName = fields[index];
        Method method = data.getClass().getMethod(toGetterString(fieldName), null);
        Object result = method.invoke(data, (Object[]) null);
        if (index + 1 < fields.length) {
            result = getValue(result, fields, index + 1);
        }
        return result;
    }

    /**
     * Converts the string to a getter method.
     */
    private static String toGetterString(String str) {
        StringBuilder sb = new StringBuilder();
        sb.append("get");
        sb.append(Character.toTitleCase(str.charAt(0)));
        sb.append(str.substring(1));
        return sb.toString();
    }

    /**
     * Convert a string representing an Epoch value into a Calendar object
     *
     * @param value the Epoch value as a string
     *
     * @return the Calendar object representing the date and time of the Epoch
     *         or null in case of conversion error
     */
    public static Calendar convertToCalendar(String value) {
        Calendar result = null;
        if (isValid(value)) {
            try {
                result = Calendar.getInstance();
                result.setTimeInMillis(Long.valueOf(value) * 1000);
            } catch (NumberFormatException e) {
                LoggerFactory.getLogger(WeatherUndergroundJsonUtils.class).debug("Cannot convert {} to Calendar",
                        value);
                result = null;
            }
        }
        return result;
    }

    /**
     * Convert a string representing an integer value into an Integer object
     *
     * @param value the integer value as a string
     *
     * @return the Integer object representing the value or null in case of conversion error
     */
    public static Integer convertToInteger(String value) {
        Integer result = null;
        if (isValid(value)) {
            try {
                result = Integer.valueOf(value.trim());
            } catch (NumberFormatException e) {
                LoggerFactory.getLogger(WeatherUndergroundJsonUtils.class).debug("Cannot convert {} to Integer", value);
                result = null;
            }
        }
        return result;
    }

    /**
     * Convert a string representing a decimal value into a BigDecimal object
     *
     * @param value the decimal value as a string
     *
     * @return the BigDecimal object representing the value or null in case of conversion error
     */
    public static BigDecimal convertToBigDecimal(String value) {
        BigDecimal result = null;
        if (isValid(value)) {
            result = new BigDecimal(value.trim());
        }
        return result;
    }

    private static boolean isValid(String value) {
        return (value != null) && !value.isEmpty() && !value.equalsIgnoreCase("N/A") && !value.equalsIgnoreCase("NA")
                && !value.equals("-") && !value.equals("--");
    }

    /**
     * Convert a string representing a URL into a URL object
     *
     * @param url the URL as a string
     *
     * @return the URL object representing the URL or null in case of invalid URL
     */
    public static URL getValidUrl(String url) {
        URL validUrl = null;
        if (StringUtils.isNotBlank(url)) {
            try {
                validUrl = new URL(url);
            } catch (MalformedURLException e) {
                validUrl = null;
            }
        }
        return validUrl;
    }
}
