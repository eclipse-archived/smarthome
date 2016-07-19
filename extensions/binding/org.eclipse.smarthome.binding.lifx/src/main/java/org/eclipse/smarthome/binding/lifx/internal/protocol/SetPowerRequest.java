/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal.protocol;

import java.nio.ByteBuffer;

import org.eclipse.smarthome.binding.lifx.internal.fields.Field;
import org.eclipse.smarthome.binding.lifx.internal.fields.UInt16Field;

/**
 * @author Tim Buckley - Initial Contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 */
public class SetPowerRequest extends Packet {

    public static final int TYPE = 0x15;

    public static final Field<Integer> FIELD_STATE = new UInt16Field();

    private PowerState state;

    public PowerState getState() {
        return state;
    }

    public SetPowerRequest() {
        state = PowerState.OFF;
        setTagged(false);
        setAddressable(true);
        setResponseRequired(true);
        // protocol = 0x1400;
    }

    public SetPowerRequest(PowerState state) {
        this.state = state;
        setTagged(false);
        setAddressable(true);
        setResponseRequired(true);
        // protocol = 0x1400;
    }

    @Override
    public int packetType() {
        return TYPE;
    }

    @Override
    protected int packetLength() {
        return 2;
    }

    @Override
    protected void parsePacket(ByteBuffer bytes) {
        state = PowerState.fromValue(FIELD_STATE.value(bytes));
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(2).put(FIELD_STATE.bytes(state.getValue()));
    }

    @Override
    public int[] expectedResponses() {
        return new int[] { StatePowerResponse.TYPE };
    }

}
