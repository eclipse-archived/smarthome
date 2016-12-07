/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal.protocol;

/**
 * Enumerates the LIFX products, their IDs and feature set.
 *
 * @see https://lan.developer.lifx.com/docs/lifx-products
 *
 * @author Wouter Born - Support LIFX 2016 product line-up and infrared functionality
 */
public enum Products {

    OR1000(1, 1, "Original 1000", true, false, false),
    C650(1, 3, "Color 650", true, false, false),
    W800LV(1, 10, "White 800 (Low Voltage)", false, false, false),
    W800HV(1, 11, "White 800 (High Voltage)", false, false, false),
    W900LV(1, 18, "White 900 BR30 (Low Voltage)", false, false, false),
    C900(1, 20, "Color 900 BR30", true, false, false),
    C1000(1, 22, "Color 1000", true, false, false),
    LA19(1, 27, "LIFX A19", true, false, false),
    LBR30(1, 28, "LIFX BR30", true, false, false),
    LPA19(1, 29, "LIFX+ A19", true, true, false),
    LPBR30(1, 30, "LIFX+ BR30", true, true, false),
    LZ(1, 31, "LIFX Z", true, false, true);

    private final long vendorID;
    private final long productID;
    private final String name;
    private final boolean color;
    private boolean infrared;
    private boolean multiZone;

    private Products(int vendorID, int productID, String name, boolean color, boolean infrared, boolean multiZone) {
        this.vendorID = vendorID;
        this.productID = productID;
        this.name = name;
        this.color = color;
        this.infrared = infrared;
        this.multiZone = multiZone;
    }

    @Override
    public String toString() {
        return name;
    }

    public long getVendor() {
        return vendorID;
    }

    public long getProduct() {
        return productID;
    }

    public String getName() {
        return name;
    }

    public boolean isColor() {
        return color;
    }

    public boolean isInfrared() {
        return infrared;
    }

    public boolean isMultiZone() {
        return multiZone;
    }

    public static Products getProductFromProductID(long id) throws IllegalArgumentException {

        for (Products c : Products.values()) {
            if (c.productID == id) {
                return c;
            }
        }

        throw new IllegalArgumentException(id + " is not a valid product ID.");
    }

}
