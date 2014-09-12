/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.types.State;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * {@link BaseThingHandler} provides a base implementation for the {@link ThingHandler} interface.
 * <p>
 * The default behavior for {@link Thing} updates is to {@link #dispose()} this handler first,
 * exchange the {@link Thing} and {@link #initialize()} it again. Override the method
 * {@link #thingUpdated(Thing)} to change the default behavior. 
 * 
 * @author Dennis Nobel - Initial contribution
 * @author Michael Grammling - Added dynamic configuration update
 */
public abstract class BaseThingHandler implements ThingHandler {

	protected final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    private Thing thing;
    protected ThingRegistry thingRegistry;
    protected BundleContext bundleContext;
    @SuppressWarnings("rawtypes")
    private ServiceTracker thingRegistryServiceTracker;

    /**
     * Creates a new instance of this class for the {@link Thing}.
     * 
     * @param thing
     *            thing
     */
    public BaseThingHandler(Thing thing) {
        this.thing = thing;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setBundleContext(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        thingRegistryServiceTracker = new ServiceTracker(this.bundleContext,
                ThingRegistry.class.getName(), null) {
            @Override
            public Object addingService(final ServiceReference reference) {
                thingRegistry = (ThingRegistry) bundleContext.getService(reference);
                return thingRegistry;
            }

            @Override
            public void removedService(final ServiceReference reference, final Object service) {
                synchronized (BaseThingHandler.this) {
                    thingRegistry = null;
                }
            }
        };
        thingRegistryServiceTracker.open();
    }

    public void unsetBundleContext(final BundleContext bundleContext) {
        thingRegistryServiceTracker.close();
        this.bundleContext = null;
    }

    @Override
    public void dispose() {
        // can be overridden by subclasses
    }

    @Override
    public Thing getThing() {
        return this.thing;
    }

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        // can be overridden by subclasses
    }

    @Override
    public void initialize() {
        // can be overridden by subclasses
    	// standard behavior is to set the thing to ONLINE,
    	// assuming no further initialization is necessary.
    	this.thing.setStatus(ThingStatus.ONLINE);
    }

    @Override
    public void thingUpdated(Thing thing) {
        dispose();
        this.thing = thing;
        initialize();
    }

    /**
     * Returns the configuration of the thing.
     * 
     * @return configuration of the thing
     */
    protected Configuration getConfig() {
        return getThing().getConfiguration();
    }

    /**
     * Returns the configuration of the thing and transforms it to the given
     * class.
     * 
     * @param configurationClass
     *            configuration class
     * @return configuration of thing in form of the given class
     */
    protected <T> T getConfigAs(Class<T> configurationClass) {
        return getConfig().as(configurationClass);
    }

    /**
     * 
     * Updates the state of the thing.
     * 
     * @param channelUID
     *            unique id of the channel, which was updated
     * @param state
     *            new state
     */
    protected void updateState(ChannelUID channelUID, State state) {
        thing.channelUpdated(channelUID, state);
    }

	/**
	 * 
	 * Updates the state of the thing. Will use the thing UID to infer the
	 * unique channel UID.
	 * 
	 * @param channel
	 *            ID id of the channel, which was updated
	 * @param state
	 *            new state
	 */
	protected void updateState(String channelID, State state) {
		ChannelUID channelUID = new ChannelUID(this.getThing().getUID(),
				channelID);
		updateState(channelUID, state);
	}

    /**
     * Updates the status of the thing.
     * 
     * @param status
     *            new status
     */
    protected void updateStatus(ThingStatus status) {
        if (thing.getStatus() != status) {
            thing.setStatus(status);
        }
    }

    /**
     * Returns the bridge of the thing.
     * 
     * @return returns the bridge of the thing or null if the thing has no
     *         bridge
     */
    protected Bridge getBridge() {
        ThingUID bridgeUID = thing.getBridgeUID();
        synchronized (this) {
            if (bridgeUID != null && thingRegistry != null) {
                return (Bridge) thingRegistry.getByUID(bridgeUID);
            } else {
                return null;
            }
        }
    }

}