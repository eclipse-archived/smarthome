/*******************************************************************************
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ProSyst Software GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.smarthome.automation.commands;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.handler.parser.Parser;
import org.eclipse.smarthome.automation.handler.parser.Status;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Ana Dimova - Initial Contribution
 *
 */
public class RuleProvider extends GeneralProvider {

    private RuleRegistry ruleRegistry;
    private ServiceTracker adminTracker;

    /**
     * @param bc
     */
    public RuleProvider(BundleContext context) {
        super(context);
        adminTracker = new ServiceTracker(context, RuleRegistry.class.getName(), this);
        adminTracker.open();
    }

    @Override
    public void close() {
        if (adminTracker != null) {
            adminTracker.close();
            adminTracker = null;
        }
        super.close();
    }

    /**
     * @see org.eclipse.smarthome.automation.core.provider.GeneralResourceBundleProvider#addingService(ServiceReference)
     */
    @Override
    public Object addingService(ServiceReference reference) {
        Object service = bc.getService(reference);
        if (service instanceof RuleRegistry) {
            ruleRegistry = (RuleRegistry) service;
            return service;
        }
        if (reference.getProperty(Parser.PARSER_TYPE).equals(Parser.PARSER_RULE)) {
            return super.addingService(reference);
        }
        return null;
    }

    /**
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(ServiceReference, Object)
     */
    @Override
    public void removedService(ServiceReference reference, Object service) {
        if (service instanceof RuleRegistry) {
            ruleRegistry = null;
        } else {
            super.removedService(reference, service);
        }
    }

    /**
     * @param parserType
     * @param set
     * @param file
     */
    public Status exportRules(String parserType, Set set, File file) {
        return super.exportData(parserType, set, file);
    }

    /**
     * @param parserType
     * @param url
     * @return
     */
    public Set<Status> importRules(String parserType, URL url) {
        InputStreamReader inputStreamReader = null;
        Parser parser = parsers.get(parserType);
        if (parser != null)
            try {
                inputStreamReader = new InputStreamReader(new BufferedInputStream(url.openStream()));
                return importData(parser, inputStreamReader);
            } catch (IOException e) {
                Status s = new Status(log, 0, null);
                s.error("Can't read from URL " + url, e);
                LinkedHashSet<Status> res = new LinkedHashSet<Status>();
                res.add(s);
                return res;
            } finally {
                try {
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                } catch (IOException e) {
                }
            }
        return null;
    }

    /**
     *
     * @param parser
     * @param inputStreamReader
     */
    private Set<Status> importData(Parser parser, InputStreamReader inputStreamReader) {
        Set<Status> providedRulesStatus = parser.importData(inputStreamReader);
        if (providedRulesStatus != null && !providedRulesStatus.isEmpty()) {
            Iterator i = providedRulesStatus.iterator();
            while (i.hasNext()) {
                Status status = (Status) i.next();
                Rule rule = (Rule) status.getResult();
                if (rule != null)
                    ruleRegistry.add(rule);
            }
        }
        return providedRulesStatus;
    }

    /**
     * @see org.eclipse.smarthome.automation.commands.GeneralProvider#getUID(java.lang.Object)
     */
    @Override
    protected String getUID(Object providedObject) {
        return ((Rule) providedObject).getUID();
    }

}
