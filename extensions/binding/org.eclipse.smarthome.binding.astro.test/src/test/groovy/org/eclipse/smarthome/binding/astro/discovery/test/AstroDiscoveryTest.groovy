/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.astro.discovery.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.binding.astro.AstroBindingConstants
import org.eclipse.smarthome.binding.astro.discovery.AstroDiscoveryService
import org.eclipse.smarthome.binding.astro.test.AstroOSGiTest
import org.eclipse.smarthome.config.discovery.DiscoveryListener
import org.eclipse.smarthome.config.discovery.DiscoveryResult
import org.eclipse.smarthome.config.discovery.DiscoveryService
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * OSGi tests for the {@link AstroDiscoveryService}
 *
 * @author Petar Valchev - Initial implementation
 * @author Svilen Valkanov - Replaced header and combined tests for sun and moon discovery into single test
 */
class AstroDiscoveryTest extends AstroOSGiTest {

    private final String sunThingUID = "$AstroBindingConstants.BINDING_ID:$AstroBindingConstants.SUN:$AstroBindingConstants.LOCAL"
    private final String moonThingUID = "$AstroBindingConstants.BINDING_ID:$AstroBindingConstants.MOON:$AstroBindingConstants.LOCAL"

    private boolean isSunThingDiscovered = false
    private boolean isMoonThingDiscovered = false

    AstroDiscoveryService discoveryService
    // a listener for discovery events
    DiscoveryListener discoveryListenerMock

    @Before
    public void setUp(){
        discoveryListenerMock = [
            thingDiscovered: {DiscoveryService source, DiscoveryResult result ->
                switch(result.getThingUID().toString()) {
                    case (sunThingUID) :
                        isSunThingDiscovered = true
                        break
                    case(moonThingUID) :
                        isMoonThingDiscovered = true
                        break
                }

            }
        ] as DiscoveryListener

        registerService(discoveryListenerMock, DiscoveryListener.class.getName())

        discoveryService = getService(DiscoveryService, AstroDiscoveryService)
        assertThat discoveryService, is(notNullValue())

        discoveryService.addDiscoveryListener(discoveryListenerMock)
    }

    @After
    public void tearDown() {
        isSunThingDiscovered = false
        isMoonThingDiscovered = false
    }

    @Test
    public void 'sun and moon things are discovered'(){
        assertDiscoveredThing()
    }

    private void assertDiscoveredThing(){
        discoveryService.startScan()

        waitForAssert{
            assertThat isSunThingDiscovered, is(true)
            assertThat isMoonThingDiscovered, is(true)
        }
    }
}
