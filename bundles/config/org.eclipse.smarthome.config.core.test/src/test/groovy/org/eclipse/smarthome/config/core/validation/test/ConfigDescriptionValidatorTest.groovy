/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.validation.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.config.core.ConfigDescription
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterBuilder
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type
import org.eclipse.smarthome.config.core.internal.Activator
import org.eclipse.smarthome.config.core.validation.ConfigDescriptionValidator
import org.eclipse.smarthome.config.core.validation.ConfigValidationException
import org.eclipse.smarthome.config.core.validation.ConfigValidationMessage
import org.eclipse.smarthome.config.core.validation.internal.MessageKey
import org.junit.Before
import org.junit.Test

/**
 * Testing the {@link ConfigDescriptionValidator}.
 *
 * @author Thomas Höfer - Initial contribution
 */
class ConfigDescriptionValidatorTest {

    private static final int MIN_VIOLATED = 1
    private static final int MAX_VIOLATED = 1234

    private static final int MIN = 2
    private static final int MAX = 3

    private static final String PATTERN = "ab*c"

    private static final String UNKNOWN = "unknown"
    private static final def INVALID = 0l

    private static final String BOOL_PARAM_NAME = "bool-param"
    private static final String BOOL_REQUIRED_PARAM_NAME = "bool-required-papram"

    private static final String TXT_PARAM_NAME = "txt-param"
    private static final String TXT_REQUIRED_PARAM_NAME = "txt-required-papram"
    private static final String TXT_MIN_PARAM_NAME = "txt-min-name"
    private static final String TXT_MAX_PARAM_NAME = "txt-max-name"
    private static final String TXT_PATTERN_PARAM_NAME = "txt-pattern-name"
    private static final String TXT_MAX_PATTERN_PARAM_NAME = "txt-max-pattern-name"

    private static final String INT_PARAM_NAME = "int-param"
    private static final String INT_REQUIRED_PARAM_NAME = "int-required-papram"
    private static final String INT_MIN_PARAM_NAME = "int-min-name"
    private static final String INT_MAX_PARAM_NAME = "int-max-name"

    private static final String DECIMAL_PARAM_NAME = "decimal-param"
    private static final String DECIMAL_REQUIRED_PARAM_NAME = "decimal-required-papram"
    private static final String DECIMAL_MIN_PARAM_NAME = "decimal-min-name"
    private static final String DECIMAL_MAX_PARAM_NAME = "decimal-max-name"

    private static final ConfigDescriptionParameter BOOL_PARAM = createConfigDescriptionParameter(name: BOOL_PARAM_NAME, type: ConfigDescriptionParameter.Type.BOOLEAN)
    private static final ConfigDescriptionParameter BOOL_REQUIRED_PARAM = createConfigDescriptionParameter(name: BOOL_REQUIRED_PARAM_NAME, type: ConfigDescriptionParameter.Type.BOOLEAN, required: true)

    private static final ConfigDescriptionParameter TXT_PARAM = createConfigDescriptionParameter(name: TXT_PARAM_NAME, type: ConfigDescriptionParameter.Type.TEXT)
    private static final ConfigDescriptionParameter TXT_REQUIRED_PARAM = createConfigDescriptionParameter(name: TXT_REQUIRED_PARAM_NAME, type: ConfigDescriptionParameter.Type.TEXT, required: true)
    private static final ConfigDescriptionParameter TXT_MIN_PARAM = createConfigDescriptionParameter(name: TXT_MIN_PARAM_NAME, type: ConfigDescriptionParameter.Type.TEXT, min: MIN)
    private static final ConfigDescriptionParameter TXT_MAX_PARAM = createConfigDescriptionParameter(name: TXT_MAX_PARAM_NAME, type: ConfigDescriptionParameter.Type.TEXT, max: MAX)
    private static final ConfigDescriptionParameter TXT_PATTERN_PARAM = createConfigDescriptionParameter(name: TXT_PATTERN_PARAM_NAME, type: ConfigDescriptionParameter.Type.TEXT, pattern: PATTERN)
    private static final ConfigDescriptionParameter TXT_MAX_PATTERN_PARAM = createConfigDescriptionParameter(name: TXT_MAX_PATTERN_PARAM_NAME, type: ConfigDescriptionParameter.Type.TEXT, max: MAX, pattern: PATTERN)

