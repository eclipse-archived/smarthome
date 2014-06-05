package org.eclipse.smarthome.binding.hue.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*
import nl.q42.jue.HttpClient
import nl.q42.jue.MockedHttpClient
import nl.q42.jue.HttpClient.Result

import org.eclipse.smarthome.binding.hue.internal.HueThingTypeProvider
import org.eclipse.smarthome.binding.hue.internal.handler.HueLightHandler
import org.eclipse.smarthome.binding.hue.internal.setup.HueBridgeContextKey
import org.eclipse.smarthome.binding.hue.internal.setup.HueLightContextKey
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.events.EventPublisher
import org.eclipse.smarthome.core.library.types.OnOffType
import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingProvider
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.util.ThingHelper
import org.eclipse.smarthome.test.AsyncResultWrapper
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Before
import org.junit.Test

class HueLightHandlerOSGiTest extends OSGiTest {

    ManagedThingProvider managedThingProvider

    @Before
    void setUp() {
        managedThingProvider = getService(ThingProvider, ManagedThingProvider)
        assertThat managedThingProvider, is(notNullValue())
    }


    @Test
    void 'assert that HueLightHandler is registered and unregistered'() {
        Configuration bridgeConfiguration = new Configuration().with {
            put(HueBridgeContextKey.IP.getKey(), "1.2.3.4")
            put(HueBridgeContextKey.USERNAME.getKey(), "testUserName")
            put(HueBridgeContextKey.BRIDGE_SERIAL_NUMBER.getKey(), "testSerialNumber")
            it
        }
        Bridge hueBridge = managedThingProvider.createThing(HueThingTypeProvider.BRIDGE_THING_TYPE, new ThingUID(HueThingTypeProvider.BRIDGE_THING_TYPE.getUID(), "testBridge"), null, bridgeConfiguration)
        assertThat hueBridge, is(notNullValue())


        HueLightHandler hueLightHandler = getService(ThingHandler, HueLightHandler)
        assertThat hueLightHandler, is(nullValue())
        Configuration lightConfiguration = new Configuration().with {
            put(HueLightContextKey.LIGHT_ID.getKey(), "1")
            it
        }
        Thing hueLight = managedThingProvider.createThing(HueThingTypeProvider.LIGHT_THING_TYPE, new ThingUID(HueThingTypeProvider.LIGHT_THING_TYPE.getUID(), "Light1"), hueBridge, lightConfiguration)
        assertThat hueLight, is(notNullValue())

        // wait for HueLightHandler to be registered
        waitForAssert({
            hueLightHandler = getService(ThingHandler, HueLightHandler)
            assertThat hueLightHandler, is(notNullValue())
        }, 10000)

        managedThingProvider.removeThing(hueLight.getUID())
        hueLightHandler = getService(ThingHandler, HueLightHandler)
        // wait for HueLightHandler to be unregistered
        waitForAssert({
            hueLightHandler = getService(ThingHandler, HueLightHandler)
            assertThat hueLightHandler, is(nullValue())
        }, 10000)

        managedThingProvider.removeThing(hueBridge.getUID())
    }

