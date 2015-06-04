/*******************************************************************************
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ProSyst Software GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.smarthome.automation.core.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.provider.TemplateProvider;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.template.Template;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Yordan Mihaylov - Initial Contribution
 */
public class TemplateManager {

    private ServiceTracker templateProviderTracker;

    /**
     * @param bc
     */
    public TemplateManager(BundleContext bc) {
        templateProviderTracker = new ServiceTracker(bc, TemplateProvider.class.getName(), null);
        templateProviderTracker.open();
    }

    public Template getTemplate(String templateUID) {
        return getTemplate(templateUID, null);
    }

    public Template getTemplate(String templateUID, Locale locale) {
        Template template = null;
        Object[] providers = templateProviderTracker.getServices();
        for (int i = 0; providers != null && i < providers.length; i++) {
            template = ((TemplateProvider) providers[i]).getTemplate(templateUID, locale);
            if (template != null) {
                return createCopy(template);
            }
        }
        return null;
    }

    public Collection<Template> getTemplatesByTag(String tag) {
        return getTemplatesByTag(tag, null);
    }

    public Collection<Template> getTemplatesByTag(String tag, Locale locale) {
        Collection<Template> result = new ArrayList<Template>(20);
        Collection<Template> templates = null;
        Object[] providers = templateProviderTracker.getServices();
        for (int i = 0; providers != null && i < providers.length; i++) {
            templates = ((TemplateProvider) providers[i]).getTemplates(locale);
            if (templates != null) {
                for (Iterator<Template> it = templates.iterator(); it.hasNext();) {
                    Template t = it.next();
                    if (tag != null) {
                        Collection<String> tags = t.getTags();
                        if (tags != null && tags.contains(tag)) {
                            result.add(createCopy(t));
                        }
                    } else {
                        result.add(createCopy(t));
                    }
                }
            }
        }
        return result;
    }

    public Collection<Template> getTemplates() {
        return getTemplates(null);
    }

    public Collection<Template> getTemplates(Locale locale) {
        return getTemplatesByTag(null, locale);
    }

    public void dispose() {
        templateProviderTracker.close();
        templateProviderTracker = null;
    }

    private Template createCopy(Template template) {
        if (template == null) {
            return null;
        }
        if (template instanceof RuleTemplate) {
            RuleTemplate rt = (RuleTemplate) template;
            RuleTemplate t = new RuleTemplate(template.getUID(),//
                    rt.getModules(Trigger.class), //
                    rt.getModules(Condition.class), //
                    rt.getModules(Action.class), //
                    rt.getConfigurationDescription(), rt.getVisibility());
            t.setTags(rt.getTags());
            t.setLabel(rt.getLabel());
            t.setDescription(rt.getDescription());
            return t;
        }
        throw new IllegalArgumentException("Invalid template type: " + template);
    }
}
