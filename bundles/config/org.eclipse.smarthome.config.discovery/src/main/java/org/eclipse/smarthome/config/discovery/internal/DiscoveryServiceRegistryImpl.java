/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery.internal;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.config.discovery.ScanListener;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;

/**
 * The {@link DiscoveryServiceRegistryImpl} is a concrete implementation of the {@link DiscoveryServiceRegistry}.
 * <p>
 * This implementation tracks any existing {@link DiscoveryService} and registers itself as {@link DiscoveryListener} on
 * it.
 * <p>
 * This implementation does neither handle memory leaks (orphaned listener instances) nor blocked listeners. No
 * performance optimizations have been done (synchronization).
 *
 * @author Michael Grammling - Initial Contribution
 * @author Kai Kreuzer - Refactored API
 * @author Andre Fuechsel - Added removeOlderResults
 *
 * @see DiscoveryServiceRegistry
 * @see DiscoveryListener
 */
public final class DiscoveryServiceRegistryImpl implements DiscoveryServiceRegistry, DiscoveryListener {

    private HashMultimap<DiscoveryService, DiscoveryResult> cachedResults = HashMultimap.create();

    private final class AggregatingScanListener implements ScanListener {

        private final ScanListener listener;
        private int finishedDiscoveryServices = 0;
        private boolean errorOccured = false;
        private int numberOfDiscoveryServices;

        private AggregatingScanListener(int numberOfDiscoveryServices, ScanListener listener) {
            this.numberOfDiscoveryServices = numberOfDiscoveryServices;
            this.listener = listener;
        }

        @Override
        public synchronized void onFinished() {
            synchronized (this) {
                finishedDiscoveryServices++;
                logger.debug("Finished {} of {} discovery services.", finishedDiscoveryServices,
                        numberOfDiscoveryServices);
                if (!errorOccured && finishedDiscoveryServices == numberOfDiscoveryServices) {
                    if (listener != null) {
                        listener.onFinished();
                    }
                }
            }
        }

        @Override
        public void onErrorOccurred(Exception exception) {
            synchronized (this) {
                if (!errorOccured) {
                    if (listener != null) {
                        listener.onErrorOccurred(exception);
                    }
                    errorOccured = true;
                } else {
                    logger.warn("Error occured while executing discovery service: " + exception.getMessage(), exception);
                }
            }
        }

        public void reduceNumberOfDiscoveryServices() {
            synchronized (this) {
                numberOfDiscoveryServices--;
                if (!errorOccured && finishedDiscoveryServices == numberOfDiscoveryServices) {
                    if (listener != null) {
                        listener.onFinished();
                    }
                }
            }
        }
    }

    private List<DiscoveryService> discoveryServices = new CopyOnWriteArrayList<>();

    private Set<DiscoveryListener> listeners = new CopyOnWriteArraySet<>();

    private final Logger logger = LoggerFactory.getLogger(DiscoveryServiceRegistryImpl.class);

    @Override
    public boolean abortScan(ThingTypeUID thingTypeUID) throws IllegalStateException {

        Set<DiscoveryService> discoveryServicesForThingType = getDiscoveryServices(thingTypeUID);

        if (discoveryServicesForThingType.isEmpty()) {
            logger.warn("No discovery service for thing type '{}' found!", thingTypeUID);
            return false;
        }

        return abortScans(discoveryServicesForThingType);
    }

    @Override
    public boolean abortScan(String bindingId) throws IllegalStateException {

        Set<DiscoveryService> discoveryServicesForBinding = getDiscoveryServices(bindingId);

        if (discoveryServicesForBinding.isEmpty()) {
            logger.warn("No discovery service for binding '{}' found!", bindingId);
            return false;
        }

        return abortScans(discoveryServicesForBinding);
    }

    @Override
    public void addDiscoveryListener(DiscoveryListener listener) throws IllegalStateException {
        synchronized (cachedResults) {
            Set<Entry<DiscoveryService, DiscoveryResult>> entries = cachedResults.entries();
            for (Entry<DiscoveryService, DiscoveryResult> entry : entries) {
                listener.thingDiscovered(entry.getKey(), entry.getValue());
            }
        }
        if (listener != null) {
            this.listeners.add(listener);
        }
    }

