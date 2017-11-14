/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingProvider
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.core.thing.events.ThingAddedEvent
import org.eclipse.smarthome.core.thing.events.ThingRemovedEvent
import org.eclipse.smarthome.core.thing.events.ThingUpdatedEvent
import org.eclipse.smarthome.test.AsyncResultWrapper
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test

import com.google.common.collect.Sets

/**
 * {@link ThingRegistryOSGiTest} tests the {@link ThingRegistry}.
 *
 * @author Stefan Bußweiler - Initial contribution
 * @author Kai Kreuzer - Moved createThing test from managed provider
 */
class ThingRegistryOSGiTest extends OSGiTest {

    ManagedThingProvider managedThingProvider
	ThingHandlerFactory thingHandlerFactory
	
    def THING_TYPE_UID = new ThingTypeUID("binding:type")
    def THING_UID = new ThingUID(THING_TYPE_UID, "id")
    def THING = ThingBuilder.create(THING_UID).build()
	def THING1_ID = "testThing1"
	def THING2_ID = "testThing2"
	
    @Before
    void setUp() {
        registerVolatileStorageService()
        managedThingProvider = getService(ManagedThingProvider)
		unregisterCurrentThingHandlerFactory()
    }

    @After
    void teardown() {
		unregisterCurrentThingHandlerFactory()
        managedThingProvider.getAll().each {
            managedThingProvider.remove(it.getUID())
        }
    }

    @Test
    void 'assert that ThingRegistryEventSubscribers receive events about thing changes'() {
        def receivedEvent = null
        def thingRegistryEventSubscriber = [
            receive: { event -> receivedEvent = event },
            getSubscribedEventTypes: { Sets.newHashSet(ThingAddedEvent.TYPE, ThingRemovedEvent.TYPE, ThingUpdatedEvent.TYPE) },
            getEventFilter: { null },
        ] as EventSubscriber
        registerService thingRegistryEventSubscriber

        // add new thing
        managedThingProvider.add(THING)
        waitForAssert {assertThat receivedEvent, not(null)}
        assertThat receivedEvent, is(instanceOf(ThingAddedEvent))
        receivedEvent = null

        // update thing
        def updatedThing = ThingBuilder.create(THING_UID).build()
        managedThingProvider.update(updatedThing)
        waitForAssert {assertThat receivedEvent, not(null)}
        assertThat receivedEvent, is(instanceOf(ThingUpdatedEvent))
        receivedEvent = null

        // remove thing
        managedThingProvider.remove(THING.getUID())
        waitForAssert {assertThat receivedEvent, not(null)}
        assertThat receivedEvent, is(instanceOf(ThingRemovedEvent))
        receivedEvent = null
    }

    @Test
    void 'assert that ThingRegistry delegates config update to thing handler'() {
        def changedParameters = null
        def thingUID = new ThingUID("binding:type:thing")
        def thingHandler = [
            handleConfigurationUpdate: { a ->  changedParameters = a } ]  as ThingHandler

        def thing = ThingBuilder.create(thingUID).build();
        thing.thingHandler = thingHandler;

        def thingProvider = [
            addProviderChangeListener: {},
            removeProviderChangeListener: {},
            getAll: { [thing ] as List}
        ] as ThingProvider

        registerService(thingProvider)

        ThingRegistry thingRegistry = getService(ThingRegistry)

        def parameters = [
            param1: 'value1',
            param2: 1
        ] as Map;
        thingRegistry.updateConfiguration(thingUID, parameters)

        assertThat changedParameters.entrySet(), is(equalTo(parameters.entrySet()))
    }

    @Test(expected=IllegalArgumentException)
    void 'assert that ThingRegistry throws Exception for config update of non existing thing'() {
        ThingRegistry thingRegistry = getService(ThingRegistry)
        def thingUID = new ThingUID("binding:type:thing")
        def parameters = [
            param: 'value1'
        ] as Map;
        thingRegistry.updateConfiguration(thingUID, parameters)
    }
	
	@Test
	void 'assert that createThing delegates to registered ThingHandlerFactory'() {
		def expectedThingTypeUID = THING_TYPE_UID
		def expectedThingUID = new ThingUID(THING_TYPE_UID, THING1_ID)
		def expectedConfiguration = new Configuration()
		def expectedBridgeUID = new ThingUID(THING_TYPE_UID, THING2_ID)
		def expectedLabel = "Test Thing"
		
		AsyncResultWrapper<Thing> thingResultWrapper = new AsyncResultWrapper<Thing>();
		
		ThingRegistry thingRegistry = getService(ThingRegistry)
		
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

		def thing = thingRegistry.createThingOfType(expectedThingTypeUID, expectedThingUID, expectedBridgeUID, expectedLabel, expectedConfiguration)
		waitForAssert{assertTrue thingResultWrapper.isSet}
		assertThat thing, is(thingResultWrapper.wrappedObject)
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

}
