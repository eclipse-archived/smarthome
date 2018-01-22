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
package org.eclipse.smarthome.binding.tradfri.internal.model;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

/**
 * Tests for {@link TradfriVersion}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class TradfriVersionTest {

    private static final int LESS_THAN = -1;
    private static final int EQUAL_TO = 0;
    private static final int GREATER_THAN = 1;

    private static final String VERSION_STRING = "1.2.42";
    private static final TradfriVersion VERSION = new TradfriVersion(VERSION_STRING);

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentException() throws IllegalArgumentException {
        new TradfriVersion("FAILURE");
    }

    @Test
    public void testParts() {
        assertEquals(Arrays.asList(1, 2, 42), VERSION.parts);
    }

    @Test
    public void testCompareToEqualTo() {
        assertEquals(EQUAL_TO, VERSION.compareTo(VERSION));
        assertEquals(EQUAL_TO, VERSION.compareTo(new TradfriVersion(VERSION_STRING)));
    }

    @Test
    public void testCompareToLessThan() {
        assertEquals(LESS_THAN, VERSION.compareTo(new TradfriVersion("2")));
        assertEquals(LESS_THAN, VERSION.compareTo(new TradfriVersion("1.3")));
        assertEquals(LESS_THAN, VERSION.compareTo(new TradfriVersion("1.2.50")));
        assertEquals(LESS_THAN, VERSION.compareTo(new TradfriVersion("1.2.42.5")));
    }

    @Test
    public void testCompareToGreaterThan() {
        assertEquals(GREATER_THAN, VERSION.compareTo(new TradfriVersion("1")));
        assertEquals(GREATER_THAN, VERSION.compareTo(new TradfriVersion("1.1")));
        assertEquals(GREATER_THAN, VERSION.compareTo(new TradfriVersion("1.2.30")));
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testEquals() {
        assertTrue(VERSION.equals(VERSION));
        assertTrue(VERSION.equals(new TradfriVersion(VERSION_STRING)));

        assertFalse(VERSION.equals((TradfriVersion) null));
        assertFalse(VERSION.equals(new Integer("1")));
        assertFalse(VERSION.equals(new TradfriVersion("1.2.5")));
    }

    @Test
    public void testToString() {
        assertEquals(VERSION_STRING, VERSION.toString());
    }
}
