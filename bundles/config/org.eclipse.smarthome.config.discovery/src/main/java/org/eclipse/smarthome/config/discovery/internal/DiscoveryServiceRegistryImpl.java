/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery.internal;

import java.util.Collection;
import java.util.List;
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

/**
 * The {@link DiscoveryServiceRegistryImpl} is a concrete implementation of the
 * {@link DiscoveryServiceRegistry}.
 * <p>
 * This implementation tracks any existing {@link DiscoveryService} and
 * registers itself as {@link DiscoveryListener} on it.
 * <p>
 * This implementation does neither handle memory leaks (orphaned listener
 * instances) nor blocked listeners. No performance optimizations have been done
 * (synchronization).
 * 
 * @author Michael Grammling - Initial Contribution
 * @author Kai Kreuzer - Refactored API
 * 
 * @see DiscoveryServiceRegistry
 * @see DiscoveryListener
 */
public final class DiscoveryServiceRegistryImpl implements DiscoveryServiceRegistry,
        DiscoveryListener {

    private List<DiscoveryService> discoveryServices = new CopyOnWriteArrayList<>();

    private Set<DiscoveryListener> listeners = new CopyOnWriteArraySet<>();
    
    static final private Logger logger = LoggerFactory.getLogger(DiscoveryServiceRegistryImpl.class);

    @Override
    public boolean abortScan(ThingTypeUID thingTypeUID) throws IllegalStateException {
        DiscoveryService discoveryService = getDiscoveryService(thingTypeUID);
        if (discoveryService != null) {
            try {
                logger.debug("Abort scan for thing type '{}' on '{}'...", thingTypeUID,
                        discoveryService.getClass().getName());

                discoveryService.abortScan();

                logger.debug("Scan for thing type '{}' aborted on '{}'.",
                        thingTypeUID, discoveryService.getClass().getName());

                return true;
            } catch (Exception ex) {
                logger.error("Cannot abort scan for thing type '" + thingTypeUID
                        + "' on '" + discoveryService.getClass().getName() + "'!", ex);
            }
        }

        return false;
    }

    @Override
    public void addDiscoveryListener(DiscoveryListener listener) throws IllegalStateException {
        if (listener != null) {
            this.listeners.add(listener);
        }
    }

    @Override
    public boolean startScan(ThingTypeUID thingTypeUID, ScanListener listener) throws IllegalStateException {
        DiscoveryService discoveryService = getDiscoveryService(thingTypeUID);
        if (discoveryService != null) {
            try {
                logger.debug("Triggering scan for thing type '{}' on '{}'...", thingTypeUID,
                        discoveryService.getClass().getSimpleName());

                discoveryService.startScan(listener);
                return true;
            } catch (Exception ex) {
                logger.error("Cannot trigger scan for thing type '" + thingTypeUID
                        + "' on '" + discoveryService.getClass().getSimpleName() + "'!", ex);
            }
        } else {
	        logger.warn("No discovery service for thing type '{}' found!", thingTypeUID);
        }
        return false;
    }

    @Override
    public synchronized void removeDiscoveryListener(DiscoveryListener listener)
            throws IllegalStateException {

        if (listener != null) {
            this.listeners.remove(listener);
        }
    }

    @Override
    public synchronized void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
        for (DiscoveryListener listener : this.listeners) {
            try {
                listener.thingDiscovered(source, result);
            } catch (Exception ex) {
                logger.error("Cannot notify the DiscoveryListener "
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
                logger.error("Cannot notify the DiscoveryListener '"
                        + listener.getClass().getName() + "' on Thing removed event!", ex);
            }
        }
    }

    private synchronized DiscoveryService getDiscoveryService(ThingTypeUID thingTypeUID)
            throws IllegalStateException {

        if (thingTypeUID != null) {
            for (DiscoveryService discoveryService : this.discoveryServices) {
            	Collection<ThingTypeUID> discoveryThingTypes = discoveryService.getSupportedThingTypes();
                if(discoveryThingTypes.contains(thingTypeUID)) {
                    return discoveryService;
                }
            }
        }

        return null;
    }

    protected void addDiscoveryService(DiscoveryService discoveryService) {
        discoveryService.addDiscoveryListener(this);
        this.discoveryServices.add(discoveryService);
    }

    protected void removeDiscoveryService(DiscoveryService discoveryService) {
        this.discoveryServices.remove(discoveryService);
        discoveryService.removeDiscoveryListener(this);
    }

	protected void deactivate() {
	    this.discoveryServices.clear();
	    this.listeners.clear();
	}

}
