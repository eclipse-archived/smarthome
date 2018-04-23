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
package org.eclipse.smarthome.core.thing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.thing.i18n.ThingTypeI18nUtil;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.EventDescription;
import org.eclipse.smarthome.core.types.EventOption;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Implementation providing default system wide channel types
 *
 * @author Ivan Iliev - Initial Contribution
 * @author Chris Jackson - Added battery level
 * @author Dennis Nobel - Changed to {@link ChannelTypeProvider}
 * @author Markus Rathgeb - Make battery-low indication read-only
 * @author Moritz Kammerer - Added system trigger types
 * @author Christoph Weitkamp - Added support for translation
 * @author Laurent Garnier - Added weather system channels
 *
 */
@Component(immediate = true)
public class DefaultSystemChannelTypeProvider implements ChannelTypeProvider {

    /**
     * Signal strength default system wide {@link ChannelType}. Represents signal strength of a device as a number
     * with values 0, 1, 2, 3 or 4, 0 being worst strength and 4 being best strength.
     */
    public static final ChannelType SYSTEM_CHANNEL_SIGNAL_STRENGTH = new ChannelType(
            new ChannelTypeUID("system:signal-strength"), false, "Number", "Signal Strength", null, "QualityOfService",
            null,
            new StateDescription(BigDecimal.ZERO, new BigDecimal(4), BigDecimal.ONE, null, true,
                    Arrays.asList(new StateOption("0", "no signal"), new StateOption("1", "weak"),
                            new StateOption("2", "average"), new StateOption("3", "good"),
                            new StateOption("4", "excellent"))),
            null);

    /**
     * Low battery default system wide {@link ChannelType}. Represents a low battery warning with possible values
     * on/off.
     */
    public static final ChannelType SYSTEM_CHANNEL_LOW_BATTERY = new ChannelType(
            new ChannelTypeUID("system:low-battery"), false, "Switch", "Low Battery", null, "Battery", null,
            new StateDescription(null, null, null, null, true, null), null);

    /**
     * Battery level default system wide {@link ChannelType}. Represents the battery level as a percentage.
     */
    public static final ChannelType SYSTEM_CHANNEL_BATTERY_LEVEL = new ChannelType(
            new ChannelTypeUID("system:battery-level"), false, "Number", "Battery Level", null, "Battery", null,
            new StateDescription(BigDecimal.ZERO, new BigDecimal(100), BigDecimal.ONE, "%.0f %%", true, null), null);

    /**
     * System wide trigger {@link ChannelType} without event options.
     */
    public static final ChannelType SYSTEM_TRIGGER = new ChannelType(new ChannelTypeUID("system:trigger"), false, null,
            ChannelKind.TRIGGER, "Trigger", null, null, null, null, null, null);

    /**
     * System wide trigger {@link ChannelType} which triggers "PRESSED" and "RELEASED" events.
     */
    public static final ChannelType SYSTEM_RAWBUTTON = new ChannelType(new ChannelTypeUID("system:rawbutton"), false,
            null, ChannelKind.TRIGGER, "Raw button", null, null, null, null,
            new EventDescription(Arrays.asList(new EventOption(CommonTriggerEvents.PRESSED, null),
                    new EventOption(CommonTriggerEvents.RELEASED, null))),
            null);

    /**
     * System wide trigger {@link ChannelType} which triggers "SHORT_PRESSED", "DOUBLE_PRESSED" and "LONG_PRESSED"
     * events.
     */
    public static final ChannelType SYSTEM_BUTTON = new ChannelType(new ChannelTypeUID("system:button"), false, null,
            ChannelKind.TRIGGER, "Button", null, null, null, null,
            new EventDescription(Arrays.asList(new EventOption(CommonTriggerEvents.SHORT_PRESSED, null),
                    new EventOption(CommonTriggerEvents.DOUBLE_PRESSED, null),
                    new EventOption(CommonTriggerEvents.LONG_PRESSED, null))),
            null);

    /**
     * System wide trigger {@link ChannelType} which triggers "DIR1_PRESSED", "DIR1_RELEASED", "DIR2_PRESSED" and
     * "DIR2_RELEASED" events.
     */
    public static final ChannelType SYSTEM_RAWROCKER = new ChannelType(new ChannelTypeUID("system:rawrocker"), false,
            null, ChannelKind.TRIGGER, "Raw rocker button", null, null, null, null,
            new EventDescription(Arrays.asList(new EventOption(CommonTriggerEvents.DIR1_PRESSED, null),
                    new EventOption(CommonTriggerEvents.DIR1_RELEASED, null),
                    new EventOption(CommonTriggerEvents.DIR2_PRESSED, null),
                    new EventOption(CommonTriggerEvents.DIR2_RELEASED, null))),
            null);

