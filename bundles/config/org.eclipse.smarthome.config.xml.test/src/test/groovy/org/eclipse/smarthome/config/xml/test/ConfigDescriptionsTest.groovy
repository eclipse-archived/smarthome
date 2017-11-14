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
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type
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
 * @author Thomas Höfer - Added unit
 */
class ConfigDescriptionsTest extends OSGiTest {

    static final String TEST_BUNDLE_NAME = "ConfigDescriptionsTest.bundle"
    static final String FRAGMENT_TEST_HOST_NAME = "ConfigDescriptionsFragmentTest.host"
    static final String FRAGMENT_TEST_FRAGMENT_NAME = "ConfigDescriptionsFragmentTest.fragment"

    ConfigDescriptionRegistry configDescriptionRegistry

    @Before
    void setUp() {
        configDescriptionRegistry = getService(ConfigDescriptionRegistry)
        assertThat configDescriptionRegistry, is(notNullValue())
    }

    @After
    void tearDown() {
        SyntheticBundleInstaller.uninstall(getBundleContext(), TEST_BUNDLE_NAME)
        SyntheticBundleInstaller.uninstall(getBundleContext(), FRAGMENT_TEST_FRAGMENT_NAME)
        SyntheticBundleInstaller.uninstall(getBundleContext(), FRAGMENT_TEST_HOST_NAME)
    }

