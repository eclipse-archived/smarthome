/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.common.osgi;

import static org.junit.Assert.*;

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
        assertTrue(testObject.isCharsetValid("abcßäüöÄÜÖ~âéè\u00B2".getBytes(Charset.forName("UTF-8")),
                Charset.forName("UTF-8")));
    }

    @Test
    public void testInvalidUTF8() {
        ResourceBundleClassLoader testObject = new ResourceBundleClassLoader();
        assertFalse(testObject.isCharsetValid("abcßäüöÄÜÖ~âéè".getBytes(Charset.forName("ISO-8859-1")),
                Charset.forName("UTF-8")));
    }

    @Test
    public void testValidISO8859() {
        ResourceBundleClassLoader testObject = new ResourceBundleClassLoader();
        byte[] allChars = new byte[256];
        for (char c = 0; c <= 255; c++) {
            allChars[c] = (byte) (c - 127);
        }
        assertTrue(testObject.isCharsetValid(allChars, Charset.forName("ISO-8859-1")));
    }
}
