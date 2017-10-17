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
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.LocationProvider;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.library.types.PointType;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class I18nProviderImpl implements TranslationProvider, LocaleProvider, LocationProvider, TimeZoneProvider {

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

    @Activate
    @SuppressWarnings("unchecked")
    protected void activate(ComponentContext componentContext) {
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

        setTimeZone(zoneId);
        setLocation(location);

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

        logger.info("Locale set to {}, Location set to {}, Time zone set to {}", locale, this.location, this.timeZone);
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
}
