/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal.protocol;

import org.eclipse.smarthome.core.library.types.OnOffType;

/**
 * Represents light power states (on or off).
 *
 * @author Tim Buckley - Initial Contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 * @author Wouter Born - Added OnOffType conversion methods
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
        // a response can have a power level between 0 and 65535 when the light
        // has just been switched ON or OFF
        return value == OFF.value ? OFF : ON;
    }

    public static PowerState fromOnOffType(OnOffType onOff) {
        return onOff == OnOffType.ON ? ON : OFF;
    }

    public OnOffType toOnOffType() {
        return this == ON ? OnOffType.ON : OnOffType.OFF;
    }

}
