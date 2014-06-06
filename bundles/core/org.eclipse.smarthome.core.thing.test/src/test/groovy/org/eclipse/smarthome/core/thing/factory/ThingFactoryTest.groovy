package org.eclipse.smarthome.core.thing.factory

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.BridgeType
import org.eclipse.smarthome.core.thing.ChannelType;
import org.eclipse.smarthome.core.thing.DescriptionTypeMetaInfo
import org.eclipse.smarthome.core.thing.ThingType;
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingFactory;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.junit.Test;

class ThingFactoryTest {

	def META_INFO = new DescriptionTypeMetaInfo("label", "description");

	@Test
	void 'create simple Thing'() {

		def thingType = new ThingType("bindingId", "thingTypeId", META_INFO, "manufacturer")
		def configuration = new Configuration();
		
		def thing = ThingFactory.createThing(thingType, new ThingUID(thingType.getUID(), "thingId"), configuration)

		assertThat thing.getUID().toString(), is(equalTo("bindingId:thingTypeId:thingId"))
		assertThat thing.getThingTypeUID().toString(), is(equalTo("bindingId:thingTypeId"))
		assertThat thing.getConfiguration(), is(not(null))
	}
	
	@Test
	void 'create simple Bridge'() {

		def thingType = new BridgeType("bindingId", "thingTypeId", META_INFO, "manufacturer")
		def configuration = new Configuration();
		
		def thing = ThingFactory.createThing(thingType, new ThingUID(thingType.getUID(), "thingId"), configuration)

		assertThat thing, is(instanceOf(Bridge))
	}
	
	@Test
	void 'create Thing with Bridge'() {

		def bridge = BridgeBuilder.create(new ThingTypeUID("binding:bridge"), "1").build();
		
		def thingType = new ThingType("bindingId", "thingTypeId", META_INFO, "manufacturer")
		def configuration = new Configuration();
		
		def thing = ThingFactory.createThing(thingType, new ThingUID(thingType.getUID(), "thingId"), configuration, bridge)

		assertThat thing.getBridge(), is(equalTo(bridge))
	}
	
	@Test
	void 'create Thing with Channels'() {

//		ChannelType channelType = new ChannelType(uid, itemType, metaInfo, configDescriptionURI)
//		
//		def thingType = new ThingType("bindingId", "thingTypeId", META_INFO, "manufacturer")
//		def configuration = new Configuration();
//		
//		def thing = ThingFactory.createThing(thingType, new ThingUID(thingType.getUID(), "thingId"), configuration, bridge)
//
//		assertThat thing.getBridge(), is(equalTo(bridge))
	}
}
