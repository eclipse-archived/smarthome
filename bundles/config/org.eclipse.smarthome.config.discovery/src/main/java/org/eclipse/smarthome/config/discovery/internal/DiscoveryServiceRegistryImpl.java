package org.eclipse.smarthome.config.discovery.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceInfo;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DiscoveryServiceRegistryImpl} is a concrete implementation of the
 * {@link DiscoveryServiceRegistry}.
 * <p>
 * This implementation tracks any existing {@link DiscoveryService} and
 * registers itself as {@link DiscoveryListener} on it.
 * <p>
 * This service must be started by calling {@link #open()} and stopped by
 * calling {@link #close()}. To uninitialize this implementation
 * {@link #release()} must be called (this also closes implicitly any tracked
 * service).
 * <p>
 * This implementation does neither handle memory leaks (orphaned listener
 * instances) nor blocked listeners. No performance optimizations have been done
 * (synchronization).
 * 
 * @author Michael Grammling - Initial Contribution
 * 
 * @see DiscoveryServiceRegistry
 * @see DiscoveryListener
 */
public final class DiscoveryServiceRegistryImpl implements DiscoveryServiceRegistry,
        DiscoveryListener {

    private Logger logger = LoggerFactory.getLogger(DiscoveryServiceRegistryImpl.class);

    private List<DiscoveryListener> listeners;

    private ServiceTracker discoveryServiceTracker;
    private List<DiscoveryService> discoveryServiceList;

    private boolean invalid;

    public DiscoveryServiceRegistryImpl(final BundleContext bundleContext)
            throws IllegalArgumentException {

        if (bundleContext == null) {
            throw new IllegalArgumentException("The BundleContext must not be null!");
        }

        this.listeners = new ArrayList<>();

        this.discoveryServiceList = new ArrayList<>();

        this.discoveryServiceTracker = new ServiceTracker(bundleContext,
                DiscoveryService.class.getName(), new ServiceTrackerCustomizer() {

                    @Override
                    public Object addingService(ServiceReference reference) {
                        synchronized (DiscoveryServiceRegistryImpl.this) {
                            DiscoveryService service = bundleContext.getService(reference);

                            if (service != null) {
                                logger.debug("Add DiscoveryService '{}'.", service.getClass()
                                        .getName());

                                try {
                                    service.addDiscoveryListener(DiscoveryServiceRegistryImpl.this);
                                    discoveryServiceList.add(service);

                                    return service;
                                } catch (Exception ex) {
                                    logger.error("Could not register the DiscoveryListener at the"
                                            + " DiscoveryService '" + service.getClass().getName()
                                            + "'!", ex);
                                }
                            }

                            return null;
                        }
                    }

                    @Override
                    public void modifiedService(ServiceReference reference, Object service) {
                        // nothing to do
                    }

                    @Override
                    public void removedService(ServiceReference reference, Object service) {
                        synchronized (DiscoveryServiceRegistryImpl.this) {
                            if (service != null) {
                                logger.debug("Remove DiscoveryService '{}'.", service.getClass()
                                        .getName());

                                try {
                                    discoveryServiceList.remove(service);

                                    ((DiscoveryService) service)
                                            .removeDiscoveryListener(DiscoveryServiceRegistryImpl.this);
                                } catch (Exception ex) {
                                    logger.error(
                                            "Could not unregister the DiscoveryListener at the"
                                                    + " DiscoveryService '"
                                                    + service.getClass().getName() + "'!", ex);
                                }
                            }
                        }
                    }

                });
    }

    public synchronized void open() {
        if (!this.invalid) {
            this.discoveryServiceTracker.open();
        }
    }

    public synchronized void close() {
        if (!this.invalid) {
            this.discoveryServiceTracker.close();
        }
    }

    public synchronized void release() {
        if (!this.invalid) {
            this.invalid = true;

            this.discoveryServiceTracker.close();

            this.discoveryServiceList.clear();
            this.listeners.clear();
        }
    }

    private synchronized DiscoveryService getDiscoveryService(ThingTypeUID thingTypeUID)
            throws IllegalStateException {

        if (this.invalid) {
            throw new IllegalStateException("The service is not available!");
        }

        if (thingTypeUID != null) {
            for (DiscoveryService discoveryService : this.discoveryServiceList) {
                DiscoveryServiceInfo discoveryInfo = discoveryService.getInfo();
                if (discoveryInfo != null) {
                    List<ThingTypeUID> discoveryThingTypes = discoveryInfo.getSupportedThingTypes();
                    for (ThingTypeUID discoveryThingType : discoveryThingTypes) {
                        if (thingTypeUID.equals(discoveryThingType)) {
                            return discoveryService;
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public DiscoveryServiceInfo getDiscoveryInfo(ThingTypeUID thingTypeUID) {
        DiscoveryService discoveryService = getDiscoveryService(thingTypeUID);

        if (discoveryService != null) {
            return discoveryService.getInfo();
        }

        return null;
    }

    @Override
    public boolean forceDiscovery(ThingTypeUID thingTypeUID) throws IllegalStateException {
        DiscoveryService discoveryService = getDiscoveryService(thingTypeUID);
        if (discoveryService != null) {
            try {
                this.logger.debug("Force discovery for Thing type '{}' on '{}'...", thingTypeUID,
                        discoveryService.getClass().getName());

                discoveryService.forceDiscovery();

                this.logger.debug("Discovery for Thing type '{}' forced on '{}'.", thingTypeUID,
                        discoveryService.getClass().getName());

                return true;
            } catch (Exception ex) {
                this.logger.error("Cannot force discovery for Thing type '" + thingTypeUID
                        + "' on '"
                        + discoveryService.getClass().getName() + "'!", ex);
            }
        }

        return false;
    }

    @Override
    public boolean abortForceDiscovery(ThingTypeUID thingTypeUID) throws IllegalStateException {
        DiscoveryService discoveryService = getDiscoveryService(thingTypeUID);
        if (discoveryService != null) {
            try {
                this.logger.debug("Abort discovery for Thing type '{}' on '{}'...", thingTypeUID,
                        discoveryService.getClass().getName());

                discoveryService.abortForceDiscovery();

                this.logger.debug("Force discovery for Thing type '{}' aborted on '{}'.",
                        thingTypeUID, discoveryService.getClass().getName());

                return true;
            } catch (Exception ex) {
                this.logger.error("Cannot abort force discovery for Thing type '" + thingTypeUID
                        + "' on '" + discoveryService.getClass().getName() + "'!", ex);
            }
        }

        return false;
    }

    @Override
    public void addDiscoveryListener(DiscoveryListener listener) throws IllegalStateException {
        if (listener != null) {
            synchronized (this) {
                if (this.invalid) {
                    throw new IllegalStateException("The service is not available!");
                }

                if (!this.listeners.contains(listener)) {
                    this.listeners.add(listener);
                }
            }
        }
    }

    @Override
    public synchronized void removeDiscoveryListener(DiscoveryListener listener)
            throws IllegalStateException {

        if (listener != null) {
            synchronized (this) {
                if (this.invalid) {
                    throw new IllegalStateException("The service is not available!");
                }

                this.listeners.remove(listener);
            }
        }
    }

    @Override
    public synchronized void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
        for (DiscoveryListener listener : this.listeners) {
            try {
                listener.thingDiscovered(source, result);
            } catch (Exception ex) {
                this.logger.error("Cannot notify the DiscoveryListener "
                        + listener.getClass().getName() + " on Thing discovered event!", ex);
            }
        }
    }

    @Override
    public synchronized void thingRemoved(DiscoveryService source, ThingUID thingUID) {
        for (DiscoveryListener listener : this.listeners) {
            try {
                listener.thingRemoved(source, thingUID);
            } catch (Exception ex) {
                this.logger.error("Cannot notify the DiscoveryListener '"
                        + listener.getClass().getName() + "' on Thing removed event!", ex);
            }
        }
    }

    @Override
    public synchronized void discoveryFinished(DiscoveryService source) {
        for (DiscoveryListener listener : this.listeners) {
            try {
                listener.discoveryFinished(source);
            } catch (Exception ex) {
                this.logger.error("Cannot notify the DiscoveryListener '"
                        + listener.getClass().getName() + "' on discovery finished event!", ex);
            }
        }
    }

    @Override
    public synchronized void discoveryErrorOccurred(DiscoveryService source, Exception exception) {
        for (DiscoveryListener listener : this.listeners) {
            try {
                listener.discoveryErrorOccurred(source, exception);
            } catch (Exception ex) {
                this.logger.error("Cannot notify the DiscoveryListener '"
                        + listener.getClass().getName() + "' on error occurred event!", ex);
            }
        }
    }

}
