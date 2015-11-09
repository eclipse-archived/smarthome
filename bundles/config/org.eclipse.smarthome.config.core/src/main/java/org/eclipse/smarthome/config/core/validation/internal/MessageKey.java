/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
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
public final class MessageKey {

    public static final MessageKey PARAMETER_REQUIRED = new MessageKey("parameter_required",
            "The parameter is required");

    public static final MessageKey MAX_VALUE_EXCEEDED = new MessageKey("max_value_exceeded",
            "The value must not be greater/longer than {0}.");

    /** The key to be used for internationalization. */
    public final String key;

    /** The default message. */
    public final String defaultMessage;

    private MessageKey(String key, String defaultMessage) {
        this.key = key;
        this.defaultMessage = defaultMessage;
    }

}
