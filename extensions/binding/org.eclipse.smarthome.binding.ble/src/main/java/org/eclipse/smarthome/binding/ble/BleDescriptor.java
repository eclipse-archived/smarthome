/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.ble;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The {@link BleDescriptor} class defines the BLE Descriptor.
 * <p>
 * Descriptors are defined attributes that describe a characteristic value.
 * <p>
 * https://www.bluetooth.com/specifications/gatt/descriptors
 *
 * @author Chris Jackson - Initial contribution
 */
public class BleDescriptor {
    protected BleCharacteristic characteristic;
    protected UUID uuid;
    protected byte[] value;

    /**
     * Returns the characteristic this descriptor belongs to.
     *
     * @return
     */
    BleCharacteristic getCharacteristic() {
        return characteristic;

    }

    /**
     * Returns the permissions for this descriptor.
     *
     * @return
     */
    public int getPermissions() {
        return 0;
    }

    /**
     * Returns the UUID of this descriptor.
     *
     * @return
     */
    public UUID getUuid() {
        return uuid;

    }

    /**
     * Returns the stored value for this descriptor. It doesn't read remove data.
     */
    public byte[] getValue() {
        return value;
    }

    /**
     * Returns the stored value for this descriptor. It doesn't read remove data.
     *
     * @param value
     * @return
     */
    public boolean setValue(byte[] value) {
        return false;
    }

    public GattDescriptor getDescriptor() {
        return GattDescriptor.getDescriptor(uuid);
    }

    public enum GattDescriptor {
        // Descriptors
        CHARACTERISTIC_EXTENDED_PROPERTIES(0x2900),
        CHARACTERISTIC_USER_DESCRIPTION(0x2901),
        CLIENT_CHARACTERISTIC_CONFIGURATION(0x2902),
        SERVER_CHARACTERISTIC_CONFIGURATION(0x2903),
        CHARACTERISTIC_PRESENTATION_FORMAT(0x2904),
        CHARACTERISTIC_AGGREGATE_FORMAT(0x2905),
        VALID_RANGE(0x2906),
        EXTERNAL_REPORT_REFERENCE(0x2907),
        REPORT_REFERENCE(0x2908),
        NUMBER_OF_DIGITALS(0x2909),
        TRIGGER_SETTING(0x290A);

        private static Map<UUID, GattDescriptor> uuidToServiceMapping;

        private UUID uuid;

        private GattDescriptor(long key) {
            this.uuid = new UUID((key << 32) | 0x1000, BleBindingConstants.bleUuid);
        }

        private static void initMapping() {
            uuidToServiceMapping = new HashMap<UUID, GattDescriptor>();
            for (GattDescriptor s : values()) {
                uuidToServiceMapping.put(s.uuid, s);
            }
        }

        public static GattDescriptor getDescriptor(UUID uuid) {
            if (uuidToServiceMapping == null) {
                initMapping();
            }
            return uuidToServiceMapping.get(uuid);
        }

        /**
         * @return the key
         */
        public UUID getUUID() {
            return uuid;
        }
    }
}
