/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.test

import static org.eclipse.smarthome.binding.hue.HueBindingConstants.*
import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*
import nl.q42.jue.FullLight
import nl.q42.jue.MockedHttpClient
import nl.q42.jue.HttpClient.Result

import org.eclipse.smarthome.binding.hue.handler.HueBridgeHandler
import org.eclipse.smarthome.binding.hue.internal.HueThingHandlerFactory
import org.eclipse.smarthome.binding.hue.internal.discovery.HueLightDiscoveryService
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.config.discovery.DiscoveryListener
import org.eclipse.smarthome.config.discovery.DiscoveryResult
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag
import org.eclipse.smarthome.config.discovery.DiscoveryService
import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingStatusDetail
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder
import org.eclipse.smarthome.test.AsyncResultWrapper
import org.junit.After
import org.junit.Before
import org.junit.Test


/**
 * Tests for {@link HueLightDiscoveryService}.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Andre Fuechsel - added test 'assert start search is called()'
 *                        - modified tests after introducing the generic thing types
 */
class HueLightDiscoveryServiceOSGITest extends AbstractHueOSGiTest {

    HueThingHandlerFactory hueThingHandlerFactory
    DiscoveryListener discoveryListener
    ThingRegistry thingRegistry
    Bridge hueBridge
    HueBridgeHandler hueBridgeHandler
    HueLightDiscoveryService discoveryService

    final ThingTypeUID BRIDGE_THING_TYPE_UID = new ThingTypeUID("hue", "bridge")
    final ThingUID BRIDGE_THING_UID = new ThingUID(BRIDGE_THING_TYPE_UID, "testBridge")


    @Before
    void setUp() {
        registerVolatileStorageService()

        thingRegistry = getService(ThingRegistry, ThingRegistry)
        assertThat thingRegistry, is(notNullValue())

        Configuration configuration = new Configuration().with {
            put(HOST, "1.2.3.4")
            put(USER_NAME, "testUserName")
            put(SERIAL_NUMBER, "testSerialNumber")
            it
        }

        hueBridge = thingRegistry.createThingOfType(
                BRIDGE_THING_TYPE_UID,
                BRIDGE_THING_UID,
                null, "Bridge", configuration)

        assertThat hueBridge, is(notNullValue())
        thingRegistry.add(hueBridge)

        hueBridgeHandler = getThingHandler(HueBridgeHandler)
        assertThat hueBridgeHandler, is(notNullValue())

        discoveryService = getService(DiscoveryService, HueLightDiscoveryService)
        assertThat discoveryService, is(notNullValue())
    }

    @After
    void cleanUp() {
        thingRegistry.remove(BRIDGE_THING_UID)
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
        light.type = "Extended color light"

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
            assertThat thingUID.toString(), is("hue:0210:testBridge:" + light.id)
            assertThat thingTypeUID, is (THING_TYPE_EXTENDED_COLOR_LIGHT)
            assertThat bridgeUID, is(hueBridge.getUID())
            assertThat properties.get(LIGHT_ID), is (light.id)
        }
    }

    @Test
    void 'assert startSearch is called'() {
        def searchHasBeenTriggered = false
        def AsyncResultWrapper<String> addressWrapper = new AsyncResultWrapper<String>()
        def AsyncResultWrapper<String> bodyWrapper = new AsyncResultWrapper<String>()

        MockedHttpClient mockedHttpClient =  [
            put: { String address, String body ->
                addressWrapper.set(address)
                bodyWrapper.set(body)
                new Result("", 200)
            },
            get: { String address ->
                if (address.endsWith("testUserName/")) {
                    def body = """
						{"lights":{}}
						"""
                    new Result(body, 200)
                }
            },
            post: { String address, String body ->
                if (address.endsWith("lights")) {
                    def bodyReturn = """
						{"success": {"/lights": "Searching for new devices"}}
						"""
                    searchHasBeenTriggered = true
                    new Result(bodyReturn, 200)
                }
            }
        ] as MockedHttpClient

        installHttpClientMock(hueBridgeHandler, mockedHttpClient)

        def online = ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build()
        waitForAssert({
            assertThat hueBridge.getStatusInfo(), is(online)
        }, 10000)

        discoveryService.startScan();
        waitForAssert{assertTrue searchHasBeenTriggered}
    }

    private void installHttpClientMock(HueBridgeHandler hueBridgeHandler,
            MockedHttpClient mockedHttpClient) {

        // mock HttpClient
        def hueBridgeField = hueBridgeHandler.getClass().getDeclaredField("bridge")
        hueBridgeField.accessible = true
        def hueBridgeValue = hueBridgeField.get(hueBridgeHandler)

        def httpClientField = hueBridgeValue.getClass().getDeclaredField("http")
        httpClientField.accessible = true
        httpClientField.set(hueBridgeValue, mockedHttpClient)

        def usernameField = hueBridgeValue.getClass().getDeclaredField("username")
        usernameField.accessible = true
        usernameField.set(hueBridgeValue, hueBridgeHandler.config.get(USER_NAME))

        hueBridgeHandler.initialize()
    }
}
