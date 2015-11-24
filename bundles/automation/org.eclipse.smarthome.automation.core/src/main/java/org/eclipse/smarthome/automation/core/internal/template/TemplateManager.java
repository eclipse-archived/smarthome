/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.internal.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.automation.core.internal.RuleEngine;
import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.template.TemplateProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * Internal implementation for handling templates that is independent of the Registry interface.
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
@SuppressWarnings("rawtypes")
public class TemplateManager implements ServiceTrackerCustomizer {

    private ServiceTracker templateProviderTracker;
    private RuleEngine ruleEngine;
    private BundleContext bc;
    private Collection<TemplateProvider> providers = new HashSet<TemplateProvider>();;

    @SuppressWarnings("unchecked")
    public TemplateManager(BundleContext bc, RuleEngine re) {
        this.bc = bc;
        this.ruleEngine = re;
        templateProviderTracker = new ServiceTracker(bc, TemplateProvider.class.getName(), this);
        templateProviderTracker.open();
    }

    public <T extends Template> T get(String templateUID) {
        return get(templateUID, null);
    }

    public <T extends Template> T get(String templateUID, Locale locale) {
        T template = null;
        for (TemplateProvider templateProvider : providers) {
            template = templateProvider.getTemplate(templateUID, locale);
            if (template != null) {
                return template;
            }
        }
        return null;
    }

    public <T extends Template> Collection<T> getTemplatesByTag(String tag) {
        return getByTag(tag, null);
    }

    public <T extends Template> Collection<T> getByTag(String tag, Locale locale) {
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
        return getByTags(tags, null);
    }

    public <T extends Template> Collection<T> getByTags(Set<String> tags, Locale locale) {
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
        return getAll(null);
    }

    public <T extends Template> Collection<T> getAll(Locale locale) {
        return getByTag(null, locale);
    }

    public void dispose() {
        templateProviderTracker.close();
        templateProviderTracker = null;
    }

    @Override
    public Object addingService(ServiceReference reference) {
        @SuppressWarnings("unchecked")
        TemplateProvider provider = (TemplateProvider) bc.getService(reference);
        if (provider != null) {
            providers.add(provider);
            ruleEngine.templateUpdated(provider.getTemplates(null));
        }
        return provider;
    }

    @Override
    public void modifiedService(ServiceReference reference, Object service) {
        TemplateProvider provider = (TemplateProvider) service;
        if (provider != null) {
            ruleEngine.templateUpdated(provider.getTemplates(null));
        }
    }

    @Override
    public void removedService(ServiceReference reference, Object service) {
        providers.remove(service);
    }

}
