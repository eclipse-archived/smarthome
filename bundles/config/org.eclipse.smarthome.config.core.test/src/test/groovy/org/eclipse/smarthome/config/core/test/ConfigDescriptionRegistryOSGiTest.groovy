package org.eclipse.smarthome.config.core.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.config.core.ConfigDescription
import org.eclipse.smarthome.config.core.ConfigDescriptionListener
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
        configDescription = new ConfigDescription("Dummy")
        configDescriptionProviderMock = [
            addConfigDescriptionListener: { def ConfigDescriptionListener listener ->
                listener.configDescriptionAdded(configDescription)
            },
            removeConfigDescriptionListener: { def ConfigDescriptionListener listener ->
                listener.configDescriptionRemoved(configDescription)
            }
        ] as ConfigDescriptionProvider
    }

    @Test
    void 'assert ConfigDescriptionRegistry tracks registered ConfigDescriptionProvider'() {

        assertThat configDescriptionRegistry.getConfigDescriptions().size(), is(0)

        registerService configDescriptionProviderMock

        def configDescriptions = configDescriptionRegistry.getConfigDescriptions()
        assertThat configDescriptions.size(), is(1)
        assertThat configDescriptions[0].uri, is(equalTo("Dummy"))

        unregisterService configDescriptionProviderMock

        assertThat configDescriptionRegistry.getConfigDescriptions().size(), is(0)
    }

    @Test
    void 'assert getConfigDescription returns according config description'() {

        registerService configDescriptionProviderMock

        def configDescription = configDescriptionRegistry.getConfigDescription("Dummy")
        assertThat configDescription, is(not(null))
        assertThat configDescription.uri, is(equalTo("Dummy"))
    }
}
