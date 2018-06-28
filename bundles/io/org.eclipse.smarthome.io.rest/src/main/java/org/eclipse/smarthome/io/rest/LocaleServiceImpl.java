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
package org.eclipse.smarthome.io.rest;

import java.util.Locale;

import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link LocaleServiceImpl} provides helper method for working with locales in REST
 * resources.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Markus Rathgeb - Use locale provider
 * @author Martin Herbst - Support of different language definition variants
 * @authro Lyubomir Papazov - Add component annotation, rename the class to LocaleService and add method tryGetLocale
 */
@Component
public class LocaleServiceImpl implements LocaleService {

    private final Logger logger = LoggerFactory.getLogger(LocaleServiceImpl.class);

    private volatile LocaleProvider localeProvider;

    /**
     * Returns the locale in respect to the given "Accept-Language" HTTP header.
     *
     * @param language value of the "Accept-Language" HTTP header (can be null).
     * @return Locale for the "Accept-Language" HTTP header or default locale if
     *         header is not set or can not be parsed.
     */
    @Override
    public Locale getLocale(String acceptLanguageHttpHeader) {
        Locale locale = tryGetLocale();
        if (acceptLanguageHttpHeader != null) {
            int pos = acceptLanguageHttpHeader.indexOf(',');
            String[] split;
            if (pos > -1) {
                split = acceptLanguageHttpHeader.substring(0, pos).split("-");
            } else {
                split = acceptLanguageHttpHeader.split("-");
            }
            if (split.length == 2) {
                locale = new Locale(split[0], split[1]);
            } else {
                locale = new Locale(split[0]);
            }
        }
        return locale;
    }

    @Reference(policy = ReferencePolicy.DYNAMIC)
    protected void setLocaleProvider(LocaleProvider provider) {
        logger.debug("The localeProvider in LocaleUtilService has been set");
        localeProvider = provider;
    }

    protected void unsetLocaleProvider(LocaleProvider provider) {
        logger.debug("The localeProvider in LocaleUtilService has been unset");
        localeProvider = null;
    }

    /**
     * Returns the provided locale or the default one if the provider is null.
     *
     * @return the default locale
     */
    private Locale tryGetLocale() {
        final LocaleProvider provider = localeProvider;
        if (provider != null) {
            return provider.getLocale();
        } else {
            logger.error("There should ALWAYS be a local provider available, as it is provided by the core.");
            return Locale.US;
        }
    }

}
