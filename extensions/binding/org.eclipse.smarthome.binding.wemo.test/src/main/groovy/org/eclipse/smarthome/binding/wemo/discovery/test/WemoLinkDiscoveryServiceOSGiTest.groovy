/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.wemo.discovery.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.binding.wemo.WemoBindingConstants
import org.eclipse.smarthome.binding.wemo.internal.discovery.WemoLinkDiscoveryService
import org.eclipse.smarthome.binding.wemo.test.GenericWemoHttpServlet
import org.eclipse.smarthome.binding.wemo.test.GenericWemoLightOSGiTest
import org.eclipse.smarthome.config.discovery.DiscoveryResult
import org.eclipse.smarthome.config.discovery.inbox.Inbox
import org.eclipse.smarthome.config.discovery.inbox.InboxFilterCriteria
import org.eclipse.smarthome.core.thing.ThingUID
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import groovy.xml.XmlUtil

/**
 * Tests for {@link WemoLinkDiscoveryService}.
 *
 * @author Svilen Valkanov - Initial contribution
 */
class WemoLinkDiscoveryServiceOSGiTest extends GenericWemoLightOSGiTest{

    def inbox

    @Before
    void setUp() {
        setUpServices()

        inbox = getService(Inbox.class)
        assertThat inbox, is(notNullValue())

        servlet = new WemoLinkDiscoveryServlet(SERVICE_ID, SERVICE_NUMBER)
        registerServlet(SERVLET_URL, servlet)
    }

    @After
    void tearDown() {
        unregisterServlet(SERVLET_URL)
    }

    @Test
    @Ignore("see https://github.com/eclipse/smarthome/issues/4071")
    void 'assert supported thing is discovered' (){
        def bridgeTypeUID = WemoBindingConstants.THING_TYPE_BRIDGE
        def thingTypeUID = WemoBindingConstants.THING_TYPE_MZ100
        def model = WemoBindingConstants.THING_TYPE_MZ100.getId()

        servlet.friendlyName = DEVICE_FRIENDLY_NAME
        servlet.modelCode = model
        servlet.deviceId = DEVICE_UDN

        addUpnpDevice(SERVICE_ID, SERVICE_NUMBER, model)

        // This is needed, because the WemoLinkDiscoveryService is registered from the
        // WemoHandlerFactory, when a handler for a bridge is created
        createBridge(bridgeTypeUID)

        // The sleep is required, because the DiscoveryService is started after this interval
        sleep(WemoLinkDiscoveryService.INITIAL_DELAY * 1000)

        ThingUID bridgeUID = new ThingUID(bridgeTypeUID, WEMO_BRIDGE_ID);
        ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, DEVICE_UDN);

        waitForAssert{
            assertThat servlet.hasReceivedRequest, is(true)
        }

        waitForAssert {
            List<DiscoveryResult> results = inbox.get(new InboxFilterCriteria(thingUID, null))
            assertFalse results.isEmpty()
        }

    }

    class WemoLinkDiscoveryServlet extends GenericWemoHttpServlet {
        def hasReceivedRequest = false
        def deviceIndex
        def deviceId
        def friendlyName
        def manufacturer
        def modelCode


        WemoLinkDiscoveryServlet(String service, String serviceNumber) {
            super(service, serviceNumber)
            manufacturer = WemoBindingConstants.BINDING_ID
        }

        @Override
        protected String handleRequest(Node root) {
            def endDevices = root[soapNamespace.Body][uNamespace.GetEndDevices];

            if (endDevices.size() > 0) {
                def endDeviceNode = endDevices.get(0);

                hasReceivedRequest = true

                // Add information about a single device
                endDeviceNode.replaceNode {
                    DeviceLists {
                        DeviceInfo {
                            DeviceIndex(this.deviceIndex)
                            DeviceID(this.deviceId)
                            FriendlyName(this.friendlyName)
                            Manufacturer(this.manufacturer)
                            ModelCode(this.modelCode)
                        }
                    }
                }

                return XmlUtil.serialize(root)
            }
        }
    }
}
