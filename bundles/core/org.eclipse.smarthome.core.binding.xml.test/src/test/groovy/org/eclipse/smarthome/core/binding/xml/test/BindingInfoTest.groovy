/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
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
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.SyntheticBundleInstaller;
import org.junit.After;
import org.junit.Before
import org.junit.Test
import org.osgi.framework.Bundle

class BindingInfoTest extends OSGiTest {

    static final String TEST_BUNDLE_NAME = "BundleInfoTest.bundle"
	
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
	void 'assert that BindingInfo was read'() {
		def bundleContext = getBundleContext()
        def initialNumberOfBindingInfos = bindingInfoRegistry.bindingInfos.size()
		
        // install test bundle
		Bundle bundle = SyntheticBundleInstaller.install(bundleContext, TEST_BUNDLE_NAME)
		assertThat bundle, is(notNullValue())
		
        def bindingInfos = bindingInfoRegistry.bindingInfos
        assertThat bindingInfos.size(), is(initialNumberOfBindingInfos + 1)
        BindingInfo bindingInfo = bindingInfos.head()
        assertThat bindingInfo.id, is("hue")
        assertThat bindingInfo.configDescriptionURI, is(nullValue())
        assertThat bindingInfo.description, is("The hue Binding integrates the Philips hue system. It allows to control hue lights.")
        assertThat bindingInfo.name, is("hue Binding")
        assertThat bindingInfo.author, is("Deutsche Telekom AG")
        
        // uninstall test bundle
        bundle.uninstall();
        assertThat bundle.state, is(Bundle.UNINSTALLED)
	}
    
    @Test
    void 'assert that BindingInfo was removed after the bundle was uninstalled'() {
        def bundleContext = getBundleContext()
        def initialNumberOfBindingInfos = bindingInfoRegistry.bindingInfos.size()
        
        // install test bundle
        Bundle bundle = SyntheticBundleInstaller.install(bundleContext, TEST_BUNDLE_NAME)
        assertThat bundle, is(notNullValue())
        
        def bindingInfos = bindingInfoRegistry.bindingInfos
        assertThat bindingInfos.size(), is(initialNumberOfBindingInfos + 1)
        BindingInfo bindingInfo = bindingInfos.head()
        
        // uninstall test bundle
        bundle.uninstall();
        assertThat bundle.state, is(Bundle.UNINSTALLED)
        
        bindingInfos = bindingInfoRegistry.bindingInfos
        assertThat bindingInfos.size(), is(initialNumberOfBindingInfos)
        
        if (initialNumberOfBindingInfos > 0) {
            for (BindingInfo bindingInfo_ in bindingInfos) {
                assertThat bindingInfo_.id, is(not(bindingInfo.id))
            }
        }
    }
}
