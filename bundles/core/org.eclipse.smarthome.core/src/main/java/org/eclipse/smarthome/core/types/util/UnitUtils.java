/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.types.util;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tec.uom.se.quantity.Quantities;

/**
 * A utility for parsing dimensions to interface classes of {@link Quantity} and parsing units from format strings.
 *
 * @author Henning Treu - initial contribution
 *
 */
@NonNullByDefault
public class UnitUtils {

    public static final String UNIT_PLACEHOLDER = "%unit%";

    private static final Logger logger = LoggerFactory.getLogger(UnitUtils.class);

    private static final String JAVAX_MEASURE_QUANTITY = "javax.measure.quantity.";

    /**
     * Parses a String denoting a dimension (e.g. Length, Temperature, Mass,..) into a {@link Class} instance of an
     * interface from {@link javax.measure.Quantity}. The interfaces reside in {@code javax.measure.quantity}.
     *
     * @param dimension the simple name of an interface from the package {@code javax.measure.quantity}.
     * @return the {@link Class} instance of the interface or {@code null} if the given dimension is blank or parsing
     *         failed due to {@link ClassNotFoundException}.
     */
    public static @Nullable Class<? extends Quantity<?>> parseDimension(String dimension) {
        if (StringUtils.isBlank(dimension)) {
            return null;
        }

        try {
            @SuppressWarnings("unchecked")
            Class<? extends Quantity<?>> dimensionClass = (Class<? extends Quantity<?>>) Class
                    .forName(JAVAX_MEASURE_QUANTITY + dimension);
            return dimensionClass;
        } catch (ClassNotFoundException e) {
            logger.error("Error creating a Class instance for name '{}'.", dimension);
        }

        return null;
    }

    /**
     * A utility method to parse a unit symbol from a given pattern (like stateDescription or widget label).
     * The unit is always expected to be the last part of the pattern separated by " " (e.g. "%.2f °C" for °C).
     *
     * @param pattern The pattern to extract the unit symbol from.
     * @return the unit symbol extracted from the pattern or {@code null} if the pattern did not match the expected
     *         format.
     */
    public static @Nullable Unit<?> parseUnit(String pattern) {
        if (StringUtils.isBlank(pattern)) {
            return null;
        }

        int lastBlankIndex = pattern.lastIndexOf(" ");
        if (lastBlankIndex < 0) {
            return null;
        }

        String unitSymbol = pattern.substring(lastBlankIndex).trim();
        if (StringUtils.isNotBlank(unitSymbol) && !unitSymbol.equals(UNIT_PLACEHOLDER)) {
            try {
                Quantity<?> quantity = Quantities.getQuantity("1 " + unitSymbol);
                return quantity.getUnit();
            } catch (IllegalArgumentException e) {
                // we expect this exception in case the extracted string does not match any known unit
                logger.warn("Unknown unit from pattern: {}", unitSymbol);
            }
        }

        return null;
    }
}
