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
import org.eclipse.smarthome.binding.lifx.internal.fields.StringField;
import org.eclipse.smarthome.binding.lifx.internal.fields.UInt64Field;

/**
 * @author Tim Buckley - Initial Contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 */
public class StateLocationResponse extends Packet {

    public static final int TYPE = 0x32;

    public static final Field<ByteBuffer> FIELD_LOCATION = new ByteField(16);
    public static final Field<String> FIELD_LABEL = new StringField(32).utf8();
    public static final Field<Long> FIELD_UPDATED_AT = new UInt64Field().little();

    private ByteBuffer location;
    private String label;
    private long updated_at;

    public static int getType() {
        return TYPE;
    }

    public ByteBuffer getLocation() {
        return location;
    }

    public void setLocation(ByteBuffer location) {
        this.location = location;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getUpdatedAt() {
        return updated_at;
    }

    public void setUpdatedAt(long updated_at) {
        this.updated_at = updated_at;
    }

    @Override
    public int packetType() {
        return TYPE;
    }

    @Override
    protected int packetLength() {
        return 14;
    }

    @Override
    protected void parsePacket(ByteBuffer bytes) {
        location = FIELD_LOCATION.value(bytes);
        label = FIELD_LABEL.value(bytes);
        updated_at = FIELD_UPDATED_AT.value(bytes);
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(packetLength()).put(FIELD_LOCATION.bytes(location)).put(FIELD_LABEL.bytes(label))
                .put(FIELD_UPDATED_AT.bytes(updated_at));
    }

    @Override
    public int[] expectedResponses() {
        return new int[] {};
    }

}
