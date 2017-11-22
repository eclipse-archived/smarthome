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
package org.eclipse.smarthome.binding.serialbutton.handler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.binding.serialbutton.SerialButtonBindingConstants;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.RXTXPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

/**
 * The {@link SerialButtonHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class SerialButtonHandler extends BaseThingHandler implements SerialPortEventListener {

    private final Logger logger = LoggerFactory.getLogger(SerialButtonHandler.class);

    private CommPortIdentifier portId;
    private RXTXPort serialPort;

    private InputStream inputStream;

    public SerialButtonHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        String port = (String) getConfig().get(SerialButtonBindingConstants.PARAMETER_CONFIG);
        if (port == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Port must be set!");
            return;
        }

        // parse ports and if the port is found, initialize the reader
        @SuppressWarnings("unchecked")
        Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            CommPortIdentifier id = portList.nextElement();
            if (id.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                if (id.getName().equals(port)) {
                    logger.debug("Serial port '{}' has been found.", port);
                    portId = id;
                }
            }
        }
        if (portId != null) {
            // initialize serial port
            try {
                serialPort = portId.open(getThing().getUID().toString(), 2000);
                serialPort.addEventListener(this);

                // activate the DATA_AVAILABLE notifier
                serialPort.notifyOnDataAvailable(true);
                inputStream = serialPort.getInputStream();

                updateStatus(ThingStatus.ONLINE);
            } catch (PortInUseException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Port is in use!");
            } catch (TooManyListenersException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        "Cannot attach listener to port!");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Port is not known!");
        }
    }

    @Override
    public void dispose() {
        if (serialPort != null) {
            serialPort.close();
        }
        IOUtils.closeQuietly(inputStream);
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPortEvent.DATA_AVAILABLE:
                // we get here if data has been received
                try {
                    do {
                        // read data from serial device
                        byte[] readBuffer = new byte[20];
                        while (inputStream.available() > 0) {
                            inputStream.read(readBuffer);
                        }
                        try {
                            // add wait states around reading the stream, so that interrupted transmissions are merged
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            // ignore interruption
                        }
                    } while (inputStream.available() > 0);

                    triggerChannel(SerialButtonBindingConstants.TRIGGER_CHANNEL, CommonTriggerEvents.PRESSED);
                } catch (IOException e1) {
                    logger.debug("Error reading from serial port: {}", e1.getMessage(), e1);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        // we do not have any state channels, so nothing to do here
    }
}
