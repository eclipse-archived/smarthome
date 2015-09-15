/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider
import org.eclipse.smarthome.core.thing.type.AbstractDescriptionType
import org.eclipse.smarthome.core.thing.type.ChannelType
import org.eclipse.smarthome.core.thing.type.ThingType
import org.eclipse.smarthome.core.thing.type.TypeResolver
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.SyntheticBundleInstaller
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.framework.Bundle

class ThingTypeI18nTest extends OSGiTest {

    static final String TEST_BUNDLE_NAME = "yahooweather.bundle"

    ThingTypeProvider thingTypeProvider

    @Before
    void setUp() {
        thingTypeProvider = getService(ThingTypeProvider)
        assertThat thingTypeProvider, is(notNullValue())
    }

    @After
    void tearDown() {
        SyntheticBundleInstaller.uninstall(getBundleContext(), TEST_BUNDLE_NAME)
    }

    @Test
    void 'assert that thing type was localized'() {
        def bundleContext = getBundleContext()
        def initialNumberOfThingTypes = thingTypeProvider.getThingTypes(null).size()

        // install test bundle
        Bundle bundle = SyntheticBundleInstaller.install(bundleContext, TEST_BUNDLE_NAME)
        assertThat bundle, is(notNullValue())

        def thingTypes = thingTypeProvider.getThingTypes(Locale.GERMAN)
        assertThat thingTypes.size(), is(initialNumberOfThingTypes + 2)

        def weatherType = thingTypes.find { it.toString().equals("yahooweather:weather") } as ThingType
        assertThat weatherType, is(notNullValue())
        assertEquals("""
        label = Wetterinformation
        description = Stellt verschiedene Wetterdaten vom yahoo Wetterdienst bereit
        """, asString(weatherType))
    }

    @Test
    void 'assert that channel group type was localized'() {

        def bundleContext = getBundleContext()
        def initialNumberOfThingTypes = thingTypeProvider.getThingTypes(null).size()

        // install test bundle
        Bundle bundle = SyntheticBundleInstaller.install(bundleContext, TEST_BUNDLE_NAME)
        assertThat bundle, is(notNullValue())

        def thingTypes = thingTypeProvider.getThingTypes(Locale.GERMAN)
        assertThat thingTypes.size(), is(initialNumberOfThingTypes + 2)

        def weatherGroupType = thingTypes.find { it.toString().equals("yahooweather:weather-with-group") } as ThingType
        println weatherGroupType.channelGroupDefinitions[0].typeUID
        println TypeResolver.resolve(weatherGroupType.channelGroupDefinitions[0].typeUID).label

        assertThat weatherGroupType, is(notNullValue())
        assertEquals("""
        label = Wetterinformation mit Gruppe
        description = Wetterinformation mit Gruppe Beschreibung
        """, asString(TypeResolver.resolve(weatherGroupType.channelGroupDefinitions[0].typeUID)))
    }

    @Test
    void 'assert that channel type was localized'() {

        def bundleContext = getBundleContext()
        def initialNumberOfThingTypes = thingTypeProvider.getThingTypes(null).size()

        // install test bundle
        Bundle bundle = SyntheticBundleInstaller.install(bundleContext, TEST_BUNDLE_NAME)
        assertThat bundle, is(notNullValue())

        def thingTypes = thingTypeProvider.getThingTypes(Locale.GERMAN)
        assertThat thingTypes.size(), is(initialNumberOfThingTypes + 2)

        def weatherType = thingTypes.find { it.toString().equals("yahooweather:weather") } as ThingType
        def temperatureChannelType = TypeResolver.resolve(weatherType.channelDefinitions.find { it.getId().equals("temperature")}.channelTypeUID) as ChannelType

        assertEquals("""
        label = Temperatur
        description = Temperaturwert
        pattern = %d Grad Celsius
        option = Mein String
        """, asString(temperatureChannelType))
    }


    String asString(final AbstractDescriptionType self) {
        def label = self.label
        def description = self.description
        return """
        label = ${label}
        description = ${description}
        """
    }

    String asString(final ChannelType self) {
        def label = self.label
        def description = self.description
        def pattern = self.state.pattern
        def option = self.state.options[0].label
        return """
        label = ${label}
        description = ${description}
        pattern = ${pattern}
        option = ${option}
        """
    }
}
