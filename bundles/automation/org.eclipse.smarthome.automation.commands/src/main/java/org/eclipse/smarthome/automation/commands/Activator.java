package org.eclipse.smarthome.automation.commands;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import org.eclipse.smarthome.automation.handler.parser.Parser;

// TODO: Auto-generated Javadoc
/**
 * The Class Activator.
 */
public class Activator implements BundleActivator {
  
  
  /** The s reg. */
  private ServiceRegistration sReg;

  private ServiceTracker hamTracker;

  private ServiceReference hamRef;

  private BundleContext bc;

  private Object tReg;

  private AutomationCommands loadCommands;
    
  /* (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext bc) throws Exception {
    this.bc = bc;
    //open trackers
    loadCommands = new AutomationCommandsPluggable(bc);
  }

  
  /* (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext bc) throws Exception {
    loadCommands.stop();
  }
  
}
