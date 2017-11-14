/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.wemo.handler.test
import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*
import groovy.xml.XmlUtil

import javax.servlet.http.HttpServletResponse

import org.eclipse.smarthome.binding.wemo.WemoBindingConstants
import org.eclipse.smarthome.binding.wemo.handler.WemoBridgeHandler
import org.eclipse.smarthome.binding.wemo.handler.WemoLightHandler
import org.eclipse.smarthome.binding.wemo.test.GenericWemoHttpServlet
import org.eclipse.smarthome.binding.wemo.test.GenericWemoLightOSGiTest
import org.eclipse.smarthome.core.items.Item
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType
import org.eclipse.smarthome.core.library.types.OnOffType
import org.eclipse.smarthome.core.library.types.PercentType
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.types.Command
import org.eclipse.smarthome.core.types.RefreshType
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Tests for {@link WemoLightHandler}.
 *
 * @author Svilen Valkanov - Initial contribution
 */
class WemoLightHandlerOSGiTest extends GenericWemoLightOSGiTest {

    def BRIDGE_HANDLER_INITIALIZE_TIMEOUT = 1000;

    @Before
    public void setUp() {
        setUpServices()
        servlet = new WemoLightHttpServlet(SERVICE_ID, SERVICE_NUMBER);
        registerServlet(SERVLET_URL, servlet);
        // The default timeout is 15 seconds, for this test 1 second timeout is enough
        WemoLightHandler.DEFAULT_REFRESH_INITIAL_DELAY = 1
    }

    @After
    public void tearDown() {
        unregisterServlet(SERVLET_URL);
        removeThing()
        servlet.actions.clear()
    }

    @Test
    void "assert that thing updates channels at start" () {
        servlet.binaryState = 0 // OFF command
        servlet.brightness = 0
        def expectedState = OnOffType.OFF

        addUpnpDevice(SERVICE_ID, SERVICE_NUMBER, DEVICE_MODEL_NAME)

        createBridge(BRIDGE_TYPE_UID)
        //Without this sleep a NPE could occur in the WemoBridgeHandler#initialize() method
        sleep(BRIDGE_HANDLER_INITIALIZE_TIMEOUT)
        createDefaultThing(THING_TYPE_UID)

        WemoLightHandler handler
        WemoBridgeHandler bridgeHandler

        waitForAssert {
            bridgeHandler = getThingHandler(WemoBridgeHandler.class)
            assertThat bridgeHandler, is(notNullValue())
            assertThat bridgeHandler.getThing().getStatus(), is(ThingStatus.ONLINE)
        }

        waitForAssert {
            handler = getThingHandler(WemoLightHandler.class)
            assertThat handler, is(notNullValue())
            assertThat handler.getThing().getStatus(), is(ThingStatus.ONLINE)
        }

        waitForAssert {
            assertThat "Invalid SOAP action sent to the device: ${servlet.actions}", servlet.actions.contains(WemoLightHttpServlet.GET_ACTION), is(true)
        }

        waitForAssert{
            Item item = itemRegistry.get(DEFAULT_TEST_ITEM_NAME)
            assertThat "Item with name ${DEFAULT_TEST_ITEM_NAME} may not be created. Check the createItem() method.", item, is(notNullValue())
            assertThat "The state of the item ${DEFAULT_TEST_ITEM_NAME} was not updated at start.", item.getState(), is(expectedState)
        }
    }

    @Test
    public void 'handle ON command for BRIGHTNESS channel' () {
        Command command = OnOffType.ON;
        String channelID = WemoBindingConstants.CHANNEL_BRIGHTNESS
        String acceptedItemType = "Dimmer"

        // Command ON for this channel sends the following data to the device
        def action = WemoLightHttpServlet.SET_ACTION
        // ON is equal to brightness value of 255
        def value= "255:0"
        def capitability = "10008"

        assertRequestForCommand(channelID, command, action, value, capitability)
    }


    @Test
    public void 'handle Percent command for BRIGHTNESS channel' () {
        // Set brightness value to 20 Percent
        Command command = new PercentType(20);
        String channelID = WemoBindingConstants.CHANNEL_BRIGHTNESS
        String acceptedItemType = "Dimmer"

        def action = WemoLightHttpServlet.SET_ACTION
        // 20 Percent brightness is equal to a brightness value of 51
        def value= "51:0"
        def capitability = "10008"

        assertRequestForCommand(channelID, command, action, value, capitability)
    }

    @Test
    public void 'handle Increase command for BRIGHTNESS channel' () {
        // The value is increased by 5 Percents by default
        Command command = IncreaseDecreaseType.INCREASE;
        String channelID = WemoBindingConstants.CHANNEL_BRIGHTNESS
        String acceptedItemType = "Dimmer"

        def action = WemoLightHttpServlet.SET_ACTION
        // 5 Percents brightness is equal to a brightness value of 12
        def value= "12:0"
        def capitability = "10008"

        assertRequestForCommand(channelID, command, action, value, capitability)
    }

