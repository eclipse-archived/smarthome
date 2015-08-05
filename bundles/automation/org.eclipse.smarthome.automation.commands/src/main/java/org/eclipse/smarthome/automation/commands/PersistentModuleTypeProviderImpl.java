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
import java.util.Set;

import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.provider.util.PersistableCompositeActionType;
import org.eclipse.smarthome.automation.provider.util.PersistableCompositeConditionType;
import org.eclipse.smarthome.automation.provider.util.PersistableCompositeTriggerType;
import org.eclipse.smarthome.automation.provider.util.PersistableModuleType;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.CompositeActionType;
import org.eclipse.smarthome.automation.type.CompositeConditionType;
import org.eclipse.smarthome.automation.type.CompositeTriggerType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.osgi.framework.BundleContext;

/**
 * This class extends functionality of {@link ModuleTypeProviderImpl} by providing functionality for creating,
 * getting and deleting {@link PersistableModuleType}s from storage.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class PersistentModuleTypeProviderImpl extends ModuleTypeProviderImpl<PersistableModuleType> {

    /**
     * This constructor extends the parent constructor functionality with initializing the version of persistence.
     *
     * @param context is the {@code BundleContext}, used for creating a tracker for {@link Parser} services.
     */
    public PersistentModuleTypeProviderImpl(BundleContext context) {
        super(context, PersistentModuleTypeProviderImpl.class);
    }

    @Override
    protected String getKey(ModuleType element) {
        return element.getUID();
    }

    @Override
    protected String getStorageName() {
        return "commands_module_types";
    }

    @Override
    protected String keyToString(String key) {
        return key;
    }

    @Override
    protected ModuleType toElement(String key, PersistableModuleType persistableElement) {
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
            Set<? extends ModuleType> localizedModuleTypes = null;
            switch (persistableElement.type) {
                case 0:
                    localizedModuleTypes = persistableElement.localizedActionTypes;
                    break;
                case 1:
                    localizedModuleTypes = PersistableCompositeActionType
                            .createFrom(persistableElement.localizedCActionTypes, factory);
                    break;
                case 2:
                    localizedModuleTypes = persistableElement.localizedConditionTypes;
                    break;
                case 3:
                    localizedModuleTypes = PersistableCompositeConditionType
                            .createFrom(persistableElement.localizedCConditionTypes, factory);
                    break;
                case 4:
                    localizedModuleTypes = persistableElement.localizedTriggerTypes;
                    break;
                default: // case 5:
                    localizedModuleTypes = PersistableCompositeTriggerType
                            .createFrom(persistableElement.localizedCTriggerTypes, factory);
                    break;
            }
            ModuleType[] mts = new ModuleType[localizedModuleTypes.size()];
            localizedModuleTypes.toArray(mts);
            String[] languages = new String[persistableElement.languages.size()];
            persistableElement.languages.toArray(languages);
            Localizer l = new Localizer(mts[0]);
            for (int i = 0; i < mts.length; i++) {
                l.addLanguage(languages[i], mts[i]);
            }
            synchronized (providedObjectsHolder) {
                providedObjectsHolder.put(key, l);
            }
            return (ModuleType) l.getPerLocale(null);
        } catch (MalformedURLException notPossible) {
        }
        return null;
    }

    @Override
    protected PersistableModuleType toPersistableElement(ModuleType element) {
        int type = 0;
        if (element instanceof ActionType) {
            if (element instanceof CompositeActionType) {
                type = 1;
            }
        } else if (element instanceof ConditionType) {
            type = 2;
            if (element instanceof CompositeActionType) {
                type = 3;
            }
        } else if (element instanceof TriggerType) {
            type = 4;
            if (element instanceof CompositeTriggerType) {
                type = 5;
            }
        }
        String uid = element.getUID();
        Localizer lmoduleType = null;
        synchronized (providedObjectsHolder) {
            lmoduleType = providedObjectsHolder.get(uid);
        }
        synchronized (providerPortfolio) {
            for (URL url : providerPortfolio.keySet()) {
                List<String> portfolio = providerPortfolio.get(url);
                if (portfolio.contains(uid)) {
                    PersistableModuleType pe = new PersistableModuleType();
                    pe.url = url.toString();
                    pe.languages = lmoduleType.getAvailableLanguages();
                    pe.type = type;
                    for (String language : lmoduleType.getAvailableLanguages()) {
                        switch (pe.type) {
                            case 0:
                                pe.localizedActionTypes.add((ActionType) lmoduleType.getPerLanguage(language));
                                break;
                            case 1:
                                pe.localizedCActionTypes.add(new PersistableCompositeActionType(
                                        (CompositeActionType) lmoduleType.getPerLanguage(language)));
                                break;
                            case 2:
                                pe.localizedConditionTypes.add((ConditionType) lmoduleType.getPerLanguage(language));
                                break;
                            case 3:
                                pe.localizedCConditionTypes.add(new PersistableCompositeConditionType(
                                        (CompositeConditionType) lmoduleType.getPerLanguage(language)));
                                break;
                            case 4:
                                pe.localizedTriggerTypes.add((TriggerType) lmoduleType.getPerLanguage(language));
                                break;
                            case 5:
                                pe.localizedCTriggerTypes.add(new PersistableCompositeTriggerType(
                                        (CompositeTriggerType) lmoduleType.getPerLanguage(language)));
                                break;
                        }
                    }
                    return pe;
                }
            }
        }
        return null;
    }
}
