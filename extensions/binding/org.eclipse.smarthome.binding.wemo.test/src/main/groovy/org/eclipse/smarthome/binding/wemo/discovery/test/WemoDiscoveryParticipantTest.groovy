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

import org.eclipse.smarthome.binding.wemo.WemoBindingConstants
import org.eclipse.smarthome.binding.wemo.discovery.WemoDiscoveryParticipant
import org.eclipse.smarthome.binding.wemo.test.GenericWemoOSGiTest
import org.eclipse.smarthome.config.discovery.DiscoveryResult
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.junit.Test
import org.jupnp.model.meta.DeviceDetails
import org.jupnp.model.meta.ManufacturerDetails
import org.jupnp.model.meta.ModelDetails
import org.jupnp.model.meta.RemoteDevice
import org.jupnp.model.meta.RemoteDeviceIdentity
import org.jupnp.model.types.DeviceType
import org.jupnp.model.types.UDN

/**
 * Tests for {@link WemoDiscoveryParticipant}.
 *
 * @author Svilen Valkanov - Initial contribution
 */

class WemoDiscoveryParticipantTest {
    UpnpDiscoveryParticipant participant = new WemoDiscoveryParticipant()

    def DEVICE_UDN = "${GenericWemoOSGiTest.DEVICE_MANUFACTURER}_3434xxx"
    def DEVICE_FRIENDLY_NAME = "Wemo Test"

    RemoteDevice createUpnpDevice(def modelName) {
        return new RemoteDevice(
                new RemoteDeviceIdentity(new UDN(DEVICE_UDN), 60, new URL("http://wemo"), null, null),
                new DeviceType("namespace", "type"),
                new DeviceDetails(DEVICE_FRIENDLY_NAME,
                new ManufacturerDetails(GenericWemoOSGiTest.DEVICE_MANUFACTURER),
                new ModelDetails(modelName), new URI("http://1.2.3.4/")))
    }

    @Test
    void 'assert discovery result for Socket is correct' () {
        testDiscoveryResult(WemoBindingConstants.THING_TYPE_SOCKET)
    }

    @Test
    void 'assert discovery result for Insight is correct' () {
        testDiscoveryResult(WemoBindingConstants.THING_TYPE_INSIGHT)
    }

    @Test
    void 'assert discovery result for Lightswitch is correct' () {
        testDiscoveryResult(WemoBindingConstants.THING_TYPE_LIGHTSWITCH)
    }

    @Test
    void 'assert discovery result for Motion is correct' () {
        testDiscoveryResult(WemoBindingConstants.THING_TYPE_MOTION)
    }

    @Test
    void 'assert discovery result for Bridge is correct' () {
        testDiscoveryResult(WemoBindingConstants.THING_TYPE_BRIDGE)
    }

    @Test
    void 'assert discovery result for Maker is correct' () {
        testDiscoveryResult(WemoBindingConstants.THING_TYPE_MAKER)
    }

    void testDiscoveryResult (ThingTypeUID thingTypeUid ) {
        def thingTypeId = thingTypeUid.getId()
        RemoteDevice device = createUpnpDevice(thingTypeId)
        DiscoveryResult result = participant.createResult(device)

        assertThat result.thingUID, is(new ThingUID(thingTypeUid, DEVICE_UDN))
        assertThat result.thingTypeUID, is(thingTypeUid)
        assertThat result.bridgeUID, is(nullValue())
        assertThat result.properties.get(WemoBindingConstants.UDN), is(DEVICE_UDN.toString())
        assertThat result.representationProperty, is(WemoBindingConstants.UDN)
    }
}
