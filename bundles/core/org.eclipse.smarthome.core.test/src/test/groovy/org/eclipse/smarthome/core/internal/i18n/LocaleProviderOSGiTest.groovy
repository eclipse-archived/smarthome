/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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
import org.osgi.service.cm.ConfigurationAdmin

/**
 * The {@link LocaleProviderOSGiTest} tests the basic functionality of the {@link LocaleProvider} OSGi service.
 *
 * @author Thomas Höfer - Initial contribution
 */
class LocaleProviderOSGiTest extends OSGiTest {

    def static final LANGUAGE_DE = "de"
    def static final LANGUAGE_ES = "es"
    def static final LANGUAGE_RU = "ru"

    def static final SCRIPT_DE = "Latn"
    def static final SCRIPT_ES = "Latn"
    def static final SCRIPT_RU = "Cyrl"

    def static final REGION_DE = "DE"
    def static final REGION_ES = "ES"
    def static final REGION_RU = "RU"

    LocaleProvider localeProvider
    ConfigurationAdmin configAdmin
    def defaultLocale

    @Before
    void setup() {
        localeProvider = getService(LocaleProvider)
        assertThat localeProvider, is(notNullValue())

        configAdmin = getService(ConfigurationAdmin)
        assertThat configAdmin, is(notNullValue())

        clearLocale()

        waitForAssert {
            assertThat localeProvider.@locale, is(null)
        }

        defaultLocale = Locale.getDefault()
    }

    @After
    void tearDown () {
        Locale.setDefault(defaultLocale)
    }

    @Test
    void 'assert that locale provider does not return null value'() {
        assertThat localeProvider.getLocale(), is(notNullValue())
    }

    @Test
    void 'assert that default locale is provided if no locale has been configured'() {
        Locale.setDefault(Locale.GERMAN)
        waitForAssert {
            assertThat localeProvider.getLocale(), is(Locale.GERMAN)
        }

        Locale.setDefault(Locale.ENGLISH)
        waitForAssert {
            assertThat localeProvider.getLocale(), is(Locale.ENGLISH)
        }
    }

    @Test
    void 'assert that configured locale is provided'() {
        updateAndAssertLocale(createLocaleProperties(LANGUAGE_DE, SCRIPT_DE, REGION_DE))
        updateAndAssertLocale(createLocaleProperties(LANGUAGE_RU, SCRIPT_RU, REGION_RU))
    }

    @Test
    void 'assert that locale can be updated using directly the modified operation'() {
        // must be ignored
        Locale.setDefault(Locale.ENGLISH)

        def cfg = createLocaleProperties(LANGUAGE_ES, SCRIPT_ES, REGION_ES)
        localeProvider.modified(cfg)

        assertLocale(cfg)
    }

    @Test
    void 'assert that default locale is provided after locale has been cleared'() {
        updateAndAssertLocale(createLocaleProperties(LANGUAGE_DE, SCRIPT_DE, REGION_DE), Locale.CHINA)

        clearLocale()

        waitForAssert {
            assertThat localeProvider.getLocale(), is(Locale.CHINA)
        }
    }

    void updateAndAssertLocale(def cfg, def locale=Locale.ENGLISH) {
        updateConfig(cfg)

        // must be ignored
        Locale.setDefault(locale)

        assertLocale(cfg)
    }

    def updateConfig(def cfg) {
        def config = getConfig()
        def properties = config.getProperties()
        if (properties == null) {
            properties = new Properties()
        }
        cfg.each { k, v ->
            properties.put(k,  v)
        }
        config.update(properties)
    }

    def assertLocale(def cfg) {
        waitForAssert {
            def locale = localeProvider.getLocale()
            assertThat locale, is(notNullValue())
            assertThat locale.getLanguage(), is(cfg[localeProvider.LANGUAGE])
            assertThat locale.getScript(), is(cfg[localeProvider.SCRIPT])
            assertThat locale.getCountry(), is(cfg[localeProvider.REGION])
            assertThat locale.getVariant(), is("")
        }
    }

    def clearLocale() {
        def config = getConfig()
        config.update(createLocaleProperties())
    }

    def createLocaleProperties(def language="", def script="", def region = "") {
        return [(localeProvider.LANGUAGE) : language, (localeProvider.SCRIPT) : script, (localeProvider.REGION) : region] as Properties
    }

    def getConfig() {
        return configAdmin.getConfiguration("org.eclipse.smarthome.core.localeprovider")
    }
}
