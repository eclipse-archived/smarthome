/**
 * Copyright (c) 2016 Deutsche Telekom AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.normalization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common base class for all normalizers, doing the specific type conversion.
 *
 * @author Simon Kaufmann - initial contribution and API.
 */
abstract class AbstractNormalizer implements INormalizer {

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

    abstract Object doNormalize(Object value);

}
