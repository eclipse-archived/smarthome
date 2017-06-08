/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.transform.simplemath.internal;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of {@link TransformationService} which
 *
 * @author Martin van Wingerden
 */
abstract class AbstractSimpleMathTransformationService implements TransformationService {
    private final Logger logger = LoggerFactory.getLogger(AbstractSimpleMathTransformationService.class);

    private static final Pattern NUMBER_PATTERN = Pattern.compile(".*?(-?((0)|([1-9][0-9]*))(\\.[0-9]*)?).*?");

    @Override
    public String transform(String valueString, String sourceString) throws TransformationException {
        BigDecimal source;
        String extractedNumericString = extractNumericString(sourceString);
        try {
            source = new BigDecimal(extractedNumericString);
        } catch (NumberFormatException e) {
            logger.info("Input value {} could not converted to a valid number", extractedNumericString);
            throw new TransformationException("SimpleMath can only be used with numeric inputs");
        }
        BigDecimal value;
        try {
            value = new BigDecimal(extractNumericString(valueString));
        } catch (NumberFormatException e) {
            logger.info("Input value {} could not converted to a valid number", extractNumericString(valueString));
            throw new TransformationException("SimpleMath can only be used with numeric inputs");
        }

        String result = performCalculation(source, value).toString();
        return sourceString.replace(extractedNumericString, result);
    }

    private String extractNumericString(String sourceString) throws TransformationException {
        Matcher matcher = NUMBER_PATTERN.matcher(sourceString);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            throw new TransformationException("SimpleMath can only be used with numeric inputs");
        }
    }

    abstract BigDecimal performCalculation(BigDecimal source, BigDecimal value);
}