    @Test
    public void 'handle Decrease command for BRIGHTNESS channel' () {
        // The value can not be decreased below 0
        Command command = IncreaseDecreaseType.DECREASE;
        String channelID = WemoBindingConstants.CHANNEL_BRIGHTNESS
        String acceptedItemType = "Dimmer"

        def action = WemoLightHttpServlet.SET_ACTION
        def value= "0:0"
        def capitability = "10008"

        assertRequestForCommand(channelID, command, action, value, capitability)
    }

    @Test
    public void 'handle On command for STATE channel' () {
        Command command = OnOffType.ON;
        String channelID = WemoBindingConstants.CHANNEL_STATE
        String acceptedItemType = "Switch"

        // Command ON for this channel sends the following data to the device
        def action = WemoLightHttpServlet.SET_ACTION
        def value= "1"
        def capitability = "10006"

        assertRequestForCommand(channelID, command, action, value, capitability)
    }

    @Test
    public void 'handle REFRESH command for channel STATE' () {
        Command command = RefreshType.REFRESH;
        String channelID = WemoBindingConstants.CHANNEL_STATE

        def action = WemoLightHttpServlet.GET_ACTION
        def value= null
        def capitability = null

        assertRequestForCommand(channelID, command, action, value, capitability)

    }

    private assertRequestForCommand(String channelID, Command command, String action, String value, String capitability) {
        createBridge(BRIDGE_TYPE_UID)
        //Without this sleep a NPE could occur in the WemoBridgeHandler#initialize() method
        sleep(BRIDGE_HANDLER_INITIALIZE_TIMEOUT)
        createDefaultThing(THING_TYPE_UID)

        WemoLightHandler handler
        WemoBridgeHandler bridgeHandler

        waitForAssert {
            bridgeHandler = getThingHandler(WemoBridgeHandler.class)
            assertThat bridgeHandler, is(notNullValue())
            assertThat bridgeHandler.getThing().getStatus(), is(ThingStatus.ONLINE)
        }

        waitForAssert {
            handler = getThingHandler(WemoLightHandler.class)
            assertThat handler, is(notNullValue())
            assertThat handler.getThing().getStatus(), is(ThingStatus.ONLINE)
        }

        // The device is registered as UPnP Device after the initialization, this will ensure that the polling job will not start
        addUpnpDevice(SERVICE_ID, SERVICE_NUMBER, DEVICE_MODEL_NAME)

        ThingUID thingUID = new ThingUID(THING_TYPE_UID, TEST_THING_ID);
        ChannelUID channelUID = new ChannelUID(thingUID, channelID)
        handler.handleCommand(channelUID, command)

        waitForAssert{
            assertThat "Invalid SOAP action sent to the device: ${servlet.actions}", servlet.actions.contains(action), is(true)
            assertThat "No request received for capitability: ${servlet.capitability}, after command ${command}", servlet.capitability, is(capitability)
            assertThat "Incorrect value recevied for capitability ${servlet.capitability} ", servlet.value, is(value)
        }
    }

    class WemoLightHttpServlet extends GenericWemoHttpServlet {
        static def GET_ACTION = "GetDeviceStatus"
        static def SET_ACTION = "SetDeviceStatus"

        def binaryState
        def brightness

        def actions = [] as Set
        def value
        def capitability

        WemoLightHttpServlet(String service, String serviceNumber) {
            super(service, serviceNumber)
            binaryState = 0
            brightness = 0
        }

        protected String handleRequest (Node root) {

            def getActions = root[soapNamespace.Body][uNamespace.GetDeviceStatus];
            def setActions = root[soapNamespace.Body][uNamespace.SetDeviceStatus];
            def getAllDevices = root[soapNamespace.Body][uNamespace.GetEndDevices];

            if (getActions.size() > 0) {
                super.setResponseStatus(HttpServletResponse.SC_OK)

                def getAction = getActions.get(0)
                this.actions.add(getAction.name().getLocalPart())

                getAction.replaceNode {
                    GetDeviceStatus {  CapabilityValue ("${this.binaryState},${this.brightness}")  }
                }

                return XmlUtil.serialize(root)
            } else if(setActions.size() > 0) {

                def setAction = setActions[0]
                this.actions.add(setAction.name().getLocalPart())

                def innerXML = setAction.DeviceStatusList.text()
                def innerRoot
                synchronized(parser) {
                    innerRoot = parser.parseText(innerXML)
                }
                this.capitability = innerRoot.CapabilityID.text()
                this.value  = innerRoot.CapabilityValue.text()

                return "";
            } else  if (getAllDevices.size() > 0) {
                // Do not answer requests of the discovery service
                setResponseStatus(HttpServletResponse.SC_NOT_FOUND)
                return ""
            }

        }

    }
}
