/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.binding.lifx.internal.protocol;

import java.nio.ByteBuffer;

import org.eclipse.smarthome.binding.lifx.internal.fields.Field;
import org.eclipse.smarthome.binding.lifx.internal.fields.UInt16Field;

/**
 * @author Wouter Born - Support LIFX 2016 product line-up and infrared functionality
 */
public class StateLightInfraredResponse extends Packet {

    public static final int TYPE = 0x79;

    public static final Field<Integer> FIELD_STATE = new UInt16Field().little();

    private int infrared;

    public int getInfrared() {
        return infrared;
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
        return ByteBuffer.allocate(packetLength()).put(FIELD_STATE.bytes(infrared));
    }

    @Override
    public int[] expectedResponses() {
        return new int[] {};
    }

}
