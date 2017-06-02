/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.ble;

/**
 * The {@link BluetoothAddress} class defines a bluetooth address
 *
 * @author Chris Jackson - Initial contribution
 */
public class BluetoothAddress {
    public static final int BD_ADDRESS_LENGTH = 17;

    private final String address;

    public BluetoothAddress(String address) {
        if (address == null || address.length() != BD_ADDRESS_LENGTH) {
            throw new IllegalArgumentException("BT Address can not be null or zero length");
        }
        for (int i = 0; i < BD_ADDRESS_LENGTH; i++) {
            char c = address.charAt(i);

            // Check address - 2 bytes should be hex, and then a colon
            switch (i % 3) {
                case 0:
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
