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

import org.eclipse.smarthome.core.events.EventPublisher
import org.eclipse.smarthome.core.library.types.DecimalType
import org.eclipse.smarthome.core.thing.Channel
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.core.thing.link.ItemChannelLink
import org.eclipse.smarthome.core.thing.link.ManagedItemChannelLinkProvider
import org.eclipse.smarthome.core.types.State
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class ThingManagerOSGiTest extends OSGiTest {

	ManagedThingProvider managedThingProvider

    ManagedItemChannelLinkProvider managedItemChannelLinkProvider
    
    def THING_TYPE_UID = new ThingTypeUID("binding:type")
    
    def THING_UID = new ThingUID(THING_TYPE_UID, "id")
    
    def CHANNEL_UID = new ChannelUID(THING_UID, "channel")
    
	def THING = ThingBuilder.create(THING_UID).withChannels([new Channel(CHANNEL_UID, "Switch")]).build()

    EventPublisher eventPublisher
    
	@Before
	void setUp() {
		registerVolatileStorageService()
        managedItemChannelLinkProvider = getService(ManagedItemChannelLinkProvider)
		managedThingProvider = getService(ManagedThingProvider)
        eventPublisher = getService(EventPublisher)
	}
	
	@After
	void teardown() {
		managedThingProvider.getAll().each {
			managedThingProvider.remove(it.getUID())
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

		managedThingProvider.add(THING)

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

		managedThingProvider.add(THING)

		managedThingProvider.remove(THING.getUID())

		waitForAssert {assertThat unregisterHandlerCalled, is(true)}
	}

	@Test
	void 'ThingManager tracks handler for Thing'() {

		def registerHandlerCalled = false

		managedThingProvider.add(THING)
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
    

    @Test
    void 'ThingManager does not delegate update events to its source'() {

        def itemName = "name"
        def handleUpdateWasCalled = false
        
        managedThingProvider.add(THING)
        managedItemChannelLinkProvider.add(new ItemChannelLink(itemName, CHANNEL_UID))
        def thingHandler = [
            handleUpdate: { ChannelUID channelUID, State newState ->
                handleUpdateWasCalled = true
            }
        ] as ThingHandler
        
        registerService(thingHandler,[
            (ThingHandler.SERVICE_PROPERTY_THING_ID): THING.getUID(),
            (ThingHandler.SERVICE_PROPERTY_THING_TYPE): THING.getThingTypeUID()
        ] as Hashtable)
        
        // event should be delivered
        eventPublisher.postUpdate(itemName, new DecimalType(10))
        waitForAssert { assertThat handleUpdateWasCalled, is(true) }
       
        handleUpdateWasCalled = false
        
        // event should not be delivered, because the source is the same
        eventPublisher.postUpdate(itemName, new DecimalType(10), CHANNEL_UID.toString())
        waitFor {handleUpdateWasCalled == true}
        assertThat handleUpdateWasCalled, is(false) 

    }
}
