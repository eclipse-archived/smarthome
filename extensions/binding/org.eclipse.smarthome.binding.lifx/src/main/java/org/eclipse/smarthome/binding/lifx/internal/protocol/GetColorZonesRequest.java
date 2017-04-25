/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal.protocol;

import static org.eclipse.smarthome.binding.lifx.LifxBindingConstants.*;

import java.nio.ByteBuffer;

import org.eclipse.smarthome.binding.lifx.internal.fields.Field;
import org.eclipse.smarthome.binding.lifx.internal.fields.UInt8Field;

/**
 * @author Wouter Born - Add support for MultiZone light control
 */
public class GetColorZonesRequest extends Packet {

    public static final int TYPE = 0x1F6;

    public static final Field<Integer> FIELD_START_INDEX = new UInt8Field();
    public static final Field<Integer> FIELD_END_INDEX = new UInt8Field();

    private int startIndex = MIN_ZONE_INDEX;
    private int endIndex = MAX_ZONE_INDEX;

    public GetColorZonesRequest() {
        setTagged(false);
        setAddressable(true);
        setResponseRequired(true);
    }

    public GetColorZonesRequest(int index) {
        this(index, index);
    }

    public GetColorZonesRequest(int startIndex, int endIndex) {
        this();
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
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
        startIndex = FIELD_START_INDEX.value(bytes);
        endIndex = FIELD_END_INDEX.value(bytes);
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(packetLength()).put(FIELD_START_INDEX.bytes(startIndex))
                .put(FIELD_END_INDEX.bytes(endIndex));
    }

    @Override
    public int[] expectedResponses() {
        return new int[] { StateMultiZoneResponse.TYPE, StateZoneResponse.TYPE };
    }

}
