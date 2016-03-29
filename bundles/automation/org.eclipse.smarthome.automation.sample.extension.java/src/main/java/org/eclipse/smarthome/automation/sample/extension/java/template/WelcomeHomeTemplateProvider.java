/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.sample.extension.java.template;

import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.template.TemplateProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * The purpose of this class is to illustrate how to provide Rule Templates and how to use them for creation of the
 * {@link Rule}s. Of course, the templates are not mandatory for the creation of rules, the rules also can be created
 * directly.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class WelcomeHomeTemplateProvider implements TemplateProvider {

    private Map<String, RuleTemplate> providedRuleTemplates;
    @SuppressWarnings("rawtypes")
    private ServiceRegistration providerReg;

    public WelcomeHomeTemplateProvider() {
        providedRuleTemplates = new HashMap<String, RuleTemplate>();
        providedRuleTemplates.put(AirConditionerRuleTemplate.UID, AirConditionerRuleTemplate.initialize());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Template> T getTemplate(String UID, Locale locale) {
        return (T) providedRuleTemplates.get(UID);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Template> Collection<T> getTemplates(Locale locale) {
        return (Collection<T>) providedRuleTemplates.values();
    }

    /**
     * To provide the {@link Template}s should register the WelcomeHomeTemplateProvider as {@link TemplateProvider}
     * service.
     *
     * @param bc
     *            is a bundle's execution context within the Framework.
     */
    public void register(BundleContext bc) {
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(REG_PROPERTY_RULE_TEMPLATES, providedRuleTemplates.keySet());
        providerReg = bc.registerService(TemplateProvider.class.getName(), this, properties);
    }

    /**
     * This method unregisters the WelcomeHomeTemplateProvider as {@link TemplateProvider}
     * service.
     */
    public void unregister() {
        providerReg.unregister();
        providerReg = null;
        providedRuleTemplates = null;
    }

}
