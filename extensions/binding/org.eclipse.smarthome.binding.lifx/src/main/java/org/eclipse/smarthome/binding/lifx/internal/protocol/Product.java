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
package org.eclipse.smarthome.binding.lifx.internal.protocol;

import static org.eclipse.smarthome.binding.lifx.internal.protocol.Product.Feature.*;
import static org.eclipse.smarthome.binding.lifx.internal.protocol.Product.TemperatureRange.*;
import static org.eclipse.smarthome.binding.lifx.internal.protocol.Product.Vendor.LIFX;

import java.util.Arrays;
import java.util.EnumSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.lifx.LifxBindingConstants;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * Enumerates the LIFX products, their IDs and feature set.
 *
 * @see https://lan.developer.lifx.com/docs/lifx-products
 *
 * @author Wouter Born - Support LIFX 2016 product line-up and infrared functionality
 * @author Wouter Born - Add temperature ranges and simplify feature definitions
 */
@NonNullByDefault
public enum Product {

    PRODUCT_1(1, "Original 1000", TR_2500_9000, COLOR),
    PRODUCT_3(3, "Color 650", TR_2500_9000, COLOR),
    PRODUCT_10(10, "White 800 (Low Voltage)", TR_2700_6500),
    PRODUCT_11(11, "White 800 (High Voltage)", TR_2700_6500),
    PRODUCT_18(18, "White 900 BR30 (Low Voltage)", TR_2700_6500),
    PRODUCT_20(20, "Color 1000 BR30", TR_2500_9000, COLOR),
    PRODUCT_22(22, "Color 1000", TR_2500_9000, COLOR),
    PRODUCT_27(27, "LIFX A19", TR_2500_9000, COLOR),
    PRODUCT_28(28, "LIFX BR30", TR_2500_9000, COLOR),
    PRODUCT_29(29, "LIFX+ A19", TR_2500_9000, COLOR, INFRARED),
    PRODUCT_30(30, "LIFX+ BR30", TR_2500_9000, COLOR, INFRARED),
    PRODUCT_31(31, "LIFX Z", TR_2500_9000, COLOR, MULTIZONE),
    PRODUCT_32(32, "LIFX Z 2", TR_2500_9000, COLOR, MULTIZONE),
    PRODUCT_36(36, "LIFX Downlight", TR_2500_9000, COLOR),
    PRODUCT_37(37, "LIFX Downlight", TR_2500_9000, COLOR),
    PRODUCT_38(38, "LIFX Beam", TR_2500_9000, COLOR, MULTIZONE),
    PRODUCT_43(43, "LIFX A19", TR_2500_9000, COLOR),
    PRODUCT_44(44, "LIFX BR30", TR_2500_9000, COLOR),
    PRODUCT_45(45, "LIFX+ A19", TR_2500_9000, COLOR, INFRARED),
    PRODUCT_46(46, "LIFX+ BR30", TR_2500_9000, COLOR, INFRARED),
    PRODUCT_49(49, "LIFX Mini", TR_2500_9000, COLOR),
    PRODUCT_50(50, "LIFX Mini Day and Dusk", TR_1500_4000),
    PRODUCT_51(51, "LIFX Mini White", TR_2700_2700),
    PRODUCT_52(52, "LIFX GU10", TR_2500_9000, COLOR),
    PRODUCT_55(55, "LIFX Tile", TR_2500_9000, CHAIN, COLOR),
    PRODUCT_56(56, "LIFX Beam", TR_2500_9000, COLOR, MULTIZONE),
    PRODUCT_59(59, "LIFX Mini Color", TR_2500_9000, COLOR),
    PRODUCT_60(60, "LIFX Mini Day and Dusk", TR_1500_4000),
    PRODUCT_61(61, "LIFX Mini White", TR_2700_2700);

    /**
     * Enumerates the product features.
     */
    public enum Feature {
        CHAIN,
        COLOR,
        INFRARED,
        MULTIZONE,
    }

