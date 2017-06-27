/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.internal.i18n;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.Locale;

import org.eclipse.smarthome.config.core.ParameterOption;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link I18nConfigOptionsProvider}
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
public class I18nConfigOptionsProviderTest {

    private I18nConfigOptionsProvider provider;
    private ParameterOption expectedLangEN = new ParameterOption("en", "English");
    private ParameterOption expectedLangFR = new ParameterOption("en", "anglais");
    private ParameterOption expectedCntryEN = new ParameterOption("US", "United States");
    private ParameterOption expectedCntryFR = new ParameterOption("US", "Etats-Unis");
    private URI uriI18N;

    @Before
    public void setup() throws Exception {
        provider = new I18nConfigOptionsProvider();
        uriI18N = new URI("system:i18n");
    }

    @Test
    public void testLanguage() throws Exception {
        assertTrue(provider.getParameterOptions(uriI18N, "language", Locale.US).contains(expectedLangEN));
        assertTrue(provider.getParameterOptions(uriI18N, "language", Locale.FRENCH).contains(expectedLangFR));
        assertFalse(provider.getParameterOptions(uriI18N, "language", null).isEmpty());
    }

    @Test
    public void testRegion() throws Exception {
        assertTrue(provider.getParameterOptions(uriI18N, "region", Locale.US).contains(expectedCntryEN));
        assertTrue(provider.getParameterOptions(uriI18N, "region", Locale.FRENCH).contains(expectedCntryFR));
        assertFalse(provider.getParameterOptions(uriI18N, "region", null).isEmpty());
    }

    @Test
    public void testUnknownParameter() throws Exception {
        assertNull(provider.getParameterOptions(uriI18N, "unknown", Locale.US));
    }

}
