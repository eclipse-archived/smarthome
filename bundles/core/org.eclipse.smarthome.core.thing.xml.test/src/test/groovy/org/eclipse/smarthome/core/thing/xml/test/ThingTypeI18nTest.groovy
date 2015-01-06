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

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider
import org.eclipse.smarthome.core.thing.type.BridgeType
import org.eclipse.smarthome.core.thing.type.ChannelDefinition
import org.eclipse.smarthome.core.thing.type.ThingType
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.SyntheticBundleInstaller
import org.junit.After
import org.junit.Before
import org.junit.Ignore;
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
        assertThat thingTypes.size(), is(initialNumberOfThingTypes + 1)

        def weatherType = thingTypes.find { it.toString().equals("yahooweather:weather") } as ThingType
        assertThat weatherType, is(notNullValue())
        assertEquals("""
        label = Wetterinformation
        description = Stellt verschiedene Wetterdaten vom yahoo Wetterdienst bereit
        """, asString(weatherType))
    }
    
    String asString(final ThingType self) {
        def label = self.label
        def description = self.description
        return """
        label = ${label}
        description = ${description}
        """
    }
    

}
