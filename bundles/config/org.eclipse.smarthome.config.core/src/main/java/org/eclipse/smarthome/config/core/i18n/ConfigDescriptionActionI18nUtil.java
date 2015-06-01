/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.i18n;

import java.net.URI;
import java.util.Locale;
import java.util.regex.Pattern;

import org.eclipse.smarthome.core.i18n.I18nProvider;
import org.eclipse.smarthome.core.i18n.I18nUtil;
import org.osgi.framework.Bundle;

/**
 * The {@link ConfigDescriptionActionI18nUtil} uses the {@link I18nProvider} to
 * resolve the localized texts. It automatically infers the key if the default
 * text is not a constant.
 *
 * @author Chris Jackson - Initial contribution
 */
public class ConfigDescriptionActionI18nUtil {

    private I18nProvider i18nProvider;

    private static final Pattern delimiter = Pattern.compile("[:=\\s]");

    public ConfigDescriptionActionI18nUtil(I18nProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
    }

    public String getActionPattern(Bundle bundle, URI configDescriptionURI, String actionName,
            String defaultPattern, Locale locale) {
        String key = I18nUtil.isConstant(defaultPattern) ? I18nUtil.stripConstant(defaultPattern) : inferKey(
                configDescriptionURI, actionName, "pattern");
        return i18nProvider.getText(bundle, key, defaultPattern, locale);
    }

    public String getActionDescription(Bundle bundle, URI configDescriptionURI, String actionName,
            String defaultDescription, Locale locale) {
        String key = I18nUtil.isConstant(defaultDescription) ? I18nUtil.stripConstant(defaultDescription) : inferKey(
                configDescriptionURI, actionName, "description");
        return i18nProvider.getText(bundle, key, defaultDescription, locale);
    }

    public String getActionLabel(Bundle bundle, URI configDescriptionURI, String actionName, String defaultLabel,
            Locale locale) {
        String key = I18nUtil.isConstant(defaultLabel) ? I18nUtil.stripConstant(defaultLabel) : inferKey(
                configDescriptionURI, actionName, "label");
        return i18nProvider.getText(bundle, key, defaultLabel, locale);
    }

    public String getActionOptionLabel(Bundle bundle, URI configDescriptionURI, String actionName,
            String optionValue, String defaultOptionLabel, Locale locale) {
        if (!isValidPropertyKey(optionValue))
            return defaultOptionLabel;

        String key = I18nUtil.isConstant(defaultOptionLabel) ? I18nUtil.stripConstant(defaultOptionLabel) : inferKey(
                configDescriptionURI, actionName, "option." + optionValue);

        return i18nProvider.getText(bundle, key, defaultOptionLabel, locale);
    }

    private String inferKey(URI configDescriptionURI, String actionName, String lastSegment) {
        String uri = configDescriptionURI.getSchemeSpecificPart().replace(":", ".");
        return configDescriptionURI.getScheme() + ".config." + uri + ".action." + actionName + "." + lastSegment;
    }

    private boolean isValidPropertyKey(String key) {
        if (key != null) {
            return !delimiter.matcher(key).find();
        }
        return false;
    }

}
