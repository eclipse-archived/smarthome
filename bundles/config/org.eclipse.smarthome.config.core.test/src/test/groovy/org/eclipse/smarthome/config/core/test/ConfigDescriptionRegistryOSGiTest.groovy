/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
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
import org.eclipse.smarthome.config.core.ConfigDescriptionsChangeListener
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Before
import org.junit.Test

class ConfigDescriptionRegistryOSGiTest extends OSGiTest {

    ConfigDescriptionRegistry configDescriptionRegistry
    ConfigDescription configDescription
    ConfigDescriptionProvider configDescriptionProviderMock

    @Before
    void setUp() {
        configDescriptionRegistry = getService(ConfigDescriptionRegistry)
        configDescription = new ConfigDescription(new URI("Dummy"))
        configDescriptionProviderMock = [
            addConfigDescriptionsChangeListener: { def ConfigDescriptionsChangeListener listener ->
                listener.configDescriptionAdded(configDescriptionProviderMock, configDescription)
            },
            removeConfigDescriptionsChangeListener: { def ConfigDescriptionsChangeListener listener ->
                listener.configDescriptionRemoved(configDescriptionProviderMock, configDescription)
            },
            getConfigDescriptions: {
                -> [ configDescription ]
            }
        ] as ConfigDescriptionProvider
    }

    @Test
    void 'assert ConfigDescriptionRegistry tracks registered ConfigDescriptionProvider'() {

        assertThat configDescriptionRegistry.getConfigDescriptions().size(), is(0)

        registerService configDescriptionProviderMock

        def configDescriptions = configDescriptionRegistry.getConfigDescriptions()
        assertThat configDescriptions.size(), is(1)
        assertThat configDescriptions[0].uri, is(equalTo(new URI("Dummy")))

        unregisterService configDescriptionProviderMock

        assertThat configDescriptionRegistry.getConfigDescriptions().size(), is(0)
    }

    @Test
    void 'assert getConfigDescription returns according config description'() {

        registerService configDescriptionProviderMock

        def configDescription = configDescriptionRegistry.getConfigDescription(new URI("Dummy"))
        assertThat configDescription, is(not(null))
        assertThat configDescription.uri, is(equalTo(new URI("Dummy")))
    }
}
