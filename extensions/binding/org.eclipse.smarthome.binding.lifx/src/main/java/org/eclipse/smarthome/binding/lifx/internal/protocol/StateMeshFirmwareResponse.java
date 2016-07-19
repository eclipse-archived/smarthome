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
import org.eclipse.smarthome.binding.lifx.internal.fields.StringField;
import org.eclipse.smarthome.binding.lifx.internal.fields.UInt32Field;
import org.eclipse.smarthome.binding.lifx.internal.fields.UInt8Field;

/**
 * @author Tim Buckley - Initial Contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 */
public class StateMeshFirmwareResponse extends Packet {

    public static final int TYPE = 0x0F;

    public static final Field<Integer> FIELD_BUILD_SECOND = new UInt8Field();
    public static final Field<Integer> FIELD_BUILD_MINUTE = new UInt8Field();
    public static final Field<Integer> FIELD_BUILD_HOUR = new UInt8Field();
    public static final Field<Integer> FIELD_BUILD_DAY = new UInt8Field();
    public static final Field<String> FIELD_BUILD_MONTH = new LittleField<>(new StringField(3));
    public static final Field<Integer> FIELD_BUILD_YEAR = new UInt8Field();
    public static final Field<Integer> FIELD_INSTALL_SECOND = new UInt8Field();
    public static final Field<Integer> FIELD_INSTALL_MINUTE = new UInt8Field();
    public static final Field<Integer> FIELD_INSTALL_HOUR = new UInt8Field();
    public static final Field<Integer> FIELD_INSTALL_DAY = new UInt8Field();
    public static final Field<String> FIELD_INSTALL_MONTH = new LittleField<>(new StringField(3));
    public static final Field<Integer> FIELD_INSTALL_YEAR = new UInt8Field();
    public static final Field<Long> FIELD_VERSION = new LittleField<>(new UInt32Field());

    private int buildSecond;
    private int buildMinute;
    private int buildHour;
    private int buildDay;
    private String buildMonth;
    private int buildYear;

    private int installSecond;
    private int installMinute;
    private int installHour;
    private int installDay;
    private String installMonth;
    private int installYear;

    public static int getType() {
        return TYPE;
    }

    public int getBuildSecond() {
        return buildSecond;
    }

    public int getBuildMinute() {
        return buildMinute;
    }

    public int getBuildHour() {
        return buildHour;
    }

    public int getBuildDay() {
        return buildDay;
    }

    public String getBuildMonth() {
        return buildMonth;
    }

    public int getBuildYear() {
        return buildYear;
    }

    public int getInstallSecond() {
        return installSecond;
    }

    public int getInstallMinute() {
        return installMinute;
    }

    public int getInstallHour() {
        return installHour;
    }

    public int getInstallDay() {
        return installDay;
    }

    public String getInstallMonth() {
        return installMonth;
    }

    public int getInstallYear() {
        return installYear;
    }

    @Override
    public int packetType() {
        return TYPE;
    }

    @Override
    protected int packetLength() {
        return 20;
    }

    @Override
    protected void parsePacket(ByteBuffer bytes) {
        buildSecond = FIELD_BUILD_SECOND.value(bytes);
        buildMinute = FIELD_BUILD_MINUTE.value(bytes);
        buildHour = FIELD_BUILD_HOUR.value(bytes);
        buildDay = FIELD_BUILD_DAY.value(bytes);
        buildMonth = FIELD_BUILD_MONTH.value(bytes);
        buildYear = FIELD_BUILD_YEAR.value(bytes);

        installSecond = FIELD_INSTALL_SECOND.value(bytes);
        installMinute = FIELD_INSTALL_MINUTE.value(bytes);
        installHour = FIELD_INSTALL_HOUR.value(bytes);
        installDay = FIELD_INSTALL_DAY.value(bytes);
        installMonth = FIELD_INSTALL_MONTH.value(bytes);
        installYear = FIELD_INSTALL_YEAR.value(bytes);
    }

    @Override
    protected ByteBuffer packetBytes() {
        return ByteBuffer.allocate(packetLength()).put(FIELD_BUILD_SECOND.bytes(buildSecond))
                .put(FIELD_BUILD_MINUTE.bytes(buildMinute)).put(FIELD_BUILD_HOUR.bytes(buildHour))
                .put(FIELD_BUILD_DAY.bytes(buildDay)).put(FIELD_BUILD_MONTH.bytes(buildMonth))
                .put(FIELD_BUILD_YEAR.bytes(buildYear)).put(FIELD_INSTALL_SECOND.bytes(installSecond))
                .put(FIELD_INSTALL_MINUTE.bytes(installMinute)).put(FIELD_INSTALL_HOUR.bytes(installHour))
                .put(FIELD_INSTALL_DAY.bytes(installDay)).put(FIELD_INSTALL_MONTH.bytes(installMonth))
                .put(FIELD_INSTALL_YEAR.bytes(installYear));

    }

    @Override
    public int[] expectedResponses() {
        return new int[] {};
    }

}
