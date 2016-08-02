/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.eclipse.smarthome.core.i18n.I18nProvider;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The {@link I18nProviderImpl} is a concrete implementation of the {@link I18nProvider} service interface.
 * <p>
 * This implementation uses the i18n mechanism of Java ({@link ResourceBundle}) to translate a given key into text. The
 * resources must be placed under the specific directory {@link LanguageResourceBundleManager#RESOURCE_DIRECTORY} within
 * the certain modules. Each module is tracked in the platform by using the {@link ResourceBundleTracker} and managed by
 * using one certain {@link LanguageResourceBundleManager} which is responsible for the translation.
 *
 * @author Michael Grammling - Initial Contribution
 * @author Thomas Höfer - Added getText operation with arguments
 * @author Markus Rathgeb - Add locale provider support
 */
public class I18nProviderImpl implements I18nProvider {

    private LocaleProvider localeProvider;
    private ResourceBundleTracker resourceBundleTracker;

    protected void activate(BundleContext bundleContext) {
        this.resourceBundleTracker = new ResourceBundleTracker(bundleContext, localeProvider);
        this.resourceBundleTracker.open();
    }

    protected void deactivate(BundleContext bundleContext) {
        this.resourceBundleTracker.close();
    }

    protected void setLocaleProvider(final LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    protected void unsetLocaleProvider(final LocaleProvider localeProvider) {
        if (this.localeProvider == localeProvider) {
            this.localeProvider = null;
        }
    }

    @Override
    public String getText(Bundle bundle, String key, String defaultText, Locale locale) {
        LanguageResourceBundleManager languageResource = this.resourceBundleTracker.getLanguageResource(bundle);

        if (languageResource != null) {
            String text = languageResource.getText(key, locale);
            if (text != null) {
                return text;
            }
        }

        return defaultText;
    }

    @Override
    public String getText(Bundle bundle, String key, String defaultText, Locale locale, Object... arguments) {
        String text = getText(bundle, key, defaultText, locale);

        if (text != null) {
            return MessageFormat.format(text, arguments);
        }

        return text;
    }

}
