/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.factory

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.DescriptionTypeMetaInfo
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingChangeListener
import org.eclipse.smarthome.core.thing.ThingProvider
import org.eclipse.smarthome.core.thing.ThingType
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.test.AsyncResultWrapper
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Tests for {@link ManagedThingProvider}.
 * @author Oliver Libutzki - Initital contribution
 *
 */
class ManagedThingProviderOSGiTest extends OSGiTest {

	
	ManagedThingProvider managedThingProvider
	
	ThingChangeListener thingChangeListener
	ThingHandlerFactory thingHandlerFactory
	
	final static String BINDIND_ID = "testBinding"
	final static String THING_TYPE_ID = "testThingType"
	final static ThingTypeUID THING_TYPE_UID = new ThingTypeUID(BINDIND_ID, THING_TYPE_ID)
	final static String THING1_ID = "testThing1"
	final static String THING2_ID = "testThing2"
	
	@Before
	void setup() {
		managedThingProvider = getService ManagedThingProvider
		assertThat managedThingProvider, is(notNullValue())
		unregisterCurrentThingChangeListener()
		unregisterCurrentThingHandlerFactory()
	}
	
	@After
	void teardown() {
		unregisterCurrentThingChangeListener()
		unregisterCurrentThingHandlerFactory()
		managedThingProvider.getThings().each {
			managedThingProvider.removeThing(it.getUID())
		} 
	}
	
	
	private void registerThingChangeListener(ThingChangeListener thingChangeListener) {
		unregisterCurrentThingChangeListener()
		this.thingChangeListener = thingChangeListener
		managedThingProvider.addThingChangeListener(this.thingChangeListener)
	}

	private void unregisterCurrentThingChangeListener() {
		if (this.thingChangeListener != null) {
			managedThingProvider.removeThingChangeListener(this.thingChangeListener)
		}
	}
	
	private void registerThingHandlerFactory(ThingHandlerFactory thingHandlerFactory) {
		unregisterCurrentThingHandlerFactory()
		this.thingHandlerFactory = thingHandlerFactory
		registerService(thingHandlerFactory, ThingHandlerFactory.class.name)
	}
	
	private void unregisterCurrentThingHandlerFactory() {
		if (this.thingHandlerFactory != null) {
			unregisterService(thingHandlerFactory)
		}
	}
	
	@Test
	void 'assert that added thing is returned by getThings'() {
		def thing1 = ThingBuilder.create(THING_TYPE_UID, THING1_ID).build()
		managedThingProvider.addThing(thing1)
		def things = managedThingProvider.getThings()
		assertThat things.size(), is(1)
		assertThat things.first(), is(thing1)
		def thing2 = ThingBuilder.create(THING_TYPE_UID, THING2_ID).build()
		managedThingProvider.addThing(thing2)
		things = managedThingProvider.getThings()
		assertThat things.size(), is(2)
		assertThat things.getAt(0), is(thing1)
		assertThat things.getAt(1), is(thing2)
		
	}
	
	@Test
	void 'assert that twice added thing is returned once by getThings'() {
		def thing1 = ThingBuilder.create(THING_TYPE_UID, THING1_ID).build()
		def thing2 = ThingBuilder.create(THING_TYPE_UID, THING1_ID).build()
		managedThingProvider.addThing(thing1)
		managedThingProvider.addThing(thing2)
		def things = managedThingProvider.getThings()
		assertThat things.size(), is(1)
		assertThat things.first(), is(thing2)
	}
	
