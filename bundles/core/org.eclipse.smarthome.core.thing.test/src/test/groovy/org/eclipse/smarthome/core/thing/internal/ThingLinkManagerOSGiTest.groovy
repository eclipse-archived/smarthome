/**
 * Copyright (c) 2014-15 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.items.GroupItem
import org.eclipse.smarthome.core.items.Item
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.thing.Channel
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry
import org.eclipse.smarthome.core.thing.link.ItemThingLinkRegistry
import org.eclipse.smarthome.core.thing.setup.ThingSetupManager
import org.eclipse.smarthome.core.thing.type.ChannelDefinition
import org.eclipse.smarthome.core.thing.type.ChannelType
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID
import org.eclipse.smarthome.core.thing.type.ThingType
import org.eclipse.smarthome.core.types.Command
import org.eclipse.smarthome.core.types.StateDescription
import org.eclipse.smarthome.core.types.StateOption
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Before
import org.junit.Test
import org.osgi.service.component.ComponentContext

/**
 * 
 * These tests will check (un-)linking of items and things, or items and channels managed 
 * by {@link ThingLinkManager}.
 * 
 * @author Alex Tugarev - Initial contribution
 * @author Dennis Nobel - Added test for bug 459628 (lifecycle problem)
 */
class ThingLinkManagerOSGiTest extends OSGiTest{
    
    def ThingRegistry thingRegistry 
    def ThingSetupManager thingSetupManager

    @Before
    void setup() {
        registerVolatileStorageService()
        
        thingRegistry = getService(ThingRegistry)
        assertThat thingRegistry, is(notNullValue())
        
        def ComponentContext componentContext = [
            getBundleContext: { -> bundleContext} 
        ] as ComponentContext
    
        def thingHandlerFactory = new TestThingHandlerFactory()
        thingHandlerFactory.activate(componentContext)
        registerService(thingHandlerFactory, ThingHandlerFactory.class.getName())
        
        def StateDescription state = new StateDescription(0, 100, 10, "%d Peek", true, [ new StateOption("SOUND", "My great sound.") ])
        
        def ChannelType channelType = new ChannelType(new ChannelTypeUID("hue:alarm"), false, "Number", " ", "", null, null, state, null)
        
        def thingTypeProvider = new TestThingTypeProvider([ new ThingType(new ThingTypeUID("hue:lamp"), null, " ", null, [ new ChannelDefinition("1", channelType) ], null, null) ])
        registerService(thingTypeProvider)

        thingSetupManager = getService(ThingSetupManager)
        assertThat thingSetupManager, is(notNullValue())
    }
    
    @Test
    void 'assert that items are linked to thing and channel'() {
        ThingUID thingUID = new ThingUID("hue:lamp:lamp1")
        thingSetupManager.addThing(thingUID, new Configuration(), /* bridge */ null)
        
        Thing thing = thingRegistry.get(thingUID)
        assertThat thing, is(notNullValue())
        
        GroupItem linkedGroupItem = thing.getLinkedItem()
        assertThat linkedGroupItem, is(notNullValue())
        assertThat linkedGroupItem.getName(), is("hue_lamp_lamp1")
        
        def channels = thing.getChannels()
        assertThat channels.size(), is(1)
        Channel channel = channels.first()
        
        def linkedItems = channel.getLinkedItems()
        assertThat linkedItems.size(), is(1)
        Item item = linkedItems.first()
        assertThat item.getName(), is("hue_lamp_lamp1_1")
    }
    
    @Test
    void 'assert that items are unlinked'() {
        ThingUID thingUID = new ThingUID("hue:lamp:lamp1")
        thingSetupManager.addThing(thingUID, new Configuration(), /* bridge */ null)
        
        Thing thing = thingRegistry.get(thingUID)
        assertThat thing, is(notNullValue())
        
        thingSetupManager.removeThing(thingUID)
        
        GroupItem linkedGroupItem = thing.getLinkedItem()
        assertThat linkedGroupItem, is(null)
        
        def channels = thing.getChannels()
        assertThat channels.size(), is(1)
        Channel channel = channels.first()
        
        def linkedItems = channel.getLinkedItems()
        assertThat linkedItems.size(), is(0)
    }
    
    @Test
    void 'assert that existing things are linked'() {
        def componentContext = [getBundleContext: {getBundleContext()}] as ComponentContext
        def thingManger = new ThingManager()
        try {
            ThingUID thingUID = new ThingUID("hue:lamp:lamp1")
            thingSetupManager.addThing(thingUID, new Configuration(), /* bridge */ null)
            
            Thing thing = thingRegistry.get(thingUID)
            assertThat thing, is(notNullValue())
            
            // create thing manager manually to simulate start up of declarative service
            thingManger.setItemRegistry(getService(ItemRegistry))
            thingManger.setThingRegistry(getService(ThingRegistry))
            thingManger.setItemChannelLinkRegistry(getService(ItemChannelLinkRegistry))
            thingManger.setItemThingLinkRegistry(getService(ItemThingLinkRegistry))
            thingManger.activate(componentContext)
            
            def channels = thing.getChannels()
            assertThat channels.size(), is(1)
            Channel channel = channels.first()
            
            def linkedItems = channel.getLinkedItems()
            assertThat linkedItems.size(), is(1)
        } finally {
            thingManger.deactivate(componentContext)
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
            return null
        }
    }
    
}