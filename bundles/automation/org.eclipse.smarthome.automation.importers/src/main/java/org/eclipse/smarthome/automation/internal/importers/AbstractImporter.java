/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.importers;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.automation.RuleProvider;
import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.template.TemplateProvider;
import org.eclipse.smarthome.automation.type.ModuleTypeProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is base for {@link RuleProvider}, {@link ModuleTypeProvider} and {@link TemplateProvider} which are
 * responsible for importing the automation objects from local file system.
 * <p>
 * It provides functionality for tracking {@link Parser} services by implementing {@link ServiceTrackerCustomizer} and
 * provides common functionality for importing the automation objects.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractImporter<E> implements ServiceTrackerCustomizer {

    protected Logger logger;

    /**
     * A bundle's execution context within the Framework.
     */
    protected BundleContext bc;

    /**
     * This field is a {@link ServiceTracker} for {@link Parser} services.
     */
    protected ServiceTracker<Parser, Parser> parserTracker;

    /**
     * This Map provides structure for fast access to the {@link Parser}s. This provides opportunity for high
     * performance at runtime of the system.
     */
    protected Map<String, Parser<E>> parsers = new HashMap<String, Parser<E>>();

    /**
     * This Map provides structure for fast access to the provided automation objects. This provides opportunity for
     * high performance at runtime of the system, when the Rule Engine asks for any particular object, instead of
     * waiting it for parsing every time.
     * <p>
     * The Map has for keys UIDs of the objects and for values {@link Localizer}s of the objects.
     */
    protected Map<String, E> providedObjectsHolder = new HashMap<String, E>();

    /**
     * This Map holds URL resources that waiting for a parser to be loaded.
     */
    protected Map<String, List<URL>> urls = new HashMap<String, List<URL>>();

    /**
     * This constructor is responsible for creation and opening a tracker for {@link Parser} services.
     *
     * @param context is the {@link BundleContext}, used for creating a tracker for {@link Parser} services.
     */
    @SuppressWarnings("unchecked")
    public AbstractImporter(BundleContext context) {
        this.bc = context;
        logger = LoggerFactory.getLogger(AbstractImporter.this.getClass());
        parserTracker = new ServiceTracker(context, Parser.class.getName(), this);
        parserTracker.open();
    }

    /**
     * This method closes the {@link Parser} service tracker.
     * Sets <code>null</code> to {@link #parsers}, {@link #providedObjectsHolder}, {@link #urls}
     */
    public void close() {
        if (parserTracker != null) {
            parserTracker.close();
            parserTracker = null;
            parsers = null;
            urls = null;
            synchronized (providedObjectsHolder) {
                providedObjectsHolder = null;
            }
        }
    }

    @Override
    public void modifiedService(ServiceReference reference, Object service) {
        // do nothing
    }

    /**
     * This method removes the {@link Parser} service objects from the Map "{@link #parsers}".
     */
    @Override
    public void removedService(ServiceReference reference, Object service) {
        String parsertype = (String) reference.getProperty(Parser.FORMAT);
        parsertype = parsertype == null ? Parser.FORMAT_JSON : parsertype;
        parsers.remove(parsertype);
    }

    /**
     * This method is responsible for importing automation objects from InputStreamReader by using the Parser.
     *
     * @param parser the {@link Parser} which to be used for operation.
     * @param inputStreamReader provides the resources for parsing.
     */
    protected abstract void importData(Parser<E> parser, InputStreamReader inputStreamReader);

}