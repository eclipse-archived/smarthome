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

package org.eclipse.smarthome.automation.core.provider;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.handler.parser.Parser;
import org.eclipse.smarthome.automation.handler.parser.Status;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Ana Dimova - Initial Contribution
 *
 */
public class RuleResourceBundleImporter extends GeneralResourceBundleProvider {

    private RuleRegistry ruleRegistry;
    private ServiceTracker adminTracker;

    /**
     * @param bc
     */
    public RuleResourceBundleImporter(BundleContext context) {
        super(context);
        path = PATH + "/rules/";
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

    @Override
    protected void processAutomationProvider(Bundle bundle) {
        String parserType = (String) bundle.getHeaders().get(
                AutomationResourceBundlesEventQueue.AUTOMATION_RESOURCES_HEADER);
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
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            try {
                importData(parser, new InputStreamReader(url.openStream()));
            } catch (IOException e) {
                log.error("Can't read from URL " + url, e);
            }
        }
    }

    /**
     * @see org.eclipse.smarthome.automation.core.provider.GeneralResourceBundleProvider#getUID(java.lang.Object)
     */
    @Override
    protected String getUID(Object providedObject) {
        return ((Rule) providedObject).getUID();
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

    protected Object getKey(Object element) {
        Rule r = (Rule) element;
        return r.getUID();
    }

    protected String getStorageName() {
        return "automation-rules";
    }

    protected String keyToString(Object key) {
        return (String) key;
    }

    protected Object toElement(String key, Object persistableElement) {
        return persistableElement;
    }

    protected Object toPersistableElement(Object element) {
        return element;
    }

}
