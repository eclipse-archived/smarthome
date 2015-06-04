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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.handler.parser.Parser;
import org.eclipse.smarthome.automation.handler.parser.Status;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ana Dimova - Initial Contribution
 *
 */
public abstract class GeneralProvider implements ServiceTrackerCustomizer {

    protected BundleContext bc;
    protected ServiceTracker parserTracker;
    protected Map<String, Parser> parsers = new HashMap<String, Parser>();

    protected Map<String, Localizer> providedObjectsHolder = new HashMap<String, Localizer>();
    protected Map<String, List> providerPortfolio = new HashMap<String, List>();
    protected Object lock = new Object();
    protected Logger log;

    /**
     * @param bc
     */
    public GeneralProvider(BundleContext context) {
        bc = context;
        this.log = LoggerFactory.getLogger(GeneralProvider.class);
        parserTracker = new ServiceTracker(context, Parser.class.getName(), this);
        parserTracker.open();
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
        }
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

    /**
     *
     * @param parserType
     * @param set
     * @param file
     */
    public Status exportData(String parserType, Set set, File file) {
        OutputStreamWriter oWriter = null;
        Status s = new Status(log, 0, null);
        try {
            oWriter = new OutputStreamWriter(new FileOutputStream(file));
            Parser parser = parsers.get(parserType);
            if (parser != null) {
                try {
                    parser.exportData(set, oWriter);
                    s.success(null);
                } catch (IOException e) {
                    s.error(e.getMessage(), e);
                }
            }
        } catch (FileNotFoundException e) {
            s.error("File not found : " + file, e);
        } finally {
            try {
                if (oWriter != null) {
                    oWriter.flush();
                    oWriter.close();
                }
            } catch (IOException e) {
            }
        }
        return s;
    }

    /**
     *
     * @param url
     */
    public void remove(URL url) {
        synchronized (lock) {
            if (providerPortfolio.isEmpty())
                return;
            List portfolio = providerPortfolio.get(url.toString());
            if (portfolio == null || portfolio.isEmpty())
                return;
            Iterator i = portfolio.iterator();
            while (i.hasNext()) {
                i.remove();
            }
        }
    }

    /**
     * @param providedObject
     * @return
     */
    protected abstract String getUID(Object providedObject);

}
