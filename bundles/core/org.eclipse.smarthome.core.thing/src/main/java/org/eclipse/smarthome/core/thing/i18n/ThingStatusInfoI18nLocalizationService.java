/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.i18n;

import java.util.Locale;

import org.eclipse.smarthome.core.i18n.I18nProvider;
import org.eclipse.smarthome.core.i18n.I18nUtil;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

/**
 * The {@link ThingStatusInfoI18nLocalizationService} can be used to localize the {@link ThingStatusInfo} of a thing
 * using the I18N mechanism of the Eclipse SmartHome framework. Currently the description of the {@link ThingStatusInfo}
 * is the single attribute which can be localized.
 * </p>
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
public final class ThingStatusInfoI18nLocalizationService {

    private I18nProvider i18nProvider;

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
     * @throws NullPointerException if given thing is null
     */
    public ThingStatusInfo getLocalizedThingStatusInfo(Thing thing, Locale locale) {
        Preconditions.checkNotNull(thing, "Thing must not be null.");

        if (thing.getHandler() == null) {
            return thing.getStatusInfo();
        }

        String description = thing.getStatusInfo().getDescription();
        if (!I18nUtil.isConstant(description)) {
            return thing.getStatusInfo();
        }

        Bundle bundle = FrameworkUtil.getBundle(thing.getHandler().getClass());

        Description desc = new Description(bundle, locale, description, i18nProvider);
        String translatedDescription = i18nProvider.getText(bundle, desc.key, description, locale, desc.args);

        return new ThingStatusInfo(thing.getStatus(), thing.getStatusInfo().getStatusDetail(), translatedDescription);
    }

    protected void setI18nProvider(I18nProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
    }

    protected void unsetI18nProvider(I18nProvider i18nProvider) {
        i18nProvider = null;
    }

    private final class Description {

        private static final int LIMIT = 2;

        private final String key;
        private final Object[] args;

        private Description(final Bundle bundle, final Locale locale, String description,
                final I18nProvider theI18nProvider) {
            String[] parts = description.split("\\s+", LIMIT);
            this.key = I18nUtil.stripConstant(parts[0]);

            if (parts.length == 1) {
                this.args = null;
            } else {
                Iterable<String> source = Splitter.on(",").trimResults().omitEmptyStrings()
                        .split(parts[1].replaceAll("\\[|\\]|\"", ""));
                Iterable<String> target = Iterables.transform(source, new Function<String, String>() {
                    @Override
                    public String apply(final String input) {
                        if (I18nUtil.isConstant(input)) {
                            return theI18nProvider.getText(bundle, I18nUtil.stripConstant(input), input, locale);
                        }
                        return input;
                    }
                });
                this.args = Iterables.toArray(target, Object.class);
            }
        }
    }
}
