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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.rxtx.RxTxSerialPort;

import gnu.io.rfc2217.TelnetSerialPort;

/**
 * Specific serial port identifier implementation for RFC2217.
 *
 * @author atthias Steigenberger - Initial contribution
 */
@NonNullByDefault
public class SerialPortIdentifierImpl implements SerialPortIdentifier {

    final TelnetSerialPort id;
    private URI uri;

    /**
     * Constructor.
     *
     * @param id the underlying comm port identifier implementation
     */
    public SerialPortIdentifierImpl(final TelnetSerialPort id, URI uri) {
        this.id = id;
        this.uri = uri;
    }

    @Override
    public String getName() {
        final String name = id.getName();
        return name != null ? name : "";
    }

    @Override
    public SerialPort open(String owner, int timeout) throws PortInUseException {
        try {
            id.getTelnetClient().setDefaultTimeout(timeout);
            id.getTelnetClient().connect(uri.getHost(), uri.getPort());
            return new RxTxSerialPort(id);
        } catch (Exception e) {
            throw new IllegalStateException(
                    String.format("Unable to establish remote connection to serial port %s", uri), e);
        }
    }

}
