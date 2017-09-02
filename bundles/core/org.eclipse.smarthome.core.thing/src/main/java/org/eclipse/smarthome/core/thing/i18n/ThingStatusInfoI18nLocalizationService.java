/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.i18n;

import java.util.Arrays;
import java.util.Locale;

import org.eclipse.smarthome.core.i18n.I18nUtil;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * <p>
 * The {@link ThingStatusInfoI18nLocalizationService} can be used to localize the {@link ThingStatusInfo} of a thing
 * using the I18N mechanism of the Eclipse SmartHome framework. Currently the description of the {@link ThingStatusInfo}
 * is the single attribute which can be localized.
 *
 * <p>
 * In order to provide a localized description the corresponding {@link ThingHandler} of the thing does not provide a
 * localized string in the <i>ThingStatus.description</i> attribute, but instead provides the reference of the
 * localization string, e.g &#64;text/rate_limit. The handler is able to provide placeholder values as a JSON-serialized
 * array of strings:
 *
 * <pre>
 * &#64;text/rate_limit ["60", "10", "@text/hour"]
 * </pre>
 *
 * <pre>
 * rate_limit=Device is blocked by remote service for {0} minutes. Maximum limit of {1} configuration
 * changes per {2} has been exceeded. For further info please refer to device vendor.
 * </pre>
 *
 * @author Thomas Höfer - Initial contribution
 */
@Component(immediate = true, service = ThingStatusInfoI18nLocalizationService.class)
public final class ThingStatusInfoI18nLocalizationService {

    private TranslationProvider i18nProvider;

    /**
     * Localizes the {@link ThingStatusInfo} for the given thing.
     *
     * @param thing the thing whose thing status info is to be localized (must not be null)
     * @param locale the locale to be used (can be null)
     * @return the localized thing status or the original thing status if
     *         <ul>
     *         <li>there is nothing to be localized</li>
     *         <li>the thing does not have a handler</li>
     *         </ul>
     *
     * @throws IllegalArgumentException if given thing is null
     */
    public ThingStatusInfo getLocalizedThingStatusInfo(Thing thing, Locale locale) {
        if (thing == null) {
            throw new IllegalArgumentException("Thing must not be null.");
        }

        ThingHandler thingHandler = thing.getHandler();

        if (thingHandler == null) {
            return thing.getStatusInfo();
        }

        String description = thing.getStatusInfo().getDescription();
        if (!I18nUtil.isConstant(description)) {
            return thing.getStatusInfo();
        }

        Bundle bundle = FrameworkUtil.getBundle(thingHandler.getClass());

        Description desc = new Description(bundle, locale, description, i18nProvider);
        String translatedDescription = i18nProvider.getText(bundle, desc.key, description, locale, desc.args);

        return new ThingStatusInfo(thing.getStatus(), thing.getStatusInfo().getStatusDetail(), translatedDescription);
    }

    @Reference
    protected void setTranslationProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
    }

    protected void unsetTranslationProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = null;
    }

    /**
     * Utility class to parse the thing status description into the text reference and optional arguments.
     */
    private final class Description {

        private static final int LIMIT = 2;

        private final String key;
        private final Object[] args;

        private Description(final Bundle bundle, final Locale locale, String description,
                final TranslationProvider theTranslationProvider) {
            String[] parts = description.split("\\s+", LIMIT);
            this.key = I18nUtil.stripConstant(parts[0]);

            if (parts.length == 1) {
                this.args = null;
            } else {
                this.args = Arrays.stream(parts[1].replaceAll("\\[|\\]|\"", "").split(",")).filter(s -> {
                    if (s == null) {
                        return false;
                    }
                    return !s.trim().isEmpty();
                }).map(s -> {
                    String input = s.trim();
                    return I18nUtil.isConstant(input)
                            ? theTranslationProvider.getText(bundle, I18nUtil.stripConstant(input), input, locale)
                            : input;
                }).toArray(size -> new String[size]);
            }
        }
    }
}
