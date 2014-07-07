/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.items.GroupItem
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemProvider
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.items.ItemRegistryChangeListener;
import org.eclipse.smarthome.core.items.ItemsChangeListener;
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After;
import org.junit.Before
import org.junit.Test
import org.osgi.framework.BundleContext
import org.osgi.framework.FrameworkUtil

/**
 * The {@link ItemRegistryOSGiTest} runs inside an OSGi container and tests the {@link ItemRegistry}.  
 * 
 * @author Dennis Nobel - Initial contribution
 */
class ItemRegistryOSGiTest extends OSGiTest {

	ItemRegistry itemRegistry
	ItemProvider itemProvider
	ItemsChangeListener itemsChangeListener
	def ITEM_NAME = "switchItem"

	@Before
	void setUp() {
		itemRegistry = getService(ItemRegistry)
		itemProvider = [
			getItems: {[new SwitchItem(ITEM_NAME)]}, 
			addItemChangeListener: {def icl -> itemsChangeListener = icl},
			removeItemChangeListener: {def icl -> itemsChangeListener = icl },
			allItemsChanged: {}] as ItemProvider
	}

	@Test
	void 'assert getItems returns item from registered ItemProvider'() {

		assertThat itemRegistry.getItems().size, is(0)

		registerService itemProvider

		def items = itemRegistry.getItems()
		assertThat items.size, is(1)
		assertThat items.first().name, is(equalTo(ITEM_NAME))

		unregisterService itemProvider

		assertThat itemRegistry.getItems().size, is(0)
	}
	
	@Test
	void 'assert itemRegistry sets and removes members of GroupItems'() {

		assertThat itemRegistry.getItems().size, is(0)

		registerService itemProvider

		// test added item with group name is added as member to group
		itemsChangeListener.itemAdded(itemProvider, new GroupItem("group"))
		SwitchItem switchItem = new SwitchItem("switch")
		switchItem.addGroupName("group")
		itemsChangeListener.itemAdded(itemProvider, switchItem)


		def groupItem = (itemRegistry.getItem("group") as GroupItem)
		assertThat groupItem.members.contains(switchItem), is(true)

		// test removed item is removed as member from group
		itemsChangeListener.itemRemoved(itemProvider, switchItem)
		assertThat groupItem.members.contains(switchItem), is(false)
		
		// test added group item with gets all members set when it is added at last
		switchItem.addGroupName("group2")
		itemsChangeListener.itemAdded(itemProvider, switchItem)
		itemsChangeListener.itemAdded(itemProvider, new GroupItem("group2"))
		
		def groupItem2 = (itemRegistry.getItem("group2") as GroupItem)
		assertThat groupItem2.members.contains(switchItem), is(true)
		
		// test update item
		itemsChangeListener.itemAdded(itemProvider, new GroupItem("group3"))
		def groupItem3 = (itemRegistry.getItem("group") as GroupItem)
		
		SwitchItem updatedSwitchItem = new SwitchItem("switch")
		
		updatedSwitchItem.addGroupName("group")
		updatedSwitchItem.addGroupName("group3")
		
		// old item has group: [group, group2], new item has [group, group3]
		itemsChangeListener.itemUpdated(itemProvider, switchItem, updatedSwitchItem)
		
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
		
		itemRegistry.addItemRegistryChangeListener([
			itemAdded: {Item item -> itemAddedCalled = true},
			itemRemoved: {Item item -> itemRemovedCalled = true },
			itemUpdated: { Item oldItem, Item item -> itemUpdatedCalled = true },
			allItemsChanged: {}
		] as ItemRegistryChangeListener)
		
		def item = new SwitchItem("switch")
		itemsChangeListener.itemAdded(itemProvider, item)
		itemsChangeListener.itemRemoved(itemProvider, item)
		itemsChangeListener.itemAdded(itemProvider, item)
		def oldItem = item;
		def newItem = new SwitchItem("switch")
		itemsChangeListener.itemUpdated(itemProvider, oldItem, newItem)
		
		assertThat itemAddedCalled && itemRemovedCalled && itemUpdatedCalled, is(true)
	}
	
}
