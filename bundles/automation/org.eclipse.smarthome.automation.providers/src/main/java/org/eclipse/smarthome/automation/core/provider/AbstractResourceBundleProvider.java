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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.AutomationFactory;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.parser.Status;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.template.TemplateProvider;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is base for {@link ModuleTypeProvider}, {@link TemplateProvider} and RuleImporter which are responsible
 * for importing and persisting the {@link ModuleType}s, {@link RuleTemplate}s and {@link Rule}s from bundles which
 * provides resource files.
 * <p>
 * It tracks {@link Parser} services by implementing {@link ServiceTrackerCustomizer}.
 * <p>
 * The additional functionality, responsible for tracking bundles with resources, comes from
 * {@link AutomationResourceBundlesEventQueue} by implementing a {@link BundleTrackerCustomizer}
 * <p>
 * but {@code AbstractResourceBundleProvider} provides common functionality for processing the tracked bundles.
 *
 * @author Ana Dimova - Initial Contribution
 * @author Kai Kreuzer - refactored (managed) provider and registry implementation
 *
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractResourceBundleProvider<E> implements ServiceTrackerCustomizer {

    /**
     * This static field provides a root directory for automation object resources in the bundle resources.
     * It is common for all resources - {@link ModuleType}s, {@link RuleTemplate}s and {@link Rule}s.
     */
    protected static String PATH = "ESH-INF/automation";

    /**
     * This field keeps instance of {@link Logger} that is used for logging.
     */
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * A bundle's execution context within the Framework.
     */
    protected BundleContext bc;

    /**
     * This field is an {@link AutomationFactory}. It uses for creation of
     * modules in deserializing the automation objects.
     */
    protected AutomationFactory factory;

    /**
     * This field is initialized in constructors of any particular provider with specific path for the particular
     * resources from specific type as {@link ModuleType}s, {@link RuleTemplate}s and {@link Rule}s:
     * <li>for {@link ModuleType}s it is a "ESH-INF/automation/moduletypes/"
     * <li>for {@link RuleTemplate}s it is a "ESH-INF/automation/templates/"
     * <li>for {@link Rule}s it is a "ESH-INF/automation/rules/"
     */
    protected String path;

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
    protected Map<String, Localizer> providedObjectsHolder = new HashMap<String, Localizer>();

    /**
     * This Map provides reference between provider of resources and the loaded objects from these resources.
     * <p>
     * The Map has for keys - {@link Vendor}s and for values - Lists with UIDs of the objects.
     */
    protected Map<Vendor, List<String>> providerPortfolio = new HashMap<Vendor, List<String>>();

    /**
     * This Map holds bundles whose {@link Parser} for resources is missing in the moment of processing the bundle.
     * Later, if the {@link Parser} appears, they will be added again in the {@link #queue} for processing.
     */
    protected Map<Bundle, List<URL>> waitingProviders = new HashMap<Bundle, List<URL>>();

    /**
     * This field provides an access to the queue for processing bundles.
     */
    protected AutomationResourceBundlesEventQueue queue;

    /**
     * This constructor is responsible for creation of a tracker for {@link Parser} services.
     *
     * @param context is the {@code BundleContext}, used for creating a tracker for {@link Parser} services.
     */
    @SuppressWarnings("unchecked")
    public AbstractResourceBundleProvider(BundleContext context) {
        this.bc = context;
        logger = LoggerFactory.getLogger(AbstractResourceBundleProvider.this.getClass());
        parserTracker = new ServiceTracker(context, Parser.class.getName(), this);
        parserTracker.open();
    }

    /**
     * This method is used to initialize field {@link #queue}, when the instance of
     * {@link AutomationResourceBundlesEventQueue} is created.
     *
     * @param queue provides an access to the queue for processing bundles.
     */
    public void setQueue(AutomationResourceBundlesEventQueue queue) {
        this.queue = queue;
        queue.open();
    }

    /**
     * This method is called before the {@link Parser} services to be added to the {@code ServiceTracker} and storing
     * them in the {@link #parsers} into the memory, for fast access on demand. The returned service object is stored in
     * the {@code ServiceTracker} and is available from the {@code getService} and {@code getServices} methods.
     * <p>
     * Also if there are bundles that were stored in {@link #waitingProviders}, to be processed later, because of
     * missing {@link Parser} for particular format,
     * <p>
     * and then the {@link Parser} service appears, they will be processed.
     *
     * @param reference The reference to the service being added to the {@code ServiceTracker}.
     * @return the service object to be tracked for the specified {@code ServiceReference}.
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object addingService(ServiceReference reference) {
        Parser service = (Parser) bc.getService(reference);
        String key = (String) reference.getProperty(Parser.FORMAT);
        key = key == null ? Parser.FORMAT_JSON : key;
        parsers.put(key, service);
        synchronized (waitingProviders) {
            Iterator<Bundle> i = waitingProviders.keySet().iterator();
            while (i.hasNext()) {
                Bundle bundle = i.next();
                if (bundle.getState() != Bundle.UNINSTALLED) {
                    queue.addingBundle(bundle, new BundleEvent(BundleEvent.INSTALLED, bundle));
                }
            }
        }
        return service;
    }

    /**
     * This method is called when the service being tracked by the {@code ServiceTracker} has had it properties
     * modified. This case is not useful for the {@link Parser} services, so this method do nothing.
     *
     * @param reference The reference to the service that has been modified.
     * @param service The service object for the specified referenced service.
     */
    @Override
    public void modifiedService(ServiceReference reference, Object service) {
        // do nothing
    }

    /**
     * This method is called after a service is no longer being tracked by the {@code ServiceTracker} and removes the
     * {@link Parser} service objects from the structure Map "{@link #parsers}".
     *
     * @param reference The reference to the service that has been removed.
     * @param service The service object for the specified referenced service.
     */
    @Override
    public void removedService(ServiceReference reference, Object service) {
        String key = (String) reference.getProperty(Parser.FORMAT);
        key = key == null ? Parser.FORMAT_JSON : key;
        parsers.remove(key);
    }

    /**
     * This method is inherited from {@link AbstractPersistentProvider}.
     * <p>
     * Extends parent's functionality with closing the {@link Parser} service tracker and
     * <p>
     * sets {@code null} to {@link #parsers}, {@link #providedObjectsHolder}, {@link #providerPortfolio} and
     * {@link #waitingProviders}
     */
    public void close() {
        if (parserTracker != null) {
            parserTracker.close();
            parserTracker = null;
            parsers = null;
        }
        synchronized (providedObjectsHolder) {
            providedObjectsHolder = null;
        }
        synchronized (providerPortfolio) {
            providerPortfolio = null;
        }
        synchronized (waitingProviders) {
            waitingProviders = null;
        }
    }

    /**
     * This method is called from {@link AutomationResourceBundlesEventQueue} to ensure that the tracked bundle is
     * already processed and its version is the same.
     *
     * @param bundle is a {@link Bundle} object, corresponding to the tracked bundle and serves as key in
     *            {@link #waitingProviders}
     * @return {@code true} if the bundle is missing in {@link #waitingProviders} and ID and the Version on the tracked
     *         bundle are matching to these on the {@link Vendor} objects, contained as keys in
     *         {@link #providerPortfolio} and {@code false} in the opposite case.
     */
    public boolean isProviderProcessed(Bundle bundle) {
        boolean res = false;
        synchronized (waitingProviders) {
            res = waitingProviders.get(bundle) == null;
        }
        if (res) {
            Vendor vendor = new Vendor(Long.toString(bundle.getBundleId()), bundle.getVersion().toString());
            synchronized (providerPortfolio) {
                res = providerPortfolio.get(vendor) != null;
            }
        }
        return res;
    }

    /**
     * This method is used in {@link AutomationResourceBundlesEventQueue#open()} to confirms that the provider is ready
     * to work.
     *
     * @return {@code true} if the provider is ready and {@code false} in the opposite case.
     */
    public boolean isReady() {
        return false;
    }

    /**
     * This method provides common functionality for {@link ModuleTypeProvider} and {@link TemplateProvider} to process
     * the bundles. For {@link RuleResourceBundleImporter} this method is overridden.
     * <p>
     * Checks for availability of the needed {@link Parser}. If it is not available - the bundle is added into
     * {@link #waitingProviders} and the execution of the method ends.
     * <p>
     * If it is available, the execution of the method continues with checking if the version of the bundle is changed.
     * If the version is changed - removes persistence of old variants of the objects, provided by this bundle.
     * <p>
     * Continues with loading the new version of these objects. If this bundle is added for the very first time, only
     * loads the provided objects.
     * <p>
     * The loading can fail because of {@link IOException}.
     *
     * @param bundle it is a {@link Bundle} which has to be processed, because it provides resources for automation
     *            objects.
     */
    protected void processAutomationProvider(Bundle bundle) {
        synchronized (providerPortfolio) {
            for (Vendor vendor : providerPortfolio.keySet()) {
                if (vendor.getVendorId().equals(Long.toString(bundle.getBundleId()))
                        && !vendor.getVendorVersion().equals(bundle.getVersion().toString())) {
                    List<String> portfolio = providerPortfolio.remove(vendor);
                    if (portfolio != null && !portfolio.isEmpty())
                        for (String uid : portfolio) {
                            synchronized (providedObjectsHolder) {
                                providedObjectsHolder.remove(uid);
                            }
                        }
                    break;
                }
            }
        }
        Enumeration<URL> urlEnum = bundle.findEntries(path, null, false);
        if (urlEnum == null)
            return;
        Vendor vendor = new Vendor(Long.toString(bundle.getBundleId()), bundle.getVersion().toString());
        while (urlEnum.hasMoreElements()) {
            URL url = urlEnum.nextElement();
            String parserType = getParserType(url);
            Parser<E> parser = parsers.get(parserType);
            synchronized (waitingProviders) {
                List<URL> urlList = waitingProviders.get(bundle);
                if (parser != null) {
                    if (urlList != null && urlList.remove(url) && urlList.isEmpty())
                        waitingProviders.remove(bundle);
                    try {
                        importData(vendor, parser, new InputStreamReader(url.openStream()));
                    } catch (IOException e) {
                        logger.error("Can't read from resource of bundle with ID " + bundle.getBundleId() + ". URL is "
                                + url, e);
                    }
                } else if (parser == null) {
                    if (urlList == null)
                        urlList = new ArrayList<URL>();
                    urlList.add(url);
                    waitingProviders.put(bundle, urlList);
                }
            }
        }
    }

    protected String getParserType(URL url) {
        String fileName = url.getPath();
        int fileExtesionStartIndex = fileName.lastIndexOf(".") + 1;
        if (fileExtesionStartIndex == -1)
            return Parser.FORMAT_JSON;
        String fileExtesion = fileName.substring(fileExtesionStartIndex);
        if (fileExtesion.equals("txt"))
            return Parser.FORMAT_JSON;
        return fileExtesion;
    }

    /**
     * This method provides common functionality for {@link ModuleTypeProvider} and {@link TemplateProvider} to process
     * the bundles. For {@link RuleResourceBundleImporter} this method is overridden.
     * <p>
     * When some of the bundles that provides automation objects is uninstalled, this method will remove it from
     * {@link #waitingProviders}, if it is still there or from {@link #providerPortfolio} in the other case.
     * <p>
     * Will remove the provided objects from {@link #providedObjectsHolder} and will remove their persistence, injected
     * in the system from this bundle.
     *
     * @param bundle the uninstalled {@link Bundle}, provider of automation objects.
     */
    protected void processAutomationProviderUninstalled(Bundle bundle) {
        synchronized (waitingProviders) {
            waitingProviders.remove(bundle);
        }
        List<String> portfolio = null;
        Vendor vendor = new Vendor(Long.toString(bundle.getBundleId()), bundle.getVersion().toString());
        synchronized (providerPortfolio) {
            portfolio = providerPortfolio.remove(vendor);
        }
        if (portfolio != null && portfolio.isEmpty()) {
            Iterator<String> i = portfolio.iterator();
            while (i.hasNext()) {
                String uid = i.next();
                synchronized (providedObjectsHolder) {
                    providedObjectsHolder.remove(uid);
                }
            }
        }
    }

    /**
     * This method is called from {@link #processAutomationProvider(Bundle)} to process the loading of the provided
     * objects.
     *
     * @param parser the {@link Parser} which is responsible for parsing of a particular format in which the provided
     *            objects are presented
     * @param inputStreamReader the {@link InputStreamReader} which is used for loading the objects.
     * @return a set of {@link Status}es, each of them shows the result of loading per object.
     */
    protected abstract Set<Status> importData(Vendor vendor, Parser<E> parser, InputStreamReader inputStreamReader);

}
