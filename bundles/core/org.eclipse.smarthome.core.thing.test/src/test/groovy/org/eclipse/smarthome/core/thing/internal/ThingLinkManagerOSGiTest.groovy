/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import java.util.concurrent.ConcurrentHashMap

import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.thing.Channel
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
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry
import org.eclipse.smarthome.core.thing.type.ChannelDefinition
import org.eclipse.smarthome.core.thing.type.ChannelType
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID
import org.eclipse.smarthome.core.thing.type.ThingType
import org.eclipse.smarthome.core.types.Command
import org.eclipse.smarthome.core.types.StateDescription
import org.eclipse.smarthome.core.types.StateOption
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.service.component.ComponentContext

/**
 *
 * These tests will check (un-)linking of items and channels managed
 * by {@link ThingLinkManager}.
 *
 * @author Alex Tugarev - Initial contribution
 * @author Dennis Nobel - Added test for bug 459628 (lifecycle problem)
 * @author Thomas Höfer - Thing type constructor modified because of thing properties introduction
 * @author Kai Kreuzer - Adapted to new service implementation
 */
class ThingLinkManagerOSGiTest extends OSGiTest {

    def ThingRegistry thingRegistry
    def ManagedThingProvider managedThingProvider
    def ItemRegistry itemRegistry
    def ItemChannelLinkRegistry itemChannelLinkRegistry

    private Map<String,Object> context = new ConcurrentHashMap<>()

    @Before
    void setup() {
        context.clear();

        registerVolatileStorageService()
        thingRegistry = getService(ThingRegistry)
        managedThingProvider = getService(ManagedThingProvider)
        itemRegistry = getService(ItemRegistry)
        assertNotNull(itemRegistry)
        itemChannelLinkRegistry = getService(ItemChannelLinkRegistry)
        assertThat thingRegistry, is(notNullValue())

        def ComponentContext componentContext = [
            getBundleContext: { -> bundleContext }
        ] as ComponentContext

        def thingHandlerFactory = new TestThingHandlerFactory()
        thingHandlerFactory.activate(componentContext)
        registerService(thingHandlerFactory, ThingHandlerFactory.class.getName())

        def StateDescription state = new StateDescription(0, 100, 10, "%d Peek", true, [new StateOption("SOUND", "My great sound.")])

        def ChannelType channelType = new ChannelType(new ChannelTypeUID("hue:alarm"), false, "Number", " ", "", null, null, state, null)
        def channelTypes = [channelType]
        registerService([
            getChannelTypes: { channelTypes },
            getChannelType: { ChannelTypeUID uid, Locale locale ->
                channelTypes.find { it.UID == uid }
            },
            getChannelGroupTypes: { []},
            getChannelGroupType: { null }
        ] as ChannelTypeProvider)

        def thingTypeProvider = new SimpleThingTypeProvider([new ThingType(new ThingTypeUID("hue:lamp"), null, " ", null, [new ChannelDefinition("1", channelType.UID)], null, null, null)])
        registerService(thingTypeProvider)
    }

    @After
    void teardown() {
        ManagedThingProvider managedThingProvider = getService(ManagedThingProvider)
        managedThingProvider.getAll().each {
            managedThingProvider.remove(it.getUID())
        }
        itemChannelLinkRegistry.getAll().each {
            itemChannelLinkRegistry.remove(it.ID)
        }
    }

    @Test
    void 'assert that links are removed upon thing removal'() {
        ThingUID thingUID = new ThingUID("hue:lamp:lamp1")
        Thing thing = thingRegistry.createThingOfType(new ThingTypeUID("hue:lamp"), thingUID, null, "test thing", new Configuration())

        def channels = thing.getChannels()
        assertThat channels.size(), is(1)
        Channel channel = channels.first()

        managedThingProvider.add(thing)
        waitForAssert {
            assertThat itemChannelLinkRegistry.getLinkedItems(channel.getUID()).size(), is(1)
        }

        managedThingProvider.remove(thingUID)
        waitForAssert {
            assertThat itemChannelLinkRegistry.getLinkedItems(channel.getUID()).size(), is(0)
        }
    }


    @Test
    void 'assert that channelLinked and channelUnlinked at ThingHandler is called'() {
        ThingUID thingUID = new ThingUID("hue:lamp:lamp1")
        Thing thing = thingRegistry.createThingOfType(new ThingTypeUID("hue:lamp"), thingUID, null, "test thing", new Configuration())
        managedThingProvider.add(thing)

        def channelUID = new ChannelUID(thingUID, "1")

        waitForAssert {
            assertThat takeContext("linkedChannel"), is(equalTo(channelUID))
            assertThat takeContext("unlinkedChannel"), is(null)
        }

        itemChannelLinkRegistry.removeLinksForThing(thingUID)

        waitForAssert {
            assertThat takeContext("unlinkedChannel"), is(equalTo(channelUID))
        }
    }

    private Object takeContext(final String key) {
        final Object obj = context.remove(key)
        println "take from context: " + key + "=" + obj
        return obj
    }

    private Object putContext(final String key, final Object value) {
        final Object old = context.put(key, value);
        println "put to context: " + key + "=" + value + " [old: " + old + "]"
        return old
    }

    /*
     * Helper
     */

    class TestThingHandlerFactory extends BaseThingHandlerFactory {
        @Override
        public boolean supportsThingType(ThingTypeUID thingTypeUID) {
            return true
        }

        @Override
        protected ThingHandler createHandler(Thing thing) {
            return new BaseThingHandler(thing) {
                        public void handleCommand(ChannelUID channelUID, Command command) { }

                        @Override
                        public void initialize() {
                            updateStatus(ThingStatus.ONLINE);
                        }

                        void channelLinked(ChannelUID channelUID) {
                            putContext("linkedChannel", channelUID)
                        }

                        void channelUnlinked(ChannelUID channelUID) {
                            println "Cannel unlinked: " + channelUID
                            putContext("unlinkedChannel", channelUID)
                        }
                    }
        }
    }
}