/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.binding.xml.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.binding.BindingInfo
import org.eclipse.smarthome.core.binding.BindingInfoRegistry
import org.eclipse.smarthome.core.i18n.LocaleProvider
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.SyntheticBundleInstaller
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.framework.Bundle
import org.osgi.service.cm.Configuration
import org.osgi.service.cm.ConfigurationAdmin

class BindingInfoI18nTest extends OSGiTest {

    static final String TEST_BUNDLE_NAME = "yahooweather.bundle"

    BindingInfoRegistry bindingInfoRegistry

    @Before
    void setUp() {
        bindingInfoRegistry = getService(BindingInfoRegistry)
        assertThat bindingInfoRegistry, is(notNullValue())
    }

    @After
    void tearDown() {
        SyntheticBundleInstaller.uninstall(getBundleContext(), TEST_BUNDLE_NAME)
    }

    @Test
    void 'assert binding infos were localized in German'() {
        def bundleContext = getBundleContext()
        def initialNumberOfBindingInfos = bindingInfoRegistry.getBindingInfos().size()

        // install test bundle
        Bundle bundle = SyntheticBundleInstaller.install(bundleContext, TEST_BUNDLE_NAME)
        assertThat bundle, is(notNullValue())

        def bindingInfos = bindingInfoRegistry.getBindingInfos(Locale.GERMAN)
        assertThat bindingInfos.size(), is(initialNumberOfBindingInfos + 1)
        BindingInfo bindingInfo = bindingInfos.first()

        assertThat bindingInfo, is(notNullValue())
        assertEquals("""
        name = Yahoo Wetter Binding
        description = Das Yahoo Wetter Binding stellt verschiedene Wetterdaten wie die Temperatur, die Luftfeuchtigkeit und den Luftdruck für konfigurierbare Orte vom yahoo Wetterdienst bereit
        """, asString(bindingInfo))
    }

    @Test
    void 'assert binding infos were localized in Dutch'() {
        def bundleContext = getBundleContext()
        def initialNumberOfBindingInfos = bindingInfoRegistry.getBindingInfos().size()

        // install test bundle
        Bundle bundle = SyntheticBundleInstaller.install(bundleContext, TEST_BUNDLE_NAME)
        assertThat bundle, is(notNullValue())

        def bindingInfos = bindingInfoRegistry.getBindingInfos(new Locale("nl"))
        assertThat bindingInfos.size(), is(initialNumberOfBindingInfos + 1)
        BindingInfo bindingInfo = bindingInfos.first()

        assertThat bindingInfo, is(notNullValue())
        assertEquals("""
        name = Yahoo Weer Binding
        description = De Yahoo Weer Binding biedt verschillende meteorologische gegevens zoals temperatuur, vochtigheid en luchtdruk voor configureerbare locaties uit yahoo weerdienst klaar
        """, asString(bindingInfo))
    }

    @Test
    void 'assert using original binding infos, if provided locale is not supported'() {
        def bundleContext = getBundleContext()
        def initialNumberOfBindingInfos = bindingInfoRegistry.getBindingInfos().size()

        // install test bundle
        Bundle bundle = SyntheticBundleInstaller.install(bundleContext, TEST_BUNDLE_NAME)
        assertThat bundle, is(notNullValue())

        def bindingInfos = bindingInfoRegistry.getBindingInfos(Locale.FRENCH)
        assertThat bindingInfos.size(), is(initialNumberOfBindingInfos + 1)
        BindingInfo bindingInfo = bindingInfos.first()

        assertThat bindingInfo, is(notNullValue())
        assertEquals("""
        name = YahooWeather Binding
        description = The Yahoo Weather Binding requests the Yahoo Weather Service to show the current temperature, humidity and pressure.
        """, asString(bindingInfo))
    }

    @Test
    void 'assert using default locale'() {
        // Set german locale
        ConfigurationAdmin configAdmin = getService(ConfigurationAdmin.class);
        Configuration config = configAdmin.getConfiguration("org.eclipse.smarthome.core.i18nprovider", null);
        Dictionary<String, String> localeCfg = new Hashtable<String, String>();
        localeCfg.put("language", "de");
        localeCfg.put("country", "DE");
        config.update(localeCfg);

        //before running the test with a default locale make sure the locale has been set
        LocaleProvider localeProvider = getService(LocaleProvider.class);
        waitForAssert {
            assertThat localeProvider.getLocale().toString(), is("de")
        }

        def bundleContext = getBundleContext()
        def initialNumberOfBindingInfos = bindingInfoRegistry.getBindingInfos().size()

        // install test bundle
        Bundle bundle = SyntheticBundleInstaller.install(bundleContext, TEST_BUNDLE_NAME)
        assertThat bundle, is(notNullValue())

        def bindingInfos = bindingInfoRegistry.getBindingInfos(/*use default locale*/ null)
        assertThat bindingInfos.size(), is(initialNumberOfBindingInfos + 1)
        BindingInfo bindingInfo = bindingInfos.first()

        assertThat bindingInfo, is(notNullValue())
        assertEquals("""
        name = Yahoo Wetter Binding
        description = Das Yahoo Wetter Binding stellt verschiedene Wetterdaten wie die Temperatur, die Luftfeuchtigkeit und den Luftdruck für konfigurierbare Orte vom yahoo Wetterdienst bereit
        """, asString(bindingInfo))
    }

    String asString(final BindingInfo self) {
        def name = self.getName()
        def description = self.getDescription()
        return """
        name = ${name}
        description = ${description}
        """
    }
}
