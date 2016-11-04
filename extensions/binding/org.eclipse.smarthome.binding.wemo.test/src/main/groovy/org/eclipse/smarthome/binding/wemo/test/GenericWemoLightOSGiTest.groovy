/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.wemo.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import javax.servlet.http.HttpServlet

import org.eclipse.smarthome.binding.wemo.WemoBindingConstants
import org.eclipse.smarthome.binding.wemo.handler.WemoBridgeHandler
import org.eclipse.smarthome.binding.wemo.handler.WemoLightHandler
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.Channel
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.io.transport.upnp.UpnpIOParticipant

/**
 * Generic test class for all WemoLight related tests that contains methods and constants used across the different test classes
 *
 * @author Svilen Valkanov - Initial contribution
 */
class GenericWemoLightOSGiTest extends GenericWemoOSGiTest {

    // Thing information
    def THING_TYPE_UID = WemoBindingConstants.THING_TYPE_MZ100
    def BRIDGE_TYPE_UID = WemoBindingConstants.THING_TYPE_BRIDGE
    def WEMO_LIGHT_ID = THING_TYPE_UID.getId()
    def WEMO_BRIDGE_ID = BRIDGE_TYPE_UID.getId()
    def DEFAULT_TEST_CHANNEL = WemoBindingConstants.CHANNEL_STATE
    def DEFAULT_TEST_CHANNEL_TYPE = "Switch"

    // UPnP service information
    def BRIDGE_MODEL_NAME = WEMO_BRIDGE_ID
    def DEVICE_MODEL_NAME = WEMO_LIGHT_ID
    def SERVICE_ID = 'bridge'
    def SERVICE_NUMBER = '1'
    def SERVLET_URL = "${DEVICE_CONTROL_PATH}${SERVICE_ID}${SERVICE_NUMBER}"

    Bridge bridge;
    HttpServlet servlet;

    protected void createBridge(ThingTypeUID bridgeTypeUID) {
        Configuration configuration = new Configuration();
        configuration.put(WemoBindingConstants.UDN, DEVICE_UDN)

        ThingUID bridgeUID = new ThingUID(bridgeTypeUID, WEMO_BRIDGE_ID);

        bridge = BridgeBuilder.create(bridgeTypeUID, bridgeUID)
                .withConfiguration(configuration)
                .build();

        managedThingProvider.add(bridge)
    }

    protected void createDefaultThing(ThingTypeUID thingTypeUID) {
        createThing(thingTypeUID, DEFAULT_TEST_CHANNEL, DEFAULT_TEST_CHANNEL_TYPE)
    }

    @Override
    protected void createThing(ThingTypeUID thingTypeUID, String channelID, String itemAcceptedType) {
        Configuration configuration = new Configuration();
        configuration.put(WemoBindingConstants.DEVICE_ID, WEMO_LIGHT_ID)

        ThingUID thingUID = new ThingUID(thingTypeUID, TEST_THING_ID);

        ChannelUID channelUID = new ChannelUID(thingUID, channelID)
        Channel channel = new Channel(channelUID, itemAcceptedType)

        ThingUID bridgeUID = new ThingUID(BRIDGE_TYPE_UID, WEMO_BRIDGE_ID);

        thing = ThingBuilder.create(thingTypeUID, thingUID)
                .withConfiguration(configuration)
                .withChannel(channel)
                .withBridge(bridgeUID)
                .build();

        managedThingProvider.add(thing)

        createItem(channelUID,DEFAULT_TEST_ITEM_NAME,itemAcceptedType)
    }


    protected void removeThing() {
        if(thing != null) {
            Thing removedThing = thingRegistry.remove(thing.getUID())
            assertThat("The thing ${thing.getUID()} cannot be deleted", removedThing, is(notNullValue()))
        }

        waitForAssert {
            ThingHandler thingHandler = getThingHandler(WemoLightHandler)
            assertThat thingHandler, is(nullValue())
        }

        if(bridge != null) {
            Bridge bridgeThing = thingRegistry.remove(bridge.getUID())
            assertThat "The bridge ${bridge.getUID()} cannot be deleted", bridgeThing, is(notNullValue())
        }

        waitForAssert {
            ThingHandler bridgeHandler = getThingHandler(WemoBridgeHandler)
            assertThat bridgeHandler, is(nullValue())
        }

        waitForAssert {
            Set<UpnpIOParticipant> participants  = upnpIOService.participants.keySet();
            assertThat "UPnP Registry is not clear: ${participants}", participants.size(), is(0)
        }

        itemRegistry.remove(DEFAULT_TEST_ITEM_NAME)
        waitForAssert {
            assertThat itemRegistry.getAll().size(), is(0)
        }
    }
}
