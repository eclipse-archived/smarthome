/*******************************************************************************
 *
 * Copyright (c) 2016  Bosch Software Innovations GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * The Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 *******************************************************************************/
package org.eclipse.smarthome.automation.internal.core.provider;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.smarthome.automation.ManagedRuleProvider;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.core.common.registry.Provider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

/**
 * This class is responsible for tracking the bundles - suppliers of automation resources. It implements
 * {@link BundleTrackerCustomizer} and is notified for events for adding, modifying or removing the bundles.
 *
 * @author Ana Dimova
 *
 */
@SuppressWarnings("deprecation")
public class AutomationResourceBundlesTracker implements BundleTrackerCustomizer<Bundle> {

    /**
     * This field holds a list with an {@link AutomationResourceBundlesEventQueue} instances owned by
     * {@link AbstractResourceBundleProvider}s of {@link ModuleType}s, {@link Template}s and {@link Rule}s.
     */
    @SuppressWarnings("rawtypes")
    private List<AutomationResourceBundlesEventQueue> providerEventsQueue = new ArrayList<AutomationResourceBundlesEventQueue>();

    /**
     * This field holds a reference to an importer of {@link Rule}s.
     */
    protected RuleResourceBundleImporter rImporter;

    /**
     * This field is a bundle tracker for bundles providing automation resources.
     */
    private BundleTracker<Bundle> bTracker;

    /**
     * This field serves for saving the BundleEvents for the bundles providing automation resources until their
     * processing completes. The events have been for adding, modifying or removing a bundle.
     */
    private List<BundleEvent> queue = new LinkedList<BundleEvent>();

    public AutomationResourceBundlesTracker() {
        rImporter = createImporter();
    }

    protected RuleResourceBundleImporter createImporter() {
        return new RuleResourceBundleImporter();
    }

    protected void activate(BundleContext bc) {
        bTracker = new BundleTracker<Bundle>(bc, ~Bundle.UNINSTALLED, this);
        bTracker.open();
    }

    protected void deactivate(BundleContext bc) {
        bTracker.close();
        bTracker = null;
        rImporter.deactivate();
        rImporter = null;
    }

