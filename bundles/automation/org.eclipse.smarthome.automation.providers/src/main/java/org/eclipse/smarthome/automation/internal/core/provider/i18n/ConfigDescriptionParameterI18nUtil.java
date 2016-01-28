/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.core.provider.i18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterBuilder;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.core.i18n.I18nProvider;
import org.eclipse.smarthome.core.i18n.I18nUtil;
import org.osgi.framework.Bundle;

/**
 * This class is used as utility for resolving the localized {@link ConfigDescriptionParameter}s. It automatically
 * infers the key if the default text is not a constant with the assistance of {@link I18nProvider}.
 *
 * @author Ana Dimova - Initial Contribution
 * @author Yordan Mihaylov - updates related to api changes
 * @author Thomas Höfer - Added config description parameter unit
 */
public class ConfigDescriptionParameterI18nUtil {

    private static final Pattern delimiter = Pattern.compile("[:=\\s]");

    public static List<ConfigDescriptionParameter> getLocalizedConfigurationDescription(I18nProvider i18nProvider,
            List<ConfigDescriptionParameter> config, Bundle bundle, String uid, String prefix, Locale locale) {
        List<ConfigDescriptionParameter> configDescriptions = new ArrayList<ConfigDescriptionParameter>();
        if (config != null) {
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
                String lunitLabel = getUnitLabel(i18nProvider, bundle, uid, parameterName, parameter.getUnitLabel(),
                        prefix, locale);

                configDescriptions.add(ConfigDescriptionParameterBuilder.create(parameterName, parameter.getType())
                        .withMinimum(parameter.getMinimum()).withMaximum(parameter.getMaximum())
                        .withStepSize(parameter.getStepSize()).withPattern(lpattern)
                        .withRequired(parameter.isRequired()).withMultiple(parameter.isMultiple())
                        .withReadOnly(parameter.isReadOnly()).withContext(parameter.getContext())
                        .withDefault(parameter.getDefault()).withLabel(llabel).withDescription(ldescription)
                        .withFilterCriteria(parameter.getFilterCriteria()).withGroupName(parameter.getGroupName())
                        .withAdvanced(parameter.isAdvanced()).withOptions(loptions)
                        .withLimitToOptions(parameter.getLimitToOptions())
                        .withMultipleLimit(parameter.getMultipleLimit()).withUnit(parameter.getUnit())
                        .withUnitLabel(lunitLabel).build());
            }
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

    private static String getUnitLabel(I18nProvider i18nProvider, Bundle bundle, String uid, String parameterName,
            String defaultUnitLabel, String prefix, Locale locale) {
        String key = I18nUtil.isConstant(defaultUnitLabel) ? I18nUtil.stripConstant(defaultUnitLabel)
                : inferKey(prefix, uid, parameterName, "unitLabel");
        return i18nProvider.getText(bundle, key, defaultUnitLabel, locale);
    }

    private static List<ParameterOption> getLocalizedOptions(I18nProvider i18nProvider,
            List<ParameterOption> originalOptions, Bundle bundle, String uid, String parameterName, String prefix,
            Locale locale) {
        if (originalOptions == null || originalOptions.isEmpty()) {
            return originalOptions;
        }
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
        if (!isValidPropertyKey(optionValue)) {
            return defaultOptionLabel;
        }
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
