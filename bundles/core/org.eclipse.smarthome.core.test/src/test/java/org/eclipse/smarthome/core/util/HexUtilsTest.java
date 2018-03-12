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
package org.eclipse.smarthome.core.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test for {@link HexUtils}.
 *
 * @author Kai Kreuzer - Initial implementation
 */
public class HexUtilsTest {

    @Test
    public void test_bytesToHex_noParams() {
        byte[] bytes = "ABCD".getBytes();
        String result = HexUtils.bytesToHex(bytes);
        assertEquals("41424344", result);
    }

    @Test
    public void test_bytesToHex_withDelimiter() {
        byte[] bytes = "ABCD".getBytes();
        String result = HexUtils.bytesToHex(bytes, " ", null);
        assertEquals("41 42 43 44", result);
    }

    @Test
    public void test_bytesToHex_withPrefix() {
        byte[] bytes = "ABCD".getBytes();
        String result = HexUtils.bytesToHex(bytes, null, "0x");
        assertEquals("0x410x420x430x44", result);
    }

    @Test
    public void test_bytesToHex_withDelimiterAndPrefix() {
        byte[] bytes = "ABCD".getBytes();
        String result = HexUtils.bytesToHex(bytes, "-", "0x");
        assertEquals("0x41-0x42-0x43-0x44", result);
    }
}
