/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.i18n;

import java.util.Locale;

import org.osgi.framework.Bundle;

/**
 * The {@link I18nProvider} is a service interface for internationalization support within
 * the platform. This service can be used to translate specific keys into its according
 * text by considering the specified {@link Locale} (language). Any module which supports
 * resource files is managed by this provider and used for translation. This service uses
 * the i18n mechanism of Java.
 *
 * @author Michael Grammling - Initial Contribution
 * @author Thomas Höfer - Added getText operation with arguments
 */
public interface I18nProvider {

    /**
     * Returns a translation for the specified key in the specified locale (language) by only
     * considering the translations within the specified module.
     * <p>
     * If no translation could be found, the specified default text is returned.<br>
     * If the specified locale is {@code null}, the default locale is used.
     *
     * @param bundle the module to be used for the look-up (could be null)
     * @param key the key to be translated (could be null or empty)
     * @param defaultText the default text to be used (could be null or empty)
     * @param locale the locale (language) to be used (could be null)
     *
     * @return the translated text or the default text (could be null or empty)
     */
    String getText(Bundle bundle, String key, String defaultText, Locale locale);

    /**
     * Returns a translation for the specified key in the specified locale (language) by only
     * considering the translations within the specified module. The operation will inject the
     * given arguments into the translation.
     * <p>
     * If no translation could be found, the specified default text is returned.<br>
     * If the specified locale is {@code null}, the default locale is used.
     *
     * @param bundle the module to be used for the look-up (could be null)
     * @param key the key to be translated (could be null or empty)
     * @param defaultText the default text to be used (could be null or empty)
     * @param locale the locale (language) to be used (could be null)
     * @param arguments the arguments to be injected into the translation (could be null)
     *
     * @return the translated text or the default text (could be null or empty)
     */
    String getText(Bundle bundle, String key, String defaultText, Locale locale, Object... arguments);

}
