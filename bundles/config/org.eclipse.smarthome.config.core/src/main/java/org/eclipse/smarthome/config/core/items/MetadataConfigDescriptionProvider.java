/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.config.core.items;

import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ParameterOption;

/**
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
public interface MetadataConfigDescriptionProvider {

    /**
     * Get the identifier of the metadata namespace
     *
     * @return the metadata namespace
     */
    String getNamespace();

    /**
     * Get the human-readable description of the metadata namespace
     * <p>
     * Overriding this method is optional - it will default to the namespace identifier.
     *
     * @param locale a locale, if available
     * @return the metadata namespace description
     */
    default String getDescription(@Nullable Locale locale) {
        return getNamespace();
    }

    /**
     * Get all valid options if the main metadata value should be restricted to certain values.
     *
     * @param locale
     * @return
     */
    @Nullable
    List<ParameterOption> getParameterOptions(@Nullable Locale locale);

    /**
     * Get the config descriptions for all expected parameters.
     * <p>
     * This list may depend on the current "main" value
     *
     * @param value
     * @param locale
     * @return
     */
    @Nullable
    List<ConfigDescriptionParameter> getParameters(String value, @Nullable Locale locale);

}