    @Override
    public boolean startScan(ThingTypeUID thingTypeUID, ScanListener listener) throws IllegalStateException {
        Set<DiscoveryService> discoveryServicesForThingType = getDiscoveryServices(thingTypeUID);

        if (discoveryServicesForThingType.isEmpty()) {
            logger.warn("No discovery service for thing type '{}' found!", thingTypeUID);
            return false;
        }

        return startScans(discoveryServicesForThingType, listener);
    }

    @Override
    public boolean startScan(String bindingId, final ScanListener listener) throws IllegalStateException {

        final Set<DiscoveryService> discoveryServicesForBinding = getDiscoveryServices(bindingId);

        if (discoveryServicesForBinding.isEmpty()) {
            logger.warn("No discovery service for binding id '{}' found!", bindingId);
            return false;
        }

        return startScans(discoveryServicesForBinding, listener);
    }

    @Override
    public boolean supportsDiscovery(ThingTypeUID thingTypeUID) {
        return !getDiscoveryServices(thingTypeUID).isEmpty();
    }

    @Override
    public boolean supportsDiscovery(String bindingId) {
        return !getDiscoveryServices(bindingId).isEmpty();
    }

    @Override
    public List<ThingTypeUID> getSupportedThingTypes() {
        List<ThingTypeUID> thingTypeUIDs = new ArrayList<>();
        for (DiscoveryService discoveryService : this.discoveryServices) {
            thingTypeUIDs.addAll(discoveryService.getSupportedThingTypes());
        }
        return thingTypeUIDs;
    }

    @Override
    public List<String> getSupportedBindings() {
        List<String> bindings = new ArrayList<>();
        for (DiscoveryService discoveryService : this.discoveryServices) {
            Collection<ThingTypeUID> supportedThingTypes = discoveryService.getSupportedThingTypes();
            for (ThingTypeUID thingTypeUID : supportedThingTypes) {
                bindings.add(thingTypeUID.getBindingId());
            }
        }
        return bindings;
    }

    @Override
    public synchronized void removeDiscoveryListener(DiscoveryListener listener) throws IllegalStateException {

        if (listener != null) {
            this.listeners.remove(listener);
        }
    }

