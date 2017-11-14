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
import org.eclipse.smarthome.binding.lifx.internal.fields.HSBK;
import org.eclipse.smarthome.binding.lifx.internal.fields.HSBKField;
import org.eclipse.smarthome.binding.lifx.internal.fields.UInt32Field;

/**
 * @author Tim Buckley - Initial Contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 */
public class SetColorRequest extends Packet {

    public static final int TYPE = 0x66;

    public static final Field<ByteBuffer> FIELD_STREAM = new ByteField(1);
    public static final HSBKField FIELD_COLOR = new HSBKField();
    public static final Field<Long> FIELD_FADE_TIME = new UInt32Field().little();

    private ByteBuffer stream;

    private HSBK color;
    private long fadeTime;

    public ByteBuffer getStream() {
        return stream;
    }

    public HSBK getColor() {
        return color;
    }

    public long getFadeTime() {
        return fadeTime;
    }

    public SetColorRequest() {
        stream = ByteBuffer.allocate(1);
        setTagged(false);
        setAddressable(true);
        setResponseRequired(true);
    }

    public SetColorRequest(HSBK color, long fadeTime) {
        this();
        this.color = color;
        this.fadeTime = fadeTime;
    }

    @Override
    public int packetType() {
        return TYPE;
    }

    @Override
    protected int packetLength() {
        return 13;
    }

    @Override
    protected void parsePacket(ByteBuffer bytes) {
        stream = FIELD_STREAM.value(bytes);
        color = FIELD_COLOR.value(bytes);
        fadeTime = FIELD_FADE_TIME.value(bytes);
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(packetLength()).put(FIELD_STREAM.bytes(stream)).put(FIELD_COLOR.bytes(color))
                .put(FIELD_FADE_TIME.bytes(fadeTime));
    }

    @Override
    public int[] expectedResponses() {
        return new int[] { StateResponse.TYPE };
    }

}
