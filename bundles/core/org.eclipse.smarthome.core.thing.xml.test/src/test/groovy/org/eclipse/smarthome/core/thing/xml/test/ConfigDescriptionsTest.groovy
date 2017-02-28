/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.config.core.ConfigDescription
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.SyntheticBundleInstaller
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.framework.Bundle

class ConfigDescriptionsTest extends OSGiTest {

    static final String TEST_BUNDLE_NAME = "ConfigDescriptionsTest.bundle"
	
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
	void 'assert that a synthetic bundle is loaded from test resources'() {
		def bundleContext = getBundleContext()
		
        // install test bundle
		Bundle bundle = SyntheticBundleInstaller.install(bundleContext, TEST_BUNDLE_NAME)
		assertThat bundle, is(notNullValue())
        
        // uninstall test bundle
        bundle.uninstall();
        assertThat bundle.state, is(Bundle.UNINSTALLED)
	}
	
	@Test
	void 'assert that ConfigDescriptions are loaded properly'() {
		def bundleContext = getBundleContext()
        def initialNumberOfConfigDescriptions = configDescriptionRegistry.getConfigDescriptions().size()
		
        // install test bundle
		Bundle bundle = SyntheticBundleInstaller.install(bundleContext, TEST_BUNDLE_NAME)
		assertThat bundle, is(notNullValue())
		
        def configDescriptions = configDescriptionRegistry.getConfigDescriptions()
        assertThat configDescriptions.size(), is(initialNumberOfConfigDescriptions + 3)
        
        ConfigDescription bridgeConfigDescription = configDescriptions.find {
                it.uri.equals(new URI("thing-type:hue:bridge")) }
        assertThat bridgeConfigDescription, is(notNullValue())
        
        def parameters = bridgeConfigDescription.parameters
        assertThat parameters.size(), is(2)

        def ipParameter = parameters.find { it.name.equals("ip") }
        assertThat ipParameter, is(notNullValue())
        assertThat ipParameter.type, is(Type.TEXT)
        ipParameter.with {
            assertThat context, is("network-address")
            assertThat label, is("Network Address")
            assertThat description, is("Network address of the hue bridge.")
            assertThat required, is(true)
        }

        def userNameParameter = parameters.find { it.name.equals("username") }
		assertThat userNameParameter, is(notNullValue())

		assertThat userNameParameter.advanced, is(true)

        assertThat userNameParameter.type, is(Type.TEXT)
        userNameParameter.with {
            assertThat context, is("password")
            assertThat label, is("Username")
            assertThat description, is("Name of a registered hue bridge user, that allows to access the API.")
        }
        
        ConfigDescription colorConfigDescription = configDescriptions.find {
                it.uri.equals(new URI("channel-type:hue:color")) }
        assertThat colorConfigDescription, is(notNullValue())
        
        parameters = colorConfigDescription.parameters
        assertThat parameters.size(), is(1)
        
        def lastDimValueParameter = parameters.find { it.name.equals("lastDimValue") }
        assertThat lastDimValueParameter, is(notNullValue())
        assertThat lastDimValueParameter.type, is(Type.BOOLEAN)

        def groups = bridgeConfigDescription.parameterGroups
        assertThat groups.size(), is(2)

        def group1 = groups.find { it.name.equals("group1") }
        assertThat group1, is(notNullValue())
        group1.with {
            assertThat context, is("Group1-context")
            assertThat label, is("Group Label 1")
            assertThat description, is("Group description 1")
        }

        // uninstall test bundle
        bundle.uninstall();
        assertThat bundle.state, is(Bundle.UNINSTALLED)
	}
    
    
    @Test
    void 'assert that parameters with options and filters are loaded properly'() {
        def bundleContext = getBundleContext()
        def initialNumberOfConfigDescriptions = configDescriptionRegistry.getConfigDescriptions().size()
        
        // install test bundle
        Bundle bundle = SyntheticBundleInstaller.install(bundleContext, TEST_BUNDLE_NAME)
        assertThat bundle, is(notNullValue())
        
        def configDescriptions = configDescriptionRegistry.getConfigDescriptions()
        assertThat configDescriptions.size(), is(initialNumberOfConfigDescriptions + 3)
        
        ConfigDescription bridgeConfigDescription = configDescriptions.find {
                it.uri.equals(new URI("thing-type:hue:dummy")) }
        assertThat bridgeConfigDescription, is(notNullValue())
        
        def parameters = bridgeConfigDescription.parameters
        assertThat parameters.size(), is(2)
        
        ConfigDescriptionParameter unitParameter = parameters.find { it.name.equals("unit") }
        assertThat unitParameter, is(notNullValue())
        unitParameter.with {
            assertThat options.join(", "), is("ParameterOption [value=\"us\", label=\"US\"], ParameterOption [value=\"metric\", label=\"Metric\"]")
        }
        
        ConfigDescriptionParameter lightParameter = parameters.find { it.name.equals("color-alarming-light") }
        assertThat lightParameter, is(notNullValue())
        lightParameter.with {
            assertThat filterCriteria.join(", "), is("FilterCriteria [name=\"tags\", value=\"alarm, light\"], FilterCriteria [name=\"type\", value=\"color\"], FilterCriteria [name=\"binding-id\", value=\"hue\"]")
        }
    }
}
