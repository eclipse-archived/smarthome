/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.cache;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for the {@link ExpiringCacheMap} class.
 *
 * @author Christoph Weitkamp - Initial contribution.
 */
public class ExpiringCacheMapTest {

    private final Logger logger = LoggerFactory.getLogger(ExpiringCacheMapTest.class);

    public static final long CACHE_EXPIRY = 2 * 1000; // 2s

    public static final Supplier<String> CACHE_ACTION = () -> RandomStringUtils.random(8);

    public static final String FIRST_TEST_KEY = "FIRST_TEST_KEY";
    public static final String SECOND_TEST_KEY = "SECOND_TEST_KEY";

    @Test(expected = IllegalArgumentException.class)
    public void testAddIllegalArgumentException1() throws IllegalArgumentException {
        final ExpiringCacheMap<String, String> cache = new ExpiringCacheMap<>(CACHE_EXPIRY);

        final Supplier<String> action = null;
        cache.put(FIRST_TEST_KEY, action);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddIllegalArgumentException2() throws IllegalArgumentException {
        final ExpiringCacheMap<String, String> cache = new ExpiringCacheMap<>(CACHE_EXPIRY);

        final ExpiringCache<String> item = null;
        cache.put(FIRST_TEST_KEY, item);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddIllegalArgumentException3() throws IllegalArgumentException {
        final ExpiringCacheMap<String, String> cache = new ExpiringCacheMap<>(CACHE_EXPIRY);

        cache.put(null, CACHE_ACTION);
    }

    @Test
    public void testContainsKey() {
        final ExpiringCacheMap<String, String> cache = new ExpiringCacheMap<>(CACHE_EXPIRY);

        cache.put(FIRST_TEST_KEY, CACHE_ACTION);

        assertTrue(cache.containsKey(FIRST_TEST_KEY));
    }

    @Test
    public void testKeys() {
        final ExpiringCacheMap<String, String> cache = new ExpiringCacheMap<>(CACHE_EXPIRY);

        cache.put(FIRST_TEST_KEY, CACHE_ACTION);

        // get all keys
        final Set<String> expected_keys = new LinkedHashSet<>();
        expected_keys.add(FIRST_TEST_KEY);

        final Set<String> keys = cache.keys();
        assertEquals(expected_keys, keys);
    }

    @Test
    public void testValues() {
        final ExpiringCacheMap<String, String> cache = new ExpiringCacheMap<>(CACHE_EXPIRY);

        cache.put(FIRST_TEST_KEY, CACHE_ACTION);

        // use the same key twice
        String value1 = cache.get(FIRST_TEST_KEY);
        assertNotNull(value1);
        String value2 = cache.get(FIRST_TEST_KEY);
        assertNotNull(value2);
        assertEquals(value1, value2);

        cache.put(SECOND_TEST_KEY, CACHE_ACTION);

        // use a different key
        String value3 = cache.get(SECOND_TEST_KEY);
        assertNotNull(value3);
        assertNotEquals(value1, value3);

        // get all values
        final Collection<String> expected_values = new LinkedList<>();
        expected_values.add(value3);
        expected_values.add(value1);

        final Collection<String> values = cache.values();
        assertEquals(expected_values, values);

        // use another different key
        String value4 = cache.get("KEY_NOT_FOUND");
        assertNull(value4);
    }

    @Test
    public void testExpired() throws InterruptedException {
        final ExpiringCacheMap<String, String> cache = new ExpiringCacheMap<>(CACHE_EXPIRY);

        cache.put(FIRST_TEST_KEY, CACHE_ACTION);

        String value1 = cache.get(FIRST_TEST_KEY);

        // wait until cache expires
        Thread.sleep(CACHE_EXPIRY + 100);

        String value2 = cache.get(FIRST_TEST_KEY);
        assertNotEquals(value1, value2);
    }

    @Test
    public void testInvalidate() {
        final ExpiringCacheMap<String, String> cache = new ExpiringCacheMap<>(CACHE_EXPIRY);

        cache.put(FIRST_TEST_KEY, CACHE_ACTION);

        String value1 = cache.get(FIRST_TEST_KEY);

        // invalidate item
        cache.invalidate(FIRST_TEST_KEY);

        String value2 = cache.get(FIRST_TEST_KEY);
        assertNotEquals(value1, value2);

        // invalidate all
        cache.invalidateAll();

        String value3 = cache.get(FIRST_TEST_KEY);
        assertNotEquals(value2, value3);
    }

    @Test
    public void testRefresh() {
        final ExpiringCacheMap<String, String> cache = new ExpiringCacheMap<>(CACHE_EXPIRY);

        cache.put(FIRST_TEST_KEY, CACHE_ACTION);

        String value1 = cache.get(FIRST_TEST_KEY);

        // refresh item
        String value2 = cache.refresh(FIRST_TEST_KEY);
        assertNotEquals(value1, value2);

        // refresh all
        final Collection<String> expected_values = new LinkedList<>();
        expected_values.add(value2);

        final Collection<String> values = cache.refreshAll();
        assertNotEquals(expected_values, values);
    }

    @Test
    public void testRemove() {
        final ExpiringCacheMap<String, String> cache = new ExpiringCacheMap<>(CACHE_EXPIRY);

        cache.put(FIRST_TEST_KEY, CACHE_ACTION);

        // remove item
        cache.remove(FIRST_TEST_KEY);

        String value1 = cache.get(FIRST_TEST_KEY);
        assertNull(value1);

    }

    @Test
    public void testClear() {
        final ExpiringCacheMap<String, String> cache = new ExpiringCacheMap<>(CACHE_EXPIRY);

        cache.put(FIRST_TEST_KEY, CACHE_ACTION);

        // clear cache
        cache.clear();

        String value1 = cache.get(FIRST_TEST_KEY);
        assertNull(value1);
    }
}
