/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.storage.mapdb

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import java.util.List;

import org.eclipse.smarthome.core.items.Item
import org.eclipse.smarthome.core.library.items.StringItem
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author Thomas Eichstaedt-Engelen - Initial contribution
 * @author Alex Tugarev - Added test for getStorage without classloader
 */
class StorageServiceOSGiTest extends OSGiTest {

	MapDbStorageService storageService
	MapDbStorage storage
	
	
	@Before
	void setUp() {
		storageService = getService(StorageService)
		storage = storageService.getStorage('TestStorage', this.getClass().getClassLoader());
	}

	@After
	void tearDown() {
		unregisterService(storageService)
		
		// clean up database files ...
		new File('./runtime').deleteDir()
	}

	@Test
	void 'assert elements are serialized and deserialized by the Storage'() {

		assertThat storage.getKeys().size(), is(0)
		
		storage.put 'Key1', new PersistedItem('String', ['LIGHT', 'GROUND_FLOOR']);
		storage.put 'Key2', new PersistedItem('Number', ['TEMPERATURE', 'OUTSIDE']);
		assertThat storage.getKeys().size(), is(2)
		
		PersistedItem pItem = storage.get 'Key1'
		
		storage.remove 'Key1'		
		storage.remove 'Key2'
		assertThat storage.getKeys().size(), is(0)
	}
	
	@Test
	void 'assert old element gets overwritten when new value is stored under an existing key'() {
		PersistedItem pItem = null
		
		assertThat storage.getKeys().size(), is(0)
		
		pItem = storage.put 'Key1', new PersistedItem('String', ['LIGHT', 'GROUND_FLOOR']);
		assertThat storage.getKeys().size(), is(1)
		assertThat pItem, is(null)
		
		pItem = storage.get 'Key1'		
		assertThat pItem.itemType, is('String')
		
		pItem = storage.put 'Key1', new PersistedItem('Number', ['TEMPERATURE']);
		assertThat storage.getKeys().size(), is(1)
		assertThat pItem.itemType, is('String')
		assertThat storage.get('Key1').itemType, is('Number')

		storage.remove 'Key1'
		assertThat storage.getKeys().size(), is(0)
	}
    
    @Test
    void 'assert storage works without classloader'() {
        def storageWithoutClassloader = storageService.getStorage("storageWithoutClassloader")
        storageWithoutClassloader.put("Key1", "Value")
        
        assertThat storageWithoutClassloader.get("Key1"), is(equalTo("Value"))
    }
    
    @Test
    void 'assert store configuration works'() {
        def storageWithoutClassloader = storageService.getStorage("storage")

        def configuration = new MockConfiguration();
        configuration.put("bigDecimal", new BigDecimal(3))

        storageWithoutClassloader.put("configuration", configuration)

        def bigDecimal = storageWithoutClassloader.get("configuration").get("bigDecimal")
        assertThat bigDecimal instanceof BigDecimal, is(true)
    }
    
    private class MockConfiguration {
        private Map<String, Object> configuration = new HashMap<String, Object>();
        
        public void put(String key, Object value) {
            configuration.put(key,  value);
        }
        
        public Object get(String key) {
            return configuration.get(key);
        }
        
    }

	
	private class PersistedItem {

		public String itemType;
		public List<String> groupNames;
		public String baseItemType;
		
		public PersistedItem(String itemType, List<String> groupNames) {
			this(itemType, groupNames, null);
		}

		public PersistedItem(String itemType, List<String> groupNames, String baseItemType) {
			this.itemType = itemType;
			this.groupNames = groupNames;
			this.baseItemType = baseItemType;
		}

		@Override
		public String toString() {
			return "PersistedItem [itemType=$itemType, groupNames=$groupNames, baseItemType=$baseItemType]";
		}
		
	}
	
	
}
