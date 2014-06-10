/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.binding.hue.config.HueBridgeConfiguration
import org.eclipse.smarthome.binding.hue.internal.HueThingTypeProvider
import org.eclipse.smarthome.binding.hue.internal.handler.HueBridgeHandler
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingProvider
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Before
import org.junit.Test


/**
 * Tests for {@link HueBridgeHandler}.
 *
 * @author Oliver Libutzki - Initial contribution
 */
class HueBridgeHandlerOSGiTest extends OSGiTest {

    ManagedThingProvider managedThingProvider

    @Before
    void setUp() {
        managedThingProvider = getService(ThingProvider, ManagedThingProvider)
        assertThat managedThingProvider, is(notNullValue())
    }

    @Test
    void 'assert that HueBridgeHandler is registered and unregistered'() {
        HueBridgeHandler hueBridgeHandler = getService(ThingHandler, HueBridgeHandler)
        assertThat hueBridgeHandler, is(nullValue())
        Configuration configuration = new Configuration().with {
            put(HueBridgeConfiguration.IP_ADDRESS, "1.2.3.4")
            put(HueBridgeConfiguration.USER_NAME, "testUserName")
            put(HueBridgeConfiguration.BRIDGE_SERIAL_NUMBER, "testSerialNumber")
            it
        }
        Thing hueBridge = managedThingProvider.createThing(HueThingTypeProvider.BRIDGE_THING_TYPE, new ThingUID(HueThingTypeProvider.BRIDGE_THING_TYPE.getUID(), "testBridge"), null, configuration)
        assertThat hueBridge, is(notNullValue())

        // wait for HueBridgeHandler to be registered
        waitForAssert({
            hueBridgeHandler = getService(ThingHandler, HueBridgeHandler)
            assertThat hueBridgeHandler, is(notNullValue())
        }, 10000)

        managedThingProvider.removeThing(hueBridge.getUID())
        hueBridgeHandler = getService(ThingHandler, HueBridgeHandler)
        // wait for HueBridgeHandler to be unregistered
        waitForAssert({
            hueBridgeHandler = getService(ThingHandler, HueBridgeHandler)
            assertThat hueBridgeHandler, is(nullValue())
        }, 10000)
    }

}
