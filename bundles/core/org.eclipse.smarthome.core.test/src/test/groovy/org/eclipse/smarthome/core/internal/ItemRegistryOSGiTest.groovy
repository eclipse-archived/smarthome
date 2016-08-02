/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import java.util.concurrent.atomic.AtomicInteger

import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.items.GenericItem
import org.eclipse.smarthome.core.items.GroupItem
import org.eclipse.smarthome.core.items.Item
import org.eclipse.smarthome.core.items.ItemProvider
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.items.ItemRegistryChangeListener
import org.eclipse.smarthome.core.items.ItemsChangeListener
import org.eclipse.smarthome.core.items.events.ItemAddedEvent
import org.eclipse.smarthome.core.items.events.ItemRemovedEvent
import org.eclipse.smarthome.core.items.events.ItemUpdatedEvent
import org.eclipse.smarthome.core.library.items.NumberItem
import org.eclipse.smarthome.core.library.items.StringItem
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Before
import org.junit.Test

import com.google.common.collect.Sets

/**
 * The {@link ItemRegistryOSGiTest} runs inside an OSGi container and tests the {@link ItemRegistry}.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Andre Fuechsel - extended with tag tests
 * @author Kai Kreuzer - added tests for all items changed cases
 * @author Sebastian Janzen - added test for getItemsByTag
 */
class ItemRegistryOSGiTest extends OSGiTest {

    ItemRegistry itemRegistry
    ItemProvider itemProvider
    ItemsChangeListener itemsChangeListener
    def ITEM_NAME = "switchItem"
    def CAMERA_ITEM_NAME1 = "cameraItem1"
    def CAMERA_ITEM_NAME2 = "cameraItem2"
    def CAMERA_ITEM_NAME3 = "cameraItem3"
    def CAMERA_TAG = "camera"
    def SENSOR_TAG = "sensor"
    def OTHER_TAG = "other"

    @Before
    void setUp() {
        itemRegistry = getService(ItemRegistry)
        def cameraItem1 = new SwitchItem(CAMERA_ITEM_NAME1)
        def cameraItem2 = new SwitchItem(CAMERA_ITEM_NAME2)
        def cameraItem3 = new NumberItem(CAMERA_ITEM_NAME3)
        cameraItem1.addTag(CAMERA_TAG)
        cameraItem2.addTag(CAMERA_TAG)
        cameraItem2.addTag(SENSOR_TAG)
        cameraItem3.addTag(CAMERA_TAG)
        itemProvider = [
            getAll: {
                [
                    new SwitchItem(ITEM_NAME),
                    cameraItem1,
                    cameraItem2,
                    cameraItem3
                ]
            },
            addProviderChangeListener: {def icl -> itemsChangeListener = icl},
            removeProviderChangeListener: {def icl -> itemsChangeListener = icl },
            allItemsChanged: {}] as ItemProvider
    }

    @Test
    void 'assert getItems returns item from registered ItemProvider'() {

        assertThat itemRegistry.getItems().size(), is(0)

        registerService itemProvider

        def items = itemRegistry.getItems()
        assertThat items.size(), is(4)
        assertThat items.first().name, is(equalTo(ITEM_NAME))

        unregisterService itemProvider

        assertThat itemRegistry.getItems().size(), is(0)
    }

    @Test
    void 'assert getItemsOfType returns item from registered ItemProvider'() {

        assertThat itemRegistry.getItemsOfType("Switch").size(), is(0)

        registerService itemProvider

        def items = itemRegistry.getItemsOfType("Switch")
        assertThat items.size(), is(3)
        assertThat items.first().name, is(equalTo(ITEM_NAME))

        unregisterService itemProvider

        assertThat itemRegistry.getItems().size(), is(0)
    }

    @Test
    void 'assert getItemsByTag returns item from registered ItemProvider'() {

        assertThat itemRegistry.getItemsByTag(CAMERA_TAG).size(), is(0)

        registerService itemProvider

        def items = itemRegistry.getItemsByTag(CAMERA_TAG)
        assertThat items.size(), is(3)
        assertThat items.first().name, is(equalTo(CAMERA_ITEM_NAME1))
        assertThat items.get(1).name, is(equalTo(CAMERA_ITEM_NAME2))
        assertThat items.last().name, is(equalTo(CAMERA_ITEM_NAME3))

        unregisterService itemProvider

        assertThat itemRegistry.getItems().size(), is(0)
    }

