/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.eir;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Defines an EIR record used in the BLE advertisement packets.
 *
 * @author Chris Jackson
 *
 */
public class EirRecord {
    private EirDataType type;
    private Object record;

    EirRecord(int[] data) {
        if (data == null || data.length == 0) {
            return;
        }

        type = EirDataType.getEirPacketType(data[0]);
        switch (type) {
            case EIR_FLAGS:
                record = processFlags(data);
                break;
            case EIR_MANUFACTURER_SPECIFIC:
                record = processManufacturer(data);
                break;
            case EIR_SVC_UUID16_COMPLETE:
            case EIR_SVC_UUID16_INCOMPLETE:
                record = processUuid16(data);
                break;
            case EIR_SVC_UUID32_COMPLETE:
            case EIR_SVC_UUID32_INCOMPLETE:
                record = processUuid32(data);
                break;
            case EIR_SVC_UUID128_COMPLETE:
            case EIR_SVC_UUID128_INCOMPLETE:
                record = processUuid128(data);
                break;
            case EIR_NAME_LONG:
            case EIR_NAME_SHORT:
                record = processString(data);
                break;
            case EIR_TXPOWER:
                record = processUInt8(data);
                break;
            case EIR_SLAVEINTERVALRANGE:
                record = processUInt16List(data);
                break;
            case EIR_DEVICE_CLASS:
                record = processUInt8(data);
                break;
            default:
                record = processUnknown(data);
                break;
        }
    }

    private byte[] processManufacturer(int[] data) {
        // we have to drop the first byte/int
        byte[] manufacturerData = new byte[data.length - 1];
        for (int i = 1; i < data.length; i++) {
            manufacturerData[i - 1] = (byte) data[i];
        }
        return manufacturerData;
    }

    private List<UUID> processUuid16(int[] data) {
        List<UUID> uuidList = new ArrayList<UUID>();

        for (int cnt = 1; cnt < data.length - 1; cnt += 2) {
            long high = ((long) data[cnt] << 32) + ((long) data[cnt + 1] << 40);
            uuidList.add(new UUID(high, 0));
        }

        return uuidList;
    }

    private List<UUID> processUuid32(int[] data) {
        List<UUID> uuidList = new ArrayList<UUID>();

        for (int cnt = 1; cnt < data.length - 1; cnt += 4) {
            long high = ((long) data[cnt++] << 32) + ((long) data[cnt++] << 40) + ((long) data[cnt++] << 48)
                    + ((long) data[cnt++] << 56);
            uuidList.add(new UUID(high, 0));
        }

        return uuidList;
    }

    private List<UUID> processUuid128(int[] data) {
        List<UUID> uuidList = new ArrayList<UUID>();

        for (int cnt = 1; cnt < data.length - 1; cnt += 16) {
            long low = (data[cnt++]) + ((long) data[cnt++] << 8) + ((long) data[cnt++] << 16)
                    + ((long) data[cnt++] << 24) + ((long) data[cnt++] << 32) + ((long) data[cnt++] << 40)
                    + ((long) data[cnt++] << 48) + ((long) data[cnt++] << 56);
            long high = (data[cnt++]) + ((long) data[cnt++] << 8) + ((long) data[cnt++] << 16)
                    + ((long) data[cnt++] << 24) + ((long) data[cnt++] << 32) + ((long) data[cnt++] << 40)
                    + ((long) data[cnt++] << 48) + ((long) data[cnt++] << 56);

            uuidList.add(new UUID(high, low));
        }

        return uuidList;
    }

    private List<Integer> processUInt16List(int[] data) {
        List<Integer> intList = new ArrayList<Integer>();

        for (int cnt = 1; cnt < data.length - 1; cnt += 2) {
            intList.add(Integer.valueOf(data[cnt] + (data[cnt + 1] << 8)));
        }

        return intList;
    }

    private List<EirFlags> processFlags(int[] data) {
        List<EirFlags> flags = new ArrayList<EirFlags>();
        int flagBit = 0;
        for (int cnt = 1; cnt < data.length; cnt++) {
            for (int bitcnt = 0; bitcnt < 8; bitcnt++) {
                if ((data[cnt] & (1 << bitcnt)) != 0) {
                    flags.add(EirFlags.getEirFlag(flagBit));
                }
                flagBit++;
            }
        }

        return flags;
    }

    private String processString(int[] data) {
        StringBuilder builder = new StringBuilder();
        for (int cnt = 1; cnt < data.length; cnt++) {
            builder.append((char) data[cnt]);
        }
        return builder.toString();
    }

    private int processUInt8(int[] data) {
        if (data[1] > 127) {
            return data[1] - 256;
        } else {
            return data[1];
        }
    }

    private String processUnknown(int[] data) {
        StringBuilder builder = new StringBuilder();
        for (int cnt = 0; cnt < data.length; cnt++) {
            builder.append(String.format("%02X", data[cnt]));
        }
        return builder.toString();
    }

    public EirDataType getType() {
        return type;
    }

    public Object getRecord() {
        return record;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("EirRecord [type=");
        builder.append(type);
        builder.append(", record=");
        builder.append(record);
        builder.append(']');
        return builder.toString();
    }
}
