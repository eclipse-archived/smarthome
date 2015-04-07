/*******************************************************************************
 * Copyright (c) 2013, 2015 Orange.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Victor PERRON, Antonin CHAZALET, Andre BOTTARO.
 *******************************************************************************/

package org.eclipse.smarthome.protocols.enocean.basedriver.impl.utils;

/**
 * Utils.
 */
public class Utils {
    private static byte[] crc8_table = new byte[] { (byte) 0x00, (byte) 0x07, (byte) 0x0e, (byte) 0x09, (byte) 0x1c,
            (byte) 0x1b, (byte) 0x12, (byte) 0x15, (byte) 0x38, (byte) 0x3f, (byte) 0x36, (byte) 0x31, (byte) 0x24,
            (byte) 0x23, (byte) 0x2a, (byte) 0x2d, (byte) 0x70, (byte) 0x77, (byte) 0x7e, (byte) 0x79, (byte) 0x6c,
            (byte) 0x6b, (byte) 0x62, (byte) 0x65, (byte) 0x48, (byte) 0x4f, (byte) 0x46, (byte) 0x41, (byte) 0x54,
            (byte) 0x53, (byte) 0x5a, (byte) 0x5d, (byte) 0xe0, (byte) 0xe7, (byte) 0xee, (byte) 0xe9, (byte) 0xfc,
            (byte) 0xfb, (byte) 0xf2, (byte) 0xf5, (byte) 0xd8, (byte) 0xdf, (byte) 0xd6, (byte) 0xd1, (byte) 0xc4,
            (byte) 0xc3, (byte) 0xca, (byte) 0xcd, (byte) 0x90, (byte) 0x97, (byte) 0x9e, (byte) 0x99, (byte) 0x8c,
            (byte) 0x8b, (byte) 0x82, (byte) 0x85, (byte) 0xa8, (byte) 0xaf, (byte) 0xa6, (byte) 0xa1, (byte) 0xb4,
            (byte) 0xb3, (byte) 0xba, (byte) 0xbd, (byte) 0xc7, (byte) 0xc0, (byte) 0xc9, (byte) 0xce, (byte) 0xdb,
            (byte) 0xdc, (byte) 0xd5, (byte) 0xd2, (byte) 0xff, (byte) 0xf8, (byte) 0xf1, (byte) 0xf6, (byte) 0xe3,
            (byte) 0xe4, (byte) 0xed, (byte) 0xea, (byte) 0xb7, (byte) 0xb0, (byte) 0xb9, (byte) 0xbe, (byte) 0xab,
            (byte) 0xac, (byte) 0xa5, (byte) 0xa2, (byte) 0x8f, (byte) 0x88, (byte) 0x81, (byte) 0x86, (byte) 0x93,
            (byte) 0x94, (byte) 0x9d, (byte) 0x9a, (byte) 0x27, (byte) 0x20, (byte) 0x29, (byte) 0x2e, (byte) 0x3b,
            (byte) 0x3c, (byte) 0x35, (byte) 0x32, (byte) 0x1f, (byte) 0x18, (byte) 0x11, (byte) 0x16, (byte) 0x03,
            (byte) 0x04, (byte) 0x0d, (byte) 0x0a, (byte) 0x57, (byte) 0x50, (byte) 0x59, (byte) 0x5e, (byte) 0x4b,
            (byte) 0x4c, (byte) 0x45, (byte) 0x42, (byte) 0x6f, (byte) 0x68, (byte) 0x61, (byte) 0x66, (byte) 0x73,
            (byte) 0x74, (byte) 0x7d, (byte) 0x7a, (byte) 0x89, (byte) 0x8e, (byte) 0x87, (byte) 0x80, (byte) 0x95,
            (byte) 0x92, (byte) 0x9b, (byte) 0x9c, (byte) 0xb1, (byte) 0xb6, (byte) 0xbf, (byte) 0xb8, (byte) 0xad,
            (byte) 0xaa, (byte) 0xa3, (byte) 0xa4, (byte) 0xf9, (byte) 0xfe, (byte) 0xf7, (byte) 0xf0, (byte) 0xe5,
            (byte) 0xe2, (byte) 0xeb, (byte) 0xec, (byte) 0xc1, (byte) 0xc6, (byte) 0xcf, (byte) 0xc8, (byte) 0xdd,
            (byte) 0xda, (byte) 0xd3, (byte) 0xd4, (byte) 0x69, (byte) 0x6e, (byte) 0x67, (byte) 0x60, (byte) 0x75,
            (byte) 0x72, (byte) 0x7b, (byte) 0x7c, (byte) 0x51, (byte) 0x56, (byte) 0x5f, (byte) 0x58, (byte) 0x4d,
            (byte) 0x4a, (byte) 0x43, (byte) 0x44, (byte) 0x19, (byte) 0x1e, (byte) 0x17, (byte) 0x10, (byte) 0x05,
            (byte) 0x02, (byte) 0x0b, (byte) 0x0c, (byte) 0x21, (byte) 0x26, (byte) 0x2f, (byte) 0x28, (byte) 0x3d,
            (byte) 0x3a, (byte) 0x33, (byte) 0x34, (byte) 0x4e, (byte) 0x49, (byte) 0x40, (byte) 0x47, (byte) 0x52,
            (byte) 0x55, (byte) 0x5c, (byte) 0x5b, (byte) 0x76, (byte) 0x71, (byte) 0x78, (byte) 0x7f, (byte) 0x6A,
            (byte) 0x6d, (byte) 0x64, (byte) 0x63, (byte) 0x3e, (byte) 0x39, (byte) 0x30, (byte) 0x37, (byte) 0x22,
            (byte) 0x25, (byte) 0x2c, (byte) 0x2b, (byte) 0x06, (byte) 0x01, (byte) 0x08, (byte) 0x0f, (byte) 0x1a,
            (byte) 0x1d, (byte) 0x14, (byte) 0x13, (byte) 0xae, (byte) 0xa9, (byte) 0xa0, (byte) 0xa7, (byte) 0xb2,
            (byte) 0xb5, (byte) 0xbc, (byte) 0xbb, (byte) 0x96, (byte) 0x91, (byte) 0x98, (byte) 0x9f, (byte) 0x8a,
            (byte) 0x8D, (byte) 0x84, (byte) 0x83, (byte) 0xde, (byte) 0xd9, (byte) 0xd0, (byte) 0xd7, (byte) 0xc2,
            (byte) 0xc5, (byte) 0xcc, (byte) 0xcb, (byte) 0xe6, (byte) 0xe1, (byte) 0xe8, (byte) 0xef, (byte) 0xfa,
            (byte) 0xfd, (byte) 0xf4, (byte) 0xf3 };

