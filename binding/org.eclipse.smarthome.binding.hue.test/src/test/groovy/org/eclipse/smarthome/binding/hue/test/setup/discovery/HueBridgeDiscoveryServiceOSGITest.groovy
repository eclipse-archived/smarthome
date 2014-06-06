/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.test.setup.discovery

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.binding.hue.internal.HueThingTypeProvider
import org.eclipse.smarthome.binding.hue.internal.setup.HueBridgeContextKey
import org.eclipse.smarthome.binding.hue.internal.setup.discovery.bridge.HueBridgeDiscoveryService
import org.eclipse.smarthome.config.discovery.DiscoveryListener
import org.eclipse.smarthome.config.discovery.DiscoveryResult
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag
import org.eclipse.smarthome.config.discovery.DiscoveryService
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.test.AsyncResultWrapper
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.service.device.Constants
import org.osgi.service.upnp.UPnPDevice


/**
 * Tests for {@link HueBridgeDiscoveryService}.
 *
 * @author Oliver Libutzki - Initial contribution
 */
class HueBridgeDiscoveryServiceOSGITest extends OSGiTest {

    DiscoveryService discoveryService
    DiscoveryListener discoveryListener

    private final static String IP_ADDRESS = "UPnP.device.IP"
    private final static String PORT = "UPnP.device.PORT"

    @Before
    void setUp() {
        discoveryService = getService(DiscoveryService, HueBridgeDiscoveryService)
        assertThat discoveryService, is(notNullValue())
        discoveryService.autoDiscoveryEnabled = true
        unregisterCurrentDiscoveryListener()
    }

    @After
    void cleanUp() {
        unregisterCurrentDiscoveryListener()
    }

    private void registerDiscoveryListener(DiscoveryListener discoveryListener) {
        unregisterCurrentDiscoveryListener()
        this.discoveryListener = discoveryListener
        discoveryService.addDiscoveryListener(this.discoveryListener)
    }

    private void unregisterCurrentDiscoveryListener() {
        if (this.discoveryListener != null) {
            discoveryService.removeDiscoveryListener(this.discoveryListener)
        }
    }

    @Test
    void 'assert hue bridge registration'() {
        def AsyncResultWrapper<DiscoveryResult> resultWrapper = new AsyncResultWrapper<DiscoveryResult>()
        registerDiscoveryListener( [
            thingDiscovered: { DiscoveryService source, DiscoveryResult result ->
                resultWrapper.set(result)
            },
            thingRemoved: { DiscoveryService source, ThingUID thingId ->
            },
            discoveryFinished: { DiscoveryService source ->
            },
            discoveryErrorOccurred: { DiscoveryService source, Exception exception ->
            }
        ] as DiscoveryListener)
        registerHueBridge()

        waitForAssert{assertTrue resultWrapper.isSet}

        resultWrapper.wrappedObject.with {
            assertThat flag, is (DiscoveryResultFlag.NEW)
            assertThat label, is ("00178814e058 (192.168.3.99)")
            assertThat thingTypeUID, is (HueThingTypeProvider.BRIDGE_THING_TYPE.getUID())
            assertThat properties.get(HueBridgeContextKey.IP.getKey()), is ("192.168.3.99")
            assertThat properties.get(HueBridgeContextKey.BRIDGE_SERIAL_NUMBER.getKey()), is ("00178814e058")
        }
    }

