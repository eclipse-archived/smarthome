/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.xml.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.config.core.ConfigDescription
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterGroup
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.SyntheticBundleInstaller
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.framework.Bundle

/**
 * The ConfigDescriptionsTest is a test for loading of configuration description from XML documents.
 *
 * @author Alex Tugarev - Initial contribution; Extended tests for options and filters
 *
 */
class ConfigDescriptionI18nTest extends OSGiTest {

    static final String TEST_BUNDLE_NAME = "yahooweather.bundle"

    ConfigDescriptionRegistry configDescriptionRegistry

    @Before
    void setUp() {
        configDescriptionRegistry = getService(ConfigDescriptionRegistry)
        assertThat configDescriptionRegistry, is(notNullValue())
    }

    @After
    void tearDown() {
        SyntheticBundleInstaller.uninstall(getBundleContext(), TEST_BUNDLE_NAME)
    }

    @Test
    void 'assert config decriptions were localized'() {
        def bundleContext = getBundleContext()
        def initialNumberOfConfigDescriptions = configDescriptionRegistry.getConfigDescriptions().size()

        // install test bundle
        Bundle bundle = SyntheticBundleInstaller.install(bundleContext, TEST_BUNDLE_NAME)
        assertThat bundle, is(notNullValue())

        def configDescriptions = configDescriptionRegistry.getConfigDescriptions(Locale.GERMAN)
        assertThat configDescriptions.size(), is(initialNumberOfConfigDescriptions + 1)

        def config = configDescriptions.first() as ConfigDescription

        assertThat config, is(notNullValue())
        assertEquals("""
        location.label = Ort
        location.description = Ort der Wetterinformation.
        unit.label = Einheit
        unit.description = Spezifiziert die Einheit der Daten. Valide Werte sind 'us' und 'metric'
        refresh.label = Aktualisierungsintervall
        refresh.description = Spezifiziert das Aktualisierungsintervall in Sekunden
        question.pattern = Wie ist das Wetter in [\\w]*?
        question.options = München, Köln
        group.label = Group 1 German Label
        group.description = Group 1 German Description
        """, asString(config))


    }

    String asString(final ConfigDescription self) {
        ConfigDescriptionParameter location = self.getParameters().find { it.getName().equals("location") }
        def location_label = location.getLabel()
        def location_description = location.getDescription()

        ConfigDescriptionParameter unit = self.getParameters().find { it.getName().equals("unit") }
        def unit_label = unit.getLabel()
        def unit_description = unit.getDescription()

        ConfigDescriptionParameter refresh = self.getParameters().find { it.getName().equals("refresh") }
        def refresh_label = refresh.getLabel()
        def refresh_description = refresh.getDescription()

        ConfigDescriptionParameter question = self.getParameters().find { it.getName().equals("question") }
        def pattern = question.getPattern()
        def options = question.getOptions().collect { it.getLabel() }.join(", ")

        ConfigDescriptionParameterGroup group = self.getParameterGroups().find { it.getName().equals("group1") }
        def group_label = group.getLabel()
        def group_description = group.getDescription()

        return """
        location.label = ${location_label}
        location.description = ${location_description}
        unit.label = ${unit_label}
        unit.description = ${unit_description}
        refresh.label = ${refresh_label}
        refresh.description = ${refresh_description}
        question.pattern = ${pattern}
        question.options = ${options}
        group.label = ${group_label}
        group.description = ${group_description}
        """
    }
}
