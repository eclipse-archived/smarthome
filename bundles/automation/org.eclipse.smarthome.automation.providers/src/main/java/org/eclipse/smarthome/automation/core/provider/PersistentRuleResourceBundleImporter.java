/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.provider;

import java.util.List;

import org.eclipse.smarthome.automation.Rule;
import org.osgi.framework.BundleContext;

/**
 * This class extends functionality of {@link RuleResourceBundleImporter} by providing functionality for reading,
 * writing and
 * deleting persistence of {@link Rule}s provisioning from storage.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class PersistentRuleResourceBundleImporter extends RuleResourceBundleImporter<List<String>> {

    /**
     * This constructor extends the parent constructor functionality with initializing the version of persistence.
     *
     * @param context is the {@code BundleContext}, used for creating a tracker for {@link Parser} services.
     */
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
        String vendorVersion = persistableElement.remove(0);
        Vendor vendor = new Vendor(key, vendorVersion);
        synchronized (providerPortfolio) {
            providerPortfolio.put(vendor, persistableElement);
        }
        return vendor;
    }

    @Override
    protected List<String> toPersistableElement(Vendor element) {
        List<String> portfolio = null;
        synchronized (providerPortfolio) {
            portfolio = providerPortfolio.get(element);
            if (portfolio != null && !portfolio.isEmpty()) {
                portfolio.add(0, element.getVendorVersion());
            }
        }
        return portfolio;
    }

}
