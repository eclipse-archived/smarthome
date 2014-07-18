package org.eclipse.smarthome.model.thing.tests

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.Channel
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.model.core.ModelRepository
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class GenericThingProviderTest extends OSGiTest {

	private final static String TESTMODEL_NAME = "testModel.things"
	
	ModelRepository modelRepository
	ThingRegistry thingRegistry
	
	@Before
	void setUp() {
		thingRegistry = getService ThingRegistry
		assertThat thingRegistry, is(notNullValue())
		modelRepository = getService ModelRepository
		assertThat modelRepository, is(notNullValue())
		modelRepository.removeModel(TESTMODEL_NAME)
	}
	
	@After
	void tearDown() {
		modelRepository.removeModel(TESTMODEL_NAME)
	}
	
	@Test
	void 'assert that things that are contained in things files are added to ThingRegistry'() {

		def things = thingRegistry.things
		assertThat things.size(), is(0)

		String model =
			'''
			Bridge hue:bridge:myBridge [ ip = "1.2.3.4", username = "123" ] {
				light bulb1 [ lightId = "1" ] { Switch : notification }
				Bridge bridge myBridge2 [ ] {
					light bulb2 [ ]
				}
			}
	
			hue:light:bulb3 [ lightId = "4" ] {
				Switch : notification [ duration = "5" ]
			}
			'''
		modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))
		def actualThings = thingRegistry.things

		assertThat actualThings.size(), is(5)

		def bridge1 = actualThings.find {
			"hue:bridge:myBridge".equals(it.UID.toString())
		}

		assertThat bridge1, isA(Bridge)
		assertThat bridge1.channels.size(), is(0)
		//assertThat bridge.status, is(ThingStatus.ONLINE)
		assertThat bridge1.bridgeUID, is(nullValue())
		assertThat bridge1.configuration.values().size(), is(2)
		assertThat bridge1.configuration.get("ip"), is("1.2.3.4")
		assertThat bridge1.configuration.get("username"), is("123")
		assertThat bridge1.name, is(nullValue())
		assertThat bridge1.thingTypeUID.toString(), is("hue:bridge")

		def bridge2 = actualThings.find {
			"hue:bridge:myBridge:myBridge2".equals(it.UID.toString())
		}

		assertThat bridge2, isA(Bridge)
		assertThat bridge2.channels.size(), is(0)
		//assertThat bridge.status, is(ThingStatus.ONLINE)
		assertThat bridge2.bridgeUID, is(bridge1.UID)
		assertThat bridge2.configuration.values().size(), is(0)
		assertThat bridge2.name, is(nullValue())
		assertThat bridge2.thingTypeUID.toString(), is("hue:bridge")

		def bulb1 = actualThings.find {
			"hue:light:myBridge:bulb1".equals(it.UID.toString())
		}

		assertThat bulb1, isA(Thing)
		assertThat bulb1.channels.size(), is(1)
		Channel firstChannel = bulb1.channels.first()
		assertThat firstChannel.uid.toString(), is("hue:light:myBridge:bulb1:notification")
		assertThat firstChannel.acceptedItemType, is("Switch")
		//assertThat bridge.status, is(ThingStatus.ONLINE)
		assertThat bulb1.bridgeUID, is(bridge1.UID)
		assertThat bulb1.configuration.values().size(), is(1)
		assertThat bulb1.configuration.get("lightId"), is("1")
		assertThat bulb1.name, is(nullValue())
		assertThat bulb1.thingTypeUID.toString(), is("hue:light")

		def bulb2 = actualThings.find {
			"hue:light:myBridge:myBridge2:bulb2".equals(it.UID.toString())
		}

		assertThat bulb2, isA(Thing)
		assertThat bulb2.channels.size(), is(0)
		//assertThat bridge.status, is(ThingStatus.ONLINE)
		assertThat bulb2.bridgeUID, is(bridge2.UID)
		assertThat bulb2.configuration.values().size(), is(0)
		assertThat bulb2.name, is(nullValue())
		assertThat bulb2.thingTypeUID.toString(), is("hue:light")

		def bulb3 = actualThings.find {
			"hue:light:bulb3".equals(it.UID.toString())
		}

		assertThat bulb3, isA(Thing)
		assertThat bulb3.channels.size(), is(1)
		firstChannel = bulb3.channels.first()
		assertThat firstChannel.uid.toString(), is("hue:light:bulb3:notification")
		assertThat firstChannel.acceptedItemType, is("Switch")
		assertThat firstChannel.configuration.values().size(), is(1)
		assertThat firstChannel.configuration.get("duration"), is("5")
		//assertThat bridge.status, is(ThingStatus.ONLINE)
		assertThat bulb3.bridgeUID, is(nullValue())
		assertThat bulb3.configuration.values().size(), is(1)
		assertThat bulb3.configuration.get("lightId"), is("4")
		assertThat bulb3.name, is(nullValue())
		assertThat bulb3.thingTypeUID.toString(), is("hue:light")
	}

	@Test
	void 'assert that the things in an updated things file is registered in the ThingRegistry'() {
		ThingRegistry thingRegistry = getService ThingRegistry
		assertThat thingRegistry, is(notNullValue())
		def things = thingRegistry.things
		assertThat things.size(), is(0)
		ModelRepository modelRepository = getService ModelRepository
		assertThat modelRepository, is(notNullValue())
		String model =
			'''
			Bridge hue:bridge:myBridge [ ip = "1.2.3.4", username = "123" ]  {
				light bulb1 [ lightId = "1" ] { Switch : notification }
				Bridge bridge myBridge2 [ ] {
					light bulb2 [ ]
				}
			}
	
			hue:light:bulb3 [ lightId = "4" ] {
				Switch : notification [ duration = "5" ]
			}
			'''
		modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))
		String newModel =
			'''
			Bridge hue:bridge:myBridge [ ip = "5.6.7.8", secret = "123" ] {
				light bulb1 [ ]
			}
	
			hue:light:bulb2 [ lightId = "2" ] {
				Color : color
			}
			'''
		modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(newModel.bytes))
		def actualThings = thingRegistry.things

		assertThat actualThings.size(), is(3)

		def bridge1 = actualThings.find {
			"hue:bridge:myBridge".equals(it.UID.toString())
		}

		assertThat bridge1, isA(Bridge)
		assertThat bridge1.channels.size(), is(0)
		//assertThat bridge.status, is(ThingStatus.ONLINE)
		assertThat bridge1.bridgeUID, is(nullValue())
		assertThat bridge1.configuration.values().size(), is(2)
		assertThat bridge1.configuration.get("ip"), is("5.6.7.8")
		assertThat bridge1.configuration.get("secret"), is("123")
		assertThat bridge1.name, is(nullValue())
		assertThat bridge1.thingTypeUID.toString(), is("hue:bridge")

		def bulb1 = actualThings.find {
			"hue:light:myBridge:bulb1".equals(it.UID.toString())
		}

		assertThat bulb1, isA(Thing)
		assertThat bulb1.channels.size(), is(0)
		//assertThat bridge.status, is(ThingStatus.ONLINE)
		assertThat bulb1.bridgeUID, is(bridge1.UID)
		assertThat bulb1.configuration.values().size(), is(0)
		assertThat bulb1.name, is(nullValue())
		assertThat bulb1.thingTypeUID.toString(), is("hue:light")

		def bulb2 = actualThings.find {
			"hue:light:bulb2".equals(it.UID.toString())
		}

		assertThat bulb2, isA(Thing)
		assertThat bulb2.channels.size(), is(1)
		Channel firstChannel = bulb2.channels.first()
		assertThat firstChannel.uid.toString(), is("hue:light:bulb2:color")
		assertThat firstChannel.acceptedItemType, is("Color")
		//assertThat bridge.status, is(ThingStatus.ONLINE)
		assertThat bulb2.bridgeUID, is(nullValue())
		assertThat bulb2.configuration.values().size(), is(1)
		assertThat bulb2.configuration.get("lightId"), is("2")
		assertThat bulb2.name, is(nullValue())
		assertThat bulb2.thingTypeUID.toString(), is("hue:light")

	}
}
