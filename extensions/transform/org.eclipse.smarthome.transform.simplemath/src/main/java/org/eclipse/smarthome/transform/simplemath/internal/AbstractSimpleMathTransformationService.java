/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.transform.simplemath.internal;

import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * The implementation of {@link TransformationService} which
 *
 * @author Martin van Wingerden
 */
abstract class AbstractSimpleMathTransformationService implements TransformationService {
    private final Logger logger = LoggerFactory.getLogger(AbstractSimpleMathTransformationService.class);

    @Override
    public String transform(String valueString, String sourceString) throws TransformationException {
        BigDecimal source;
        BigDecimal value;
        try {
            source = new BigDecimal(sourceString);
            value = new BigDecimal(valueString);
        } catch (NumberFormatException e) {
            logger.info("Input value {} could not converted to a valid number", e);
            throw new TransformationException("Scale can only be used with numeric inputs");
        }

        return performCalculation(source, value).toString();
    }

    abstract BigDecimal performCalculation(BigDecimal source, BigDecimal value);
}
