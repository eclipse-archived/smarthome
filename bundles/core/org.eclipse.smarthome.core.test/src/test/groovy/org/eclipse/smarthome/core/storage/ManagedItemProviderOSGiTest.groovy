/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.storage

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.items.ManagedItemProvider
import org.eclipse.smarthome.core.library.items.StringItem
import org.eclipse.smarthome.core.library.items.SwitchItem
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
		itemProvider.getItems().each {
			itemProvider.removeItem(it.name)
		}
		unregisterService(itemProvider)
	}

	@Test
	void 'assert getItems returns item from registered ManagedItemProvider'() {

		assertThat itemProvider.getItems().size, is(0)
		
		itemProvider.addItem new SwitchItem('SwitchItem')
		itemProvider.addItem new StringItem('StringItem')

		def items = itemProvider.getItems()
		assertThat items.size, is(2)
		
		itemProvider.removeItem 'StringItem'		
		itemProvider.removeItem 'SwitchItem'
		
		assertThat itemProvider.getItems().size, is(0)
	}

	@Test
	void 'assert adding twice returns first value'() {

		assertThat itemProvider.getItems().size, is(0)
		
		itemProvider.addItem new StringItem('Item')
		def result = itemProvider.addItem new SwitchItem('Item')

		assertThat result.type, is("String")
		
		itemProvider.removeItem 'Item'
		
		assertThat itemProvider.getItems().size, is(0)
	}

	@Test
	void 'assert removal returns old value'() {

		assertThat itemProvider.getItems().size, is(0)
		
		itemProvider.addItem new StringItem('Item')
		def result = itemProvider.removeItem 'Unknown'

		assertNull result

		result = itemProvider.removeItem 'Item'

		assertThat result.name, is('Item')
						
		assertThat itemProvider.getItems().size, is(0)
	}
	
	@Test
	void 'assert two items with same name can not be added'() {

		assertThat itemProvider.getItems().size, is(0)
		
		itemProvider.addItem new StringItem('Item')
		itemProvider.addItem new StringItem('Item')

		assertThat itemProvider.getItems().size(), is(1)
		assertThat itemRegistry.getItems().size(), is(1)
	}
}
