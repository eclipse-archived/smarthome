/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.internal.normalization;

import java.math.BigDecimal;

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * The normalizer for {@link ConfigDescriptionParameter.Type#DECIMAL}. It converts all number types to BigDecimal,
 * having at least one digit after the floating point. Also {@link String}s are converted if possible.
 *
 * @author Simon Kaufmann - initial contribution and API.
 * @author Thomas Höfer - made class final and minor javadoc changes
 */
final class DecimalNormalizer extends AbstractNormalizer {

    @Override
    public Object doNormalize(Object value) {
        try {
            if (value instanceof BigDecimal) {
                return stripTrailingZeros((BigDecimal) value);
            }
            if (value instanceof String) {
                return stripTrailingZeros(new BigDecimal((String) value));
            }
            if (value instanceof Byte) {
                return new BigDecimal((Byte) value).setScale(1);
            }
            if (value instanceof Integer) {
                return new BigDecimal((Integer) value).setScale(1);
            }
            if (value instanceof Long) {
                return new BigDecimal((Long) value).setScale(1);
            }
            if (value instanceof Float) {
                return new BigDecimal(((Float) value).toString());
            }
            if (value instanceof Double) {
                return BigDecimal.valueOf((Double) value);
            }
        } catch (ArithmeticException | NumberFormatException e) {
            logger.trace("\"{}\" is not a valid decimal number.", e, value);
            return value;
        }
        logger.trace("Class \"{}\" cannot be converted to a decimal number.", value.getClass().getName());
        return value;
    }

    private BigDecimal stripTrailingZeros(BigDecimal value) {
        BigDecimal ret = new BigDecimal(value.stripTrailingZeros().toPlainString());
        if (ret.scale() == 0) {
            ret = ret.setScale(1);
        }
        return ret;
    }

}
