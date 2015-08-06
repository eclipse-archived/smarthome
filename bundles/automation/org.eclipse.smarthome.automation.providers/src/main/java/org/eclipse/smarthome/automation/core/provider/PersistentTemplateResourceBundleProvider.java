/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.provider;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.provider.util.PersistableAction;
import org.eclipse.smarthome.automation.provider.util.PersistableCondition;
import org.eclipse.smarthome.automation.provider.util.PersistableLocalizedRuleTemplate;
import org.eclipse.smarthome.automation.provider.util.PersistableRuleTemplate;
import org.eclipse.smarthome.automation.provider.util.PersistableTrigger;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.osgi.framework.BundleContext;

/**
 * This class extends functionality of {@link TemplateResourceBundleProvider} by providing functionality for reading,
 * writing and deleting {@link PersistableRuleTemplate}s from storage.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class PersistentTemplateResourceBundleProvider
        extends TemplateResourceBundleProvider<PersistableLocalizedRuleTemplate> {

    /**
     * This constructor extends the parent constructor functionality with initializing the version of persistence.
     *
     * @param context is the {@code BundleContext}, used for creating a tracker for {@link Parser} services.
     */
    public PersistentTemplateResourceBundleProvider(BundleContext context) {
        super(context);
        isReady = true;
    }

    @Override
    protected String getKey(RuleTemplate element) {
        return element.getUID();
    }

    @Override
    protected String getStorageName() {
        return "providers_templates";
    }

    @Override
    protected String keyToString(String key) {
        return key;
    }

    @Override
    protected RuleTemplate toElement(String key, PersistableLocalizedRuleTemplate persistableElement) {
        Vendor vendor = new Vendor(persistableElement.vendorId, persistableElement.vendorVersion);
        synchronized (providerPortfolio) {
            List<String> portfolio = providerPortfolio.get(vendor);
            if (portfolio == null) {
                portfolio = new ArrayList<String>();
                providerPortfolio.put(vendor, portfolio);
            }
            portfolio.add(key);
        }
        PersistableRuleTemplate[] rts = new PersistableRuleTemplate[persistableElement.localizedTemplates.size()];
        persistableElement.localizedTemplates.toArray(rts);
        String[] languages = new String[persistableElement.languages.size()];
        persistableElement.languages.toArray(languages);
        Localizer l = null;
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
            RuleTemplate rt = new RuleTemplate(key, prt.label, prt.description, prt.tags, triggers, conditions, actions,
                    prt.configDescriptions, prt.visibility);
            if (l == null) {
                l = new Localizer(rt);
            }
            l.addLanguage(languages[i], rt);
        }
        if (l != null) {
            synchronized (providedObjectsHolder) {
                providedObjectsHolder.put(key, l);
            }
            return (RuleTemplate) l.getPerLocale(null);
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
            for (Vendor vendor : providerPortfolio.keySet()) {
                List<String> portfolio = providerPortfolio.get(vendor);
                if (portfolio.contains(uid)) {
                    PersistableLocalizedRuleTemplate pe = new PersistableLocalizedRuleTemplate();
                    pe.vendorId = vendor.getVendorId();
                    pe.vendorVersion = vendor.getVendorVersion();
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
