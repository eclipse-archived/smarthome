/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeRegistry;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.TypeResolver;
import org.eclipse.smarthome.test.SyntheticBundleInstaller;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 * @author Ivan Iliev - Initial contribution
 *
 */
public class SystemWideChannelTypesTest extends JavaOSGiTest {

    private static final String SYSTEM_CHANNELS_BUNDLE_NAME = "SystemChannels.bundle";

    private static final String SYSTEM_CHANNELS_USER_BUNDLE_NAME = "SystemChannelsUser.bundle";

    private static final String SYSTEM_CHANNELS_WITHOUT_THING_TYPES_BUNDLE_NAME = "SystemChannelsNoThingTypes.bundle";

    private ThingTypeProvider thingTypeProvider;

    @Before
    public void setUp() {
        thingTypeProvider = getService(ThingTypeProvider.class);
        assertThat(thingTypeProvider, is(notNullValue()));
    }

    @After
    public void tearDown() throws Exception {
        SyntheticBundleInstaller.uninstall(bundleContext, SYSTEM_CHANNELS_BUNDLE_NAME);
        SyntheticBundleInstaller.uninstall(bundleContext, SYSTEM_CHANNELS_USER_BUNDLE_NAME);
        SyntheticBundleInstaller.uninstall(bundleContext, SYSTEM_CHANNELS_WITHOUT_THING_TYPES_BUNDLE_NAME);
    }

    @Test
    public void systemChannelsShouldLoadAndUnload() throws Exception {
        int initialNumberOfThingTypes = thingTypeProvider.getThingTypes(null).size();

        int initialNumberOfChannelTypes = getChannelTypes().size();

        // install test bundle
        Bundle bundle = SyntheticBundleInstaller.install(bundleContext, SYSTEM_CHANNELS_BUNDLE_NAME);
        assertThat(bundle, is(notNullValue()));

        Collection<ThingType> thingTypes = thingTypeProvider.getThingTypes(null);
        assertThat(thingTypes.size(), is(initialNumberOfThingTypes + 1));

        assertThat(getChannelTypes().size(), is(initialNumberOfChannelTypes + 1));

        // uninstall test bundle
        bundle.uninstall();
        assertThat(bundle.getState(), is(Bundle.UNINSTALLED));

        thingTypes = thingTypeProvider.getThingTypes(null);
        assertThat(thingTypes.size(), is(initialNumberOfThingTypes));

        assertThat(getChannelTypes().size(), is(initialNumberOfChannelTypes));
    }

    @Test
    public void systemChannelsShouldBeusedByOtherBinding() throws Exception {
        int initialNumberOfThingTypes = thingTypeProvider.getThingTypes(null).size();
        int initialNumberOfChannelTypes = getChannelTypes().size();

        // install test bundle
        Bundle sysBundle = SyntheticBundleInstaller.install(bundleContext, SYSTEM_CHANNELS_BUNDLE_NAME);
        assertThat(sysBundle, is(notNullValue()));

        Bundle sysUserBundle = SyntheticBundleInstaller.install(bundleContext, SYSTEM_CHANNELS_USER_BUNDLE_NAME);
        assertThat(sysUserBundle, is(notNullValue()));

        Collection<ThingType> thingTypes = thingTypeProvider.getThingTypes(null);
        assertThat(thingTypes.size(), is(initialNumberOfThingTypes + 2));

        assertThat(getChannelTypes().size(), is(initialNumberOfChannelTypes + 1));
    }

    @Test
    public void thingTyoesShouldHaveProperChannelDefinitions() throws Exception {
        // install test bundle
        Bundle sysBundle = SyntheticBundleInstaller.install(bundleContext, SYSTEM_CHANNELS_BUNDLE_NAME);
        assertThat(sysBundle, is(notNullValue()));

        ThingType wirelessRouterType = thingTypeProvider.getThingTypes(null).stream()
                .filter(it -> it.getUID().getAsString().equals("SystemChannels:wireless-router")).findFirst().get();
        assertThat(wirelessRouterType, is(notNullValue()));

        Collection<ChannelDefinition> channelDefs = wirelessRouterType.getChannelDefinitions();
        assertThat(channelDefs.size(), is(3));

        ChannelDefinition myChannel = channelDefs.stream().filter(
                it -> it.getId().equals("test") && it.getChannelTypeUID().getAsString().equals("system:my-channel"))
                .findFirst().get();
        assertThat(myChannel, is(notNullValue()));

        ChannelDefinition sigStr = channelDefs.stream().filter(it -> it.getId().equals("sigstr")
                && it.getChannelTypeUID().getAsString().equals("system:signal-strength")).findFirst().get();
        assertThat(sigStr, is(notNullValue()));

        ChannelDefinition lowBat = channelDefs.stream().filter(
                it -> it.getId().equals("lowbat") && it.getChannelTypeUID().getAsString().equals("system:low-battery"))
                .findFirst().get();
        assertThat(lowBat, is(notNullValue()));
    }

