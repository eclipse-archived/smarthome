/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider
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
class ChannelTypesI18nTest extends OSGiTest {

    static final String TEST_BUNDLE_NAME = "ChannelTypesI18nTest.bundle"

    ChannelTypeProvider channelTypeProvider
    ThingTypeProvider thingTypeProvider

    @Before
    void setUp() {
        // get ONLY the XMLChannelTypeProvider
        channelTypeProvider = getService(ChannelTypeProvider, {ServiceReference serviceReference ->
            serviceReference.getBundle().getSymbolicName().contains("xml")})
        assertThat channelTypeProvider, is(notNullValue())
        thingTypeProvider = getService(ThingTypeProvider)
        assertThat thingTypeProvider, is(notNullValue())
    }

    @After
    void tearDown() {
        SyntheticBundleInstaller.uninstall(getBundleContext(), TEST_BUNDLE_NAME)
    }

    @Test
    void 'assert that channel types are translated correctly'() {
        def bundleContext = getBundleContext()
        Bundle bundle = SyntheticBundleInstaller.install(bundleContext, TEST_BUNDLE_NAME)
        assertThat bundle, is(notNullValue())

        def channelTypes = channelTypeProvider.getChannelTypes(null)
        def channelType1 = channelTypes.find( {it.UID.toString().equals("somebinding:channel-with-i18n")} )
        assertThat channelType1, is(not(null))
        assertThat channelType1.getLabel(), is(equalTo("Channel Label"))
        assertThat channelType1.getDescription(), is(equalTo("Channel Description"))

        def channelGroupTypes = channelTypeProvider.getChannelGroupTypes(null)
        def channelGroupType = channelGroupTypes.find( {it.UID.toString().equals("somebinding:channelgroup-with-i18n")} )
        assertThat channelGroupType, is(not(null))
        assertThat channelGroupType.getLabel(), is(equalTo("Channel Group Label"))
        assertThat channelGroupType.getDescription(), is(equalTo("Channel Group Description"))
    }

    @Test
    void 'assert that channel definitions are translated correctly'() {
        def bundleContext = getBundleContext()
        Bundle bundle = SyntheticBundleInstaller.install(bundleContext, TEST_BUNDLE_NAME)
        assertThat bundle, is(notNullValue())

        def thingType = thingTypeProvider.getThingTypes(null).find({it.UID.toString().equals("somebinding:something")})
        assertThat thingType, is(notNullValue())
        assertThat thingType.getChannelDefinitions().size(), is(2)

        def channelDefinition1 = thingType.getChannelDefinitions().find({it.getId().equals("channelPlain")})
        assertThat channelDefinition1.getLabel(), is(equalTo("Channel Plain Label"))
        assertThat channelDefinition1.getDescription(), is(equalTo("Channel Plain Description"))

        def channelDefinition2 = thingType.getChannelDefinitions().find({it.getId().equals("channelInplace")})
        assertThat channelDefinition2.getLabel(), is(equalTo("Channel Inplace Label"))
        assertThat channelDefinition2.getDescription(), is(equalTo("Channel Inplace Description"))
    }

}
