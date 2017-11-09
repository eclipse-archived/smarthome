/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal.i18n;

import java.text.MessageFormat;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TimeZone;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.LocationProvider;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.i18n.UnitProvider;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.types.Dimension;
import org.eclipse.smarthome.core.types.ESHUnits;
import org.eclipse.smarthome.core.types.MeasurementSystem;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.Units;

/**
 * The {@link I18nProviderImpl} is a concrete implementation of the {@link TranslationProvider}, {@link LocaleProvider},
 * and {@link LocationProvider} service interfaces.
 * *
 * <p>
 * This implementation uses the i18n mechanism of Java ({@link ResourceBundle}) to translate a given key into text. The
 * resources must be placed under the specific directory {@link LanguageResourceBundleManager#RESOURCE_DIRECTORY} within
 * the certain modules. Each module is tracked in the platform by using the {@link ResourceBundleTracker} and managed by
 * using one certain {@link LanguageResourceBundleManager} which is responsible for the translation.
 * <p>
 * <p>
 * It reads a user defined configuration to set a locale and a location for this installation.
 *
 * @author Michael Grammling - Initial Contribution of TranslationProvider
 * @author Thomas Höfer - Added getText operation with arguments
 * @author Markus Rathgeb - Initial contribution and API of LocaleProvider
 * @author Stefan Triller - Initial contribution and API of LocationProvider
 * @author Erdoan Hadzhiyusein - Added time zone
 *
 */

@Component(immediate = true, configurationPid = "org.eclipse.smarthome.core.i18nprovider", property = {
        "service.pid=org.eclipse.smarthome.core.i18nprovider", "service.config.description.uri:String=system:i18n",
        "service.config.label:String=Regional Settings", "service.config.category:String=system" })
