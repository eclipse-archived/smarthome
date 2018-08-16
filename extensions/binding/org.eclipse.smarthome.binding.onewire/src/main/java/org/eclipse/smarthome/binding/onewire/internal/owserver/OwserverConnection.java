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
package org.eclipse.smarthome.binding.onewire.internal.owserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.onewire.internal.OwException;
import org.eclipse.smarthome.binding.onewire.internal.OwPageBuffer;
import org.eclipse.smarthome.binding.onewire.internal.handler.OwserverBridgeHandler;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OwserverConnection} defines the protocol for connections to owservers
 *
 * @author Jan N. Klug - Initial contribution
 */

@NonNullByDefault
public class OwserverConnection {
    public static final int DEFAULT_PORT = 4304;
    public static final int KEEPALIVE_INTERVAL = 1000;

    private static final int CONNECTION_MAX_RETRY = 5;

    private final Logger logger = LoggerFactory.getLogger(OwserverConnection.class);

    private final OwserverBridgeHandler thingHandlerCallback;
    private String owserverAddress = "";
    private int owserverPort = DEFAULT_PORT;

    private @Nullable Socket owserverSocket = null;
    private @Nullable DataInputStream owserverInputStream = null;
    private @Nullable DataOutputStream owserverOutputStream = null;
    private OwserverConnectionState owserverConnectionState = OwserverConnectionState.STOPPED;

    // reset to 0 after successful request
    private int connectionErrorCounter = 0;

    public OwserverConnection(OwserverBridgeHandler thingHandlerCallback) {
        this.thingHandlerCallback = thingHandlerCallback;
    }

    /**
     * set the owserver host address
     *
     * @param address as String (IP or FQDN), defaults to localhost
     */
    public void setHost(String address) {
        this.owserverAddress = address;
        if (owserverConnectionState != OwserverConnectionState.STOPPED) {
            close();
        }
    }

    /**
     * set the owserver port
     *
     * @param port defaults to 4304
     */
    public void setPort(int port) {
        this.owserverPort = port;
        if (owserverConnectionState != OwserverConnectionState.STOPPED) {
            close();
        }
    }

    /**
     * start the owserver connection
     */
    public void start() {
        connectionErrorCounter = 0;
        owserverConnectionState = OwserverConnectionState.CLOSED;
        boolean success = false;
        do {
            success = open();
        } while (success != true && owserverConnectionState != OwserverConnectionState.FAILED);
    }

    /**
     * stop the owserver connection
     */
    public void stop() {
        close();
        owserverConnectionState = OwserverConnectionState.STOPPED;
        thingHandlerCallback.reportConnectionState(owserverConnectionState);
    }

    /**
     * list all devices on this owserver
     *
     * @return a list of device ids
     */
    public List<String> getDirectory() throws OwException {
        List<String> directory = new ArrayList<String>();
        OwserverPacket requestPacket = new OwserverPacket(OwserverMessageType.DIR, "/");

        write(requestPacket);

        OwserverPacket returnPacket = null;
        do {
            try {
                returnPacket = read(false);
            } catch (OwException e) {
                logger.debug("getDirectory may have returned incomplete result: {}", e.getMessage());
                closeOnError();
                return directory;
            }
            if (returnPacket.hasPayload()) {
                directory.add(returnPacket.getPayloadString());
            }
        } while ((returnPacket.isPingPacket() || returnPacket.hasPayload()));

        if (!returnPacket.hasControlFlag(OwserverControlFlag.PERSISTENCE)) {
            logger.trace("closing connection because persistence was denied");
            close();
        }

        connectionErrorCounter = 0;
        return directory;
    }

    /**
     * check sensor presence
     *
     * @param path full owfs path to sensor
     * @return OnOffType, ON=present, OFF=not present
     * @throws OwException
     */
    public State checkPresence(String path) throws OwException {
        State returnValue = OnOffType.OFF;
        try {
            OwserverPacket requestPacket;
            requestPacket = new OwserverPacket(OwserverMessageType.PRESENT, path, OwserverControlFlag.UNCACHED);

            OwserverPacket returnPacket = request(requestPacket);
            if (returnPacket.getReturnCode() == 0) {
                returnValue = OnOffType.ON;
            }

        } catch (OwException e) {
            returnValue = OnOffType.OFF;
        }
        logger.trace("presence {} : {}", path, returnValue);
        return returnValue;
    }

