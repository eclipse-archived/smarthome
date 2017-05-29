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
import java.util.concurrent.PriorityBlockingQueue;

import org.eclipse.smarthome.io.transport.bluetooth.internal.BluetoothCommand;
import org.eclipse.smarthome.io.transport.bluetooth.internal.BluetoothCommand.CommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The BluetoothGatt service is the main communication interface with the Bluetooth LE device. The service
 * provides information about available services and provides methods for reading and writing to its characteristics.
 * <p>
 * This class implements a transmit queue - any calls to the descriptor/characteristic read/write methods
 * will be queued to ensure that only one message is outstanding at once. This is a deviation from the Android
 * implementation and has been added to make life easier for developers.
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public abstract class BluetoothGatt implements BluetoothProfile {

    private static final Logger logger = LoggerFactory.getLogger(BluetoothGatt.class);

    public static final int GATT_SUCCESS = 0;
    public static final int GATT_FAILURE = 1;

    protected Map<UUID, BluetoothGattService> gattServices = new HashMap<UUID, BluetoothGattService>();

    protected boolean autoConnect;
    protected BluetoothGattCallback callback;
    protected int transport;
    protected boolean connectPending = false;

    private final int INITIAL_TX_QUEUE_SIZE = 25;
    private final PriorityBlockingQueue<BluetoothCommand> sendQueue = new PriorityBlockingQueue<BluetoothCommand>(
            INITIAL_TX_QUEUE_SIZE, new BluetoothCommand.PriorityComparator());
    TransmitNotify transmitMonitor = new TransmitNotify();
    private BluetoothCommand sentMessage;
    private TransmitThread sendThread = null;

    protected int connectionState = STATE_DISCONNECTED;

    protected BluetoothGatt(boolean autoConnect, BluetoothGattCallback callback, int transport) {
        this.autoConnect = autoConnect;
        this.callback = callback;
        this.transport = transport;
    }

    /**
     * Initiates a reliable write transaction for a given remote device.
     *
     * @return true, if the request to execute the transaction has been sent
     */
    public boolean beginReliableWrite() {
        return false;
    }

    /**
     * Executes a reliable write transaction for a given remote device.
     *
     * This function will commit all queued up characteristic write operations for a given remote device.
     *
     * A onReliableWriteCompleted(BluetoothGatt, int) callback is called when complete.
     *
     * @return true, if the request to execute the transaction has been sent
     */
    public boolean executeReliableWrite() {
        return false;
    }

    /**
     * Cancels a reliable write transaction for a given device.
     */
    public void abortReliableWrite() {

    }

    /**
     * Connect to devices GATT server.
     *
     * @return true, if the connection attempt was initiated successfully
     */
    public boolean connect() {
        connectPending = true;
        return false;
    }

    /**
     * Disconnect from devices GATT server.
     *
     */
    public void disconnect() {
        connectPending = false;
    }

    /**
     * Discovers services proided by a remote device including their characteristics and descriptors.
     *
     *
     * @return true, if the remote service discovery started ok
     */
    public abstract boolean discoverServices();

    /**
     * Reads the requested characteristic from the associated remote device.
     *
     * @param characteristic
     * @return true, if the read operation started ok
     */
    public boolean readCharacteristic(BluetoothGattCharacteristic characteristic) {
        return queueAdd(new BluetoothCommand(CommandType.CHARACTERISTIC_READ, characteristic));
    }

    /**
     * Read the RSSI for a connected remote device.
     *
     * The onReadRemoteRssi callback will be called when the RSSI value has been read.
     *
     * @return true if the RSSI value has been requested successfully
     */
    public boolean readRemoteRssi() {
        return false;
    }

    /**
     * Writes a given characteristic and its values to the associated remote device.
     *
     * @param characteristic
     * @return
     */
    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        return queueAdd(new BluetoothCommand(CommandType.CHARACTERISTIC_WRITE, characteristic));
    }

    /**
     * Reads the value for a given descriptor from the associated remote device.
     *
     * Once the read operation is complete, the onDescriptorRead callback is called
     *
     * @param descriptor
     * @return true, if the read operation was initiated successfully
     */
    public boolean readDescriptor(BluetoothGattDescriptor descriptor) {
        return queueAdd(new BluetoothCommand(CommandType.DESCRIPTOR_READ, descriptor));
    }

    /**
     * Write the value of a given descriptor to the associated remote device.
     *
     * @param descriptor
     * @return
     */
    public boolean writeDescriptor(BluetoothGattDescriptor descriptor) {
        return queueAdd(new BluetoothCommand(CommandType.DESCRIPTOR_WRITE, descriptor));
    }

    /**
     * Enable or disable notifications/indications for a characteristic
     *
     * @param characteristic
     * @param enable
     * @return true, if the requested notification status was set successfully
     */
    public boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enable) {
        return characteristic.setNotification(this, enable);
    }

    /**
     * Return a GATT service based on a UUID.
     *
     * This requires that service discovery has been completed for the given device.
     *
     * @param uuid UUID of service
     * @return BluetoothGattService
     */
    public BluetoothGattService getService(UUID uuid) {
        return gattServices.get(uuid);
    }

    /**
     * Get a list of GATT services offered by the device.
     *
     * This requires that service discovery has been completed for the given device.
     *
     * @return List of services
     */
    public List<BluetoothGattService> getServices() {
        return new ArrayList<BluetoothGattService>(gattServices.values());
    }

    /**
     * Get a characteristic from the server.
     * This searches through all the services looking for an existing characteristic supported
     * by this server.
     *
     * @param UUID the UUID of this characteristic
     * @return the characteristic, or null if not found
     */
    public BluetoothGattCharacteristic getCharacteristic(UUID uuid) {
        BluetoothGattCharacteristic characteristic = null;
        for (BluetoothGattService service : getServices()) {
            characteristic = service.getCharacteristic(uuid);
            if (characteristic != null) {
                break;
            }
        }

        return characteristic;
    }

    /**
     * Notify the callback that a characteristic has changed.
     * This is called by the characteristic when notifications are enabled.
     *
     * @param characteristic
     */
    public void doCharacteristicNotification(BluetoothGattCharacteristic characteristic) {
        if (callback != null) {
            callback.onCharacteristicChanged(this, characteristic);
        }
    }

    /**
     *
     */
    protected void addService(BluetoothGattService service) {
        // Make sure we don't already know about this device
        if (gattServices.containsKey(service.getUuid())) {
            return;
        }
        gattServices.put(service.getUuid(), service);

        // Notify the callback
        if (callback != null) {
            callback.onServicesDiscovered(this, GATT_SUCCESS);
        }
    }

    /**
     * Close the GATT client and release resources
     */
    public void close() {
        gattServices.clear();
        callback = null;

        if (sendThread != null) {
            sendThread.interrupt();
            try {
                sendThread.join();
            } catch (InterruptedException e) {
            }
            sendThread = null;
        }
    }

    private boolean queueAdd(BluetoothCommand command) {
        if (autoConnect) {
            connect();
        }

        // Make sure the transmit thread is alive - if not, start it
        if (sendThread == null || (sendThread != null && !sendThread.isAlive())) {
            this.sendThread = new TransmitThread();
            this.sendThread.start();
        }

        // Check for duplicates so there's only one request to a UUID queued
        if (sendQueue.contains(command)) {
            logger.debug("Message already exists in queue - removing older message from queue");
            sendQueue.remove(command);
        }

        // Queue the request
        logger.debug("Adding frame to queue: current length is {}", sendQueue.size());
        return sendQueue.add(command);
    }

    public int getQueueLength() {
        return sendQueue.size();
    }

    // TODO: Do we need the message type, or is the UUID enough
    public void processQueueResponse(UUID uuid) {
        if (sentMessage == null) {
            // TODO: is this correct?
            logger.debug("sentMessage is null while processing response! Queue is now {}", sendQueue.size());
            return;
        }

        if (uuid.equals(sentMessage.getUuid())) {
            transmitMonitor.setFreeToSend(true);
            logger.debug("Queue updated with {} - queue is now {}", uuid, sendQueue.size());
        } else {
            logger.debug("Message didn't correlate with lastSent - queue is now {}", sendQueue.size());

        }
    }

    /**
     * This method needs to be called by stack implementations to advise the queue handler that the connection
     * state has changed.
     *
     * This manages a semaphore used to start/stop the sending of queued data.
     *
     * @param connected
     */
    protected void setConnectedState(boolean connected) {
        // Keep track of our connection state!
        // int oldState = connectionState;
        connectionState = connected ? STATE_CONNECTED : STATE_DISCONNECTED;
        transmitMonitor.setConnected(connected);

        if (connected) {
            // Advise the listeners
            if (callback != null) {
                callback.onConnectionStateChange(this, GATT_SUCCESS, STATE_CONNECTED);
            }
        } else {
            // Advise the listeners
            if (callback != null) {
                callback.onConnectionStateChange(this, GATT_SUCCESS, STATE_DISCONNECTED);
            }

            // We've disconnected.
            // This means any outstanding messages will time out, but more importantly, when we reconnect, we're ok to
            // send a new request
            transmitMonitor.setFreeToSend(true);

            // Check if we have anything waiting to send and reconnect if we do
            if (sendQueue.size() != 0 && autoConnect == true) {
                // Messages are queued - attempt to reconnect
                connect();
            }
        }
    }

    private class TransmitThread extends Thread {

        @Override
        public void run() {
            logger.debug("Starting transmit thread");
            transmitMonitor.setFreeToSend(true);
            try {
                while (!interrupted()) {
                    // Wait until we're allowed to send
                    // This waits until we're connected to the device...
                    // It also ensures there's no outstanding requests, and there's a message in teh queue
                    transmitMonitor.waitForSend();

                    // Take the message from the send queue
                    try {
                        sentMessage = sendQueue.take();
                        transmitMonitor.setFreeToSend(false);
                        logger.debug(": Took message from queue for sending. Queue length = {}", sendQueue.size());
                    } catch (InterruptedException e1) {
                        break;
                    }

                    // Process the message
                    switch (sentMessage.getMessageType()) {
                        case CHARACTERISTIC_READ:
                            logger.debug(": Sending CHARACTERISTIC_READ {}", sentMessage.getCharacteristic().getUuid());
                            sendReadCharacteristic(sentMessage.getCharacteristic());
                            break;
                        case CHARACTERISTIC_WRITE:
                            logger.debug(": Sending CHARACTERISTIC_WRITE {}",
                                    sentMessage.getCharacteristic().getUuid());
                            sendWriteCharacteristic(sentMessage.getCharacteristic());
                            break;
                        case DESCRIPTOR_READ:
                            logger.debug(": Sending DESCRIPTOR_READ {}", sentMessage.getDescriptor().getUuid());
                            sendReadDescriptor(sentMessage.getDescriptor());
                            break;
                        case DESCRIPTOR_WRITE:
                            logger.debug("Sending DESCRIPTOR_WRITE {}", sentMessage.getDescriptor().getUuid());
                            sendWriteDescriptor(sentMessage.getDescriptor());
                            break;
                        default:
                            logger.debug("Sending unknown {}", sentMessage.getMessageType());
                            break;
                    }
                }
            } catch (Exception e) {
                logger.error("Exception during Send thread", e);
            }

            logger.debug("Exiting transmit thread");
        }
    };

    protected void sendReadCharacteristic(BluetoothGattCharacteristic characteristic) {
    }

    protected void sendWriteCharacteristic(BluetoothGattCharacteristic characteristic) {
    }

    protected void sendReadDescriptor(BluetoothGattDescriptor descriptor) {
    }

    protected void sendWriteDescriptor(BluetoothGattDescriptor descriptor) {
    }

    /**
     * This class implements a synchronisation mechanism to wait until we are free to send.
     *
     * The transmit queue calls waitForSend which will block until we're connected, there's a message
     * to send, and there's no outstanding messages awaiting responses.
     *
     */
    private class TransmitNotify {
        private Object myMonitorObject = new Object();
        private boolean isConnected = false;
        private boolean isFreeToSend = false;

        public void waitForSend() {
            synchronized (myMonitorObject) {
                logger.debug("waitForSend start: connected={}, free={}, queue={}", isConnected, isFreeToSend,
                        sendQueue.size());
                while (isConnected == false || isFreeToSend == false || sendQueue.size() == 0) {
                    logger.debug("waitForSend waiting: connected={}, free={}, queue={}", isConnected, isFreeToSend,
                            sendQueue.size());
                    try {
                        myMonitorObject.wait();
                    } catch (InterruptedException e) {
                    }
                }

                logger.debug("waitForSend ok: connected={}, free={}, queue={}", isConnected, isFreeToSend,
                        sendQueue.size());
            }
        }

        public void setConnected(boolean connected) {
            synchronized (myMonitorObject) {
                isConnected = connected;
                myMonitorObject.notify();
            }
        }

        public void setFreeToSend(boolean free) {
            synchronized (myMonitorObject) {
                isFreeToSend = free;
                myMonitorObject.notify();
            }
        }
    }
}