    @Test
    void 'assert that ConfigDescriptions were loaded properly'() {
        def bundleContext = getBundleContext()
        def initialNumberOfConfigDescriptions = configDescriptionRegistry.getConfigDescriptions().size()

        // install test bundle
        Bundle bundle = SyntheticBundleInstaller.install(bundleContext, TEST_BUNDLE_NAME)
        assertThat bundle, is(notNullValue())

        def configDescriptions = configDescriptionRegistry.getConfigDescriptions(Locale.ENGLISH)
        assertThat configDescriptions.size(), is(initialNumberOfConfigDescriptions + 1)

        ConfigDescription dummyConfigDescription = configDescriptions.find {
            it.uri.equals(new URI("config:dummyConfig")) }
        assertThat dummyConfigDescription, is(notNullValue())

        def parameters = dummyConfigDescription.parameters
        assertThat parameters.size(), is(14)

        ConfigDescriptionParameter ipParameter = parameters.find { it.name.equals("ip") }
        assertThat ipParameter, is(notNullValue())
        assertThat ipParameter.type, is(Type.TEXT)
        ipParameter.with {
            assertThat groupName, is(null)
            assertThat context, is("network-address")
            assertThat label, is("Network Address")
            assertThat description, is("Network address of the hue bridge.")
            assertThat pattern, is("[0-9]{3}.[0-9]{3}.[0-9]{3}.[0-9]{3}")
            assertThat required, is(true)
            assertThat multiple, is(false)
            assertThat readOnly, is(true)
            assertThat unit, is(null)
            assertThat unitLabel, is(null)
        }

        ConfigDescriptionParameter usernameParameter = parameters.find { it.name.equals("username") }
        assertThat usernameParameter, is(notNullValue())
        assertThat usernameParameter.type, is(Type.TEXT)
        usernameParameter.with {
            assertThat groupName, is("user")
            assertThat context, is("password")
            assertThat label, is("Username")
            assertThat required, is(false)
            assertThat multiple, is(false)
            assertThat readOnly, is(false)
            assertThat description, is("Name of a registered hue bridge user, that allows to access the API.")
        }

        ConfigDescriptionParameter userPassParameter = parameters.find { it.name.equals("user-pass") }
        assertThat userPassParameter, is(notNullValue())
        assertThat userPassParameter.type, is(Type.TEXT)
        userPassParameter.with {
            assertThat min, is(8 as BigDecimal)
            assertThat max, is(16 as BigDecimal)
            assertThat required, is(true)
            assertThat verify, is(true)
            assertThat multiple, is(false)
            assertThat readOnly, is(false)
            assertThat context, is("password")
            assertThat label, is("Password")
        }

        ConfigDescriptionParameter colorItemParameter = parameters.find { it.name.equals("color-alarming-light") }
        assertThat colorItemParameter, is(notNullValue())
        assertThat colorItemParameter.type, is(Type.TEXT)
        colorItemParameter.with {
            assertThat required, is(false)
            assertThat readOnly, is(false)
            assertThat context, is("item")
            assertThat filterCriteria, is(notNullValue())
            assertThat filterCriteria.join(", "), is("FilterCriteria [name=\"tags\", value=\"alarm, light\"], FilterCriteria [name=\"type\", value=\"color\"], FilterCriteria [name=\"binding-id\", value=\"hue\"]")
        }

        ConfigDescriptionParameter listParameter1 = parameters.find { it.name.equals("list1") }
        assertThat listParameter1, is(notNullValue())
        assertThat listParameter1.type, is(Type.TEXT)
        listParameter1.with {
            assertThat required, is(false)
            assertThat multiple, is(true)
            assertThat readOnly, is(false)
            assertThat min, is(2 as BigDecimal)
            assertThat max, is(3 as BigDecimal)
            assertThat options, is(notNullValue())
            assertThat advanced, is(false)
            assertThat verify, is(false)
            assertThat limitToOptions, is(true)
            assertThat multipleLimit, is(null)
            assertThat options.join(", "), is("ParameterOption [value=\"key1\", label=\"label1\"], ParameterOption [value=\"key2\", label=\"label2\"]")
        }

        ConfigDescriptionParameter listParameter2 = parameters.find { it.name.equals("list2") }
        assertThat listParameter2, is(notNullValue())
        assertThat listParameter2.type, is(Type.TEXT)
        listParameter2.with {
            assertThat required, is(false)
            assertThat multiple, is(true)
            assertThat readOnly, is(false)
            assertThat options, is(notNullValue())
            assertThat advanced, is(true)
            assertThat limitToOptions, is(false)
            assertThat multipleLimit, is(4)
        }

        ConfigDescriptionParameter unitParameter = parameters.find { it.name.equals("unit") }
        assertThat unitParameter, is(notNullValue())
        unitParameter.with {
            assertThat unit, is("m")
            assertThat unitLabel, is(null)
        }

        ConfigDescriptionParameter unitLabelParameter = parameters.find { it.name.equals("unit-label") }
        assertThat unitLabelParameter, is(notNullValue())
        unitLabelParameter.with {
            assertThat unit, is(null)
            assertThat unitLabel, is("Runs")
        }

        ConfigDescriptionParameter unitOhmParameter = parameters.find { it.name.equals("unit-ohm") }
        assertThat unitLabelParameter, is(notNullValue())
        unitOhmParameter.with {
            assertThat unit, is("Ω")
            assertThat unitLabel, is(null)
        }

        ConfigDescriptionParameter unitAccelerationParameter = parameters.find { it.name.equals("unit-acceleration") }
        assertThat unitAccelerationParameter, is(notNullValue())
        unitAccelerationParameter.with {
            assertThat unit, is("m/s2")
            assertThat unitLabel, is("m/s\u00B2")
        }

        ConfigDescriptionParameter unitCelcius = parameters.find { it.name.equals("unit-celcius") }
        assertThat unitCelcius, is(notNullValue())
        unitCelcius.with {
            assertThat unit, is("Cel")
            assertThat unitLabel, is("°C")
        }

        ConfigDescriptionParameter unitSeconds = parameters.find { it.name.equals("unit-seconds") }
        assertThat unitSeconds, is(notNullValue())
        unitSeconds.with {
            assertThat unit, is("s")
            assertThat unitLabel, is("seconds")
        }

        ConfigDescriptionParameter unitMovements = parameters.find { it.name.equals("unit-movements") }
        assertThat unitMovements, is(notNullValue())
        unitMovements.with {
            assertThat unit, is(null)
            assertThat unitLabel, is("Movements")
        }

        ConfigDescriptionParameter unitKph = parameters.find { it.name.equals("unit-kph") }
        assertThat unitKph, is(notNullValue())
        unitKph.with {
            assertThat unit, is("kph")
            assertThat unitLabel, is("km/h")
        }

        def groups = dummyConfigDescription.parameterGroups
        assertThat groups.size(), is(2)

        ConfigDescriptionParameterGroup group1 = groups.find { it.name.equals("group1") }
        assertThat group1, is(notNullValue())
        group1.with {
            assertThat label, is("Group 1")
            assertThat description, is("Description Group 1")
            assertThat advanced, is(false)
            assertThat context, is("Context-Group1")
        }

        ConfigDescriptionParameterGroup group2 = groups.find { it.name.equals("group2") }
        assertThat group1, is(notNullValue())
        group2.with {
            assertThat label, is("Group 2")
            assertThat description, is("Description Group 2")
            assertThat advanced, is(true)
            assertThat context, is("Context-Group2")
        }

        ConfigDescription dummyConfigDescriptionDe = configDescriptionRegistry.getConfigDescriptions(Locale.GERMAN).find{
            it.uri.equals(new URI("config:dummyConfig")) }

        unitSeconds = dummyConfigDescriptionDe.parameters.find { it.name.equals("unit-seconds") }
        assertThat unitSeconds, is(notNullValue())
        unitSeconds.with {
            assertThat unit, is("s")
            assertThat unitLabel, is("Sekunden")
        }

        unitMovements = dummyConfigDescriptionDe.parameters.find { it.name.equals("unit-movements") }
        assertThat unitMovements, is(notNullValue())
        unitMovements.with {
            assertThat unit, is(null)
            assertThat unitLabel, is("Bewegungen")
        }

        unitKph = dummyConfigDescriptionDe.parameters.find { it.name.equals("unit-kph") }
        assertThat unitKph, is(notNullValue())
        unitKph.with {
            assertThat unit, is("kph")
            assertThat unitLabel, is("km/h")
        }

        // uninstall test bundle
        bundle.uninstall();
        assertThat bundle.state, is(Bundle.UNINSTALLED)
    }