    private static final ConfigDescriptionParameter INT_PARAM = createConfigDescriptionParameter(name: INT_PARAM_NAME, type: ConfigDescriptionParameter.Type.INTEGER)
    private static final ConfigDescriptionParameter INT_REQUIRED_PARAM = createConfigDescriptionParameter(name: INT_REQUIRED_PARAM_NAME, type: ConfigDescriptionParameter.Type.INTEGER, required: true)
    private static final ConfigDescriptionParameter INT_MIN_PARAM = createConfigDescriptionParameter(name: INT_MIN_PARAM_NAME, type: ConfigDescriptionParameter.Type.INTEGER, min: MIN)
    private static final ConfigDescriptionParameter INT_MAX_PARAM = createConfigDescriptionParameter(name: INT_MAX_PARAM_NAME, type: ConfigDescriptionParameter.Type.INTEGER, max: MAX)

    private static final ConfigDescriptionParameter DECIMAL_PARAM = createConfigDescriptionParameter(name: DECIMAL_PARAM_NAME, type: ConfigDescriptionParameter.Type.DECIMAL)
    private static final ConfigDescriptionParameter DECIMAL_REQUIRED_PARAM = createConfigDescriptionParameter(name: DECIMAL_REQUIRED_PARAM_NAME, type: ConfigDescriptionParameter.Type.DECIMAL, required: true)
    private static final ConfigDescriptionParameter DECIMAL_MIN_PARAM = createConfigDescriptionParameter(name: DECIMAL_MIN_PARAM_NAME, type: ConfigDescriptionParameter.Type.DECIMAL, min: MIN)
    private static final ConfigDescriptionParameter DECIMAL_MAX_PARAM = createConfigDescriptionParameter(name: DECIMAL_MAX_PARAM_NAME, type: ConfigDescriptionParameter.Type.DECIMAL, max: MAX)

    private static final URI CONFIG_DESCRIPTION_URI = new URI("config:dummy")

    private static final ConfigDescription CONFIG_DESCRIPTION = new ConfigDescription(CONFIG_DESCRIPTION_URI, [
        BOOL_PARAM,
        BOOL_REQUIRED_PARAM,
        TXT_PARAM,
        TXT_REQUIRED_PARAM,
        TXT_MIN_PARAM,
        TXT_MAX_PARAM,
        TXT_PATTERN_PARAM,
        TXT_MAX_PATTERN_PARAM,
        INT_PARAM,
        INT_REQUIRED_PARAM,
        INT_MIN_PARAM,
        INT_MAX_PARAM,
        DECIMAL_PARAM,
        DECIMAL_REQUIRED_PARAM,
        DECIMAL_MIN_PARAM,
        DECIMAL_MAX_PARAM] as List)

    private Map params

    @Before
    void setUp() {
        Activator.configDescriptionRegistry = [
            getConfigDescription: { uri ->
                if(!CONFIG_DESCRIPTION_URI.equals(uri)) {
                    null
                }
                CONFIG_DESCRIPTION
            }
        ] as ConfigDescriptionRegistry

        params = [
            (BOOL_PARAM_NAME): null,
            (BOOL_REQUIRED_PARAM_NAME): Boolean.FALSE,
            (TXT_PARAM_NAME): null,
            (TXT_REQUIRED_PARAM_NAME): "",
            (TXT_MIN_PARAM_NAME): String.valueOf(MAX_VIOLATED),
            (TXT_MAX_PARAM_NAME): String.valueOf(MIN_VIOLATED),
            (TXT_PATTERN_PARAM_NAME): "abbbc",
            (TXT_MAX_PATTERN_PARAM_NAME): "abc",
            (INT_PARAM_NAME): null,
            (INT_REQUIRED_PARAM_NAME): 0,
            (INT_MIN_PARAM_NAME): MIN,
            (INT_MAX_PARAM_NAME): MAX,
            (DECIMAL_PARAM_NAME): null,
            (DECIMAL_REQUIRED_PARAM_NAME): 0f,
            (DECIMAL_MIN_PARAM_NAME): (float) MIN,
            (DECIMAL_MAX_PARAM_NAME): (float) MAX
        ]
    }


