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
package org.eclipse.smarthome.io.transport.serial;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface of a serial port identifier.
 *
 * @author Markus Rathgeb - Initial contribution
 */
@NonNullByDefault
public interface SerialPortIdentifier {
    /**
     * Gets the name of the port.
     *
     * @return the port's name
     */
    String getName();

    /**
     * Opens a serial port for communicating.
     *
     * @param owner name of the owner that port should be assigned to
     * @param timeout time in milliseconds to block waiting for opening the port
     * @return a serial port
     */
    SerialPort open(String owner, int timeout) throws PortInUseException;
}
