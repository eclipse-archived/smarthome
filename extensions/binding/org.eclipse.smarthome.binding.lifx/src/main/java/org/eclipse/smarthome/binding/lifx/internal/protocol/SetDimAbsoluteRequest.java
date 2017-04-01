/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal.protocol;

import java.nio.ByteBuffer;

import org.eclipse.smarthome.binding.lifx.internal.fields.Field;
import org.eclipse.smarthome.binding.lifx.internal.fields.UInt16Field;
import org.eclipse.smarthome.binding.lifx.internal.fields.UInt32Field;

/**
 * @author Tim Buckley - Initial Contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 */
public class SetDimAbsoluteRequest extends Packet {

    public static final int TYPE = 0x68;

    public static final Field<Integer> FIELD_DIM = new UInt16Field().little();
    public static final Field<Long> FIELD_DURATION = new UInt32Field().little();

    private int dim;
    private long duration;

    public int getDim() {
        return dim;
    }

    public long getDuration() {
        return duration;
    }

    public SetDimAbsoluteRequest() {

    }

    public SetDimAbsoluteRequest(int dim, long duration) {
        this.dim = dim;
        this.duration = duration;
    }

    @Override
    public int packetType() {
        return TYPE;
    }

    @Override
    protected int packetLength() {
        return 6;
    }

    @Override
    protected void parsePacket(ByteBuffer bytes) {
        dim = FIELD_DIM.value(bytes);
        duration = FIELD_DURATION.value(bytes);
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(packetLength()).put(FIELD_DIM.bytes(dim)).put(FIELD_DURATION.bytes(duration));
    }

    @Override
    public int[] expectedResponses() {
        return new int[] {};
    }

}
