/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.setup

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry
import org.eclipse.smarthome.core.thing.link.ItemThingLinkRegistry
import org.eclipse.smarthome.core.thing.type.BridgeType
import org.eclipse.smarthome.core.thing.type.ThingType
import org.eclipse.smarthome.core.types.Command
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.service.component.ComponentContext


/**
 * RemovalTest is a test for the framework's behavior regarding the removal of things.
 *
 * @author Simon Kaufmann - Initial contribution
 * @author Dennis Nobel - Added test for dead lock bug (#473143)
 */
class RemovalTest extends OSGiTest {

    def ThingSetupManager thingSetupManager
    def ThingRegistry thingRegistry
    def ItemRegistry itemRegistry
    def ItemChannelLinkRegistry itemChannelLinkRegistry
    def ItemThingLinkRegistry itemThingLinkRegistry

    def Lock lock = new ReentrantLock()
    def Condition removalDone = lock.newCondition()
    def int removalCount

    class DefaultThingHandler extends BaseThingHandler {

        public DefaultThingHandler(Thing thing) {
            super(thing)
        }

        @Override
        public void handleCommand(ChannelUID channelUID, Command command) {
        }

        @Override
        public void handleRemoval() {
            def thread = new Thread(new Runnable() {
                        public void run() {
                            try {
                                RemovalTest.this.lock.lock()
                                RemovalTest.this.removalDone.await()
                                RemovalTest.this.removalCount++
                            } finally {
                                RemovalTest.this.lock.unlock()
                            }
                            DefaultThingHandler.this.signalRemovalDone()
                        }
                    })
            thread.start()
        }

        public void signalRemovalDone() {
            super.handleRemoval()
        }
    }

    class SynchronousDefaultThingHandler extends BaseThingHandler {

        public SynchronousDefaultThingHandler(Thing thing) {
            super(thing)
        }

        @Override
        public void handleCommand(ChannelUID channelUID, Command command) {
        }
    }

    class DefaultThingHandlerFactory extends BaseThingHandlerFactory {

        @Override
        public boolean supportsThingType(ThingTypeUID thingTypeUID) {
            return true;
        }

        @Override
        protected ThingHandler createHandler(Thing thing) {
            return !thing.UID.toString().contains("synchronous") ? new DefaultThingHandler(thing) : new SynchronousDefaultThingHandler(thing);
        }
    }

    @Before
    void setup() {
        registerVolatileStorageService()
        thingSetupManager = getService(ThingSetupManager)
        thingRegistry = getService(ThingRegistry)
        itemRegistry = getService(ItemRegistry)
        itemChannelLinkRegistry = getService(ItemChannelLinkRegistry)
        itemThingLinkRegistry = getService(ItemThingLinkRegistry)
        def componentContext = [getBundleContext: { bundleContext }] as ComponentContext
        def thingHandlerFactory = new DefaultThingHandlerFactory()
        thingHandlerFactory.activate(componentContext)
        registerService(thingHandlerFactory, ThingHandlerFactory.class.getName())

        def thingTypeUID1 = new ThingTypeUID("removal-binding:thing-type")
        def bridgeTypeUID = new ThingTypeUID("removal-binding:bridge-type")

        def thingTypes = [
            new ThingType(thingTypeUID1, null, "label", null, null, null, null, null),
            new BridgeType(bridgeTypeUID, null, "label", null, null, null, null, null)
        ]

        registerService([
            getThingTypes: { return thingTypes },
            getThingType: { ThingTypeUID thingTypeUID, Locale locale ->
                thingTypes.find { it.UID == thingTypeUID }
            }
        ] as ThingTypeProvider)
    }

    @After
    void tearDown() {
        def managedThingProvider = getService(ManagedThingProvider)
        managedThingProvider.getAll().each {
            managedThingProvider.remove(it.getUID())
        }
    }

