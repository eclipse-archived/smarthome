/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.wemo.handler.test;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.binding.wemo.WemoBindingConstants
import org.eclipse.smarthome.binding.wemo.handler.WemoHandler
import org.eclipse.smarthome.binding.wemo.test.GenericWemoHttpServlet
import org.eclipse.smarthome.binding.wemo.test.GenericWemoOSGiTest
import org.eclipse.smarthome.core.items.Item
import org.eclipse.smarthome.core.library.types.OnOffType
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.types.RefreshType
import org.eclipse.smarthome.core.types.State
import org.eclipse.smarthome.core.types.UnDefType
import org.junit.After
import org.junit.Before
import org.junit.Test

import groovy.xml.XmlUtil

/**
 * Tests for {@link WemoHandler}.
 *
 * @author Svilen Valkanov - Initial contribution
 */

public class WemoHandlerOSGiTest extends GenericWemoOSGiTest {

    // Thing information
    def DEFAULT_TEST_CHANNEL = WemoBindingConstants.CHANNEL_STATE
    def DEFAULT_TEST_CHANNEL_TYPE = "Switch"
    def THING_TYPE_UID = WemoBindingConstants.THING_TYPE_SOCKET;

    // UPnP information
    def MODEL_NAME = WemoBindingConstants.THING_TYPE_SOCKET.getId()
    def SERVICE_ID = 'basicevent'
    def SERVICE_NUMBER = '1'

    def SERVLET_URL = "${DEVICE_CONTROL_PATH}${SERVICE_ID}${SERVICE_NUMBER}"
    WemoHttpServlet servlet;

    @Before
    public void setUp() {
        setUpServices()
        servlet = new WemoHttpServlet(SERVICE_ID, SERVICE_NUMBER);
        registerServlet(SERVLET_URL, servlet);
    }

    @After
    public void tearDown() {
        removeThing()
        unregisterServlet(SERVLET_URL);
        servlet.actions.clear()
    }

    @Test
    void "assert that thing handles OnOff command correctly"() {
        def command = OnOffType.OFF;
        // Binary state 0 is equivalent to OFF
        def exptectedBinaryState = "0"

        createThing(THING_TYPE_UID, DEFAULT_TEST_CHANNEL, DEFAULT_TEST_CHANNEL_TYPE)

        waitForAssert {
            WemoHandler handler = getThingHandler(WemoHandler.class)
            assertThat handler, is(notNullValue())
            assertThat handler.getThing().getStatus(), is(ThingStatus.ONLINE)
        }

        // The device is registered as UPnP Device after the initialization, this will ensure that the polling job will not start
        addUpnpDevice(SERVICE_ID, SERVICE_NUMBER, MODEL_NAME)

        ChannelUID channelUID = new ChannelUID(thing.getUID(), DEFAULT_TEST_CHANNEL)
        thing.getHandler().handleCommand(channelUID, command)

        waitForAssert{
            assertThat "Invalid SOAP action sent to the device: ${servlet.actions}", servlet.actions.contains(WemoHttpServlet.SET_ACTION) ,is (true)
            assertThat "The state of the device after the command ${command} was not updated with the expected value.", servlet.binaryState, is(exptectedBinaryState)
        }
    }

