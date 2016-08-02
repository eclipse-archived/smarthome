/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.i18n;

import java.net.URI;
import java.util.Locale;

import org.eclipse.smarthome.core.i18n.I18nProvider;
import org.eclipse.smarthome.core.i18n.I18nUtil;
import org.osgi.framework.Bundle;

/**
 * The {@link ConfigDescriptionGroupI18nUtil} uses the {@link I18nProvider} to
 * resolve the localized texts in the configuration parameter groups. It automatically infers the key if the default
 * text is not a constant.
 *
 * @author Chris Jackson - Initial contribution
 */
public class ConfigDescriptionGroupI18nUtil {

    private I18nProvider i18nProvider;

    public ConfigDescriptionGroupI18nUtil(I18nProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
    }

    public String getGroupDescription(Bundle bundle, URI configDescriptionURI, String groupName,
            String defaultDescription, Locale locale) {
        String key = I18nUtil.isConstant(defaultDescription) ? I18nUtil.stripConstant(defaultDescription) : inferKey(
                configDescriptionURI, groupName, "description");
        return i18nProvider.getText(bundle, key, defaultDescription, locale);
    }

    public String getGroupLabel(Bundle bundle, URI configDescriptionURI, String groupName, String defaultLabel,
            Locale locale) {
        String key = I18nUtil.isConstant(defaultLabel) ? I18nUtil.stripConstant(defaultLabel) : inferKey(
                configDescriptionURI, groupName, "label");
        return i18nProvider.getText(bundle, key, defaultLabel, locale);
    }

    private String inferKey(URI configDescriptionURI, String groupName, String lastSegment) {
        String uri = configDescriptionURI.getSchemeSpecificPart().replace(":", ".");
        return configDescriptionURI.getScheme() + ".config." + uri + ".group." + groupName + "." + lastSegment;
    }
}
