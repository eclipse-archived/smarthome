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

import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
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
}
