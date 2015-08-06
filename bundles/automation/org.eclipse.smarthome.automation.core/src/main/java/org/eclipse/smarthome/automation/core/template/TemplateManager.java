/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.template.TemplateProvider;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Yordan Mihaylov - Initial Contribution
 */
public class TemplateManager {

    @SuppressWarnings("rawtypes")
    private ServiceTracker templateProviderTracker;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public TemplateManager(BundleContext bc) {
        templateProviderTracker = new ServiceTracker(bc, TemplateProvider.class.getName(), null);
        templateProviderTracker.open();
    }

    public <T extends Template> T getTemplate(String templateUID) {
        return getTemplate(templateUID, null);
    }

    public <T extends Template> T getTemplate(String templateUID, Locale locale) {
        T template = null;
        Object[] providers = templateProviderTracker.getServices();
        for (int i = 0; providers != null && i < providers.length; i++) {
            template = ((TemplateProvider) providers[i]).getTemplate(templateUID, locale);
            if (template != null) {
                return template;
            }
        }
        return null;
    }

    public <T extends Template> Collection<T> getTemplatesByTag(String tag) {
        return getTemplatesByTag(tag, null);
    }

    public <T extends Template> Collection<T> getTemplatesByTag(String tag, Locale locale) {
        Collection<T> result = new ArrayList<T>(20);
        Collection<T> templates = null;
        Object[] providers = templateProviderTracker.getServices();
        for (int i = 0; providers != null && i < providers.length; i++) {
            templates = ((TemplateProvider) providers[i]).getTemplates(locale);
            if (templates != null) {
                for (Iterator<T> it = templates.iterator(); it.hasNext();) {
                    T t = it.next();
                    if (tag != null) {
                        Collection<String> tags = t.getTags();
                        if (tags != null && tags.contains(tag)) {
                            result.add(t);
                        }
                    } else {
                        result.add(t);
                    }
                }
            }
        }
        return result;
    }

    public <T extends Template> Collection<T> getTemplatesByTags(Set<String> tags) {
        return getTemplatesByTags(tags, null);
    }

    public <T extends Template> Collection<T> getTemplatesByTags(Set<String> tags, Locale locale) {
        Collection<T> result = new ArrayList<T>(20);
        Collection<T> templates = null;
        Object[] providers = templateProviderTracker.getServices();
        for (int i = 0; providers != null && i < providers.length; i++) {
            templates = ((TemplateProvider) providers[i]).getTemplates(locale);
            if (templates != null) {
                for (Iterator<T> it = templates.iterator(); it.hasNext();) {
                    T t = it.next();
                    if (tags != null) {
                        Collection<String> rTags = t.getTags();
                        if (rTags != null) {
                            for (Iterator<String> itt = rTags.iterator(); itt.hasNext();) {
                                String tag = itt.next();
                                if (tags.contains(tag)) {
                                    result.add(t);
                                    break;
                                }
                            }
                        }
                    } else {
                        result.add(t);
                    }
                }
            }
        }
        return result;
    }

    public <T extends Template> Collection<T> getTemplates() {
        return getTemplates(null);
    }

    public <T extends Template> Collection<T> getTemplates(Locale locale) {
        return getTemplatesByTag(null, locale);
    }

    public void dispose() {
        templateProviderTracker.close();
        templateProviderTracker = null;
    }

}
