/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class ThingManagerOSGiTest extends OSGiTest {

	ManagedThingProvider managedThingProvider

	Thing THING = ThingBuilder.create(new ThingTypeUID("binding:type"), "id").withChannels([]).build()

	@Before
	void setUp() {
		managedThingProvider = getService(ManagedThingProvider)
	}
	
	@After
	void teardown() {
		managedThingProvider.getThings().each {
			managedThingProvider.removeThing(it.getUID())
		}
	}

	@Test
	void 'ThingManager calls registerHandler for added Thing'() {

		def registerHandlerCalled = false

		def thingHandlerFactory = [
			supportsThingType: {ThingTypeUID thingTypeUID -> true},
			registerHandler: {Thing thing -> registerHandlerCalled = true}
		] as ThingHandlerFactory

		registerService(thingHandlerFactory)

		managedThingProvider.addThing(THING)

		waitForAssert {assertThat registerHandlerCalled, is(true)}
	}

	@Test
	void 'ThingManager calls unregisterHandler for removed Thing'() {

		def unregisterHandlerCalled = false

		def thingHandlerFactory = [
			supportsThingType: {ThingTypeUID thingTypeUID -> true},
			registerHandler: {
				def thingHandler = [] as ThingHandler
				registerService(thingHandler,[
					(ThingHandler.SERVICE_PROPERTY_THING_ID): THING.getUID(),
					(ThingHandler.SERVICE_PROPERTY_THING_TYPE): THING.getThingTypeUID()
				] as Hashtable)
			},
			unregisterHandler: {Thing thing -> unregisterHandlerCalled = true}
		] as ThingHandlerFactory

		registerService(thingHandlerFactory)

		managedThingProvider.addThing(THING)

		managedThingProvider.removeThing(THING.getUID())

		waitForAssert {assertThat unregisterHandlerCalled, is(true)}
	}

	@Test
	void 'ThingManager tracks handler for Thing'() {

		def registerHandlerCalled = false

		managedThingProvider.addThing(THING)
		assertThat THING.getStatus(), is(not(ThingStatus.ONLINE))

		def thingHandler = [] as ThingHandler
		registerService(thingHandler,[
			(ThingHandler.SERVICE_PROPERTY_THING_ID): THING.getUID(),
			(ThingHandler.SERVICE_PROPERTY_THING_TYPE): THING.getThingTypeUID()
		] as Hashtable)

		assertThat THING.getStatus(), is(ThingStatus.ONLINE)

		unregisterService(thingHandler)

		assertThat THING.getStatus(), is(ThingStatus.OFFLINE)
	}
}
