/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class PortableBase64 will provide Base64 encode and decode functionality in a portable way for JavaSE 7 and
 * JavaSE 8. Its design is based on the new java.util.Base64 class from JavaSE 8.
 *
 * The implementation checks for Java version. It will call either an included class from javax.xml.bind or use the new
 * Base64 class from JavaSE 8. This has been chosen to avoid a compile dependency to Java 8.
 *
 * For JavaSE 7 it will use the class javax.xml.bind.DatatypeConverter for
 * Base64 functionality. It will get the encode and decode methods to be able to call this methods later via reflection.
 *
 * For JavaSE 8 it will use the built-in class java.util.Base64, get the instances for encoder and decoder and will use
 * them for calling.
 *
 * @author Jochen Hiller - Initial contribution
 * @author Miki Jankov - Adding error checks
 *
 */
public class PortableBase64 {

    /**
     * The encoder instance will be needed for JavaSE 8 as the encode method is an instance method. For JavaSE 7 this is
     * null as it is a static method.
     */
    private static Object encoderInstance;

    /**
     * The encode method will expect a signature of:
     *
     * <pre>
     * String encodeMethod(byte[] b);
     * </pre>
     */
    private static Method encodeMethod;

    /**
     * The decoder instance will be needed for JavaSE 8 as the decode method is an instance method. For JavaSE 7 this is
     * null as it is a static method.
     */
    private static Object decoderInstance;

    /**
     * The decode method will expect a signature of:
     *
     * <pre>
     * byte[] decodeMethod(String s);
     * </pre>
     */
    private static Method decodeMethod;

    /**
     * A flag to show if initialization was already performed
     */
    private static volatile boolean isInitialized = false;

    // static helpers to initialize

    public static void initialize() {
        if (isInitialized) {
            logDebug("PortableBase64 class already initialized");
            return;
        }

        // just try to get Java 8(or newer) class
        boolean isJava8OrNewer = true;
        try {
            Class.forName("java.util.Base64", false, PortableBase64.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
            // not found, so we run on JavaSE 7 or older
            isJava8OrNewer = false;
        }
        logDebug("PortableBase64 class is running on JavaSE " + (isJava8OrNewer ? ">=8" : "<=7"));

        try {
            if (isJava8OrNewer) {
                initializeJava8();
            } else {
                initializeJava7();
            }
            // make one test call for encode and decode to be sure that it is working
            // see https://tools.ietf.org/html/rfc4648#section-10 for samples
            String encodedAsString = (String) encodeMethod.invoke(encoderInstance, "foobar".getBytes("UTF-8"));
            if (!"Zm9vYmFy".equals(encodedAsString)) {
                throw new IllegalAccessError("encode does not work as expected");
            }
            byte[] decodedAsByteArray = (byte[]) decodeMethod.invoke(decoderInstance, "Zm9vYmFy");
            String decodedAsString = new String(decodedAsByteArray, "UTF-8");
            if (!"foobar".equals(decodedAsString)) {
                throw new IllegalAccessError("decode does not work as expected");
            }
            PortableBase64.isInitialized = true;
        } catch (Exception ex) {
            logError(
                    "Could not initialize PortableBase64 class- Check your Java environment to run on Java 7 or 8 or later.",
                    ex);
            encodeMethod = null;
            decodeMethod = null;
            // TODO fallback to an internal implementation? E.g. for JavaSE 6/5/4...
        }
    }

    /**
     * Initialization for JavaSE 7 using javax.xml.bind.DatatypeConverter class.
     */
    private static void initializeJava7() throws Exception {
        // now we know that the class DataTypeConverter is available
        Class<?> datatypeConverterClass = Class.forName("javax.xml.bind.DatatypeConverter", false,
                PortableBase64.class.getClassLoader());
        // preserve static methods for later use
        encodeMethod = datatypeConverterClass.getMethod("printBase64Binary", new Class[] { byte[].class });
        decodeMethod = datatypeConverterClass.getMethod("parseBase64Binary", new Class[] { String.class });
    }

    /**
     * Initialization for JavaSE 8 using java.util.Base64 class.
     */
    private static void initializeJava8() throws Exception {
        Class<?> baseClass = Class.forName("java.util.Base64", false, PortableBase64.class.getClassLoader());

        // search for inner classes
        Class<?>[] innerClasses = baseClass.getDeclaredClasses();
        Class<?> encoderClass = null;
        Class<?> decoderClass = null;
        for (int i = 0; i < innerClasses.length; i++) {
            Class<?> c = innerClasses[i];
            if (c.getName().equals("java.util.Base64$Encoder")) {
                encoderClass = c;
            } else if (c.getName().equals("java.util.Base64$Decoder")) {
                decoderClass = c;
            } else {
                // ignore, we do not need
            }
        }
        // check if we found the classes
        if (encoderClass == null) {
            throw new IllegalAccessError("Could not find encoderClass java.util.Base64$Encoder");
        }
        if (decoderClass == null) {
            throw new IllegalAccessError("Could not find decoderClass java.util.Base64$Decoder");
        }

        // preserve the instances of encoder and decoder
        PortableBase64.encoderInstance = baseClass.getMethod("getEncoder", new Class[] {}).invoke(null,
                (Object[]) null);
        PortableBase64.decoderInstance = baseClass.getMethod("getDecoder", new Class[] {}).invoke(null,
                (Object[]) null);
        // preserve method on instances for later use
        encodeMethod = encoderClass.getMethod("encodeToString", new Class[] { byte[].class });
        decodeMethod = decoderClass.getMethod("decode", new Class[] { String.class });
    }

    private static void logError(String msg, Exception ex) {
        Logger l = LoggerFactory.getLogger(PortableBase64.class);
        l.error(msg, ex);
    }

    private static void logDebug(String msg) {
        Logger l = LoggerFactory.getLogger(PortableBase64.class);
        l.debug(msg);
    }

    // PortablBase64 implementation

    private static Encoder basicEncoder = new Encoder();
    private static Decoder basicDecoder = new Decoder();

    public static Encoder getEncoder() {
        return basicEncoder;
    }

    public static Decoder getDecoder() {
        return basicDecoder;
    }

    public static class Encoder {
        public String encode(byte[] base64) {
            try {
                if (!isInitialized) {
                    throw new IllegalStateException("PortableBase64 is not initialized");
                }
                Object res = encodeMethod.invoke(encoderInstance, base64);
                return (String) res;
            } catch (IllegalStateException ise) {
                throw ise;
            } catch (Exception ex) {
                PortableBase64.logError("PortableBase64 - Could not encode", ex);
                throw new IllegalArgumentException(ex.getMessage());
            }
        }
    }

    public static class Decoder {
        public byte[] decode(String s) {
            try {
                if (!isInitialized) {
                    throw new IllegalStateException("PortableBase64 is not initialized");
                }
                Object res = decodeMethod.invoke(decoderInstance, s);
                byte[] b = (byte[]) res;
                // System.out.println("'" + new String(b) + "'");
                if ((b.length == 0) && (s.length() > 0)) {
                    throw new IllegalArgumentException("decode returned empty result");
                }
                return b;
            } catch (IllegalStateException ise) {
                throw ise;
            } catch (IllegalArgumentException ex) {
                throw ex;
            } catch (Exception ex) {
                PortableBase64.logError("PortableBase64 - Could not decode", ex);
                throw new IllegalArgumentException(ex.getMessage());
            }
        }
    }
}