/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.xml.osgi;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;

import org.osgi.framework.Bundle;
import org.osgi.util.tracker.BundleTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles processing of bundles in an asynchronous way.
 * 
 * This helper class can be used in order to process bundles asynchronously, e.g.
 * loading some XML configuration content.
 * <p>
 * The {@link AbstractAsyncBundleProcessor} maintains a queue and takes care by itself for spawning a
 * new thread in order to process the bundles one by one, following the FIFO principle.
 * <p>
 * Subclasses must implement {@link #processBundle(Bundle)}, where
 * the actual bundle processing logic must be provided.
 * <p>
 * If it is possible easily to determine if a bundle actually is relevant for later processing,
 * e.g. by presence of a OSGi Manifest parameter or a directory,
 * the {@link #isBundleRelevant(Bundle)} method can be overridden for this purpose.
 * 
 * @author Simon Kaufmann - Initial contribution and API
 * @author Benedikt Niehues - added helper method for filtering patched resources.
 * 
 */

public abstract class AbstractAsyncBundleProcessor {

    private final Logger logger = LoggerFactory.getLogger(AbstractAsyncBundleProcessor.class);

    private Thread thread;

    private final Queue<Bundle> queue = new ConcurrentLinkedQueue<>();

    private static final Set<AbstractAsyncBundleProcessor> ALL_PROCESSORS = new CopyOnWriteArraySet<>();

    /**
     * This method creates a list where all resources are contained
     * except the ones from the host bundle which are also contained in
     * a fragment. So the fragment bundle resources can override the
     * host bundles resources.
     * 
     * @param xmlDocumentPaths
     * @param bundle
     * @return
     */
    protected Collection<URL> filterPatches(Enumeration<URL> xmlDocumentPaths, Bundle bundle) {
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
     * Determines whether a bundle is relevant to be further processed or not.
     * 
     * Subclasses may override this method in order to determine in an efficient
     * way if the bundle is relevant to be processed or not. This usually should
     * happen in a cost-effective way, such as parsing the bundle's manifest for
     * a header.
     * 
     * @param bundle
     * @return <code>true</code> if the bundle should be queued for further
     *         processing (default).
     */
    protected boolean isBundleRelevant(Bundle bundle) {
        return true;
    }

    /**
     * Checks for the existence of a given directors inside the bundle.
     *
     * Helper method which can be used in {@link #isBundleRelevant(Bundle)}.
     * 
     * @param bundle
     * @param path the directory name to look for
     * @return <code>true</code> id the bundle contains the given directory
     */
    protected final boolean isDirectoryPresent(Bundle bundle, String path) {
        return bundle.getEntry(path) != null;
    }

    /**
     * Process the given bundle.
     * 
     * Subclasses must override this method and handle the bundle processing
     * according to the intended purpose.
     * <p>
     * This method will be called from a separate thread.
     * <p>
     * Exceptions which are thrown will get caught and logged, but not handled
     * otherwise.
     * 
     * @param bundle
     */
    protected abstract void processBundle(Bundle bundle);

    /**
     * Add a bundle which potentially needs to be processed.
     * 
     * This method should be called in order to queue a new bundle for asynchronous processing.
     * It can be used e.g. by a {@link BundleTracker}, detecting a new bundle.
     * <p>
     * If the bundle actually will be put into the queue depends on the outcome if
     * {@link #isBundleRelevant(Bundle)}.
     * 
     * @param bundle
     */
    public void addingBundle(Bundle bundle) {
        if (!isBundleRelevant(bundle)) {
            return;
        }
        queue.add(bundle);
        startThread();
    }

    private void startThread() {
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(processorRunnable, "Bundle processor thread");
            thread.start();
            ALL_PROCESSORS.add(this);
        }
    }

    private boolean isFinishedLoading(Bundle bundle) {
        return !queue.contains(bundle);
    }

    /**
     * Determines if a know relevant bundle's configuration has been processed
     * yet.
     * 
     * <p>
     * NOTE: This method is primarily intended to be used in testing scenarios.
     * 
     * @param bundle
     * @return
     */
    public static boolean isBundleFinishedLoading(Bundle bundle) {
        for (AbstractAsyncBundleProcessor processor : ALL_PROCESSORS) {
            if (!processor.isFinishedLoading(bundle)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Notifies the {@link AbstractAsyncBundleProcessor} that a bundle has been
     * removed.
     * 
     * Needs to be called by the {@link BundleTracker} when a bundle was
     * removed.
     * 
     * @param bundle
     */
    public void removeBundle(Bundle bundle) {
        queue.remove(bundle);
    }

    private final Runnable processorRunnable = new Runnable() {
        @Override
        public void run() {
            AbstractAsyncBundleProcessor.this.logger.trace("Bundle processor thread started");
            while (!queue.isEmpty()) {
                Bundle bundle = null;

                // get first element from the queue, but keep it in
                // there in order to indicate it's not yet processed
                bundle = queue.peek();

                // process the bundle
                if (bundle != null) {
                    try {
                        processBundle(bundle);
                    } catch (Exception e) {
                        AbstractAsyncBundleProcessor.this.logger
                                .error("Exception processing bundle " + bundle.getSymbolicName(), e);
                    }
                }

                // remove bundle from queue
                if (bundle != null) {
                    queue.remove(bundle);
                }
            }
            AbstractAsyncBundleProcessor.this.logger.trace("Terminating gracefully");
            ALL_PROCESSORS.remove(this);
        }
    };

}
