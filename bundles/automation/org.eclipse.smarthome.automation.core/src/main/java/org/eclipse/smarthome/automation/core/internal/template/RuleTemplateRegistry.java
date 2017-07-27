/*******************************************************************************
 *
 * Copyright (c) 2016  Bosch Software Innovations GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * The Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 *******************************************************************************/
package org.eclipse.smarthome.automation.core.internal.template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.template.RuleTemplateProvider;
import org.eclipse.smarthome.automation.template.TemplateProvider;
import org.eclipse.smarthome.automation.template.TemplateRegistry;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.common.registry.AbstractRegistry;
import org.eclipse.smarthome.core.common.registry.Provider;

/**
 * The implementation of {@link TemplateRegistry} that is registered as a service.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - TemplateRegistry extends AbstractRegistry
 */
public class RuleTemplateRegistry extends AbstractRegistry<RuleTemplate, String, RuleTemplateProvider>
        implements TemplateRegistry<RuleTemplate> {

    public RuleTemplateRegistry() {
        super(RuleTemplateProvider.class);
    }

    @Override
    protected void addProvider(Provider<RuleTemplate> provider) {
        if (provider instanceof TemplateProvider) {
            super.addProvider(provider);
        }
    }

    @Override
    public RuleTemplate get(String templateUID) {
        return get(templateUID, null);
    }

    @Override
    public RuleTemplate get(String templateUID, Locale locale) {
        for (Provider<RuleTemplate> provider : elementMap.keySet()) {
            for (RuleTemplate resultTemplate : elementMap.get(provider)) {
                if (resultTemplate.getUID().equals(templateUID)) {
                    RuleTemplate t = locale == null ? resultTemplate
                            : ((RuleTemplateProvider) provider).getTemplate(templateUID, locale);
                    return createCopy(t);
                }
            }
        }
        return null;
    }

    private RuleTemplate createCopy(RuleTemplate template) {
        return new RuleTemplate(template.getUID(), template.getLabel(), template.getDescription(),
                new HashSet<String>(template.getTags()), copyTriggers(template.getTriggers()),
                copyConditions(template.getConditions()), copyActions(template.getActions()),
                new LinkedList<ConfigDescriptionParameter>(template.getConfigurationDescriptions()),
                template.getVisibility());
    }

    private List<Trigger> copyTriggers(List<Trigger> triggers) {
        List<Trigger> res = new ArrayList<Trigger>(11);
        if (triggers != null) {
            for (Trigger t : triggers) {
                Configuration c = new Configuration();
                c.setProperties(t.getConfiguration().getProperties());
                Trigger trigger = new Trigger(t.getId(), t.getTypeUID(), c);
                trigger.setLabel(t.getLabel());
                trigger.setDescription(t.getDescription());
                res.add(trigger);
            }
        }
        return res;
    }

    private List<Condition> copyConditions(List<Condition> conditions) {
        List<Condition> res = new ArrayList<Condition>(11);
        if (conditions != null) {
            for (Condition c : conditions) {
                Configuration conf = new Configuration();
                conf.setProperties(c.getConfiguration().getProperties());
                Condition condition = new Condition(c.getId(), c.getTypeUID(), conf,
                        new HashMap<String, String>(c.getInputs()));
                condition.setLabel(c.getLabel());
                condition.setDescription(c.getDescription());
                res.add(condition);
            }
        }
        return res;
    }

    private List<Action> copyActions(List<Action> actions) {
        List<Action> res = new ArrayList<Action>();
        if (actions != null) {
            for (Action a : actions) {
                Configuration c = new Configuration();
                c.setProperties(a.getConfiguration().getProperties());
                Action action = new Action(a.getId(), a.getTypeUID(), c, a.getInputs());
                action.setLabel(a.getLabel());
                action.setDescription(a.getDescription());
                res.add(action);
            }
        }
        return res;
    }

    @Override
    public Collection<RuleTemplate> getByTag(String tag) {
        return getByTag(tag, null);
    }

    @Override
    public Collection<RuleTemplate> getByTag(String tag, Locale locale) {
        Collection<RuleTemplate> result = new ArrayList<RuleTemplate>(20);
        for (Provider<RuleTemplate> provider : elementMap.keySet()) {
            for (RuleTemplate resultTemplate : elementMap.get(provider)) {
                Collection<String> tags = resultTemplate.getTags();
                RuleTemplate t = locale == null ? resultTemplate
                        : ((RuleTemplateProvider) provider).getTemplate(resultTemplate.getUID(), locale);
                if (tag == null) {
                    result.add(t);
                } else if (tags != null && tags.contains(tag)) {
                    result.add(t);
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
        for (Provider<RuleTemplate> provider : elementMap.keySet()) {
            for (RuleTemplate resultTemplate : elementMap.get(provider)) {
                Collection<String> tTags = resultTemplate.getTags();
                RuleTemplate t = locale == null ? resultTemplate
                        : ((RuleTemplateProvider) provider).getTemplate(resultTemplate.getUID(), locale);
                if (tagSet == null) {
                    result.add(t);
                } else if (tTags != null && tTags.containsAll(tagSet)) {
                    result.add(t);
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