    @Test
    void 'assert validation throws no exception for valid config parameters'() {
        ConfigDescriptionValidator.validate(params, CONFIG_DESCRIPTION_URI)
    }

    // ===========================================================================
    // REQUIRED VALIDATIONS
    // ===========================================================================

    @Test
    void 'assert validation throws exception for missing required boolean config parameter'() {
        assertRequired(BOOL_REQUIRED_PARAM_NAME)
    }

    @Test
    void 'assert validation throws exception for missing required txt config parameter'() {
        assertRequired(TXT_REQUIRED_PARAM_NAME)
    }

    @Test
    void 'assert validation throws exception for missing required int config parameter'() {
        assertRequired(INT_REQUIRED_PARAM_NAME)
    }

    @Test
    void 'assert validation throws exception for missing required decimal config parameter'() {
        assertRequired(DECIMAL_REQUIRED_PARAM_NAME)
    }

    @Test
    void 'assert validation throws exception containing messages for all required config parameters'() {
        def expected =
                [
                    new ConfigValidationMessage(BOOL_REQUIRED_PARAM_NAME, MessageKey.PARAMETER_REQUIRED.defaultMessage, MessageKey.PARAMETER_REQUIRED.key),
                    new ConfigValidationMessage(TXT_REQUIRED_PARAM_NAME, MessageKey.PARAMETER_REQUIRED.defaultMessage, MessageKey.PARAMETER_REQUIRED.key),
                    new ConfigValidationMessage(INT_REQUIRED_PARAM_NAME, MessageKey.PARAMETER_REQUIRED.defaultMessage, MessageKey.PARAMETER_REQUIRED.key),
                    new ConfigValidationMessage(DECIMAL_REQUIRED_PARAM_NAME, MessageKey.PARAMETER_REQUIRED.defaultMessage, MessageKey.PARAMETER_REQUIRED.key)
                ]
        try {
            params.put(BOOL_PARAM_NAME, null)
            params.put(TXT_PARAM_NAME, null)
            params.put(INT_PARAM_NAME, null)
            params.put(DECIMAL_PARAM_NAME, null)
            params.put(BOOL_REQUIRED_PARAM_NAME, null)
            params.put(TXT_REQUIRED_PARAM_NAME, null)
            params.put(INT_REQUIRED_PARAM_NAME, null)
            params.put(DECIMAL_REQUIRED_PARAM_NAME, null)
            ConfigDescriptionValidator.validate(params, CONFIG_DESCRIPTION_URI)
            failBecauseOfMissingConfigValidationException()
        } catch(ConfigValidationException e) {
            assertThat e.configValidationMessages, is(expected)
        }
    }

    void assertRequired(def parameterName) {
        def expected =
                [
                    new ConfigValidationMessage(parameterName, MessageKey.PARAMETER_REQUIRED.defaultMessage, MessageKey.PARAMETER_REQUIRED.key)
                ]
        try {
            params.put(parameterName, null)
            ConfigDescriptionValidator.validate(params, CONFIG_DESCRIPTION_URI)
            failBecauseOfMissingConfigValidationException()
        } catch(ConfigValidationException e) {
            assertThat e.configValidationMessages, is(expected)
        }
    }

    // ===========================================================================
    // MIN MAX VALIDATIONS
    // ===========================================================================

    @Test
    void 'assert validation throws exception for invalid min attribute of txt config parameter'() {
        assertMinMax(TXT_MIN_PARAM_NAME, String.valueOf(MIN_VIOLATED), MessageKey.MIN_VALUE_TXT_VIOLATED, MIN)
    }

    @Test
    void 'assert validation throws exception for invalid max attribute of txt config parameter'() {
        assertMinMax(TXT_MAX_PARAM_NAME, String.valueOf(MAX_VIOLATED), MessageKey.MAX_VALUE_TXT_VIOLATED, MAX)
    }

    @Test
    void 'assert validation throws exception for invalid min attribute of int config parameter'() {
        assertMinMax(INT_MIN_PARAM_NAME, MIN_VIOLATED, MessageKey.MIN_VALUE_NUMERIC_VIOLATED, MIN)
    }

    @Test
    void 'assert validation throws exception for invalid max attribute of int config parameter'() {
        assertMinMax(INT_MAX_PARAM_NAME, MAX_VIOLATED, MessageKey.MAX_VALUE_NUMERIC_VIOLATED, MAX)
    }

