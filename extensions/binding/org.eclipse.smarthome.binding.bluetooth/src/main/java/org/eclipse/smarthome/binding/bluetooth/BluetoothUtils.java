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
package org.eclipse.smarthome.binding.bluetooth;

/**
 * Static utility methods that are helpful when dealing with Bluetooth data.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class BluetoothUtils {

    // used for hex conversions
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    // private constructor as we only have static methods
    private BluetoothUtils() {
    }

    /**
     * Converts a byte array into a hex string (in format "01 23 45 67 89 0A BC DE").
     *
     * @param bytes the byte array
     * @return the corresponding hex string
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 3];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';
        }
        return new String(hexChars);
    }
}
