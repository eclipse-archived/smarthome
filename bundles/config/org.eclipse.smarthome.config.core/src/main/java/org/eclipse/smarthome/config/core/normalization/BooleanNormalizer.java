/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.normalization;

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

import com.google.common.base.Strings;

/**
 * The normalizer for {@link ConfigDescriptionParameter.Type#BOOLEAN}. It tries to convert the given value into a
 * {@link Boolean} object.
 * <p>
 * Therefore it considers numbers (0/1 and their {@link String} representations) as well as {@link Strings}, containing
 * apart from the typical "true"/"false" also other values like "yes"/"no", "on"/"off".
 *
 * @author Simon Kaufmann - initial contribution and API.
 * @author Thomas Höfer - made class final and minor javadoc changes
 */
final class BooleanNormalizer extends AbstractNormalizer {

    @Override
    public Object doNormalize(Object value) {
        if (value instanceof Boolean) {
            return value;
        }
        if (value instanceof Byte) {
            return handleNumeric(((Byte) value).longValue());
        }
        if (value instanceof Integer) {
            return handleNumeric(((Integer) value).longValue());
        }
        if (value instanceof Long) {
            return handleNumeric((Long) value);
        }
        String s = value.toString();
        if ("true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s) || "on".equalsIgnoreCase(s)
                || "1".equalsIgnoreCase(s)) {
            return true;
        } else if ("false".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s) || "off".equalsIgnoreCase(s)
                || "0".equalsIgnoreCase(s)) {
            return false;
        }
        logger.trace("Class \"{}\" cannot be converted to boolean.", value.getClass().getName());
        return value;
    }

    private Object handleNumeric(long numeric) {
        if (numeric == 1) {
            return true;
        } else if (numeric == 0) {
            return false;
        } else {
            logger.trace("\"{}\" cannot be interpreted as a boolean.", numeric);
            return numeric;
        }
    }

}