    @Test
    void 'removeThing'() {
        def thingUID = new ThingUID("removal-binding", "thing-type", "thingToRemove")
        thingSetupManager.addThing(thingUID, new Configuration(), null, "MyThing", [] as List, true)

        assertThat thingRegistry.getAll().size(), is(1)
        assertThat itemThingLinkRegistry.getAll().size(), is(1)
        assertThat itemRegistry.getItems().size(), is(1)
        removalCount = 0

        thingSetupManager.removeThing(thingUID)

        assertThat thingRegistry.getAll().size(), is(1)
        assertThat itemThingLinkRegistry.getAll().size(), is(1)
        assertThat itemRegistry.getItems().size(), is(1)

        assertThat thingRegistry.getAll()[0].getStatus(), is(ThingStatus.REMOVING)

        def c = 0
        while (thingRegistry.getAll().size() > 0 && c++ < 100) {
            Thread.sleep(100)
            try {
                lock.lock()
                removalDone.signal()
            } finally {
                lock.unlock()
            }
        }

        waitForAssert({
            ->
            assertThat removalCount, is(1)
            assertThat thingRegistry.getAll().size(), is(0)
            assertThat itemThingLinkRegistry.getAll().size(), is(0)
            assertThat itemRegistry.getItems().size(), is(0)
            assertThat itemChannelLinkRegistry.getAll().size(), is(0)
        }, 10000, 100)
    }


    @Test
    void 'remove Bridge with Things recursively'() {
        def bridgeUID = new ThingUID("removal-binding", "bridge-type", "thing")
        thingSetupManager.addThing(bridgeUID, new Configuration(), null, "MyBridge", [] as List, true)

        def thingUID1 = new ThingUID("removal-binding", "thing-type", "thing1")
        thingSetupManager.addThing(thingUID1, new Configuration(), bridgeUID, "MyThing", [] as List, true)

        def thingUID2 = new ThingUID("removal-binding", "thing-type", "thing2")
        thingSetupManager.addThing(thingUID2, new Configuration(), bridgeUID, "MyThing", [] as List, true)

        assertThat thingRegistry.getAll().size(), is(3)
        assertThat itemThingLinkRegistry.getAll().size(), is(3)
        assertThat itemRegistry.getItems().size(), is(3)
        removalCount = 0

        // if bridge is removed, things are removed recursively, too
        thingSetupManager.removeThing(bridgeUID)

        assertThat thingRegistry.getAll().size(), is(3)
        assertThat itemThingLinkRegistry.getAll().size(), is(3)
        assertThat itemRegistry.getItems().size(), is(3)

        assertThat thingRegistry.getAll()[0].getStatus(), is(ThingStatus.REMOVING)
        assertThat thingRegistry.getAll()[1].getStatus(), is(ThingStatus.REMOVING)

        def c = 0
        while (thingRegistry.getAll().size() > 0 && c++ < 100) {
            Thread.sleep(100)
            try {
                lock.lock()
                removalDone.signalAll()
            } finally {
                lock.unlock()
            }
        }

        waitForAssert({
            ->
            assertThat removalCount, is(3)
            assertThat thingRegistry.getAll().size(), is(0)
            assertThat itemThingLinkRegistry.getAll().size(), is(0)
            assertThat itemRegistry.getItems().size(), is(0)
            assertThat itemChannelLinkRegistry.getAll().size(), is(0)
        }, 10000, 100)
    }

    @Test
    void 'synchronous remove Thing terminates fast (no deadlock)'() {

        def thingUID1 = new ThingUID("removal-binding", "thing-type", "thing-synchronous")
        thingSetupManager.addThing(thingUID1, new Configuration(), null, "MyThing", [] as List, true)

        def start = System.currentTimeMillis()
        thingSetupManager.removeThing(thingUID1)
        def duration = System.currentTimeMillis() - start;

        // timeout would be 5 seconds
        assertTrue(duration < 1000)

        waitForAssert({
            assertThat thingRegistry.getAll().size(), is(0)
        })
    }
}
