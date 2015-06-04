package org.eclipse.smarthome.automation.commands;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The OSGi Bundle Activator.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 */
public class Activator implements BundleActivator {

    /** The s reg. */
    private ServiceRegistration sReg;

    private ServiceTracker hamTracker;

    private ServiceReference hamRef;

    private BundleContext bc;

    private Object tReg;

    private AutomationCommands loadCommands;

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext bc) throws Exception {
        this.bc = bc;
        // open trackers
        loadCommands = new AutomationCommandsPluggable(bc);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext bc) throws Exception {
        loadCommands.stop();
    }

}
