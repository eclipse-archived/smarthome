/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.storage.mapdb;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Thomas Eichstaedt-Engelen - Initial contribution
 * @author Alex Tugarev - Added test for getStorage without classloader
 * @author Simon Kaufmann - ported to Java
 */
public class StorageServiceOSGiTest extends JavaOSGiTest {

    private StorageService storageService;
    private Storage<PersistedItem> storage;

    @Before
    public void setUp() {
        storageService = getService(StorageService.class);
        storage = storageService.getStorage("TestStorage", this.getClass().getClassLoader());
    }

    @After
    public void tearDown() throws IOException {
        unregisterService(storageService);

        // clean up database files ...
        FileUtils.deleteDirectory(new File("./runtime"));
    }

    @Test
    public void testPut() {

        assertThat(storage.getKeys().size(), is(0));

        storage.put("Key1", new PersistedItem("String", Arrays.asList("LIGHT", "GROUND_FLOOR")));
        storage.put("Key2", new PersistedItem("Number", Arrays.asList("TEMPERATURE", "OUTSIDE")));
        assertThat(storage.getKeys().size(), is(2));

        PersistedItem pItem = storage.get("Key1");
        assertThat(pItem, is(notNullValue()));

        storage.remove("Key1");
        storage.remove("Key2");
        assertThat(storage.getKeys().size(), is(0));
    }

    @Test
    public void testPut_overrides() {
        PersistedItem pItem = null;

        assertThat(storage.getKeys().size(), is(0));

        pItem = storage.put("Key1", new PersistedItem("String", Arrays.asList("LIGHT", "GROUND_FLOOR")));
        assertThat(storage.getKeys().size(), is(1));
        assertThat(pItem, is(nullValue()));

        pItem = storage.get("Key1");
        assertThat(pItem.itemType, is("String"));

        pItem = storage.put("Key1", new PersistedItem("Number", Arrays.asList("TEMPERATURE")));
        assertThat(storage.getKeys().size(), is(1));
        assertThat(pItem.itemType, is("String"));
        assertThat(storage.get("Key1").itemType, is("Number"));

        storage.remove("Key1");
        assertThat(storage.getKeys().size(), is(0));
    }

    @Test
    public void testPut_noClassloader() {
        Storage<String> storageWithoutClassloader = storageService.getStorage("storageWithoutClassloader");
        storageWithoutClassloader.put("Key1", "Value");

        assertThat(storageWithoutClassloader.get("Key1"), is(equalTo("Value")));
    }

    @Test
    public void testConfiguration() {
        Storage<MockConfiguration> storageWithoutClassloader = storageService.getStorage("storage");

        MockConfiguration configuration = new MockConfiguration();
        configuration.put("bigDecimal", new BigDecimal(3));

        storageWithoutClassloader.put("configuration", configuration);

        Object bigDecimal = storageWithoutClassloader.get("configuration").get("bigDecimal");
        assertThat(bigDecimal instanceof BigDecimal, is(true));
    }

    private class MockConfiguration {
        private final Map<String, Object> configuration = new HashMap<String, Object>();

        public void put(String key, Object value) {
            configuration.put(key, value);
        }

        public Object get(String key) {
            return configuration.get(key);
        }
    }

    private static class PersistedItem {

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
