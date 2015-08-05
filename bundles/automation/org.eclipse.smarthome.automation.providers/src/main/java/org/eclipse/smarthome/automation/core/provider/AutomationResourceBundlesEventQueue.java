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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.provider.ModuleTypeProvider;
import org.eclipse.smarthome.automation.provider.TemplateProvider;

/**
 * This class is responsible for tracking the bundles providing automation resources and delegating the processing to
 * the responsible providers in separate thread.
 * 
 * @author Ana Dimova - Initial Contribution
 * 
 */
class AutomationResourceBundlesEventQueue implements Runnable, BundleTrackerCustomizer {

    /**
     * This static field serves as criteria for recognizing bundles providing automation resources. If these bundles
     * have such manifest header this means that they are such providers.
     */
    public static final String AUTOMATION_RESOURCES_HEADER = "Automation-ResourceType";

    /**
     * This field serves for saving the bundles providing automation resources until their processing completes.
     */
    private List queue = new ArrayList();

    /**
     * These fields are for synchronization purposes
     */
    private boolean running = false;
    private boolean closed = false;
    private boolean shared = false;

    /**
     * This field is a bundle tracker for bundles providing automation resources.
     */
    private BundleTracker bTracker;

    /**
     * This field holds a reference to an implementation of {@link TemplateProvider}.
     */
    private TemplateResourceBundleProvider tProvider;

    /**
     * This field holds a reference to an implementation of {@link ModuleTypeProvider}.
     */
    private ModuleTypeResourceBundleProvider mProvider;

    /**
     * This field holds a reference to an importer for {@link Rule}s.
     */
    private RuleResourceBundleImporter rImporter;

    /**
     * This constructor is responsible for initializing the tracker for bundles providing automation resources and their
     * providers.
     * 
     * @param bc is the execution context of the bundle being started, serves for creation of the tracker.
     * @param tProvider is a reference to an implementation of {@link TemplateProvider}.
     * @param mProvider is a reference to an implementation of {@link ModuleTypeProvider}.
     * @param rImporter is a reference to an importer for {@link Rule}s.
     */
    public AutomationResourceBundlesEventQueue(BundleContext bc, TemplateResourceBundleProvider tProvider,
            ModuleTypeResourceBundleProvider mProvider, RuleResourceBundleImporter rImporter) {
        this.tProvider = tProvider;
        this.mProvider = mProvider;
        this.rImporter = rImporter;
        bTracker = new BundleTracker(bc, ~Bundle.UNINSTALLED, this);
    }

    /**
     * This method serves to open the bundle tracker when all providers are ready for work.
     */
    public void open() {
        if (tProvider.isReady() && mProvider.isReady() && rImporter.isReady())
            bTracker.open();
    }

    /**
     * When a new event for a bundle providing automation resources is received, this will causes a creation of a new
     * thread if there is no other created yet. If the thread already exists, then it will be notified for the event.
     * Starting the thread will cause the execution of this method in separate thread.
     * <p>
     * The general contract of this method <code>run</code> is invoking of the
     * {@link #processBundleChanged(BundleEvent)} method and executing it in separate thread.
     * 
     * @see java.lang.Thread#run()
     */
    public void run() {
        boolean waitForEvents = true;
        while (true) {
            List l_queue = null;
            synchronized (this) {
                if (closed) {
                    notifyAll();
                    return;
                }
                if (queue.isEmpty()) {
                    if (waitForEvents) {
                        try {
                            wait(180000);
                        } catch (Throwable _) {
                        }
                        waitForEvents = false;
                        continue;
                    }
                    running = false;
                    notifyAll();
                    return;
                }
                l_queue = queue;
                shared = true;
            }
            Iterator events = l_queue.iterator();
            while (events.hasNext()) {
                processBundleChanged((BundleEvent) events.next());
            }
            synchronized (this) {
                if (shared)
                    queue.clear();
                shared = false;
                waitForEvents = true;
                notifyAll();
            }
        }
    }

