/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
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
import nl.q42.jue.HueBridge
import nl.q42.jue.exceptions.ApiException
import nl.q42.jue.exceptions.LinkButtonException
import nl.q42.jue.exceptions.UnauthorizedException

import org.eclipse.smarthome.binding.hue.handler.HueBridgeHandler
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.ThingProvider
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingStatusDetail
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
    private static final TEST_USER_NAME = "eshTestUser"
    private static final DUMMY_HOST = "1.2.3.4"

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
            put(HOST, DUMMY_HOST)
            put(USER_NAME, TEST_USER_NAME)
            put(SERIAL_NUMBER, "testSerialNumber")
            it
        }

        Bridge hueBridge = createBridgeThing(configuration)

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

    @Test
    void 'assert that a new user is added to config if not existing yet'() {

        Configuration configuration = new Configuration().with {
            put(HOST, DUMMY_HOST)
            put(SERIAL_NUMBER, "testSerialNumber")
            it
        }
        Bridge bridge = createBridgeThing(configuration)

        HueBridgeHandler hueBridgeHandler = getRegisteredHueBridgeHandler()
        hueBridgeHandler.thingUpdated(bridge)

        HueBridge hueBridge = new HueBridge(DUMMY_HOST) {
                    String link(String deviceType) throws IOException, ApiException {
                        return TEST_USER_NAME;
                    };
                }

        hueBridgeHandler.onNotAuthenticated(hueBridge)

        assertThat(bridge.getConfiguration().get(USER_NAME), equalTo(TEST_USER_NAME))
    }

    @Test
    void 'assert that an existing user is used if authentication was successful'() {

        Configuration configuration = new Configuration().with {
            put(HOST, DUMMY_HOST)
            put(USER_NAME, TEST_USER_NAME)
            put(SERIAL_NUMBER, "testSerialNumber")
            it
        }
        Bridge bridge = createBridgeThing(configuration)

        HueBridgeHandler hueBridgeHandler = getRegisteredHueBridgeHandler()
        hueBridgeHandler.thingUpdated(bridge)

        HueBridge hueBridge = new HueBridge(DUMMY_HOST) {
                    void authenticate(String userName) throws IOException, ApiException {};
                }

        hueBridgeHandler.onNotAuthenticated(hueBridge)

        assertThat(bridge.getConfiguration().get(USER_NAME), equalTo(TEST_USER_NAME))
    }

    @Test
    void 'assert correct status if authentication failed for old user'() {

        Configuration configuration = new Configuration().with {
            put(HOST, DUMMY_HOST)
            put(USER_NAME, "notAuthenticatedUser")
            put(SERIAL_NUMBER, "testSerialNumber")
            it
        }
        Bridge bridge = createBridgeThing(configuration)

        HueBridgeHandler hueBridgeHandler = getRegisteredHueBridgeHandler()
        hueBridgeHandler.thingUpdated(bridge)

        HueBridge hueBridge = new HueBridge(DUMMY_HOST) {
                    void authenticate(String userName) throws IOException, ApiException {
                        throw new UnauthorizedException()
                    };
                }

        hueBridgeHandler.onNotAuthenticated(hueBridge)

        assertThat(bridge.getConfiguration().get(USER_NAME), equalTo("notAuthenticatedUser"))
        assertThat(bridge.getStatus(), equalTo(ThingStatus.OFFLINE))
        assertThat(bridge.getStatusInfo().getStatusDetail(), equalTo(ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR))
    }

    @Test
    void 'verify status if link button is not pressed'() {

        Configuration configuration = new Configuration().with {
            put(HOST, DUMMY_HOST)
            put(SERIAL_NUMBER, "testSerialNumber")
            it
        }
        Bridge bridge = createBridgeThing(configuration)

        HueBridgeHandler hueBridgeHandler = getRegisteredHueBridgeHandler()
        hueBridgeHandler.thingUpdated(bridge)

        HueBridge hueBridge = new HueBridge(DUMMY_HOST) {
                    String link(String deviceType) throws IOException, ApiException {
                        throw new LinkButtonException()
                    };
                }

        hueBridgeHandler.onNotAuthenticated(hueBridge)

        assertThat(bridge.getConfiguration().get(USER_NAME), is(nullValue()))
        assertThat(bridge.getStatus(), equalTo(ThingStatus.OFFLINE))
        assertThat(bridge.getStatusInfo().getStatusDetail(), equalTo(ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR))
    }

    @Test
    void 'verify status if new user cannot be created'() {

        Configuration configuration = new Configuration().with {
            put(HOST, DUMMY_HOST)
            put(SERIAL_NUMBER, "testSerialNumber")
            it
        }
        Bridge bridge = createBridgeThing(configuration)

        HueBridgeHandler hueBridgeHandler = getRegisteredHueBridgeHandler()
        hueBridgeHandler.thingUpdated(bridge)

        HueBridge hueBridge = new HueBridge(DUMMY_HOST) {
                    String link(String deviceType) throws IOException, ApiException {
                        throw new ApiException()
                    };
                }

        hueBridgeHandler.onNotAuthenticated(hueBridge)

        assertThat(bridge.getConfiguration().get(USER_NAME), is(nullValue()))
        assertThat(bridge.getStatus(), equalTo(ThingStatus.OFFLINE))
        assertThat(bridge.getStatusInfo().getStatusDetail(), equalTo(ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR))
    }

    private Bridge createBridgeThing(Configuration configuration){
        Bridge bridge = managedThingProvider.createThing(
                BRIDGE_THING_TYPE_UID,
                new ThingUID(BRIDGE_THING_TYPE_UID, "testBridge"),
                null, configuration)

        assertThat bridge, is(notNullValue())
        return bridge
    }

    private HueBridgeHandler getRegisteredHueBridgeHandler(){
        HueBridgeHandler hueBridgeHandler
        // wait for HueBridgeHandler to be registered
        waitForAssert({
            hueBridgeHandler = getService(ThingHandler, HueBridgeHandler)
            assertThat hueBridgeHandler, is(notNullValue())
        }, 10000)
        return hueBridgeHandler
    }

}