    @Test
    void 'assert getItemsByTagAndType returns item from registered ItemProvider'() {

        assertThat itemRegistry.getItemsByTagAndType("Switch", CAMERA_TAG).size(), is(0)

        registerService itemProvider

        def items = itemRegistry.getItemsByTagAndType("Switch", CAMERA_TAG)
        assertThat items.size(), is(2)
        assertThat items.first().name, is(equalTo(CAMERA_ITEM_NAME1))
        assertThat items.last().name, is(equalTo(CAMERA_ITEM_NAME2))

        unregisterService itemProvider

        assertThat itemRegistry.getItems().size(), is(0)
    }

    @Test
    void 'assert getItemsByTag with two tags returns item from registered ItemProvider'() {

        assertThat itemRegistry.getItemsByTag(CAMERA_TAG).size(), is(0)

        registerService itemProvider

        def items = itemRegistry.getItemsByTag(CAMERA_TAG, SENSOR_TAG)
        assertThat items.size(), is(1)
        assertThat items.first().name, is(equalTo(CAMERA_ITEM_NAME2))

        unregisterService itemProvider

        assertThat itemRegistry.getItems().size(), is(0)
    }

    @Test
    void 'assert getItemsByTag returns no item from registered ItemProvider'() {

        assertThat itemRegistry.getItemsByTag(CAMERA_TAG).size(), is(0)

        registerService itemProvider

        def items = itemRegistry.getItemsByTag(OTHER_TAG)
        assertThat items.size(), is(0)

        unregisterService itemProvider

        assertThat itemRegistry.getItems().size(), is(0)
    }

    @Test
    void 'assert getItemsByTag can filter by class and tag'() {

      assertThat itemRegistry.getItemsByTag(SwitchItem.class, CAMERA_TAG).size(), is(0)

      registerService itemProvider

      def items = itemRegistry.getItemsByTag(SwitchItem.class, CAMERA_TAG)
      assertThat items.size(), is(2)
      assertThat items.first().name, is(equalTo(CAMERA_ITEM_NAME1))
      assertThat items.last().name, is(equalTo(CAMERA_ITEM_NAME2))
    }

    @Test
    void 'assert getItemsByTag can filter by class and tag with GenericItem'() {

      assertThat itemRegistry.getItemsByTag(GenericItem.class, CAMERA_TAG).size(), is(0)

      registerService itemProvider

      assertThat itemRegistry.getItemsByTag(GenericItem.class, CAMERA_TAG).size(), is(3)
    }

    @Test
    void 'assert itemRegistry sets and removes members of GroupItems'() {

        assertThat itemRegistry.getItems().size(), is(0)

        registerService itemProvider

        // test added item with group name is added as member to group
        itemsChangeListener.added(itemProvider, new GroupItem("group"))
        SwitchItem switchItem = new SwitchItem("switch")
        switchItem.addGroupName("group")
        itemsChangeListener.added(itemProvider, switchItem)


        def groupItem = (itemRegistry.getItem("group") as GroupItem)
        assertThat groupItem.members.contains(switchItem), is(true)

        // test removed item is removed as member from group
        itemsChangeListener.removed(itemProvider, switchItem)
        assertThat groupItem.members.contains(switchItem), is(false)

        // test added group item with gets all members set when it is added at last
        switchItem.addGroupName("group2")
        itemsChangeListener.added(itemProvider, switchItem)
        itemsChangeListener.added(itemProvider, new GroupItem("group2"))

        def groupItem2 = (itemRegistry.getItem("group2") as GroupItem)
        assertThat groupItem2.members.contains(switchItem), is(true)

        // test update item
        itemsChangeListener.added(itemProvider, new GroupItem("group3"))
        def groupItem3 = (itemRegistry.getItem("group") as GroupItem)

        SwitchItem updatedSwitchItem = new SwitchItem("switch")

        updatedSwitchItem.addGroupName("group")
        updatedSwitchItem.addGroupName("group3")

        // old item has group: [group, group2], new item has [group, group3]
        itemsChangeListener.updated(itemProvider, switchItem, updatedSwitchItem)

        assertThat groupItem.members.contains(updatedSwitchItem), is(true)
        assertThat groupItem2.members.contains(updatedSwitchItem), is(false)
        assertThat groupItem3.members.contains(updatedSwitchItem), is(true)
    }