    /**
     * Current temperature default system wide {@link ChannelType}. Represents the current temperature.
     */
    public static final ChannelType SYSTEM_CHANNEL_CURRENT_TEMPERATURE = new ChannelType(
            new ChannelTypeUID("system:current-temperature"), false, "Number:Temperature", "Current Temperature",
            "Current temperature", "Temperature", null,
            new StateDescription(null, null, null, "%.1f %unit%", true, null), null);

    /**
     * Minimum temperature default system wide {@link ChannelType}. Represents the minimum temperature.
     */
    public static final ChannelType SYSTEM_CHANNEL_MIN_TEMPERATURE = new ChannelType(
            new ChannelTypeUID("system:minimum-temperature"), false, "Number:Temperature", "Minimum Temperature",
            "Minimum temperature", "Temperature", null,
            new StateDescription(null, null, null, "%.1f %unit%", true, null), null);

    /**
     * Maximum temperature default system wide {@link ChannelType}. Represents the maximum temperature.
     */
    public static final ChannelType SYSTEM_CHANNEL_MAX_TEMPERATURE = new ChannelType(
            new ChannelTypeUID("system:maximum-temperature"), false, "Number:Temperature", "Maximum Temperature",
            "Maximum temperature", "Temperature", null,
            new StateDescription(null, null, null, "%.1f %unit%", true, null), null);

    /**
     * Feeling temperature default system wide {@link ChannelType}. Represents the feeling temperature.
     */
    public static final ChannelType SYSTEM_CHANNEL_FEELING_TEMPERATURE = new ChannelType(
            new ChannelTypeUID("system:feeling-temperature"), false, "Number:Temperature", "Feeling Temperature",
            "Feeling temperature", "Temperature", null,
            new StateDescription(null, null, null, "%.1f %unit%", true, null), null);

    /**
     * Dew point temperature default system wide {@link ChannelType}. Represents the dew point temperature.
     */
    public static final ChannelType SYSTEM_CHANNEL_DEW_POINT_TEMPERATURE = new ChannelType(
            new ChannelTypeUID("system:dew-point-temperature"), false, "Number:Temperature", "Dew Point Temperature",
            "Dew point temperature", "Temperature", null,
            new StateDescription(null, null, null, "%.1f %unit%", true, null), null);

    /**
     * Wind chill temperature default system wide {@link ChannelType}. Represents the wind chill temperature.
     */
    public static final ChannelType SYSTEM_CHANNEL_WIND_CHILL_TEMPERATURE = new ChannelType(
            new ChannelTypeUID("system:wind-chill-temperature"), false, "Number:Temperature", "Wind Chill Temperature",
            "Wind chill temperature", "Temperature", null,
            new StateDescription(null, null, null, "%.1f %unit%", true, null), null);

    /**
     * Heat index default system wide {@link ChannelType}. Represents the heat index.
     */
    public static final ChannelType SYSTEM_CHANNEL_HEAT_INDEX = new ChannelType(new ChannelTypeUID("system:heat-index"),
            false, "Number:Temperature", "Heat Index", "Heat index", "Temperature", null,
            new StateDescription(null, null, null, "%.1f %unit%", true, null), null);

    /**
     * Relative humidity default system wide {@link ChannelType}. Represents the relative humidity in %.
     */
    public static final ChannelType SYSTEM_CHANNEL_RELATIVE_HUMIDITY = new ChannelType(
            new ChannelTypeUID("system:relative-humidity"), false, "Number:Dimensionless", "Relative Humidity",
            "Relative humidity in %", "Humidity", null,
            new StateDescription(BigDecimal.ZERO, new BigDecimal(100), BigDecimal.ONE, "%.0f %unit%", true, null),
            null);

    /**
     * Barometric pressure default system wide {@link ChannelType}. Represents the barometric pressure.
     */
    public static final ChannelType SYSTEM_CHANNEL_BAROMETRIC_PRESSURE = new ChannelType(
            new ChannelTypeUID("system:barometric-pressure"), false, "Number:Pressure", "Barometric Pressure",
            "Barometric pressure", "Pressure", null, new StateDescription(null, null, null, "%.3f %unit%", true, null),
            null);

