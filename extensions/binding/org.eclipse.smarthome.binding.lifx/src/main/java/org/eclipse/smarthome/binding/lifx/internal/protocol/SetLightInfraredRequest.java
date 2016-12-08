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
 * @author Wouter Born - Support LIFX 2016 product line-up and infrared functionality
 */
public class SetLightInfraredRequest extends Packet {

    public static final int TYPE = 0x7A;

    public static final Field<Integer> FIELD_STATE = new UInt16Field();

    private int infrared;

    public int getInfrared() {
        return infrared;
    }

    public SetLightInfraredRequest() {
        setTagged(false);
        setAddressable(true);
        setResponseRequired(true);
    }

    public SetLightInfraredRequest(int infrared) {
        this();
        this.infrared = infrared;
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
        infrared = FIELD_STATE.value(bytes);
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(2).put(FIELD_STATE.bytes(infrared));
    }

    @Override
    public int[] expectedResponses() {
        return new int[] { StateLightInfraredResponse.TYPE };
    }

}
