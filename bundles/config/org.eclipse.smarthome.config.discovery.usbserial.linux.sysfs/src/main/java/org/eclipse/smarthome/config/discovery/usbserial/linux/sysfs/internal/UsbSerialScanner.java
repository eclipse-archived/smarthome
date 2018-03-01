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
package org.eclipse.smarthome.config.discovery.usbserial.linux.sysfs.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.usbserial.UsbSerialDeviceInformation;

/**
 * Implementations of this interface scan for serial ports provided by USB devices.
 *
 * @author Henning Sudbrock - initial contribution
 */
@NonNullByDefault
public interface UsbSerialScanner {

    /**
     * Performs a single scan for serial ports provided by USB devices.
     *
     * @return A collection containing all scan results.
     * @throws Exception any checked exception that prevented the scan. Note that exceptions preventing the successful
     *             identification of a single USB device might be swallowed, simply skipping that device while still
     *             discovering other devices successfully.
     */
    Set<UsbSerialDeviceInformation> scan() throws Exception;

}
