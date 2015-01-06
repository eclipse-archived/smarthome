/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.util

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.thing.Channel
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.junit.Test

class ThingHelperTest {

	@Test
	void 'Two technical equal Thing instances are detected as "equal"'() {
		Thing thingA = ThingBuilder.create(new ThingUID(new ThingTypeUID("binding:type"), "thingId"))
				.withChannels(new Channel(new ChannelUID("binding:type:thingId:channel1"), "itemType"),
				new Channel(new ChannelUID("binding:type:thingId:channel2"), "itemType"))
				.withConfiguration(new Configuration())
				.build()
		thingA.getConfiguration().put("prop1", "value1")
		thingA.getConfiguration().put("prop2", "value2")

		assertTrue ThingHelper.equals(thingA, thingA)

		Thing thingB = ThingBuilder.create(new ThingUID(new ThingTypeUID("binding:type"), "thingId"))
				.withChannels(new Channel(new ChannelUID("binding:type:thingId:channel2"), "itemType"),
				new Channel(new ChannelUID("binding:type:thingId:channel1"), "itemType"))
				.withConfiguration(new Configuration())
				.build()
		thingB.getConfiguration().put("prop2", "value2")
		thingB.getConfiguration().put("prop1", "value1")

		assertTrue ThingHelper.equals(thingA, thingB)
	}

	@Test
	void 'Two things are different after properties were modified'() {
		Thing thingA = ThingBuilder.create(new ThingUID(new ThingTypeUID("binding:type"), "thingId"))
				.withChannels(new Channel(new ChannelUID("binding:type:thingId:channel1"), "itemType"),
				new Channel(new ChannelUID("binding:type:thingId:channel2"), "itemType"))
				.withConfiguration(new Configuration())
				.build()
		thingA.getConfiguration().put("prop1", "value1")

		Thing thingB = ThingBuilder.create(new ThingUID(new ThingTypeUID("binding:type"), "thingId"))
				.withChannels(new Channel(new ChannelUID("binding:type:thingId:channel2"), "itemType"),
				new Channel(new ChannelUID("binding:type:thingId:channel1"), "itemType"))
				.withConfiguration(new Configuration())
				.build()
		thingB.getConfiguration().put("prop1", "value1")

		assertTrue ThingHelper.equals(thingA, thingB)

		thingB.getConfiguration().put("prop3", "value3")

		assertFalse ThingHelper.equals(thingA, thingB)
	}

	@Test
	void 'Two things are different after channels were modified'() {
		Thing thingA = ThingBuilder.create(new ThingUID(new ThingTypeUID("binding:type"), "thingId"))
				.withConfiguration(new Configuration()).build()

		Thing thingB = ThingBuilder.create(new ThingUID(new ThingTypeUID("binding:type"), "thingId"))
				.withConfiguration(new Configuration()).build()

		assertTrue ThingHelper.equals(thingA, thingB)

		thingB.setChannels([
			new Channel(new ChannelUID("binding:type:thingId:channel3"), "itemType3")
		])

		assertFalse ThingHelper.equals(thingA, thingB)
	}

	@Test
	void 'Two things are different after name was modified'() {
		Thing thingA = ThingBuilder.create(new ThingUID(new ThingTypeUID("binding:type"), "thingId")).build()

		Thing thingB = ThingBuilder.create(new ThingUID(new ThingTypeUID("binding:type"), "thingId")).build()

		assertTrue ThingHelper.equals(thingA, thingB)

		thingB.setName("Thing B")

		assertFalse ThingHelper.equals(thingA, thingB)
	}
}
