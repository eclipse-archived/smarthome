/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.util;

import javax.measure.Quantity;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A static helper which parses Strings denoting a dimension (e.g. Length, Temperature, Mass,..) into a {@link Class}
 * instance of an interface from {@link javax.measure.Quantity}.
 *
 * @author Henning Treu - initial contribution
 *
 */
public class DimensionClassParser {

    private static final String JAVAX_MEASURE_QUANTITY = "javax.measure.quantity.";

    private DimensionClassParser() {
        // avoid instantiation
    }

    /**
     * Parses a String denoting a dimension (e.g. Length, Temperature, Mass,..) into a {@link Class} instance of an
     * interface from {@link javax.measure.Quantity}. The interfaces reside in {@code javax.measure.quantity}.
     *
     * @param dimension the simple name of an interface from the package {@code javax.measure.quantity}.
     * @return the {@link Class} instance of the interface or {@code null} if the given dimension is blank or parsing
     *         failed due to {@link ClassNotFoundException}.
     */
    public static Class<Quantity<?>> parseDimension(String dimension) {
        if (StringUtils.isBlank(dimension)) {
            return null;
        }

        try {
            @SuppressWarnings("unchecked")
            Class<Quantity<?>> dimensionClass = (Class<Quantity<?>>) Class.forName(JAVAX_MEASURE_QUANTITY + dimension);
            return dimensionClass;
        } catch (ClassNotFoundException e) {
            Logger logger = LoggerFactory.getLogger(DimensionClassParser.class);
            logger.error("Error creating a Class instance for name '{}'.", dimension);
        }

        return null;
    }
}
