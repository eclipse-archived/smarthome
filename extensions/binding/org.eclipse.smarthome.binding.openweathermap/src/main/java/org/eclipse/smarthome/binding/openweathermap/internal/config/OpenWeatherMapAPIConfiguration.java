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
package org.eclipse.smarthome.binding.openweathermap.internal.config;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.openweathermap.internal.handler.OpenWeatherMapAPIHandler;

/**
 * The {@link OpenWeatherMapAPIConfiguration} is the class used to match the {@link OpenWeatherMapAPIHandler}s
 * configuration.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class OpenWeatherMapAPIConfiguration {

    // supported languages (see https://openweathermap.org/current#multi)
    public static final Set<String> SUPPORTED_LANGUAGES = Collections.unmodifiableSet(Stream.of("ar", "bg", "ca", "cz",
            "de", "el", "en", "es", "fa", "fi", "fr", "gl", "hr", "hu", "it", "ja", "kr", "la", "lt", "mk", "nl", "pl",
            "pt", "ro", "ru", "sk", "sl", "sw", "tr", "ua", "vi", "zh_cn", "zh_tw").collect(Collectors.toSet()));

    private @Nullable String apikey;
    private int refreshInterval;
    private @Nullable String language;

    public @Nullable String getApikey() {
        return apikey;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public @Nullable String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