    @Test
    void 'assert validation throws exception for invalid min attribute of decimal config parameter'() {
        assertMinMax(DECIMAL_MIN_PARAM_NAME, (float) MIN_VIOLATED, MessageKey.MIN_VALUE_NUMERIC_VIOLATED, MIN)
    }

    @Test
    void 'assert validation throws exception for invalid max attribute of decimal config parameter'() {
        assertMinMax(DECIMAL_MAX_PARAM_NAME, (float) MAX_VIOLATED, MessageKey.MAX_VALUE_NUMERIC_VIOLATED, MAX)
    }

    @Test
    void 'assert validation throws exception containing messages for all min max config parameters'() {
        def expected =
                [
                    new ConfigValidationMessage(TXT_MIN_PARAM_NAME, MessageKey.MIN_VALUE_TXT_VIOLATED.defaultMessage, MessageKey.MIN_VALUE_TXT_VIOLATED.key, MIN),
                    new ConfigValidationMessage(TXT_MAX_PARAM_NAME, MessageKey.MAX_VALUE_TXT_VIOLATED.defaultMessage, MessageKey.MAX_VALUE_TXT_VIOLATED.key, MAX),
                    new ConfigValidationMessage(INT_MIN_PARAM_NAME, MessageKey.MIN_VALUE_NUMERIC_VIOLATED.defaultMessage, MessageKey.MIN_VALUE_NUMERIC_VIOLATED.key, MIN),
                    new ConfigValidationMessage(INT_MAX_PARAM_NAME, MessageKey.MAX_VALUE_NUMERIC_VIOLATED.defaultMessage, MessageKey.MAX_VALUE_NUMERIC_VIOLATED.key, MAX),
                    new ConfigValidationMessage(DECIMAL_MIN_PARAM_NAME, MessageKey.MIN_VALUE_NUMERIC_VIOLATED.defaultMessage, MessageKey.MIN_VALUE_NUMERIC_VIOLATED.key, MIN),
                    new ConfigValidationMessage(DECIMAL_MAX_PARAM_NAME, MessageKey.MAX_VALUE_NUMERIC_VIOLATED.defaultMessage, MessageKey.MAX_VALUE_NUMERIC_VIOLATED.key, MAX)
                ]
        try {
            params.put(TXT_MIN_PARAM_NAME, String.valueOf(MIN_VIOLATED))
            params.put(TXT_MAX_PARAM_NAME, String.valueOf(MAX_VIOLATED))
            params.put(INT_MIN_PARAM_NAME, MIN_VIOLATED)
            params.put(INT_MAX_PARAM_NAME, MAX_VIOLATED)
            params.put(DECIMAL_MIN_PARAM_NAME, (float) MIN_VIOLATED)
            params.put(DECIMAL_MAX_PARAM_NAME, (float) MAX_VIOLATED)
            ConfigDescriptionValidator.validate(params, CONFIG_DESCRIPTION_URI)
            failBecauseOfMissingConfigValidationException()
        } catch(ConfigValidationException e) {
            assertThat e.configValidationMessages, is(expected)
        }
    }

    void assertMinMax(String parameterName, def value, MessageKey msgKey, int minMax) {
        def expected =
                [
                    new ConfigValidationMessage(parameterName, msgKey.defaultMessage, msgKey.key, minMax)
                ]
        try {
            params.put(parameterName, value)
            ConfigDescriptionValidator.validate(params, CONFIG_DESCRIPTION_URI)
            failBecauseOfMissingConfigValidationException()
        } catch(ConfigValidationException e) {
            assertThat e.configValidationMessages, is(expected)
        }
    }

    // ===========================================================================
    // TYPE VALIDATIONS
    // ===========================================================================

    @Test
    void 'assert validation throws exception for invalid type for boolean config parameter'() {
        assertType(BOOL_PARAM_NAME, Type.BOOLEAN)
    }

    @Test
    void 'assert validation throws exception for invalid type for txt config parameter'() {
        assertType(TXT_PARAM_NAME, Type.TEXT)
    }

    @Test
    void 'assert validation throws exception for invalid type for int config parameter'() {
        assertType(INT_PARAM_NAME, Type.INTEGER)
    }