    @Test
    public void systemChannelsShouldBeAddedWithoutThingTypes() throws Exception {
        int initialNumberOfThingTypes = thingTypeProvider.getThingTypes(null).size();
        int initialNumberOfChannelTypes = getChannelTypes().size();

        // install test bundle
        Bundle sysBundle = SyntheticBundleInstaller.install(bundleContext,
                SYSTEM_CHANNELS_WITHOUT_THING_TYPES_BUNDLE_NAME);
        assertThat(sysBundle, is(notNullValue()));

        Collection<ThingType> thingTypes = thingTypeProvider.getThingTypes(null);
        assertThat(thingTypes.size(), is(initialNumberOfThingTypes));

        assertThat(getChannelTypes().size(), is(initialNumberOfChannelTypes + 1));

        // uninstall test bundle
        sysBundle.uninstall();
        assertThat(sysBundle.getState(), is(Bundle.UNINSTALLED));

        assertThat(getChannelTypes().size(), is(initialNumberOfChannelTypes));
    }

    @Test
    public void systemChannelsShouldTranslateProperly() throws Exception {
        int initialNumberOfThingTypes = thingTypeProvider.getThingTypes(null).size();

        // install test bundle
        Bundle sysBundle = SyntheticBundleInstaller.install(bundleContext, SYSTEM_CHANNELS_BUNDLE_NAME);
        assertThat(sysBundle, is(notNullValue()));

        Collection<ThingType> thingTypes = thingTypeProvider.getThingTypes(Locale.GERMAN);
        assertThat(thingTypes.size(), is(initialNumberOfThingTypes + 1));

        ThingType wirelessRouterType = thingTypes.stream()
                .filter(it -> it.getUID().getAsString().equals("SystemChannels:wireless-router")).findFirst().get();
        assertThat(wirelessRouterType, is(notNullValue()));

        List<ChannelDefinition> channelDefs = wirelessRouterType.getChannelDefinitions();
        assertThat(channelDefs.size(), is(3));

        ChannelDefinition myChannel = channelDefs.stream().filter(
                it -> it.getId().equals("test") && it.getChannelTypeUID().getAsString().equals("system:my-channel"))
                .findFirst().get();
        assertThat(myChannel, is(notNullValue()));

        ChannelDefinition sigStr = channelDefs.stream().filter(it -> it.getId().equals("sigstr")
                && it.getChannelTypeUID().getAsString().equals("system:signal-strength")).findFirst().get();
        assertThat(sigStr, is(notNullValue()));

        ChannelDefinition lowBat = channelDefs.stream().filter(
                it -> it.getId().equals("lowbat") && it.getChannelTypeUID().getAsString().equals("system:low-battery"))
                .findFirst().get();
        assertThat(lowBat, is(notNullValue()));

        assertThat(TypeResolver.resolve(myChannel.getChannelTypeUID(), Locale.GERMAN).getLabel(),
                is("Mein String My Channel"));
        assertThat(TypeResolver.resolve(myChannel.getChannelTypeUID(), Locale.GERMAN).getDescription(),
                is("Wetterinformation mit My Channel Type Beschreibung"));

        assertThat(myChannel.getLabel(), is("Mein String My Channel"));
        assertThat(myChannel.getDescription(), is("Wetterinformation mit My Channel Type Beschreibung"));

        assertThat(TypeResolver.resolve(sigStr.getChannelTypeUID(), Locale.GERMAN).getLabel(), is("Signalstärke"));

        assertThat(sigStr.getLabel(), is("Meine spezial Signalstärke"));
        assertThat(sigStr.getDescription(), is("Meine spezial Beschreibung für Signalstärke"));

        assertThat(TypeResolver.resolve(lowBat.getChannelTypeUID(), Locale.GERMAN).getLabel(),
                is("Niedriger Batteriestatus"));

        assertThat(lowBat.getLabel(), is("Niedriger Batteriestatus"));
        assertThat(lowBat.getDescription(), is(nullValue()));
    }

    private List<ChannelType> getChannelTypes() {
        return getService(ChannelTypeRegistry.class).getChannelTypes();
    }
}