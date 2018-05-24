/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
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

import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

/**
 * The static methods of this class are made available as functions in the scripts.
 * This allows a script to use jsonpath features.
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
public class JSonPath {

    public static Object jsonpath(String jsonPathExpression, String jsonString) {
        if (jsonPathExpression == null || jsonString == null) {
            throw new RuntimeException("the given parameters 'jsonPathExpression' and 'jsonString' must not be null");
        }
        try {
            return JsonPath.read(jsonString, jsonPathExpression);
        } catch (PathNotFoundException e) {
            throw new RuntimeException("Invalid path '" + jsonPathExpression + "' in '" + jsonString + "'");
        } catch (InvalidPathException | InvalidJsonException e) {
            throw new RuntimeException(
                    "An error occurred while transforming JSON expression: " + e.getLocalizedMessage(), e);
        }
    }

}
