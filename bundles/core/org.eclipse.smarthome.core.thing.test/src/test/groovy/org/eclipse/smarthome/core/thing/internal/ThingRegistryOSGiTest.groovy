/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.core.thing.events.ThingAddedEvent
import org.eclipse.smarthome.core.thing.events.ThingRemovedEvent
import org.eclipse.smarthome.core.thing.events.ThingUpdatedEvent
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test

import com.google.common.collect.Sets

/**
 * {@link ThingRegistryOSGiTest} tests the {@link ThingRegistry}.
 *
 * @author Stefan Bußweiler - Initial contribution
 */
class ThingRegistryOSGiTest extends OSGiTest {

    ManagedThingProvider managedThingProvider

    def THING_TYPE_UID = new ThingTypeUID("binding:type")

    def THING_UID = new ThingUID(THING_TYPE_UID, "id")

    def THING = ThingBuilder.create(THING_UID).build()

    @Before
    void setUp() {
        registerVolatileStorageService()
        managedThingProvider = getService(ManagedThingProvider)
    }

    @After
    void teardown() {
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
}
