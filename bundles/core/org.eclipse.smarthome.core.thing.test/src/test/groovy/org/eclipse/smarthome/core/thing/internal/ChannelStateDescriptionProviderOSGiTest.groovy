/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener
import org.eclipse.smarthome.core.items.GenericItem
import org.eclipse.smarthome.core.items.Item
import org.eclipse.smarthome.core.items.ItemProvider
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.library.items.NumberItem
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider
import org.eclipse.smarthome.core.thing.link.ItemChannelLink
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry
import org.eclipse.smarthome.core.thing.type.ChannelDefinition
import org.eclipse.smarthome.core.thing.type.ChannelType
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID
import org.eclipse.smarthome.core.thing.type.ThingType
import org.eclipse.smarthome.core.types.Command
import org.eclipse.smarthome.core.types.StateDescription
import org.eclipse.smarthome.core.types.StateDescriptionProvider
import org.eclipse.smarthome.core.types.StateOption
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.service.component.ComponentContext

/**
 * Tests for {@link ChannelStateDescriptionProvider}.
 *
 * @author Alex Tugarev - Initial contribution
 * @author Thomas Höfer - Thing type constructor modified because of thing properties introduction
 */
class ChannelStateDescriptionProviderOSGiTest extends OSGiTest {

    def ItemRegistry itemRegistry
    def ItemChannelLinkRegistry linkRegistry
    def StateDescriptionProvider stateDescriptionProvider

    @Before
    void setup() {
        registerVolatileStorageService()

        itemRegistry = getService(ItemRegistry)
        assertThat itemRegistry, is(notNullValue())

        def ComponentContext componentContext = [
            getBundleContext: { -> bundleContext}
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

        def thingTypeProvider = new TestThingTypeProvider([new ThingType(new ThingTypeUID("hue:lamp"), null, " ", null, [new ChannelDefinition("1", channelType.UID)], null, null, null)])
        registerService(thingTypeProvider)

        def itemProvider = new TestItemProvider([new NumberItem("TestItem")])
        registerService(itemProvider)

        linkRegistry = getService(ItemChannelLinkRegistry)

        stateDescriptionProvider = getService(StateDescriptionProvider)
        assertThat stateDescriptionProvider, is(notNullValue())
    }

    @After
    void teardown() {
        ManagedThingProvider managedThingProvider = getService(ManagedThingProvider)
        managedThingProvider.getAll().each {
            managedThingProvider.remove(it.getUID())
        }
        linkRegistry.getAll().each { linkRegistry.remove(it.ID) }
    }

    @Test
    void 'assert that item\'s state description is present'() {
        ThingRegistry thingRegistry = getService(ThingRegistry)
        ManagedThingProvider managedThingProvider = getService(ManagedThingProvider)
        Thing thing = thingRegistry.createThingOfType(new ThingTypeUID("hue:lamp"), new ThingUID("hue:lamp:lamp1"), null, "test thing", new Configuration())
        managedThingProvider.add(thing)
        def link = new ItemChannelLink("TestItem", thing.channels.get(0).UID)
        linkRegistry.add(link)

        def items = itemRegistry.getItems()
        assertThat items.isEmpty(), is(false)

        def GenericItem numberItem = items.find { "Number" == it.getType() }
        assertThat numberItem, is(notNullValue())

        def StateDescription state = numberItem.getStateDescription()
        assertThat state, is(notNullValue())

        state.with {
            assertThat minimum, is(0 as BigDecimal)
            assertThat maximum, is(100 as BigDecimal)
            assertThat step, is(10 as BigDecimal)
            assertThat pattern, is("%d Peek")
            assertThat readOnly, is(true)
            assertThat options.size(), is(1)
            assertThat options.first().getValue(), is("SOUND")
            assertThat options.first().getLabel(), is("My great sound.")
        }
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
                    }
        }
    }

    class TestThingTypeProvider implements ThingTypeProvider {
        def Collection<ThingType> thingTypes

        TestThingTypeProvider(Collection<ThingType> thingTypes){
            this.thingTypes = thingTypes
        }

        @Override
        public Collection<ThingType> getThingTypes(Locale locale) {
            return this.thingTypes;
        }

        @Override
        public ThingType getThingType(ThingTypeUID thingTypeUID, Locale locale) {
            return thingTypes.find { it.UID == thingTypeUID }
        }
    }

    class TestItemProvider implements ItemProvider {
        def Collection<Item> items

        TestItemProvider(Collection<Item> items) {
            this.items = items
        }

        @Override
        public void addProviderChangeListener(ProviderChangeListener<Item> listener) {
        }

        @Override
        public Collection<Item> getAll() {
            return items;
        }

        @Override
        public void removeProviderChangeListener(ProviderChangeListener<Item> listener) {
        }
    }
}
