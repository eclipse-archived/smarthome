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

import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.SyntheticBundleInstaller
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.framework.Bundle
import org.osgi.framework.ServiceReference

/***
 *
 * This test checks if channel types are loaded properly.
 *
 * @author Dennis Nobel - Initial contribution
 */
class ChannelTypesTest extends OSGiTest {

    static final String TEST_BUNDLE_NAME = "ChannelTypesTest.bundle"

    ChannelTypeProvider channelTypeProvider

    @Before
    void setUp() {
        // get ONLY the XMLChannelTypeProvider
        channelTypeProvider = getService(ChannelTypeProvider, {ServiceReference serviceReference ->
            serviceReference.getBundle().getSymbolicName().contains("xml")})
        assertThat channelTypeProvider, is(notNullValue())
    }

    @After
    void tearDown() {
        SyntheticBundleInstaller.uninstall(getBundleContext(), TEST_BUNDLE_NAME)
    }

    @Test
    void 'assert that ChannelTypes were loaded'() {
        def bundleContext = getBundleContext()
        def initialNumberOfChannelTypes = channelTypeProvider.getChannelTypes(null).size()
        def initialNumberOfChannelGroupTypes = channelTypeProvider.getChannelGroupTypes(null).size()

        // install test bundle
        Bundle bundle = SyntheticBundleInstaller.install(bundleContext, TEST_BUNDLE_NAME)
        assertThat bundle, is(notNullValue())

        def channelTypes = channelTypeProvider.getChannelTypes(null)
        assertThat channelTypes.size(), is(initialNumberOfChannelTypes + 2)

        def channelType1 = channelTypes.find( {it.UID.toString().equals("somebinding:channel1")} )
        assertThat channelType1, is(not(null))

        def channelType2 = channelTypes.find( {it.UID.toString().equals("somebinding:channel-without-reference")} )
        assertThat channelType2, is(not(null))

        def channelGroupTypes = channelTypeProvider.getChannelGroupTypes(null)
        assertThat channelGroupTypes.size(), is(initialNumberOfChannelGroupTypes + 1)

        def channelGroupType = channelGroupTypes.find( {it.UID.toString().equals("somebinding:channelgroup")} )
        assertThat channelGroupType, is(not(null))

        SyntheticBundleInstaller.uninstall(bundleContext, TEST_BUNDLE_NAME)

        assertThat channelTypeProvider.getChannelTypes(null).size(), is(initialNumberOfChannelTypes)
        assertThat channelTypeProvider.getChannelGroupTypes(null).size(), is(initialNumberOfChannelGroupTypes)
    }
}