    /**
     * read a decimal type
     *
     * @param path full owfs path to sensor
     * @return DecimalType if successful
     * @throws OwException
     */
    public State readDecimalType(String path) throws OwException {
        State returnState = UnDefType.UNDEF;
        OwserverPacket requestPacket = new OwserverPacket(OwserverMessageType.READ, path);

        OwserverPacket returnPacket = request(requestPacket);
        if ((returnPacket.getReturnCode() != -1) && returnPacket.hasPayload()) {
            returnState = DecimalType.valueOf(returnPacket.getPayloadString().trim());
        } else {
            throw new OwException("invalid of empty packet");
        }

        return returnState;
    }

    /**
     * read a string
     *
     * @param path full owfs path to sensor
     * @return requested String
     * @throws OwException
     */
    public String readString(String path) throws OwException {
        OwserverPacket requestPacket = new OwserverPacket(OwserverMessageType.READ, path);
        OwserverPacket returnPacket = request(requestPacket);

        if ((returnPacket.getReturnCode() != -1) && returnPacket.hasPayload()) {
            return returnPacket.getPayloadString().trim();
        } else {
            throw new OwException("invalid or empty packet");
        }
    }

    /**
     * read all sensor pages
     *
     * @param path full owfs path to sensor
     * @return page buffer
     * @throws OwException
     */
    public OwPageBuffer readPages(String path) throws OwException {
        OwserverPacket requestPacket = new OwserverPacket(OwserverMessageType.READ, path + "/pages/page.ALL");
        OwserverPacket returnPacket = request(requestPacket);
        if ((returnPacket.getReturnCode() != -1) && returnPacket.hasPayload()) {
            return returnPacket.getPayload();
        } else {
            throw new OwException("invalid or empty packet");
        }
    }

    /**
     * write a DecimalType
     *
     * @param path full owfs path to the sensor
     * @param value the value to write
     * @throws OwException
     */
    public void writeDecimalType(String path, DecimalType value) throws OwException {
        OwserverPacket requestPacket = new OwserverPacket(OwserverMessageType.WRITE, path);
        requestPacket.setPayload(String.valueOf(value));

        OwserverPacket returnPacket = request(requestPacket);

        logger.trace("wrote: {}, got: {} ", requestPacket, returnPacket);
    }

    /**
     * process a request to the owserver
     *
     * @param requestPacket the request to be send
     * @return the raw owserver answer
     * @throws OwException
     */
    private OwserverPacket request(OwserverPacket requestPacket) throws OwException {
        OwserverPacket returnPacket = new OwserverPacket(OwserverPacketType.RETURN);
        try {
            write(requestPacket);
            do {
                if (requestPacket.getMessageType() == OwserverMessageType.PRESENT
                        || requestPacket.getMessageType() == OwserverMessageType.NOP) {
                    returnPacket = read(true);
                } else {
                    returnPacket = read(false);
                }
            } while (returnPacket.isPingPacket() || !returnPacket.hasPayload());
        } catch (OwException e) {
            logger.debug("failed requesting {}->{} [{}]", requestPacket, returnPacket, e.getMessage());
            throw e;
        }

        if (!returnPacket.hasControlFlag(OwserverControlFlag.PERSISTENCE)) {
            logger.trace("closing connection because persistence was denied");
            close();
        }

        connectionErrorCounter = 0;
        return returnPacket;
    }

