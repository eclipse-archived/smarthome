/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
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
	void 'assert that ConfigDescriptions were loaded properly'() {
		def bundleContext = getBundleContext()
        def initialNumberOfConfigDescriptions = configDescriptionRegistry.getConfigDescriptions().size()
		
        // install test bundle
		Bundle bundle = SyntheticBundleInstaller.install(bundleContext, TEST_BUNDLE_NAME)
		assertThat bundle, is(notNullValue())
		
        def configDescriptions = configDescriptionRegistry.getConfigDescriptions()
        assertThat configDescriptions.size(), is(initialNumberOfConfigDescriptions + 1)
        
        ConfigDescription dummyConfigDescription = configDescriptions.find {
                it.uri.equals(new URI("config:dummyConfig")) }
        assertThat dummyConfigDescription, is(notNullValue())
        
        def parameters = dummyConfigDescription.parameters
        assertThat parameters.size(), is(2)
        
        def ipParameter = parameters.find { it.name.equals("ip") }
        assertThat ipParameter, is(notNullValue())
        assertThat ipParameter.type, is(Type.TEXT)
        ipParameter.with {
            assertThat context, is("network_address")
            assertThat label, is("Network Address")
            assertThat description, is("Network address of the hue bridge.")
            assertThat required, is(true)
        }
        
        def usernameParameter = parameters.find { it.name.equals("username") }
        assertThat usernameParameter, is(notNullValue())
        assertThat usernameParameter.type, is(Type.TEXT)
        usernameParameter.with {
            assertThat context, is("password")
            assertThat label, is("Username")
            assertThat description, is("Name of a registered hue bridge user, that allows to access the API.")
        }
        
        // uninstall test bundle
        bundle.uninstall();
        assertThat bundle.state, is(Bundle.UNINSTALLED)
	}
}
