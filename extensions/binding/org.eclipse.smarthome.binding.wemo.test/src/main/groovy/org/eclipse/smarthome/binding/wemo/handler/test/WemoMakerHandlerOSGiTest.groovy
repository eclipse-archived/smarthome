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

import javax.servlet.http.HttpServlet

import org.eclipse.smarthome.binding.wemo.WemoBindingConstants
import org.eclipse.smarthome.binding.wemo.handler.WemoMakerHandler
import org.eclipse.smarthome.binding.wemo.test.GenericWemoHttpServlet
import org.eclipse.smarthome.binding.wemo.test.GenericWemoOSGiTest
import org.eclipse.smarthome.core.items.Item
import org.eclipse.smarthome.core.library.types.OnOffType
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.types.RefreshType
import org.eclipse.smarthome.core.types.UnDefType
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Tests for {@link WemoMakerHandler}.
 *
 * @author Svilen Valkanov - Initial contribution
 */
class WemoMakerHandlerOSGiTest extends GenericWemoOSGiTest {

    // Specific Thing information
    def DEFAULT_TEST_CHANNEL = WemoBindingConstants.CHANNEL_RELAY
    def DEFAULT_TEST_CHANNEL_TYPE = "Switch"
    def THING_TYPE_UID = WemoBindingConstants.THING_TYPE_MAKER;

    // Specific UpnP service information
    def MODEL = THING_TYPE_UID.getId()
    def DEVICE_EVENT_SERVICE_ID = 'deviceevent'
    def BASIC_EVENT_SERVICE_ID = 'basicevent'
    def SERVICE_NUMBER = '1'
    def DEVICE_EVENT_SERVLET_URL = "${DEVICE_CONTROL_PATH}${DEVICE_EVENT_SERVICE_ID}${SERVICE_NUMBER}"
    def BASIC_EVENT_SERVLET_URL = "${DEVICE_CONTROL_PATH}${BASIC_EVENT_SERVICE_ID}${SERVICE_NUMBER}"

    HttpServlet basicServlet
    HttpServlet deviceServlet

    @Before
    public void setUp() {
        setUpServices()
        basicServlet = new WemoMakerHttpServlet(BASIC_EVENT_SERVICE_ID, SERVICE_NUMBER);
        deviceServlet = new WemoMakerHttpServlet(DEVICE_EVENT_SERVICE_ID, SERVICE_NUMBER)
        registerServlet(BASIC_EVENT_SERVLET_URL, basicServlet);
        registerServlet(DEVICE_EVENT_SERVLET_URL, deviceServlet);
    }

    @After
    public void tearDown() {
        removeThing()
        unregisterServlet(BASIC_EVENT_SERVLET_URL);
        unregisterServlet(DEVICE_EVENT_SERVLET_URL);
        deviceServlet.actions.clear()
        basicServlet.actions.clear()
    }

    @Test
    void "assert that thing updates channels at start" () {
        deviceServlet.attributeName = 'Switch'
        deviceServlet.attributeValue = 0 // OFF command
        def expectedState = OnOffType.OFF

        addUpnpDevice(DEVICE_EVENT_SERVICE_ID, SERVICE_NUMBER, MODEL)

        createThing(THING_TYPE_UID, DEFAULT_TEST_CHANNEL, DEFAULT_TEST_CHANNEL_TYPE);

        waitForAssert {
            WemoMakerHandler handler = getThingHandler(WemoMakerHandler.class)
            assertThat handler, is(notNullValue())
            assertThat handler.getThing().getStatus(), is(ThingStatus.ONLINE)
        }

        waitForAssert{
            assertThat "Invalid SOAP action sent to the device: ${deviceServlet.actions}", deviceServlet.actions.contains(WemoMakerHttpServlet.GET_ACTION), is (true)
        }

        waitForAssert{
            Item item = itemRegistry.getItem(DEFAULT_TEST_ITEM_NAME)
            assertThat "Item with name ${DEFAULT_TEST_ITEM_NAME} may not be created. Check the createItem() method.", item, is(notNullValue())
            assertThat "The state of the item ${DEFAULT_TEST_ITEM_NAME} was not updated at start.", item.getState(), is(expectedState)
        }
    }

