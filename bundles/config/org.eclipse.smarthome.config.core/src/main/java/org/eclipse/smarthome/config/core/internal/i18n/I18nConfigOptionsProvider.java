/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.internal.i18n;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.smarthome.config.core.ConfigOptionProvider;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.osgi.service.component.annotations.Component;

/**
 * {@link ConfigOptionProvider} that provides a list of
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
@Component(immediate = true)
public class I18nConfigOptionsProvider implements ConfigOptionProvider {

    @Override
    public Collection<ParameterOption> getParameterOptions(URI uri, String param, Locale locale) {
        if (uri.toString().equals("system:i18n")) {
            Locale translation = locale != null ? locale : Locale.getDefault();
            if (param.equals("language")) {
                return getAvailable(locale, l -> new ParameterOption(l.getLanguage(), l.getDisplayLanguage(translation)));
            } else if (param.equals("region")) {
                return getAvailable(locale, l -> new ParameterOption(l.getCountry(), l.getDisplayCountry(translation)));
            } else if (param.equals("variant")) {
                return getAvailable(locale, l -> new ParameterOption(l.getVariant(), l.getDisplayVariant(translation)));
            }
        }
        return null;
    }

    private Collection<ParameterOption> getAvailable(Locale locale, Function<Locale, ParameterOption> mapFunction) {
        return Arrays.stream(Locale.getAvailableLocales()).map(l -> mapFunction.apply(l)).distinct()
                .sorted(Comparator.comparing(a -> a.getLabel())).collect(Collectors.toList());
    }

}
