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
import org.eclipse.smarthome.binding.lifx.internal.fields.UInt64Field;

/**
 * @author Tim Buckley - Initial Contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 */
public class StateInfoResponse extends Packet {

    public static final int TYPE = 0x23;

    public static final Field<Long> FIELD_TIME = new UInt64Field().little();
    public static final Field<Long> FIELD_UPTIME = new UInt64Field().little();
    public static final Field<Long> FIELD_DOWNTIME = new UInt64Field().little();

    private long time;
    private long uptime;
    private long downtime;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getUptime() {
        return uptime;
    }

    public void setUptime(long uptime) {
        this.uptime = uptime;
    }

    public long getDowntime() {
        return downtime;
    }

    public void setDowntime(long downtime) {
        this.downtime = downtime;
    }

    public StateInfoResponse() {
        setTagged(false);
        setAddressable(true);
        setResponseRequired(true);
    }

    @Override
    public int packetType() {
        return TYPE;
    }

    @Override
    protected int packetLength() {
        return 24;
    }

    @Override
    protected void parsePacket(ByteBuffer bytes) {
        time = FIELD_TIME.value(bytes);
        uptime = FIELD_UPTIME.value(bytes);
        downtime = FIELD_DOWNTIME.value(bytes);
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(packetLength()).put(FIELD_TIME.bytes(time)).put(FIELD_UPTIME.bytes(uptime))
                .put(FIELD_DOWNTIME.bytes(downtime));
    }

    @Override
    public int[] expectedResponses() {
        return new int[] {};
    }

}