    @Test
    void 'assert validation throws exception for invalid type for decimal config parameter'() {
        assertType(DECIMAL_PARAM_NAME, Type.DECIMAL)
    }

    @Test
    void 'assert validation throws exception containing messages for multiple invalid typed config parameters'() {
        def expected =
                [
                    new ConfigValidationMessage(BOOL_PARAM_NAME, MessageKey.DATA_TYPE_VIOLATED.defaultMessage, MessageKey.DATA_TYPE_VIOLATED.key, Type.BOOLEAN),
                    new ConfigValidationMessage(TXT_PARAM_NAME, MessageKey.DATA_TYPE_VIOLATED.defaultMessage, MessageKey.DATA_TYPE_VIOLATED.key, Type.TEXT),
                    new ConfigValidationMessage(INT_PARAM_NAME, MessageKey.DATA_TYPE_VIOLATED.defaultMessage, MessageKey.DATA_TYPE_VIOLATED.key, Type.INTEGER),
                    new ConfigValidationMessage(DECIMAL_PARAM_NAME, MessageKey.DATA_TYPE_VIOLATED.defaultMessage, MessageKey.DATA_TYPE_VIOLATED.key, Type.DECIMAL)
                ]
        try {
            params.put(BOOL_PARAM_NAME, INVALID)
            params.put(TXT_PARAM_NAME, INVALID)
            params.put(INT_PARAM_NAME, INVALID)
            params.put(DECIMAL_PARAM_NAME, INVALID)
            ConfigDescriptionValidator.validate(params, CONFIG_DESCRIPTION_URI)
            failBecauseOfMissingConfigValidationException()
        } catch(ConfigValidationException e) {
            assertThat e.configValidationMessages, is(expected)
        }
    }

    void assertType(String parameterName, Type type) {
        def expected =
                [
                    new ConfigValidationMessage(parameterName, MessageKey.DATA_TYPE_VIOLATED.defaultMessage, MessageKey.DATA_TYPE_VIOLATED.key, type)
                ]
        try {
            params.put(parameterName, INVALID)
            ConfigDescriptionValidator.validate(params, CONFIG_DESCRIPTION_URI)
            failBecauseOfMissingConfigValidationException()
        } catch(ConfigValidationException e) {
            assertThat e.configValidationMessages, is(expected)
        }
    }

    // ===========================================================================
    // PATTERN VALIDATIONS
    // ===========================================================================

    @Test
    void 'assert validation throws exception containing messages for invalid pattern for txt config parameters'()  {
        def expected =
                [
                    new ConfigValidationMessage(TXT_PATTERN_PARAM_NAME, MessageKey.PATTERN_VIOLATED.defaultMessage, MessageKey.PATTERN_VIOLATED.key, String.valueOf(MAX_VIOLATED), PATTERN)
                ]
        try {
            params.put(TXT_PATTERN_PARAM_NAME, String.valueOf(MAX_VIOLATED))
            ConfigDescriptionValidator.validate(params, CONFIG_DESCRIPTION_URI)
            failBecauseOfMissingConfigValidationException()
        } catch(ConfigValidationException e) {
            assertThat e.configValidationMessages, is(expected)
        }
    }

    // ===========================================================================
    // MISC VALIDATIONS
    // ===========================================================================