    /**
     * Enumerates the product vendors.
     */
    public enum Vendor {
        LIFX(1, "LIFX");

        private final int id;
        private final String name;

        Vendor(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getID() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Enumerates the color temperature ranges of lights.
     */
    public enum TemperatureRange {
        /**
         * 1500-4000K
         */
        TR_1500_4000(1500, 4000),

        /**
         * 2500-9000K
         */
        TR_2500_9000(2500, 9000),

        /**
         * 2700-2700K
         */
        TR_2700_2700(2700, 2700),

        /**
         * 2700-6500K
         */
        TR_2700_6500(2700, 6500);

        private final int minimum;
        private final int maximum;

        TemperatureRange(int minimum, int maximum) {
            this.minimum = minimum;
            this.maximum = maximum;
        }

        /**
         * The minimum color temperature in degrees Kelvin.
         *
         * @return minimum color temperature (K)
         */
        public int getMinimum() {
            return minimum;
        }

        /**
         * The maxiumum color temperature in degrees Kelvin.
         *
         * @return maximum color temperature (K)
         */
        public int getMaximum() {
            return maximum;
        }

        /**
         * The color temperature range in degrees Kelvin.
         *
         * @return difference between maximum and minimum color temperature values
         */
        public int getRange() {
            return maximum - minimum;
        }
    }

    private final Vendor vendor;
    private final long id;
    private final String name;
    private final TemperatureRange temperatureRange;
    private final EnumSet<Feature> features = EnumSet.noneOf(Feature.class);

    private Product(long id, String name, TemperatureRange temperatureRange) {
        this(LIFX, id, name, temperatureRange);
    }

    private Product(long id, String name, TemperatureRange temperatureRange, Feature... features) {
        this(LIFX, id, name, temperatureRange, features);
    }

    private Product(Vendor vendor, long id, String name, TemperatureRange temperatureRange) {
        this(vendor, id, name, temperatureRange, new Feature[0]);
    }

    private Product(Vendor vendor, long id, String name, TemperatureRange temperatureRange, Feature... features) {
        this.vendor = vendor;
        this.id = id;
        this.name = name;
        this.temperatureRange = temperatureRange;
        this.features.addAll(Arrays.asList(features));
    }

    @Override
    public String toString() {
        return name;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public long getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TemperatureRange getTemperatureRange() {
        return temperatureRange;
    }

    public ThingTypeUID getThingTypeUID() {
        if (hasFeature(COLOR)) {
            if (hasFeature(INFRARED)) {
                return LifxBindingConstants.THING_TYPE_COLORIRLIGHT;
            } else if (hasFeature(MULTIZONE)) {
                return LifxBindingConstants.THING_TYPE_COLORMZLIGHT;
            } else {
                return LifxBindingConstants.THING_TYPE_COLORLIGHT;
            }
        } else {
            return LifxBindingConstants.THING_TYPE_WHITELIGHT;
        }
    }

    public boolean hasFeature(Feature feature) {
        return features.contains(feature);
    }

    /**
     * Returns a product that has the given thing type UID.
     *
     * @param uid a thing type UID
     * @return a product that has the given thing type UID
     * @throws IllegalArgumentException when <code>uid</code> is not a valid LIFX thing type UID
     */
    public static Product getLikelyProduct(ThingTypeUID uid) throws IllegalArgumentException {
        for (Product product : Product.values()) {
            if (product.getThingTypeUID().equals(uid)) {
                return product;
            }
        }

        throw new IllegalArgumentException(uid + " is not a valid product thing type UID");
    }

    /**
     * Returns the product that has the given product ID.
     *
     * @param id the product ID
     * @return the product that has the given product ID
     * @throws IllegalArgumentException when <code>id</code> is not a valid LIFX product ID
     */
    public static Product getProductFromProductID(long id) throws IllegalArgumentException {
        for (Product product : Product.values()) {
            if (product.id == id) {
                return product;
            }
        }

        throw new IllegalArgumentException(id + " is not a valid product ID");
    }

}
