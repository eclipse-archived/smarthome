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

import org.eclipse.smarthome.core.items.Item
import org.eclipse.smarthome.core.library.items.StringItem
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
		storage = storageService.getStorage(getBundleContext(), 'TestStorage');
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
		
		storage.put 'Key1', new TestDto('Key1', Calendar.getInstance().getTime());
		storage.put 'Key2', new TestDto('Key2', Calendar.getInstance().getTime());
		
		assertThat(storage.getKeys().size(), is(2))
		
		TestDto dto = storage.get 'Key1'
		println dto
		
		storage.remove 'Key1'		
		storage.remove 'Key2'
		
		assertThat(storage.getKeys().size(), is(0))
	}
	
	static class TestDto {
		
		String name
		Date date
		
		public TestDto() {
		}
		
		public TestDto(String name, Date date) {
			this.name = name
			this.date = date
		}

		@Override
		public String toString() {
			return "TestDto [name=" + name + ", date=" + date + "]";
		}
	
	}
	
}