    /**
     * Absolute pressure default system wide {@link ChannelType}. Represents the absolute pressure.
     */
    public static final ChannelType SYSTEM_CHANNEL_ABSOLUTE_PRESSURE = new ChannelType(
            new ChannelTypeUID("system:absolute-pressure"), false, "Number:Pressure", "Absolute Pressure",
            "Absolute pressure", "Pressure", null, new StateDescription(null, null, null, "%.3f %unit%", true, null),
            null);

    /**
     * Wind speed default system wide {@link ChannelType}. Represents the wind speed.
     */
    public static final ChannelType SYSTEM_CHANNEL_WIND_SPEED = new ChannelType(new ChannelTypeUID("system:wind-speed"),
            false, "Number:Speed", "Wind Speed", "Wind speed", "Wind", null,
            new StateDescription(null, null, null, "%.1f %unit%", true, null), null);

    /**
     * Maximum wind speed default system wide {@link ChannelType}. Represents the maximum wind speed.
     */
    public static final ChannelType SYSTEM_CHANNEL_MAX_WIND_SPEED = new ChannelType(
            new ChannelTypeUID("system:maximum-wind-speed"), false, "Number:Speed", "Maximum Wind Speed",
            "Maximum wind speed", "Wind", null, new StateDescription(null, null, null, "%.1f %unit%", true, null),
            null);

    /**
     * Average wind speed default system wide {@link ChannelType}. Represents the Average wind speed.
     */
    public static final ChannelType SYSTEM_CHANNEL_AVG_WIND_SPEED = new ChannelType(
            new ChannelTypeUID("system:average-wind-speed"), false, "Number:Speed", "Average Wind Speed",
            "Average wind speed", "Wind", null, new StateDescription(null, null, null, "%.1f %unit%", true, null),
            null);

    /**
     * Wind gust speed default system wide {@link ChannelType}. Represents the wind gust speed.
     */
    public static final ChannelType SYSTEM_CHANNEL_WIND_GUST_SPEED = new ChannelType(
            new ChannelTypeUID("system:wind-gust-speed"), false, "Number:Speed", "Wind Gust Speed", "Wind gust speed",
            "Wind", null, new StateDescription(null, null, null, "%.1f %unit%", true, null), null);

    /**
     * Wind direction default system wide {@link ChannelType}. Represents the wind direction in degrees.
     */
    public static final ChannelType SYSTEM_CHANNEL_WIND_DIRECTION = new ChannelType(
            new ChannelTypeUID("system:wind-direction"), false, "Number:Angle", "Wind Direction",
            "Wind direction in degrees", "Wind", null,
            new StateDescription(BigDecimal.ZERO, new BigDecimal(360), BigDecimal.ONE, "%.0f %unit%", true, null),
            null);

    /**
     * Maximum wind direction default system wide {@link ChannelType}. Represents the maximum wind direction in degrees.
     */
    public static final ChannelType SYSTEM_CHANNEL_MAX_WIND_DIRECTION = new ChannelType(
            new ChannelTypeUID("system:maximum-wind-direction"), false, "Number:Angle", "Maximum Wind Direction",
            "Maximum wind direction in degrees", "Wind", null,
            new StateDescription(BigDecimal.ZERO, new BigDecimal(360), BigDecimal.ONE, "%.0f %unit%", true, null),
            null);

    /**
     * Average wind direction default system wide {@link ChannelType}. Represents the average wind direction in degrees.
     */
    public static final ChannelType SYSTEM_CHANNEL_AVG_WIND_DIRECTION = new ChannelType(
            new ChannelTypeUID("system:average-wind-direction"), false, "Number:Angle", "Average Wind Direction",
            "Average wind direction in degrees", "Wind", null,
            new StateDescription(BigDecimal.ZERO, new BigDecimal(360), BigDecimal.ONE, "%.0f %unit%", true, null),
            null);

    /**
     * Wind gust direction default system wide {@link ChannelType}. Represents the wind gust direction in degrees.
     */
    public static final ChannelType SYSTEM_CHANNEL_WIND_GUST_DIRECTION = new ChannelType(
            new ChannelTypeUID("system:wind-gust-direction"), false, "Number:Angle", "Wind Gust Direction",
            "Wind gust direction in degrees", "Wind", null,
            new StateDescription(BigDecimal.ZERO, new BigDecimal(360), BigDecimal.ONE, "%.0f %unit%", true, null),
            null);

