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
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingProvider
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory
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
	
	ProviderChangeListener<Thing> thingChangeListener
	ThingHandlerFactory thingHandlerFactory
	
	final static String BINDIND_ID = "testBinding"
	final static String THING_TYPE_ID = "testThingType"
	final static ThingTypeUID THING_TYPE_UID = new ThingTypeUID(BINDIND_ID, THING_TYPE_ID)
	final static String THING1_ID = "testThing1"
	final static String THING2_ID = "testThing2"
	
	@Before
	void setup() {
		registerVolatileStorageService()
		managedThingProvider = getService ManagedThingProvider
		assertThat managedThingProvider, is(notNullValue())
		unregisterCurrentThingsChangeListener()
		unregisterCurrentThingHandlerFactory()
	}
	
	@After
	void teardown() {
		unregisterCurrentThingsChangeListener()
		unregisterCurrentThingHandlerFactory()
		managedThingProvider.getAll().each {
			managedThingProvider.remove(it.getUID())
		} 
	}
	
	
	private void registerThingsChangeListener(ProviderChangeListener<Thing> thingChangeListener) {
		unregisterCurrentThingsChangeListener()
		this.thingChangeListener = thingChangeListener
		managedThingProvider.addProviderChangeListener(this.thingChangeListener)
	}

	private void unregisterCurrentThingsChangeListener() {
		if (this.thingChangeListener != null) {
			managedThingProvider.removeProviderChangeListener(this.thingChangeListener)
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
		managedThingProvider.add(thing1)
		def things = managedThingProvider.getAll()
		assertThat things.size(), is(1)
		assertThat things.first(), is(thing1)
		def thing2 = ThingBuilder.create(THING_TYPE_UID, THING2_ID).build()
		managedThingProvider.add(thing2)
		things = managedThingProvider.getAll()
		assertThat things.size(), is(2)
		assertThat things.getAt(0), is(thing1)
		assertThat things.getAt(1), is(thing2)
		
	}
	
	@Test(expected=IllegalArgumentException.class)
	void 'assert that twice added thing throws exception'() {
		def thing1 = ThingBuilder.create(THING_TYPE_UID, THING1_ID).build()
		def thing2 = ThingBuilder.create(THING_TYPE_UID, THING1_ID).build()
		managedThingProvider.add(thing1)
		managedThingProvider.add(thing2)
	}
	
	@Test
	void 'assert that removed thing is not returned by getThings'() {
		def thing = ThingBuilder.create(THING_TYPE_UID, THING1_ID).build()
		managedThingProvider.add(thing)
		managedThingProvider.remove(thing.getUID())
		def things = managedThingProvider.getAll()
		assertThat things.size(), is(0)
	}
	
	
	@Test
	void 'assert that ThingsChangeListener is notified about added thing'() {
		AsyncResultWrapper<ThingProvider> thingProviderWrapper = new AsyncResultWrapper<ThingProvider>()
		AsyncResultWrapper<Thing> thingWrapper = new AsyncResultWrapper<Thing>()
		registerThingsChangeListener( [
			added : { ThingProvider provider, Thing thing ->
				thingProviderWrapper.set(provider)
				thingWrapper.set(thing)
			}
		] as ProviderChangeListener<Thing>)
		def thing1 = ThingBuilder.create(THING_TYPE_UID, THING1_ID).build()
		managedThingProvider.add(thing1)
		
		waitForAssert{assertTrue thingProviderWrapper.isSet}
		waitForAssert{assertTrue thingWrapper.isSet}
		
		assertThat thingProviderWrapper.wrappedObject, is(managedThingProvider)
		assertThat thingWrapper.wrappedObject, is(thing1)
	}
	
	@Test
	void 'assert that ThingsChangeListener is notified about updated thing'() {
		AsyncResultWrapper<ThingProvider> updatedThingProviderWrapper = new AsyncResultWrapper<ThingProvider>()
		AsyncResultWrapper<Thing> oldThingWrapper = new AsyncResultWrapper<Thing>()
        AsyncResultWrapper<Thing> updatedThingWrapper = new AsyncResultWrapper<Thing>()
		
		def thing1 = ThingBuilder.create(THING_TYPE_UID, THING1_ID).build()
		managedThingProvider.add(thing1)
		
		registerThingsChangeListener( [
			added : {},
            updated : { ThingProvider provider, Thing oldThing, Thing thing ->
				updatedThingProviderWrapper.set(provider)
                oldThingWrapper.set(oldThing)
				updatedThingWrapper.set(thing)
            }
		] as ProviderChangeListener<Thing>)
		
		def thing2 = ThingBuilder.create(THING_TYPE_UID, THING1_ID).build()
		managedThingProvider.update(thing2)
		
		waitForAssert{assertTrue updatedThingProviderWrapper.isSet}
		waitForAssert{assertTrue oldThingWrapper.isSet}
		waitForAssert{assertTrue updatedThingWrapper.isSet}
		
		assertThat updatedThingProviderWrapper.wrappedObject, is(managedThingProvider)
		assertThat oldThingWrapper.wrappedObject, is(thing1)
		assertThat updatedThingWrapper.wrappedObject, is(thing2)
	}
	
	@Test
	void 'assert that createThing delegates to registered ThingHandlerFactory'() {
		def expectedThingTypeUID = THING_TYPE_UID
		def expectedThingUID = new ThingUID(THING_TYPE_UID, THING1_ID)
		def expectedConfiguration = new Configuration()
		def expectedBridgeUID = new ThingUID(THING_TYPE_UID, THING2_ID)
		
		AsyncResultWrapper<Thing> thingResultWrapper = new AsyncResultWrapper<Thing>();
		
		registerThingHandlerFactory( [
			supportsThingType: { ThingTypeUID thingTypeUID -> true },
			createThing : { ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID, ThingUID bridgeUID ->
				assertThat thingTypeUID, is(expectedThingTypeUID)
				assertThat configuration, is(expectedConfiguration)
				assertThat thingUID, is(expectedThingUID)
				assertThat bridgeUID, is(expectedBridgeUID)
				def thing = ThingBuilder.create(thingTypeUID, thingUID.getId()).withBridge(bridgeUID).build()
				thingResultWrapper.set(thing)
				thing
			},
            registerHandler: {}
		] as ThingHandlerFactory)

		def thing = managedThingProvider.createThing(expectedThingTypeUID, expectedThingUID, expectedBridgeUID, expectedConfiguration)
		waitForAssert{assertTrue thingResultWrapper.isSet}
		assertThat thing, is(thingResultWrapper.wrappedObject)
	}

}
