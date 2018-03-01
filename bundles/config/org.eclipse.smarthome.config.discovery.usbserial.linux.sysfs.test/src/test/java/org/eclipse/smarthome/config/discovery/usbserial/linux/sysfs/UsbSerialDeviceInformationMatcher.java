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
package org.eclipse.smarthome.config.discovery.usbserial.linux.sysfs;

import java.util.Objects;

import org.eclipse.smarthome.config.discovery.usbserial.UsbSerialDeviceInformation;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Hamcrest matcher for {@link UsbSerialDeviceInformation} objects.
 *
 * @author Henning Sudbrock - initial contribution
 */
public class UsbSerialDeviceInformationMatcher extends TypeSafeMatcher<UsbSerialDeviceInformation> {

    private final UsbSerialDeviceInformation other;

    public static UsbSerialDeviceInformationMatcher isUsbSerialDeviceInfo(int vendorId, int productId,
            String serialNumber, String manufacturer, String product, String serialPort) {
        return new UsbSerialDeviceInformationMatcher(
                new UsbSerialDeviceInformation(vendorId, productId, serialNumber, manufacturer, product, serialPort));
    }

    public static UsbSerialDeviceInformationMatcher isUsbSerialDeviceInfo(UsbSerialDeviceInformation usb) {
        return isUsbSerialDeviceInfo(usb.getVendorId(), usb.getProductId(), usb.getSerialNumber(),
                usb.getManufacturer(), usb.getProduct(), usb.getSerialPort());
    }

    private UsbSerialDeviceInformationMatcher(UsbSerialDeviceInformation other) {
        this.other = other;
    }

    @Override
    protected boolean matchesSafely(UsbSerialDeviceInformation deviceInfo) {
        if (deviceInfo == null) {
            return other == null;
        }

        return Objects.equals(deviceInfo, other)
                && Objects.equals(deviceInfo.getManufacturer(), other.getManufacturer())
                && Objects.equals(deviceInfo.getProduct(), other.getProduct())
                && Objects.equals(deviceInfo.getSerialNumber(), other.getSerialNumber());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("A UsbSerialDeviceInformation which is: ").appendValue(other);
    }

}
