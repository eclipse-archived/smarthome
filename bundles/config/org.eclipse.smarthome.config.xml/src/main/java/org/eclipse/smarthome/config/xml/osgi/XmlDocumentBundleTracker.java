/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.xml.osgi;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.smarthome.config.xml.util.XmlDocumentReader;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.service.ReadyMarker;
import org.eclipse.smarthome.core.service.ReadyUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.BundleTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link XmlDocumentBundleTracker} tracks files in the specified XML folder
 * of modules and tries to parse them as XML file with the specified
 * {@link XmlDocumentReader}. Any converted XML files are assigned to its
 * according bundle and added to an {@link XmlDocumentProvider} for further
 * processing. For each module an own {@link XmlDocumentProvider} is created by
 * using the specified {@link XmlDocumentProviderFactory}.
 *
 * @author Michael Grammling - Initial Contribution
 * @author Benedikt Niehues - Changed resource handling so that resources can be
 *         patched by fragments.
 * @author Simon Kaufmann - Tracking of remaining bundles
 *
 * @param <T>
 *            the result type of the conversion
 */
public class XmlDocumentBundleTracker<T> extends BundleTracker<Bundle> {

    private final Logger logger = LoggerFactory.getLogger(XmlDocumentBundleTracker.class);
    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("xml-processing");
    private final String xmlDirectory;
    private final XmlDocumentReader<T> xmlDocumentTypeReader;
    private final XmlDocumentProviderFactory<T> xmlDocumentProviderFactory;
    private final Map<Bundle, XmlDocumentProvider<T>> bundleDocumentProviderMap = new ConcurrentHashMap<>();
    private final Map<Bundle, ScheduledFuture<?>> queue = new ConcurrentHashMap<>();
    private final Set<Bundle> finishedBundles = new CopyOnWriteArraySet<>();
    @SuppressWarnings("rawtypes")
    private final Map<String, ServiceRegistration> bundleReadyMarkerRegistrations = new ConcurrentHashMap<>();
    private final String readyMarkerKey;

    @SuppressWarnings("rawtypes")
    private BundleTracker relevantBundlesTracker;

    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param bundleContext
     *            the bundle context to be used for tracking bundles (must not
     *            be null)
     * @param xmlDirectory
     *            the directory to search for XML files (must neither be null,
     *            nor empty)
     * @param xmlDocumentTypeReader
     *            the XML converter to be used (must not be null)
     * @param xmlDocumentProviderFactory
     *            the result object processor to be used (must not be null)
     * @param readyMarkerKey
     *            the key to use for registering {@link ReadyMarker}s
     *
     * @throws IllegalArgumentException
     *             if any of the arguments is null
     */
    public XmlDocumentBundleTracker(BundleContext bundleContext, String xmlDirectory,
            XmlDocumentReader<T> xmlDocumentTypeReader, XmlDocumentProviderFactory<T> xmlDocumentProviderFactory,
            String readyMarkerKey) throws IllegalArgumentException {
        super(bundleContext, Bundle.ACTIVE, null);

        if (bundleContext == null) {
            throw new IllegalArgumentException("The BundleContext must not be null!");
        }
        if ((xmlDirectory == null) || (xmlDirectory.isEmpty())) {
            throw new IllegalArgumentException("The XML directory must neither be null, nor empty!");
        }
        if (xmlDocumentTypeReader == null) {
            throw new IllegalArgumentException("The XmlDocumentTypeReader must not be null!");
        }
        if (xmlDocumentProviderFactory == null) {
            throw new IllegalArgumentException("The XmlDocumentProviderFactory must not be null!");
        }

        this.readyMarkerKey = readyMarkerKey;
        this.xmlDirectory = xmlDirectory;
        this.xmlDocumentTypeReader = xmlDocumentTypeReader;
        this.xmlDocumentProviderFactory = xmlDocumentProviderFactory;
    }

    private boolean isBundleRelevant(Bundle bundle) {
        return bundle.getHeaders().get(Constants.FRAGMENT_HOST) == null && isResourcePresent(bundle, xmlDirectory);
    }

