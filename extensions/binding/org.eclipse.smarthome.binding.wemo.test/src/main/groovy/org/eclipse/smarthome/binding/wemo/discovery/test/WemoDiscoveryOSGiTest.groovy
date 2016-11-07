/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.wemo.discovery.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.binding.wemo.WemoBindingConstants
import org.eclipse.smarthome.binding.wemo.test.GenericWemoOSGiTest
import org.eclipse.smarthome.config.discovery.DiscoveryResult
import org.eclipse.smarthome.config.discovery.inbox.Inbox
import org.eclipse.smarthome.config.discovery.inbox.InboxFilterCriteria
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.jupnp.model.meta.Device

/**
 * Tests for {@link WemoDiscoveryService}.
 *
 * @author Svilen Valkanov - Initial contribution
 */

class WemoDiscoveryOSGiTest extends GenericWemoOSGiTest{

    // UpnP service information
    def SERVICE_ID = 'basicevent'
    def SERVICE_NUMBER = '1'

    Inbox inbox

    @Before
    public void setUp() {
        setUpServices()

        inbox = getService(Inbox.class)
        assertThat (inbox, is(notNullValue()))
    }

    @After
    public void tearDown() {
        List<DiscoveryResult> results = inbox.getAll()
        assertThat "Inbox is not empty: ${Arrays.toString(results.toArray())}", results.size(), is(0)
    }

    @Test
    public void 'assert supported thing is discovered'() {
        def thingType = WemoBindingConstants.THING_TYPE_INSIGHT
        def model = WemoBindingConstants.THING_TYPE_INSIGHT.getId()

        addUpnpDevice(SERVICE_ID, SERVICE_NUMBER, model)

        waitForAssert {
            Collection<Device> devices =  mockUpnpService.getRegistry().getDevices()
            assertThat "Not exactly one UPnP device is  added to the UPnP Registry: ${devices}", devices.size(), is(1)
            Device device = devices.getAt(0)
            assertThat "UPnP device ${device} has incorrect model name:", device.getDetails().getModelDetails().getModelName(), is(model)
        }

        ThingUID thingUID = new ThingUID(thingType, DEVICE_UDN);

        waitForAssert {
            List<DiscoveryResult> results = inbox.get(new InboxFilterCriteria(thingUID, null))
            assertFalse "No Thing with UID " + thingUID.getAsString() + " in inbox", results.isEmpty()
        }

        inbox.approve(thingUID, DEVICE_FRIENDLY_NAME)

        waitForAssert {
            Thing thing = thingRegistry.get(thingUID)
            assertThat "Thing is not created when approved.", thing, is(notNullValue())
        }
    }
}
