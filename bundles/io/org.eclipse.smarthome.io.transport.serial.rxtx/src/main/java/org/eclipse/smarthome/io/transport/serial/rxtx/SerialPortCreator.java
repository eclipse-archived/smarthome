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
package org.eclipse.smarthome.io.transport.serial.rxtx;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.UnsupportedCommOperationException;

import gnu.io.NoSuchPortException;

/**
 *
 * @author MatthiasS
 *
 * @param <T>
 */
@NonNullByDefault
public interface SerialPortCreator<T> {

    final static String LOCAL = "local";

    /**
     * Gets whether this {@link SerialPortCreator} is applicable to create the given port.
     *
     * @param portName The ports name.
     * @return Whether the port can be created and opened by this creator.
     */
    public boolean isApplicable(String portName, Class<T> expectedClass);

    /**
     * Gets the {@link SerialPortIdentifier} if it is available or null otherwise.
     *
     * @param portName The ports name.
     * @return The created {@link SerialPort}.
     * @throws NoSuchPortException If the serial port does not exist.
     * @throws UnsupportedCommOperationException
     * @throws PortInUseException
     */
    public @Nullable SerialPortIdentifier getPortIdentifier(String portName);

    /**
     * Gets the protocol type of the Port to create.
     *
     * @return The protocol type.
     */
    public String getProtocol();

    /**
     * Gets all the available {@link SerialPortIdentifier}s for this {@link SerialPortCreator}.
     * Please note: Discovery is not available necessarily
     *
     * @return The available ports
     */
    public Stream<SerialPortIdentifier> getSerialPortIdentifiers();
}
