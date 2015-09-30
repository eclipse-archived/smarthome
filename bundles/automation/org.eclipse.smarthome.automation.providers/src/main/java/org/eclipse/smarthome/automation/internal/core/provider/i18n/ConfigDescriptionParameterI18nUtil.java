/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.core.provider.i18n;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.core.i18n.I18nProvider;
import org.eclipse.smarthome.core.i18n.I18nUtil;
import org.osgi.framework.Bundle;

/**
 * This class is used as utility for resolving the localized {@link ConfigDescriptionParameter}s. It automatically
 * infers the key if the default text is not a constant with the assistance of {@link I18nProvider}.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class ConfigDescriptionParameterI18nUtil {

    private static final Pattern delimiter = Pattern.compile("[:=\\s]");

    public static Set<ConfigDescriptionParameter> getLocalizedConfigurationDescription(I18nProvider i18nProvider,
            Set<ConfigDescriptionParameter> config, Bundle bundle, String uid, String prefix, Locale locale) {
        Set<ConfigDescriptionParameter> configDescriptions = new HashSet<ConfigDescriptionParameter>();
        for (ConfigDescriptionParameter parameter : config) {
            String parameterName = parameter.getName();
            String llabel = getModuleTypeConfigParameterLabel(i18nProvider, bundle, uid, parameterName,
                    parameter.getLabel(), prefix, locale);
            String ldescription = getModuleTypeConfigParameterDescription(i18nProvider, bundle, uid, parameterName,
                    parameter.getDescription(), prefix, locale);
            String lpattern = getParameterPattern(i18nProvider, bundle, uid, parameterName, parameter.getPattern(),
                    prefix, locale);
            List<ParameterOption> loptions = getLocalizedOptions(i18nProvider, parameter.getOptions(), bundle, uid,
                    parameterName, prefix, locale);
            configDescriptions.add(new ConfigDescriptionParameter(parameterName, parameter.getType(),
                    parameter.getMinimum(), parameter.getMaximum(), parameter.getStepSize(), lpattern,
                    parameter.isMultiple(), parameter.isReadOnly(), parameter.isMultiple(), parameter.getContext(),
                    parameter.getDefault(), llabel, ldescription, loptions, parameter.getFilterCriteria(),
                    parameter.getGroupName(), parameter.isAdvanced(), parameter.getLimitToOptions(),
                    parameter.getMultipleLimit()));
        }
        return configDescriptions;
    }

    private static String getParameterPattern(I18nProvider i18nProvider, Bundle bundle, String uid,
            String parameterName, String defaultPattern, String prefix, Locale locale) {
        String key = I18nUtil.isConstant(defaultPattern) ? I18nUtil.stripConstant(defaultPattern)
                : inferKey(prefix, uid, parameterName, "pattern");
        return i18nProvider.getText(bundle, key, defaultPattern, locale);
    }

    private static String getModuleTypeConfigParameterLabel(I18nProvider i18nProvider, Bundle bundle, String uid,
            String parameterName, String defaultLabel, String prefix, Locale locale) {
        String key = I18nUtil.isConstant(defaultLabel) ? I18nUtil.stripConstant(defaultLabel)
                : inferKey(prefix, uid, parameterName, "label");
        return i18nProvider.getText(bundle, key, defaultLabel, locale);
    }

    private static String getModuleTypeConfigParameterDescription(I18nProvider i18nProvider, Bundle bundle, String uid,
            String parameterName, String defaultDescription, String prefix, Locale locale) {
        String key = I18nUtil.isConstant(defaultDescription) ? I18nUtil.stripConstant(defaultDescription)
                : inferKey(prefix, uid, parameterName, "description");
        return i18nProvider.getText(bundle, key, defaultDescription, locale);
    }

    private static List<ParameterOption> getLocalizedOptions(I18nProvider i18nProvider,
            List<ParameterOption> originalOptions, Bundle bundle, String uid, String parameterName, String prefix,
            Locale locale) {
        if (originalOptions == null || originalOptions.isEmpty())
            return originalOptions;
        List<ParameterOption> localizedOptions = new ArrayList<ParameterOption>();
        for (ParameterOption option : originalOptions) {
            String localizedLabel = getParameterOptionLabel(i18nProvider, bundle, uid, parameterName, option.getValue(),
                    option.getLabel(), prefix, locale);
            ParameterOption localizedOption = new ParameterOption(option.getValue(), localizedLabel);
            localizedOptions.add(localizedOption);
        }
        return localizedOptions;
    }

    private static String getParameterOptionLabel(I18nProvider i18nProvider, Bundle bundle, String uid,
            String parameterName, String optionValue, String defaultOptionLabel, String prefix, Locale locale) {
        if (!isValidPropertyKey(optionValue))
            return defaultOptionLabel;
        String key = I18nUtil.isConstant(defaultOptionLabel) ? I18nUtil.stripConstant(defaultOptionLabel)
                : inferKey(prefix, uid, parameterName, "option." + optionValue);
        return i18nProvider.getText(bundle, key, defaultOptionLabel, locale);
    }

    private static String inferKey(String prefix, String uid, String parameterName, String lastSegment) {
        return prefix + uid + ".config." + parameterName + "." + lastSegment;
    }

    private static boolean isValidPropertyKey(String key) {
        if (key != null) {
            return !delimiter.matcher(key).find();
        }
        return false;
    }

}
