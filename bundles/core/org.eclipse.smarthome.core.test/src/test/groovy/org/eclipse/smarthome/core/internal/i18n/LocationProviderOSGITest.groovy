/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal.i18n

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.i18n.LocationProvider
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Before
import org.junit.Test
import org.osgi.service.cm.ConfigurationAdmin

/**
 * The {@link LocationProviderOSGiTest} tests the basic functionality of the {@link LocationProvider} OSGi service.
 *
 * @author Stefan Triller - Initial contribution
 */
class LocationProviderOSGiTest extends OSGiTest {

    def static final LOCATION_ZERO = "0,0"
    def static final LOCATION_DARMSTADT = "49.876733,8.666809,1"
    def static final LOCATION_HAMBURG = "53.588231,9.920082,5"

    LocationProvider locationProvider
    ConfigurationAdmin configAdmin

    @Before
    void setup() {
        locationProvider = getService(LocationProvider)
        assertThat locationProvider, is(notNullValue())

        configAdmin = getService(ConfigurationAdmin)
        assertThat configAdmin, is(notNullValue())

        clearLocation()

        waitForAssert {
            def location = locationProvider.getLocation()
            assertThat location, is(notNullValue())
            assertThat location.toString(), is(LOCATION_ZERO)
        }
    }


    @Test
    void 'assert that configured location is provided'() {
        updateAndAssertLocation(createLocationProperties(LOCATION_DARMSTADT))
    }

    @Test
    void 'assert that locale can be updated using directly the modified operation'() {
        def cfg = createLocationProperties(LOCATION_HAMBURG)
        locationProvider.modified(cfg)

        assertLocation(cfg)
    }

    void updateAndAssertLocation(def cfg, def locale=Locale.ENGLISH) {
        updateConfig(cfg)
        assertLocation(cfg)
    }

    def updateConfig(def cfg) {
        def config = getConfig()
        def properties = config.getProperties()
        if (properties == null) {
            properties = new Properties()
        }
        cfg.each { k, v ->
            properties.put(k,  v)
        }
        config.update(properties)
    }

    def assertLocation(def cfg) {
        waitForAssert {
            def location = locationProvider.getLocation()
            assertThat location, is(notNullValue())
            assertThat location.toString(), is(cfg[locationProvider.LOCATION])
        }
    }

    def clearLocation() {
        def config = getConfig()
        config.update(createLocationProperties())
    }

    def createLocationProperties(def location = "0,0") {
        return [(locationProvider.LOCATION) : location] as Properties
    }

    def getConfig() {
        return configAdmin.getConfiguration("org.eclipse.smarthome.core.i18nprovider")
    }
}
