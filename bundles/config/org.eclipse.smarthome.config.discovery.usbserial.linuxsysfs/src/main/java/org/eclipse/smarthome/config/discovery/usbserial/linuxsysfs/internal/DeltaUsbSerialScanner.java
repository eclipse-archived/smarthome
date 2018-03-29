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
package org.eclipse.smarthome.config.discovery.usbserial.linuxsysfs.internal;

import static java.util.stream.Collectors.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.usbserial.UsbSerialDeviceInformation;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * Permits to perform repeated scans for USB devices with associated serial port. Keeps the last scan result as internal
 * state, for detecting which devices were added, as well as which devices were removed.
 *
 * @author Henning Sudbrock - initial contribution
 */
@NonNullByDefault
public class DeltaUsbSerialScanner {

    private Collection<UsbSerialDeviceInformation> lastScanResult = Sets.newHashSet();

    private final UsbSerialScanner usbSerialScanner;

    public DeltaUsbSerialScanner(UsbSerialScanner usbSerialScanner) {
        this.usbSerialScanner = usbSerialScanner;
    }

    /**
     * Scans for usb-serial devices, and returns the delta to the last scan result.
     * <p/>
     * This method is synchronized to prevent multiple parallel invocations of this method that could bring the value of
     * lastScanResult into an inconsistent state.
     *
     * @return The delta to the last scan result.
     * @throws IOException if the scan using the {@link UsbSerialScanner} throws an IOException.
     */
    public synchronized Delta<UsbSerialDeviceInformation> scan() throws IOException {
        Set<UsbSerialDeviceInformation> deviceInfos = usbSerialScanner.scan();

        Set<UsbSerialDeviceInformation> added = getAddedDeviceInfos(deviceInfos);
        Set<UsbSerialDeviceInformation> removed = getRemovedDeviceInfos(deviceInfos);
        Set<UsbSerialDeviceInformation> unchanged = Sets.difference(deviceInfos, added);

        lastScanResult = deviceInfos;

        return new Delta<>(added, removed, unchanged);
    }

    private ImmutableSet<UsbSerialDeviceInformation> getAddedDeviceInfos(
            Collection<UsbSerialDeviceInformation> deviceInfos) {
        return deviceInfos.stream().filter(deviceInfo -> !lastScanResult.contains(deviceInfo))
                .collect(collectingAndThen(toSet(), ImmutableSet::copyOf));
    }

    private ImmutableSet<UsbSerialDeviceInformation> getRemovedDeviceInfos(
            Collection<UsbSerialDeviceInformation> deviceInfos) {
        return lastScanResult.stream().filter(deviceInfo -> !deviceInfos.contains(deviceInfo))
                .collect(collectingAndThen(toSet(), ImmutableSet::copyOf));
    }

    /**
     * Delta between two subsequent scan results.
     */
    class Delta<T> {

        private final Set<T> added;
        private final Set<T> removed;
        private final Set<T> unchanged;

        public Delta(Set<T> added, Set<T> removed, Set<T> unchanged) {
            this.added = added;
            this.removed = removed;
            this.unchanged = unchanged;
        }

        public Set<T> getAdded() {
            return added;
        }

        public Set<T> getRemoved() {
            return removed;
        }

        public Set<T> getUnchanged() {
            return unchanged;
        }
    }

}