    /**
     * sends a nop to keep the server connection active
     *
     * @return true if successful send
     * @throws OwException
     */
    private boolean sendNop() throws OwException {
        OwserverPacket requestPacket = new OwserverPacket(OwserverMessageType.NOP, "");
        OwserverPacket returnPacket = request(requestPacket);
        if (returnPacket.getReturnCode() == -1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * open/reopen the connection to the owserver
     *
     * @return true if open
     */
    private boolean open() {
        try {
            if (owserverConnectionState == OwserverConnectionState.CLOSED) {
                // open socket & set timeout to 3000ms
                owserverSocket = new Socket(owserverAddress, owserverPort);
                owserverSocket.setSoTimeout(3000);

                if (owserverSocket != null) {
                    owserverInputStream = new DataInputStream(owserverSocket.getInputStream());
                    if (owserverInputStream == null) {
                        logger.warn("could not get input stream after opening connection");
                        closeOnError();
                        return false;
                    }
                } else {
                    closeOnError();
                    return false;
                }
                if (owserverSocket != null) {
                    owserverOutputStream = new DataOutputStream(owserverSocket.getOutputStream());
                    if (owserverOutputStream == null) {
                        logger.warn("could not get output stream after opening connection");
                        closeOnError();
                        return false;
                    }
                } else {
                    closeOnError();
                    return false;
                }

                owserverConnectionState = OwserverConnectionState.OPENED;
                thingHandlerCallback.reportConnectionState(owserverConnectionState);

                logger.debug("opened OwServerConnection to {}:{}", owserverAddress, owserverPort);
                return true;
            } else if (owserverConnectionState == OwserverConnectionState.OPENED) {
                // socket already open, clear input buffer
                logger.trace("owServerConnection already open, skipping input buffer");
                while (owserverInputStream != null) {
                    if (owserverInputStream.skip(owserverInputStream.available()) == 0) {
                        return true;
                    }
                }
                logger.debug("input stream not available on skipping");
                closeOnError();
                return false;
            } else {
                return false;
            }
        } catch (IOException e) {
            logger.debug("could not open owServerConnection to {}:{}: {}", owserverAddress, owserverPort,
                    e.getMessage());
            closeOnError();
            return false;
        }
    }

    /**
     * close the connection to the owserver instance
     */
    private void close() {
        if (owserverSocket != null) {
            try {
                owserverSocket.close();
            } catch (IOException e) {
                owserverConnectionState = OwserverConnectionState.FAILED;
                logger.warn("could not close connection: {}", e.getMessage());
            }
        }

        owserverSocket = null;
        owserverInputStream = null;
        owserverOutputStream = null;

        logger.debug("closed connection");
        owserverConnectionState = OwserverConnectionState.CLOSED;

        thingHandlerCallback.reportConnectionState(owserverConnectionState);
    }

    /**
     * close the connection to the owserver instance after an error occured
     */
    private void closeOnError() {
        connectionErrorCounter++;
        close();
        if (connectionErrorCounter > CONNECTION_MAX_RETRY) {
            owserverConnectionState = OwserverConnectionState.FAILED;
            thingHandlerCallback.reportConnectionState(owserverConnectionState);
        }
    }

    /**
     * write to the owserver
     *
     * @param requestPacket data to write
     * @throws OwException
     */
    private void write(OwserverPacket requestPacket) throws OwException {
        try {
            if (open()) {
                requestPacket.setControlFlags(OwserverControlFlag.PERSISTENCE);
                if (owserverOutputStream != null) {
                    owserverOutputStream.write(requestPacket.toBytes());
                    logger.trace("wrote: {}", requestPacket);
                } else {
                    logger.debug("output stream not available on write");
                    closeOnError();
                }
            } else {
                throw new OwException("I/O error: could not open connection to send request packet");
            }
        } catch (IOException e) {
            closeOnError();
            logger.debug("couldn't send {}, {}", requestPacket, e.getMessage());
            throw new OwException("I/O Error: exception while sending request packet - " + e.getMessage());
        }
    }

    /**
     * read from owserver
     *
     * @return the read packet
     * @throws OwException
     */
    private OwserverPacket read(boolean noTimeoutException) throws OwException {
        OwserverPacket returnPacket = new OwserverPacket(OwserverPacketType.RETURN);
        try {
            if (owserverInputStream != null) {
                DataInputStream inputStream = owserverInputStream;
                returnPacket = new OwserverPacket(inputStream, OwserverPacketType.RETURN);
                logger.trace("read: {}", returnPacket);
            } else {
                logger.debug("input stream not available on read");
                closeOnError();
            }
        } catch (EOFException e) {
            // nothing to read
        } catch (OwException e) {
            closeOnError();
            throw e;
        } catch (IOException e) {
            if (e.getMessage().equals("Read timed out") && noTimeoutException) {
                logger.trace("timeout - setting error code to -1");
                returnPacket.setPayload("timeout");
                returnPacket.setReturnCode(-1);
            } else {
                closeOnError();
                throw new OwException("I/O error: exception while reading packet - " + e.getMessage());
            }
        }

        return returnPacket;
    }

}
