/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.core.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.internal.core.provider.i18n.ModuleI18nUtil;
import org.eclipse.smarthome.automation.internal.core.provider.i18n.RuleTemplateI18nUtil;
import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.template.RuleTemplateProvider;
import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.template.TemplateProvider;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;
import org.osgi.framework.Bundle;

/**
 * This class is implementation of {@link TemplateProvider}. It serves for providing {@link RuleTemplates}s by loading
 * bundle resources. It extends functionality of {@link AbstractResourceBundleProvider} by specifying:
 * <ul>
 * <li>the path to resources, corresponding to the {@link RuleTemplates}s - root directory
 * {@link AbstractResourceBundleProvider#PATH} with sub-directory "templates".
 * <li>type of the {@link Parser}s, corresponding to the {@link RuleTemplates}s - {@link Parser#PARSER_TEMPLATE}
 * <li>specific functionality for loading the {@link RuleTemplates}s
 * <li>tracking the managing service of the {@link ModuleType}s.
 * <li>tracking the managing of the {@link RuleTemplates}s.
 * </ul>
 *
 * @author Ana Dimova - Initial Contribution
 * @author Kai Kreuzer - refactored (managed) provider and registry implementation
 * @author Yordan Mihaylov - updates related to api changes
 */
public class TemplateResourceBundleProvider extends AbstractResourceBundleProvider<RuleTemplate>
        implements RuleTemplateProvider {

    /**
     * This constructor is responsible for initializing the path to resources and tracking the managing service of the
     * {@link ModuleType}s and the managing service of the {@link RuleTemplates}s.
     *
     * @param context is the {@code BundleContext}, used for creating a tracker for {@link Parser} services.
     */
    public TemplateResourceBundleProvider() {
        listeners = new LinkedList<ProviderChangeListener<RuleTemplate>>();
        path = PATH + "/templates/";
    }

    @Override
    public Collection<RuleTemplate> getAll() {
        return providedObjectsHolder.values();
    }

    @Override
    public void addProviderChangeListener(ProviderChangeListener<RuleTemplate> listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeProviderChangeListener(ProviderChangeListener<RuleTemplate> listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * @see TemplateProvider#getTemplate(java.lang.String, java.util.Locale)
     */
    @Override
    public RuleTemplate getTemplate(String UID, Locale locale) {
        return getPerLocale(providedObjectsHolder.get(UID), locale);
    }

    /**
     * @see TemplateProvider#getTemplates(java.util.Locale)
     */
    @Override
    public Collection<RuleTemplate> getTemplates(Locale locale) {
        ArrayList<RuleTemplate> templatesList = new ArrayList<RuleTemplate>();
        for (RuleTemplate t : providedObjectsHolder.values()) {
            templatesList.add(getPerLocale(t, locale));
        }
        return templatesList;
    }

    /**
     * This method is used to localize the {@link Template}s.
     *
     * @param element is the {@link Template} that must be localized.
     * @param locale represents a specific geographical, political, or cultural region.
     * @return the localized {@link Template}.
     */
    private RuleTemplate getPerLocale(RuleTemplate defTemplate, Locale locale) {
        if (locale == null || defTemplate == null || i18nProvider == null) {
            return defTemplate;
        }
        String uid = defTemplate.getUID();
        Bundle bundle = getBundle(uid);
        if (defTemplate instanceof RuleTemplate) {
            String llabel = RuleTemplateI18nUtil.getLocalizedRuleTemplateLabel(i18nProvider, bundle, uid,
                    defTemplate.getLabel(), locale);
            String ldescription = RuleTemplateI18nUtil.getLocalizedRuleTemplateDescription(i18nProvider, bundle, uid,
                    defTemplate.getDescription(), locale);
            List<ConfigDescriptionParameter> lconfigDescriptions = getLocalizedConfigurationDescription(i18nProvider,
                    defTemplate.getConfigurationDescriptions(), bundle, uid, RuleTemplateI18nUtil.RULE_TEMPLATE,
                    locale);
            List<Action> lactions = ModuleI18nUtil.getLocalizedModules(i18nProvider, defTemplate.getActions(), bundle,
                    uid, RuleTemplateI18nUtil.RULE_TEMPLATE, locale);
            List<Condition> lconditions = ModuleI18nUtil.getLocalizedModules(i18nProvider, defTemplate.getConditions(),
                    bundle, uid, RuleTemplateI18nUtil.RULE_TEMPLATE, locale);
            List<Trigger> ltriggers = ModuleI18nUtil.getLocalizedModules(i18nProvider, defTemplate.getTriggers(),
                    bundle, uid, RuleTemplateI18nUtil.RULE_TEMPLATE, locale);
            return new RuleTemplate(uid, llabel, ldescription, defTemplate.getTags(), ltriggers, lconditions, lactions,
                    lconfigDescriptions, defTemplate.getVisibility());
        }
        return null;
    }

    @Override
    protected String getUID(RuleTemplate parsedObject) {
        return parsedObject.getUID();
    }

}
