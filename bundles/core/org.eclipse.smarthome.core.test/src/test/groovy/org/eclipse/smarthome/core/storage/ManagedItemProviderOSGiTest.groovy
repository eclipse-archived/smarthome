/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.storage

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.core.items.GenericItem
import org.eclipse.smarthome.core.items.GroupItem
import org.eclipse.smarthome.core.items.Item
import org.eclipse.smarthome.core.items.ItemFactory
import org.eclipse.smarthome.core.items.ItemNotFoundException
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.items.ManagedItemProvider
import org.eclipse.smarthome.core.items.ManagedItemProvider.PersistedItem
import org.eclipse.smarthome.core.library.items.NumberItem
import org.eclipse.smarthome.core.library.items.StringItem
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType
import org.eclipse.smarthome.core.library.types.ArithmeticGroupFunction.And
import org.eclipse.smarthome.core.library.types.ArithmeticGroupFunction.Avg
import org.eclipse.smarthome.core.library.types.ArithmeticGroupFunction.Sum
import org.eclipse.smarthome.core.types.Command
import org.eclipse.smarthome.core.types.State
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * The {@link ManagedItemProviderOSGiTest} runs inside an
 * OSGi container and tests the {@link ManagedItemProvider}.
 *
 * @author Thomas Eichstaedt-Engelen - Initial contribution
 * @author Kai Kreuzer - added tests for repeated addition and removal
 * @author Andre Fuechsel - added tests for tags
 * @author Simon Kaufmann - added test for late registration of item factory
 */
class ManagedItemProviderOSGiTest extends OSGiTest {

    ManagedItemProvider itemProvider
    ItemRegistry itemRegistry

    @Before
    void setUp() {
        registerVolatileStorageService()
        itemProvider = getService(ManagedItemProvider)
        itemRegistry = getService(ItemRegistry)
    }

    @After
    void tearDown() {
        itemProvider.getAll().each {
            itemProvider.remove(it.name)
        }
        unregisterService(itemProvider)
    }

    private static class StrangeItem extends GenericItem {
        static final String STRANGE_TEST_TYPE = "StrangeTestType"

        public StrangeItem(String name) {
            super(STRANGE_TEST_TYPE, name)
        }

        @Override
        public List<Class<? extends State>> getAcceptedDataTypes() {
            return Collections.unmodifiableList(StringType.class);
        }

        @Override
        public List<Class<? extends Command>> getAcceptedCommandTypes() {
            return Collections.unmodifiableList(StringType.class);
        }
    }

    private static class StrangeItemFactory implements ItemFactory {
        @Override
        public GenericItem createItem(String itemTypeName, String itemName) {
            return new StrangeItem(itemName)
        }

        @Override
        public String[] getSupportedItemTypes() {
            return StrangeItem.STRANGE_TEST_TYPE;
        }
    }

    @Test
    void 'assert getItems returns item from registered ManagedItemProvider'() {

        assertThat itemProvider.getAll().size(), is(0)

        itemProvider.add new SwitchItem('SwitchItem')
        itemProvider.add new StringItem('StringItem')

        def items = itemProvider.getAll()
        assertThat items.size(), is(2)

        itemProvider.remove 'StringItem'
        itemProvider.remove 'SwitchItem'

        assertThat itemProvider.getAll().size(), is(0)
    }

    @Test
    void 'updating existing item returns old value'() {

        assertThat itemProvider.getAll().size(), is(0)

        itemProvider.add new StringItem('Item')
        def result = itemProvider.update new SwitchItem('Item')

        assertThat result.type, is("String")

        itemProvider.remove 'Item'

        assertThat itemProvider.getAll().size(), is(0)
    }

    @Test
    void 'assert removal returns old value'() {

        assertThat itemProvider.getAll().size(), is(0)

        itemProvider.add new StringItem('Item')
        def result = itemProvider.remove 'Unknown'

        assertNull result

        result = itemProvider.remove 'Item'

        assertThat result.name, is('Item')

        assertThat itemProvider.getAll().size(), is(0)
    }

