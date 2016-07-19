/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal.protocol;

import java.nio.ByteBuffer;

import org.eclipse.smarthome.binding.lifx.internal.fields.ByteField;
import org.eclipse.smarthome.binding.lifx.internal.fields.Field;
import org.eclipse.smarthome.binding.lifx.internal.fields.UInt16Field;
import org.eclipse.smarthome.binding.lifx.internal.fields.UInt32Field;

/**
 * @author Tim Buckley - Initial Contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 */
public class SetColorRequest extends Packet {

    public static final int TYPE = 0x66;

    public static final Field<ByteBuffer> FIELD_STREAM = new ByteField(1);
    public static final Field<Integer> FIELD_HUE = new UInt16Field().little();
    public static final Field<Integer> FIELD_SATURATION = new UInt16Field().little();
    public static final Field<Integer> FIELD_BRIGHTNESS = new UInt16Field().little();
    public static final Field<Integer> FIELD_KELVIN = new UInt16Field().little();
    public static final Field<Long> FIELD_FADE_TIME = new UInt32Field().little();

    private ByteBuffer stream;

    private int hue;
    private int saturation;
    private int brightness;
    private int kelvin;
    private long fadeTime;

    public ByteBuffer getStream() {
        return stream;
    }

    public int getHue() {
        return hue;
    }

    public int getSaturation() {
        return saturation;
    }

    public int getBrightness() {
        return brightness;
    }

    public int getKelvin() {
        return kelvin;
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

    public SetColorRequest(int hue, int saturation, int brightness, int kelvin, long fadeTime) {
        this();

        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
        this.kelvin = kelvin;
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
        hue = FIELD_HUE.value(bytes);
        saturation = FIELD_SATURATION.value(bytes);
        brightness = FIELD_BRIGHTNESS.value(bytes);
        kelvin = FIELD_KELVIN.value(bytes);
        fadeTime = FIELD_FADE_TIME.value(bytes);
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(packetLength()).put(FIELD_STREAM.bytes(stream)).put(FIELD_HUE.bytes(hue))
                .put(FIELD_SATURATION.bytes(saturation)).put(FIELD_BRIGHTNESS.bytes(brightness))
                .put(FIELD_KELVIN.bytes(kelvin)).put(FIELD_FADE_TIME.bytes(fadeTime));
    }

    @Override
    public int[] expectedResponses() {
        return new int[] { StateResponse.TYPE };
    }

}
