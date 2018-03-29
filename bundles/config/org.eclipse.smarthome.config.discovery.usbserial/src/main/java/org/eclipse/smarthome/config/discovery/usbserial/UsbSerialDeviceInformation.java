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
package org.eclipse.smarthome.config.discovery.usbserial;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This is a data container for information about a USB device and the serial port that can be
 * used to access the device using a serial interface.
 * <p/>
 * It contains, on the one hand, information from the USB standard device descriptor, and, on the other hand, the
 * name of the serial port (for Linux, this would be, e.g., '/dev/ttyUSB0', for Windows, e.g., 'COM4').
 *
 * @author Henning Sudbrock - initial contribution
 */
@NonNullByDefault
public class UsbSerialDeviceInformation {

    private final int vendorId;
    private final int productId;

    @Nullable
    private final String serialNumber;
    @Nullable
    private final String manufacturer;
    @Nullable
    private final String product;

    private final String serialPort;

    public UsbSerialDeviceInformation(int vendorId, int productId, @Nullable String serialNumber,
            @Nullable String manufacturer, @Nullable String product, String serialPort) {
        this.vendorId = requireNonNull(vendorId);
        this.productId = requireNonNull(productId);

        this.serialNumber = serialNumber;
        this.manufacturer = manufacturer;
        this.product = product;

        this.serialPort = requireNonNull(serialPort);
    }

    /**
     * @return The vendor ID of the USB device.
     */
    public int getVendorId() {
        return vendorId;
    }

    /**
     * @return The product ID of the USB device.
     */
    public int getProductId() {
        return productId;
    }

    /**
     * @return The serial number of the USB device.
     */
    @Nullable
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * @return The manufacturer of the USB device.
     */
    @Nullable
    public String getManufacturer() {
        return manufacturer;
    }

    /**
     * @return The product description of the USB device.
     */
    @Nullable
    public String getProduct() {
        return product;
    }

    /**
     * @return The name of the serial port assigned to the USB device. Examples: /dev/ttyUSB1, COM4
     */
    public String getSerialPort() {
        return serialPort;
    }

    @SuppressWarnings("null")
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + vendorId;
        result = prime * result + productId;
        result = prime * result + serialPort.hashCode();
        result = prime * result + ((manufacturer == null) ? 0 : manufacturer.hashCode());
        result = prime * result + ((product == null) ? 0 : product.hashCode());
        result = prime * result + ((serialNumber == null) ? 0 : serialNumber.hashCode());
        return result;
    }

    @SuppressWarnings("null")
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        UsbSerialDeviceInformation other = (UsbSerialDeviceInformation) obj;

        if (vendorId != other.vendorId) {
            return false;
        }

        if (productId != other.productId) {
            return false;
        }

        if (!serialPort.equals(other.serialPort)) {
            return false;
        }

        if (manufacturer == null) {
            if (other.manufacturer != null) {
                return false;
            }
        } else if (!manufacturer.equals(other.manufacturer)) {
            return false;
        }

        if (product == null) {
            if (other.product != null) {
                return false;
            }
        } else if (!product.equals(other.product)) {
            return false;
        }

        if (serialNumber == null) {
            if (other.serialNumber != null) {
                return false;
            }
        } else if (!serialNumber.equals(other.serialNumber)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return String.format(
                "UsbSerialDeviceInformation [vendorId=0x%04X, productId=0x%04X, serialNumber=%s, manufacturer=%s, product=%s, serialPort=%s]",
                vendorId, productId, serialNumber, manufacturer, product, serialPort);
    }

}
