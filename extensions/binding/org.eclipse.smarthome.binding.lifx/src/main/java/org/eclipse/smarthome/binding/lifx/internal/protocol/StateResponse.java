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
import org.eclipse.smarthome.binding.lifx.internal.fields.StringField;
import org.eclipse.smarthome.binding.lifx.internal.fields.UInt16Field;
import org.eclipse.smarthome.binding.lifx.internal.fields.UInt64Field;

/**
 * @author Tim Buckley - Initial Contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 */
public class StateResponse extends Packet {

    public static final int TYPE = 0x6B;

    public static final Field<Integer> FIELD_HUE = new UInt16Field().little();
    public static final Field<Integer> FIELD_SATURATION = new UInt16Field().little();
    public static final Field<Integer> FIELD_BRIGHTNESS = new UInt16Field().little();
    public static final Field<Integer> FIELD_KELVIN = new UInt16Field().little();
    public static final Field<Integer> FIELD_DIM = new UInt16Field().little();
    public static final Field<Integer> FIELD_POWER = new UInt16Field();
    public static final Field<String> FIELD_LABEL = new StringField(32);
    public static final Field<Long> FIELD_TAGS = new UInt64Field();

    private int hue;
    private int saturation;
    private int brightness;
    private int kelvin;
    private int dim;
    private PowerState power; // PowerState?
    private String label;
    private long tags;

    @Override
    public String toString() {
        return "hue=" + hue + ", saturation=" + saturation + ", brightness=" + brightness + ", kelvin=" + kelvin
                + ", dim=" + dim + ", power=" + power + ", label=" + label;
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

    public int getDim() {
        return dim;
    }

    public PowerState getPower() {
        return power;
    }

    public String getLabel() {
        return label;
    }

    public long getTags() {
        return tags;
    }

    @Override
    public int packetType() {
        return TYPE;
    }

    @Override
    protected int packetLength() {
        return 52;
    }

    @Override
    protected void parsePacket(ByteBuffer bytes) {
        hue = FIELD_HUE.value(bytes);
        saturation = FIELD_SATURATION.value(bytes);
        brightness = FIELD_BRIGHTNESS.value(bytes);
        kelvin = FIELD_KELVIN.value(bytes);
        dim = FIELD_DIM.value(bytes);
        power = PowerState.fromValue(FIELD_POWER.value(bytes));
        label = FIELD_LABEL.value(bytes);
        tags = FIELD_TAGS.value(bytes);
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(packetLength()).put(FIELD_HUE.bytes(hue)).put(FIELD_SATURATION.bytes(saturation))
                .put(FIELD_BRIGHTNESS.bytes(brightness)).put(FIELD_KELVIN.bytes(kelvin)).put(FIELD_DIM.bytes(dim))
                .put(FIELD_POWER.bytes(hue)).put(FIELD_LABEL.bytes(label)).put(FIELD_TAGS.bytes(tags));
    }

    @Override
    public int[] expectedResponses() {
        return new int[] {};
    }

}
