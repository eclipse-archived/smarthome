/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.validation.internal;

import org.eclipse.smarthome.config.core.validation.ConfigDescriptionValidator;

/**
 * The {@link ConfigDescriptionParameterValidatorFactory} creates the corresponding
 * {@link ConfigDescriptionParameterValidator}s used by {@link ConfigDescriptionValidator}.
 *
 * @author Thomas Höfer - Initial contribution
 */
public final class ConfigDescriptionParameterValidatorFactory {

    private ConfigDescriptionParameterValidatorFactory() {
        super();
    }

    /**
     * Returns a new validator for the required attribute of a config description parameter.
     *
     * @return a new validator for the required attribute of a config description parameter
     */
    public static ConfigDescriptionParameterValidator createRequiredValidator() {
        return new RequiredValidator();
    }

    /**
     * Returns a new validator for the data type validation of a config description parameter.
     *
     * @return a new validator for the data type validation of a config description parameter
     */
    public static ConfigDescriptionParameterValidator createTypeValidator() {
        return new TypeValidator();
    }

    /**
     * Returns a new validator for the min and max attribute of a config description parameter.
     *
     * @return a new validator for the min and max attribute of a config description parameter
     */
    public static ConfigDescriptionParameterValidator createMinMaxValidator() {
        return new MinMaxValidator();
    }

    /**
     * Returns a new validator for the pattern attribute of a config description parameter.
     *
     * @return a new validator for the pattern attribute of a config description parameter
     */
    public static ConfigDescriptionParameterValidator createPatternValidator() {
        return new PatternValidator();
    }
}
