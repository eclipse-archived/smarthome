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

import org.osgi.framework.BundleContext;

public class PersistentRuleResourceBundleImporter extends RuleResourceBundleImporter<List<String>> {

    public PersistentRuleResourceBundleImporter(BundleContext context) {
        super(context, PersistentRuleResourceBundleImporter.class);
        isReady = true;
    }

    @Override
    protected String getKey(Vendor element) {
        return element.getVendorId();
    }

    @Override
    protected String getStorageName() {
        return "providers_rules";
    }
    
    @Override
    protected String keyToString(String key) {
        return key;
    }

    @Override
    protected Vendor toElement(String key, List<String> persistableElement) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected List<String> toPersistableElement(Vendor element) {
        // TODO Auto-generated method stub
        return new ArrayList<String>();
    }

}