    @Test
    void 'assert hue bridge deregistration'() {
        def AsyncResultWrapper<ThingUID> discoveredThingIdWrapper= new AsyncResultWrapper<ThingUID>()
        def AsyncResultWrapper<ThingUID> removedThingIdWrapper = new AsyncResultWrapper<ThingUID>()
        registerDiscoveryListener([
            thingDiscovered: { DiscoveryService source, DiscoveryResult result ->
                discoveredThingIdWrapper.set(result.thingUID)
            },
            thingRemoved: { DiscoveryService source, ThingUID thingUID ->
                removedThingIdWrapper.set(thingUID)
            },
            discoveryFinished: { DiscoveryService source ->
            },
            discoveryErrorOccurred: { DiscoveryService source, Exception exception ->
            }
        ] as DiscoveryListener)
        def hueBridge = registerHueBridge()

        waitForAssert{assertTrue discoveredThingIdWrapper.isSet}

        unregisterService(hueBridge)
        waitForAssert{assertTrue removedThingIdWrapper.isSet}

        assertThat removedThingIdWrapper.wrappedObject, is (discoveredThingIdWrapper.wrappedObject)
    }
    @Test
    void 'assert fritzbox registration'() {
        def AsyncResultWrapper<DiscoveryResult> discoveryResultWrapper= new AsyncResultWrapper<DiscoveryResult>()
        registerDiscoveryListener([
            thingDiscovered: { DiscoveryService source, DiscoveryResult result ->
                discoveryResultWrapper.set(result)
            },
            thingRemoved: { DiscoveryService source, ThingUID thingId ->
            },
            discoveryFinished: { DiscoveryService source ->
            },
            discoveryErrorOccurred: { DiscoveryService source, Exception exception ->
            }
        ] as DiscoveryListener)
        registerFritzbox()
        waitFor{discoveryResultWrapper.isSet}
        assertFalse discoveryResultWrapper.isSet
    }

    @Test
    void 'assert fritzbox deregistration'() {
        def AsyncResultWrapper<String> discoveredThingId = new AsyncResultWrapper<String>()
        def AsyncResultWrapper<String> removedThingId = new AsyncResultWrapper<String>()
        registerDiscoveryListener([
            thingDiscovered: { DiscoveryService source, DiscoveryResult result ->
                discoveredThingId.set(result.thingUID)
            },
            thingRemoved: { DiscoveryService source, ThingUID thingId ->
                removedThingId.set(thingId)
            },
            discoveryFinished: { DiscoveryService source ->
            },
            discoveryErrorOccurred: { DiscoveryService source, Exception exception ->
            }
        ] as DiscoveryListener)


        def fritzbox = registerFritzbox()

        waitFor{discoveredThingId.isSet}
        assertFalse discoveredThingId.isSet

        unregisterService(fritzbox)
        waitFor{removedThingId.isSet}
        assertFalse removedThingId.isSet
    }

    @Test
    void 'assert hue bridge registration without auto-recovery'() {
        discoveryService.setAutoDiscoveryEnabled(false)
        def AsyncResultWrapper<DiscoveryResult> resultWrapper = new AsyncResultWrapper<DiscoveryResult>()

        registerDiscoveryListener([
            thingDiscovered: { DiscoveryService source, DiscoveryResult result ->
                resultWrapper.set(result)
            },
            thingRemoved: { DiscoveryService source, ThingUID thingId ->
            },
            discoveryFinished: { DiscoveryService source ->
            },
            discoveryErrorOccurred: { DiscoveryService source, Exception exception ->
            }
        ] as DiscoveryListener)
        registerHueBridge()

        waitFor{resultWrapper.isSet}
        assertFalse resultWrapper.isSet
    }

