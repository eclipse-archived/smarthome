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
import nl.q42.jue.HueBridge
import nl.q42.jue.exceptions.ApiException
import nl.q42.jue.exceptions.LinkButtonException
import nl.q42.jue.exceptions.UnauthorizedException

import org.eclipse.smarthome.binding.hue.handler.HueBridgeHandler
import org.eclipse.smarthome.binding.hue.internal.HueConfigStatusMessage
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage
import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingStatusDetail
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.junit.Before
import org.junit.Test


/**
 * Tests for {@link HueBridgeHandler}.
 *
 * @author Oliver Libutzki - Initial contribution
 * @author Michael Grammling - Initial contribution
 */
class HueBridgeHandlerOSGiTest extends AbstractHueOSGiTest {

    final ThingTypeUID BRIDGE_THING_TYPE_UID = new ThingTypeUID("hue", "bridge")
    private static final TEST_USER_NAME = "eshTestUser"
    private static final DUMMY_HOST = "1.2.3.4"

    ThingRegistry thingRegistry


    @Before
    void setUp() {
        registerVolatileStorageService()
        thingRegistry = getService(ThingRegistry, ThingRegistry)
        assertThat thingRegistry, is(notNullValue())
    }

    @Test
    void 'assert that a new user is added to config if not existing yet'() {

        Configuration configuration = new Configuration().with {
            put(HOST, DUMMY_HOST)
            put(SERIAL_NUMBER, "testSerialNumber")
            it
        }
        Bridge bridge = createBridgeThing(configuration)

        HueBridgeHandler hueBridgeHandler = getThingHandler(HueBridgeHandler)
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

        HueBridgeHandler hueBridgeHandler = getThingHandler(HueBridgeHandler)
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

        HueBridgeHandler hueBridgeHandler = getThingHandler(HueBridgeHandler)
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

        HueBridgeHandler hueBridgeHandler = getThingHandler(HueBridgeHandler)
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

        HueBridgeHandler hueBridgeHandler = getThingHandler(HueBridgeHandler)
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

    @Test
    void 'verify offline is set without bridge offline status'() {
        Configuration configuration = new Configuration().with {
            put(HOST, DUMMY_HOST)
            put(SERIAL_NUMBER, "testSerialNumber")
            it
        }
        Bridge bridge = createBridgeThing(configuration)

        HueBridgeHandler hueBridgeHandler = getThingHandler(HueBridgeHandler)
        hueBridgeHandler.thingUpdated(bridge)

        hueBridgeHandler.onConnectionLost(hueBridgeHandler.bridge)

        assertThat(bridge.getStatus(), is(ThingStatus.OFFLINE))
        assertThat(bridge.getStatusInfo().getStatusDetail(), is(not(ThingStatusDetail.BRIDGE_OFFLINE)))
    }

    @Test
    void 'assert that a status configuration message for missing bridge IP is properly returned (IP is null)'() {
        Configuration configuration = new Configuration().with {
            put(HOST, null)
            put(SERIAL_NUMBER, "testSerialNumber")
            it
        }

        createBridgeThing(configuration)

        HueBridgeHandler hueBridgeHandler = getThingHandler(HueBridgeHandler)

        def expected = ConfigStatusMessage.Builder.error(HOST)
                .withMessageKeySuffix(HueConfigStatusMessage.IP_ADDRESS_MISSING.getMessageKey()).withArguments(HOST)
                .build()

        waitForAssert {
            assertThat hueBridgeHandler.getConfigStatus().first(), is(expected)
        }
    }

    @Test
    void 'assert that a status configuration message for missing bridge IP is properly returned (IP is an empty string)'() {
        Configuration configuration = new Configuration().with {
            put(HOST, "")
            put(SERIAL_NUMBER, "testSerialNumber")
            it
        }

        createBridgeThing(configuration)

        HueBridgeHandler hueBridgeHandler = getThingHandler(HueBridgeHandler)

        def expected = ConfigStatusMessage.Builder.error(HOST)
                .withMessageKeySuffix(HueConfigStatusMessage.IP_ADDRESS_MISSING.getMessageKey()).withArguments(HOST)
                .build()

        waitForAssert {
            assertThat hueBridgeHandler.getConfigStatus().first(), is(expected)
        }
    }

    private Bridge createBridgeThing(Configuration configuration){
        Bridge bridge = thingRegistry.createThingOfType(
                BRIDGE_THING_TYPE_UID,
                new ThingUID(BRIDGE_THING_TYPE_UID, "testBridge"),
                null, "Bridge", configuration)

        assertThat bridge, is(notNullValue())
        thingRegistry.add(bridge)
        return bridge
    }
}