    /**
     * Rainfall in hour default system wide {@link ChannelType}. Represents the rainfall in the hour.
     */
    public static final ChannelType SYSTEM_CHANNEL_RAIN_HOUR = new ChannelType(new ChannelTypeUID("system:rain-hour"),
            false, "Number:Length", "Rainfall Hour", "Rainfall in the hour", "Rain", null,
            new StateDescription(null, null, null, "%.2f %unit%", true, null), null);

    /**
     * Rainfall in day default system wide {@link ChannelType}. Represents the rainfall in the day.
     */
    public static final ChannelType SYSTEM_CHANNEL_RAIN_DAY = new ChannelType(new ChannelTypeUID("system:rain-day"),
            false, "Number:Length", "Rainfall Day", "Rainfall in the day", "Rain", null,
            new StateDescription(null, null, null, "%.2f %unit%", true, null), null);

    /**
     * Snowfall in hour default system wide {@link ChannelType}. Represents the snowfall in the hour.
     */
    public static final ChannelType SYSTEM_CHANNEL_SNOW_HOUR = new ChannelType(new ChannelTypeUID("system:snow-hour"),
            false, "Number:Length", "Snowfall Hour", "Snowfall in the hour", "Rain", null,
            new StateDescription(null, null, null, "%.2f %unit%", true, null), null);

    /**
     * Snowfall in day default system wide {@link ChannelType}. Represents the snowfall in the day.
     */
    public static final ChannelType SYSTEM_CHANNEL_SNOW_DAY = new ChannelType(new ChannelTypeUID("system:snow-day"),
            false, "Number:Length", "Snowfall Day", "Snowfall in the day", "Rain", null,
            new StateDescription(null, null, null, "%.2f %unit%", true, null), null);

    /**
     * Probability of precipitation default system wide {@link ChannelType}. Represents the probability of precipitation
     * in %.
     */
    public static final ChannelType SYSTEM_CHANNEL_PROBA_PRECIPITATION = new ChannelType(
            new ChannelTypeUID("system:proba-precipitation"), false, "Number:Dimensionless",
            "Probability of precipitation", "Probability of precipitation in %", "Rain", null,
            new StateDescription(BigDecimal.ZERO, new BigDecimal(100), BigDecimal.ONE, "%.0f %unit%", true, null),
            null);

    /**
     * Current weather conditions system wide {@link ChannelType}. Represents the current weather conditions.
     */
    public static final ChannelType SYSTEM_CHANNEL_CURRENT_WEATHER_CONDITIONS = new ChannelType(
            new ChannelTypeUID("system:current-weather-conditions"), false, "String", "Current Weather Conditions",
            "Current weather conditions", null, null, new StateDescription(null, null, null, "%s", true, null), null);

    /**
     * Forecast weather conditions system wide {@link ChannelType}. Represents the forecast weather conditions.
     */
    public static final ChannelType SYSTEM_CHANNEL_FORECAST_WEATHER_CONDITIONS = new ChannelType(
            new ChannelTypeUID("system:forecast-weather-conditions"), false, "String", "Forecast Weather Conditions",
            "Forecast weather conditions", null, null, new StateDescription(null, null, null, "%s", true, null), null);

    private static class LocalizedChannelTypeKey {
        public final String locale;
        public final UID uid;

        public LocalizedChannelTypeKey(UID uid, String locale) {
            this.uid = uid;
            this.locale = locale;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            LocalizedChannelTypeKey other = (LocalizedChannelTypeKey) obj;
            if (locale == null) {
                if (other.locale != null) {
                    return false;
                }
            } else if (!locale.equals(other.locale)) {
                return false;
            }
            if (uid == null) {
                if (other.uid != null) {
                    return false;
                }
            } else if (!uid.equals(other.uid)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((locale == null) ? 0 : locale.hashCode());
            result = prime * result + ((uid == null) ? 0 : uid.hashCode());
            return result;
        }

    }

    private final Collection<ChannelGroupType> channelGroupTypes;
    private final Collection<ChannelType> channelTypes;

    private final Map<LocalizedChannelTypeKey, ChannelType> localizedChannelTypeCache = new ConcurrentHashMap<>();

    private ThingTypeI18nUtil thingTypeI18nUtil;

