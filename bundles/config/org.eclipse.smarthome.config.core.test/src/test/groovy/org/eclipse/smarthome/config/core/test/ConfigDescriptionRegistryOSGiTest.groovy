/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.config.core.ConfigDescription
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Before
import org.junit.Test

class ConfigDescriptionRegistryOSGiTest extends OSGiTest {

    ConfigDescriptionRegistry configDescriptionRegistry
    ConfigDescription configDescription
    ConfigDescriptionProvider configDescriptionProviderMock
    ConfigDescription configDescription1
    ConfigDescriptionProvider configDescriptionProviderMock1
    ConfigDescription configDescription2
    ConfigDescriptionProvider configDescriptionProviderMock2

    @Before
    void setUp() {
        configDescriptionRegistry = getService(ConfigDescriptionRegistry)
        ConfigDescriptionParameter param1 = new ConfigDescriptionParameter("param1", ConfigDescriptionParameter.Type.INTEGER)
        List<ConfigDescriptionParameter> pList1 = new ArrayList<ConfigDescriptionParameter>();
        pList1.add(param1);
        configDescription = new ConfigDescription(new URI("config:Dummy"), pList1)

        configDescriptionProviderMock = [
            getConfigDescriptions: {p -> [configDescription]},
            getConfigDescription: {p1,p2 -> configDescription }
        ] as ConfigDescriptionProvider

        configDescription1 = new ConfigDescription(new URI("config:Dummy1"))
        configDescriptionProviderMock1 = [
            getConfigDescriptions: {p -> [configDescription1]},
            getConfigDescription: {p1,p2 -> configDescription1 }
        ] as ConfigDescriptionProvider

        ConfigDescriptionParameter param2 = new ConfigDescriptionParameter("param2", ConfigDescriptionParameter.Type.INTEGER)
        List<ConfigDescriptionParameter> pList2 = new ArrayList<ConfigDescriptionParameter>();
        pList2.add(param2);
        configDescription2 = new ConfigDescription(new URI("config:Dummy"), pList2)
        configDescriptionProviderMock2 = [
            getConfigDescriptions: {p -> [configDescription2]},
            getConfigDescription: {p1,p2 -> configDescription2 }
        ] as ConfigDescriptionProvider
    }

    @Test
    void 'assert getConfigDescription returns according config description'() {

        registerService configDescriptionProviderMock

        def configDescription = configDescriptionRegistry.getConfigDescription(new URI("config:Dummy"))
        assertThat configDescription, is(not(null))
        assertThat configDescription.uri, is(equalTo(new URI("config:Dummy")))
    }

    @Test
    void 'assert ConfigDescriptionRegistry tracks registered ConfigDescriptionProvider'() {

        assertThat "Registery is empty to start", configDescriptionRegistry.getConfigDescriptions().size(), is(0)

        configDescriptionRegistry.addConfigDescriptionProvider(configDescriptionProviderMock)
        assertThat "Registery added first description", configDescriptionRegistry.getConfigDescriptions().size(), is(1)

        def configDescriptions = configDescriptionRegistry.getConfigDescriptions()
        assertThat configDescriptions[0].uri, is(equalTo(new URI("config:Dummy")))
        assertThat configDescriptions[0].toParametersMap().size(), is(1)
        assertThat configDescriptions[0].toParametersMap().get("param1"), not(null)

        configDescriptionRegistry.addConfigDescriptionProvider(configDescriptionProviderMock1)
        assertThat "Registery added second description", configDescriptionRegistry.getConfigDescriptions().size(), is(2)

        configDescriptionRegistry.removeConfigDescriptionProvider(configDescriptionProviderMock)
        assertThat "Registery removed first description", configDescriptionRegistry.getConfigDescriptions().size(), is(1)

        configDescriptionRegistry.removeConfigDescriptionProvider(configDescriptionProviderMock1)
        assertThat "Registery is empty to finish", configDescriptionRegistry.getConfigDescriptions().size(), is(0)
    }

    @Test
    void 'assert ConfigDescriptionRegistry merges multiple providers'() {

        assertThat "Registery is empty to start", configDescriptionRegistry.getConfigDescriptions().size(), is(0)

        configDescriptionRegistry.addConfigDescriptionProvider(configDescriptionProviderMock)
        assertThat "First description added ok", configDescriptionRegistry.getConfigDescriptions().size(), is(1)

        configDescriptionRegistry.addConfigDescriptionProvider(configDescriptionProviderMock2)
        assertThat "Second description merged ok", configDescriptionRegistry.getConfigDescriptions().size(), is(1)

        def configDescriptions = configDescriptionRegistry.getConfigDescriptions()
        assertThat "Config is found", configDescriptions[0].uri, is(equalTo(new URI("config:Dummy")))

        assertThat "Config contains both parameters", configDescriptions[0].getParameters().size(), is(2)
        assertThat "Config parameter 1 found", configDescriptions[0].getParameters().get(0).getName(), is(equalTo("param1"))
        assertThat "Config parameter 2 found", configDescriptions[0].getParameters().get(1).getName(), is(equalTo("param2"))

        configDescriptionRegistry.removeConfigDescriptionProvider(configDescriptionProviderMock)
        assertThat "First description removed ok", configDescriptionRegistry.getConfigDescriptions().size(), is(1)

        configDescriptionRegistry.removeConfigDescriptionProvider(configDescriptionProviderMock2)
        assertThat "Registery is empty to finish", configDescriptionRegistry.getConfigDescriptions().size(), is(0)
    }
}
