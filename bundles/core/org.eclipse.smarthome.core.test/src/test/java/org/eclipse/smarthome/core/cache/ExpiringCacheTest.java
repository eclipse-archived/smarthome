/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.cache;

import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for the {@link ExpiringCache} class.
 *
 * @author Christoph Weitkamp - Initial contribution.
 */
public class ExpiringCacheTest {

    private final Logger logger = LoggerFactory.getLogger(ExpiringCacheTest.class);

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentException1() throws IllegalArgumentException {
        new ExpiringCache<>(ExpiringCacheMapTest.CACHE_EXPIRY, null);
    }

    @Test
    public void testGetValue() {
        final ExpiringCache<String> single_cache = new ExpiringCache<>(ExpiringCacheMapTest.CACHE_EXPIRY,
                ExpiringCacheMapTest.CACHE_ACTION);

        // use the same key twice
        String value1 = single_cache.getValue();
        assertNotNull(value1);
        String value2 = single_cache.getValue();
        assertNotNull(value2);
        assertEquals(value1, value2);
    }

    @Test
    public void testExpired() throws InterruptedException {
        final ExpiringCache<String> single_cache = new ExpiringCache<>(ExpiringCacheMapTest.CACHE_EXPIRY,
                ExpiringCacheMapTest.CACHE_ACTION);

        String value1 = single_cache.getValue();
        assertFalse(single_cache.isExpired());

        // wait until cache expires
        Thread.sleep(ExpiringCacheMapTest.CACHE_EXPIRY + 100);
        assertTrue(single_cache.isExpired());

        String value2 = single_cache.getValue();
        assertFalse(single_cache.isExpired());
        assertNotEquals(value1, value2);
    }

    @Test
    public void testInvalidate() {
        final ExpiringCache<String> single_cache = new ExpiringCache<>(ExpiringCacheMapTest.CACHE_EXPIRY,
                ExpiringCacheMapTest.CACHE_ACTION);

        String value1 = single_cache.getValue();

        // invalidate item
        single_cache.invalidateValue();

        String value2 = single_cache.getValue();
        assertNotEquals(value1, value2);
    }

    @Test
    public void testRefresh() {
        final ExpiringCache<String> single_cache = new ExpiringCache<>(ExpiringCacheMapTest.CACHE_EXPIRY,
                ExpiringCacheMapTest.CACHE_ACTION);

        String value1 = single_cache.getValue();

        // refresh item
        String value2 = single_cache.refreshValue();
        assertNotEquals(value1, value2);
    }
}