    @Test(expected=IllegalArgumentException.class)
    void 'assert two items with same name can not be added'() {

        assertThat itemProvider.getAll().size(), is(0)

        itemProvider.add new StringItem('Item')
        itemProvider.add new StringItem('Item')
    }

    @Test
    void 'assert tags are stored and retrieve as well'() {

        assertThat itemProvider.getAll().size(), is(0)

        def item1 = new SwitchItem('SwitchItem1')
        def item2 = new SwitchItem('SwitchItem2')
        item1.addTag('tag1')
        item1.addTag('tag2')
        item2.addTag('tag3')

        itemProvider.add item1
        itemProvider.add item2

        def items = itemProvider.getAll()
        assertThat items.size(), is(2)

        def result1 = itemProvider.remove 'SwitchItem1'
        def result2 = itemProvider.remove 'SwitchItem2'

        assertThat result1.name, is('SwitchItem1')
        assertThat result1.getTags().size(), is(2)
        assertThat result1.hasTag('tag1'), is(true)
        assertThat result1.hasTag('tag2'), is(true)
        assertThat result1.hasTag('tag3'), is(false)

        assertThat result2.name, is('SwitchItem2')
        assertThat result2.getTags().size(), is(1)
        assertThat result2.hasTag('tag1'), is(false)
        assertThat result2.hasTag('tag2'), is(false)
        assertThat result2.hasTag('tag3'), is(true)

        assertThat itemProvider.getAll().size(), is(0)
    }

    @Test
    void 'assert remove recursively works'() {

        assertThat itemProvider.getAll().size(), is(0)

        def group = new GroupItem("group")

        def item1 = new SwitchItem('SwitchItem1')
        item1.addGroupName(group.name)
        def item2 = new SwitchItem('SwitchItem2')
        item2.addGroupName(group.name)

        itemProvider.add group
        itemProvider.add item1
        itemProvider.add item2

        assertThat itemProvider.getAll().size(), is(3)

        def oldItem = itemProvider.remove(group.name, true)

        assertThat oldItem, is(group)
        assertThat itemProvider.getAll().size(), is(0)
    }

    @Test
    void 'assert items are there once the factory gets added'() {
        StorageService storageService = getService(StorageService)
        assertThat storageService, is(notNullValue())

        Storage storage = storageService.getStorage(Item.class.getName())
        StrangeItem item = new StrangeItem('SomeStrangeItem')
        String key = itemProvider.keyToString(item.getUID())

        // put an item into the storage that cannot be handled (yet)
        PersistedItem persistableElement = storage.put(key, itemProvider.toPersistableElement(item))

        // start without the appropriate item factory - it's going to fail silently, leaving a debug log

        assertThat itemProvider.getAll().size(), is(0)
        assertThat itemRegistry.getItems().size(), is(0)
        assertThat itemProvider.get("SomeStrangeItem"), is(nullValue())
        try {
            assertThat itemRegistry.getItem("SomeStrangeItem"), is(nullValue())
            fail("the item is not (yet) expected to be there")
        } catch (ItemNotFoundException e) {
            // all good
        }

        // now register the item factory. The item should be there...
        StrangeItemFactory factory = new StrangeItemFactory()
        registerService(factory)
        try {
            assertThat itemProvider.getAll().size(), is(1)
            assertThat itemRegistry.getItems().size(), is(1)
            assertThat itemProvider.get("SomeStrangeItem"), is(notNullValue())
            assertThat itemRegistry.getItem("SomeStrangeItem"), is(notNullValue())
        } finally {
            unregisterService(factory)
        }
    }

