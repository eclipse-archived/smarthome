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

import org.eclipse.smarthome.automation.template.Template;

/**
 * @author Ana Dimova
 *
 */
class AutomationResourceBundlesEventQueue implements Runnable, BundleTrackerCustomizer {

  public static final String AUTOMATION_RESOURCES_HEADER = "Automation-ResourceType";

  private List queue = new ArrayList();
  
  private boolean running = false;
  private boolean closed  = false;
  private boolean shared  = false;
  
  private BundleTracker bTracker;
  private TemplateResourceBundleProvider tProvider;
  private ModuleTypeResourceBundleProvider mProvider;
  private RuleResourceBundleImporter rProvider;
  
  public AutomationResourceBundlesEventQueue(BundleContext bc, TemplateResourceBundleProvider tProvider,
      ModuleTypeResourceBundleProvider mProvider, RuleResourceBundleImporter rProvider) {
    this.tProvider = tProvider;
    this.mProvider = mProvider;
    this.rProvider = rProvider;
    bTracker = new BundleTracker(bc, ~Bundle.UNINSTALLED, this);
  }
  
  public void open() {
    bTracker.open();
  }

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
            }
            catch (Throwable _) {}
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
      try {
        Iterator events = l_queue.iterator();
        while (events.hasNext()) {
          processBundleChanged((BundleEvent) events.next());
        }
      }
      catch (Throwable t) {
        t.printStackTrace(); // TODO
      }
      synchronized (this) {
        if (shared) queue.clear();
        shared = false;
        waitForEvents = true;
        notifyAll();
      }
    }
  }
  
  public void stop() {
    synchronized (this) {
      closed = true;
      notifyAll();
    }
    bTracker.close();
  }
  
 /**
   * @see org.osgi.util.tracker.BundleTrackerCustomizer#addingBundle(org.osgi.framework.Bundle, org.osgi.framework.BundleEvent)
   */
  public Object addingBundle(Bundle bundle, BundleEvent event) {
    if (isAnAutomationProvider(bundle)) {
      addEvent(event == null ? new BundleEvent(BundleEvent.INSTALLED, bundle) : event);
    }
    return bundle;
  }

  /**
   * @see org.osgi.util.tracker.BundleTrackerCustomizer#modifiedBundle(org.osgi.framework.Bundle, org.osgi.framework.BundleEvent, java.lang.Object)
   */
  public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {
    if (isAnAutomationProvider(bundle) && event.getType() == BundleEvent.UPDATED) {
        addEvent(event);
    }
  }

  /**
   * @see org.osgi.util.tracker.BundleTrackerCustomizer#removedBundle(org.osgi.framework.Bundle, org.osgi.framework.BundleEvent, java.lang.Object)
   */
  public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
    if (!closed) {
      if (isAnAutomationProvider(bundle)) {
        addEvent(event);
      }
    }
  }
  
  /**
   * This method is used to check if the specified {@link Bundle} contains resource files providing {@link Template}s.
   * @param bundle is a {@link Bundle} object to check.
   * @return <tt>true</tt> if the specified {@link Bundle} contains resource files providing {@link Template}s, <tt>false</tt> otherwise.
   */
  private boolean isAnAutomationProvider(Bundle bundle) {
    return bundle.getHeaders().get(AUTOMATION_RESOURCES_HEADER) != null;
  }

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
        Thread th = new Thread(this, "Template Provider Processing Queue");
        th.start();
        running = true;
      }
    }
  }

  
  private void processBundleChanged(BundleEvent event) {
    Bundle bundle = event.getBundle();
    switch (event.getType()) {
      case BundleEvent.UPDATED:
        rProvider.processAutomationProviderUninstalled(bundle);
        tProvider.processAutomationProviderUninstalled(bundle);
        mProvider.processAutomationProviderUninstalled(bundle);
        mProvider.processAutomationProvider(bundle);
        tProvider.processAutomationProvider(bundle);
        rProvider.processAutomationProvider(bundle);
        break;
      case BundleEvent.UNINSTALLED:
        rProvider.processAutomationProviderUninstalled(bundle);
        tProvider.processAutomationProviderUninstalled(bundle);
        mProvider.processAutomationProviderUninstalled(bundle);
        break;
      default:
        mProvider.processAutomationProvider(bundle);
        tProvider.processAutomationProvider(bundle);
        rProvider.processAutomationProvider(bundle);
    }
  }
}
 