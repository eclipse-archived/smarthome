/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.transform.jsonpath.internal;

import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

/**
 * <p>
 * The implementation of {@link TransformationService} which transforms the input by JSonPath Expressions.
 *
 * @author Gaël L'hopital
 * @author Sebastian Janzen
 *
 */
public class JSonPathTransformationService implements TransformationService {

    private final Logger logger = LoggerFactory.getLogger(JSonPathTransformationService.class);

    /**
     * Transforms the input <code>source</code> by JSonPath expression.
     *
     * @param function JsonPath expression
     * @param source String which contains JSON
     * @throws TransformationException If the JsonPath expression is invalid, a {@link InvalidPathException} is thrown,
     *             which is encapsulated in a {@link TransformationException}.
     */
    @Override
    public String transform(String jsonPathExpression, String source) throws TransformationException {

        if (jsonPathExpression == null || source == null) {
            throw new TransformationException("the given parameters 'JSonPath' and 'source' must not be null");
        }

        logger.debug("about to transform '{}' by the function '{}'", source, jsonPathExpression);

        try {
            Object transformationResult = JsonPath.read(source, jsonPathExpression);
            logger.debug("transformation resulted in '{}'", transformationResult);
            return (transformationResult != null) ? transformationResult.toString() : source;
        } catch (PathNotFoundException e) {
            throw new TransformationException("Invalid path '" + jsonPathExpression + "' in '" + source + "'");
        } catch (InvalidPathException | InvalidJsonException e) {
            throw new TransformationException("An error occurred while transforming JSON expression.", e);
        }

    }

}
