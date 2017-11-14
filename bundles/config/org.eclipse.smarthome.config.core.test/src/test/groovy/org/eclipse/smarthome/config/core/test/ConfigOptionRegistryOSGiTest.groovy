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
import org.eclipse.smarthome.config.core.ConfigOptionProvider
import org.eclipse.smarthome.config.core.ParameterOption
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Before
import org.junit.Test

/**
 * Test the ConfigOptionRegistry
 *
 * @author Chris Jackson
 *
 */
class ConfigOptionRegistryOSGiTest extends OSGiTest {

    ConfigDescriptionRegistry configDescriptionRegistry
    ConfigDescription configDescription
    ConfigDescriptionProvider configDescriptionProviderMock
    ConfigOptionProvider configOptionsProviderMock
    ParameterOption parameterOption

    @Before
    void setUp() {
        // Register config registry
        configDescriptionRegistry = getService(ConfigDescriptionRegistry)
        ConfigDescriptionParameter parm1 = new ConfigDescriptionParameter("Parm1", ConfigDescriptionParameter.Type.INTEGER)
        List<ConfigDescriptionParameter> pList1 = new ArrayList<ConfigDescriptionParameter>();
        pList1.add(parm1);
        configDescription = new ConfigDescription(new URI("config:Dummy"), pList1)

        // Create config option list
        List<ParameterOption> oList1 = new ArrayList<ConfigDescriptionParameter>();
        parameterOption = new ParameterOption("Option1", "Option1")
        oList1.add(parameterOption);
        parameterOption = new ParameterOption("Option2", "Option2")
        oList1.add(parameterOption);

        configOptionsProviderMock = [
            getParameterOptions: {p1, p2, p3 -> oList1}
        ] as ConfigOptionProvider

        configDescriptionProviderMock = [
            getConfigDescriptions: {p -> [configDescription]},
            getConfigDescription: {p1,p2 -> configDescription }
        ] as ConfigDescriptionProvider
    }

    @Test
    void 'assert ConfigDescriptionRegistry merges options'() {

        assertThat "Registery is empty to start", configDescriptionRegistry.getConfigDescriptions().size(), is(0)

        configDescriptionRegistry.addConfigDescriptionProvider(configDescriptionProviderMock)
        assertThat "Config description added ok", configDescriptionRegistry.getConfigDescriptions().size(), is(1)

        configDescriptionRegistry.addConfigOptionProvider(configOptionsProviderMock)

        def configDescriptions = configDescriptionRegistry.getConfigDescription(new URI("config:Dummy"))
        assertThat "Config is found", configDescriptions.uri, is(equalTo(new URI("config:Dummy")))

        assertThat "Config contains parameter", configDescriptions.getParameters().size(), is(1)
        assertThat "Config parameter found", configDescriptions.getParameters().get(0).getName(), is(equalTo("Parm1"))
        assertThat "Config parameter contains options", configDescriptions.getParameters().get(0).getOptions().size(), is(2)

        configDescriptionRegistry.removeConfigOptionProvider(configOptionsProviderMock)

        configDescriptionRegistry.removeConfigDescriptionProvider(configDescriptionProviderMock)
        assertThat "Description registery is empty to finish", configDescriptionRegistry.getConfigDescriptions().size(), is(0)
    }
}
