/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.common.osgi;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.util.tracker.BundleTracker;

/**
 * The {@link ResolvedBundleTracker} tracks any bundles which have reached the "resolved" state
 * semantically. This means that not only a "resolved" bundle is tracked but also a bundle who
 * has reached the "started" or "starting" state.
 * <p>
 * Override the methods {@link #addingBundle(Bundle)} or {@link #removedBundle(Bundle)} to consume the events.
 * <p>
 * This class is a simple replacement for an <i>OSGi</i> {@link BundleTracker}, whose usage for monitoring semantic
 * states is more complex.
 *
 * @author Michael Grammling - Initial Contribution
 */
public abstract class ResolvedBundleTracker implements SynchronousBundleListener {

    private BundleContext bundleContext;
    private List<Bundle> trackedBundles;

    /**
     * Creates a new instance of this class with the specified parameter.
     *
     * @param bundleContext a bundle context to be used to track any bundles (must not be null)
     * @throws IllegalArgumentException if the bundle context is null
     */
    public ResolvedBundleTracker(BundleContext bundleContext) throws IllegalArgumentException {
        if (bundleContext == null) {
            throw new IllegalArgumentException("The bundle context must not be null!");
        }

        this.bundleContext = bundleContext;
        this.trackedBundles = new ArrayList<Bundle>();
    }

    /**
     * Opens the tracker.
     * <p>
     * For each bundle which is already available and which has reached at least the "resolved" state, an
     * {@link #addingBundle(Bundle)} event is fired.
     */
    public synchronized void open() {
        this.bundleContext.addBundleListener(this);
        initialize();
    }

    /**
     * Closes the tracker.
     * <p>
     * For each tracked bundle a {@link #removedBundle(Bundle)} event is fired.
     */
    public synchronized void close() {
        this.bundleContext.removeBundleListener(this);
        uninitialize();
    }

    private synchronized void initialize() {
        Bundle[] bundles = this.bundleContext.getBundles();
        for (Bundle bundle : bundles) {
            int state = bundle.getState();
            boolean shouldNotBeTracked = (state & (Bundle.INSTALLED | Bundle.UNINSTALLED)) > 0;
            if (shouldNotBeTracked) {
                remove(bundle);
            } else {
                add(bundle);
            }
        }
    }

    private synchronized void uninitialize() {
        for (Bundle bundle : this.trackedBundles) {
            try {
                removedBundle(bundle);
            } catch (Exception ex) {
                // nothing to do
            }
        }

        this.trackedBundles.clear();
    }

    @Override
    public synchronized void bundleChanged(BundleEvent event) {
        Bundle bundle = event.getBundle();
        int type = event.getType();

        boolean shouldBeTracked = (type & ((BundleEvent.STARTING | BundleEvent.STARTED | BundleEvent.RESOLVED))) > 0;
        boolean shouldNotBeTracked = (type & (BundleEvent.UNINSTALLED | BundleEvent.UNRESOLVED)) > 0;

        if (shouldBeTracked) {
            add(bundle);
        } else if (shouldNotBeTracked) {
            remove(bundle);
        }
    }

    private void add(Bundle bundle) {
        if (!this.trackedBundles.contains(bundle)) {
            try {
                if (addingBundle(bundle)) {
                    this.trackedBundles.add(bundle);
                }
            } catch (Exception ex) {
                // nothing to do
            }
        }
    }

    private void remove(Bundle bundle) {
        if (this.trackedBundles.contains(bundle)) {
            try {
                removedBundle(bundle);
            } catch (Exception ex) {
                // nothing to do
            } finally {
                this.trackedBundles.remove(bundle);
            }
        }
    }

    /**
     * The callback method to be invoked when a bundle was detected which at least reached
     * the "resolved" state.
     * <p>
     * This method might be overridden.
     *
     * @param bundle the according bundle (not null)
     * @return true if the bundle should be tracked, otherwise false
     */
    public boolean addingBundle(Bundle bundle) {
        // override this method if needed
        return false;
    }

    /**
     * The callback method to be invoked when a tracked bundle was detected to leave at
     * least the "resolved" state semantically (e.g. if it was "uninstalled", etc.).
     * <p>
     * This method might be overridden.
     *
     * @param bundle the according bundle (not null)
     */
    public void removedBundle(Bundle bundle) {
        // override this method if needed
    }

}
