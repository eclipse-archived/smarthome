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

import org.eclipse.smarthome.core.items.ItemProvider
import org.eclipse.smarthome.core.library.items.StringItem
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Before
import org.junit.Test

/**
 * The {@link ManagedItemProviderOSGiTest} runs inside an 
 * OSGi container and tests the {@link ManagedItemProvider}.  
 * 
 * @author Thomas Eichstaedt-Engelen - Initial contribution
 */
class ManagedItemProviderOSGiTest extends OSGiTest {

	ItemProvider itemProvider
	
	
	@Before
	void setUp() {
		itemProvider = getService(ItemProvider)
	}

	@Test
	void 'assert getItems returns item from registered ItemProvider'() {

		assertThat itemProvider.getItems().size, is(0)
		
		itemProvider.addItem new SwitchItem('SwitchItem')
		itemProvider.addItem new StringItem('StringItem')

		def items = itemProvider.getItems()
		assertThat items.size, is(2)
		
		itemProvider.removeItem new StringItem('StringItem')		
		itemProvider.removeItem new SwitchItem('SwitchItem')
		
		assertThat itemProvider.getItems().size, is(0)
	}
	
}
