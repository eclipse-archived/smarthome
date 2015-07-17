/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
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
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener
import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.Channel
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.core.types.Command
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.framework.ServiceRegistration
import org.osgi.service.component.ComponentContext

/**
 * Tests for {@link ManagedThingProvider}.
 * @author Oliver Libutzki - Initital contribution
 * @author Dennis Nobel - Added test for bridgeInitialized and bridgeDisposed callbacks
 *
 */
class BindingBaseClassesOSGiTest extends OSGiTest {


    ManagedThingProvider managedThingProvider
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
    }

    @After
    void teardown() {
        managedThingProvider.getAll().each {
            managedThingProvider.remove(it.getUID())
        }
    }

    class SimpleThingHandlerFactory extends BaseThingHandlerFactory {

        @Override
        public boolean supportsThingType(ThingTypeUID thingTypeUID) {
            true
        }

        @Override
        protected ThingHandler createHandler(Thing thing) {
            return new SimpleThingHandler(thing)
        }
    }

    class SimpleThingHandler extends BaseThingHandler {

        SimpleThingHandler(Thing thing) {
            super(thing)
        }

        @Override
        public void handleCommand(ChannelUID channelUID, Command command) {
            // check getBridge works
            assertThat getBridge().getUID().toString(), is("bindingId:type1:bridgeId")
        }
    }


    @Test
    void 'assert BaseThingHandlerFactory registers handler and BaseThingHandlers getBridge works'() {

        def componentContext = [getBundleContext: {bundleContext}] as ComponentContext
        def thingHandlerFactory = new SimpleThingHandlerFactory()
        thingHandlerFactory.activate(componentContext)
        registerService(thingHandlerFactory, ThingHandlerFactory.class.name)

        def bridge = BridgeBuilder.create(new ThingUID("bindingId:type1:bridgeId")).build()
        def thing = ThingBuilder.create(new ThingUID("bindingId:type2:thingId")).withBridge(bridge.getUID()).build()

        managedThingProvider.add(bridge)
        managedThingProvider.add(thing)

        def handler = thing.getHandler()
        assertThat handler, is(not(null))

        // check that the handler is registered as OSGi service
        def handlerOsgiService = getService(ThingHandler, {
            it.getProperty(ThingHandler.SERVICE_PROPERTY_THING_ID).toString() == "bindingId:type2:thingId"
        })
        assertThat handlerOsgiService, is(handler)

        // the assertion is in handle command
        handler.handleCommand(null, null)

        unregisterService(ThingHandlerFactory.class.name)
        thingHandlerFactory.deactivate(componentContext)
    }

    class AnotherSimpleThingHandlerFactory extends BaseThingHandlerFactory {

        @Override
        public boolean supportsThingType(ThingTypeUID thingTypeUID) {
            true
        }

        @Override
        protected ThingHandler createHandler(Thing thing) {
            return new AnotherSimpleThingHandler(thing)
        }
    }

    def bridgeInitCalled = false;
    def bridgeDisposedCalled = false;

    class AnotherSimpleThingHandler extends BaseThingHandler {

        public AnotherSimpleThingHandler(Thing thing) {
            super(thing)
        }

        @Override
        public void handleCommand(ChannelUID channelUID, Command command) {
        }

        @Override
        protected void bridgeHandlerInitialized(ThingHandler thingHandler, Bridge bridge) {
            updateStatus(ThingStatus.ONLINE)
            bridgeInitCalled = true
        }

        @Override
        protected void bridgeHandlerDisposed(ThingHandler thingHandler, Bridge bridge) {
            updateStatus(ThingStatus.OFFLINE)
            bridgeDisposedCalled = true
        }
    }


    @Test
    void 'assert bridgeInitialized is called by BaseThingHandler'() {
        def componentContext = [getBundleContext: {bundleContext}] as ComponentContext
        def thingHandlerFactory = new AnotherSimpleThingHandlerFactory()
        thingHandlerFactory.activate(componentContext)
        registerService(thingHandlerFactory, ThingHandlerFactory.class.name)

        def bridge = BridgeBuilder.create(new ThingUID("bindingId:type1:bridgeId")).build()
        def thing = ThingBuilder.create(new ThingUID("bindingId:type2:thingId")).withBridge(bridge.getUID()).build()

        // add thing first
        managedThingProvider.add(thing)
        managedThingProvider.add(bridge)

        assertThat bridgeInitCalled, is(true)
        assertThat bridgeDisposedCalled, is(false)
        assertThat thing.status, is(ThingStatus.ONLINE)

        // remove bridge
        managedThingProvider.remove(bridge.UID)

        assertThat bridgeDisposedCalled, is(true)
        assertThat thing.status, is(ThingStatus.OFFLINE)

        managedThingProvider.remove(thing.UID)
        bridgeInitCalled = false
        bridgeDisposedCalled = false

        // add bridge first
        managedThingProvider.add(bridge)
        managedThingProvider.add(thing)

        assertThat bridgeInitCalled, is(true)
    }

    class YetAnotherThingHandlerFactory extends BaseThingHandlerFactory {

        @Override
        public boolean supportsThingType(ThingTypeUID thingTypeUID) {
            true
        }

        @Override
        protected ThingHandler createHandler(Thing thing) {
            return new YetAnotherThingHandler(thing)
        }
    }

    class YetAnotherThingHandler extends BaseThingHandler {

        YetAnotherThingHandler(Thing thing) {
            super(thing)
        }

        @Override
        public void initialize() {
            super.initialize()
            ThingBuilder thingBuilder = editThing()
            thingBuilder.withChannels([
                new Channel(new ChannelUID("bindingId:type:thingId:1"), "String")
            ])
            updateThing(thingBuilder.build())
        }

        @Override
        public void handleCommand(ChannelUID channelUID, Command command) {

        }

        public updateConfig() {
            Configuration configuration = editConfiguration()
            configuration.put("key", "value")
            updateConfiguration(configuration)
        }

        public updateProperties() {
            def properties = editProperties()
            properties.put(Thing.PROPERTY_MODEL_ID, "1234")
            updateProperties(properties)
        }

        public updateProperty() {
            updateProperty(Thing.PROPERTY_VENDOR, "vendor")
        }
    }

    @Test
    void 'assert thing can be updated from ThingHandler'() {
        def componentContext = [getBundleContext: {bundleContext}] as ComponentContext
        def thingHandlerFactory = new YetAnotherThingHandlerFactory()
        thingHandlerFactory.activate(componentContext)
        registerService(thingHandlerFactory, ThingHandlerFactory.class.name)

        def thingUpdated = false
        Thing updatedThing = null
        ThingRegistry thingRegistry = getService(ThingRegistry)

        def registryChangeListener = [
            added: {thing -> },
            updated: {old, updated ->
                thingUpdated = true; updatedThing = updated}
        ] as RegistryChangeListener

        try {
            thingRegistry.addRegistryChangeListener(registryChangeListener)
            def thing = ThingBuilder.create(new ThingUID("bindingId:type:thingId")).build()
            assertThat thing.channels.size(), is(0)
            managedThingProvider.add(thing)
            assertThat thingUpdated, is(true)
            assertThat updatedThing.channels.size(), is(1)

            updatedThing.getHandler().updateConfig()
            assertThat updatedThing.getConfiguration().get("key"), is("value")
        } finally {
            thingRegistry.removeRegistryChangeListener(registryChangeListener)
        }
    }

    @Test
    void 'assert properties can be updated from ThingHandler'() {
        def componentContext = [getBundleContext: {bundleContext}] as ComponentContext
        def thingHandlerFactory = new YetAnotherThingHandlerFactory()
        thingHandlerFactory.activate(componentContext)
        registerService(thingHandlerFactory, ThingHandlerFactory.class.name)

        def thingUpdated = false
        Thing updatedThing = null
        ThingRegistry thingRegistry = getService(ThingRegistry)

        def registryChangeListener = [
            added: {thing -> },
            updated: {old, updated ->
                thingUpdated = true; updatedThing = updated}
        ] as RegistryChangeListener

        try {
            thingRegistry.addRegistryChangeListener(registryChangeListener)
            def thing = ThingBuilder.create(new ThingUID("bindingId:type:thingId")).build()

            managedThingProvider.add(thing)

            assertThat updatedThing.getProperties().get(Thing.PROPERTY_MODEL_ID), is(null)
            assertThat updatedThing.getProperties().get(Thing.PROPERTY_VENDOR), is(null)

            updatedThing.getHandler().updateProperties()

            assertThat updatedThing.getProperties().get(Thing.PROPERTY_MODEL_ID), is("1234")

            updatedThing.getHandler().updateProperty()

            assertThat updatedThing.getProperties().get(Thing.PROPERTY_VENDOR), is("vendor")
        } finally {
            thingRegistry.removeRegistryChangeListener(registryChangeListener)
        }
    }

    @Test
    void 'assert configuration will be updated by default implementation'() {
        def componentContext = [getBundleContext: {bundleContext}] as ComponentContext
        def thingHandlerFactory = new SimpleThingHandlerFactory()
        thingHandlerFactory.activate(componentContext)
        registerService(thingHandlerFactory, ThingHandlerFactory.class.name)

        def thingUpdated = false
        Thing updatedThing = null
        ThingRegistry thingRegistry = getService(ThingRegistry)

        def registryChangeListener = [
            added: {thing -> },
            updated: {old, updated ->
                thingUpdated = true; updatedThing = updated}
        ] as RegistryChangeListener

        try {
            thingRegistry.addRegistryChangeListener(registryChangeListener)
            def thingUID = new ThingUID("bindingId:type:thingId")
            def thing = ThingBuilder.create(thingUID).build()

            managedThingProvider.add(thing)

            thingRegistry.updateConfiguration(thingUID, [parameter: 'value'] as Map)

            assertThat updatedThing.getConfiguration().get('parameter'), is('value')
        } finally {
            thingRegistry.removeRegistryChangeListener(registryChangeListener)
        }
    }
}
