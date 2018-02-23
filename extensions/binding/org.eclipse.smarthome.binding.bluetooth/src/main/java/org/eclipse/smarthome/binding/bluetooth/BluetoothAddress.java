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
 * The {@link BluetoothAddress} class defines a bluetooth address
 *
 * @author Chris Jackson - Initial contribution
 */
public class BluetoothAddress {

    public static final int BD_ADDRESS_LENGTH = 17;

    private final String address;

    /**
     * The default constructor
     *
     * @param address the device address
     */
    public BluetoothAddress(String address) {
        if (address == null || address.length() != BD_ADDRESS_LENGTH) {
            throw new IllegalArgumentException("BT Address cannot be null and must be in format XX:XX:XX:XX:XX:XX");
        }
        for (int i = 0; i < BD_ADDRESS_LENGTH; i++) {
            char c = address.charAt(i);

            // Check address - 2 bytes should be hex, and then a colon
            switch (i % 3) {
                case 0: // fall through
                case 1:
                    if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F')) {
                        break;
                    }
                    throw new IllegalArgumentException("BT Address must contain upper case hex values only");
                case 2:
                    if (c == ':') {
                        break;
                    }
                    throw new IllegalArgumentException("BT Address bytes must be separated with colon");
            }
        }

        this.address = address;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BluetoothAddress other = (BluetoothAddress) obj;
        if (address == null) {
            if (other.address != null) {
                return false;
            }
        } else if (!address.equals(other.address)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return address;
    }
}
