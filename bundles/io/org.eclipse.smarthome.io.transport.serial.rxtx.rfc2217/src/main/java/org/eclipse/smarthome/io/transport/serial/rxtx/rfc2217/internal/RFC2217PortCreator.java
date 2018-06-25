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
import org.eclipse.smarthome.io.transport.serial.ProtocolType;
import org.eclipse.smarthome.io.transport.serial.ProtocolType.PathType;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortProvider;
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
@Component(service = SerialPortProvider.class)
public class RFC2217PortCreator implements SerialPortProvider<TelnetSerialPort> {

    private final static String PROTOCOL = "rfc2217";

    /**
     * @throws UnsupportedCommOperationException if connection to the remote serial port fails.
     * @throws NoSuchPortException if the host does not exist.
     */
    @Override
    public @Nullable SerialPortIdentifier getPortIdentifier(URI portName) {
        TelnetSerialPort telnetSerialPort = new TelnetSerialPort();
        telnetSerialPort.setName(portName.toString());
        return new SerialPortIdentifierImpl(telnetSerialPort, portName);
    }

    @Override
    public Stream<ProtocolType> getAcceptedProtocols() {
        return Stream.of(new ProtocolType(PathType.NET, PROTOCOL));
    }

    @Override
    public Stream<SerialPortIdentifier> getSerialPortIdentifiers() {
        return Stream.empty();
    }

}
