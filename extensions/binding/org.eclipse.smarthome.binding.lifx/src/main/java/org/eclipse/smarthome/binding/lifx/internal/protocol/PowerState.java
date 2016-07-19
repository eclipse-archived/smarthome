/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal.protocol;

/**
 * Represents bulb power states (on or off).
 *
 * @author Tim Buckley - Initial Contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 */
public enum PowerState {

    ON(0xFFFF),
    OFF(0x0000);

    private final int value;

    private PowerState(int value) {
        this.value = value;
    }

    /**
     * Gets the integer value of this power state.
     *
     * @return the integer value
     */
    public int getValue() {
        return value;
    }

    public static PowerState fromValue(int value) {
        for (PowerState p : values()) {
            if (p.getValue() == value) {
                return p;
            }
        }

        return null;
    }

}