    @Override
    public synchronized void thingDiscovered(final DiscoveryService source, final DiscoveryResult result) {
        synchronized (cachedResults) {
            cachedResults.put(source, result);
        }
        for (final DiscoveryListener listener : this.listeners) {
            try {
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    @Override
                    public Void run() {
                        listener.thingDiscovered(source, result);
                        return null;
                    }
                });
            } catch (Exception ex) {
                logger.error("Cannot notify the DiscoveryListener " + listener.getClass().getName()
                        + " on Thing discovered event!", ex);
            }
        }
    }

    @Override
    public synchronized void thingRemoved(final DiscoveryService source, final ThingUID thingUID) {
        synchronized (cachedResults) {
            cachedResults.remove(source, thingUID);
        }
        for (final DiscoveryListener listener : this.listeners) {
            try {
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    @Override
                    public Void run() {
                        listener.thingRemoved(source, thingUID);
                        return null;
                    }
                });
            } catch (Exception ex) {
                logger.error("Cannot notify the DiscoveryListener '" + listener.getClass().getName()
                        + "' on Thing removed event!", ex);
            }
        }
    }

    @Override
    public Collection<ThingUID> removeOlderResults(final DiscoveryService source, final long timestamp,
            final Collection<ThingTypeUID> thingTypeUIDs) {
        HashSet<ThingUID> removedResults = new HashSet<>();
        for (final DiscoveryListener listener : this.listeners) {
            try {
                Collection<ThingUID> olderResults = AccessController
                        .doPrivileged(new PrivilegedAction<Collection<ThingUID>>() {
                            @Override
                            public Collection<ThingUID> run() {
                                return listener.removeOlderResults(source, timestamp, thingTypeUIDs);
                            }
                        });
                if (olderResults != null) {
                    removedResults.addAll(olderResults);
                }
            } catch (Exception ex) {
                logger.error("Cannot notify the DiscoveryListener '" + listener.getClass().getName()
                        + "' on all things removed event!", ex);
            }
        }

        return removedResults;
    }

    private boolean abortScans(Set<DiscoveryService> discoveryServices) {
        boolean allServicesAborted = true;

        for (DiscoveryService discoveryService : discoveryServices) {
            Collection<ThingTypeUID> supportedThingTypes = discoveryService.getSupportedThingTypes();
            try {
                logger.debug("Abort scan for thing types '{}' on '{}'...", supportedThingTypes, discoveryService
                        .getClass().getName());

                discoveryService.abortScan();

                logger.debug("Scan for thing types '{}' aborted on '{}'.", supportedThingTypes, discoveryService
                        .getClass().getName());
            } catch (Exception ex) {
                logger.error("Cannot abort scan for thing types '" + supportedThingTypes + "' on '"
                        + discoveryService.getClass().getName() + "'!", ex);
                allServicesAborted = false;
            }
        }

        return allServicesAborted;
    }

    private boolean startScans(Set<DiscoveryService> discoveryServices, ScanListener listener) {

        boolean atLeastOneDiscoveryServiceHasBeenStarted = false;

        if (discoveryServices.size() > 1) {
            logger.debug("Trying to start {} scans with an aggregating listener.", discoveryServices.size());
            AggregatingScanListener aggregatingScanListener = new AggregatingScanListener(discoveryServices.size(),
                    listener);
            for (DiscoveryService discoveryService : discoveryServices) {
                if (startScan(discoveryService, aggregatingScanListener)) {
                    atLeastOneDiscoveryServiceHasBeenStarted = true;
                } else {
                    logger.debug("Reducing number of discovery services in aggregating listener, because discovery service failed to start scan.");
                    aggregatingScanListener.reduceNumberOfDiscoveryServices();
                }
            }
        } else {
            if (startScan(discoveryServices.iterator().next(), listener)) {
                atLeastOneDiscoveryServiceHasBeenStarted = true;
            }

        }

        return atLeastOneDiscoveryServiceHasBeenStarted;
    }

    private boolean startScan(DiscoveryService discoveryService, ScanListener listener) {
        Collection<ThingTypeUID> supportedThingTypes = discoveryService.getSupportedThingTypes();
        try {
            logger.debug("Triggering scan for thing types '{}' on '{}'...", supportedThingTypes, discoveryService
                    .getClass().getSimpleName());

            discoveryService.startScan(listener);
            return true;
        } catch (Exception ex) {
            logger.error("Cannot trigger scan for thing types '" + supportedThingTypes + "' on '"
                    + discoveryService.getClass().getSimpleName() + "'!", ex);
            return false;
        }
    }

    private synchronized Set<DiscoveryService> getDiscoveryServices(ThingTypeUID thingTypeUID)
            throws IllegalStateException {

        Set<DiscoveryService> discoveryServices = new HashSet<>();

        if (thingTypeUID != null) {
            for (DiscoveryService discoveryService : this.discoveryServices) {
                Collection<ThingTypeUID> discoveryThingTypes = discoveryService.getSupportedThingTypes();
                if (discoveryThingTypes.contains(thingTypeUID)) {
                    discoveryServices.add(discoveryService);
                }
            }
        }

        return discoveryServices;
    }

    private synchronized Set<DiscoveryService> getDiscoveryServices(String bindingId) throws IllegalStateException {

        Set<DiscoveryService> discoveryServices = new HashSet<>();

        for (DiscoveryService discoveryService : this.discoveryServices) {
            Collection<ThingTypeUID> discoveryThingTypes = discoveryService.getSupportedThingTypes();
            for (ThingTypeUID thingTypeUID : discoveryThingTypes) {
                if (thingTypeUID.getBindingId().equals(bindingId)) {
                    discoveryServices.add(discoveryService);
                }
            }
        }

        return discoveryServices;
    }

    protected void addDiscoveryService(DiscoveryService discoveryService) {
        discoveryService.addDiscoveryListener(this);
        this.discoveryServices.add(discoveryService);
    }

    protected void removeDiscoveryService(DiscoveryService discoveryService) {
        this.discoveryServices.remove(discoveryService);
        discoveryService.removeDiscoveryListener(this);
        synchronized (cachedResults) {
            this.cachedResults.removeAll(discoveryService);
        }
    }

    protected void deactivate() {
        this.discoveryServices.clear();
        this.listeners.clear();
        this.cachedResults.clear();
    }

}
