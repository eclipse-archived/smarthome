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

import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.template.TemplateProvider;
import org.eclipse.smarthome.automation.template.TemplateRegistry;
import org.eclipse.smarthome.core.common.registry.AbstractRegistry;
import org.eclipse.smarthome.core.common.registry.Provider;

/**
 * The implementation of {@link TemplateRegistry} that is registered as a service.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - TemplateRegistry extends AbstractRegistry
 */
public class RuleTemplateRegistry extends AbstractRegistry<RuleTemplate, String, Provider<RuleTemplate>>
        implements TemplateRegistry<RuleTemplate> {

    private Collection<TemplateProvider> providers = new HashSet<TemplateProvider>();

    public RuleTemplateRegistry() {
        super(null);
    }

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
    public RuleTemplate get(String templateUID) {
        return get(templateUID, null);
    }

    @Override
    public RuleTemplate get(String templateUID, Locale locale) {
        RuleTemplate resultTemplate = null;
        for (TemplateProvider templateProvider : providers) {
            RuleTemplate template = templateProvider.getTemplate(templateUID, locale);
            if (template != null) {
                resultTemplate = template;
                break;
            }
        }
        return resultTemplate;
    }

    @Override
    public Collection<RuleTemplate> getByTag(String tag) {
        return getByTag(tag, null);
    }

    @Override
    public Collection<RuleTemplate> getByTag(String tag, Locale locale) {
        Collection<RuleTemplate> result = new ArrayList<RuleTemplate>(20);
        Collection<RuleTemplate> templates = null;
        for (TemplateProvider templateProvider : providers) {
            templates = templateProvider.getTemplates(locale);
            if (templates != null) {
                for (Iterator<RuleTemplate> it = templates.iterator(); it.hasNext();) {
                    RuleTemplate t = it.next();
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
    public Collection<RuleTemplate> getByTags(String... tags) {
        return getByTags(null, tags);
    }

    @Override
    public Collection<RuleTemplate> getByTags(Locale locale, String... tags) {
        Set<String> tagSet = tags != null ? new HashSet<String>(Arrays.asList(tags)) : null;
        Collection<RuleTemplate> result = new ArrayList<RuleTemplate>(20);
        Collection<RuleTemplate> templates = null;
        for (TemplateProvider templateProvider : providers) {
            templates = templateProvider.getTemplates(locale);
            if (templates != null) {
                for (Iterator<RuleTemplate> it = templates.iterator(); it.hasNext();) {
                    RuleTemplate t = it.next();
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
    public Collection<RuleTemplate> getAll(Locale locale) {
        return getByTag(null, locale);
    }
}
