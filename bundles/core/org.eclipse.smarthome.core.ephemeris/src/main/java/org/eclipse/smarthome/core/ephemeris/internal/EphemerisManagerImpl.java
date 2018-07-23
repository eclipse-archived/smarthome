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
package org.eclipse.smarthome.core.ephemeris.internal;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigOptionProvider;
import org.eclipse.smarthome.config.core.ConfigurableService;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.core.ephemeris.EphemerisManager;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jollyday.Holiday;
import de.jollyday.HolidayManager;
import de.jollyday.ManagerParameter;
import de.jollyday.ManagerParameters;

/**
 * This service provides functionality around ephemeris services and is the central service to be used directly by
 * others.
 *
 * @author Gaël L'hopital - Initial contribution and API
 */
@Component(immediate = true, configurationPid = "org.eclipse.smarthome.ephemeris", property = {
        Constants.SERVICE_PID + "=org.eclipse.smarthome.ephemeris",
        ConfigurableService.SERVICE_PROPERTY_CATEGORY + "=system",
        ConfigurableService.SERVICE_PROPERTY_LABEL + "=Ephemeris",
        ConfigurableService.SERVICE_PROPERTY_DESCRIPTION_URI + "=" + EphemerisManagerImpl.CONFIG_URI })
public class EphemerisManagerImpl implements EphemerisManager, ConfigOptionProvider {
    private final Logger logger = LoggerFactory.getLogger(EphemerisManagerImpl.class);

    // constants for the configuration properties
    protected static final String CONFIG_URI = "system:ephemeris";
    private static final String CONFIG_DAYSET_PREFIX = "dayset-";
    private static final String CONFIG_DAYSET_WEEKEND = "weekend";
    private static final String CONFIG_COUNTRY = "country";
    private static final String CONFIG_REGION = "region";
    private static final String CONFIG_CITY = "city";

    private final Map<String, Set<DayOfWeek>> daysets = new HashMap<>();
    private final Map<Object, HolidayManager> holidayManagers = new HashMap<>();

    private @Nullable String country;
    private final List<String> countryParameters = new ArrayList<>();

    private @Nullable LocaleProvider localeProvider;

    @Activate
    protected void activate(Map<String, Object> config) {
        modified(config);
    }

    @Modified
    protected void modified(Map<String, Object> config) {

        config.keySet().stream().filter(k -> k.startsWith(CONFIG_DAYSET_PREFIX)).forEach(k -> {
            String[] setDefinition = config.get(k).toString().split(",");
            String[] setNameParts = k.split("-");
            if (setDefinition.length > 0 && setNameParts.length > 1) {
                Set<DayOfWeek> dayset = new HashSet<>();
                Stream.of(setDefinition).forEach(day -> {
                    dayset.add(DayOfWeek.valueOf(day.toUpperCase()));
                });
                daysets.put(setNameParts[1], dayset);
            } else {
                logger.warn("Erroneus dayset definition {} : {}", k, config.get(k));
            }
        });

        country = getValueAsString(config, CONFIG_COUNTRY);
        if (country == null && localeProvider != null) {
            country = localeProvider.getLocale().getCountry();
            logger.debug("Using system default country '{}' ", country);
        }

        if (config.containsKey(CONFIG_REGION)) {
            countryParameters.add(getValueAsString(config, CONFIG_REGION));
        }

        if (config.containsKey(CONFIG_CITY)) {
            countryParameters.add(getValueAsString(config, CONFIG_CITY));
        }
    }

    private String getValueAsString(Map<String, Object> config, String key) {
        return config.containsKey(key) ? config.get(key).toString() : null;
    }

    @Reference
    protected void setLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    protected void unsetLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = null;
    }

    @Override
    public Collection<ParameterOption> getParameterOptions(URI uri, String param, Locale locale) {
        if (CONFIG_URI.equals(uri.toString())) {
            if (localeProvider != null) {
                Locale nullSafeLocale = locale == null ? localeProvider.getLocale() : locale;
                List<ParameterOption> options = new ArrayList<>();
                for (DayOfWeek day : DayOfWeek.values()) {
                    ParameterOption option = new ParameterOption(day.name(),
                            day.getDisplayName(TextStyle.FULL, nullSafeLocale));
                    options.add(option);
                }
                return options;
            } else {
                logger.warn("No locale provider available, could not define week days list");
            }
        }
        return null;
    }

    private boolean isBankHoliday(ZonedDateTime date) {
        Holiday holiday = getHoliday(date);
        return holiday != null;
    }

    private HolidayManager getHolidayManager(Object managerKey) {
        if (!holidayManagers.containsKey(managerKey)) {
            ManagerParameter parameters;
            if (managerKey.getClass() == String.class) {
                parameters = ManagerParameters.create((String) managerKey);
            } else {
                parameters = ManagerParameters.create((URL) managerKey);
            }
            HolidayManager holidayManager = HolidayManager.getInstance(parameters);
            holidayManagers.put(managerKey, holidayManager);
        }
        return holidayManagers.get(managerKey);
    }

    @Override
    public boolean isBankHoliday(int offset) {
        return isBankHoliday(ZonedDateTime.now().plusDays(offset));
    }

    @Override
    public boolean isWeekEnd(int offset) {
        return isWeekEnd(ZonedDateTime.now().plusDays(offset));
    }

    private boolean isWeekEnd(ZonedDateTime date) {
        return isInDayset(CONFIG_DAYSET_WEEKEND, date);
    }

    @Override
    public boolean isInDayset(String daysetName, int offset) {
        return isInDayset(daysetName, ZonedDateTime.now().plusDays(offset));
    }

    private boolean isInDayset(String daysetName, ZonedDateTime date) {
        DayOfWeek dow = date.getDayOfWeek();
        Set<DayOfWeek> dayset = daysets.get(daysetName);
        if (dayset != null) {
            return dayset.contains(dow);
        } else {
            logger.warn("This dayset does is not configured : {}", daysetName);
            return false;
        }
    }

    @Override
    public @Nullable String getBankHolidayName(int offset) {
        return getBankHolidayName(ZonedDateTime.now().plusDays(offset));
    }

    private @Nullable String getBankHolidayName(ZonedDateTime date) {
        Holiday holiday = getHoliday(date);
        if (holiday != null && localeProvider != null) {
            return holiday.getDescription(localeProvider.getLocale());
        }
        return null;
    }

    private @Nullable Holiday getHoliday(ZonedDateTime date) {
        if (country != null) {
            HolidayManager manager = getHolidayManager(country);
            LocalDate localDate = date.toLocalDate();

            Set<Holiday> holidays = manager.getHolidays(localDate, localDate, countryParameters.toArray(new String[0]));
            if (!holidays.isEmpty()) {
                return holidays.iterator().next();
            }
        } else {
            logger.warn("No country defined by system configuration, neither by service configuration");
        }
        return null;
    }

    private @Nullable String getHolidayUserFile(ZonedDateTime date, String filename) throws MalformedURLException {
        URL url = new URL("file:" + filename);
        Set<Holiday> days = getHolidayManager(url).getHolidays(date.toLocalDate(), date.toLocalDate());
        if (!days.isEmpty()) {
            return days.iterator().next().getPropertiesKey();
        } else {
            return null;
        }
    }

    @Override
    public @Nullable String getHolidayUserFile(int offset, String filename) throws MalformedURLException {
        return getHolidayUserFile(ZonedDateTime.now().plusDays(offset), filename);
    }

}
