/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.commands;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.provider.util.PersistableAction;
import org.eclipse.smarthome.automation.provider.util.PersistableCondition;
import org.eclipse.smarthome.automation.provider.util.PersistableLocalizedRuleTemplate;
import org.eclipse.smarthome.automation.provider.util.PersistableRuleTemplate;
import org.eclipse.smarthome.automation.provider.util.PersistableTrigger;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.osgi.framework.BundleContext;

/**
 * This class extends functionality of {@link TemplateProviderImpl} by providing functionality for creating,
 * getting and deleting {@link PersistableRuleTemplate}s from storage.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class PersistentTemplateProviderImpl extends TemplateProviderImpl<PersistableLocalizedRuleTemplate> {

    public PersistentTemplateProviderImpl(BundleContext context) {
        super(context, PersistentTemplateProviderImpl.class);
    }

    @Override
    protected String getKey(RuleTemplate element) {
        return element.getUID();
    }

    @Override
    protected String getStorageName() {
        return "commands_templates";
    }

    @Override
    protected String keyToString(String key) {
        return key;
    }

    @Override
    protected RuleTemplate toElement(String key, PersistableLocalizedRuleTemplate persistableElement) {
        try {
            URL url = new URL(persistableElement.url);
            synchronized (providerPortfolio) {
                List<String> portfolio = providerPortfolio.get(url);
                if (portfolio == null) {
                    portfolio = new ArrayList<String>();
                    providerPortfolio.put(url, portfolio);
                }
                portfolio.add(key);
            }
            PersistableRuleTemplate[] rts = new PersistableRuleTemplate[persistableElement.localizedTemplates.size()];
            persistableElement.localizedTemplates.toArray(rts);
            String[] languages = new String[persistableElement.languages.size()];
            persistableElement.languages.toArray(languages);
            Localizer l = new Localizer(rts[0]);
            for (int i = 0; i < rts.length; i++) {
                PersistableRuleTemplate prt = rts[i];
                List<Action> actions = new ArrayList<Action>();
                for (PersistableAction paction : prt.actions) {
                    actions.add(paction.createAction(factory));
                }
                List<Trigger> triggers = new ArrayList<Trigger>();
                for (PersistableTrigger ptrigger : prt.triggers) {
                    triggers.add(ptrigger.createTrigger(factory));
                }
                List<Condition> conditions = new ArrayList<Condition>();
                for (PersistableCondition pcondition : prt.conditions) {
                    conditions.add(pcondition.createCondition(factory));
                }
                RuleTemplate rt = new RuleTemplate(key, prt.label, prt.description, prt.tags, triggers, conditions,
                        actions, prt.configDescriptions, prt.visibility);
                l.addLanguage(languages[i], rt);
            }
            synchronized (providedObjectsHolder) {
                providedObjectsHolder.put(key, l);
            }
            return (RuleTemplate) l.getPerLocale(null);
        } catch (MalformedURLException notPossible) {
        }
        return null;
    }

    @Override
    protected PersistableLocalizedRuleTemplate toPersistableElement(RuleTemplate element) {
        String uid = element.getUID();
        Localizer lTemplate = null;
        synchronized (providedObjectsHolder) {
            lTemplate = providedObjectsHolder.get(uid);
        }
        synchronized (providerPortfolio) {
            for (URL url : providerPortfolio.keySet()) {
                List<String> portfolio = providerPortfolio.get(url);
                if (portfolio.contains(uid)) {
                    PersistableLocalizedRuleTemplate pe = new PersistableLocalizedRuleTemplate();
                    pe.url = url.toString();
                    pe.languages = lTemplate.getAvailableLanguages();
                    for (String language : pe.languages) {
                        RuleTemplate rt = (RuleTemplate) lTemplate.getPerLanguage(language);
                        pe.localizedTemplates.add(new PersistableRuleTemplate(rt));
                    }
                    return pe;
                }
            }
        }
        return null;
    }
}