    /**
     * Computes the EnOcean ESP3 CRC8 of a byte array. See EnOcean ESP3
     * specification for details.
     * 
     * @param data
     * @return the computed crc8.
     */
    public static byte crc8(byte[] data) {
        int output = 0;
        for (int i = 0; i < data.length; i++) {
            int index = (output ^ data[i]) & 0xff;
            output = crc8_table[index];
        }
        return (byte) (output & 0xff);
    }

    /**
     * Returns a byte[] collection from a single byte
     * 
     * @param x
     * @return byteToBytes
     */
    public static byte[] byteToBytes(byte x) {
        byte[] out = new byte[1];
        out[0] = x;
        return out;
    }

    /**
     * Concatenates two byte arrays
     * 
     * @param a
     * @param b
     * @return byteConcat
     */
    public static byte[] byteConcat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        for (int i = 0; i < a.length; i++) {
            c[i] = a[i];
        }
        for (int i = 0; i < b.length; i++) {
            c[i + a.length] = b[i];
        }
        return c;
    }

    /**
     * @param a
     * @param b
     * @return byteConcat
     */
    public static byte[] byteConcat(byte[] a, byte b) {
        byte[] c = new byte[a.length + 1];
        for (int i = 0; i < a.length; i++) {
            c[i] = a[i];
        }
        c[a.length] = b;
        return c;
    }

    /**
     * @param a
     * @param b
     * @return byteConcat
     */
    public static byte[] byteConcat(byte a, byte[] b) {
        byte[] c = new byte[b.length + 1];
        for (int i = 0; i < b.length; i++) {
            c[1 + i] = b[i];
        }
        c[0] = a;
        return c;
    }

    /**
     * @param a
     * @param b
     * @return byteConcat
     */
    public static byte[] byteConcat(byte a, byte b) {
        byte[] c = new byte[2];
        c[0] = a;
        c[1] = b;
        return c;
    }

    /**
     * Concatenates a byte array and an int, up to max bytes from the right
     * 
     * @param a
     * @param x
     * @param max
     * @return intConcat
     */
    public static byte[] intConcat(byte[] a, int x, int max) {
        byte[] c = new byte[a.length + max];
        for (int i = 0; i < max; i++) {
            byte b = (byte) ((x >> (8 * i)) & 0xff);
            c[a.length + max - i - 1] = b;
        }
        return c;
    }

    // Collection of helper functions --> Pay attention to ENDIANNESS

    /**
     * @param x
     * @return intTo1Byte
     */
    public static byte[] intTo1Byte(int x) {
        byte[] out = new byte[1];
        out[0] = (byte) (x & 0xff);
        return out;
    }

    /**
     * @param x
     * @return intTo2Bytes
     */
    public static byte[] intTo2Bytes(int x) {
        byte[] out = new byte[2];
        out[1] = (byte) (x & 0xff);
        out[0] = (byte) ((x >> 8) & 0xff);
        return out;
    }

    /**
     * @param x
     * @return intTo4Bytes
     */
    public static byte[] intTo4Bytes(int x) {
        byte[] out = new byte[4];
        out[3] = (byte) (x & 0xff);
        out[2] = (byte) ((x >> 8) & 0xff);
        out[1] = (byte) ((x >> 16) & 0xff);
        out[0] = (byte) ((x >> 24) & 0xff);
        return out;
    }

    private static final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
            'f' };

    /**
     * @param bytes
     * @return bytesToHexString
     */
    public static String bytesToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * @param b
     * @return byteToHexString
     */
    public static String byteToHexString(byte b) {
        char[] hexChar = new char[2];
        int v = b & 0xFF;
        hexChar[0] = hexArray[v >>> 4];
        hexChar[1] = hexArray[v & 0x0F];
        return new String(hexChar);
    }

    /**
     * @param bytes
     * @param offset
     * @param len
     * @return bytes2intLE
     */
    public static int bytes2intLE(byte[] bytes, int offset, int len) {
        int sum = 0;
        for (int i = 0; i < len; i++) {
            byte b = bytes[offset + len - i - 1];
            sum = sum + ((b & 0xff) << (i * 8));
        }
        return sum;
    }

    /**
     * @param bytes
     * @param offset
     * @param len
     * @return byteRange
     */
    public static byte[] byteRange(byte[] bytes, int offset, int len) {
        byte[] out = new byte[len];
        for (int i = 0; i < len; i++) {
            out[i] = bytes[offset + i];
        }
        return out;
    }

    /**
     * @param orig
     * @param len
     * @return padUpTo
     */
    public static byte[] padUpTo(byte[] orig, int len) {
        byte[] padded = new byte[len];
        if (orig.length < len) {
            for (int i = 0; i < orig.length; i++) {
                padded[i] = orig[i];
            }
        }
        return padded;
    }
}
