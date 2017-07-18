/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lirc.internal.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.binding.lirc.internal.config.LIRCBridgeConfiguration;
import org.eclipse.smarthome.binding.lirc.internal.messages.LIRCButtonEvent;
import org.eclipse.smarthome.binding.lirc.internal.messages.LIRCResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connector for communication with the LIRC server
 *
 * @author Andrew Nagle - Initial contributor
 */
public class LIRCConnector {

    private final Logger logger = LoggerFactory.getLogger(LIRCConnector.class);

    private Set<LIRCEventListener> listeners = new CopyOnWriteArraySet<LIRCEventListener>();
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private PrintWriter outWriter;
    private Thread readerThread;

    public void addEventListener(LIRCEventListener listener) {
        listeners.add(listener);
    }

    public void removeEventListener(LIRCEventListener listener) {
        listeners.remove(listener);
    }

    public void connect(LIRCBridgeConfiguration config) throws UnknownHostException, IOException {
        logger.debug("Connecting");

        // Consider adding support for Unix Domain sockets as well.
        // This would allow us to autodiscover the local LIRC server.
        // The junixsocket library should work nicely
        socket = new Socket(config.getHost(), config.getPortNumber());
        out = socket.getOutputStream();
        in = socket.getInputStream();
        outWriter = new PrintWriter(out, true);
        readerThread = new LIRCStreamReader(this, in);
        readerThread.start();
    }

    public void disconnect() {
        logger.debug("Disconnecting");
        if (readerThread != null) {
            logger.debug("Interrupt stream listener");
            readerThread.interrupt();
            readerThread = null;
        }
        if (outWriter != null) {
            logger.debug("Close print writer stream");
            IOUtils.closeQuietly(outWriter);
            outWriter = null;
        }
        if (out != null) {
            logger.debug("Close tcp out stream");
            IOUtils.closeQuietly(out);
            out = null;
        }
        if (in != null) {
            logger.debug("Close tcp in stream");
            IOUtils.closeQuietly(in);
            in = null;
        }
        if (socket != null) {
            logger.debug("Close socket");
            IOUtils.closeQuietly(socket);
            socket = null;
        }
        logger.debug("Disconnected");
    }

    /**
     * Begins discovery of all remotes the LIRC server knows about.
     */
    public void startRemoteDiscovery() {
        sendCommand("LIST");
    }

    /**
     * Transmits the button press for the specified remote.
     *
     * @param remote
     *            Name of the remote
     * @param button
     *            Button to press
     */
    public void transmit(String remote, String button) {
        int timesToSend = 1;
        String[] parts = button.split(" ");
        if (parts.length > 1) {
            button = parts[0];
            timesToSend = Integer.parseInt(parts[1]);
        }
        transmit(remote, button, timesToSend);
    }

    /**
     * Transmits the button press for the specified remote
     *
     * @param remote
     *            Name of the remote
     * @param button
     *            Button to press
     * @param timesToSend
     *            Number of times to transmit the button
     */
    public void transmit(String remote, String button, int timesToSend) {
        // The last parameter is the number of times the command should be repeated.
        // For example, the command "SEND_ONCE TV KEY_VOLUMEUP 4" will transmit
        // the volume up code 5 times.
        sendCommand(String.format("SEND_ONCE %s %s %s", remote, button, timesToSend - 1));
    }

    private synchronized void sendCommand(String command) {
        outWriter.println(command);
        outWriter.flush();
    }

    /**
     * Sends a button pressed event to all listeners
     *
     * @param buttonEvent
     *            the button pressed
     */
    public synchronized void sendButtonToListeners(LIRCButtonEvent buttonEvent) {
        try {
            for (LIRCEventListener listener : listeners) {
                listener.buttonPressed(buttonEvent);
            }
        } catch (Exception e) {
            logger.error("Error invoking event listener", e);
        }
    }

    /**
     * Sends an error message to all listeners
     *
     * @param error
     *            error message to send
     */
    public synchronized void sendErrorToListeners(String error) {
        try {
            for (LIRCEventListener listener : listeners) {
                listener.errorOccured(error);
            }
        } catch (Exception e) {
            logger.error("Error invoking event listener", e);
        }
    }

    /**
     * Sends a LIRC message to all listeners
     *
     * @param message
     *            message to send
     */
    public synchronized void sendMessageToListeners(LIRCResponse message) {
        try {
            for (LIRCEventListener listener : listeners) {
                listener.messageReceived(message);
            }
        } catch (Exception e) {
            logger.error("Error invoking event listener", e);
        }
    }

}
