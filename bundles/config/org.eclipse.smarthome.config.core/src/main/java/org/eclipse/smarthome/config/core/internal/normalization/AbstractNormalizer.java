/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.internal.normalization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common base class for all normalizers, doing the specific type conversion.
 *
 * @author Simon Kaufmann - initial contribution and API.
 * @author Thomas Höfer - renamed normalizer interface and added javadoc
 */
abstract class AbstractNormalizer implements Normalizer {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public final Object normalize(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String && "".equals(value)) {
            return "";
        }
        return doNormalize(value);
    }

    /**
     * Executes the concrete normalization of the given value.
     *
     * @param value the value to be normalized
     * @return the normalized value or the given value, if it was not possible to normalize it
     */
    abstract Object doNormalize(Object value);

}
