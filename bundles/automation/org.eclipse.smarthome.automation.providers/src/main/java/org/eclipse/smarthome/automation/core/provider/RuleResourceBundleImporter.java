/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.provider;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.parser.Status;

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
 * 
 */
public abstract class RuleResourceBundleImporter<PE> extends AbstractResourceBundleProvider<Vendor, PE> {

    protected RuleRegistry ruleRegistry;
    private ServiceTracker rulesTracker;

    /**
     * This constructor is responsible for initializing the path to resources and tracking the managing service of the
     * {@link Rule}s.
     * 
     * @param context is the {@code BundleContext}, used for creating a tracker for {@link Parser} services.
     * @param providerClass the class object, used for creation of a {@link Logger}, which belongs to this specific
     *            provider.
     */
    public RuleResourceBundleImporter(BundleContext context, Class providerClass) {
        super(context, providerClass);
        path = PATH + "/rules/";
        rulesTracker = new ServiceTracker(context, RuleRegistry.class.getName(),
                new ServiceTrackerCustomizer<RuleRegistry, RuleRegistry>() {

                    public RuleRegistry addingService(ServiceReference<RuleRegistry> reference) {
                        ruleRegistry = bc.getService(reference);
                        if (isReady && queue != null)
                            queue.open();
                        return ruleRegistry;
                    }

                    public void modifiedService(ServiceReference<RuleRegistry> reference, RuleRegistry service) {
                    }

                    public void removedService(ServiceReference<RuleRegistry> reference, RuleRegistry service) {
                        ruleRegistry = null;
                    }
                });
        rulesTracker.open();
    }

    /**
     * This method is inherited from {@link AbstractResourceBundleProvider}.
     * <p>
     * Extends parent's functionality with closing the {@link #rulesTracker} and
     * <p>
     * sets {@code null} to {@link #ruleRegistry}.
     * 
     * @see AbstractResourceBundleProvider#close()
     */
    @Override
    public void close() {
        if (rulesTracker != null) {
            rulesTracker.close();
            rulesTracker = null;
            ruleRegistry = null;
        }
        super.close();
    }

    /**
     * @see AbstractResourceBundleProvider#addingService(ServiceReference)
     */
    @Override
    public Object addingService(ServiceReference reference) {
        if (reference.getProperty(Parser.PARSER_TYPE).equals(Parser.PARSER_RULE)) {
            return super.addingService(reference);
        }
        return null;
    }

    /**
     * This method provides functionality for processing the bundles with rule resources.
     * <p>
     * Checks for availability of the needed {@link Parser} and for availability of the rules managing service. If one
     * of them is not available - the bundle is added into {@link #waitingProviders} and the execution of the method
     * ends.
     * <p>
     * If both are available, the execution of the method continues with checking if the version of the bundle is
     * changed. If the version is changed - removes persistence of old variants of the rules, provided by this bundle.
     * <p>
     * Continues with loading the new version of these rules. If this bundle is added for the very first time, only
     * loads the provided rules.
     * <p>
     * The loading can fail because of {@link IOException}.
     * 
     * @param bundle it is a {@link Bundle} which has to be processed, because it provides resources for automation
     *            rules.
     */
    @Override
    protected void processAutomationProvider(Bundle bundle) {
        String parserType = bundle.getHeaders().get(AutomationResourceBundlesEventQueue.AUTOMATION_RESOURCES_HEADER);
        Parser parser = parsers.get(parserType);
        if (parser == null || ruleRegistry == null) {
            synchronized (waitingProviders) {
                waitingProviders.put(new Long(bundle.getBundleId()), bundle);
            }
            return;
        }
        synchronized (providerPortfolio) {
            for (Vendor vendor : providerPortfolio.keySet()) {
                if (vendor.getVendorId().equals(Long.toString(bundle.getBundleId()))
                        && !vendor.getVendorVersion().equals(bundle.getVersion().toString())) {
                    remove(vendor.getVendorId());
                    break;
                }
            }
        }
        Enumeration<URL> urls = bundle.findEntries(path, null, false);
        if (urls == null) {
            return;
        }
        ArrayList portfolio = new ArrayList();
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            try {
                Vendor vendor = new Vendor(Long.toString(bundle.getBundleId()), bundle.getVersion().toString());
                importData(vendor, parser, new InputStreamReader(url.openStream()), portfolio);
            } catch (IOException e) {
                log.error("Can't read from resource of bundle with ID " + bundle.getBundleId() + ". URL is " + url, e);
            }
        }
    }

    /**
     * This method provides functionality for processing the uninstalled bundles with rule resources.
     * <p>
     * When some of the bundles that provides rule resources is uninstalled, this method will remove it from
     * {@link #waitingProviders}, if it is still there or from {@link #providerPortfolio} in the other case.
     * <p>
     * Will remove these rules from {@link #providedObjectsHolder} and will remove their persistence,
     * injected in the system from this bundle.
     * 
     * @param bundle the uninstalled {@link Bundle}, provider of automation rules.
     */
    @Override
    protected void processAutomationProviderUninstalled(Bundle bundle) {
        synchronized (waitingProviders) {
            if (waitingProviders.remove(new Long(bundle.getBundleId())) != null)
                return;
        }
        Vendor vendor = new Vendor(Long.toString(bundle.getBundleId()), bundle.getVersion().toString());
        List portfolio = null;
        synchronized (providerPortfolio) {
            if (providerPortfolio.isEmpty())
                return;
            portfolio = providerPortfolio.get(vendor);
        }
        remove(vendor.getVendorId());
        if (portfolio == null || portfolio.isEmpty())
            return;
        Iterator ip = portfolio.iterator();
        while (ip.hasNext()) {
            String uid = (String) ip.next();
            ruleRegistry.remove(uid);
        }
    }

    /**
     * @see AbstractResourceBundleProvider#importData(Vendor, Parser, java.io.InputStreamReader, java.util.ArrayList)
     */
    @Override
    protected Set<Status> importData(Vendor vendor, Parser parser, InputStreamReader inputStreamReader,
            ArrayList<String> portfolio) {
        Set<Status> providedRulesStatus = parser.importData(inputStreamReader);
        if (providedRulesStatus != null && !providedRulesStatus.isEmpty()) {
            Iterator<Status> i = providedRulesStatus.iterator();
            while (i.hasNext()) {
                Rule rule = (Rule) i.next().getResult();
                if (rule != null) {
                    ruleRegistry.add(rule);
                    portfolio.add(rule.getUID());
                }
            } // while
            if (vendor != null) {
                synchronized (providerPortfolio) {
                    providerPortfolio.put(vendor, portfolio);
                }
                add(vendor);
            }
        }
        return providedRulesStatus;
    }

}
