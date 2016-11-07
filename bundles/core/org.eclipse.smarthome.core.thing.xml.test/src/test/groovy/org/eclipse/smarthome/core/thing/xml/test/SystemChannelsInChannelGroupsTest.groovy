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
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID
import org.eclipse.smarthome.core.thing.type.ChannelTypeRegistry
import org.eclipse.smarthome.core.thing.type.ThingType
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.SyntheticBundleInstaller
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.framework.Bundle


/**
 * @author Simon Kaufmann - Initial contribution and API
 *
 */
class SystemChannelsInChannelGroupsTest extends OSGiTest {

    static final String SYSTEM_CHANNELS_IN_CHANNEL_GROUPS_BUNDLE_NAME = "SystemChannelsInChannelGroups.bundle"

    ThingTypeProvider thingTypeProvider
    ChannelTypeRegistry channelTypeRegistry


    @Before
    void setUp() {
        thingTypeProvider = getService(ThingTypeProvider)
        assertThat thingTypeProvider, is(notNullValue())

        channelTypeRegistry = getService(ChannelTypeRegistry)
        assertThat channelTypeRegistry, is(notNullValue())
    }

    @After
    void tearDown() {
        SyntheticBundleInstaller.uninstall(getBundleContext(), SYSTEM_CHANNELS_IN_CHANNEL_GROUPS_BUNDLE_NAME)
    }

    @Test
    void 'assert that System Channels used within channel groups are loaded and unloaded'() {
        def bundleContext = getBundleContext()
        def initialNumberOfThingTypes = thingTypeProvider.getThingTypes(null).size()
        def initialNumberOfChannelTypes = channelTypeRegistry.getChannelTypes().size()
        def initialNumberOfChannelGroupTypes = channelTypeRegistry.getChannelGroupTypes().size()

        // install test bundle
        Bundle bundle = SyntheticBundleInstaller.install(bundleContext, SYSTEM_CHANNELS_IN_CHANNEL_GROUPS_BUNDLE_NAME)
        assertThat bundle, is(notNullValue())

        def thingTypes = thingTypeProvider.getThingTypes(null)
        assertThat thingTypes.size(), is(initialNumberOfThingTypes + 1)
        assertThat channelTypeRegistry.getChannelTypes().size(), is(initialNumberOfChannelTypes + 1)
        assertThat channelTypeRegistry.getChannelGroupTypes().size(), is(initialNumberOfChannelGroupTypes + 1)

        // uninstall test bundle
        bundle.uninstall();
        assertThat bundle.state, is(Bundle.UNINSTALLED)

        assertThat thingTypeProvider.getThingTypes(null).size(), is(initialNumberOfThingTypes)
        assertThat channelTypeRegistry.getChannelTypes().size(), is(initialNumberOfChannelTypes)
        assertThat channelTypeRegistry.getChannelGroupTypes().size(), is(initialNumberOfChannelGroupTypes)

    }

    @Test
    void 'assert that Thing Types with system channels in channel groups have proper channel definitions'() {
        def bundleContext = getBundleContext()

        // install test bundle
        Bundle sysBundle = SyntheticBundleInstaller.install(bundleContext, SYSTEM_CHANNELS_IN_CHANNEL_GROUPS_BUNDLE_NAME)
        assertThat sysBundle, is(notNullValue())

        List<ThingType> thingTypes = thingTypeProvider.getThingTypes(null).findAll{it.getUID().getId().equals("wireless-router") }
        assertThat thingTypes.size(), is(1)

        def channelGroupTypes = channelTypeRegistry.getChannelGroupTypes()

        def channelGroup = channelGroupTypes.find{it.getUID().equals(new ChannelGroupTypeUID("SystemChannelsInChannelGroups:channelGroup"))}
        assertThat channelGroup, is(notNullValue())

        def channelDefs = channelGroup.getChannelDefinitions()

        def myChannel = channelDefs.findAll {
            it.id.equals("test") &&
                    it.getChannelTypeUID().getAsString().equals("system:my-channel") }

        def sigStr = channelDefs.findAll {
            it.id.equals("sigstr") &&
                    it.getChannelTypeUID().getAsString().equals("system:signal-strength") }

        def lowBat = channelDefs.findAll {
            it.id.equals("lowbat") &&
                    it.getChannelTypeUID().getAsString().equals("system:low-battery") }


        assertThat myChannel.size(), is(1)
        assertThat sigStr.size(), is(1)
        assertThat lowBat.size(), is(1)
    }

}