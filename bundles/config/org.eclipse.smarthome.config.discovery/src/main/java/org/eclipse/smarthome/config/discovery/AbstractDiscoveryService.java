/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractDiscoveryService} provides methods which handle the
 * {@link DiscoveryListener}s.
 * 
 * Subclasses do not have to care about adding and removing those listeners.
 * They can use the protected methods {@link #thingDiscovered(DiscoveryResult)}
 * and {@link #thingRemoved(String)} in order to notify the registered
 * {@link DiscoveryListener}s.
 * 
 * @author Oliver Libutzki - Initial contribution
 * @author Kai Kreuzer - Refactored API
 */
public abstract class AbstractDiscoveryService implements DiscoveryService {

    private final static Logger logger = LoggerFactory.getLogger(AbstractDiscoveryService.class);

	static protected final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    private Set<DiscoveryListener> discoveryListeners = new CopyOnWriteArraySet<>();
    protected ScanListener scanListener = null;
    
    private boolean backgroundDiscoveryEnabled = getBackgroundDiscoveryDefault();

    private Map<ThingUID, DiscoveryResult> cachedResults = new HashMap<>();
    
    final private Set<ThingTypeUID> supportedThingTypes;
    final private int timeout;

	private ScheduledFuture<?> scheduledStop;

    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param supportedThingTypes the list of Thing types which are supported (can be null)
     *
     * @param timeout the discovery timeout in seconds after which the discovery service
     *     automatically stops its forced discovery process (>= 0).
     *
     * @throws IllegalArgumentException if the timeout < 0
     */
    public AbstractDiscoveryService(Set<ThingTypeUID> supportedThingTypes, int timeout)
            throws IllegalArgumentException {

        if (supportedThingTypes == null) {
        	this.supportedThingTypes = Collections.emptySet();
        } else {
            this.supportedThingTypes = supportedThingTypes;
        }

        if (timeout < 0) {
            throw new IllegalArgumentException("The timeout must be >= 0!");
        }

        this.timeout = timeout;
    }

    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param timeout the discovery timeout in seconds after which the discovery service
     *     automatically stops its forced discovery process (>= 0).
     *
     * @throws IllegalArgumentException if the timeout < 0
     */
    public AbstractDiscoveryService(int timeout) throws IllegalArgumentException {
    	this(null, timeout);
    }

    /**
     * Returns the list of {@code Thing} types which are supported by the {@link DiscoveryService}.
     *
     * @return the list of Thing types which are supported by the discovery service
     *     (not null, could be empty)
     */
    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return this.supportedThingTypes;
    }

    /**
     * Returns the amount of time in seconds after which the discovery service automatically
     * stops its forced discovery process.
     *
     * @return the discovery timeout in seconds (>= 0).
     */
    @Override
    public int getScanTimeout() {
        return this.timeout;
    }

    @Override
    public void setBackgroundDiscoveryEnabled(boolean enabled) {
        this.backgroundDiscoveryEnabled = enabled;
    }

    @Override
    public boolean isBackgroundDiscoveryEnabled() {
        return backgroundDiscoveryEnabled;
    }

    @Override
    public void addDiscoveryListener(DiscoveryListener listener) {
    	synchronized (cachedResults) {
        	for(DiscoveryResult cachedResult : cachedResults.values()) {
            	listener.thingDiscovered(this, cachedResult);
            }
		}
        discoveryListeners.add(listener);
    }

    @Override
    public void removeDiscoveryListener(DiscoveryListener listener) {
        discoveryListeners.remove(listener);
    }

    @Override
    public synchronized void startScan(ScanListener listener) {
        synchronized (this) {

            // we first stop any currently running scan and its scheduled stop
            // call
            stopScan();
            if (scheduledStop != null) {
                scheduledStop.cancel(false);
                scheduledStop = null;
            }

            this.scanListener = listener;

            // schedule an automatic call of stopScan when timeout is reached
            if (getScanTimeout() > 0) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            stopScan();
                        } catch (Exception e) {
                            logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                        }
                    }
                };

                scheduledStop = scheduler.schedule(runnable, getScanTimeout(), TimeUnit.SECONDS);
            }

            startScan();
        }
    }
    
    @Override
    public synchronized void abortScan() {
        synchronized (this) {        
            if (scheduledStop != null) {
                scheduledStop.cancel(false);
                scheduledStop = null;
            }
            if (scanListener != null) {
                Exception e = new CancellationException("Scan has been aborted.");
                scanListener.onErrorOccurred(this, e);
                scanListener = null;
            }    	
        }
    }
    
    /**
     * This method is called by the {@link #startScan(ScanListener))} implementation of the {@link AbstractDiscoveryService}.
     * The abstract class schedules a call of {@link #stopScan()} after {@link #getScanTimeout()} 
     * seconds. If this behavior is not appropriate, the {@link #startScan(ScanListener))} method should be overridden.
     */
    abstract protected void startScan();

    /**
     * This method cleans up after a scan, i.e. it removes listeners and other required operations.
     */
    protected synchronized void stopScan() {
    	if(scanListener!=null) {
    		scanListener.onFinished(this);
    		scanListener = null;
    	}
    }
    
	/**
     * Notifies the registered {@link DiscoveryListener}s about a discovered device.
     * 
     * @param discoveryResult
     *            Holds the information needed to identify the discovered device.
     */
    protected void thingDiscovered(DiscoveryResult discoveryResult) {
        for (DiscoveryListener discoveryListener : discoveryListeners) {
            try {
                discoveryListener.thingDiscovered(this, discoveryResult);
            } catch (Exception e) {
                logger.error(
                        "An error occurred while calling the discovery listener "
                                + discoveryListener.getClass().getName() + ".", e);
            }
        }
        synchronized (cachedResults) {
            cachedResults.put(discoveryResult.getThingUID(), discoveryResult);
		}
    }

    /**
     * Defines, whether the default for this discovery service is to enable
     * background discovery or not
     * 
     * @return <code>true</code>, if background discovery is enabled by default, otherwise <code>false</code>
     */
    abstract protected boolean getBackgroundDiscoveryDefault();
    
    /**
     * Notifies the registered {@link DiscoveryListener}s about a removed device.
     * 
     * @param thingUID
     *            The UID of the removed thing.
     */
    protected void thingRemoved(ThingUID thingUID) {
        for (DiscoveryListener discoveryListener : discoveryListeners) {
            try {
                discoveryListener.thingRemoved(this, thingUID);
            } catch (Exception e) {
                logger.error(
                        "An error occurred while calling the discovery listener "
                                + discoveryListener.getClass().getName() + ".", e);
            }
        }
        synchronized (cachedResults) {
        	cachedResults.remove(thingUID);
        }
    }

}