    @Test
    void 'assert json call for handleCommand'() {

        Configuration bridgeConfiguration = new Configuration().with {
            put(HueBridgeContextKey.IP.getKey(), "1.2.3.4")
            put(HueBridgeContextKey.USERNAME.getKey(), "testUserName")
            put(HueBridgeContextKey.BRIDGE_SERIAL_NUMBER.getKey(), "testSerialNumber")
            it
        }
        Bridge hueBridge = managedThingProvider.createThing(HueThingTypeProvider.BRIDGE_THING_TYPE, new ThingUID(HueThingTypeProvider.BRIDGE_THING_TYPE.getUID(), "testBridge"), null, bridgeConfiguration)
        assertThat hueBridge, is(notNullValue())


        HueLightHandler hueLightHandler = getService(ThingHandler, HueLightHandler)
        assertThat hueLightHandler, is(nullValue())
        Configuration lightConfiguration = new Configuration().with {
            put(HueLightContextKey.LIGHT_ID.getKey(), "1")
            it
        }
        Thing hueLight = managedThingProvider.createThing(HueThingTypeProvider.LIGHT_THING_TYPE, new ThingUID(HueThingTypeProvider.LIGHT_THING_TYPE.getUID(), "Light1"), hueBridge, lightConfiguration)
        assertThat hueLight, is(notNullValue())

        // wait for HueLightHandler to be registered
        waitForAssert({
            hueLightHandler = getService(ThingHandler, HueLightHandler)
            assertThat hueLightHandler, is(notNullValue())
        }, 10000)

        // mock HttpClient
        def hueBridgeHandler = hueLightHandler.getHueBridgeHandler()
        def hueBridgeField = hueBridgeHandler.getClass().getDeclaredField("bridge")
        hueBridgeField.accessible = true
        waitForAssert({
            hueLightHandler = getService(ThingHandler, HueLightHandler)
            assertThat hueBridgeField.get(hueBridgeHandler), is(notNullValue())
        }, 10000)
        def hueBridgeValue = hueBridgeField.get(hueBridgeHandler)
        def httpClientField = hueBridgeValue.getClass().getDeclaredField("http")
        httpClientField.accessible = true
        def AsyncResultWrapper<String> addressWrapper = new AsyncResultWrapper<String>()
        def AsyncResultWrapper<String> bodyWrapper = new AsyncResultWrapper<String>()
        MockedHttpClient mockedHttpClient =  [
            put: { String address, String body ->
                addressWrapper.set(address)
                bodyWrapper.set(body)
                new Result("", 200)
            },
            get: { String address ->
                if (address.endsWith("lights")) {
                    def body = """
						{
						  "1": {
						    "name": "Hue Light 1"
						  }
						}
					"""
                    new Result(body, 200)
                } else if (address.endsWith("lights/1")) {
                    def body = """
						{
						  "state": {
						    "on": true,
						    "bri": 200,
						    "hue": 50000,
						    "sat": 0,
						    "xy": [
						      0,
						      0
						    ],
						    "ct": 0,
						    "alert": "none",
						    "effect": "none",
						    "colormode": "hs",
						    "reachable": true
						  },
						  "type": "Extended color light",
						  "name": "Hue Light 1",
						  "modelid": "LCT001",
						  "swversion": "65003148",
						  "pointsymbol": {
						    "1": "none",
						    "2": "none",
						    "3": "none",
						    "4": "none",
						    "5": "none",
						    "6": "none",
						    "7": "none",
						    "8": "none"
						  }
						}
					"""
                    new Result(body, 200)
                }
            }
        ] as MockedHttpClient


        httpClientField.set(hueBridgeValue, mockedHttpClient)
        hueBridgeHandler.initialize()

        // Create items and channel bindings
        ThingHelper thingHelper = new ThingHelper(bundleContext)

        thingHelper.createAndBindItems(hueLight)
        def colorItem = hueLight.getUID().toString().replace(":", "_") + "_color"


        EventPublisher eventPublisher = getService(EventPublisher)
        assertThat eventPublisher, is(notNullValue())
        eventPublisher.postCommand(colorItem, OnOffType.ON)

        waitForAssert({assertTrue addressWrapper.isSet}, 10000)
        waitForAssert({assertTrue bodyWrapper.isSet}, 10000)

        assertThat addressWrapper.wrappedObject, is("http://1.2.3.4/api/testUserName/lights/1/state")
        def jsonSlurper = Class.forName("groovy.json.JsonSlurper").newInstance()
        def actualResult = jsonSlurper.parseText(bodyWrapper.wrappedObject)
        def expectedResult = jsonSlurper.parseText("""
			{
			    "on": true
			}
		""")

        assertThat actualResult, is(expectedResult)

        managedThingProvider.removeThing(hueLight.getUID())
        managedThingProvider.removeThing(hueBridge.getUID())
    }
}