    @Test
    void 'assert hue bridge registration with forced discovery'() {
        discoveryService.setAutoDiscoveryEnabled(false)
        def AsyncResultWrapper<DiscoveryResult> resultWrapper = new AsyncResultWrapper<DiscoveryResult>()
        def AsyncResultWrapper<Boolean> discoveryFinishedWrapper = new AsyncResultWrapper<Boolean>()
        registerDiscoveryListener([
            thingDiscovered: { DiscoveryService source, DiscoveryResult result ->
                resultWrapper.set(result)
            },
            thingRemoved: { DiscoveryService source, ThingUID thingUID ->
            },
            discoveryFinished: { DiscoveryService source ->
                discoveryFinishedWrapper.set(Boolean.TRUE)
            },
            discoveryErrorOccurred: { DiscoveryService source, Exception exception ->
            }
        ] as DiscoveryListener)
        registerHueBridge()

        waitFor{resultWrapper.isSet}
        assertFalse resultWrapper.isSet

        discoveryService.forceDiscovery()
        waitForAssert{ assertTrue resultWrapper.isSet }
        discoveryService.abortForceDiscovery()

        waitForAssert { assertTrue discoveryFinishedWrapper.isSet }
        assertTrue discoveryFinishedWrapper.wrappedObject == Boolean.TRUE

        resultWrapper.wrappedObject.with {
            assertThat flag, is (DiscoveryResultFlag.NEW)
            assertThat label, is ("00178814e058 (192.168.3.99)")
            assertThat thingTypeUID, is (HueThingTypeProvider.BRIDGE_THING_TYPE.getUID())
            assertThat properties.get(HueBridgeContextKey.IP.getKey()), is ("192.168.3.99")
            assertThat properties.get(HueBridgeContextKey.BRIDGE_SERIAL_NUMBER.getKey()), is ("00178814e058")
        }
    }

    def private UPnPDevice registerHueBridge() {
        def Hashtable<String, String>properties  = [
            (Constants.DEVICE_CATEGORY)		:	"UPnP",
            (IP_ADDRESS)  					:	"192.168.3.99",
            (PORT)							:	"80",
            (UPnPDevice.UDN)  				:	"uuid:2f402f80-da50-11e1-9b23-00178814e058",
            (UPnPDevice.FRIENDLY_NAME)  	: 	"Philips hue (192.168.3.99)",
            (UPnPDevice.MANUFACTURER)		:	"Royal Philips Electronics",
            (UPnPDevice.MANUFACTURER_URL)	: 	"http://www.philips.com",
            (UPnPDevice.MODEL_DESCRIPTION)  : 	"Philips hue Personal Wireless Lighting",
            (UPnPDevice.MODEL_NAME)  		:	"Philips hue bridge 2012",
            (UPnPDevice.MODEL_NUMBER)		: 	"929000226503",
            (UPnPDevice.MODEL_URL)			: 	"http://www.meethue.com",
            (UPnPDevice.SERIAL_NUMBER)		: 	"00178814e058",
            (UPnPDevice.TYPE)				: 	"urn:schemas-upnp-org:device:Basic:1:1",
            (UPnPDevice.PRESENTATION_URL)	:	"http://192.168.3.99:80/index.html"
        ]
        UPnPDevice hueBridgeDevice = {} as UPnPDevice
        registerService(hueBridgeDevice, properties)
        hueBridgeDevice
    }

    def private UPnPDevice registerFritzbox() {
        def Hashtable<String, String>properties = [
            (Constants.DEVICE_CATEGORY)		:	"UPnP",
            (IP_ADDRESS)  					:	"192.168.3.1",
            (PORT)							:	"49000",
            (UPnPDevice.UDN)  				:	"uid:95802409-bccb-40e7-8e6c-9CC7A640D187",
            (UPnPDevice.FRIENDLY_NAME)  	: 	"FRITZ!Box 3390",
            (UPnPDevice.MANUFACTURER)		:	"AVM Berlin",
            (UPnPDevice.MANUFACTURER_URL)	: 	"http://www.avm.de",
            (UPnPDevice.MODEL_DESCRIPTION)  : 	"FRITZ!Box 3390",
            (UPnPDevice.MODEL_NAME)  		:	"FRITZ!Box 3390",
            (UPnPDevice.MODEL_NUMBER)		: 	"avm",
            (UPnPDevice.MODEL_URL)			: 	"http://www.avm.de",
            (UPnPDevice.TYPE)				: 	"urn:schemas-upnp-org:device:l2tpv3:1:1",
            (UPnPDevice.PRESENTATION_URL)	:	"http://fritz.box"
        ]
        UPnPDevice fritzboxDevice = {} as UPnPDevice
        registerService(fritzboxDevice, properties)
        fritzboxDevice
    }
}