    private Set<Bundle> getRelevantBundles() {
        if (relevantBundlesTracker.getBundles() == null) {
            return Collections.emptySet();
        }
        return Arrays.stream(relevantBundlesTracker.getBundles()).collect(Collectors.toSet());
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public final synchronized void open() {
        relevantBundlesTracker = new BundleTracker(context,
                Bundle.RESOLVED | Bundle.STARTING | Bundle.STOPPING | Bundle.ACTIVE, null) {
            @Override
            public Object addingBundle(Bundle bundle, BundleEvent event) {
                return isBundleRelevant(bundle) ? bundle : null;
            }
        };
        relevantBundlesTracker.open();

        super.open();
    }

    @Override
    public final synchronized void close() {
        super.close();
        unregisterReadyMarkers();
        bundleDocumentProviderMap.clear();
        relevantBundlesTracker.close();
        clearQueue();
        finishedBundles.clear();
    }

    private void clearQueue() {
        for (ScheduledFuture<?> future : queue.values()) {
            future.cancel(true);
        }
        queue.clear();
    }

    private XmlDocumentProvider<T> acquireXmlDocumentProvider(Bundle bundle) {
        if (bundle == null) {
            return null;
        }

        XmlDocumentProvider<T> xmlDocumentProvider = bundleDocumentProviderMap.get(bundle);
        if (xmlDocumentProvider == null) {
            xmlDocumentProvider = xmlDocumentProviderFactory.createDocumentProvider(bundle);
            logger.trace("Create an empty XmlDocumentProvider for the module '{}'.", bundle.getSymbolicName());
            bundleDocumentProviderMap.put(bundle, xmlDocumentProvider);
        }
        return xmlDocumentProvider;
    }

    private void releaseXmlDocumentProvider(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        XmlDocumentProvider<T> xmlDocumentProvider = bundleDocumentProviderMap.get(bundle);
        if (xmlDocumentProvider == null) {
            return;
        }
        try {
            logger.debug("Releasing the XmlDocumentProvider for module '{}'.", bundle.getSymbolicName());
            xmlDocumentProvider.release();
        } catch (Exception e) {
            logger.error("Could not release the XmlDocumentProvider for '{}'!", bundle.getSymbolicName(), e);
        }
        bundleDocumentProviderMap.remove(bundle);
    }

    private void addingObject(Bundle bundle, T object) {
        XmlDocumentProvider<T> xmlDocumentProvider = acquireXmlDocumentProvider(bundle);
        if (xmlDocumentProvider != null) {
            xmlDocumentProvider.addingObject(object);
        }
    }

    private void addingFinished(Bundle bundle) {
        XmlDocumentProvider<T> xmlDocumentProvider = bundleDocumentProviderMap.get(bundle);
        if (xmlDocumentProvider == null) {
            return;
        }
        try {
            xmlDocumentProvider.addingFinished();
        } catch (Exception ex) {
            logger.error("Could not send adding finished event for the module '{}'!", bundle.getSymbolicName(), ex);
        }
    }

    @Override
    public final synchronized Bundle addingBundle(Bundle bundle, BundleEvent event) {
        addingBundle(bundle);
        return bundle;
    }

    @Override
    public final synchronized void removedBundle(Bundle bundle, BundleEvent event, Bundle object) {
        logger.debug("Removing the XML related objects from module '{}'...", bundle.getSymbolicName());
        finishedBundles.remove(bundle);
        ScheduledFuture<?> future = queue.remove(bundle);
        if (future != null) {
            future.cancel(true);
        }
        releaseXmlDocumentProvider(bundle);
        unregisterReadyMarker(bundle);
    }

    /**
     * This method creates a list where all resources are contained
     * except the ones from the host bundle which are also contained in
     * a fragment. So the fragment bundle resources can override the
     * host bundles resources.
     *
     * @param xmlDocumentPaths the paths within the bundle/fragments
     * @param bundle the host bundle
     * @return the URLs of the resources, never {@code null}
     */
    private Collection<URL> filterPatches(Enumeration<URL> xmlDocumentPaths, Bundle bundle) {
        List<URL> hostResources = new ArrayList<URL>();
        List<URL> fragmentResources = new ArrayList<URL>();

        while (xmlDocumentPaths.hasMoreElements()) {
            URL path = xmlDocumentPaths.nextElement();
            if (bundle.getEntry(path.getPath()) != null && bundle.getEntry(path.getPath()).equals(path)) {
                hostResources.add(path);
            } else {
                fragmentResources.add(path);
            }
        }
        if (!fragmentResources.isEmpty()) {
            Map<String, URL> helper = new HashMap<String, URL>();
            for (URL url : hostResources) {
                helper.put(url.getPath(), url);
            }
            for (URL url : fragmentResources) {
                helper.put(url.getPath(), url);
            }
            return helper.values();
        }
        return hostResources;
    }

    /**
     * Checks for the existence of a given resource inside the bundle and its attached fragments.
     *
     * Helper method which can be used in {@link #isBundleRelevant(Bundle)}.
     *
     * @param bundle
     * @param path the directory name to look for
     * @return <code>true</code> if the bundle or one of its attached fragments contain the given directory
     */
    private final boolean isResourcePresent(Bundle bundle, String path) {
        return bundle.getEntry(path) != null;
    }

    /**
     * Add a bundle which potentially needs to be processed.
     *
     * This method should be called in order to queue a new bundle for asynchronous processing.
     * It can be used e.g. by a {@link BundleTracker}, detecting a new bundle.
     * <p>
     * If the bundle actually will be put into the queue depends on the presence of the corresponding XML configuration
     * directory.
     *
     * @param bundle
     */
    private void addingBundle(Bundle bundle) {
        if (!isBundleRelevant(bundle)) {
            finishBundle(bundle);
            return;
        }
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            processBundle(bundle);
        }, 0, TimeUnit.SECONDS);
        queue.put(bundle, future);
    }

    private void finishBundle(Bundle bundle) {
        queue.remove(bundle);
        finishedBundles.add(bundle);
        registerReadyMarker(bundle);

        Set<Bundle> remainingBundles = getRemainingBundles();
        if (!remainingBundles.isEmpty()) {
            logger.trace("Remaining bundles with {}: {}", xmlDirectory, remainingBundles);
        } else {
            logger.trace("Finished loading bundles with {}", xmlDirectory);
            loadingCompleted();
        }
    }

    private Set<Bundle> getRemainingBundles() {
        return getRelevantBundles().stream().filter(b -> !finishedBundles.contains(b)).collect(Collectors.toSet());
    }

    private void processBundle(Bundle bundle) {
        Enumeration<URL> xmlDocumentPaths = bundle.findEntries(xmlDirectory, "*.xml", true);
        if (xmlDocumentPaths != null) {
            Collection<URL> filteredPaths = filterPatches(xmlDocumentPaths, bundle);
            parseDocuments(bundle, filteredPaths);
        }
        finishBundle(bundle);
    }

    private void parseDocuments(Bundle bundle, Collection<URL> filteredPaths) {
        int numberOfParsedXmlDocuments = 0;
        for (URL xmlDocumentURL : filteredPaths) {
            String moduleName = bundle.getSymbolicName();
            String xmlDocumentFile = xmlDocumentURL.getFile();
            logger.debug("Reading the XML document '{}' in module '{}'...", xmlDocumentFile, moduleName);
            try {
                T object = xmlDocumentTypeReader.readFromXML(xmlDocumentURL);
                addingObject(bundle, object);
                numberOfParsedXmlDocuments++;
            } catch (Exception ex) {
                logger.warn("The XML document '{}' in module '{}' could not be parsed: {}", xmlDocumentFile, moduleName,
                        ex.getLocalizedMessage(), ex);
            }
        }
        if (numberOfParsedXmlDocuments > 0) {
            addingFinished(bundle);
        }
    }

    private void registerReadyMarker(Bundle bundle) {
        String bsn = bundle.getSymbolicName();
        if (!bundleReadyMarkerRegistrations.containsKey(bsn)) {
            @SuppressWarnings("rawtypes")
            ServiceRegistration reg = ReadyUtil.markAsReady(context, readyMarkerKey, bsn);
            bundleReadyMarkerRegistrations.put(bsn, reg);
        }
    }

    private void unregisterReadyMarker(Bundle bundle) {
        String bsn = bundle.getSymbolicName();
        @SuppressWarnings("rawtypes")
        ServiceRegistration reg = bundleReadyMarkerRegistrations.remove(bsn);
        if (reg != null) {
            reg.unregister();
        }
    }

    private void unregisterReadyMarkers() {
        for (@SuppressWarnings("rawtypes")
        ServiceRegistration reg : bundleReadyMarkerRegistrations.values()) {
            reg.unregister();
        }
        bundleReadyMarkerRegistrations.clear();
    }

    private void loadingCompleted() {
        // TODO register ready marker to denote overall processing is completed.
    }

    @Override
    public String toString() {
        return super.toString() + "(" + xmlDirectory + ")";
    }

}