	@Test
	void 'assert that removed thing is not returned by getThings'() {
		def thing = ThingBuilder.create(THING_TYPE_UID, THING1_ID).build()
		managedThingProvider.addThing(thing)
		managedThingProvider.removeThing(thing.getUID())
		def things = managedThingProvider.getThings()
		assertThat things.size(), is(0)
	}
	
	
	@Test
	void 'assert that ThingChangeListener is notified about added thing'() {
		AsyncResultWrapper<ThingProvider> thingProviderWrapper = new AsyncResultWrapper<ThingProvider>()
		AsyncResultWrapper<Thing> thingWrapper = new AsyncResultWrapper<Thing>()
		registerThingChangeListener( [
			thingAdded : { ThingProvider provider, Thing thing ->
				thingProviderWrapper.set(provider)
				thingWrapper.set(thing)
			}
		] as ThingChangeListener)
		def thing1 = ThingBuilder.create(THING_TYPE_UID, THING1_ID).build()
		managedThingProvider.addThing(thing1)
		
		waitForAssert{assertTrue thingProviderWrapper.isSet}
		waitForAssert{assertTrue thingWrapper.isSet}
		
		assertThat thingProviderWrapper.wrappedObject, is(managedThingProvider)
		assertThat thingWrapper.wrappedObject, is(thing1)
	}
	
	@Test
	void 'assert that ThingChangeListener is notified about updated thing'() {
		AsyncResultWrapper<ThingProvider> addedThingProviderWrapper = new AsyncResultWrapper<ThingProvider>()
		AsyncResultWrapper<Thing> addedThingWrapper = new AsyncResultWrapper<Thing>()
		AsyncResultWrapper<ThingProvider> removedThingProviderWrapper = new AsyncResultWrapper<ThingProvider>()
		AsyncResultWrapper<Thing> removedThingWrapper = new AsyncResultWrapper<Thing>()
		
		def thing1 = ThingBuilder.create(THING_TYPE_UID, THING1_ID).build()
		managedThingProvider.addThing(thing1)
		
		registerThingChangeListener( [
			thingAdded : { ThingProvider provider, Thing thing ->
				addedThingProviderWrapper.set(provider)
				addedThingWrapper.set(thing)
		},
			thingRemoved : { ThingProvider provider, Thing thing ->
				removedThingProviderWrapper.set(provider)
				removedThingWrapper.set(thing)
			}
		] as ThingChangeListener)
		
		def thing2 = ThingBuilder.create(THING_TYPE_UID, THING1_ID).build()
		managedThingProvider.addThing(thing2)
		
		waitForAssert{assertTrue addedThingProviderWrapper.isSet}
		waitForAssert{assertTrue addedThingWrapper.isSet}
		waitForAssert{assertTrue removedThingProviderWrapper.isSet}
		waitForAssert{assertTrue removedThingWrapper.isSet}
		
		assertThat addedThingProviderWrapper.wrappedObject, is(managedThingProvider)
		assertThat addedThingWrapper.wrappedObject, is(thing2)
		assertThat removedThingProviderWrapper.wrappedObject, is(managedThingProvider)
		assertThat removedThingWrapper.wrappedObject, is(thing1)
	}
	
	@Test
	void 'assert that createThing delegates to registered ThingHandlerFactory'() {
		def expectedThingType = new ThingType(BINDIND_ID, THING_TYPE_ID, new DescriptionTypeMetaInfo("label", "description"), "testManufacturer")
		def expectedThingUID = new ThingUID(THING_TYPE_UID, THING1_ID)
		def expectedConfiguration = new Configuration()
		def expectedBridge = BridgeBuilder.create(THING_TYPE_UID, THING2_ID).build()
		
		AsyncResultWrapper<Thing> thingResultWrapper = new AsyncResultWrapper<Thing>();
		
		registerThingHandlerFactory( [
			supportsThingType: { ThingTypeUID thingTypeUID ->
				true
			},
			createThing : { ThingType thingType, Configuration configuration, ThingUID thingUID, Bridge bridge ->
				assertThat thingType, is(expectedThingType)
				assertThat configuration, is(expectedConfiguration)
				assertThat thingUID, is(expectedThingUID)
				assertThat bridge, is(expectedBridge)
				def thing = ThingBuilder.create(thingType.getUID(), thingUID.getId()).withBridge(bridge).build()
				thingResultWrapper.set(thing)
				thing
			}
		] as ThingHandlerFactory)
		
		def thing = managedThingProvider.createThing(expectedThingType, expectedThingUID, expectedBridge, expectedConfiguration)
		waitForAssert{assertTrue thingResultWrapper.isSet}
		assertThat thing, is(thingResultWrapper.wrappedObject)
	}

}
