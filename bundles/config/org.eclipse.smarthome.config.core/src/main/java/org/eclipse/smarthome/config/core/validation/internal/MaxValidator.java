/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.validation.internal;

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.validation.ConfigValidationMessage;

/**
 * The {@link ConfigDescriptionParameterValidator} for the max attribute of a {@link ConfigDescriptionParameter}.
 *
 * @author Thomas Höfer - Initial contribution
 */
public final class MaxValidator implements ConfigDescriptionParameterValidator {

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.config.core.validation.internal.ConfigDescriptionParameterValidator#validate(org.eclipse.
     * smarthome.config.core.ConfigDescriptionParameter, java.lang.Object)
     */
    @Override
    public ConfigValidationMessage validate(ConfigDescriptionParameter configDescriptionParameter,
            Object value) {
        if (configDescriptionParameter.getMaximum() != null && value != null) {
            int max = configDescriptionParameter.getMaximum().intValue();
            if (configDescriptionParameter.getType() == Type.TEXT && value instanceof String) {
                String string = (String) value;
                if (string.length() > max) {
                    MessageKey messageKey = MessageKey.MAX_VALUE_EXCEEDED;
                    return new ConfigValidationMessage(configDescriptionParameter.getName(),
                            messageKey.defaultMessage, messageKey.key, max);
                }
            }
        }
        return null;
    }

}