    @Test
    void 'assert itemRegistryChangeListeners are informed about item changes'() {
        registerService itemProvider

        def itemAddedCalled = false
        def itemRemovedCalled = false
        def itemUpdatedCalled = false

        itemRegistry.addRegistryChangeListener([
            added: {Item item -> itemAddedCalled = true},
            removed: {Item item -> itemRemovedCalled = true },
            updated: { Item oldItem, Item item -> itemUpdatedCalled = true },
            allItemsChanged: {
            }
        ] as ItemRegistryChangeListener)

        def item = new SwitchItem("switch")
        itemsChangeListener.added(itemProvider, item)
        itemsChangeListener.removed(itemProvider, item)
        itemsChangeListener.added(itemProvider, item)
        def oldItem = item;
        def newItem = new SwitchItem("switch")
        itemsChangeListener.updated(itemProvider, oldItem, newItem)

        assertThat itemAddedCalled && itemRemovedCalled && itemUpdatedCalled, is(true)
    }

    @Test
    void 'assert itemRegistry keeps same instance on item updates without changes'() {
        def items = new HashSet<Item>()
        def itemToKeep = new SwitchItem("Keep");
        items.add(itemToKeep);

        def itemProvider2 = [
            getAll: { items },
            addProviderChangeListener: {def icl -> itemsChangeListener = icl},
            removeProviderChangeListener: {def icl -> itemsChangeListener = icl },
            allItemsChanged: {}] as ItemProvider

        registerService itemProvider2

        assertThat itemRegistry.getItem("Keep") == itemToKeep, is(true)

        items.clear()
        items.add(new SwitchItem("Keep"))
        itemsChangeListener.allItemsChanged(itemProvider2, null)

        assertThat itemRegistry.getItem("Keep") == itemToKeep, is(true)

        items.clear()
        def modifiedItem = new StringItem("Keep")
        items.add(modifiedItem)
        itemsChangeListener.allItemsChanged(itemProvider2, null)

        assertThat itemRegistry.getItem("Keep") == modifiedItem, is(true)
    }

    @Test
    void 'assert itemRegistry is thread safe'() {
        registerService itemProvider

        AtomicInteger numberOfSuccessfulGetItemCalls = new AtomicInteger(0);

        10.times({
            Thread.start {
                10.times({
                    // get item throws an exception if item is not present and counter is not incremented
                    itemRegistry.getItem(ITEM_NAME)
                    numberOfSuccessfulGetItemCalls.incrementAndGet()
                })
            }
        });

        waitFor({numberOfSuccessfulGetItemCalls.get() >= 100})
        assertThat numberOfSuccessfulGetItemCalls.get(), is(100)
    }

    @Test
    void 'assert ItemRegistryEventSubscribers receive events about item changes'() {
        registerService itemProvider

        def receivedEvent = null
        def itemRegistryEventSubscriber = [
            receive: { event -> receivedEvent = event },
            getSubscribedEventTypes: { Sets.newHashSet(ItemAddedEvent.TYPE, ItemRemovedEvent.TYPE, ItemUpdatedEvent.TYPE) },
            getEventFilter: { null },
        ] as EventSubscriber
        registerService itemRegistryEventSubscriber

        // add new item
        def item = new SwitchItem("SomeSwitch")
        itemsChangeListener.added(itemProvider, item)
        waitForAssert {assertThat receivedEvent, not(null)}
        assertThat receivedEvent, is(instanceOf(ItemAddedEvent))
        receivedEvent = null

        // update item
        def oldItem = item;
        def newItem = new SwitchItem("SomeSwitch")
        itemsChangeListener.updated(itemProvider, oldItem, newItem)
        waitForAssert {assertThat receivedEvent, not(null)}
        assertThat receivedEvent, is(instanceOf(ItemUpdatedEvent))
        receivedEvent = null

        // remove item
        itemsChangeListener.removed(itemProvider, newItem)
        waitForAssert {assertThat receivedEvent, not(null)}
        assertThat receivedEvent, is(instanceOf(ItemRemovedEvent))
    }

}