    @Test
    void "assert that thing handles OnOff command correctly"() {
        def command = OnOffType.OFF
        def expectedState = "0"

        createThing(THING_TYPE_UID, DEFAULT_TEST_CHANNEL, DEFAULT_TEST_CHANNEL_TYPE)

        waitForAssert {
            WemoMakerHandler handler = getThingHandler(WemoMakerHandler.class)
            assertThat handler, is(notNullValue())
            assertThat handler.getThing().getStatus(), is(ThingStatus.ONLINE)
        }

        // The Device is registered as UPnP Device after the initialization, this will ensure that the polling job will not start
        addUpnpDevice(BASIC_EVENT_SERVICE_ID, SERVICE_NUMBER, MODEL)

        ChannelUID channelUID = new ChannelUID(thing.getUID(), DEFAULT_TEST_CHANNEL)
        thing.getHandler().handleCommand(channelUID, command)

        waitForAssert {
            assertThat "Invalid SOAP action sent to the device: ${basicServlet.actions}", basicServlet.actions.contains(WemoMakerHttpServlet.SET_ACTION), is(true)
            assertThat "The state of the device after the command ${command} was not updated with the expected value.", basicServlet.binaryState, is(expectedState)
        }
    }

    @Test
    void "assert that thing handles REFRESH command"() {
        deviceServlet.attributeName = 'Switch'
        deviceServlet.attributeValue = 1 // ON command
        def expectedState = OnOffType.ON
        def command = RefreshType.REFRESH

        createThing(THING_TYPE_UID, DEFAULT_TEST_CHANNEL, DEFAULT_TEST_CHANNEL_TYPE)

        waitForAssert {
            WemoMakerHandler handler = getThingHandler(WemoMakerHandler.class)
            assertThat handler, is(notNullValue())
            assertThat handler.getThing().getStatus(), is(ThingStatus.ONLINE)
        }

        waitForAssert{
            Item item = itemRegistry.get(DEFAULT_TEST_ITEM_NAME)
            assertThat "Item with name ${DEFAULT_TEST_ITEM_NAME} may not be created. Check the createItem() method.", item, is(notNullValue())
            assertThat "The state of the item ${DEFAULT_TEST_ITEM_NAME} was not updated at start.", item.getState(), is(UnDefType.NULL)
        }

        // The Device is registered as UPnP Device after the initialization, this will ensure that the polling job will not start
        addUpnpDevice(BASIC_EVENT_SERVICE_ID, SERVICE_NUMBER, MODEL)

        ChannelUID channelUID = new ChannelUID(thing.getUID(), DEFAULT_TEST_CHANNEL)
        thing.getHandler().handleCommand(channelUID, command)

        waitForAssert {
            assertThat "Invalid SOAP action sent to the device:${deviceServlet.actions}", deviceServlet.actions.contains(WemoMakerHttpServlet.GET_ACTION), is(true)
        }

        waitForAssert{
            Item item = itemRegistry.get(DEFAULT_TEST_ITEM_NAME)
            assertThat "Item with name ${DEFAULT_TEST_ITEM_NAME} may not be created. Check the createItem() method.", item, is(notNullValue())
            assertThat "The state of the item ${DEFAULT_TEST_ITEM_NAME} was not updated after command ${command}.", item.getState(), is(expectedState)
        }
    }

    private void removeThing() {
        if(thing != null) {
            Thing removedThing = thingRegistry.remove(thing.getUID())
            assertThat("The thing cannot be deleted", removedThing, is(notNullValue()))
        }

        waitForAssert {
            ThingHandler thingHandler = getThingHandler(WemoMakerHandler)
            assertThat thingHandler, is(nullValue())
        }

        waitForAssert {
            assertThat "UPnP registry is not clear", upnpIOService.participants.keySet().size(), is(0)
        }

        itemRegistry.remove(DEFAULT_TEST_ITEM_NAME)
        waitForAssert {
            assertThat itemRegistry.getAll().size(), is(0)
        }
    }

    class WemoMakerHttpServlet extends GenericWemoHttpServlet {
        final static def GET_ACTION = "GetAttributes"
        final static def SET_ACTION = "SetBinaryState"

        def actions = [] as Set
        def attributeName
        def attributeValue

        def binaryState

        WemoMakerHttpServlet (String service, String serviceNumber) {
            super(service, serviceNumber)
        }

        protected String handleRequest (Node root) {
            def getActions = root[soapNamespace.Body][uNamespace.GetAttributes];
            def setActions = root[soapNamespace.Body][uNamespace.SetBinaryState];

            if (getActions.size() > 0) {
                def getAction = getActions.get(0);


                getAction.replaceNode {
                    attributeList {
                        attribute {
                            name(this.attributeName)
                            value(this.attributeValue)
                        }
                    }
                }
                this.actions.add(getAction.name().getLocalPart())

                return XmlUtil.serialize(root)
            } else if(setActions.size() > 0) {
                def setAction = setActions.get(0);
                this.actions.add(setAction.name().getLocalPart())
                this.binaryState =  setAction.text()
            }
            return ""
        }
    }
}
