/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.validation.internal;

/**
 * Utility class providing the {@link MessageKey}s for config description validation. The {@link MessageKey}
 * consists of a key to be used for internationalization and a general default text.
 *
 * @author Thomas Höfer - Initial contribution
 */
final class MessageKey {

    static final MessageKey PARAMETER_REQUIRED = new MessageKey("parameter_required", "The parameter is required");

    static final MessageKey DATA_TYPE_VIOLATED = new MessageKey("data_type_violated",
            "The data type of the value does not match with the type declaration ({0}) in the configuration description.");

    static final MessageKey MAX_VALUE_TXT_VIOLATED = new MessageKey("max_value_txt_violated",
            "The value must not consist of more than {0} characters.");
    static final MessageKey MAX_VALUE_NUMERIC_VIOLATED = new MessageKey("max_value_numeric_violated",
            "The value must not be greater than {0}.");
    static final MessageKey MAX_VALUE_OPTIONS_VIOLATED = new MessageKey("max_value_options_violated",
            "There are not more than {0} options allowed.");

    static final MessageKey MIN_VALUE_TXT_VIOLATED = new MessageKey("min_value_txt_violated",
            "The value must not consist of less than {0} characters.");
    static final MessageKey MIN_VALUE_NUMERIC_VIOLATED = new MessageKey("min_value_numeric_violated",
            "The value must not be less than {0}.");
    static final MessageKey MIN_VALUE_OPTIONS_VIOLATED = new MessageKey("min_value_options_violated",
            "There are at least {0} options required.");

    static final MessageKey PATTERN_VIOLATED = new MessageKey("pattern_violated",
            "The value {0} does not match the pattern {1}.");

    /** The key to be used for internationalization. */
    final String key;

    /** The default message. */
    final String defaultMessage;

    private MessageKey(String key, String defaultMessage) {
        this.key = key;
        this.defaultMessage = defaultMessage;
    }

}
