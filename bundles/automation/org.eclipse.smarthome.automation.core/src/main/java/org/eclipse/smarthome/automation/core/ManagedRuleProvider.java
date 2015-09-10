/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleProvider;
import org.eclipse.smarthome.core.common.registry.DefaultAbstractManagedProvider;
import org.eclipse.smarthome.core.storage.StorageService;

/**
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Persistence implementation
 * @author Kai Kreuzer - refactored (managed) provider and registry implementation
 */
public class ManagedRuleProvider extends DefaultAbstractManagedProvider<Rule, String>implements RuleProvider {

    public ManagedRuleProvider(StorageService storage) {
        setStorageService(storage);
    }

    @Override
    protected String getKey(Rule element) {
        return element.getUID();
    }

    @Override
    protected String getStorageName() {
        return "automation_rules";
    }

    @Override
    protected String keyToString(String key) {
        return key;
    }

    // @Override
    // public void addProviderChangeListener(ProviderChangeListener<Rule> listener) {
    // // TODO Auto-generated method stub
    //
    // }
    //
    // @Override
    // public void removeProviderChangeListener(ProviderChangeListener<Rule> listener) {
    // // TODO Auto-generated method stub
    //
    // }

}
