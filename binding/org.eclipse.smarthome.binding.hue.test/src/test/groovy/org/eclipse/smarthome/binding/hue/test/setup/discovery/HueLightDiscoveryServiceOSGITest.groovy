/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.test.setup.discovery

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.binding.hue.internal.HueThingTypeProvider
import org.eclipse.smarthome.binding.hue.internal.factory.HueThingHandlerFactory
import org.eclipse.smarthome.binding.hue.internal.setup.HueLightContextKey
import org.eclipse.smarthome.binding.hue.internal.setup.discovery.bulb.HueLightDiscoveryService
import org.eclipse.smarthome.config.discovery.DiscoveryListener
import org.eclipse.smarthome.config.discovery.DiscoveryResult
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag
import org.eclipse.smarthome.config.discovery.DiscoveryService
import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder
import org.eclipse.smarthome.test.AsyncResultWrapper
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test



/**
 * Tests for {@link HueLightDiscoveryService}.
 * 
 * @author Oliver Libutzki - Initial contribution
 */
class HueLightDiscoveryServiceOSGITest extends OSGiTest {

    HueLightDiscoveryService hueLightDiscoveryService
    HueThingHandlerFactory hueThingHandlerFactory
    DiscoveryListener discoveryListener


    private final static String IP_ADDRESS = "UPnP.device.IP"
    private final static String PORT = "UPnP.device.PORT"

    @Before
    void setUp() {
        def discoveryService = getService(DiscoveryService, HueLightDiscoveryService)
        assertThat discoveryService, is(notNullValue())
        hueLightDiscoveryService = discoveryService

        //		def thingHandlerFactory = getService(ThingHandlerFactory, HueThingHandlerFactory)
        //		assertThat thingHandlerFactory, is(notNullValue())
        //		hueThingHandlerFactory = thingHandlerFactory


        hueLightDiscoveryService.autoDiscoveryEnabled = true
        unregisterCurrentDiscoveryListener()
    }

    @After
    void cleanUp() {
        unregisterCurrentDiscoveryListener()
    }

    private void registerDiscoveryListener(DiscoveryListener discoveryListener) {
        unregisterCurrentDiscoveryListener()
        this.discoveryListener = discoveryListener
        hueLightDiscoveryService.addDiscoveryListener(this.discoveryListener)
    }

    private void unregisterCurrentDiscoveryListener() {
        if (this.discoveryListener != null) {
            hueLightDiscoveryService.removeDiscoveryListener(this.discoveryListener)
        }
    }

    @Test
    void 'assert hue light registration'() {

        Bridge bridge = BridgeBuilder
                .create(HueThingTypeProvider.BRIDGE_THING_TYPE.getUID(), "testBridge").build()


        def lightId = "1"
        def lightName = "Light 1"

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

        // hueThingHandlerFactory.createHandler(bridge)
        hueLightDiscoveryService.onHueLightDiscovered(bridge, lightId, lightName)
        waitForAssert{assertTrue resultWrapper.isSet}

        resultWrapper.wrappedObject.with {
            assertThat flag, is (DiscoveryResultFlag.NEW)
            assertThat thingUID.toString(), is("hue:light:testBridgeLight" + lightId)
            assertThat label, is (lightName)
            assertThat thingTypeUID, is (HueThingTypeProvider.LIGHT_THING_TYPE.getUID())
            assertThat bridgeUID, is(bridge.getUID())
            assertThat properties.get(HueLightContextKey.LIGHT_ID.getKey()), is (lightId)
        }
    }
}
