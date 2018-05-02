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
package org.eclipse.smarthome.io.transport.serial.rxtx.rfc2217.internal;

import java.net.URI;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.rxtx.SerialPortCreator;
import org.osgi.service.component.annotations.Component;

import gnu.io.NoSuchPortException;
import gnu.io.UnsupportedCommOperationException;
import gnu.io.rfc2217.TelnetSerialPort;

/**
 *
 * @author MatthiasS
 *
 */
@NonNullByDefault
@Component(service = SerialPortCreator.class)
public class RFC2217PortCreator implements SerialPortCreator<TelnetSerialPort> {

    private final static String PROTOCOL = "rfc2217";

    @Override
    public boolean isApplicable(String portName, Class<TelnetSerialPort> expectedClass) {
        try {
            if (expectedClass.isAssignableFrom(TelnetSerialPort.class)) {
                URI uri = URI.create(portName);
                return uri.getScheme().equalsIgnoreCase(PROTOCOL);
            }
            return false;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * @throws UnsupportedCommOperationException if connection to the remote serial port fails.
     * @throws NoSuchPortException if the host does not exist.
     */
    @Override
    public @Nullable SerialPortIdentifier getPortIdentifier(String portName) {
        TelnetSerialPort telnetSerialPort = new TelnetSerialPort();
        telnetSerialPort.setName(portName);
        return new SerialPortIdentifierImpl(telnetSerialPort);
    }

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public Stream<SerialPortIdentifier> getSerialPortIdentifiers() {
        return Stream.empty();
    }

}
