/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.transform;

import java.util.Collection;
import java.util.IllegalFormatException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class TransformationHelper {

    private final static Logger logger = LoggerFactory.getLogger(TransformationHelper.class);

    /* RegEx to extract and parse a function String <code>'(.*?)\((.*)\):(.*)'</code> */
    protected static final Pattern EXTRACT_TRANSFORMFUNCTION_PATTERN = Pattern.compile("(.*?)\\((.*)\\):(.*)");

    /**
     * determines whether a pattern refers to a transformation service
     *
     * @param pattern the pattern to check
     * @return true, if the pattern contains a transformation
     */
    static public boolean isTransform(String pattern) {
        return EXTRACT_TRANSFORMFUNCTION_PATTERN.matcher(pattern).matches();
    }

    /**
     * Queries the OSGi service registry for a service that provides a transformation service of
     * a given transformation type (e.g. REGEX, XSLT, etc.)
     *
     * @param transformationType the desired transformation type
     * @return a service instance or null, if none could be found
     */
    static public TransformationService getTransformationService(BundleContext context, String transformationType) {
        if (context != null) {
            Logger logger = LoggerFactory.getLogger(TransformationHelper.class);
            String filter = "(smarthome.transform=" + transformationType + ")";
            try {
                Collection<ServiceReference<TransformationService>> refs = context
                        .getServiceReferences(TransformationService.class, filter);
                if (refs != null && refs.size() > 0) {
                    return context.getService(refs.iterator().next());
                } else {
                    logger.warn("Cannot get service reference for transformation service of type {}",
                            transformationType);
                }
            } catch (InvalidSyntaxException e) {
                logger.warn("Cannot get service reference for transformation service of type {}", transformationType,
                        e);
            }
        }
        return null;
    }

    /**
     * Transforms a state string using transformation functions within a given pattern.
     *
     * @param context a valid bundle context, required for accessing the services
     * @param stateDescPattern the pattern that contains the transformation instructions
     * @param state the state to be formatted before being passed into the transformation function
     * @return the result of the transformation. If no transformation was done, the state is returned
     */
    public static String transform(BundleContext context, String stateDescPattern, String state) {
        Matcher matcher = EXTRACT_TRANSFORMFUNCTION_PATTERN.matcher(stateDescPattern);
        if (matcher.find()) {
            String type = matcher.group(1);
            String pattern = matcher.group(2);
            String value = matcher.group(3);
            TransformationService transformation = TransformationHelper.getTransformationService(context, type);
            if (transformation != null) {
                try {
                    value = String.format(value, state);
                    try {
                        pattern = transformation.transform(pattern, value);
                    } catch (TransformationException e) {
                        logger.warn("Transformation '{}' with value '{}' failed: {}", transformation, value,
                                e.getMessage());
                        pattern = state;
                    }
                } catch (IllegalFormatException e) {
                    logger.warn("Cannot format state '{}' to format '{}': {}", state, value, e.getMessage());
                    pattern = state;
                }
            } else {
                logger.warn("Couldn't transform value because transformation service of type '{}' is not available.",
                        type);
                pattern = state;
            }
            return pattern;
        } else {
            return state;
        }
    }

}
