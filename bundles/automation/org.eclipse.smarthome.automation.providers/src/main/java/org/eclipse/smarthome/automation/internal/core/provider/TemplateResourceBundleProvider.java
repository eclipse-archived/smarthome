/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.core.provider;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.internal.core.provider.i18n.ConfigDescriptionParameterI18nUtil;
import org.eclipse.smarthome.automation.internal.core.provider.i18n.ModuleI18nUtil;
import org.eclipse.smarthome.automation.internal.core.provider.i18n.RuleTemplateI18nUtil;
import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.parser.ParsingException;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.template.TemplateProvider;
import org.eclipse.smarthome.automation.template.TemplateRegistry;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.core.i18n.I18nProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

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
public class TemplateResourceBundleProvider extends AbstractResourceBundleProvider<Template>
        implements TemplateProvider {

    protected TemplateRegistry templateRegistry;
    protected ModuleTypeRegistry moduleTypeRegistry;

    @SuppressWarnings("rawtypes")
    private ServiceTracker tracker;

    @SuppressWarnings("rawtypes")
    private ServiceRegistration /* <S> */ tpReg;

    @SuppressWarnings("rawtypes")
    private ServiceTracker localizationTracker;

    /**
     * This constructor is responsible for initializing the path to resources and tracking the managing service of the
     * {@link ModuleType}s and the managing service of the {@link RuleTemplates}s.
     *
     * @param context is the {@code BundleContext}, used for creating a tracker for {@link Parser} services.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public TemplateResourceBundleProvider(BundleContext context) {
        super(context);
        path = PATH + "/templates/";
        try {
            Filter filter = bc.createFilter("(|(objectClass=" + TemplateRegistry.class.getName() + ")(objectClass="
                    + ModuleTypeRegistry.class.getName() + "))");
            tracker = new ServiceTracker(bc, filter, new ServiceTrackerCustomizer() {

                @Override
                public Object addingService(ServiceReference reference) {
                    Object service = bc.getService(reference);
                    if (service instanceof TemplateRegistry) {
                        templateRegistry = (TemplateRegistry) service;
                    } else {
                        moduleTypeRegistry = (ModuleTypeRegistry) service;
                    }
                    queue.open();
                    return service;
                }

                @Override
                public void modifiedService(ServiceReference reference, Object service) {
                }

                @Override
                public void removedService(ServiceReference reference, Object service) {
                    if (service == templateRegistry) {
                        templateRegistry = null;
                    } else {
                        moduleTypeRegistry = null;
                    }
                }
            });
        } catch (InvalidSyntaxException notPossible) {
        }
        localizationTracker = new ServiceTracker(bc, I18nProvider.class.getName(), new ServiceTrackerCustomizer() {

            @Override
            public Object addingService(ServiceReference reference) {
                i18nProvider = (I18nProvider) bc.getService(reference);
                return i18nProvider;
            }

            @Override
            public void modifiedService(ServiceReference reference, Object service) {
            }

            @Override
            public void removedService(ServiceReference reference, Object service) {
                i18nProvider = null;
            }
        });
        localizationTracker.open();
    }

    @Override
    public void setQueue(AutomationResourceBundlesEventQueue queue) {
        super.setQueue(queue);
        tracker.open();
    }

    /**
     * This method is inherited from {@link AbstractResourceBundleProvider}. Extends parent's functionality with closing
     * the {@link #tracker} and sets <code>null</code> to {@link #moduleTypeRegistry} and {@link #templateRegistry}.
     *
     * @see org.eclipse.smarthome.automation.internal.core.provider.AbstractResourceBundleProvider#close()
     */
    @Override
    public void close() {
        if (localizationTracker != null) {
            localizationTracker.close();
            localizationTracker = null;
            i18nProvider = null;
        }
        if (tracker != null) {
            tracker.close();
            tracker = null;
            moduleTypeRegistry = null;
            templateRegistry = null;
        }
        if (tpReg != null) {
            tpReg.unregister();
            tpReg = null;
        }
        super.close();
    }

    /**
     * @see TemplateProvider#getTemplate(java.lang.String, java.util.Locale)
     */
    @Override
    public <T extends Template> T getTemplate(String UID, Locale locale) {
        Template defTemplate = null;
        synchronized (providerPortfolio) {
            defTemplate = providedObjectsHolder.get(UID);
        }
        if (defTemplate != null) {
            @SuppressWarnings("unchecked")
            T t = (T) getPerLocale(defTemplate, locale);
            return t;
        }
        return null;
    }

    /**
     * @see TemplateProvider#getTemplates(java.util.Locale)
     */
    @Override
    public Collection<Template> getTemplates(Locale locale) {
        ArrayList<Template> templatesList = new ArrayList<Template>();
        synchronized (providedObjectsHolder) {
            Iterator<Template> i = providedObjectsHolder.values().iterator();
            while (i.hasNext()) {
                Template defTemplate = i.next();
                if (defTemplate != null) {
                    Template t = getPerLocale(defTemplate, locale);
                    if (t != null) {
                        templatesList.add(t);
                    }
                }
            }
        }
        return templatesList;
    }

    /**
     * @see AbstractResourceBundleProvider#addingService(ServiceReference)
     */
    @Override
    public Object addingService(@SuppressWarnings("rawtypes") ServiceReference reference) {
        if (reference.getProperty(Parser.PARSER_TYPE).equals(Parser.PARSER_TEMPLATE)) {
            return super.addingService(reference);
        }
        return null;
    }

    @Override
    public boolean isReady() {
        return moduleTypeRegistry != null && templateRegistry != null && queue != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Set<Template> importData(Vendor vendor, Parser<Template> parser, InputStreamReader inputStreamReader) {
        List<String> portfolio = null;
        if (vendor != null) {
            synchronized (providerPortfolio) {
                portfolio = providerPortfolio.get(vendor);
                if (portfolio == null) {
                    portfolio = new ArrayList<String>();
                    providerPortfolio.put(vendor, portfolio);
                }
            }
        }
        Set<Template> providedObjects = null;
        try {
            providedObjects = parser.parse(inputStreamReader);
        } catch (ParsingException e) {
            logger.error("Template parsing of vendor {} is faild!", vendor, e);
        }
        if (providedObjects != null && !providedObjects.isEmpty()) {
            for (Template ruleT : providedObjects) {
                String uid = ruleT.getUID();
                if (checkExistence(uid)) {
                    continue;
                }
                if (portfolio != null) {
                    portfolio.add(uid);
                }
                synchronized (providedObjectsHolder) {
                    providedObjectsHolder.put(uid, ruleT);
                }
            }
            Dictionary<String, Object> properties = new Hashtable<String, Object>();
            properties.put(REG_PROPERTY_RULE_TEMPLATES, providedObjectsHolder.keySet());
            if (tpReg == null) {
                tpReg = bc.registerService(TemplateProvider.class.getName(), this, properties);
            } else {
                tpReg.setProperties(properties);
            }
        }
        return providedObjects;
    }

    /**
     * This method is responsible for checking the existence of {@link ModuleType}s or {@link Template}s with the same
     * UIDs before these objects to be added in the system.
     *
     * @param uid UID of the newly created {@link Template}, which to be checked.
     * @return {@code true} if {@link Template} with the same UID exists or {@code false} in the opposite
     *         case.
     */
    private boolean checkExistence(String uid) {
        if (templateRegistry != null && templateRegistry.get(uid) != null) {
            logger.error("Rule Template with UID \"{}\" already exists! Failed to create a second with the same UID!", uid,
                    new IllegalArgumentException());
            return true;
        }
        return false;
    }

    /**
     * This method is used to localize the {@link Template}s.
     *
     * @param element is the {@link Template} that must be localized.
     * @param locale represents a specific geographical, political, or cultural region.
     * @return the localized {@link Template}.
     */
    private Template getPerLocale(Template defTemplate, Locale locale) {
        if (locale == null) {
            return defTemplate;
        }
        String uid = defTemplate.getUID();
        Bundle bundle = getBundle(uid);
        if (defTemplate instanceof RuleTemplate) {
            String llabel = RuleTemplateI18nUtil.getLocalizedRuleTemplateLabel(i18nProvider, bundle, uid,
                    defTemplate.getLabel(), locale);
            String ldescription = RuleTemplateI18nUtil.getLocalizedRuleTemplateDescription(i18nProvider, bundle, uid,
                    defTemplate.getDescription(), locale);
            List<ConfigDescriptionParameter> lconfigDescriptions = ConfigDescriptionParameterI18nUtil
                    .getLocalizedConfigurationDescription(i18nProvider,
                            ((RuleTemplate) defTemplate).getConfigurationDescription(), bundle, uid,
                            RuleTemplateI18nUtil.RULE_TEMPLATE, locale);
            List<Action> lactions = ModuleI18nUtil.getLocalizedModules(i18nProvider,
                    ((RuleTemplate) defTemplate).getActions(), bundle, uid, RuleTemplateI18nUtil.RULE_TEMPLATE, locale);
            List<Condition> lconditions = ModuleI18nUtil.getLocalizedModules(i18nProvider,
                    ((RuleTemplate) defTemplate).getConditions(), bundle, uid, RuleTemplateI18nUtil.RULE_TEMPLATE,
                    locale);
            List<Trigger> ltriggers = ModuleI18nUtil.getLocalizedModules(i18nProvider,
                    ((RuleTemplate) defTemplate).getTriggers(), bundle, uid, RuleTemplateI18nUtil.RULE_TEMPLATE,
                    locale);
            return new RuleTemplate(uid, llabel, ldescription, ((RuleTemplate) defTemplate).getTags(), ltriggers,
                    lconditions, lactions, lconfigDescriptions, ((RuleTemplate) defTemplate).getVisibility());
        }
        return null;
    }

}
