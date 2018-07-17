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

import java.util.HashMap;
import java.util.Map;

/**
 * Assigned numbers are used in GAP for inquiry response, EIR data type values, manufacturer-specific data, advertising
 * data, low energy UUIDs and appearance characteristics, and class of device.
 * 
 * https://www.bluetooth.com/specifications/assigned-numbers/generic-access-profile
 * 
 * @author Chris Jackson
 *
 */
public enum EirDataType {
    /**
     * Default unknown value
     */
    UNKNOWN(-1),
    NONE(0),

    EIR_FLAGS(0x01),
    EIR_SVC_UUID16_INCOMPLETE(0x02),
    EIR_SVC_UUID16_COMPLETE(0x03),
    EIR_SVC_UUID32_INCOMPLETE(0x04),
    EIR_SVC_UUID32_COMPLETE(0x05),
    EIR_SVC_UUID128_INCOMPLETE(0x06),
    EIR_SVC_UUID128_COMPLETE(0x07),
    EIR_NAME_SHORT(0x08),
    EIR_NAME_LONG(0x09),
    EIR_TXPOWER(0x0A),
    EIR_DEVICE_CLASS(0x0D),
    EIR_SIMPLE_PAIRING_RANDOMIZER(0x0F),
    EIR_SECMAN_TK_VALUE(0x10),
    EIR_SECMAN_OOB_FLAGS(0x11),
    EIR_SLAVEINTERVALRANGE(0x12),
    EIR_SVC_SOLICIT_UUID16(0x14),
    EIR_SVC_SOLICIT_UUID128(0x15),
    EIR_SVC_DATA_UUID16(0x16),
    EIR_PUBLIC_TARGET_ADDR(0x17),
    EIR_RANDOM_TARGET_ADDR(0x18),
    EIR_APPEARANCE(0x19),
    EIR_ADVERTISING_INTERVAL(0x1A),
    EIR_LE_DEVICE_ADDRESS(0x1B),
    EIR_LE_ROLE(0x1C),
    EIR_SIMPLE_PAIRING_HASH(0x1D),
    EIR_SVC_SOLICIT_UUID32(0x1F),
    EIR_SVC_DATA_UUID32(0x20),
    EIR_SVC_DATA_UUID128(0x21),
    EIR_LE_SEC_CONFIRMATION_VALUE(0x22),
    EIR_LE_CONNECTION_RANDOM_VALUE(0x23),
    EIR_URI(0x24),
    EIR_INDOOR_POSITIONING(0x25),
    EIR_LE_SUPPORTED_FEATURES(0x27),
    EIR_MANUFACTURER_SPECIFIC(0xFF);

    /**
     * A mapping between the integer code and its corresponding type to
     * facilitate lookup by code.
     */
    private static Map<Integer, EirDataType> codeMapping;

    private int key;

    private EirDataType(int key) {
        this.key = key;
    }

    private static void initMapping() {
        codeMapping = new HashMap<Integer, EirDataType>();
        for (EirDataType s : values()) {
            codeMapping.put(s.key, s);
        }
    }

    /**
     * Lookup function based on the type code. Returns null if the code does not exist.
     *
     * @param bluetoothAddressType
     *            the code to lookup
     * @return enumeration value.
     */
    public static EirDataType getEirPacketType(int eirDataType) {
        if (codeMapping == null) {
            initMapping();
        }

        if (codeMapping.get(eirDataType) == null) {
            return UNKNOWN;
        }

        return codeMapping.get(eirDataType);
    }

    /**
     * Returns the Bluetooth protocol defined value for this enum
     *
     * @return the EIR Data type key
     */
    public int getKey() {
        return key;
    }
}
