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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.handler.parser.Parser;
import org.eclipse.smarthome.automation.handler.parser.Status;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ana Dimova - Initial Contribution
 * @param <P>
 *
 */
public abstract class GeneralResourceBundleProvider implements ServiceTrackerCustomizer {

    protected static String PATH = "ESH-INF/automation";

    protected String path;
    protected BundleContext bc;

    protected ServiceTracker parserTracker;
    protected Map<String, Parser> parsers = new HashMap<String, Parser>();

    protected Map<String, Localizer> providedObjectsHolder = new HashMap<String, Localizer>();
    protected Map<String, List> providerPortfolio = new HashMap<String, List>();
    protected Map<Long, Bundle> waitingProviders = new HashMap<Long, Bundle>();

    protected Logger log;
    protected AutomationResourceBundlesEventQueue queue;
    protected Object lock = new Object();

    /**
     * @param bc
     */
    public GeneralResourceBundleProvider(BundleContext context) {
        bc = context;
        this.log = LoggerFactory.getLogger(GeneralResourceBundleProvider.class);
        parserTracker = new ServiceTracker(context, Parser.class.getName(), this);
        parserTracker.open();
    }

    public void setQueque(AutomationResourceBundlesEventQueue queue) {
        this.queue = queue;
    }

    /**
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public Object addingService(ServiceReference reference) {
        Object service = bc.getService(reference);
        String key = (String) reference.getProperty(Parser.FORMAT);
        key = key == null ? Parser.FORMAT_JSON : key;
        parsers.put(key, (Parser) service);
        synchronized (waitingProviders) {
            Iterator i = waitingProviders.keySet().iterator();
            while (i.hasNext()) {
                Long bundleId = (Long) i.next();
                Bundle bundle = waitingProviders.get(bundleId);
                String parserType = (String) bundle.getHeaders().get(
                        AutomationResourceBundlesEventQueue.AUTOMATION_RESOURCES_HEADER);
                Parser parser = parsers.get(parserType);
                if (parser != null && bundle.getState() != Bundle.UNINSTALLED) {
                    queue.addingBundle(bundle, new BundleEvent(BundleEvent.INSTALLED, bundle));
                    i.remove();
                }
            }
        }
        return service;
    }

    /**
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void modifiedService(ServiceReference reference, Object service) {
        // do nothing
    }

    /**
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService(ServiceReference reference, Object service) {
        String key = (String) reference.getProperty(Parser.FORMAT);
        key = key == null ? Parser.FORMAT_JSON : key;
        parsers.remove(key);
    }

    public void close() {
        if (parserTracker != null) {
            parserTracker.close();
            parserTracker = null;
            parsers = null;
            synchronized (lock) {
                providedObjectsHolder = null;
                providerPortfolio = null;
            }
            waitingProviders = null;
        }
    }

    protected void processAutomationProvider(Bundle bundle) {
        String parserType = (String) bundle.getHeaders().get(
                AutomationResourceBundlesEventQueue.AUTOMATION_RESOURCES_HEADER);
        Parser parser = parsers.get(parserType);
        if (parser == null) {
            synchronized (waitingProviders) {
                waitingProviders.put(new Long(bundle.getBundleId()), bundle);
            }
            return;
        }
        Enumeration<URL> urls = bundle.findEntries(path, null, false);
        if (urls == null)
            return;
        ArrayList portfolio = new ArrayList();
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            try {
                importData(Long.toString(bundle.getBundleId()), parser, new InputStreamReader(url.openStream()),
                        portfolio);
            } catch (IOException e) {
                log.error("Can't read from URL " + url, e);
            }
        }
    }

    /**
     * @param parser
     * @param inputStreamReader
     * @return
     */
    protected Set<Status> importData(String id, Parser parser, InputStreamReader inputStreamReader, ArrayList portfolio) {
        Set<Status> providedObjects = parser.importData(inputStreamReader);
        if (providedObjects != null && !providedObjects.isEmpty()) {
            Iterator i = providedObjects.iterator();
            while (i.hasNext()) {
                Object providedObject = ((Status) i.next()).getResult();
                if (providedObject != null) {
                    String uid = getUID(providedObject);
                    portfolio.add(uid);
                    Localizer lProvidedObject = new Localizer(providedObject);
                    synchronized (lock) {
                        providedObjectsHolder.put(uid, lProvidedObject);
                    }
                }
            }// while
            if (id != null) {
                synchronized (lock) {
                    providerPortfolio.put(id, portfolio);
                }
            }
        }
        return providedObjects;
    }

    protected void processAutomationProviderUninstalled(Bundle bundle) {
        List portfolio;
        synchronized (lock) {
            portfolio = providerPortfolio.remove(Long.toString(bundle.getBundleId()));
        }
        if (portfolio == null || portfolio.isEmpty())
            return;
        Iterator i = portfolio.iterator();
        while (i.hasNext()) {
            String uid = (String) i.next();
            synchronized (lock) {
                providedObjectsHolder.remove(uid);
            }
        }
    }

    /**
     * @param providedObject
     * @return
     */
    protected abstract String getUID(Object providedObject);

}
