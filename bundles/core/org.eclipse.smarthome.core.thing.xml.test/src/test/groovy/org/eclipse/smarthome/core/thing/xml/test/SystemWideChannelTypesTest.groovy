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
import org.eclipse.smarthome.core.thing.type.ChannelType
import org.eclipse.smarthome.core.thing.type.ChannelTypeRegistry
import org.eclipse.smarthome.core.thing.type.TypeResolver
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.SyntheticBundleInstaller
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.framework.Bundle

/**
 * @author Ivan Iliev - Initial contribution
 *
 */
class SystemWideChannelTypesTest extends OSGiTest {

    static final String SYSTEM_CHANNELS_BUNDLE_NAME = "SystemChannels.bundle"

    static final String SYSTEM_CHANNELS_USER_BUNDLE_NAME = "SystemChannelsUser.bundle"

    static final String SYSTEM_CHANNELS_WITHOUT_THING_TYPES_BUNDLE_NAME = "SystemChannelsNoThingTypes.bundle"

    ThingTypeProvider thingTypeProvider

    @Before
    void setUp() {
        thingTypeProvider = getService(ThingTypeProvider)
        assertThat thingTypeProvider, is(notNullValue())
    }

    @After
    void tearDown() {
        SyntheticBundleInstaller.uninstall(getBundleContext(), SYSTEM_CHANNELS_BUNDLE_NAME)
        SyntheticBundleInstaller.uninstall(getBundleContext(), SYSTEM_CHANNELS_USER_BUNDLE_NAME)
        SyntheticBundleInstaller.uninstall(getBundleContext(), SYSTEM_CHANNELS_WITHOUT_THING_TYPES_BUNDLE_NAME)
    }

    @Test
    void 'assert that System Channels are loaded and unloaded'() {
        def bundleContext = getBundleContext()
        def initialNumberOfThingTypes = thingTypeProvider.getThingTypes(null).size()

        def initialNumberOfChannelTypes = getChannelTypes().size()

        // install test bundle
        Bundle bundle = SyntheticBundleInstaller.install(bundleContext, SYSTEM_CHANNELS_BUNDLE_NAME)
        assertThat bundle, is(notNullValue())

        def thingTypes = thingTypeProvider.getThingTypes(null)
        assertThat thingTypes.size(), is(initialNumberOfThingTypes + 1)

        assertThat getChannelTypes().size(), is(initialNumberOfChannelTypes + 1)

        // uninstall test bundle
        bundle.uninstall();
        assertThat bundle.state, is(Bundle.UNINSTALLED)

        thingTypes = thingTypeProvider.getThingTypes(null)
        assertThat thingTypes.size(), is(initialNumberOfThingTypes)

        assertThat getChannelTypes().size(), is(initialNumberOfChannelTypes)

    }

    @Test
    void 'assert that System Channels are used by other binding'() {
        def bundleContext = getBundleContext()
        def initialNumberOfThingTypes = thingTypeProvider.getThingTypes(null).size()
        def initialNumberOfChannelTypes = getChannelTypes().size()


        // install test bundle
        Bundle sysBundle = SyntheticBundleInstaller.install(bundleContext, SYSTEM_CHANNELS_BUNDLE_NAME)
        assertThat sysBundle, is(notNullValue())

        Bundle sysUserBundle = SyntheticBundleInstaller.install(bundleContext, SYSTEM_CHANNELS_USER_BUNDLE_NAME)
        assertThat sysUserBundle, is(notNullValue())

        def thingTypes = thingTypeProvider.getThingTypes(null)
        assertThat thingTypes.size(), is(initialNumberOfThingTypes + 2)

        assertThat getChannelTypes().size(), is(initialNumberOfChannelTypes + 1)

    }

    @Test
    void 'assert that Thing Types have proper channel definitions'() {
        def bundleContext = getBundleContext()
        def initialNumberOfThingTypes = thingTypeProvider.getThingTypes(null).size()
        def initialNumberOfChannelTypes = getChannelTypes().size()


        // install test bundle
        Bundle sysBundle = SyntheticBundleInstaller.install(bundleContext, SYSTEM_CHANNELS_BUNDLE_NAME)
        assertThat sysBundle, is(notNullValue())

        def thingTypes = thingTypeProvider.getThingTypes(null).findAll{it.getUID().getId().equals("wireless-router") }

        assertThat thingTypes.size(), is(1)

        def channelDefs = thingTypes[0].getChannelDefinitions();

        assertThat channelDefs.size(),is(3)

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


    @Test
    void 'assert that System Channels are added without thing types'() {
        def bundleContext = getBundleContext()
        def initialNumberOfThingTypes = thingTypeProvider.getThingTypes(null).size()
        def initialNumberOfChannelTypes = getChannelTypes().size()


        // install test bundle
        Bundle sysBundle = SyntheticBundleInstaller.install(bundleContext, SYSTEM_CHANNELS_WITHOUT_THING_TYPES_BUNDLE_NAME)
        assertThat sysBundle, is(notNullValue())

        def thingTypes = thingTypeProvider.getThingTypes(null)
        assertThat thingTypes.size(), is(initialNumberOfThingTypes)

        assertThat getChannelTypes().size(), is(initialNumberOfChannelTypes + 1)

        // uninstall test bundle
        sysBundle.uninstall();
        assertThat sysBundle.state, is(Bundle.UNINSTALLED)

        assertThat getChannelTypes().size(), is(initialNumberOfChannelTypes)

    }

    @Test
    void 'assert that i18n is working for system channels'() {
        def bundleContext = getBundleContext()
        def initialNumberOfThingTypes = thingTypeProvider.getThingTypes(null).size()
        def initialNumberOfChannelTypes = getChannelTypes().size()


        // install test bundle
        Bundle sysBundle = SyntheticBundleInstaller.install(bundleContext, SYSTEM_CHANNELS_BUNDLE_NAME)
        assertThat sysBundle, is(notNullValue())


        def thingTypes = thingTypeProvider.getThingTypes(Locale.GERMAN);
        assertThat thingTypes.size(), is(initialNumberOfThingTypes + 1)

        def thingsFiltered = thingTypes.findAll { it.UID.getAsString().equals("SystemChannels:wireless-router") }

        assertThat thingsFiltered.size(), is(1)

        def channelDefs = thingsFiltered[0].getChannelDefinitions();

        def myChannel = channelDefs.findAll {
            it.id.equals("test") &&
                    it.getChannelTypeUID().getAsString().equals("system:my-channel") }

        def sigStr = channelDefs.findAll {
            it.id.equals("sigstr") &&
                    it.getChannelTypeUID().getAsString().equals("system:signal-strength") }


        assertThat myChannel.size(), is(1)
        assertThat sigStr.size(), is(1)

        assertThat TypeResolver.resolve(myChannel[0].channelTypeUID, Locale.GERMAN).getLabel(), is("Mein String My Channel")
        assertThat TypeResolver.resolve(myChannel[0].channelTypeUID, Locale.GERMAN).getDescription(), is("Wetterinformation mit My Channel Type Beschreibung")
    }

    List<ChannelType> getChannelTypes() {
        return getService(ChannelTypeRegistry).getChannelTypes()
    }
}