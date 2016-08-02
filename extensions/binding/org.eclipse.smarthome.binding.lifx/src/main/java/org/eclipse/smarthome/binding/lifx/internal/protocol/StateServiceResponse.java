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
import org.eclipse.smarthome.binding.lifx.internal.fields.LittleField;
import org.eclipse.smarthome.binding.lifx.internal.fields.UInt32Field;
import org.eclipse.smarthome.binding.lifx.internal.fields.UInt8Field;

/**
 * @author Tim Buckley - Initial Contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 */
public class StateServiceResponse extends Packet {

    public static final int TYPE = 0x03;

    public static final Field<Integer> FIELD_SERVICE = new UInt8Field();
    public static final Field<Long> FIELD_PORT = new LittleField<>(new UInt32Field());

    private int service;
    private long port;

    public int getService() {
        return service;
    }

    public long getPort() {
        return port;
    }

    public StateServiceResponse() {
    }

    @Override
    protected void parsePacket(ByteBuffer bytes) {
        service = FIELD_SERVICE.value(bytes);
        port = FIELD_PORT.value(bytes);
    }

    @Override
    public int packetType() {
        return TYPE;
    }

    @Override
    protected int packetLength() {
        return 5;
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(packetLength()).put(FIELD_SERVICE.bytes(service)).put(FIELD_PORT.bytes(port));
    }

    @Override
    public int[] expectedResponses() {
        return new int[] {};
    }

}