    @Test
    void 'assert items are added as group members on deferred creation'() {
        StorageService storageService = getService(StorageService)
        assertThat storageService, is(notNullValue())

        Storage storage = storageService.getStorage(Item.class.getName())
        StrangeItem item = new StrangeItem('SomeStrangeItem')
        GroupItem groupItem = new GroupItem('SomeGroupItem')
        item.addGroupName(groupItem.getName())
        groupItem.addMember(item)
        String itemKey = itemProvider.keyToString(item.getUID())
        String groupKey = itemProvider.keyToString(groupItem.getUID())

        // put items into the storage that cannot be handled (yet)
        PersistedItem persistableElement1 = storage.put(itemKey, itemProvider.toPersistableElement(item))
        PersistedItem persistableElement2 = storage.put(groupKey, itemProvider.toPersistableElement(groupItem))
        itemProvider.notifyListenersAboutAddedElement(groupItem)

        // start without the appropriate item factory - it only creates the group item

        assertThat itemProvider.getAll().size(), is(1)
        assertThat itemRegistry.getItems().size(), is(1)
        assertThat itemProvider.get("SomeStrangeItem"), is(nullValue())
        assertThat itemProvider.get("SomeGroupItem"), is(notNullValue())
        assertThat itemRegistry.getItem("SomeGroupItem"), is(notNullValue())
        try {
            assertThat itemRegistry.getItem("SomeStrangeItem"), is(nullValue())
            fail("the item is not (yet) expected to be there")
        } catch (ItemNotFoundException e) {
            // all good
        }

        // now register the item factory. Both items should be there...
        StrangeItemFactory factory = new StrangeItemFactory()
        registerService(factory)
        try {
            assertThat itemProvider.getAll().size(), is(2)
            assertThat itemRegistry.getItems().size(), is(2)
            Item item1 = itemRegistry.get("SomeStrangeItem")
            GroupItem item2 = itemRegistry.get("SomeGroupItem")
            assertThat itemRegistry.get("SomeStrangeItem"), is(notNullValue())
            assertThat item1, is(notNullValue())
            assertThat item1.getGroupNames().size(), is(1)
            assertThat item2.getMembers().size(), is(1)
        } finally {
            unregisterService(factory)
        }
    }

    @Test
    void 'assert functions are stored and retrieved as well'() {

        assertThat itemProvider.getAll().size(), is(0)

        def item1 = new GroupItem('GroupItem', new NumberItem(), new Avg())
        itemProvider.add item1

        def items = itemProvider.getAll()
        assertThat items.size(), is(1)

        GroupItem result1 = itemProvider.remove 'GroupItem'

        assertThat result1.name, is('GroupItem')
        assertThat result1.function, is(Avg)

        assertThat itemProvider.getAll().size(), is(0)
    }


    @Test
    void 'assert group functions are stored and retrieved as well'() {

        assertThat itemProvider.getAll().size(), is(0)

        def function1 = new And(OnOffType.ON, OnOffType.OFF)
        def function2 = new Sum()
        def item1 = new GroupItem('GroupItem1', new SwitchItem('Switch'), function1)
        def item2 = new GroupItem('GroupItem2', new NumberItem(), function2)

        assertThat item1.name, is('GroupItem1')
        assertThat item1.function, isA(And.class)
        assertThat item1.function.parameters, is([OnOffType.ON, OnOffType.OFF] as State[])

        assertThat item2.name, is('GroupItem2')
        assertThat item2.function, isA(Sum.class)
        assertThat item2.function.parameters, is([] as State[])

        itemProvider.add item1
        itemProvider.add item2

        def items = itemProvider.getAll()
        assertThat items.size(), is(2)

        def result1 = itemProvider.remove 'GroupItem1'
        def result2 = itemProvider.remove 'GroupItem2'

        assertThat result1.name, is('GroupItem1')
        assertThat result1.function, isA(And.class)
        assertThat result1.function.parameters, is([OnOffType.ON, OnOffType.OFF] as State[])

        assertThat result2.name, is('GroupItem2')
        assertThat result2.function, isA(Sum.class)
        assertThat result2.function.parameters, is([] as State[])

        assertThat itemProvider.getAll().size(), is(0)
    }
}
