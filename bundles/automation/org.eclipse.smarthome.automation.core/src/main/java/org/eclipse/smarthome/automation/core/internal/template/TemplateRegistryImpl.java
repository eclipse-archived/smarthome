/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.internal.template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.template.TemplateProvider;
import org.eclipse.smarthome.automation.template.TemplateRegistry;

/**
 * The implementation of {@link TemplateRegistry} that is registered as a service.
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
public class TemplateRegistryImpl implements TemplateRegistry {

    private Collection<TemplateProvider> providers = new HashSet<TemplateProvider>();

    /**
     * Called from DS.
     *
     * @param templateProvider
     */
    protected void addTemplateProvider(TemplateProvider templateProvider) {
        providers.add(templateProvider);
    }

    /**
     * Called from DS.
     *
     * @param templateProvider
     */
    protected void removeTemplateProvider(TemplateProvider templateProvider) {
        providers.remove(templateProvider);
    }

    @Override
    public <T extends Template> T get(String templateUID) {
        return get(templateUID, null);
    }

    @Override
    public <T extends Template> T get(String templateUID, Locale locale) {
        T resultTemplate = null;
        for (TemplateProvider templateProvider : providers) {
            T template = templateProvider.getTemplate(templateUID, locale);
            if (template != null) {
                resultTemplate = template;
                break;
            }
        }
        return resultTemplate;
    }

    @Override
    public <T extends Template> Collection<T> getByTag(String tag) {
        return getByTag(tag, null);
    }

    @Override
    public <T extends Template> Collection<T> getByTag(String tag, Locale locale) {
        Collection<T> result = new ArrayList<T>(20);
        Collection<T> templates = null;
        for (TemplateProvider templateProvider : providers) {
            templates = templateProvider.getTemplates(locale);
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

    @Override
    public <T extends Template> Collection<T> getByTags(String... tags) {
        return getByTags(null, tags);
    }

    @Override
    public <T extends Template> Collection<T> getByTags(Locale locale, String... tags) {
        Set<String> tagSet = tags != null ? new HashSet<String>(Arrays.asList(tags)) : null;
        Collection<T> result = new ArrayList<T>(20);
        Collection<T> templates = null;
        for (TemplateProvider templateProvider : providers) {
            templates = templateProvider.getTemplates(locale);
            if (templates != null) {
                for (Iterator<T> it = templates.iterator(); it.hasNext();) {
                    T t = it.next();
                    if (tagSet != null) {
                        Collection<String> tTags = t.getTags();
                        if (tTags != null) {
                            if (tTags.containsAll(tagSet)) {
                                result.add(t);
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

    @Override
    public <T extends Template> Collection<T> getAll() {
        return getAll(null);
    }

    @Override
    public <T extends Template> Collection<T> getAll(Locale locale) {
        return getByTag(null, locale);
    }

}
