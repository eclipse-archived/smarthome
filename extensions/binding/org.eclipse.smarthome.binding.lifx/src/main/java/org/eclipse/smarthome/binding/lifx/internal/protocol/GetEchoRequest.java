/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal.protocol;

import java.nio.ByteBuffer;

import org.eclipse.smarthome.binding.lifx.internal.fields.ByteField;
import org.eclipse.smarthome.binding.lifx.internal.fields.Field;

/**
 * @author Tim Buckley - Initial Contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 */
public class GetEchoRequest extends Packet {

    public static final int TYPE = 0x3A;

    public static final Field<ByteBuffer> FIELD_PAYLOAD = new ByteField(64);

    private ByteBuffer payload;

    public GetEchoRequest() {
        setTagged(false);
        setAddressable(true);
        setResponseRequired(true);
    }

    public ByteBuffer getPayload() {
        return payload;
    }

    public void setPayload(ByteBuffer payload) {
        this.payload = payload;
    }

    @Override
    public int packetType() {
        return TYPE;
    }

    @Override
    protected int packetLength() {
        return 64;
    }

    @Override
    protected void parsePacket(ByteBuffer bytes) {
        // do nothing
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(packetLength()).put(FIELD_PAYLOAD.bytes(payload));
    }

    @Override
    public int[] expectedResponses() {
        return new int[] { EchoRequestResponse.TYPE };
    }

    public static GetEchoRequest currentTimeEchoRequest() {
        ByteBuffer payload = ByteBuffer.allocate(Long.SIZE / 8);
        payload.putLong(System.currentTimeMillis());

        GetEchoRequest request = new GetEchoRequest();
        request.setResponseRequired(true);
        request.setPayload(payload);
        return request;
    }

}
