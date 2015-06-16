package org.eclipse.smarthome.core.transform.internal.service;

import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.InvalidCriteriaException;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

/**
 * The implementation of {@link TransformationService} which extracts values from 
 * input json string by a JsonPath expression.
 *
 * @author Sebastian Janzen
 */
public class JsonPathTransformationService implements TransformationService {

	/**
	 * @param function JsonPath expression
	 * @param source String which contains JSON
	 * @throws TransformationException If the JsonPath expression is invalid, a {@link InvalidPathException} is thrown, 
	 * which is encapsulated in a {@link TransformationException}. 
	 */
	@Override
	public String transform(String function, String source) throws TransformationException {
		try {
			DocumentContext data = JsonPath.parse(source);
			return data.read(function, String.class);
		} catch (PathNotFoundException e) {
			return null;
		} catch (InvalidCriteriaException | InvalidPathException e) {
			throw new TransformationException("Invalid JSON Path: " + function, e);
		}
	}

}
