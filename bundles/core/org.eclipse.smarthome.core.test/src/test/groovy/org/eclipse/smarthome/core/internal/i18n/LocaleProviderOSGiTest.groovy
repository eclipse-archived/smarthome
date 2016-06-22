/**
 * Copyright (c) 2016 Deutsche Telekom AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal.i18n

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.i18n.LocaleProvider
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * The {@link LocaleProviderOSGiTest} tests the basic functionality of the {@link LocaleProvider} OSGi service.
 *
 * @author Thomas Höfer - Initial contribution
 */
class LocaleProviderOSGiTest extends OSGiTest {

    def static final LANGUAGE_DE = "de"
    def static final LANGUAGE_RU = "ru"

    def static final SCRIPT_DE = "Latn"
    def static final SCRIPT_RU = "Cyrl"

    def static final REGION_DE = "DE"
    def static final REGION_RU = "RU"

    LocaleProvider localeProvider
    def defaultLocale

    @Before
    void setup() {
        localeProvider = getService(LocaleProvider)
        assertThat localeProvider, is(notNullValue())

        defaultLocale = Locale.getDefault()
    }

    @After
    void tearDown() {
        Locale.setDefault(defaultLocale)
    }

    @Test
    void 'assert that locale provider does not return null value'() {
        assertThat localeProvider.getLocale(), is(notNullValue())
    }

    @Test
    void 'assert that default locale is provided if no locale has been configured'() {
        Locale.setDefault(Locale.GERMAN)
        assertThat localeProvider.getLocale(), is(Locale.GERMAN)

        Locale.setDefault(Locale.ENGLISH)
        assertThat localeProvider.getLocale(), is(Locale.ENGLISH)
    }

    @Test
    void 'assert that configured locale is provided'() {
        assertLocale([(localeProvider.LANGUAGE) : LANGUAGE_DE, (localeProvider.SCRIPT) : SCRIPT_DE, (localeProvider.REGION) : REGION_DE])
        assertLocale([(localeProvider.LANGUAGE) : LANGUAGE_RU, (localeProvider.SCRIPT) : SCRIPT_RU, (localeProvider.REGION) : REGION_RU])
    }

    void assertLocale(def cfg) {
        localeProvider.modified(cfg)

        // must be ignored
        Locale.setDefault(Locale.ENGLISH)

        def locale = localeProvider.getLocale()
        assertThat locale, is(notNullValue())
        assertThat locale.getLanguage(), is(cfg[localeProvider.LANGUAGE])
        assertThat locale.getScript(), is(cfg[localeProvider.SCRIPT])
        assertThat locale.getCountry(), is(cfg[localeProvider.REGION])
        assertThat locale.getVariant(), is("")
    }
}
