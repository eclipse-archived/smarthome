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

import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Static utility methods that are helpful when dealing with hex data and byte arrays.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@NonNullByDefault
public class HexUtils {

    // used for hex conversions
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    // private constructor as we only have static methods
    private HexUtils() {
    }

    /**
     * Converts a byte array into a hex string with a given delimiter.
     * Example: Delimiter "-" results in Strings like "01-23-45".
     *
     * @param bytes the byte array
     * @param delimiter a delimiter that is placed between every two bytes
     * @return the corresponding hex string
     */
    public static String bytesToHex(byte[] bytes, @Nullable CharSequence delimiter) {
        return Arrays.stream(toObjects(bytes)).map(b -> {
            int v = b & 0xFF;
            return "" + hexArray[v >>> 4] + hexArray[v & 0x0F];
        }).collect(Collectors.joining(delimiter != null ? delimiter : ""));
    }

    /**
     * Converts a byte array into a hex string (in format "0123456789ABCDEF").
     *
     * @param bytes the byte array
     * @return the corresponding hex string
     */
    public static String bytesToHex(byte[] bytes) {
        return bytesToHex(bytes, null);
    }

    private static Byte[] toObjects(byte[] bytes) {
        Byte[] bytesObjects = new Byte[bytes.length];
        Arrays.setAll(bytesObjects, n -> bytes[n]);
        return bytesObjects;
    }
}
