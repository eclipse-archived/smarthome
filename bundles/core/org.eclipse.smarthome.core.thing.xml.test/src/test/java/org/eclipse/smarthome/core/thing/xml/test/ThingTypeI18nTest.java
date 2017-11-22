/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.core.thing.xml.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.Locale;

import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupDefinition;
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
        SyntheticBundleInstaller.uninstall(bundleContext, TEST_BUNDLE_NAME);
    }

    @Test
    public void thingTypeShouldBeLocalized() throws Exception {
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
        assertThat(channelGroupType, is(notNullValue()));

        assertThat(channelGroupType.getLabel(), is("Wetterinformation mit Gruppe"));
        assertThat(channelGroupType.getDescription(), is("Wetterinformation mit Gruppe Beschreibung."));
    }

    @Test
    public void channelGroupsShouldBeLocalized() throws Exception {
        int initialNumberOfThingTypes = thingTypeProvider.getThingTypes(null).size();

        // install test bundle
        Bundle bundle = SyntheticBundleInstaller.install(bundleContext, TEST_BUNDLE_NAME);
        assertThat(bundle, is(notNullValue()));

        Collection<ThingType> thingTypes = thingTypeProvider.getThingTypes(Locale.GERMAN);
        assertThat(thingTypes.size(), is(initialNumberOfThingTypes + 2));

        ThingType weatherGroupType = thingTypes.stream()
                .filter(it -> it.toString().equals("yahooweather:weather-with-group")).findFirst().get();
        assertThat(weatherGroupType, is(notNullValue()));
        assertThat(weatherGroupType.getChannelGroupDefinitions().size(), is(2));

        ChannelGroupDefinition forecastTodayChannelGroupDefinition = weatherGroupType.getChannelGroupDefinitions()
                .stream().filter(it -> it.getId().equals("forecastToday")).findFirst().get();
        assertThat(forecastTodayChannelGroupDefinition, is(notNullValue()));

        assertThat(forecastTodayChannelGroupDefinition.getLabel(), is("Wettervorhersage heute"));
        assertThat(forecastTodayChannelGroupDefinition.getDescription(), is("Wettervorhersage für den heutigen Tag."));

        ChannelGroupDefinition forecastTomorrowChannelGroupDefinition = weatherGroupType.getChannelGroupDefinitions()
                .stream().filter(it -> it.getId().equals("forecastTomorrow")).findFirst().get();
        assertThat(forecastTomorrowChannelGroupDefinition, is(notNullValue()));

        assertThat(forecastTomorrowChannelGroupDefinition.getLabel(), is("Wettervorhersage morgen"));
        assertThat(forecastTomorrowChannelGroupDefinition.getDescription(),
                is("Wettervorhersage für den morgigen Tag."));
    }

    @Test
    public void channelTypeShouldBeLocalized() throws Exception {
        int initialNumberOfThingTypes = thingTypeProvider.getThingTypes(null).size();

        // install test bundle
        Bundle bundle = SyntheticBundleInstaller.install(bundleContext, TEST_BUNDLE_NAME);
        assertThat(bundle, is(notNullValue()));

        Collection<ThingType> thingTypes = thingTypeProvider.getThingTypes(Locale.GERMAN);
        assertThat(thingTypes.size(), is(initialNumberOfThingTypes + 2));

        ThingType weatherType = thingTypes.stream().filter(it -> it.toString().equals("yahooweather:weather"))
                .findFirst().get();
        assertThat(weatherType, is(notNullValue()));
        assertThat(weatherType.getChannelDefinitions().size(), is(2));

        ChannelType temperatureChannelType = TypeResolver.resolve(weatherType.getChannelDefinitions().stream()
                .filter(it -> it.getId().equals("temperature")).findFirst().get().getChannelTypeUID(), Locale.GERMAN);
        assertThat(temperatureChannelType, is(notNullValue()));

        assertThat(temperatureChannelType.getLabel(), is("Temperatur"));
        assertThat(temperatureChannelType.getDescription(),
                is("Aktuelle Temperatur in Grad Celsius (Metrisch) oder Fahrenheit (Imperial)."));
        assertThat(temperatureChannelType.getState().getPattern(), is("%d Grad Celsius"));
        assertThat(temperatureChannelType.getState().getOptions().get(0).getLabel(), is("Mein String"));
    }

    @Test
    public void channelsShouldBeLocalized() throws Exception {
        int initialNumberOfThingTypes = thingTypeProvider.getThingTypes(null).size();

        // install test bundle
        Bundle bundle = SyntheticBundleInstaller.install(bundleContext, TEST_BUNDLE_NAME);
        assertThat(bundle, is(notNullValue()));

        Collection<ThingType> thingTypes = thingTypeProvider.getThingTypes(Locale.GERMAN);
        assertThat(thingTypes.size(), is(initialNumberOfThingTypes + 2));

        ThingType weatherType = thingTypes.stream().filter(it -> it.toString().equals("yahooweather:weather"))
                .findFirst().get();
        assertThat(weatherType, is(notNullValue()));
        assertThat(weatherType.getChannelDefinitions().size(), is(2));

        ChannelDefinition temperatureChannelDefinition = weatherType.getChannelDefinitions().stream()
                .filter(it -> it.getId().equals("temperature")).findFirst().get();
        assertThat(temperatureChannelDefinition, is(notNullValue()));

        assertThat(temperatureChannelDefinition.getLabel(), is("Temperatur"));
        assertThat(temperatureChannelDefinition.getDescription(),
                is("Aktuelle Temperatur in Grad Celsius (Metrisch) oder Fahrenheit (Imperial)."));

        ChannelDefinition minTemperatureChannelDefinition = weatherType.getChannelDefinitions().stream()
                .filter(it -> it.getId().equals("minTemperature")).findFirst().get();
        assertThat(minTemperatureChannelDefinition, is(notNullValue()));

        assertThat(minTemperatureChannelDefinition.getLabel(), is("Min. Temperatur"));
        assertThat(minTemperatureChannelDefinition.getDescription(),
                is("Minimale Temperatur in Grad Celsius (Metrisch) oder Fahrenheit (Imperial)."));
    }

}
