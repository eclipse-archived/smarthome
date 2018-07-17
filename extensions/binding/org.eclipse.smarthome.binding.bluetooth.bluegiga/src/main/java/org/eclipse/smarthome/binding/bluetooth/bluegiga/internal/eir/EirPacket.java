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
package org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.eir;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This class processes the Extended Inquiry Response data used in the BLE advertisement frame
 * 
 * @author Chris Jackson
 *
 */
public class EirPacket {
    private Map<EirDataType, Object> records = new HashMap<EirDataType, Object>();

    public EirPacket(int[] data) {
        if (data == null || data.length == 0) {
            return;
        }

        for (int cnt = 0; cnt < data.length;) {
            if (data[cnt] == 0) {
                break;
            }

            int[] rawRecord = Arrays.copyOfRange(data, cnt + 1, cnt + data[cnt] + 1);
            EirRecord record = new EirRecord(rawRecord);

            cnt += data[cnt] + 1;

            records.put(record.getType(), record.getRecord());
        }
    }

    /**
     * Returns a map of all records decoded in the packet
     * 
     * @return {@link Map} of {@link EirDataType} to {@link Object}
     */
    public Map<EirDataType, Object> getRecords() {
        return records;
    }

    /**
     * Returns the specified record decoded in the packet or null if the record is not found
     * 
     * @param recordType the requested {@link EirDataType}
     * @return {@link Map} of to {@link Object}
     */
    public Object getRecord(EirDataType recordType) {
        return records.get(recordType);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("EirPacket [records=");
        builder.append(records);
        builder.append(']');
        return builder.toString();
    }
}
