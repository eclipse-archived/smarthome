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
package org.eclipse.smarthome.binding.bluetooth.bluegiga.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main handler class for interacting with the BlueGiga serial API. This class provides transaction management and
 * queuing of of data, and conversion of packets from the serial stream into command and response classes.
 *
 * @author Chris Jackson - Initial contribution and API
 *
 */
public class BlueGigaSerialHandler {

    private static final int BLE_MAX_LENGTH = 64;
    private static final int TRANSACTION_TIMEOUT_PERIOD = 50;

    private final Logger logger = LoggerFactory.getLogger(BlueGigaSerialHandler.class);

    /**
     * The portName portName output stream.
     */
    private final OutputStream outputStream;
    private final Queue<BlueGigaCommand> sendQueue = new LinkedList<BlueGigaCommand>();
    private final Timer timer = new Timer();
    private TimerTask timerTask = null;
    private Thread parserThread = null;
    private final ExecutorService executor = ThreadPoolManager.getPool("bluegiga");

    /**
     * Transaction listeners are used internally to correlate the commands and responses
     */
    private final List<BluetoothListener> transactionListeners = new CopyOnWriteArrayList<BluetoothListener>();

    /**
     * The event listeners will be notified of any asynchronous events
     */
    private final Set<BlueGigaEventListener> eventListeners = new CopyOnWriteArraySet<>();

    /**
     * The event listeners will be notified of any life-cycle events of the handler.
     */
    private final Set<BlueGigaHandlerListener> handlerListeners = new CopyOnWriteArraySet<>();

    /**
     * Flag reflecting that parser has been closed and parser parserThread
     * should exit.
     */
    private boolean close = false;

    public BlueGigaSerialHandler(final InputStream inputStream, final OutputStream outputStream) {
        this.outputStream = outputStream;

        final int framecheckParams[] = new int[] { 0x00, 0x7F, 0xC0, 0xF8, 0xE0 };

        parserThread = new Thread("BlueGigaBLEHandler") {
            @Override
            public void run() {
                int exceptionCnt = 0;
                logger.trace("BlueGiga BLE thread started");
                int[] inputBuffer = new int[BLE_MAX_LENGTH];
                int inputCount = 0;
                int inputLength = 0;

                while (!close) {
                    try {
                        int val = inputStream.read();
                        if (val == -1) {
                            continue;
                        }
                        // logger.debug("BLE RX: " + String.format("%02X", val));

                        inputBuffer[inputCount++] = val;

                        if (inputCount < 4) {
                            // The BGAPI protocol has no packet framing, and no error detection, so we do a few
                            // sanity checks on the header to try and allow resyncronisation should there be an
                            // error.
                            // Byte 0: Check technology type is bluetooth and high length is 0
                            // Byte 1: Check length is less than 64 bytes
                            // Byte 2: Check class ID is less than 8
                            // Byte 3: Check command ID is less than 16
                            if ((val & framecheckParams[inputCount]) != 0) {
                                logger.debug("BlueGiga framing error byte {} = {}", inputCount, val);
                                inputCount = 0;
                                continue;
                            }
                        } else if (inputCount == 4) {
                            // Process the header to get the length
                            inputLength = inputBuffer[1] + (inputBuffer[0] & 0x02 << 8) + 4;
                            if (inputLength > 64) {
                                logger.error("BLE length larger than 64 bytes ({})", inputLength);
                            }
                        }
                        if (inputCount == inputLength) {
                            // End of packet reached - process
                            BlueGigaResponse responsePacket = BlueGigaResponsePackets.getPacket(inputBuffer);

                            logger.trace("BLE RX: {}", printHex(inputBuffer, inputLength));
                            logger.trace("BLE RX: {}", responsePacket);
                            if (responsePacket != null) {
                                if (responsePacket.isEvent()) {
                                    notifyEventListeners(responsePacket);
                                } else {
                                    notifyTransactionComplete(responsePacket);
                                }
                            }

                            inputCount = 0;
                        }

                    } catch (final IOException e) {
                        logger.error("BlueGiga BLE IOException: ", e);

                        if (exceptionCnt++ > 10) {
                            logger.error("BlueGiga BLE exception count exceeded");
                            // if (!close) {
                            // frameHandler.error(e);
                            close = true;
                            notifyClosed(e);
                        }
                    }
                }
                logger.debug("BlueGiga BLE exited.");
            }
        };

        parserThread.setDaemon(true);
        parserThread.start();
    }

