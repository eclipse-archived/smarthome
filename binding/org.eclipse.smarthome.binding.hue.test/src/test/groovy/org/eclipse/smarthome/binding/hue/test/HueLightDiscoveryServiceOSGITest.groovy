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
import java.lang.reflect.Field
import nl.q42.jue.FullLight
import nl.q42.jue.Light
import nl.q42.jue.HueBridge

import org.eclipse.smarthome.binding.hue.HueBindingConstants
import org.eclipse.smarthome.binding.hue.config.HueBridgeConfiguration
import org.eclipse.smarthome.binding.hue.config.HueLightConfiguration
import org.eclipse.smarthome.binding.hue.internal.factory.HueThingHandlerFactory
import org.eclipse.smarthome.binding.hue.internal.handler.HueBridgeHandler
import org.eclipse.smarthome.binding.hue.internal.discovery.HueLightDiscoveryService
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.config.discovery.DiscoveryListener
import org.eclipse.smarthome.config.discovery.DiscoveryResult
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag
import org.eclipse.smarthome.config.discovery.DiscoveryService
import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.ThingProvider
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder
import org.eclipse.smarthome.test.AsyncResultWrapper
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test


/**
 * Tests for {@link HueLightDiscoveryService}.
 *
 * @author Kai Kreuzer - Initial contribution
 */
class HueLightDiscoveryServiceOSGITest extends OSGiTest {

    HueThingHandlerFactory hueThingHandlerFactory
    DiscoveryListener discoveryListener
	ManagedThingProvider managedThingProvider
	Bridge hueBridge
	HueLightDiscoveryService discoveryService

    final ThingTypeUID BRIDGE_THING_TYPE_UID = new ThingTypeUID("hue", "bridge")
	final ThingUID BRIDGE_THING_UID = new ThingUID(BRIDGE_THING_TYPE_UID, "testBridge")

	
    @Before
    void setUp() {
        registerVolatileStorageService()
        managedThingProvider = getService(ThingProvider, ManagedThingProvider)
        assertThat managedThingProvider, is(notNullValue())

        HueBridgeHandler hueBridgeHandler = getService(ThingHandler, HueBridgeHandler)
        assertThat hueBridgeHandler, is(nullValue())

        Configuration configuration = new Configuration().with {
            put(HueBridgeConfiguration.IP_ADDRESS, "1.2.3.4")
            put(HueBridgeConfiguration.USER_NAME, "testUserName")
            put(HueBridgeConfiguration.SERIAL_NUMBER, "testSerialNumber")
            it
        }

        hueBridge = managedThingProvider.createThing(
                BRIDGE_THING_TYPE_UID,
                BRIDGE_THING_UID,
                null, configuration)

        assertThat hueBridge, is(notNullValue())

        // wait for HueBridgeHandler to be registered
        waitForAssert({
            hueBridgeHandler = getService(ThingHandler, HueBridgeHandler)
            assertThat hueBridgeHandler, is(notNullValue())
        }, 10000)

		discoveryService = getService(DiscoveryService, HueLightDiscoveryService)
		assertThat discoveryService, is(notNullValue())
    }

    @After
    void cleanUp() {
	managedThingProvider.remove(BRIDGE_THING_UID)
    }

    private void registerDiscoveryListener(DiscoveryListener discoveryListener) {
        unregisterCurrentDiscoveryListener()
        this.discoveryListener = discoveryListener
        discoveryService.addDiscoveryListener(this.discoveryListener)
    }

    private void unregisterCurrentDiscoveryListener() {
        if (this.discoveryListener != null) {
            discoveryService.removeDiscoveryListener(this.discoveryListener)
        }
    }
		
    @Test
    void 'assert hue light registration'() {
		FullLight light = FullLight.class.newInstance()
		light.id = "1"
		light.modelid = "LCT001"
		
        def AsyncResultWrapper<DiscoveryResult> resultWrapper = new AsyncResultWrapper<DiscoveryResult>()
        registerDiscoveryListener( [
            thingDiscovered: { DiscoveryService source, DiscoveryResult result ->
                resultWrapper.set(result)
            },
            thingRemoved: { DiscoveryService source, ThingUID thingId ->
            },
            discoveryFinished: { DiscoveryService source ->
            },
            discoveryErrorOccurred: { DiscoveryService source, Exception exception ->
            }
        ] as DiscoveryListener)

        discoveryService.onLightAdded(null, light)
        waitForAssert{assertTrue resultWrapper.isSet}

        resultWrapper.wrappedObject.with {
            assertThat flag, is (DiscoveryResultFlag.NEW)
            assertThat thingUID.toString(), is("hue:LCT001:testBridge:Light" + light.id)
            assertThat thingTypeUID, is (HueBindingConstants.THING_TYPE_LCT001)
            assertThat bridgeUID, is(hueBridge.getUID())
            assertThat properties.get(HueLightConfiguration.LIGHT_ID), is (light.id)
        }
    }
}
