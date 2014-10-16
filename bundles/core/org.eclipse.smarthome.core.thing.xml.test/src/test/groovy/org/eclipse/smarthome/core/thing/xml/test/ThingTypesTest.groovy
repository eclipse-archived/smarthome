/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
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
import org.eclipse.smarthome.core.thing.type.BridgeType
import org.eclipse.smarthome.core.thing.type.ChannelDefinition
import org.eclipse.smarthome.core.thing.type.ThingType
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.SyntheticBundleInstaller
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.framework.Bundle

class ThingTypesTest extends OSGiTest {

    static final String TEST_BUNDLE_NAME = "ThingTypesTest.bundle"

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
    void 'assert that ThingTypes were loaded'() {
        def bundleContext = getBundleContext()
        def initialNumberOfThingTypes = thingTypeProvider.getThingTypes(null).size()

        // install test bundle
        Bundle bundle = SyntheticBundleInstaller.install(bundleContext, TEST_BUNDLE_NAME)
        assertThat bundle, is(notNullValue())

        def thingTypes = thingTypeProvider.getThingTypes(null)
        assertThat thingTypes.size(), is(initialNumberOfThingTypes + 2)

        def bridgeType = thingTypes.find { it.toString().equals("hue:bridge") } as BridgeType
        assertThat bridgeType, is(notNullValue())
        assertThat bridgeType.label, is("HUE Bridge")
        assertThat bridgeType.description, is("The hue Bridge represents the Philips hue bridge.")

        def thingType = thingTypes.find { it.toString().equals("hue:lamp") } as ThingType
        assertThat thingType, is(notNullValue())
        assertThat thingType.label, is("HUE Lamp")
        assertThat thingType.description, is("My own great HUE Lamp.")
        assertThat thingType.supportedBridgeTypeUIDs.size(), is(1)
        assertThat thingType.supportedBridgeTypeUIDs.head(), is("hue:bridge")
        thingType.channelDefinitions.with {
            assertThat size(), is(2)
            def colorChannel = it.find { it.id.equals("color") } as ChannelDefinition
            assertThat colorChannel, is(notNullValue())
            def colorChannelType = colorChannel.type
            assertThat colorChannelType, is(notNullValue())
            colorChannelType.with {
                assertThat it.toString(), is("hue:color")
                assertThat itemType, is("ColorItem")
                assertThat label, is("HUE Lamp Color")
                assertThat description, is("The color channel allows to control the color of the hue lamp. It is also possible to dim values and switch the lamp on and off.")
            }

            def colorTemperatureChannel = it.find { it.id.equals("color_temperature") } as ChannelDefinition
            assertThat colorTemperatureChannel, is(notNullValue())
            def colorTemperatureChannelType = colorTemperatureChannel.type
            assertThat colorTemperatureChannelType, is(notNullValue())
            colorTemperatureChannelType.with {
                assertThat it.toString(), is("hue:color_temperature")
                assertThat itemType, is("DimmerItem")
                assertThat label, is("HUE Lamp Color Temperature")
                assertThat description, is("The color temperature channel allows to set the color temperature from 0 (cold) to 100 (warm).")
            }
        }

        // uninstall test bundle
        bundle.uninstall();
        assertThat bundle.state, is(Bundle.UNINSTALLED)
    }

    @Test
    void 'assert that ThingTypes were removed after the bundle was uninstalled'() {
        def bundleContext = getBundleContext()
        def initialNumberOfThingTypes = thingTypeProvider.getThingTypes(null).size()

        // install test bundle
        Bundle bundle = SyntheticBundleInstaller.install(bundleContext, TEST_BUNDLE_NAME)
        assertThat bundle, is(notNullValue())

        def thingTypes = thingTypeProvider.getThingTypes(null)
        assertThat thingTypes.size(), is(initialNumberOfThingTypes + 2)

        // uninstall test bundle
        bundle.uninstall();
        assertThat bundle.state, is(Bundle.UNINSTALLED)

        thingTypes = thingTypeProvider.getThingTypes(null)
        assertThat thingTypes.size(), is(initialNumberOfThingTypes)
    }
}
