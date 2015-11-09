/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.validation;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.internal.Activator;
import org.osgi.framework.Bundle;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

/**
 * Exception to be thrown if given {@link Configuration} parameters do not match their declaration in the
 * {@link ConfigDescription}.
 *
 * @author Thomas Höfer - Initial contribution
 */
public final class ConfigValidationException extends Exception {

    private static final long serialVersionUID = 1L;

    private final Bundle bundle;
    private final Collection<ConfigValidationMessage> configValidationMessages;

    /**
     * Creates a new {@link ConfigValidationException} for the given {@link ConfigValidationMessage}s. It
     * requires the bundle from which this exception is thrown in order to be able to provide internationalized
     * messages.
     *
     * @param bundle the bundle from which this exception is thrown
     * @param configValidationMessages the configuration description validation messages
     *
     * @throws NullPointException if given bundle or configuration description validation messages are null
     */
    public ConfigValidationException(Bundle bundle, Collection<ConfigValidationMessage> configValidationMessages) {
        Preconditions.checkNotNull(bundle, "Bundle must not be null");
        Preconditions.checkNotNull(configValidationMessages, "Config validation messages must not be null");
        this.bundle = bundle;
        this.configValidationMessages = configValidationMessages;
    }

    /**
     * Retrieves the default validation messages (cp. {@link ConfigValidationMessage#defaultMessage}) for this
     * exception.
     *
     * @return an immutable map of validation messages having affected configuration parameter name as key and the
     *         default message as value
     */
    public Map<String, String> getValidationMessages() {
        ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<>();
        for (ConfigValidationMessage configValidationMessage : configValidationMessages) {
            builder.put(configValidationMessage.parameterName,
                    MessageFormat.format(configValidationMessage.defaultMessage, configValidationMessage.content));
        }
        return builder.build();
    }

    /**
     * Retrieves the internationalized validation messages for this exception. If there is no text found to be
     * internationalized then the default message is delivered.
     *
     * @param locale the locale to be used; if null then the default locale will be used
     *
     * @return an immutable map of internationalized validation messages having affected configuration parameter name as
     *         key and the internationalized message as value (in case of there was no text found to be
     *         internationalized then the default message (cp. {@link ConfigValidationMessage#defaultMessage}) is
     *         delivered)
     */
    public Map<String, String> getValidationMessages(Locale locale) {
        Locale loc = locale != null ? locale : Locale.getDefault();

        ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<>();
        for (ConfigValidationMessage configValidationMessage : configValidationMessages) {
            String text = Activator.getI18nProvider().getText(bundle, configValidationMessage.messageKey,
                    configValidationMessage.defaultMessage, loc);
            builder.put(configValidationMessage.parameterName,
                    MessageFormat.format(text, configValidationMessage.content));
        }
        return builder.build();
    }

}
