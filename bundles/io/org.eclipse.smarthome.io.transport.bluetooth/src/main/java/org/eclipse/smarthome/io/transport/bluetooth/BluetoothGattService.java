/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.bluetooth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.smarthome.io.transport.bluetooth.internal.BluetoothConstants;

/**
 * Represents a GATT service
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public abstract class BluetoothGattService {
    protected UUID uuid;
    protected int instanceId;
    protected int serviceType;

    protected List<BluetoothGattService> includedServices;
    protected Map<UUID, BluetoothGattCharacteristic> gattCharacteristics;

    public static final int SERVICE_TYPE_PRIMARY = 0;
    public static final int SERVICE_TYPE_SECONDARY = 1;

    public BluetoothGattService() {
        this.instanceId = 0;
        this.gattCharacteristics = new HashMap<UUID, BluetoothGattCharacteristic>();
        this.includedServices = new ArrayList<BluetoothGattService>();
    }

    public BluetoothGattService(UUID uuid, int serviceType) {
        this();
        this.uuid = uuid;
        this.serviceType = serviceType;
    }

    /**
     * Get characteristic based on UUID
     */
    public BluetoothGattCharacteristic getCharacteristic(UUID uuid) {
        return gattCharacteristics.get(uuid);
    }

    /**
     * Get list of characteristics of the service
     */
    public List<BluetoothGattCharacteristic> getCharacteristics() {
        return new ArrayList<BluetoothGattCharacteristic>(gattCharacteristics.values());
    }

    /**
     * Return the UUID of this service
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Get the type of this service (primary/secondary)
     */
    public int getType() {
        return serviceType;
    }

    /**
     * Returns the instance ID for this service
     *
     * @return Instance ID of this service
     */
    public int getInstanceId() {
        return instanceId;
    }

    /**
     * Add an included service to this service
     *
     * @param service The service to be added
     * @return true, if the included service was added to the service
     */
    public boolean addService(BluetoothGattService service) {
        includedServices.add(service);
        return true;
    }

    /**
     * Add a characteristic to this service
     *
     * @param characteristic The characteristics to be added
     * @return true, if the characteristic was added to the service
     */
    public boolean addCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (gattCharacteristics.get(characteristic.getUuid()) != null) {
            return false;
        }

        this.gattCharacteristics.put(characteristic.getUuid(), characteristic);
        characteristic.setService(this);
        return true;
    }

    public GattService getService() {
        return GattService.getService(uuid);
    }

    public enum GattService {

        // List of GATT Services
        ALERT_NOTIFICATION_SERVICE(0x1811),
        AUTOMATION_IO(0x1815),
        BATTERY_SERVICE(0x180F),
        BLOOD_PRESSURE(0x1810),
        BODY_COMPOSITION(0x181B),
        BOND_MANAGEMENT(0x181E),
        CONTINUOUS_GLUCOSE_MONITORING(0x181F),
        CURRENT_TIME_SERVICE(0x1805),
        CYCLING_POWER(0x1818),
        CYCLING_SPEED_AND_CADENCE(0x1816),
        DEVICE_INFORMATION(0x180A),
        ENVIRONMENTAL_SENSING(0x181A),
        GENERIC_ACCESS(0x1800),
        GENERIC_ATTRIBUTE(0x1801),
        GLUCOSE(0x1808),
        HEALTH_THERMOMETER(0x1809),
        HEART_RATE(0x180D),
        HTTP_PROXY(0x1823),
        HUMAN_INTERFACE_DEVICE(0x1812),
        IMMEDIATE_ALERT(0x1802),
        INDOOR_POSITIONING(0x1821),
        INTERNET_PROTOCOL_SUPPORT(0x1820),
        LINK_LOSS(0x1803L),
        LOCATION_AND_NAVIGATION(0x1819),
        NEXT_DST_CHANGE_SERVICE(0x1807),
        PHONE_ALERT_STATUS_SERVICE(0x180E),
        REFERENCE_TIME_UPDATE_SERVICE(0x1806),
        RUNNING_SPEED_AND_CADENCE(0x1814),
        SCAN_PARAMETERS(0x1813),
        TX_POWER(0x1804),
        USER_DATA(0x181C),
        WEIGHT_SCALE(0x181D);

        private static Map<UUID, GattService> uuidToServiceMapping;

        private UUID uuid;

        private GattService(long key) {
            this.uuid = new UUID((key << 32) | 0x1000, BluetoothConstants.bleUuid);
        }

        private static void initMapping() {
            uuidToServiceMapping = new HashMap<UUID, GattService>();
            for (GattService s : values()) {
                uuidToServiceMapping.put(s.uuid, s);
            }
        }

        public static GattService getService(UUID uuid) {
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