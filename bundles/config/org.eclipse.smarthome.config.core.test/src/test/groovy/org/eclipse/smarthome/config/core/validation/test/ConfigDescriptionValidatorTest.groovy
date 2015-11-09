/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
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

    private static final int MAX = 3;

    private static final String TXT_MAX_EXCEEDED = "1234"
    private static final String UNKNOWN = "unknown"

    private static final String PARAM1_NAME = "param1"
    private static final String PARAM2_NAME = "param2"
    private static final String PARAM3_NAME = "param3"
    private static final String PARAM4_NAME = "param4"

    private static final ConfigDescriptionParameter PARAM1 = createConfigDescriptionParameter(PARAM1_NAME, ConfigDescriptionParameter.Type.TEXT)
    private static final ConfigDescriptionParameter PARAM2 = createConfigDescriptionParameter(PARAM2_NAME, ConfigDescriptionParameter.Type.TEXT, true)
    private static final ConfigDescriptionParameter PARAM3 = createConfigDescriptionParameter(PARAM3_NAME, ConfigDescriptionParameter.Type.TEXT, false, MAX)
    private static final ConfigDescriptionParameter PARAM4 = createConfigDescriptionParameter(PARAM4_NAME, ConfigDescriptionParameter.Type.TEXT, true, MAX)

    private static final URI CONFIG_DESCRIPTION_URI = new URI("config:dummy")

    private static final ConfigDescription CONFIG_DESCRIPTION = new ConfigDescription(CONFIG_DESCRIPTION_URI, [
        PARAM1,
        PARAM2,
        PARAM3,
        PARAM4] as List)

    @Before
    void setUp() {
        Activator.configDescriptionRegistry = [
            getConfigDescription: { uri ->
                if(!CONFIG_DESCRIPTION_URI.equals(uri)) {
                    throw new IllegalArgumentException()
                }
                CONFIG_DESCRIPTION
            }
        ] as ConfigDescriptionRegistry
    }


    @Test
    void 'assert validation throws no exception for valid config parameters'() {
        ConfigDescriptionValidator.validate(createDefaultParams(), CONFIG_DESCRIPTION_URI)
    }

    @Test
    void 'assert validation throws exception for missing required config parameter'() {
        def expected =
                [
                    new ConfigValidationMessage(PARAM2_NAME, MessageKey.PARAMETER_REQUIRED.defaultMessage, MessageKey.PARAMETER_REQUIRED.key)
                ]
        try {
            Map params = createDefaultParams()
            params.put(PARAM2_NAME, null)
            ConfigDescriptionValidator.validate(params, CONFIG_DESCRIPTION_URI)
            failBecauseOfMissingConfigValidationException()
        } catch(ConfigValidationException e) {
            assertThat e.configValidationMessages, is(expected)
        }
    }

    @Test
    void 'assert validation throws exception for invalid max attribute of config parameter'() {
        def expected =
                [
                    new ConfigValidationMessage(PARAM3_NAME, MessageKey.MAX_VALUE_EXCEEDED.defaultMessage, MessageKey.MAX_VALUE_EXCEEDED.key, MAX)
                ]
        try {
            Map params = createDefaultParams()
            params.put(PARAM3_NAME, TXT_MAX_EXCEEDED)
            ConfigDescriptionValidator.validate(params, CONFIG_DESCRIPTION_URI)
            failBecauseOfMissingConfigValidationException()
        } catch(ConfigValidationException e) {
            assertThat e.configValidationMessages, is(expected)
        }
    }

    @Test
    void 'assert validation throws exception for missing required config parameter that has also a max attribute'() {
        def expected =
                [
                    new ConfigValidationMessage(PARAM4_NAME, MessageKey.PARAMETER_REQUIRED.defaultMessage, MessageKey.PARAMETER_REQUIRED.key)
                ]
        try {
            Map params = createDefaultParams()
            params.put(PARAM4_NAME, null)
            ConfigDescriptionValidator.validate(params, CONFIG_DESCRIPTION_URI)
            failBecauseOfMissingConfigValidationException()
        } catch(ConfigValidationException e) {
            assertThat e.configValidationMessages, is(expected)
        }
    }

    @Test
    void 'assert validation throws exception for invalid max attribute of required config parameter'() {
        def expected =
                [
                    new ConfigValidationMessage(PARAM4_NAME, MessageKey.MAX_VALUE_EXCEEDED.defaultMessage, MessageKey.MAX_VALUE_EXCEEDED.key, MAX)
                ]
        try {
            Map params = createDefaultParams()
            params.put(PARAM4_NAME, TXT_MAX_EXCEEDED)
            ConfigDescriptionValidator.validate(params, CONFIG_DESCRIPTION_URI)
            failBecauseOfMissingConfigValidationException()
        } catch(ConfigValidationException e) {
            assertThat e.configValidationMessages, is(expected)
        }
    }

    @Test
    void 'assert validation throws exception containing multiple violations'() {
        def expected =
                [
                    new ConfigValidationMessage(PARAM2_NAME, MessageKey.PARAMETER_REQUIRED.defaultMessage, MessageKey.PARAMETER_REQUIRED.key),
                    new ConfigValidationMessage(PARAM3_NAME, MessageKey.MAX_VALUE_EXCEEDED.defaultMessage, MessageKey.MAX_VALUE_EXCEEDED.key, MAX),
                ]
        try {
            Map params = createDefaultParams()
            params.put(PARAM2_NAME, null)
            params.put(PARAM3_NAME, TXT_MAX_EXCEEDED)
            ConfigDescriptionValidator.validate(params, CONFIG_DESCRIPTION_URI)
            failBecauseOfMissingConfigValidationException()
        } catch(ConfigValidationException e) {
            assertThat e.configValidationMessages, is(expected)
        }
    }

    @Test
    void 'assert validation does not care about parameter that is not specified in config description'() {
        Map params = createDefaultParams()
        params.put(UNKNOWN, null)
        ConfigDescriptionValidator.validate(params, CONFIG_DESCRIPTION_URI)
    }

    @Test(expected=NullPointerException)
    void 'assert validate throws NPE for null paramerters'() {
        ConfigDescriptionValidator.validate(null, CONFIG_DESCRIPTION_URI)
    }

    @Test(expected=NullPointerException)
    void 'assert validate throws NPE for null config description uri'() {
        ConfigDescriptionValidator.validate(createDefaultParams(), null)
    }

    @Test(expected=IllegalArgumentException)
    void 'assert validate throws IllegalArgumentException for unknown uri'() {
        ConfigDescriptionValidator.validate(createDefaultParams(), new URI(UNKNOWN))
    }

    def failBecauseOfMissingConfigValidationException() {
        fail "A config validation exception was expected but it was not thrown"
    }

    def createDefaultParams() {
        return createParams(null, "", null, "")
    }

    def createParams(Object v1, v2, v3, v4) {
        return [(PARAM1_NAME):v1,(PARAM2_NAME):v2,(PARAM3_NAME):v3,(PARAM4_NAME):v4]
    }

    def static createConfigDescriptionParameter(String name, ConfigDescriptionParameter.Type type, Boolean required=false, Integer max=null) {
        return ConfigDescriptionParameterBuilder.create(name, type).withRequired(required).withMaximum(max).build()
    }
}
