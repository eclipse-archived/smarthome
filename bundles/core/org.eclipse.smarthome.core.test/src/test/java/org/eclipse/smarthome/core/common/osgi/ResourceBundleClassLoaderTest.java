/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.common.osgi;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.junit.Test;

/**
 * Tests for method {@link ResourceBundleClassLoader#isCharsetValid(InputStream, Charset)}
 *
 * @author Martin Herbst
 *
 */
public class ResourceBundleClassLoaderTest {

    @Test
    public void testValidUTF8() {
        ResourceBundleClassLoader testObject = new ResourceBundleClassLoader();
        // \u00B2 = ²
        InputStream is = new ByteArrayInputStream("abcßäüöÄÜÖ~âéè\u00B2".getBytes(Charset.forName("UTF-8")));
        assertTrue(testObject.isCharsetValid(is, Charset.forName("UTF-8")));
    }

    @Test
    public void testInvalidUTF8() {
        ResourceBundleClassLoader testObject = new ResourceBundleClassLoader();
        InputStream is = new ByteArrayInputStream("abcßäüöÄÜÖ~âéè".getBytes(Charset.forName("ISO-8859-1")));
        assertFalse(testObject.isCharsetValid(is, Charset.forName("UTF-8")));
    }

    @Test
    public void testValidUTF8Large() {
        ResourceBundleClassLoader testObject = new ResourceBundleClassLoader();
        byte[] buff = new byte[520];
        for (int i = 0; i < 520; i++) {
            buff[i] = 65;
        }
        InputStream is = new ByteArrayInputStream(buff);
        assertTrue(testObject.isCharsetValid(is, Charset.forName("UTF-8")));
    }

    @Test
    public void testInvalidUTF8Large() {
        ResourceBundleClassLoader testObject = new ResourceBundleClassLoader();
        byte[] buff = new byte[520];
        for (int i = 0; i < 520; i++) {
            buff[i] = 65;
        }
        // non-UTF-8 character at the end of second chunk
        buff[519] = (byte) 0xA7; // §
        InputStream is = new ByteArrayInputStream(buff);
        assertFalse(testObject.isCharsetValid(is, Charset.forName("UTF-8")));
    }

    @Test
    public void testValidISO8859() {
        ResourceBundleClassLoader testObject = new ResourceBundleClassLoader();
        byte[] allChars = new byte[256];
        for (char c = 0; c <= 255; c++) {
            allChars[c] = (byte) (c - 127);
        }
        InputStream is = new ByteArrayInputStream(allChars);
        assertTrue(testObject.isCharsetValid(is, Charset.forName("ISO-8859-1")));
    }
}
