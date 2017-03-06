/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest;

import java.util.Locale;

import org.eclipse.smarthome.io.rest.internal.RESTActivator;

/**
 * {@link LocaleUtil} provides helper method for working with locales in REST
 * resources.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Markus Rathgeb - Use locale provider
 * @author Martin Herbst - Support of different language definition variants
 */
public class LocaleUtil {

    /**
     * Returns the locale in respect to the given "Accept-Language" HTTP header.
     *
     * @param language
     *            value of the "Accept-Language" HTTP header (can be null).
     * @return Locale for the "Accept-Language" HTTP header or default locale if
     *         header is not set or can not be parsed.
     */
    public static Locale getLocale(String acceptLanguageHttpHeader) {
        Locale locale = RESTActivator.getLocale();
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

}
