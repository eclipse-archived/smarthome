/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.normalization;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * The normalizer for {@link ConfigDescriptionParameter.Type#INTEGER}. All different number formats will get converted
 * to BigDecimal, not allowing any fractions. Also, {@link String}s will be converted if possible.
 *
 * @author Simon Kaufmann - initial contribution and API.
 * @author Thomas Höfer - made class final and minor javadoc changes
 */
final class IntNormalizer extends AbstractNormalizer {

    @Override
    public Object doNormalize(Object value) {
        try {
            if (value instanceof BigDecimal) {
                return ((BigDecimal) value).setScale(0, RoundingMode.UNNECESSARY);
            }
            if (value instanceof Byte) {
                return new BigDecimal((Byte) value);
            }
            if (value instanceof Integer) {
                return new BigDecimal((Integer) value);
            }
            if (value instanceof Long) {
                return BigDecimal.valueOf((Long) value);
            }
            if (value instanceof String) {
                return new BigDecimal((String) value).setScale(0, RoundingMode.UNNECESSARY);
            }
            if (value instanceof Float) {
                return new BigDecimal(((Float) value).toString()).setScale(0, RoundingMode.UNNECESSARY);
            }
            if (value instanceof Double) {
                return BigDecimal.valueOf((Double) value).setScale(0, RoundingMode.UNNECESSARY);
            }
        } catch (ArithmeticException | NumberFormatException e) {
            logger.trace("\"{}\" is not a valid integer number.", e, value);
            return value;
        }
        logger.trace("Class \"{}\" cannot be converted to an integer number.", value.getClass().getName());
        return value;
    }

}
