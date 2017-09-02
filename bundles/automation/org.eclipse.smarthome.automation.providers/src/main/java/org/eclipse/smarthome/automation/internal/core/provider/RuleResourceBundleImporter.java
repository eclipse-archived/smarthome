/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.core.provider;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.automation.ManagedRuleProvider;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.parser.Parser;
import org.osgi.framework.Bundle;

/**
 * This class is implementation of {@link RuleResourceBundleImporter}. It serves for providing {@link Rule}s by loading
 * bundle resources. It extends functionality of {@link AbstractResourceBundleProvider} by specifying:
 * <ul>
 * <li>the path to resources, corresponding to the {@link Rule}s - root directory
 * {@link AbstractResourceBundleProvider#PATH} with sub-directory "rules".
 * <li>type of the {@link Parser}s, corresponding to the {@link Rule}s - {@link Parser#PARSER_RULE}
 * <li>specific functionality for loading the {@link Rule}s
 * <li>tracking the managing service of the {@link Rule}s.
 * </ul>
 *
 * @author Ana Dimova - Initial Contribution
 * @author Kai Kreuzer - refactored (managed) provider and registry implementation
 *
 */
public class RuleResourceBundleImporter extends AbstractResourceBundleProvider<Rule> {

    /**
     * This field holds the reference to the Rule Registry.
     */
    protected ManagedRuleProvider mProvider;

    /**
     * This constructor is responsible for initializing the path to resources and tracking the managing service of the
     * {@link Rule}s.
     *
     * @param registry the managing service of the {@link Rule}s.
     */
    public RuleResourceBundleImporter() {
        path = PATH + "/rules/";
    }

    protected void setManagedRuleProvider(ManagedRuleProvider mProvider) {
        this.mProvider = mProvider;
    }

    @Override
    public void deactivate() {
        mProvider = null;
        super.deactivate();
    }

    /**
     * This method provides functionality for processing the bundles with rule resources.
     * <p>
     * Checks for availability of the needed {@link Parser} and for availability of the rules managing service. If one
     * of them is not available - the bundle is added into {@link #waitingProviders} and the execution of the method
     * ends.
     * <p>
     * Continues with loading the rules. If a rule already exists, it is updated, otherwise it is added.
     * <p>
     * The loading can fail because of {@link IOException}.
     *
     * @param bundle
     *            it is a {@link Bundle} which has to be processed, because it provides resources for automation rules.
     */
    @Override
    protected void processAutomationProvider(Bundle bundle) {
        Vendor vendor = new Vendor(bundle.getSymbolicName(), bundle.getVersion().toString());
        logger.debug("Parse rules from bundle '{}' ", bundle.getSymbolicName());
        Enumeration<URL> urlEnum = null;
        try {
            if (bundle.getState() != Bundle.UNINSTALLED) {
                urlEnum = bundle.findEntries(path, null, true);
            }
        } catch (IllegalStateException e) {
            logger.debug("Can't read from resource of bundle with ID {}. The bundle is uninstalled.",
                    bundle.getBundleId(), e);
            processAutomationProviderUninstalled(bundle);
        }
        if (urlEnum != null) {
            while (urlEnum.hasMoreElements()) {
                URL url = urlEnum.nextElement();
                if (getPreviousPortfolio(vendor) != null
                        && (waitingProviders.get(bundle) == null || !waitingProviders.get(bundle).contains(url))) {
                    return;
                }
                if (url.getPath().endsWith(File.separator)) {
                    continue;
                }
                String parserType = getParserType(url);
                Parser<Rule> parser = parsers.get(parserType);
                updateWaitingProviders(parser, bundle, url);
                if (parser != null) {
                    Set<Rule> parsedObjects = parseData(parser, url, bundle);
                    if (parsedObjects != null && !parsedObjects.isEmpty()) {
                        Set<Rule> rules = setUIDs(vendor, parsedObjects);
                        addNewProvidedObjects(null, null, rules);
                    }
                }
            }
            putNewPortfolio(vendor, Collections.<String> emptyList());
        }
    }

    @Override
    protected void addNewProvidedObjects(List<String> newPortfolio, List<String> previousPortfolio,
            Set<Rule> parsedObjects) {
        if (parsedObjects != null && !parsedObjects.isEmpty()) {
            for (Rule rule : parsedObjects) {
                try {
                    mProvider.add(rule);
                } catch (IllegalArgumentException e) {
                    logger.debug("Not importing rule '{}' because: {}", rule.getUID(), e.getMessage(), e);
                } catch (IllegalStateException e) {
                    logger.debug("Not importing rule '{}' since the rule registry is in an invalid state: {}",
                            rule.getUID(), e.getMessage());
                }
            }
        }
    }

    @Override
    protected List<String> getPreviousPortfolio(Vendor vendor) {
        List<String> portfolio = providerPortfolio.get(vendor);
        if (portfolio == null) {
            for (Vendor v : providerPortfolio.keySet()) {
                if (v.getVendorSymbolicName().equals(vendor.getVendorSymbolicName())) {
                    return providerPortfolio.get(v);
                }
            }
        }
        return portfolio;
    }

    @Override
    protected void processAutomationProviderUninstalled(Bundle bundle) {
        Vendor vendor = new Vendor(bundle.getSymbolicName(), bundle.getVersion().toString());
        waitingProviders.remove(bundle);
        providerPortfolio.remove(vendor);
    }

    @Override
    protected String getUID(Rule parsedObject) {
        return parsedObject.getUID();
    }

    protected Set<Rule> setUIDs(Vendor vendor, Set<Rule> rules) {
        Set<Rule> newRules = new HashSet<Rule>();
        for (Rule rule : rules) {
            if (rule.getUID() == null) {
                rule = setUID(vendor, rule);
            }
            newRules.add(rule);
        }
        return newRules;
    }

    /**
     * This method gives UIDs on the rules that don't have one.
     *
     * @param vendor
     *            is the bundle providing the rules.
     * @param rule
     *            is the provided rule.
     */
    private Rule setUID(Vendor vendor, Rule rule) {
        String uid = vendor.getVendorID() + vendor.count();
        Rule r = new Rule(uid, rule.getTriggers(), rule.getConditions(), rule.getActions(),
                rule.getConfigurationDescriptions(), rule.getConfiguration(), rule.getTemplateUID(),
                rule.getVisibility());
        r.setName(rule.getName());
        r.setDescription(rule.getDescription());
        r.setTags(rule.getTags());
        return r;
    }

}