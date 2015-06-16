/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.eclipse.smarthome.core.transform.internal.service;

import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.InvalidPathException;
 
 /**
  * <p>
  * The implementation of {@link TransformationService} which transforms the input by JSonPath Expressions.
  * </p>
  * 
  * @author Gaël L'hopital
  * 
  */
 public class JSonPathTransformationService implements TransformationService {

 	static final Logger logger = LoggerFactory.getLogger(JSonPathTransformationService.class);

    /**
     * Transforms the input <code>source</code> by JSonPath expression. 
     * 
     * @param jsonPathExpression
     *            the JSonPath expression to be used
     *            
     * @param source
     *            the input to transform
     */
 	public String transform(String jsonPathExpression, String source) throws TransformationException {

 		if (jsonPathExpression == null || source == null) {
 			throw new TransformationException("the given parameters 'JSonPath' and 'source' must not be null");
 		}

 		logger.debug("about to transform '{}' by the function '{}'", source, jsonPathExpression);

 		try {
 			Object transformationResult = JsonPath.read(source, jsonPathExpression);
 			logger.debug("transformation resulted in '{}'", transformationResult);
 			return (transformationResult != null) ? transformationResult.toString() : null;
 		} catch(InvalidPathException e) {
 			throw new TransformationException("An error occured while transforming JSON expression.", e);
 		} 

 	}

 }
 
 