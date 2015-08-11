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
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.parser.Status;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

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
public class RuleResourceBundleImporter extends AbstractResourceBundleProvider<Vendor> {

    /**
     * This field holds the reference to the Rule Registry.
     */
    protected RuleRegistry ruleRegistry;

    /**
     * This field holds the reference to the tracker of Rule Registry.
     */
    @SuppressWarnings("rawtypes")
    private ServiceTracker rulesTracker;

    /**
     * This constructor is responsible for initializing the path to resources and tracking the managing service of the
     * {@link Rule}s.
     *
     * @param context is the {@code BundleContext}, used for creating a tracker for {@link Parser} services.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public RuleResourceBundleImporter(BundleContext context) {
        super(context);
        path = PATH + "/rules/";
        rulesTracker = new ServiceTracker(context, RuleRegistry.class.getName(), new ServiceTrackerCustomizer() {

            @Override
            public Object addingService(ServiceReference reference) {
                ruleRegistry = (RuleRegistry) bc.getService(reference);
                if (queue != null)
                    queue.open();
                return ruleRegistry;
            }

            @Override
            public void modifiedService(ServiceReference reference, Object service) {
            }

            @Override
            public void removedService(ServiceReference reference, Object service) {
                ruleRegistry = null;
            }
        });
        rulesTracker.open();
    }

    /**
     * This method is inherited from {@link AbstractResourceBundleProvider}. Extends parent's functionality with closing
     * the {@link #rulesTracker} and sets {@code null} to {@link #ruleRegistry}.
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

    @Override
    public void setQueue(AutomationResourceBundlesEventQueue queue) {
        this.queue = queue;
        parserTracker.open();
        if (ruleRegistry != null) {
            queue.open();
        }
    }

    /**
     * @see AbstractResourceBundleProvider#addingService(ServiceReference)
     */
    @SuppressWarnings("rawtypes")
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
     * Continues with loading the rules. If a rule already exists, it is updated, otherwise it is added.
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
        Enumeration<URL> urls = bundle.findEntries(path, null, false);
        if (urls == null) {
            return;
        }
        Vendor vendor = new Vendor(Long.toString(bundle.getBundleId()), bundle.getVersion().toString());
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            try {
                importData(vendor, parser, new InputStreamReader(url.openStream()));
            } catch (IOException e) {
                logger.error("Can't read from resource of bundle with ID " + bundle.getBundleId() + ". URL is " + url,
                        e);
            }
        }
    }

    @Override
    protected Set<Status> importData(Vendor vendor, Parser parser, InputStreamReader inputStreamReader) {
        Set<Status> providedRulesStatus = parser.importData(inputStreamReader);
        if (providedRulesStatus != null && !providedRulesStatus.isEmpty()) {
            Iterator<Status> i = providedRulesStatus.iterator();
            while (i.hasNext()) {
                Rule rule = (Rule) i.next().getResult();
                if (rule != null) {
                    try {
                        ruleRegistry.add(rule);
                    } catch (IllegalArgumentException e) {
                        logger.debug("Not importing rule '{}' since a rule with this id already exists", rule.getUID());
                    }
                }
            } // while
            synchronized (providerPortfolio) {
                if (providerPortfolio.get(vendor) == null) {
                    providerPortfolio.put(vendor, Collections.<String> emptyList());
                }
            }
        }
        return providedRulesStatus;
    }

}
