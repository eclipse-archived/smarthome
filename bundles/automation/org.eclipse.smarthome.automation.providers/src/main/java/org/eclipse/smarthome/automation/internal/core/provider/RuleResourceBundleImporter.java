/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.core.provider;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleProvider;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.parser.ParsingException;
import org.eclipse.smarthome.core.common.registry.ManagedProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
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
public class RuleResourceBundleImporter extends AbstractResourceBundleProvider<Rule> {

    /**
     * This field holds the reference to the Rule Registry.
     */
    protected RuleRegistry ruleRegistry;
    private RuleProvider ruleProvider;

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

        try {
            Filter filter = bc.createFilter("(|(objectClass=" + RuleRegistry.class.getName() + ")(objectClass="
                    + RuleProvider.class.getName() + "))");

            rulesTracker = new ServiceTracker(bc, filter, new ServiceTrackerCustomizer() {

                @Override
                public Object addingService(ServiceReference reference) {
                    Object service = bc.getService(reference);
                    if (service instanceof RuleRegistry) {
                        ruleRegistry = (RuleRegistry) service;
                        queue.open();
                        return service;
                    }
                    if (service instanceof ManagedProvider) {
                        ruleProvider = (RuleProvider) service;
                        queue.open();
                        return service;
                    }
                    return null;
                }

                @Override
                public void modifiedService(ServiceReference reference, Object service) {
                }

                @Override
                public void removedService(ServiceReference reference, Object service) {
                    if (service instanceof RuleRegistry) {
                        ruleRegistry = null;
                    } else if (service instanceof RuleProvider) {
                        ruleProvider = null;
                    }

                }
            });
        } catch (InvalidSyntaxException notPossible) {
        }

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
            ruleProvider = null;
        }
        super.close();
    }

    @Override
    public void setQueue(AutomationResourceBundlesEventQueue queue) {
        super.setQueue(queue);
        rulesTracker.open();
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

    @Override
    public boolean isReady() {
        return ruleRegistry != null && ruleProvider != null && queue != null;
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
        logger.debug("Parse rules from bundle '{}' ", bundle.getSymbolicName());
        Enumeration<URL> urlEnum = null;
        try {
            urlEnum = bundle.findEntries(path, null, false);
        } catch (IllegalStateException e) {
            logger.debug("Can't read from resource of bundle with ID " + bundle.getBundleId()
                    + ". The bundle is uninstalled.", e);
            processAutomationProviderUninstalled(bundle);
        }
        if (urlEnum == null) {
            return;
        }
        Vendor vendor = new Vendor(bundle.getSymbolicName(), bundle.getVersion().toString());
        while (urlEnum.hasMoreElements()) {
            URL url = urlEnum.nextElement();
            String parserType = getParserType(url);
            Parser<Rule> parser = parsers.get(parserType);
            synchronized (waitingProviders) {
                List<URL> urlList = waitingProviders.get(bundle);
                if (parser != null) {
                    if (urlList != null && urlList.remove(url) && urlList.isEmpty()) {
                        waitingProviders.remove(bundle);
                    }
                    InputStreamReader reader = null;
                    try {
                        importData(vendor, parser, reader = new InputStreamReader(url.openStream()));
                    } catch (IOException e) {
                        logger.error("Can't read from resource of bundle with ID " + bundle.getBundleId(), e);
                        processAutomationProviderUninstalled(bundle);
                    } finally {
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException ignore) {
                            }
                        }
                    }
                } else if (parser == null) {
                    if (urlList == null) {
                        urlList = new ArrayList<URL>();
                    }
                    urlList.add(url);
                    waitingProviders.put(bundle, urlList);
                }
            }
        }
    }

    @Override
    protected Set<Rule> importData(Vendor vendor, Parser<Rule> parser, InputStreamReader inputStreamReader) {
        Set<Rule> providedRules = null;
        try {
            providedRules = parser.parse(inputStreamReader);
        } catch (ParsingException e) {
        }
        if (providedRules != null && !providedRules.isEmpty()) {
            Iterator<Rule> i = providedRules.iterator();
            while (i.hasNext()) {
                Rule rule = i.next();
                if (rule != null) {
                    try {
                        if (rule.getUID() == null) {
                            rule = setUID(vendor, rule);
                        }
                        ruleRegistry.add(rule);
                    } catch (IllegalArgumentException e) {
                        logger.debug("Not importing rule '{}' since a rule with this id already exists", rule.getUID());
                    } catch (IllegalStateException e) {
                        logger.debug("Not importing rule '{}' since the rule registry is in an invalid state: {}",
                                new Object[] { rule.getUID(), e.getMessage() });
                    }
                }
            } // while
            synchronized (providerPortfolio) {
                if (providerPortfolio.get(vendor) == null) {
                    providerPortfolio.put(vendor, Collections.<String> emptyList());
                }
            }
        }
        return providedRules;
    }

    /**
     * This method gives UIDs on the rules that don't have one.
     *
     * @param vendor is the bundle providing the rules.
     * @param rule is the provided rule.
     */
    private Rule setUID(Vendor vendor, Rule rule) {
        String uid = vendor.getVendorID() + vendor.count();
        Rule r = new Rule(uid, rule.getTriggers(), rule.getConditions(), rule.getActions(),
                rule.getConfigurationDescriptions(), rule.getConfiguration());
        r.setName(rule.getName());
        r.setDescription(rule.getDescription());
        r.setTags(rule.getTags());
        return r;
    }

}