    public DefaultSystemChannelTypeProvider() {
        channelGroupTypes = Collections.emptyList();
        channelTypes = Collections.unmodifiableCollection(Arrays.asList(new ChannelType[] {
                SYSTEM_CHANNEL_SIGNAL_STRENGTH, SYSTEM_CHANNEL_LOW_BATTERY, SYSTEM_CHANNEL_BATTERY_LEVEL,
                SYSTEM_TRIGGER, SYSTEM_RAWBUTTON, SYSTEM_BUTTON, SYSTEM_RAWROCKER, SYSTEM_CHANNEL_CURRENT_TEMPERATURE,
                SYSTEM_CHANNEL_MIN_TEMPERATURE, SYSTEM_CHANNEL_MAX_TEMPERATURE, SYSTEM_CHANNEL_FEELING_TEMPERATURE,
                SYSTEM_CHANNEL_DEW_POINT_TEMPERATURE, SYSTEM_CHANNEL_WIND_CHILL_TEMPERATURE, SYSTEM_CHANNEL_HEAT_INDEX,
                SYSTEM_CHANNEL_RELATIVE_HUMIDITY, SYSTEM_CHANNEL_BAROMETRIC_PRESSURE, SYSTEM_CHANNEL_ABSOLUTE_PRESSURE,
                SYSTEM_CHANNEL_WIND_SPEED, SYSTEM_CHANNEL_MAX_WIND_SPEED, SYSTEM_CHANNEL_AVG_WIND_SPEED,
                SYSTEM_CHANNEL_WIND_GUST_SPEED, SYSTEM_CHANNEL_WIND_DIRECTION, SYSTEM_CHANNEL_MAX_WIND_DIRECTION,
                SYSTEM_CHANNEL_AVG_WIND_DIRECTION, SYSTEM_CHANNEL_WIND_GUST_DIRECTION, SYSTEM_CHANNEL_RAIN_HOUR,
                SYSTEM_CHANNEL_RAIN_DAY, SYSTEM_CHANNEL_SNOW_HOUR, SYSTEM_CHANNEL_SNOW_DAY,
                SYSTEM_CHANNEL_PROBA_PRECIPITATION, SYSTEM_CHANNEL_CURRENT_WEATHER_CONDITIONS,
                SYSTEM_CHANNEL_FORECAST_WEATHER_CONDITIONS }));
    }

    @Override
    public Collection<ChannelType> getChannelTypes(Locale locale) {
        final List<ChannelType> allChannelTypes = new ArrayList<>(10);
        final Bundle bundle = FrameworkUtil.getBundle(DefaultSystemChannelTypeProvider.class);

        for (final ChannelType channelType : channelTypes) {
            allChannelTypes.add(createLocalizedChannelType(bundle, channelType, locale));
        }

        return allChannelTypes;
    }

    @Override
    public ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale) {
        final Bundle bundle = FrameworkUtil.getBundle(DefaultSystemChannelTypeProvider.class);

        if (channelTypeUID.equals(SYSTEM_CHANNEL_SIGNAL_STRENGTH.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_SIGNAL_STRENGTH, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_LOW_BATTERY.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_LOW_BATTERY, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_BATTERY_LEVEL.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_BATTERY_LEVEL, locale);
        } else if (channelTypeUID.equals(SYSTEM_TRIGGER.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_TRIGGER, locale);
        } else if (channelTypeUID.equals(SYSTEM_RAWBUTTON.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_RAWBUTTON, locale);
        } else if (channelTypeUID.equals(SYSTEM_BUTTON.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_BUTTON, locale);
        } else if (channelTypeUID.equals(SYSTEM_RAWROCKER.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_RAWROCKER, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_CURRENT_TEMPERATURE.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_CURRENT_TEMPERATURE, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_MIN_TEMPERATURE.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_MIN_TEMPERATURE, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_MAX_TEMPERATURE.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_MAX_TEMPERATURE, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_FEELING_TEMPERATURE.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_FEELING_TEMPERATURE, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_DEW_POINT_TEMPERATURE.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_DEW_POINT_TEMPERATURE, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_WIND_CHILL_TEMPERATURE.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_WIND_CHILL_TEMPERATURE, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_HEAT_INDEX.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_HEAT_INDEX, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_RELATIVE_HUMIDITY.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_RELATIVE_HUMIDITY, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_BAROMETRIC_PRESSURE.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_BAROMETRIC_PRESSURE, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_ABSOLUTE_PRESSURE.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_ABSOLUTE_PRESSURE, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_WIND_SPEED.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_WIND_SPEED, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_MAX_WIND_SPEED.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_MAX_WIND_SPEED, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_AVG_WIND_SPEED.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_AVG_WIND_SPEED, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_WIND_GUST_SPEED.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_WIND_GUST_SPEED, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_WIND_DIRECTION.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_WIND_DIRECTION, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_MAX_WIND_DIRECTION.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_MAX_WIND_DIRECTION, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_AVG_WIND_DIRECTION.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_AVG_WIND_DIRECTION, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_WIND_GUST_DIRECTION.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_WIND_GUST_DIRECTION, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_RAIN_HOUR.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_RAIN_HOUR, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_RAIN_DAY.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_RAIN_DAY, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_SNOW_HOUR.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_SNOW_HOUR, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_SNOW_DAY.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_SNOW_DAY, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_PROBA_PRECIPITATION.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_PROBA_PRECIPITATION, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_CURRENT_WEATHER_CONDITIONS.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_CURRENT_WEATHER_CONDITIONS, locale);
        } else if (channelTypeUID.equals(SYSTEM_CHANNEL_FORECAST_WEATHER_CONDITIONS.getUID())) {
            return createLocalizedChannelType(bundle, SYSTEM_CHANNEL_FORECAST_WEATHER_CONDITIONS, locale);
        }
        return null;
    }

