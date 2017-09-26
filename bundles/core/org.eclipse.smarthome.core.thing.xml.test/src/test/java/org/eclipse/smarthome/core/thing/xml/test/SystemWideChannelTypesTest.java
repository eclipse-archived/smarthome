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
        List<ThingType> thingTypes = thingTypeProvider.getThingTypes(null).stream()
                .filter(it -> it.getUID().getId().equals("wireless-router")).collect(Collectors.toList());

        assertThat(thingTypes.size(), is(1));

        Collection<ChannelDefinition> channelDefs = thingTypes.get(0).getChannelDefinitions();

        assertThat(channelDefs.size(), is(3));

        List<ChannelDefinition> myChannel = channelDefs.stream().filter(
                it -> it.getId().equals("test") && it.getChannelTypeUID().getAsString().equals("system:my-channel"))
                .collect(Collectors.toList());

        List<ChannelDefinition> sigStr = channelDefs.stream()
                .filter(it -> it.getId().equals("sigstr")
                        && it.getChannelTypeUID().getAsString().equals("system:signal-strength"))
                .collect(Collectors.toList());

        List<ChannelDefinition> lowBat = channelDefs.stream().filter(
                it -> it.getId().equals("lowbat") && it.getChannelTypeUID().getAsString().equals("system:low-battery"))
                .collect(Collectors.toList());

        assertThat(myChannel.size(), is(1));
        assertThat(sigStr.size(), is(1));
        assertThat(lowBat.size(), is(1));
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

        List<ThingType> thingsFiltered = thingTypes.stream()
                .filter(it -> it.getUID().getAsString().equals("SystemChannels:wireless-router"))
                .collect(Collectors.toList());

        assertThat(thingsFiltered.size(), is(1));
        List<ChannelDefinition> channelDefs = thingsFiltered.get(0).getChannelDefinitions();

        List<ChannelDefinition> myChannel = channelDefs.stream().filter(
                it -> it.getId().equals("test") && it.getChannelTypeUID().getAsString().equals("system:my-channel"))
                .collect(Collectors.toList());

        List<ChannelDefinition> sigStr = channelDefs.stream()
                .filter(it -> it.getId().equals("sigstr")
                        && it.getChannelTypeUID().getAsString().equals("system:signal-strength"))
                .collect(Collectors.toList());

        List<ChannelDefinition> lowBat = channelDefs.stream().filter(
                it -> it.getId().equals("lowbat") && it.getChannelTypeUID().getAsString().equals("system:low-battery"))
                .collect(Collectors.toList());

        assertThat(myChannel.size(), is(1));
        assertThat(sigStr.size(), is(1));
        assertThat(lowBat.size(), is(1));

        assertThat(TypeResolver.resolve(myChannel.get(0).getChannelTypeUID(), Locale.GERMAN).getLabel(),
                is("Mein String My Channel"));
        assertThat(TypeResolver.resolve(myChannel.get(0).getChannelTypeUID(), Locale.GERMAN).getDescription(),
                is("Wetterinformation mit My Channel Type Beschreibung"));

        assertThat(TypeResolver.resolve(sigStr.get(0).getChannelTypeUID(), Locale.GERMAN).getLabel(),
                is(not("Mein String Signal Strength")));
        assertThat(TypeResolver.resolve(sigStr.get(0).getChannelTypeUID(), Locale.GERMAN).getDescription(),
                is(not("Wetterinformation mit Signal Strength Channel Type Beschreibung")));

        assertThat(TypeResolver.resolve(sigStr.get(0).getChannelTypeUID(), Locale.GERMAN).getLabel(),
                is("Signalstärke"));

        assertThat(TypeResolver.resolve(lowBat.get(0).getChannelTypeUID(), Locale.GERMAN).getLabel(),
                is("Niedriger Batteriestatus"));
    }

    private List<ChannelType> getChannelTypes() {
        return getService(ChannelTypeRegistry.class).getChannelTypes();
    }
}