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

import org.eclipse.smarthome.binding.hue.internal.discovery.HueBridgeDiscoveryParticipant
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.jupnp.model.meta.DeviceDetails
import org.jupnp.model.meta.ManufacturerDetails
import org.jupnp.model.meta.ModelDetails
import org.jupnp.model.meta.RemoteDevice
import org.jupnp.model.meta.RemoteDeviceIdentity
import org.jupnp.model.types.DeviceType
import org.jupnp.model.types.UDN

/**
 * Tests for {@link HueBridgeDiscoveryParticipant}.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Thomas Höfer - Added representation
 */
class HueBridgeDiscoveryParticipantOSGITest extends OSGiTest {

    UpnpDiscoveryParticipant discoveryParticipant

    RemoteDevice hueDevice
    RemoteDevice otherDevice

    @Before
    void setUp() {
        discoveryParticipant = getService(UpnpDiscoveryParticipant, HueBridgeDiscoveryParticipant)
        assertThat discoveryParticipant, is(notNullValue())

        hueDevice = new RemoteDevice(
                new RemoteDeviceIdentity(new UDN("123"), 60, new URL("http://hue"), null, null),
                new DeviceType("namespace", "type"),
                new DeviceDetails(
                new URL("http://1.2.3.4/"),
                "Hue Bridge",
                new ManufacturerDetails("Philips"),
                new ModelDetails("Philips hue bridge"),
                "serial123",
                "upc",
                null))

        otherDevice = new RemoteDevice(
                new RemoteDeviceIdentity(new UDN("567"), 60, new URL("http://acme"), null, null),
                new DeviceType("namespace", "type"),
                new DeviceDetails(
                "Some Device",
                new ManufacturerDetails("Taiwan"),
                new ModelDetails("��\$%&/"),
                "serial567",
                "upc"))
    }

    @After
    void cleanUp() {
    }

    @Test
    void 'assert correct supported types'() {
        assertThat discoveryParticipant.supportedThingTypeUIDs.size(), is(1)
        assertThat discoveryParticipant.supportedThingTypeUIDs.first(), is(THING_TYPE_BRIDGE)
    }

    @Test
    void 'assert correct thing UID'() {
        assertThat discoveryParticipant.getThingUID(hueDevice), is(new ThingUID("hue:bridge:serial123"))
    }

    @Test
    void 'assert valid DiscoveryResult'() {
        discoveryParticipant.createResult(hueDevice).with {
            assertThat flag, is (DiscoveryResultFlag.NEW)
            assertThat thingUID, is(new ThingUID("hue:bridge:serial123"))
            assertThat thingTypeUID, is (THING_TYPE_BRIDGE)
            assertThat bridgeUID, is(nullValue())
            assertThat properties.get(HOST), is("1.2.3.4")
            assertThat properties.get(SERIAL_NUMBER), is("serial123")
            assertThat representationProperty, is(SERIAL_NUMBER)
        }
    }

    @Test
    void 'assert no thing UID for unknown device'() {
        assertThat discoveryParticipant.getThingUID(otherDevice), is(nullValue())
    }

    @Test
    void 'assert no discovery result for unknown device'() {
        assertThat discoveryParticipant.createResult(otherDevice), is(nullValue())
    }
}