public class I18nProviderImpl
        implements TranslationProvider, LocaleProvider, LocationProvider, TimeZoneProvider, UnitProvider {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // LocaleProvider
    static final String LANGUAGE = "language";
    static final String SCRIPT = "script";
    static final String REGION = "region";
    static final String VARIANT = "variant";
    private Locale locale;

    // TranslationProvider
    private ResourceBundleTracker resourceBundleTracker;

    // LocationProvider
    static final String LOCATION = "location";
    private PointType location;

    // TimeZoneProvider
    static final String TIMEZONE = "timezone";
    private ZoneId timeZone;

    // UnitProvider
    private static final String MEASUREMENT_SYSTEM = "measurementSystem";
    private MeasurementSystem measurementSystem;
    private Map<Dimension, Map<MeasurementSystem, Unit<?>>> dimensionMap;

    @Activate
    @SuppressWarnings("unchecked")
    protected void activate(ComponentContext componentContext) {
        initDimensionMap();
        modified((Map<String, Object>) componentContext.getProperties());

        this.resourceBundleTracker = new ResourceBundleTracker(componentContext.getBundleContext(), this);
        this.resourceBundleTracker.open();
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        this.resourceBundleTracker.close();
    }

    @Modified
    protected synchronized void modified(Map<String, Object> config) {
        final String language = toStringOrNull(config.get(LANGUAGE));
        final String script = toStringOrNull(config.get(SCRIPT));
        final String region = toStringOrNull(config.get(REGION));
        final String variant = toStringOrNull(config.get(VARIANT));
        final String location = toStringOrNull(config.get(LOCATION));
        final String zoneId = toStringOrNull(config.get(TIMEZONE));
        final String measurementSystem = toStringOrNull(config.get(MEASUREMENT_SYSTEM));

        setTimeZone(zoneId);
        setLocation(location);
        setLocale(language, script, region, variant);
        setMeasurementSystem(measurementSystem);

        logger.info("Locale set to {}, Location set to {}, Time zone set to {}", locale, this.location, this.timeZone);
    }

    private void setMeasurementSystem(String measurementSystem) {
        if (StringUtils.isBlank(measurementSystem)) {
            this.measurementSystem = null;
            return;
        }

        try {
            this.measurementSystem = MeasurementSystem.valueOf(measurementSystem);
        } catch (RuntimeException e) {
            this.measurementSystem = null;
        }
    }

    private void setLocale(String language, String script, String region, String variant) {
        if (StringUtils.isEmpty(language)) {
            // at least the language must be defined otherwise the system default locale is used
            logger.debug("No language set, falling back to the default locale");
            locale = null;
            return;
        }

        final Locale.Builder builder = new Locale.Builder();
        try {
            builder.setLanguage(language);
        } catch (final RuntimeException ex) {
            logger.warn("Language ({}) is invalid. Cannot create locale, keep old one.", language, ex);
            return;
        }

        try {
            builder.setScript(script);
        } catch (final RuntimeException ex) {
            logger.warn("Script ({}) is invalid. Skip it.", script, ex);
            return;
        }

        try {
            builder.setRegion(region);
        } catch (final RuntimeException ex) {
            logger.warn("Region ({}) is invalid. Skip it.", region, ex);
            return;
        }

        try {
            builder.setVariant(variant);
        } catch (final RuntimeException ex) {
            logger.warn("Variant ({}) is invalid. Skip it.", variant, ex);
            return;
        }

        locale = builder.build();
    }

    private String toStringOrNull(Object value) {
        return value == null ? null : value.toString();
    }

    private void setLocation(final String location) {
        if (location != null) {
            try {
                this.location = PointType.valueOf(location);
            } catch (IllegalArgumentException e) {
                // preserve old location or null if none was set before
                logger.warn("Could not set new location, keeping old one: ", location, e.getMessage());
            }
        }
    }

    private void setTimeZone(final String zoneId) {
        if (StringUtils.isBlank(zoneId)) {
            timeZone = TimeZone.getDefault().toZoneId();
            logger.debug("No time zone set, falling back to the default time zone '{}'.", timeZone.toString());
        } else {
            try {
                timeZone = ZoneId.of(zoneId);
            } catch (DateTimeException e) {
                timeZone = TimeZone.getDefault().toZoneId();
                logger.warn("Error setting time zone '{}', falling back to the default time zone '{}': {}", zoneId,
                        timeZone.toString(), e.getMessage());
            }
        }
    }

    @Override
    public PointType getLocation() {
        return location;
    }

    @Override
    public ZoneId getTimeZone() {
        return this.timeZone;
    }

    @Override
    public Locale getLocale() {
        if (locale == null) {
            return Locale.getDefault();
        }
        return locale;
    }

    @Override
    public String getText(Bundle bundle, String key, String defaultText, Locale locale) {
        LanguageResourceBundleManager languageResource = this.resourceBundleTracker.getLanguageResource(bundle);

        if (languageResource != null) {
            String text = languageResource.getText(key, locale);
            if (text != null) {
                return text;
            }
        }

        return defaultText;
    }

    @Override
    public String getText(Bundle bundle, String key, String defaultText, Locale locale, Object... arguments) {
        String text = getText(bundle, key, defaultText, locale);

        if (text != null) {
            return MessageFormat.format(text, arguments);
        }

        return text;
    }

    @Override
    public @NonNull Unit<?> getUnit(Dimension dimension) {
        return dimensionMap.get(dimension).get(getMeasurementSystem());
    }

    @Override
    public @NonNull MeasurementSystem getMeasurementSystem() {
        if (measurementSystem != null) {
            return measurementSystem;
        }

        // Only US and Liberia use the Imperial System.
        if (Locale.US.equals(locale) || Locale.forLanguageTag("en-LR").equals(locale)) {
            return MeasurementSystem.US;
        }
        return MeasurementSystem.SI;
    }

    private void initDimensionMap() {
        dimensionMap = new HashMap<>();

        Map<MeasurementSystem, Unit<?>> temperatureMap = new HashMap<>();
        temperatureMap.put(MeasurementSystem.SI, Units.CELSIUS);
        temperatureMap.put(MeasurementSystem.US, ESHUnits.FAHRENHEIT);
        dimensionMap.put(Dimension.TEMPERATURE, temperatureMap);

        Map<MeasurementSystem, Unit<?>> pressureMap = new HashMap<>();
        pressureMap.put(MeasurementSystem.SI, ESHUnits.HECTO_PASCAL);
        pressureMap.put(MeasurementSystem.US, ESHUnits.INCH_OF_MERCURY);
        dimensionMap.put(Dimension.PRESSURE, pressureMap);

        Map<MeasurementSystem, Unit<?>> speedMap = new HashMap<>();
        speedMap.put(MeasurementSystem.SI, Units.KILOMETRE_PER_HOUR);
        speedMap.put(MeasurementSystem.US, ESHUnits.MILES_PER_HOUR);
        dimensionMap.put(Dimension.SPEED, speedMap);

        Map<MeasurementSystem, Unit<?>> lengthMap = new HashMap<>();
        lengthMap.put(MeasurementSystem.SI, Units.METRE);
        lengthMap.put(MeasurementSystem.US, ESHUnits.INCH);
        dimensionMap.put(Dimension.LENGTH, lengthMap);

        Map<MeasurementSystem, Unit<?>> intensityMap = new HashMap<>();
        intensityMap.put(MeasurementSystem.SI, ESHUnits.IRRADIANCE);
        intensityMap.put(MeasurementSystem.US, ESHUnits.IRRADIANCE);
        dimensionMap.put(Dimension.INTENSITY, intensityMap);
    }

    @Override
    public @Nullable Unit<?> parseUnit(String pattern) {
        if (StringUtils.isBlank(pattern)) {
            return null;
        }

        int lastBlankIndex = pattern.lastIndexOf(" ");
        if (lastBlankIndex < 0) {
            return null;
        }

        String unitSymbol = pattern.substring(lastBlankIndex).trim();
        if (StringUtils.isNotBlank(unitSymbol) && !unitSymbol.equals("%unit%")) {
            try {
                Quantity<?> quantity = Quantities.getQuantity("1 " + unitSymbol);
                return quantity.getUnit();
            } catch (IllegalArgumentException e) {
                // we expect this exception in case the extracted string does not match any known unit
                logger.warn("Unknown unit from pattern: {}", unitSymbol);
            }
        }

        return null;
    }
}