    @Test
    void 'assert validation throws exception containing multiple various violations'() {
        def expected =
                [
                    new ConfigValidationMessage(BOOL_REQUIRED_PARAM_NAME, MessageKey.PARAMETER_REQUIRED.defaultMessage, MessageKey.PARAMETER_REQUIRED.key),
                    new ConfigValidationMessage(TXT_REQUIRED_PARAM_NAME, MessageKey.PARAMETER_REQUIRED.defaultMessage, MessageKey.PARAMETER_REQUIRED.key),
                    new ConfigValidationMessage(TXT_MAX_PARAM_NAME, MessageKey.MAX_VALUE_TXT_VIOLATED.defaultMessage, MessageKey.MAX_VALUE_TXT_VIOLATED.key, MAX),
                    new ConfigValidationMessage(TXT_PATTERN_PARAM_NAME, MessageKey.PATTERN_VIOLATED.defaultMessage, MessageKey.PATTERN_VIOLATED.key, String.valueOf(MAX_VIOLATED), PATTERN),
                    new ConfigValidationMessage(INT_MIN_PARAM_NAME, MessageKey.MIN_VALUE_NUMERIC_VIOLATED.defaultMessage, MessageKey.MIN_VALUE_NUMERIC_VIOLATED.key, MIN),
                    new ConfigValidationMessage(DECIMAL_PARAM_NAME, MessageKey.DATA_TYPE_VIOLATED.defaultMessage, MessageKey.DATA_TYPE_VIOLATED.key, Type.DECIMAL),
                    new ConfigValidationMessage(DECIMAL_MAX_PARAM_NAME, MessageKey.MAX_VALUE_NUMERIC_VIOLATED.defaultMessage, MessageKey.MAX_VALUE_NUMERIC_VIOLATED.key, MAX)
                ]
        try {
            params.put(BOOL_REQUIRED_PARAM_NAME, null)
            params.put(TXT_REQUIRED_PARAM_NAME, null)
            params.put(TXT_MAX_PARAM_NAME, String.valueOf(MAX_VIOLATED))
            params.put(TXT_PATTERN_PARAM_NAME, String.valueOf(MAX_VIOLATED))
            params.put(INT_MIN_PARAM_NAME, MIN_VIOLATED)
            params.put(DECIMAL_PARAM_NAME, INVALID)
            params.put(DECIMAL_MAX_PARAM_NAME, (float) MAX_VIOLATED)
            ConfigDescriptionValidator.validate(params, CONFIG_DESCRIPTION_URI)
            failBecauseOfMissingConfigValidationException()
        } catch(ConfigValidationException e) {
            assertThat e.configValidationMessages, is(expected)
        }
    }

    @Test
    void 'assert validation provides only one message per parameter although multiple violations occurs'() {
        def expected =
                [
                    new ConfigValidationMessage(TXT_MAX_PATTERN_PARAM_NAME, MessageKey.MAX_VALUE_TXT_VIOLATED.defaultMessage, MessageKey.MAX_VALUE_TXT_VIOLATED.key, MAX)
                ]
        try {
            params.put(TXT_MAX_PATTERN_PARAM_NAME, String.valueOf(MAX_VIOLATED))
            ConfigDescriptionValidator.validate(params, CONFIG_DESCRIPTION_URI)
            failBecauseOfMissingConfigValidationException()
        } catch(ConfigValidationException e) {
            assertThat e.configValidationMessages, is(expected)
        }
    }

    @Test
    void 'assert validation does not care about parameter that is not specified in config description'() {
        params.put(UNKNOWN, null)
        ConfigDescriptionValidator.validate(params, CONFIG_DESCRIPTION_URI)

        params.put(UNKNOWN, MIN_VIOLATED)
        ConfigDescriptionValidator.validate(params, CONFIG_DESCRIPTION_URI)

        params.put(UNKNOWN, MAX_VIOLATED)
        ConfigDescriptionValidator.validate(params, CONFIG_DESCRIPTION_URI)

        params.put(UNKNOWN, INVALID)
        ConfigDescriptionValidator.validate(params, CONFIG_DESCRIPTION_URI)
    }

    @Test(expected=NullPointerException)
    void 'assert validate throws NPE for null paramerters'() {
        ConfigDescriptionValidator.validate(null, CONFIG_DESCRIPTION_URI)
    }

    @Test(expected=NullPointerException)
    void 'assert validate throws NPE for null config description uri'() {
        ConfigDescriptionValidator.validate(params, null)
    }

    @Test
    void 'assert validate can handle unknown URIs'() {
        ConfigDescriptionValidator.validate(params, new URI(UNKNOWN))
    }

    @Test
    void 'assert validate can handle situations without config description registry'() {
        Activator.configDescriptionRegistry = null
        ConfigDescriptionValidator.validate(params, CONFIG_DESCRIPTION_URI)
    }

    def failBecauseOfMissingConfigValidationException() {
        fail "A config validation exception was expected but it was not thrown"
    }

    def static createConfigDescriptionParameter(Map args) {
        return ConfigDescriptionParameterBuilder.create(args.name, args.type).withRequired(args.required).withMaximum(args.max).withMinimum(args.min).withPattern(args.pattern).build()
    }
}
