/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.storage.mapdb

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.items.GenericItem
import org.eclipse.smarthome.core.items.Item
import org.eclipse.smarthome.core.library.items.StringItem
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.core.storage.StorageService
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author Thomas Eichstaedt-Engelen - Initial contribution
 */
class StorageServiceOSGiTest extends OSGiTest {

	MapDbStorageService storageService
	MapDbStorage storage
	
	
	@Before
	void setUp() {
		storageService = getService(StorageService)
		storage = storageService.getStorage(GenericItem.class, getBundleContext(), Item.class.getName());
	}

	@After
	void tearDown() {
		unregisterService(storageService)
		
		// clean up database files ...
		new File('./etc/mapdb').deleteDir()
	}

	@Test
	void 'assert getItems returns item from registered StorageService'() {

		assertThat(storage.getKeys().size(), is(0))
		
		storage.put('Switch', new SwitchItem('SwitchItem'))
		storage.put('String', new StringItem('StringItem'))
		
		assertThat(storage.getKeys().size(), is(2))
		
//		storage.remove 'String'		
//		storage.remove 'Switch'
//		
//		assertThat(storage.getKeys().size(), is(0))
		
		storage.get 'String'
	}
	
}
