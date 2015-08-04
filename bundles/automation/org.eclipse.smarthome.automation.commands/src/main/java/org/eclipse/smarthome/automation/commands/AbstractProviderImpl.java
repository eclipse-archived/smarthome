/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;

import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.parser.Status;
import org.eclipse.smarthome.automation.provider.ModuleTypeProvider;
import org.eclipse.smarthome.automation.provider.TemplateProvider;
import org.eclipse.smarthome.automation.provider.util.AbstractPersistentProvider;

/**
 * This class is base for {@link ModuleTypeProvider}, {@link TemplateProvider} and RuleImporter which are responsible
 * for execution of automation commands.
 * <p>
 * It extends functionality of {@link AbstractPersistentProvider} with tracking {@link Parser} services by implementing
 * {@link ServiceTrackerCustomizer}
 * <p>
 * and provides common functionality for exporting automation objects.
 * 
 * @author Ana Dimova - Initial Contribution
 * 
 */
public abstract class AbstractProviderImpl<E, PE>  extends AbstractPersistentProvider<E, PE>  implements ServiceTrackerCustomizer {

    /**
     * This Map provides reference between provider of resources and the loaded objects from these resources.
     * <p>
     * The Map has for keys - {@link URL} resource provider and for values - Lists with UIDs of the objects.
     */
    Map<URL, List<String>> providerPortfolio = new HashMap<URL, List<String>>();

    /**
     * This field is a {@link ServiceTracker} for {@link Parser} services.
     */
    protected ServiceTracker parserTracker;
    
    /**
     * This Map provides structure for fast access to the {@link Parser}s. This provides opportunity for high
     * performance at runtime of the system.
     */
    protected Map<String, Parser> parsers = new HashMap<String, Parser>();

    /**
     * This Map provides structure for fast access to the provided automation objects. This provides opportunity for
     * high performance at runtime of the system, when the Rule Engine asks for any particular object, instead of
     * waiting it for parsing every time.
     * <p>
     * The Map has for keys UIDs of the objects and for values {@link Localizer}s of the objects.
     */
    protected Map<String, Localizer> providedObjectsHolder = new HashMap<String, Localizer>();
    
    /**
     * This constructor is responsible for creation and opening a tracker for {@link Parser} services.
     * 
     * @param context is the {@link BundleContext}, used for creating a tracker for {@link Parser} services.
     * @param providerClass the class object, used for creation of a {@link Logger}, which belongs to this specific
     *            provider.
     */
    public AbstractProviderImpl(BundleContext context, Class providerClass) {
        super(context, providerClass);
        parserTracker = new ServiceTracker(context, Parser.class.getName(), this);
        parserTracker.open();
    }

    /**
     * This method is inherited from {@link AbstractPersistentProvider}.
     * Extends parent's functionality with closing the {@link Parser} service tracker.
     * Sets <code>null</code> to {@link #parsers}, {@link #providedObjectsHolder}, {@link #providerPortfolio}
     * 
     * @see org.eclipse.smarthome.automation.provider.util.AbstractPersistentProvider#close()
     * 
     */
    @Override
    public void close() {
        if (parserTracker != null) {
            parserTracker.close();
            parserTracker = null;
            parsers = null;
            synchronized (providedObjectsHolder) {
                providedObjectsHolder = null;
            }
            synchronized (providerPortfolio) {
                providerPortfolio = null;
            }
        }
    }

    /**
     * This method tracks the {@link Parser} services and stores them into the Map "{@link #parsers}" in the
     * memory, for fast access on demand.
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
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
    public void modifiedService(ServiceReference reference, Object service) {
        // do nothing
    }

    /**
     * This method removes the {@link Parser} service objects from the Map "{@link #parsers}".
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    public void removedService(ServiceReference reference, Object service) {
        String key = (String) reference.getProperty(Parser.FORMAT);
        key = key == null ? Parser.FORMAT_JSON : key;
        parsers.remove(key);
    }

    /**
     * This method is responsible for execution of the {@link AutomationCommandExport} operation by choosing the
     * {@link Parser} which to be used for exporting a set of automation objects, in a file. When the choice is made,
     * the chosen {@link Parser} is used to do the export.
     * 
     * @param parserType is a criteria for choosing the {@link Parser} which to be used.
     * @param set a Set of automation objects for export.
     * @param file the file in which to export the automation objects.
     * @return {@link Status} of the {@link AutomationCommandExport} operation. Can be successful or can fail because of
     *         missing parser or {@link IOException} or {@link FileNotFoundException}.
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
            } else {
                s.error("Parser not found : \"" + parser + "\"!", new IllegalArgumentException());
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
     * This method is responsible for execution of the {@link AutomationCommandImport} operation.
     * 
     * @param parser the {@link Parser} which to be used for operation.
     * @param inputStreamReader
     * @return {@link Status} of the {@link AutomationCommandImport} operation. Can be successful or can fail because of
     *         {@link IOException}.
     */
    protected abstract Set<Status> importData(URL url, Parser parser, InputStreamReader inputStreamReader);

}