    /**
     * Requests parser thread to shutdown.
     */
    public void close() {
        this.close = true;
        try {
            parserThread.interrupt();
            parserThread.join();
        } catch (InterruptedException e) {
            logger.warn("Interrupted in packet parser thread shutdown join.");
        }
    }

    /**
     * Checks if parser thread is alive.
     *
     * @return true if parser thread is alive.
     */
    public boolean isAlive() {
        return parserThread != null && parserThread.isAlive() && !close;
    }

    // Synchronize this method to ensure a packet gets sent as a block
    private synchronized void sendFrame(BlueGigaCommand bleFrame) {
        // Send the data
        try {
            int[] payload = bleFrame.serialize();
            logger.trace("TX BLE frame: {}", printHex(payload, payload.length));
            for (int b : payload) {
                outputStream.write(b);
            }
        } catch (IOException e) {
            logger.debug(e.getMessage());
        }

        logger.trace("--> TX BLE frame: {}", bleFrame);
    }

    // Synchronize this method so we can do the window check without interruption.
    // Otherwise this method could be called twice from different threads that could end up with
    // more than the TX_WINDOW number of frames sent.
    private synchronized void sendNextFrame() {
        BlueGigaCommand nextFrame = sendQueue.poll();
        if (nextFrame == null) {
            // Nothing to send
            return;
        }

        sendFrame(nextFrame);
    }

    /**
     * Add a {@link BlueGigaCommand} frame to the send queue. The sendQueue is a
     * FIFO queue. This method queues a {@link BlueGigaCommand} frame without
     * waiting for a response.
     *
     * @param transaction
     *            {@link BlueGigaCommand}
     */
    public void queueFrame(BlueGigaCommand request) {
        logger.trace("TX BLE frame: {}", request);
        checkIfAlive();
        sendQueue.add(request);
        logger.trace("TX BLE queue: {}", sendQueue.size());
        sendNextFrame();
    }

    /**
     * Notify any transaction listeners when we receive a response.
     *
     * @param response
     *            the response data received
     * @return true if the response was processed
     */
    private boolean notifyTransactionComplete(final BlueGigaResponse response) {
        boolean processed = false;

        // logger.debug("NODE {}: notifyTransactionResponse {}",
        // transaction.getNodeId(), transaction.getTransactionId());
        synchronized (transactionListeners) {
            for (BluetoothListener listener : transactionListeners) {
                if (listener.transactionEvent(response)) {
                    processed = true;
                }
            }
        }

        return processed;
    }

    private void addTransactionListener(BluetoothListener listener) {
        synchronized (transactionListeners) {
            if (transactionListeners.contains(listener)) {
                return;
            }

            transactionListeners.add(listener);
        }
    }

    private void removeTransactionListener(BluetoothListener listener) {
        synchronized (transactionListeners) {
            transactionListeners.remove(listener);
        }
    }

    /**
     * Sends an BlueGiga request without waiting for the response.
     *
     * @param bleCommand
     *            Request {@link BlueGigaCommand}
     * @return response {@link Future} {@link BlueGigaResponse}
     */
    public Future<BlueGigaResponse> sendBleRequestAsync(final BlueGigaCommand bleCommand) {
        checkIfAlive();
        class TransactionWaiter implements Callable<BlueGigaResponse>, BluetoothListener {
            private boolean complete = false;
            private BlueGigaResponse response = null;

            @Override
            public BlueGigaResponse call() {
                // Register a listener
                addTransactionListener(this);

                // Send the transaction
                queueFrame(bleCommand);

                // Wait for the transaction to complete
                synchronized (this) {
                    while (!complete) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            complete = true;
                        }
                    }
                }

                // Remove the listener
                removeTransactionListener(this);

                return response;
            }

