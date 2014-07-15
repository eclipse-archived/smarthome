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
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.State;

/**
 * {@link BaseThingHandler} provides as base implementation for the
 * {@link ThingHandler} interface.
 * 
 * @author Dennis Nobel - Initial contribution
 */
public abstract class BaseThingHandler implements ThingHandler {

	protected final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    private Thing thing;

    /**
     * Creates a new instance of this class for the {@link Thing}.
     * 
     * @param thing
     *            thing
     */
    public BaseThingHandler(Thing thing) {
        this.thing = thing;
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

}