/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import static org.eclipse.smarthome.binding.hue.HueBindingConstants.*;
import org.eclipse.smarthome.binding.hue.handler.HueBridgeHandler
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.ThingProvider
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Before
import org.junit.Test


/**
 * Tests for {@link HueBridgeHandler}.
 *
 * @author Oliver Libutzki - Initial contribution
 * @author Michael Grammling - Initial contribution
 */
class HueBridgeHandlerOSGiTest extends OSGiTest {

    final ThingTypeUID BRIDGE_THING_TYPE_UID = new ThingTypeUID("hue", "bridge")

    ManagedThingProvider managedThingProvider


    @Before
    void setUp() {
        registerVolatileStorageService()
        managedThingProvider = getService(ThingProvider, ManagedThingProvider)
        assertThat managedThingProvider, is(notNullValue())
    }

    @Test
    void 'assert that HueBridgeHandler is registered and unregistered'() {
        HueBridgeHandler hueBridgeHandler = getService(ThingHandler, HueBridgeHandler)
        assertThat hueBridgeHandler, is(nullValue())

        Configuration configuration = new Configuration().with {
            put(HOST, "1.2.3.4")
            put(USER_NAME, "testUserName")
            put(SERIAL_NUMBER, "testSerialNumber")
            it
        }

        Bridge hueBridge = managedThingProvider.createThing(
                BRIDGE_THING_TYPE_UID,
                new ThingUID(BRIDGE_THING_TYPE_UID, "testBridge"),
                null, configuration)

        assertThat hueBridge, is(notNullValue())

        // wait for HueBridgeHandler to be registered
        waitForAssert({
            hueBridgeHandler = getService(ThingHandler, HueBridgeHandler)
            assertThat hueBridgeHandler, is(notNullValue())
        }, 10000)

        managedThingProvider.remove(hueBridge.getUID())

        // wait for HueBridgeHandler to be unregistered
        waitForAssert({
            hueBridgeHandler = getService(ThingHandler, HueBridgeHandler)
            assertThat hueBridgeHandler, is(nullValue())
        }, 10000)
    }

}
