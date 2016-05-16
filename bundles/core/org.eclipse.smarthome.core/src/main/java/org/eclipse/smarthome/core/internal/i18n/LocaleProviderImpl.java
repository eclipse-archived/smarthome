/**
 * Copyright (c) 2016 Markus Rathgeb
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal.i18n;

import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a local provider using an OSGi component.
 *
 * @author Markus Rathgeb - Initial contribution and API
 */
public class LocaleProviderImpl implements LocaleProvider {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String LANGUAGE = "language";
    private final String SCRIPT = "script";
    private final String REGION = "region";
    private final String VARIANT = "variant";

    private Locale locale = Locale.getDefault();

    protected void activate(Map<String, Object> config) {
        modified(config);
    }

    protected void modified(Map<String, Object> config) {
        final String language = (String) config.get(LANGUAGE);
        final String script = (String) config.get(SCRIPT);
        final String region = (String) config.get(REGION);
        final String variant = (String) config.get(VARIANT);

        if (StringUtils.isEmpty(language)) {
            // if no language is set, skip new locale...
            logger.info("LOCALE: no language is set, keep old: {}", locale);
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
        logger.info("LOCALE: new locale set: {}", locale);
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

}
