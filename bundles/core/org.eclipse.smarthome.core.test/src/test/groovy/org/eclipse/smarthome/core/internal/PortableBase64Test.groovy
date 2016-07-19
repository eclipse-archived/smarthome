/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal

import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.MatcherAssert.assertThat
import static org.junit.Assert.fail

import java.io.UnsupportedEncodingException

import org.junit.Before
import org.junit.Ignore
import org.junit.Test

/**
 * @author Jochen Hiller - Initial contribution
 * @author Miki Jankov - Adding new tests and fixing existing ones
 */
public class PortableBase64Test {

    private static boolean isJava8OrNewer = true

    @Before
    public void testInitialize() {
        try {
            Class.forName("java.util.Base64", false, PortableBase64.class.getClassLoader())
        } catch (ClassNotFoundException ex) {
            // not found, so we run on JavaSE 7 or older
            isJava8OrNewer = false
        }
        PortableBase64.initialize()
    }

    @Test
    public void testGetStaticClasses() {
        PortableBase64.Decoder decoder = PortableBase64.getDecoder()
        assertThat(decoder, is(not(nullValue())))

        PortableBase64.Encoder encoder = PortableBase64.getEncoder()
        assertThat(encoder, is(not(nullValue())))
    }

    @Test
    public void testDecode() {
        // see https://tools.ietf.org/html/rfc4648#section-10
        FROM_BASE64("", "")
        FROM_BASE64("Zg==", "f")
        FROM_BASE64("Zm8=", "fo")
        FROM_BASE64("Zm9v", "foo")
        FROM_BASE64("Zm9vYg==", "foob")
        FROM_BASE64("Zm9vYmE=", "fooba")
        FROM_BASE64("Zm9vYmFy", "foobar")
    }

    @Test
    public void testEncode() {
        // see https://tools.ietf.org/html/rfc4648#section-10
        TO_BASE64("", "")
        TO_BASE64("f", "Zg==")
        TO_BASE64("fo", "Zm8=")
        TO_BASE64("foo", "Zm9v")
        TO_BASE64("foob", "Zm9vYg==")
        TO_BASE64("fooba", "Zm9vYmE=")
        TO_BASE64("foobar", "Zm9vYmFy")
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testDecodeInvalidCharacterDot() {
        PortableBase64.getDecoder().decode("......")
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testDecodeInvalidCharacterDash() {
        PortableBase64.getDecoder().decode("---")
    }

    @Test(expected = java.lang.IllegalStateException.class)
    public void testEncodeWhenClassNotInitialized() {
        PortableBase64.isInitialized = false
        PortableBase64.getEncoder().encode("".bytes)
    }

    @Test(expected = java.lang.IllegalStateException.class)
    public void testDecodeWhenClassNotInitialized() {
        PortableBase64.isInitialized = false
        PortableBase64.getDecoder().decode("")
    }

    /** JavaSE 7 does NOT throw an IllegalArgumentException. */
    @Test
    public void testDecodeInvalidPaddingStart1() {
        try {
            PortableBase64.getDecoder().decode("=A==")
            if (isJava8OrNewer) {
                fail("IllegalArgumentException expected in JavaSE 8")
            }
        } catch (Exception ex) {
            if (!isJava8OrNewer) {
                fail("No exception expected in JavaSE 7")
            }
        }
    }

    /** JavaSE 7 does NOT throw an IllegalArgumentException. */
    @Test
    public void testDecodeInvalidPaddingStart2() {
        try {
            PortableBase64.getDecoder().decode("====")
            if (isJava8OrNewer) {
                fail("IllegalArgumentException expected in JavaSE 8")
            }
        } catch (Exception ex) {
            if (!isJava8OrNewer) {
                fail("No exception expected in JavaSE 7")
            }
        }
    }

    /** JavaSE 7 does NOT throw an IllegalArgumentException. */
    @Test
    public void testDecodeInvalidPaddingMiddle() {
        try {
            PortableBase64.getDecoder().decode("Zg=a")
            if (isJava8OrNewer) {
                fail("IllegalArgumentException expected in JavaSE 8")
            }
        } catch (Exception ex) {
            if (!isJava8OrNewer) {
                fail("No exception expected in JavaSE 7")
            }
        }
    }

    /**
     * TODO we can not easily compare this by native calls as this would
     */
    @Test
    public void testPerformancePortableBase64() {
        long tStart = System.nanoTime()
        int N = 10000000
        for (int i = 0; i < N; i++) {
            PortableBase64.getEncoder().encode("foobar".getBytes())
            PortableBase64.getDecoder().decode("Zm9vYmFy")
        }
        long tEnd = System.nanoTime()
        System.out.println("testPerformancePortableBase64 took " + (tEnd - tStart) / 1000 / 1000 + " ms for " + N
                + " iterations on " + System.getProperty("java.version") + ".")
    }

    @Test
    public void testPerformanceJavaSE7() {
        long tStart = System.nanoTime()
        int N = 10000000
        for (int i = 0; i < N; i++) {
            javax.xml.bind.DatatypeConverter.printBase64Binary("foobar".getBytes())
            javax.xml.bind.DatatypeConverter.parseBase64Binary("Zm9vYmFy")
        }
        long tEnd = System.nanoTime()
        System.out.println("testPerformanceJavaSE7 " + (tEnd - tStart) / 1000 / 1000 + " ms for " + N
                + " iterations on " + System.getProperty("java.version") + ".")
    }

    /**
     * If you want to run the test on JavaSE 8, remove @Ignore, enable the code below and import java.util.Base64 and
     * compile for Java 8.
     */
    @Test
    @Ignore
    public void testPerformanceJavaSE8() {
        long tStart = System.nanoTime()
        int N = 10000000
        for (int i = 0; i < N; i++) {
            // enable this code to run performance tests on JavaSE 8.
            // Base64.getEncoder().encode("foobar".getBytes());
            // Base64.getDecoder().decode("Zm9vYmFy");
        }
        long tEnd = System.nanoTime()
        System.out.println("testPerformanceJavaSE7 " + (tEnd - tStart) / 1000 / 1000 + " ms for " + N
                + " iterations on " + System.getProperty("java.version") + ".")
    }

    private void FROM_BASE64(String base64, String output) {
        try {
            PortableBase64.Decoder decoder = PortableBase64.getDecoder()
            byte[] decodedAsByteArray = decoder.decode(base64)
            String decodedAsString = new String(decodedAsByteArray, "UTF-8")
            assertThat(output, is(equalTo(decodedAsString)))
        } catch (UnsupportedEncodingException ex) {
            fail("Should never happen")
        }
    }

    private void TO_BASE64(String input, String res) {
        PortableBase64.Encoder encoder = PortableBase64.getEncoder()
        String base64 = encoder.encode(input.getBytes())
        assertThat(res, is(equalTo(base64)))
    }
}