    @Test
    void 'assert that ConfigDescriptions of fragment host were loaded properly'() {
        def bundleContext = getBundleContext()
        def initialNumberOfConfigDescriptions = configDescriptionRegistry.getConfigDescriptions().size()

        // install test bundle
        Bundle fragment = SyntheticBundleInstaller.installFragment(bundleContext, FRAGMENT_TEST_FRAGMENT_NAME)
        Bundle bundle = SyntheticBundleInstaller.install(bundleContext, FRAGMENT_TEST_HOST_NAME)
        assertThat bundle, is(notNullValue())

        def configDescriptions = configDescriptionRegistry.getConfigDescriptions()
        assertThat configDescriptions.size(), is(initialNumberOfConfigDescriptions + 1)

        ConfigDescription dummyConfigDescription = configDescriptions.find {
            it.uri.equals(new URI("config:fragmentConfig")) }
        assertThat dummyConfigDescription, is(notNullValue())

        def parameters = dummyConfigDescription.parameters
        assertThat parameters.size(), is(1)

        ConfigDescriptionParameter usernameParameter = parameters.find { it.name.equals("testParam") }
        assertThat usernameParameter, is(notNullValue())
        assertThat usernameParameter.type, is(Type.TEXT)
        usernameParameter.with {
            assertThat label, is("Test")
            assertThat required, is(false)
            assertThat multiple, is(false)
            assertThat readOnly, is(false)
            assertThat description, is("Test Parameter.")
        }

        fragment.uninstall();
        assertThat fragment.state, is(Bundle.UNINSTALLED)
        bundle.uninstall();
        assertThat bundle.state, is(Bundle.UNINSTALLED)
    }
}
