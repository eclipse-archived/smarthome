/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal.i18n;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.smarthome.core.common.osgi.ResolvedBundleTracker;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The {@link ResourceBundleTracker} class tracks all <i>OSGi</i> bundles which are in the {@link Bundle#RESOLVED} state
 * or which it already passed (e.g. {@link Bundle#STARTING} or {@link Bundle#ACTIVE}). Only bundles which contains i18n
 * resource files are considered
 * within this tracker.
 * <p>
 * This tracker must be started by calling {@link #open()} and stopped by calling {@link #close()}.
 *
 * @author Michael Grammling - Initial Contribution
 */
public class ResourceBundleTracker extends ResolvedBundleTracker {

    private Map<Bundle, LanguageResourceBundleManager> bundleLanguageResourceMap;

    public ResourceBundleTracker(BundleContext bundleContext) throws IllegalArgumentException {
        super(bundleContext); // can throw an IllegalArgumentException

        this.bundleLanguageResourceMap = new LinkedHashMap<Bundle, LanguageResourceBundleManager>();
    }

    @Override
    public synchronized void open() {
        super.open();
    }

    @Override
    public synchronized void close() {
        super.close();
        this.bundleLanguageResourceMap.clear();
    }

    @Override
    public synchronized boolean addingBundle(Bundle bundle) {
        if (!this.bundleLanguageResourceMap.containsKey(bundle)) {
            LanguageResourceBundleManager languageResource = new LanguageResourceBundleManager(bundle);

            if (languageResource.containsResources()) {
                this.bundleLanguageResourceMap.put(bundle, languageResource);

                return true;
            }
        }

        return false;
    }

    @Override
    public synchronized void removedBundle(Bundle bundle) {
        LanguageResourceBundleManager languageResource = this.bundleLanguageResourceMap.remove(bundle);
        if (languageResource != null) {
            languageResource.clearCache();
        }
    }

    /**
     * Returns the {@link LanguageResourceBundleManager} instance for the specified bundle,
     * or {@code null} if it cannot be found within that tracker.
     *
     * @param bundle the bundle which points to the specific resource manager (could be null)
     * @return the specific resource manager (could be null)
     */
    public LanguageResourceBundleManager getLanguageResource(Bundle bundle) {
        if (bundle != null) {
            return this.bundleLanguageResourceMap.get(bundle);
        }

        return null;
    }

    /**
     * Returns all {@link LanguageResourceBundleManager} instances managed by this tracker.
     *
     * @return the list of all resource managers (not null, could be empty)
     */
    public Collection<LanguageResourceBundleManager> getAllLanguageResources() {
        return this.bundleLanguageResourceMap.values();
    }

}
