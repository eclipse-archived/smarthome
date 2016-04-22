/**
 * Copyright (c) 2016 Deutsche Telekom AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.normalization;

/**
 * Normalizer for the Text type.
 * 
 * It basically ensures that the given value will turned into its {@link String} representation.
 * 
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
class TextNormalizer extends AbstractNormalizer {

    @Override
    public Object doNormalize(Object value) {
        if (value == null) {
            return value;
        }
        if (value instanceof String) {
            return value;
        }
        return value.toString();
    }

}
