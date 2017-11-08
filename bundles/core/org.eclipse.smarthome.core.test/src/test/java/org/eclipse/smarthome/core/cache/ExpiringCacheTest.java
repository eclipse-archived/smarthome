/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.cache;

import static org.junit.Assert.*;

import org.apache.commons.lang.RandomStringUtils;

import org.junit.Test;
import org.junit.Before;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Test class for the {@link ExpiringCache} class.
 *
 * @author Christoph Weitkamp - Initial contribution.
 */
public class ExpiringCacheTest {
    private static final long CACHE_EXPIRY = TimeUnit.SECONDS.toMillis(2);
    private static final Supplier<String> CACHE_ACTION = () -> RandomStringUtils.random(8);

    private ExpiringCache<String> subject;

    @Before
    public void setUp() {
        subject = new ExpiringCache<>(CACHE_EXPIRY, CACHE_ACTION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentException1() throws IllegalArgumentException {
        new ExpiringCache<>(CACHE_EXPIRY, null);
    }

    @Test
    public void testGetValue() {
        // use the same key twice
        String value1 = subject.getValue();

        assertNotNull(value1);

        String value2 = subject.getValue();

        assertNotNull(value2);
        assertEquals(value1, value2);
    }

    @Test
    public void testExpired() throws InterruptedException {
        String value1 = subject.getValue();
        assertFalse(subject.isExpired());

        // wait until cache expires
        Thread.sleep(CACHE_EXPIRY + 100);
        assertTrue(subject.isExpired());

        String value2 = subject.getValue();
        assertFalse(subject.isExpired());
        assertNotEquals(value1, value2);
    }

    @Test
    public void testInvalidate() {
        String value1 = subject.getValue();

        // invalidate item
        subject.invalidateValue();

        String value2 = subject.getValue();
        assertNotEquals(value1, value2);
    }

    @Test
    public void testRefresh() {
        String value1 = subject.getValue();

        // refresh item
        String value2 = subject.refreshValue();
        assertNotEquals(value1, value2);
    }
}