    @SuppressWarnings({ "rawtypes" })
    protected void addProvider(Provider provider) {
        if (provider instanceof AbstractResourceBundleProvider) {
            addAbstractResourceBundleProvider((AbstractResourceBundleProvider) provider);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void addAbstractResourceBundleProvider(AbstractResourceBundleProvider provider) {
        AutomationResourceBundlesEventQueue queue = provider.getQueue();
        synchronized (this.queue) {
            queue.addAll(this.queue);
            providerEventsQueue.add(queue);
        }
    }

    @SuppressWarnings({ "rawtypes" })
    protected void removeProvider(Provider provider) {
        if (provider instanceof AbstractResourceBundleProvider) {
            removeAbstractResourceBundleProvider((AbstractResourceBundleProvider) provider);
        }
    }

    @SuppressWarnings({ "rawtypes" })
    protected void removeAbstractResourceBundleProvider(AbstractResourceBundleProvider provider) {
        AutomationResourceBundlesEventQueue queue = provider.getQueue();
        synchronized (this.queue) {
            providerEventsQueue.remove(queue);
        }
    }

    protected void setManagedRuleProvider(ManagedRuleProvider mProvider) {
        rImporter.setManagedRuleProvider(mProvider);
        rImporter.activate(null);
        addAbstractResourceBundleProvider(rImporter);
    }

    protected void removeManagedRuleProvider(ManagedRuleProvider mProvider) {
        removeAbstractResourceBundleProvider(rImporter);
        rImporter.deactivate();
    }

    /**
     * This method provides functionality for tracking {@link Parser} services.
     *
     * @param parser {@link Parser} service
     * @param properties of the service that has been added.
     */
    protected void addParser(Parser<Rule> parser, Map<String, String> properties) {
        rImporter.addParser(parser, properties);
    }

    /**
     * This method provides functionality for tracking {@link Parser} services.
     *
     * @param parser {@link Parser} service
     * @param properties of the service that has been removed.
     */
    protected void removeParser(Parser<Rule> parser, Map<String, String> properties) {
        rImporter.removeParser(parser, properties);
    }

    protected void setPackageAdmin(PackageAdmin pkgAdmin) {
        HostFragmentMappingUtil.pkgAdmin = pkgAdmin;
    }

    protected void removePackageAdmin(PackageAdmin pkgAdmin) {
        HostFragmentMappingUtil.pkgAdmin = null;
    }

    /**
     * A bundle that provides automation resources is being added to the {@code BundleTracker}.
     *
     * <p>
     * This method is called before a bundle that provides automation resources is added to the {@code BundleTracker}.
     * This method returns the object to be tracked for the specified {@code Bundle}. The returned object is stored in
     * the {@code BundleTracker} and is available from the {@link BundleTracker#getObject(Bundle) getObject} method.
     *
     * @param bundle The {@code Bundle} being added to the {@code BundleTracker} .
     * @param event The bundle event which caused this customizer method to be
     *            called or {@code null} if there is no bundle event associated with
     *            the call to this method.
     * @return The object to be tracked for the specified {@code Bundle} object
     *         or {@code null} if the specified {@code Bundle} object should not
     *         be tracked.
     */
    @Override
    public Bundle addingBundle(Bundle bundle, BundleEvent event) {
        if (isAnAutomationProvider(bundle)) {
            if (HostFragmentMappingUtil.isFragmentBundle(bundle)) {
                List<Bundle> hosts = HostFragmentMappingUtil.returnHostBundles(bundle);
                if (HostFragmentMappingUtil.needToProcessFragment(bundle, hosts)) {
                    addEvent(bundle, event);
                    HostFragmentMappingUtil.fillHostFragmentMapping(hosts);
                }
            } else {
                addEvent(bundle, event);
                HostFragmentMappingUtil.fillHostFragmentMapping(bundle);
            }
        }
        return bundle;
    }

    /**
     * A bundle tracked by the {@code BundleTracker} has been modified.
     *
     * <p>
     * This method is called when a bundle being tracked by the {@code BundleTracker} has had its state modified.
     *
     * @param bundle The {@code Bundle} whose state has been modified.
     * @param event The bundle event which caused this customizer method to be
     *            called or {@code null} if there is no bundle event associated with
     *            the call to this method.
     * @param object The tracked object for the specified bundle.
     */
    @Override
    public void modifiedBundle(Bundle bundle, BundleEvent event, Bundle object) {
        int type = event.getType();
        if (type == BundleEvent.UPDATED || type == BundleEvent.RESOLVED) {
            addEvent(bundle, event);
        }
    }

    /**
     * A bundle tracked by the {@code BundleTracker} has been removed.
     *
     * <p>
     * This method is called after a bundle is no longer being tracked by the {@code BundleTracker}.
     *
     * @param bundle The {@code Bundle} that has been removed.
     * @param event The bundle event which caused this customizer method to be
     *            called or {@code null} if there is no bundle event associated with
     *            the call to this method.
     * @param object The tracked object for the specified bundle.
     */
    @Override
    public void removedBundle(Bundle bundle, BundleEvent event, Bundle object) {
        if (HostFragmentMappingUtil.isFragmentBundle(bundle)) {
            for (Entry<Bundle, List<Bundle>> entry : HostFragmentMappingUtil.getMapping()) {
                if (entry.getValue().contains(bundle)) {
                    Bundle host = entry.getKey();
                    addEvent(host, new BundleEvent(BundleEvent.UPDATED, host));
                }
            }
        } else {
            addEvent(bundle, event);
        }
    }

    /**
     * This method is called when a new event for a bundle providing automation resources is received. It causes a
     * creation of a new thread if there is no other created yet and starting the thread. If the thread already exists,
     * it is waiting for events and will be notified for the event.
     *
     * @param bundle
     *
     * @param event for a bundle tracked by the {@code BundleTracker}. It has been for adding, modifying or removing the
     *            bundle.
     */
    @SuppressWarnings({ "rawtypes" })
    protected void addEvent(Bundle bundle, BundleEvent event) {
        if (event == null) {
            event = initializeEvent(bundle);
        }
        synchronized (queue) {
            queue.add(event);
            for (AutomationResourceBundlesEventQueue queue : providerEventsQueue) {
                queue.addEvent(bundle, event);
            }
        }
    }

    private BundleEvent initializeEvent(Bundle bundle) {
        switch (bundle.getState()) {
            case Bundle.INSTALLED:
                return new BundleEvent(BundleEvent.INSTALLED, bundle);
            case Bundle.RESOLVED:
                return new BundleEvent(BundleEvent.RESOLVED, bundle);
            default:
                return new BundleEvent(BundleEvent.STARTED, bundle);
        }
    }

    /**
     * This method is used to check if the specified {@code Bundle} contains resource files providing automation
     * resources.
     *
     * @param bundle is a {@link Bundle} object to check.
     * @return <tt>true</tt> if the specified {@link Bundle} contains resource files providing automation
     *         resources, <tt>false</tt> otherwise.
     */
    private boolean isAnAutomationProvider(Bundle bundle) {
        return bundle.findEntries(AbstractResourceBundleProvider.PATH, null, false) != null;
    }

}
