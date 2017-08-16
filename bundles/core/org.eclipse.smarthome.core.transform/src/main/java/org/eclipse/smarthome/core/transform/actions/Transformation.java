/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.transform.actions;

import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationHelper;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.core.transform.internal.TransformationActivator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class holds static "action" methods that can be used from within rules to execute
 * transformations.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class Transformation {

    /**
     * Applies a transformation of a given type with some function to a value.
     *
     * @param type the transformation type, e.g. REGEX or MAP
     * @param function the function to call, this value depends on the transformation type
     * @param value the value to apply the transformation to
     * @return
     *         the transformed value or the original one, if there was no service registered for the
     *         given type or a transformation exception occurred.
     */
    public static String transform(String type, String function, String value) {
        String result;
        TransformationService service = TransformationHelper
                .getTransformationService(TransformationActivator.getContext(), type);
        Logger logger = LoggerFactory.getLogger(Transformation.class);
        if (service != null) {
            try {
                result = service.transform(function, value);
            } catch (TransformationException e) {
                logger.error("Error executing the transformation '{}': {}", type, e.getMessage());
                result = value;
            }
        } else {
            logger.warn("No transformation service '{}' could be found.", type);
            result = value;
        }
        return result;
    }

}
