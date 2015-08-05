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
import java.util.List;

import org.eclipse.smarthome.automation.Rule;
import org.osgi.framework.BundleContext;

/**
 * This class extends functionality of {@link RuleImporterImpl} by providing functionality for creating, getting and
 * deleting persistence of {@link Rule}s provisioning from storage.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class PersistentRuleImporter extends RuleImporterImpl<List<String>> {

    /**
     * This constructor extends the parent constructor functionality with initializing the version of persistence.
     *
     * @param context is the {@code BundleContext}, used for creating a tracker for {@link Parser} services.
     */
    public PersistentRuleImporter(BundleContext context) {
        super(context, PersistentRuleImporter.class);
    }

    @Override
    protected String getKey(URL element) {
        return element.toString();
    }

    @Override
    protected String getStorageName() {
        return "commands_rules";
    }

    @Override
    protected String keyToString(String key) {
        return key;
    }

    @Override
    protected URL toElement(String key, List<String> persistableElement) {
        URL url = null;
        try {
            url = new URL(key);
            synchronized (providerPortfolio) {
                providerPortfolio.put(url, persistableElement);
            }
        } catch (MalformedURLException e) {
        }
        return url;
    }

    @Override
    protected List<String> toPersistableElement(URL element) {
        List<String> portfolio = null;
        synchronized (providerPortfolio) {
            portfolio = providerPortfolio.get(element);
        }
        return portfolio;
    }

}
