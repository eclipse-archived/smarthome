/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.BridgeType
import org.eclipse.smarthome.core.thing.ChannelDefinition
import org.eclipse.smarthome.core.thing.ChannelType
import org.eclipse.smarthome.core.thing.ChannelTypeUID
import org.eclipse.smarthome.core.thing.ThingType
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder
import org.junit.Test

class ThingFactoryTest {

	@Test
	void 'create simple Thing'() {

		def thingType = new ThingType("bindingId", "thingTypeId", "label", "description", "manufacturer")
		def configuration = new Configuration();

		def thing = ThingFactory.createThing(thingType, new ThingUID(thingType.getUID(), "thingId"), configuration)

		assertThat thing.getUID().toString(), is(equalTo("bindingId:thingTypeId:thingId"))
		assertThat thing.getThingTypeUID().toString(), is(equalTo("bindingId:thingTypeId"))
		assertThat thing.getConfiguration(), is(not(null))
	}

	@Test
	void 'create simple Bridge'() {

		def thingType = new BridgeType("bindingId", "thingTypeId", "label", "description", "manufacturer")
		def configuration = new Configuration();

		def thing = ThingFactory.createThing(thingType, new ThingUID(thingType.getUID(), "thingId"), configuration)

		assertThat thing, is(instanceOf(Bridge))
	}

	@Test
	void 'create Thing with Bridge'() {

		def bridge = BridgeBuilder.create(new ThingTypeUID("binding:bridge"), "1").build();

		def thingType = new ThingType("bindingId", "thingTypeId", "label", "description", "manufacturer")
		def configuration = new Configuration();

		def thing = ThingFactory.createThing(thingType, new ThingUID(thingType.getUID(), "thingId"), configuration, bridge)

		assertThat thing.getBridge(), is(equalTo(bridge))
	}

	@Test
	void 'create Thing with Channels'() {

		ChannelType channelType1 = new ChannelType(new ChannelTypeUID("bindingId:channelTypeId1"), "Color", "label", "description", null)
		ChannelType channelType2 = new ChannelType(new ChannelTypeUID("bindingId:channelTypeId2"), "Dimmer", "label", "description", null)

		ChannelDefinition channelDef1 = new ChannelDefinition("ch1", channelType1)
		ChannelDefinition channelDef2 = new ChannelDefinition("ch2", channelType2)
		
		def thingType = new ThingType(new ThingTypeUID("bindingId:thingType"), [], "label", "description", "manufacturer", [channelDef1, channelDef2], null)
		def configuration = new Configuration();

		def thing = ThingFactory.createThing(thingType, new ThingUID(thingType.getUID(), "thingId"), configuration)

		assertThat thing.getChannels().size, is(2)
		assertThat thing.getChannels().get(0).getUID().toString(), is(equalTo("bindingId:thingType:thingId:ch1"))
		assertThat thing.getChannels().get(0).getAcceptedItemType(), is(equalTo("Color"))
	}
}
