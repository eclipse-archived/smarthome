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
import java.util.Locale;

import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.TypeResolver;
import org.eclipse.smarthome.test.SyntheticBundleInstaller;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class ThingTypeI18nTest extends JavaOSGiTest {

    private static final String TEST_BUNDLE_NAME = "yahooweather.bundle";

    private ThingTypeProvider thingTypeProvider;

    @Before
    public void setUp() {
        thingTypeProvider = getService(ThingTypeProvider.class);
        assertThat(thingTypeProvider, is(notNullValue()));
    }

    @After
    public void tearDown() throws Exception {
        SyntheticBundleInstaller.uninstall(getBundleContext(), TEST_BUNDLE_NAME);
    }

    @Test
    public void thingTypeShouldBeLocalized() throws Exception {
        BundleContext bundleContext = getBundleContext();
        int initialNumberOfThingTypes = thingTypeProvider.getThingTypes(null).size();

        // install test bundle
        Bundle bundle = SyntheticBundleInstaller.install(bundleContext, TEST_BUNDLE_NAME);
        assertThat(bundle, is(notNullValue()));

        Collection<ThingType> thingTypes = thingTypeProvider.getThingTypes(Locale.GERMAN);
        assertThat(thingTypes.size(), is(initialNumberOfThingTypes + 2));

        ThingType weatherType = thingTypes.stream().filter(it -> it.toString().equals("yahooweather:weather"))
                .findFirst().get();
        assertThat(weatherType, is(notNullValue()));
        assertThat(weatherType.getLabel(), is("Wetterinformation"));
        assertThat(weatherType.getDescription(), is("Stellt verschiedene Wetterdaten vom yahoo Wetterdienst bereit"));
    }

    @Test
    public void channelGroupTypeShouldBeLocalized() throws Exception {

        BundleContext bundleContext = getBundleContext();
        int initialNumberOfThingTypes = thingTypeProvider.getThingTypes(null).size();

        // install test bundle
        Bundle bundle = SyntheticBundleInstaller.install(bundleContext, TEST_BUNDLE_NAME);
        assertThat(bundle, is(notNullValue()));

        Collection<ThingType> thingTypes = thingTypeProvider.getThingTypes(Locale.GERMAN);
        assertThat(thingTypes.size(), is(initialNumberOfThingTypes + 2));

        ThingType weatherGroupType = thingTypes.stream()
                .filter(it -> it.toString().equals("yahooweather:weather-with-group")).findFirst().get();

        assertThat(weatherGroupType, is(notNullValue()));
        ChannelGroupType channelGroupType = TypeResolver
                .resolve(weatherGroupType.getChannelGroupDefinitions().get(0).getTypeUID(), Locale.GERMAN);
        assertThat(channelGroupType.getLabel(), is("Wetterinformation mit Gruppe"));
        assertThat(channelGroupType.getDescription(), is("Wetterinformation mit Gruppe Beschreibung"));
    }

    @Test
    public void channelTypeShouldBeLocalized() throws Exception {

        BundleContext bundleContext = getBundleContext();
        int initialNumberOfThingTypes = thingTypeProvider.getThingTypes(null).size();

        // install test bundle
        Bundle bundle = SyntheticBundleInstaller.install(bundleContext, TEST_BUNDLE_NAME);
        assertThat(bundle, is(notNullValue()));

        Collection<ThingType> thingTypes = thingTypeProvider.getThingTypes(Locale.GERMAN);
        assertThat(thingTypes.size(), is(initialNumberOfThingTypes + 2));

        ThingType weatherType = thingTypes.stream().filter(it -> it.toString().equals("yahooweather:weather"))
                .findFirst().get();
        ChannelType temperatureChannelType = TypeResolver.resolve(weatherType.getChannelDefinitions().stream()
                .filter(it -> it.getId().equals("temperature")).findFirst().get().getChannelTypeUID(), Locale.GERMAN);

        assertThat(temperatureChannelType.getLabel(), is("Temperatur"));
        assertThat(temperatureChannelType.getDescription(), is("Temperaturwert"));
        assertThat(temperatureChannelType.getState().getPattern(), is("%d Grad Celsius"));
        assertThat(temperatureChannelType.getState().getOptions().get(0).getLabel(), is("Mein String"));
    }

}
