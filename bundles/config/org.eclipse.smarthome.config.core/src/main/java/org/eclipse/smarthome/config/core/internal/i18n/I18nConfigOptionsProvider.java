/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.internal.i18n;

import java.net.URI;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.smarthome.config.core.ConfigOptionProvider;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.osgi.service.component.annotations.Component;

/**
 * {@link ConfigOptionProvider} that provides a list of
 *
 * @author Simon Kaufmann - initial contribution and API
 * @author Erdoan Hadzhiyusein - Added time zone
 *
 */
@Component(immediate = true)
public class I18nConfigOptionsProvider implements ConfigOptionProvider {

    private static final String NO_OFFSET_FORMAT = "(GMT) %s";
    private static final String NEGATIVE_OFFSET_FORMAT = "%s (GMT%d:%02d)";
    private static final String POSITIVE_OFFSET_FORMAT = "%s (GMT+%d:%02d)";

    @Override
    public Collection<ParameterOption> getParameterOptions(URI uri, String param, Locale locale) {
        if (uri.toString().equals("system:i18n")) {
            Locale translation = locale != null ? locale : Locale.getDefault();
            return processParamType(param, locale, translation);
        }
        return null;
    }

    private Collection<ParameterOption> processParamType(String param, Locale locale, Locale translation) {
        switch (param) {
            case "language":
                return getAvailable(locale,
                        l -> new ParameterOption(l.getLanguage(), l.getDisplayLanguage(translation)));
            case "region":
                return getAvailable(locale,
                        l -> new ParameterOption(l.getCountry(), l.getDisplayCountry(translation)));
            case "variant":
                return getAvailable(locale,
                        l -> new ParameterOption(l.getVariant(), l.getDisplayVariant(translation)));
            case "timezone":
                return ZoneId.getAvailableZoneIds().stream().map(TimeZone::getTimeZone).sorted((tz1, tz2) -> {
                    return tz2.getRawOffset() - tz2.getRawOffset();
                }).map(tz -> {
                    return new ParameterOption(tz.getID(), getTimeZoneRepresentation(tz));
                }).collect(Collectors.toList());
            default:
                return null;
        }
    }

    private static String getTimeZoneRepresentation(TimeZone tz) {
        long hours = TimeUnit.MILLISECONDS.toHours(tz.getRawOffset());
        long minutes = TimeUnit.MILLISECONDS.toMinutes(tz.getRawOffset()) - TimeUnit.HOURS.toMinutes(hours);
        minutes = Math.abs(minutes);
        final String result;
        if (hours > 0) {
            result = String.format(POSITIVE_OFFSET_FORMAT, tz.getID(), hours, minutes);
        } else if (hours < 0) {
            result = String.format(NEGATIVE_OFFSET_FORMAT, tz.getID(), hours, minutes);
        } else {
            result = String.format(NO_OFFSET_FORMAT, tz.getDisplayName(Locale.getDefault()));
        }
        return result;
    }

    private Collection<ParameterOption> getAvailable(Locale locale, Function<Locale, ParameterOption> mapFunction) {
        return Arrays.stream(Locale.getAvailableLocales()).map(l -> mapFunction.apply(l)).distinct()
                .sorted(Comparator.comparing(a -> a.getLabel())).collect(Collectors.toList());
    }
}