    @Override
    public ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID, Locale locale) {
        return null;
    }

    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(Locale locale) {
        return channelGroupTypes;
    }

    @Reference
    public void setTranslationProvider(TranslationProvider i18nProvider) {
        thingTypeI18nUtil = new ThingTypeI18nUtil(i18nProvider);
    }

    public void unsetTranslationProvider(TranslationProvider i18nProvider) {
        thingTypeI18nUtil = null;
    }

    private ChannelType createLocalizedChannelType(Bundle bundle, ChannelType channelType, Locale locale) {
        LocalizedChannelTypeKey localizedChannelTypeKey = getLocalizedChannelTypeKey(channelType.getUID(), locale);

        ChannelType cachedEntry = localizedChannelTypeCache.get(localizedChannelTypeKey);
        if (cachedEntry != null) {
            return cachedEntry;
        }

        if (thingTypeI18nUtil != null) {
            ChannelTypeUID channelTypeUID = channelType.getUID();

            String label = thingTypeI18nUtil.getChannelLabel(bundle, channelTypeUID, channelType.getLabel(), locale);
            String description = thingTypeI18nUtil.getChannelDescription(bundle, channelTypeUID,
                    channelType.getDescription(), locale);

            StateDescription state = createLocalizedChannelState(bundle, channelType, channelTypeUID, locale);

            ChannelType localizedChannelType = new ChannelType(channelTypeUID, channelType.isAdvanced(),
                    channelType.getItemType(), channelType.getKind(), label, description, channelType.getCategory(),
                    channelType.getTags(), state, channelType.getEvent(), channelType.getConfigDescriptionURI());

            localizedChannelTypeCache.put(localizedChannelTypeKey, localizedChannelType);

            return localizedChannelType;
        }

        return channelType;
    }

    private StateDescription createLocalizedChannelState(Bundle bundle, ChannelType channelType,
            ChannelTypeUID channelTypeUID, Locale locale) {
        StateDescription state = channelType.getState();

        if (state != null) {
            String pattern = thingTypeI18nUtil.getChannelStatePattern(bundle, channelTypeUID, state.getPattern(),
                    locale);

            List<StateOption> localizedOptions = new ArrayList<>();
            List<StateOption> options = state.getOptions();
            for (StateOption stateOption : options) {
                String optionLabel = thingTypeI18nUtil.getChannelStateOption(bundle, channelTypeUID,
                        stateOption.getValue(), stateOption.getLabel(), locale);
                localizedOptions.add(new StateOption(stateOption.getValue(), optionLabel));
            }

            return new StateDescription(state.getMinimum(), state.getMaximum(), state.getStep(), pattern,
                    state.isReadOnly(), localizedOptions);
        }
        return null;
    }

    private LocalizedChannelTypeKey getLocalizedChannelTypeKey(UID uid, Locale locale) {
        String localeString = locale != null ? locale.toLanguageTag() : null;
        LocalizedChannelTypeKey localizedChannelTypeKey = new LocalizedChannelTypeKey(uid,
                locale != null ? localeString : null);
        return localizedChannelTypeKey;
    }
}