    /**
     * This method is invoked when the bundle stops to close the bundle tracker and to stop the separate thread if still
     * running.
     */
    public void stop() {
        synchronized (this) {
            closed = true;
            notifyAll();
        }
        bTracker.close();
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
    public Object addingBundle(Bundle bundle, BundleEvent event) {
        if (isAnAutomationProvider(bundle)) {
            addEvent(event == null ? new BundleEvent(BundleEvent.INSTALLED, bundle) : event);
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
    public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {
        if (isAnAutomationProvider(bundle) && event.getType() == BundleEvent.UPDATED) {
            addEvent(event);
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
    public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
        if (!closed) {
            if (isAnAutomationProvider(bundle)) {
                addEvent(event);
            }
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
        return bundle.getHeaders().get(AUTOMATION_RESOURCES_HEADER) != null;
    }

    /**
     * This method is called when a new event for a bundle providing automation resources is received. It causes a
     * creation of a new thread if there is no other created yet and starting the thread. If the thread already exists,
     * it is waiting for events and will be notified for the event.
     * 
     * @param event for a bundle tracked by the {@code BundleTracker}. It has been for adding, modifying or removing the
     *            bundle.
     */
    private synchronized void addEvent(BundleEvent event) {
        if (closed)
            return;
        if (shared) {
            queue = new ArrayList();
            shared = false;
        }
        if (queue.add(event)) {
            if (running)
                notifyAll();
            else {
                Thread th = new Thread(this, "Automation Provider Processing Queue");
                th.start();
                running = true;
            }
        }
    }

    /**
     * Depending on the action committed against the bundle supplier of automation resources, this method performs the
     * appropriate actions - calls for the each provider:
     * <ul>
     * <p>
     * {@link AbstractResourceBundleProvider#processAutomationProviderUninstalled(Bundle)} method,
     * <p>
     * {@link AbstractResourceBundleProvider#processAutomationProvider(Bundle)} method
     * <p>
     * or both in this order.
     * <p>
     * </ul>
     * 
     * @param event for a bundle tracked by the {@code BundleTracker}. It has been for adding, modifying or removing the
     *            bundle.
     */
    private void processBundleChanged(BundleEvent event) {
        Bundle bundle = event.getBundle();
        Vendor vendor = new Vendor(Long.toString(bundle.getBundleId()), bundle.getVersion().toString());
        switch (event.getType()) {
            case BundleEvent.UPDATED:
                if (!mProvider.isProviderProcessed(vendor)) {
                    mProvider.processAutomationProviderUninstalled(bundle);
                    mProvider.processAutomationProvider(bundle);
                }
                if (!tProvider.isProviderProcessed(vendor)) {
                    tProvider.processAutomationProviderUninstalled(bundle);
                    tProvider.processAutomationProvider(bundle);
                }
                if (!rImporter.isProviderProcessed(vendor)) {
                    rImporter.processAutomationProviderUninstalled(bundle);
                    rImporter.processAutomationProvider(bundle);
                }
                break;
            case BundleEvent.UNINSTALLED:
                if (!mProvider.isProviderProcessed(vendor)) {
                    mProvider.processAutomationProviderUninstalled(bundle);
                }
                if (!tProvider.isProviderProcessed(vendor)) {
                    tProvider.processAutomationProviderUninstalled(bundle);
                }
                if (!rImporter.isProviderProcessed(vendor)) {
                    rImporter.processAutomationProviderUninstalled(bundle);
                }
                break;
            default:
                if (!mProvider.isProviderProcessed(vendor)) {
                    mProvider.processAutomationProvider(bundle);
                }
                if (!tProvider.isProviderProcessed(vendor)) {
                    tProvider.processAutomationProvider(bundle);
                }
                if (!rImporter.isProviderProcessed(vendor)) {
                    rImporter.processAutomationProvider(bundle);
                }
        }
    }
}
