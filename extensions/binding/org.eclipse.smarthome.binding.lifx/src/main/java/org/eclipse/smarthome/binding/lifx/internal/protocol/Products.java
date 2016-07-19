/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal.protocol;

public enum Products {

    OR1000(1, 1, "Original 1000", true),
    C650(1, 3, "Color 650", true),
    W800LV(1, 10, "White 800 (Low Voltage)", false),
    W800HV(1, 11, "White 800 (High Voltage)", false),
    W900LV(1, 18, "White 900 BR30 (Low Voltage)", false),
    C900(1, 20, "Color 900 BR30", true),
    C1000(1, 22, "Color 1000", true);

    private final long vendorID;
    private final long productID;
    private final String name;
    private final boolean color;

    private Products(int vendorID, int productID, String name, boolean color) {
        this.vendorID = vendorID;
        this.productID = productID;
        this.name = name;
        this.color = color;
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

    public static Products getProductFromProductID(long id) throws IllegalArgumentException {

        for (Products c : Products.values()) {
            if (c.productID == id) {
                return c;
            }
        }

        throw new IllegalArgumentException(id + " is not a valid product ID.");
    }

}
