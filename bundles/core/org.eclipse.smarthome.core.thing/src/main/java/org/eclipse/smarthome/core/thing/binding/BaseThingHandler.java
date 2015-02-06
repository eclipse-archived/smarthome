/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
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
 * The default behavior for {@link Thing} updates is to {@link #dispose()} this handler first, exchange the
 * {@link Thing} and {@link #initialize()} it again. Override the method {@link #thingUpdated(Thing)} to change the
 * default behavior.
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
    @SuppressWarnings("rawtypes")
    private ServiceTracker thingHandlerServiceTracker;

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
        thingRegistryServiceTracker = new ServiceTracker(this.bundleContext, ThingRegistry.class.getName(), null) {
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

        thingHandlerServiceTracker = new ServiceTracker(this.bundleContext, ThingHandler.class.getName(), null) {
            @Override
            public Object addingService(final ServiceReference reference) {
                Object thingId = reference.getProperty(SERVICE_PROPERTY_THING_ID);
                if (thingId instanceof ThingUID && BaseThingHandler.this.thing != null) {
                    ThingUID thingUID = (ThingUID) thingId;
                    if (thingUID.equals(BaseThingHandler.this.thing.getBridgeUID())) {
                        ThingHandler thingHandler = (ThingHandler) bundleContext.getService(reference);
                        Thing thing = thingHandler.getThing();
                        if (thing instanceof Bridge) {
                            bridgeHandlerInitialized(thingHandler, (Bridge) thing);
                            return thingHandler;
                        }
                    }
                }
                return null;
            }

            @Override
            public void removedService(final ServiceReference reference, final Object service) {
                ThingHandler thingHandler = (ThingHandler) service;
                bridgeHandlerDisposed(thingHandler, (Bridge) thingHandler.getThing());
            }
        };
        thingHandlerServiceTracker.open();
    }

    public void unsetBundleContext(final BundleContext bundleContext) {
        thingRegistryServiceTracker.close();
        thingHandlerServiceTracker.close();
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
        ChannelUID channelUID = new ChannelUID(this.getThing().getUID(), channelID);
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
                return (Bridge) thingRegistry.get(bridgeUID);
            } else {
                return null;
            }
        }
    }

    /**
     * Returns a set of linked items for a given channel ID.
     *
     * @param channelId
     *            channel ID (must not be null)
     * @return set of linked items
     * @throws IllegalArgumentException
     *             if no channel with the given ID exists
     */
    protected Set<Item> getLinkedItems(String channelId) {
        Channel channel = thing.getChannel(channelId);
        if (channel != null) {
            return channel.getLinkedItems();
        } else {
            throw new IllegalArgumentException("Channel with ID '" + channelId + "' does not exists.");
        }
    }

    /**
     * Returns whether at least on item is linked for the given channel ID.
     *
     * @param channelId
     *            channel ID (must not be null)
     * @return true if at least one item is linked, false otherwise
     * @throws IllegalArgumentException
     *             if no channel with the given ID exists
     */
    protected boolean isLinked(String channelId) {
        Channel channel = thing.getChannel(channelId);
        if (channel != null) {
            return channel.isLinked();
        } else {
            throw new IllegalArgumentException("Channel with ID '" + channelId + "' does not exists.");
        }
    }

    /**
     * This method is called, when the according {@link ThingHandler} of the
     * bridge was initialized. If the thing of this handler does not have a
     * bridge, this method is never called. This method can be overridden by
     * subclasses.
     *
     * @param thingHandler
     *            thing handler of the bridge
     * @param bridge
     *            bridge
     */
    protected void bridgeHandlerInitialized(ThingHandler thingHandler, Bridge bridge) {
        // can be overridden by subclasses
    }

    /**
     * This method is called, when the according {@link ThingHandler} of the
     * bridge was disposed. If the thing of this handler does not have a
     * bridge, this method is never called. This method can be overridden by
     * subclasses.
     *
     * @param thingHandler
     *            thing handler of the bridge
     * @param bridge
     *            bridge
     */
    protected void bridgeHandlerDisposed(ThingHandler thingHandler, Bridge bridge) {
        // can be overridden by subclasses
    }
}