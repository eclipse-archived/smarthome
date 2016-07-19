/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.validation.internal;

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.validation.ConfigValidationMessage;
import org.eclipse.smarthome.config.core.validation.internal.TypeIntrospections.TypeIntrospection;

/**
 * The {@link TypeValidator} validates if the given value can be assigned to the config description parameter according
 * to its type definition.
 *
 * @author Thomas Höfer - Initial contribution
 */
final class TypeValidator implements ConfigDescriptionParameterValidator {

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.config.core.validation.internal.ConfigDescriptionParameterValidator#validate(org.eclipse.
     * smarthome.config.core.ConfigDescriptionParameter, java.lang.Object)
     */
    @Override
    public ConfigValidationMessage validate(ConfigDescriptionParameter parameter, Object value) {
        if (value == null) {
            return null;
        }

        TypeIntrospection typeIntrospection = TypeIntrospections.get(parameter.getType());
        if (!typeIntrospection.isAssignable(value)) {
            return createDataTypeViolationMessage(parameter.getName(), parameter.getType());
        }

        return null;
    }

    private static ConfigValidationMessage createDataTypeViolationMessage(String parameterName, Type type) {
        return new ConfigValidationMessage(parameterName, MessageKey.DATA_TYPE_VIOLATED.defaultMessage,
                MessageKey.DATA_TYPE_VIOLATED.key, type);
    }

}
