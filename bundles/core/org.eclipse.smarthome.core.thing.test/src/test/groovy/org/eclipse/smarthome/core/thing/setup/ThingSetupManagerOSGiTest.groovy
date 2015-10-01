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

import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.items.GroupItem
import org.eclipse.smarthome.core.items.ItemRegistry
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
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry
import org.eclipse.smarthome.core.thing.link.ItemThingLinkRegistry
import org.eclipse.smarthome.core.thing.type.BridgeType
import org.eclipse.smarthome.core.thing.type.ChannelDefinition
import org.eclipse.smarthome.core.thing.type.ChannelGroupDefinition
import org.eclipse.smarthome.core.thing.type.ChannelGroupType
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID
import org.eclipse.smarthome.core.thing.type.ChannelType
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID
import org.eclipse.smarthome.core.thing.type.ThingType
import org.eclipse.smarthome.core.types.Command
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.service.component.ComponentContext


/**
 * ThingSetupManagerTest is a test for the ThingSetupManager class.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Thomas Höfer - Thing type constructor modified because of thing properties introduction
 */
class ThingSetupManagerOSGiTest extends OSGiTest {

    def ThingSetupManager thingSetupManager
    def ThingRegistry thingRegistry
    def ItemRegistry itemRegistry
    def ItemChannelLinkRegistry itemChannelLinkRegistry
    def ItemThingLinkRegistry itemThingLinkRegistry

    class DefaultThingHandler extends BaseThingHandler {

