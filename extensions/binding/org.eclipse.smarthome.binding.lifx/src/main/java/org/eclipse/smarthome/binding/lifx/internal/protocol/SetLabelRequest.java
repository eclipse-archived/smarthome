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
import org.eclipse.smarthome.binding.lifx.internal.fields.StringField;

/**
 * @author Tim Buckley - Initial Contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 */
public class SetLabelRequest extends Packet {

    public static final int TYPE = 0x18;

    public static final Field<String> FIELD_LABEL = new StringField(32).utf8();

    private String label;

    public SetLabelRequest() {
        setTagged(false);
        setAddressable(true);
        setAckRequired(true);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public int packetType() {
        return TYPE;
    }

    @Override
    protected int packetLength() {
        return 32;
    }

    @Override
    protected void parsePacket(ByteBuffer bytes) {
        label = FIELD_LABEL.value(bytes);
    }

    @Override
    protected ByteBuffer packetBytes() {
        return FIELD_LABEL.bytes(label);
    }

    @Override
    public int[] expectedResponses() {
        return new int[] {};
    }

}
