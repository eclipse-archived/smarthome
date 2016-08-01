/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.validation.internal;

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.validation.ConfigValidationMessage;

/**
 * The {@link ConfigDescriptionParameterValidator} can be implemented to provide a specific validation of a
 * {@link ConfigDescriptionParameter} and its value to be set.
 *
 * @author Thomas Höfer - Initial contribution
 */
public interface ConfigDescriptionParameterValidator {

    /**
     * Validates the given value against the given {@link ConfigDescriptionParameter}.
     *
     * @param parameter the configuration description parameter
     * @param value the value to be set for the config description parameter
     *
     * @return a {@link ConfigValidationMessage} if value does not meet the declaration of the parameter,
     *         otherwise null
     */
    ConfigValidationMessage validate(ConfigDescriptionParameter parameter, Object value);
}