        public DefaultThingHandler(Thing thing) {
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
            return new DefaultThingHandler(thing);
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
        def componentContext = [getBundleContext: {bundleContext}] as ComponentContext
        def thingHandlerFactory = new DefaultThingHandlerFactory()
        thingHandlerFactory.activate(componentContext)
        registerService(thingHandlerFactory, ThingHandlerFactory.class.getName())

        def thingTypeUID1 = new ThingTypeUID("binding:thing-type")
        def thingTypeUID2 = new ThingTypeUID("binding:thing-type-with-channel-groups")
        def bridgeTypeUID = new ThingTypeUID("binding:bridge-type")

        def channelType1 = new ChannelType(new ChannelTypeUID("binding:channelType1"), false, "String", "label", "", "light", [] as Set, null, null)
        def channelType2 = new ChannelType(new ChannelTypeUID("binding:channelType2"), true, "String", "label", "", "light", [] as Set, null, null)

        def channelTypes = [channelType1, channelType2]

        def channelDefinitions = [
            new ChannelDefinition("1", channelType1.UID),
            new ChannelDefinition("2", channelType1.UID),
            new ChannelDefinition("3", channelType2.UID),
        ] as List

        def ChannelGroupType channelGroupType = new ChannelGroupType(new ChannelGroupTypeUID("binding:channelGroupType"), false, "Group", null, channelDefinitions)
        def channelGroupDefinitions = [
            new ChannelGroupDefinition("group1", channelGroupType.UID),
            new ChannelGroupDefinition("group2", channelGroupType.UID),
        ] as List

        def channelGroupTypes = [channelGroupType]

        registerService([
            getChannelTypes: { channelTypes },
            getChannelType: { ChannelTypeUID uid, Locale locale ->
                channelTypes.find { it.UID == uid }
            },
            getChannelGroupTypes: { channelGroupTypes },
            getChannelGroupType: { ChannelGroupTypeUID uid, Locale locale ->
                channelGroupTypes.find { it.UID == uid }
            }
        ] as ChannelTypeProvider)

        def thingTypes = [
            new ThingType(thingTypeUID1, null, "label", null, channelDefinitions, null, null, null),
            new ThingType(thingTypeUID2, null, "label", null, null, channelGroupDefinitions, null, null),
            new BridgeType(bridgeTypeUID, null, "label", null, null, null, null, null)
        ]
        registerService([
            getThingTypes: { thingTypes },
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
    void 'addThingWithoutChannelsItems'() {
        def thingUID = new ThingUID("binding", "thing-type", "thing")
        thingSetupManager.addThing(thingUID, new Configuration(), null, "MyThing", [] as List, false)
        def items = itemRegistry.getItems();
        assertThat items.size(), is(1)
        assertThat itemThingLinkRegistry.isLinked(items[0].name, thingUID), is(true)
    }

    @Test
    void 'addThingWithChannelsItems'() {
        def thingUID = new ThingUID("binding", "thing-type", "thing")
        thingSetupManager.addThing(thingUID, new Configuration(), null, "MyThing", [] as List, true)

        def items = itemRegistry.getItems();
        assertThat items.size(), is(3)

        def linkedThingGroupItemName = itemThingLinkRegistry.getLinkedItems(thingUID).first()
        def linkedThingGroupItem = itemRegistry.get(linkedThingGroupItemName)
        assertThat linkedThingGroupItem.label, is(equalTo("MyThing"))

        def linkedItemName = itemChannelLinkRegistry.getLinkedItems(new ChannelUID(thingUID, "1")).first()
        def linkedItem = itemRegistry.get(linkedItemName)

        assertThat linkedItem.label, is(equalTo("label"))
        assertThat linkedItem.type, is(equalTo("String"))
        assertThat linkedItem.category, is(equalTo("light"))
    }

    @Test
    void 'addThingWithChannelsGroups'() {
        def thingUID = new ThingUID("binding", "thing-type-with-channel-groups", "thing")
        thingSetupManager.addThing(thingUID, new Configuration(), null, "MyThing", [] as List, true)

        assertThat itemRegistry.getItems().size(), is(7)
        assertThat itemChannelLinkRegistry.getAll().size(), is(4)

        def thing = thingSetupManager.getThing(thingUID)
        def item = thing.linkedItem

        assertThat item.getMembers().size(), is(2)
        def firstChannelGroupItem = item.getMembers().first()
        assertThat firstChannelGroupItem, is(instanceOf(GroupItem))
        assertThat firstChannelGroupItem.getMembers().size(), is(2)
    }

    @Test
    void 'removeThing'() {
        def thingUID = new ThingUID("binding", "thing-type", "thing")
        thingSetupManager.addThing(thingUID, new Configuration(), null, "MyThing", [] as List, true)

        assertThat thingRegistry.getAll().size(), is(1)
        assertThat itemThingLinkRegistry.getAll().size(), is(1)
        assertThat itemRegistry.getItems().size(), is(3)
        assertThat itemChannelLinkRegistry.getAll().size(), is(2)

        thingSetupManager.removeThing(thingUID)

        waitForAssert({
            ->
            assertThat thingRegistry.getAll().size(), is(0)
            assertThat itemThingLinkRegistry.getAll().size(), is(0)
            assertThat itemRegistry.getItems().size(), is(0)
            assertThat itemChannelLinkRegistry.getAll().size(), is(0)
        }, 10000, 100)
    }


    @Test
    void 'remove Bridge with Things recursively'() {
        def bridgeUID = new ThingUID("binding", "bridge-type", "thing")
        thingSetupManager.addThing(bridgeUID, new Configuration(), null, "MyBridge", [] as List, true)

        def thingUID = new ThingUID("binding", "thing-type", "thing")
        thingSetupManager.addThing(thingUID, new Configuration(), bridgeUID, "MyThing", [] as List, true)

        assertThat thingRegistry.getAll().size(), is(2)
        assertThat itemThingLinkRegistry.getAll().size(), is(2)
        assertThat itemRegistry.getItems().size(), is(4)
        assertThat itemChannelLinkRegistry.getAll().size(), is(2)

        // if bridge is removed, things are removed recursively, too
        thingSetupManager.removeThing(bridgeUID)

        waitForAssert({
            ->
            assertThat thingRegistry.getAll().size(), is(0)
            assertThat itemThingLinkRegistry.getAll().size(), is(0)
            assertThat itemRegistry.getItems().size(), is(0)
            assertThat itemChannelLinkRegistry.getAll().size(), is(0)
        }, 10000, 100)
    }

    @Test
    void 'enableAndDisableChannel'() {
        def thingUID = new ThingUID("binding", "thing-type", "thing")
        thingSetupManager.addThing(thingUID, new Configuration(), null, "MyThing", [] as List, true)
        assertThat itemRegistry.getItems().size(), is(3)
        thingSetupManager.disableChannel(new ChannelUID(thingUID, "1"))
        assertThat itemRegistry.getItems().size(), is(2)
        thingSetupManager.enableChannel(new ChannelUID(thingUID, "1"))
        assertThat itemRegistry.getItems().size(), is(3)
    }

    @Test
    void 'addAndRemoveHomeGroup'() {

        assertThat thingSetupManager.getHomeGroups().size(), is(0)
        thingSetupManager.addHomeGroup("group_1", "Home Group1")

        def homeGroups = thingSetupManager.getHomeGroups()
        assertThat homeGroups.size(), is(1)
        assertThat homeGroups[0].name, is(equalTo("group_1"))
        assertThat homeGroups[0].label, is(equalTo("Home Group1"))

        assertThat itemRegistry.get("group_1"), is(not(null))

        thingSetupManager.removeHomeGroup("group_1")
        assertThat thingSetupManager.getHomeGroups().size(), is(0)
        assertThat itemRegistry.get("group_1"), is(null)
    }

    @Test
    void 'addAndRemoveFromHomeGroup'() {

        def thingUID = new ThingUID("binding", "thing-type", "thing")
        thingSetupManager.addThing(thingUID, new Configuration(), null, "MyThing", [] as List, true)
        thingSetupManager.addHomeGroup("group_1", "Home Group1")

        thingSetupManager.addToHomeGroup(thingUID, "group_1")

        GroupItem homeGroup = itemRegistry.get("group_1")
        assertThat homeGroup.members.size(), is(1)
        assertThat homeGroup.members.find({ it.label == "MyThing"}), is(not(null))

        thingSetupManager.removeFromHomeGroup(thingUID, "group_1")

        homeGroup = itemRegistry.get("group_1")
        assertThat homeGroup.members.size(), is(0)
        assertThat homeGroup.members.find({ it.label == "MyThing"}), is(null)
    }

    @Test
    void 'setLabel'() {

        def thingUID = new ThingUID("binding", "thing-type", "thing")
        thingSetupManager.addThing(thingUID, new Configuration(), null, "MyThing", [] as List, true)
        thingSetupManager.setLabel(thingUID, "Another Label")

        assertThat thingSetupManager.getThing(thingUID).linkedItem.label, is(equalTo("Another Label"))
    }


}
