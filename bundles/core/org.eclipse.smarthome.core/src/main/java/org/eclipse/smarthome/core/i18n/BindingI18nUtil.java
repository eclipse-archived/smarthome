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
 * The {@link BindingI18nUtil} uses the {@link I18nProvider} to resolve the
 * localized texts. It automatically infers the key if the default text is not a
 * constant.
 *
 * @author Dennis Nobel - Initial contribution
 */
public class BindingI18nUtil {

    private I18nProvider i18nProvider;

    public BindingI18nUtil(I18nProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
    }

    public String getDescription(Bundle bundle, String bindingId, String defaultDescription, Locale locale) {

        String key = I18nUtil.isConstant(defaultDescription) ? I18nUtil.stripConstant(defaultDescription) : inferKey(
                bindingId, "description");

        return i18nProvider.getText(bundle, key, defaultDescription, locale);
    }

    public String getName(Bundle bundle, String bindingId, String defaultLabel, Locale locale) {
        String key = I18nUtil.isConstant(defaultLabel) ? I18nUtil.stripConstant(defaultLabel) : inferKey(bindingId,
                "name");

        return i18nProvider.getText(bundle, key, defaultLabel, locale);
    }

    private String inferKey(String bindingId, String lastSegment) {
        return "binding." + bindingId + "." + lastSegment;
    }

}
