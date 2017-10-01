/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
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
import org.eclipse.smarthome.binding.lifx.internal.fields.FloatField;
import org.eclipse.smarthome.binding.lifx.internal.fields.HSBK;
import org.eclipse.smarthome.binding.lifx.internal.fields.HSBKField;
import org.eclipse.smarthome.binding.lifx.internal.fields.LittleField;
import org.eclipse.smarthome.binding.lifx.internal.fields.UInt16Field;
import org.eclipse.smarthome.binding.lifx.internal.fields.UInt32Field;
import org.eclipse.smarthome.binding.lifx.internal.fields.UInt8Field;

/**
 * @author Wouter Born - Add light effects
 */
public class SetWaveformRequest extends Packet {

    public static final int TYPE = 0x67;

    public static final Field<Integer> FIELD_RESERVED = new UInt8Field();
    public static final Field<Integer> FIELD_TRANSIENT = new UInt8Field();
    public static final HSBKField FIELD_COLOR = new HSBKField();
    public static final Field<Long> FIELD_PERIOD = new LittleField<>(new UInt32Field());
    public static final Field<Float> FIELD_CYCLES = new FloatField().little();
    public static final Field<Integer> FIELD_SKEW_RATIO = new UInt16Field().little();
    public static final Field<Integer> FIELD_WAVEFORM = new UInt8Field();

    private int reserved;
    private int tran; // Named tran because transient is a Java keyword
    private HSBK color;
    private long period;
    private float cycles;
    private int skewRatio;
    private Waveform waveform;

    public SetWaveformRequest() {
        setTagged(false);
        setAddressable(true);
        setResponseRequired(true);
    }

    public SetWaveformRequest(boolean tran, HSBK color, long period, float cycles, int skewRatio, Waveform waveform) {
        this();

        this.tran = tran ? 1 : 0;
        this.color = color;
        this.period = period;
        this.cycles = cycles;
        this.skewRatio = skewRatio;
        this.waveform = waveform;
    }

    @Override
    public int packetType() {
        return TYPE;
    }

    @Override
    protected int packetLength() {
        return 21;
    }

    @Override
    protected void parsePacket(ByteBuffer bytes) {
        reserved = FIELD_RESERVED.value(bytes);
        tran = FIELD_TRANSIENT.value(bytes);
        color = FIELD_COLOR.value(bytes);
        period = FIELD_PERIOD.value(bytes);
        cycles = FIELD_CYCLES.value(bytes);
        skewRatio = FIELD_SKEW_RATIO.value(bytes);
        waveform = Waveform.fromValue(FIELD_WAVEFORM.value(bytes));
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(packetLength()).put(FIELD_RESERVED.bytes(reserved)).put(FIELD_TRANSIENT.bytes(tran))
                .put(FIELD_COLOR.bytes(color)).put(FIELD_PERIOD.bytes(period)).put(FIELD_CYCLES.bytes(cycles))
                .put(FIELD_SKEW_RATIO.bytes(skewRatio)).put(FIELD_WAVEFORM.bytes(waveform.getValue()));
    }

    @Override
    public int[] expectedResponses() {
        return new int[] { StateResponse.TYPE };
    }

}
