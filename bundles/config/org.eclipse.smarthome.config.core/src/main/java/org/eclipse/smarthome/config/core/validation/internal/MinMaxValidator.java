/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.validation.internal;

import java.math.BigDecimal;

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.config.core.validation.ConfigValidationMessage;
import org.eclipse.smarthome.config.core.validation.internal.TypeIntrospections.TypeIntrospection;

/**
 * The {@link ConfigDescriptionParameterValidator} for the minimum and maximum attribute of a
 * {@link ConfigDescriptionParameter}.
 *
 * @author Thomas Höfer - Initial contribution
 * @authod Chris Jackson - Allow options to be outside of min/max value
 * @param <T>
 */
final class MinMaxValidator implements ConfigDescriptionParameterValidator {

    @Override
    public ConfigValidationMessage validate(ConfigDescriptionParameter parameter, Object value) {
        if (value == null || parameter.getType() == Type.BOOLEAN) {
            return null;
        }

        // Allow specified options to be outside of the min/max value
        for (ParameterOption option : parameter.getOptions()) {
            // Option values are a string, so we can do a simple compare
            if (option.getValue().equals(value.toString())) {
                return null;
            }
        }

        TypeIntrospection typeIntrospection = TypeIntrospections.get(parameter.getType());

        if (parameter.getMinimum() != null) {
            BigDecimal min = parameter.getMinimum();
            if (typeIntrospection.isMinViolated(value, min)) {
                return createMinMaxViolationMessage(parameter.getName(), typeIntrospection.getMinViolationMessageKey(),
                        min);
            }
        }

        if (parameter.getMaximum() != null) {
            BigDecimal max = parameter.getMaximum();
            if (typeIntrospection.isMaxViolated(value, max)) {
                return createMinMaxViolationMessage(parameter.getName(), typeIntrospection.getMaxViolationMessageKey(),
                        max);
            }
        }

        return null;
    }

    private static ConfigValidationMessage createMinMaxViolationMessage(String parameterName, MessageKey messageKey,
            BigDecimal minMax) {
        return new ConfigValidationMessage(parameterName, messageKey.defaultMessage, messageKey.key, String.valueOf(minMax));
    }
}
