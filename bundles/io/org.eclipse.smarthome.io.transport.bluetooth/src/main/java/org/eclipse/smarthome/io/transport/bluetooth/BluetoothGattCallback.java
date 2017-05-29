/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.bluetooth;

/**
 * This class provides callback methods for receiving notifications about GATT changes.
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class BluetoothGattCallback {

    /**
     * Callback triggered as a result of a remote characteristic notification.
     *
     * @param gatt
     * @param characteristic
     */
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
    }

    /**
     * Callback reporting the result of a characteristic read operation.
     *
     * @param gatt
     * @param characteristic
     * @param status
     */
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
    }

    /**
     * Callback indicating the result of a characteristic write operation.
     *
     * @param gatt
     * @param characteristic
     * @param status
     */
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
    }

    /**
     * Callback indicating when GATT client has connected or disconnected from the GATT server
     *
     * @param gatt GATT client invoked readDescriptor(BluetoothGattDescriptor)
     * @param status Status of the connect or disconnect operation. GATT_SUCCESS if the operation succeeds.
     * @param newState Provides the new connection state. Can be one of STATE_DISCONNECTED or STATE_CONNECTED
     */
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
    }

    /**
     * Callback reporting the result of a descriptor read operation.
     *
     * @param gatt
     * @param descriptor
     * @param status
     */
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
    }

    /**
     * Callback indicating the result of a descriptor write operation.
     *
     * @param gatt
     * @param descriptor
     * @param status
     */
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
    }

    /**
     * Callback indicating the MTU for a given device connection has changed.
     *
     * @param gatt
     * @param mtu
     * @param status
     */
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
    }

    /**
     * Callback reporting the RSSI for a remote device connection.
     *
     * @param gatt
     * @param rssi
     * @param status
     */
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
    }

    /**
     * Callback indicating when a reliable write transaction has been completed.
     *
     * @param gatt
     * @param status
     */
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
    }

    /**
     * Callback called when the list of remote services, characteristics and descriptors have been updated
     *
     * @param gatt
     * @param status
     */
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
    }

}