            @Override
            public boolean transactionEvent(BlueGigaResponse bleResponse) {
                // Check if this response completes our transaction
                if (bleCommand.hashCode() == bleResponse.hashCode()) {
                    return false;
                }

                response = bleResponse;
                complete = true;
                synchronized (this) {
                    notify();
                }

                return true;
            }
        }

        Callable<BlueGigaResponse> worker = new TransactionWaiter();
        return executor.submit(worker);
    }

    /**
     * Sends a {@link BlueGigaCommand} request to the NCP and waits for the response. The response is correlated with
     * the request and the returned {@link BlueGigaResponse} contains the request and response data.
     *
     * @param bleRequest
     *            Request {@link BlueGigaCommand}
     * @return response {@link BlueGigaResponse}
     */
    public BlueGigaResponse sendTransaction(BlueGigaCommand bleCommand) {
        checkIfAlive();
        Future<BlueGigaResponse> futureResponse = sendBleRequestAsync(bleCommand);
        if (futureResponse == null) {
            logger.debug("Error sending BLE transaction: Future is null");
            return null;
        }

        try {
            return futureResponse.get();
            // return bleCommand;
        } catch (InterruptedException | ExecutionException e) {
            logger.debug("Error sending BLE transaction to listeners: ", e);
        }

        return null;
    }

    // TODO: Add a timeout mechanism
    @SuppressWarnings("unused")
    private synchronized void startTransactionTimer() {
        // Stop any existing timer
        resetTransactionTimer();

        // Create the timer task
        timerTask = new TransactionTimer();
        timer.schedule(timerTask, TRANSACTION_TIMEOUT_PERIOD);
    }

    private synchronized void resetTransactionTimer() {
        // Stop any existing timer
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    private class TransactionTimer extends TimerTask {
        // private final Logger logger =
        // LoggerFactory.getLogger(ZWaveTransactionTimer.class);

        @Override
        public void run() {

        }
    }

    /**
     * Notify any transaction listeners when we receive a response.
     * This uses a separate thread to separate the processing of the event.
     *
     * @param response
     *            the response data received
     * @return true if the response was processed
     */
    private void notifyEventListeners(final BlueGigaResponse response) {
        synchronized (this) {
            // Notify the listeners
            for (final BlueGigaEventListener listener : eventListeners) {
                executor.submit(() -> listener.bluegigaEventReceived(response));
            }
        }
    }

    public void addEventListener(BlueGigaEventListener listener) {
        synchronized (eventListeners) {
            eventListeners.add(listener);
        }
    }

    /**
     * Adds a handler listener.
     *
     * @param listener a new handler listener
     */
    public void addHandlerListener(BlueGigaHandlerListener listener) {
        handlerListeners.add(listener);
    }

    public void removeEventListener(BlueGigaEventListener listener) {
        eventListeners.remove(listener);
    }

    public void removeHandlerListener(BlueGigaHandlerListener listener) {
        handlerListeners.remove(listener);
    }

    private String printHex(int[] data, int len) {
        StringBuilder builder = new StringBuilder();

        for (int cnt = 0; cnt < len; cnt++) {
            builder.append(String.format("%02X ", data[cnt]));
        }

        return builder.toString();
    }

    private void checkIfAlive() {
        if (!isAlive()) {
            throw new IllegalStateException("Bluegiga handler is dead. Most likely because of IO errors. "
                    + "Re-initialization of the BlueGigaSerialHandler is required.");
        }
    }

    /**
     * Notify handler event listeners that the handler was bluegigaClosed due to an error specified as an argument.
     *
     * @param reason the reason to bluegigaClosed
     */
    private void notifyClosed(Exception reason) {
        // It should be safe enough not to use the NotificationService as this is a fatal error, no any further actions
        // can be done with the handler, a new handler should be re-created
        // There is another reason why NotificationService can't be used - the listeners should be notified immidiately
        for (BlueGigaHandlerListener listener : handlerListeners) {
            try {
                listener.bluegigaClosed(reason);
            } catch (Exception ex) {
                logger.warn("Execution error of a BlueGigaHandlerListener listener.", ex);
            }
        }
    }

    interface BluetoothListener {
        boolean transactionEvent(BlueGigaResponse bleResponse);
    }

}
