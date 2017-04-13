/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.storage.json.test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.storage.json.JsonStorage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * This test makes sure that the JSonStorage loads all stored numbers as BigDecimal
 *
 * @author Stefan Triller - Initial Contribution
 */
public class JSonStorageTest {

    private JsonStorage<Object> objectStorage;

    @Before
    public void setUp() throws IOException {
        File tmpFile = File.createTempFile("storage-debug", ".json");
        tmpFile.deleteOnExit();

        objectStorage = new JsonStorage<>(tmpFile, this.getClass().getClassLoader(), 0, 0, 0);
    }

    @Test
    public void allInsertedNumbersAreLoadedAsBigDecimal() {
        objectStorage.put("DummyObject", new DummyObject());

        DummyObject dummy = (DummyObject) objectStorage.get("DummyObject");

        Assert.assertTrue(dummy.myMap.get("testShort") instanceof BigDecimal);
        Assert.assertTrue(dummy.myMap.get("testInt") instanceof BigDecimal);
        Assert.assertTrue(dummy.myMap.get("testLong") instanceof BigDecimal);
        Assert.assertTrue(dummy.myMap.get("testDouble") instanceof BigDecimal);
        Assert.assertTrue(dummy.myMap.get("testFloat") instanceof BigDecimal);
        Assert.assertTrue(dummy.myMap.get("testBigDecimal") instanceof BigDecimal);
        Assert.assertTrue(dummy.myMap.get("testBoolean") instanceof Boolean);
        Assert.assertTrue(dummy.myMap.get("testString") instanceof String);
    }

    private class DummyObject {

        public Map<String, Object> myMap = new HashMap<String, Object>();

        public DummyObject() {
            myMap.put("testShort", Short.valueOf("12"));
            myMap.put("testInt", Integer.valueOf("12"));
            myMap.put("testLong", Long.valueOf("12"));
            myMap.put("testDouble", Double.valueOf("12.12"));
            myMap.put("testFloat", Float.valueOf("12.12"));
            myMap.put("testBigDecimal", new BigDecimal(12));
            myMap.put("testBoolean", true);
            myMap.put("testString", "hello world");
        }
    }

}