    @Test
    void "assert that thing handles REFRESH command correctly" (){
        servlet.binaryState = 0
        // Binary state 0 is equivalent to OFF
        def expectedState = OnOffType.OFF
        def command = RefreshType.REFRESH;

        createThing(THING_TYPE_UID, DEFAULT_TEST_CHANNEL, DEFAULT_TEST_CHANNEL_TYPE);

        waitForAssert {
            WemoHandler handler = getThingHandler(WemoHandler.class)
            assertThat handler, is(notNullValue())
            assertThat handler.getThing().getStatus(), is(ThingStatus.ONLINE)
        }

        waitForAssert{
            Item item = itemRegistry.get(DEFAULT_TEST_ITEM_NAME)
            assertThat "Item with name ${DEFAULT_TEST_ITEM_NAME} may not be created. Check the createItem() method.", item, is(notNullValue())
            assertThat "The state of the item ${DEFAULT_TEST_ITEM_NAME} was updated at start.", item.getState(), is(UnDefType.NULL)
        }

        // The device is registered as UPnP Device after the initialization, this will ensure that the polling job will not start
        addUpnpDevice(SERVICE_ID, SERVICE_NUMBER, MODEL_NAME)

        ChannelUID channelUID = new ChannelUID(thing.getUID(), DEFAULT_TEST_CHANNEL)
        thing.getHandler().handleCommand(channelUID, command)

        waitForAssert{
            assertThat "Invalid SOAP action sent to the device: ${servlet.actions}", servlet.actions.contains(WemoHttpServlet.GET_ACTION),is (true)
        }

        waitForAssert{
            Item item = itemRegistry.get(DEFAULT_TEST_ITEM_NAME)
            assertThat "Item with name ${DEFAULT_TEST_ITEM_NAME} may not be created. Check the createItem() method.", item, is(notNullValue())
            assertThat "The state of the item ${DEFAULT_TEST_ITEM_NAME} was not updated after command ${command}.", item.getState(), is(expectedState)
        }

    }

    @Test
    void "assert that thing updates channels states at start" () {
        servlet.binaryState = 1
        State expectedState = OnOffType.ON

        // The device is registered as UPnP Device before the initialization and the polling job will start
        addUpnpDevice(SERVICE_ID, SERVICE_NUMBER, MODEL_NAME)

        createThing(THING_TYPE_UID, DEFAULT_TEST_CHANNEL, DEFAULT_TEST_CHANNEL_TYPE);

        waitForAssert {
            WemoHandler handler = getThingHandler(WemoHandler.class)
            assertThat handler, is(notNullValue())
            assertThat handler.getThing().getStatus(), is(ThingStatus.ONLINE)
        }

        waitForAssert{
            assertThat "Invalid SOAP action sent to the device: ${servlet.actions}", servlet.actions.contains(WemoHttpServlet.GET_ACTION), is(true)
        }

        waitForAssert{
            Item item = itemRegistry.get(DEFAULT_TEST_ITEM_NAME)
            assertThat "Item with name ${DEFAULT_TEST_ITEM_NAME} is not be created. Check the createItem() method.", item, is(notNullValue())
            assertThat "The state of the item ${DEFAULT_TEST_ITEM_NAME} was not updated at start.", item.getState(), is(expectedState)
        }
    }

    private void removeThing() {
        if(thing != null) {
            Thing removedThing = thingRegistry.remove(thing.getUID())
            assertThat("The thing cannot be deleted", removedThing, is(notNullValue()))
        }

        waitForAssert {
            ThingHandler thingHandler = getThingHandler(WemoHandler)
            assertThat thingHandler, is(nullValue())
        }

        waitForAssert {
            assertThat upnpIOService.participants.keySet().size(), is(0)
        }

        itemRegistry.remove(DEFAULT_TEST_ITEM_NAME)
        waitForAssert({
            assertThat itemRegistry.getAll().size(), is(0)
        }, {
            Collection<Item> items = itemRegistry.getAll()
            for (Item item : items) {
                System.out.println(String.format("item in registry: name=%s, type=%s, state=%s", item.getName(), item.getType(), item.getState()))
            }
        })
    }
}

class WemoHttpServlet extends GenericWemoHttpServlet {
    final static def GET_ACTION = "GetBinaryState"
    final static def SET_ACTION = "SetBinaryState"

    def actions = [] as Set
    def binaryState

    WemoHttpServlet (String service, String serviceNumber) {
        super(service, serviceNumber)
    }

    protected String handleRequest (Node root) {

        def getActions = root[soapNamespace.Body][uNamespace.GetBinaryState];
        if (getActions.size() > 0) {
            def getAction = getActions.get(0);

            this.actions.add(getAction.name().getLocalPart())

            getAction.replaceNode { BinaryState(this.binaryState) }
            return XmlUtil.serialize(root)
        }

        def setActions = root[soapNamespace.Body][uNamespace.SetBinaryState];
        if(setActions.size() > 0) {
            def setAction = setActions.get(0);
            this.actions.add(setAction.name().getLocalPart())
            this.binaryState =  setAction.text()
        }
        return ""
    }

}
