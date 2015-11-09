/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding;

import java.util.Map;

import org.eclipse.smarthome.config.core.validation.ConfigValidationException;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

/**
 * A {@link ThingHandler} can 'handle' a {@link Thing}. It must be registered as
 * OSGi service with the id of the corresponding {@link Thing} as service
 * property 'thing.id' and its type with the property 'thing.type'. When a {@link Command} is sent to an {@link Item}
 * and the item is bound to a
 * channel, the handler of the corresponding thing will receive the command via
 * the {@link ThingHandler#handleCommand(Channel, Command)} method.
 *
 * @author Dennis Nobel - Initial contribution and API
 * @author Michael Grammling - Added dynamic configuration update
 * @auther Thomas Höfer - Added config description validation exception to handleConfigurationUpdate operation
 */
public interface ThingHandler {

    public static final String SERVICE_PROPERTY_THING_ID = "thing.id";
    public static final String SERVICE_PROPERTY_THING_TYPE = "thing.type";

    /**
     * Returns the {@link Thing}, which belongs to the handler.
     *
     * @return {@link Thing}, which belongs to the handler
     */
    Thing getThing();

    /**
     * Handles a command for a given channel.
     *
     * @param channelUID
     *            unique identifier of the channel to which the command was sent
     * @param command
     *            {@link Command}
     */
    void handleCommand(ChannelUID channelUID, Command command);

    /**
     * Handles a command for a given channel.
     *
     * @param channelUID
     *            unique identifier of the channel on which the update was performed
     * @param newState
     *            new state
     */
    void handleUpdate(ChannelUID channelUID, State newState);

    /**
     * Notifies the handler about an updated {@link Thing}.
     *
     * @param thing the {@link Thing}, that has been updated
     */
    void thingUpdated(Thing thing);

    /**
     * This method is called before a thing will be removed.
     * An implementing class can handle the removal in order to trigger some tidying work for a thing.
     * <p>
     * The framework expects this method to be non-blocking and return quickly.
     * For longer running tasks, the implementation has to take care of scheduling a separate job.
     * <p>
     * The {@link Thing} is in {@link ThingStatus#REMOVING} when this method is called.
     * Implementations of this method must signal to the framework that the handling has been
     * completed by setting the {@link Thing}s state to {@link ThingStatus#REMOVED}.
     * Only then it will be removed completely.
     */
    void handleRemoval();

    /**
     * This method is called when the configuration of a thing should be updated.
     * An implementing class needs to persist the configuration changes if necessary.
     *
     * @param configurationParameters map of changed configuration parameters
     *
     * @throws ConfigValidationException if one or more of the given configuration parameters do not match
     *             their declarations in the configuration description
     */
    void handleConfigurationUpdate(Map<String, Object> configurationParameters) throws ConfigValidationException;

    /**
     * This method is called, before the handler is shut down.
     * An implementing class can clean resources here.
     */
    void dispose();

    /**
     * This method is called, when the handler is started.
     */
    void initialize();

    /**
     * Sets the {@link ThingHandlerCallback} of the handler, which must be used for informing the framework about
     * changes. After the handler was disposed, this method is called again with null as argument, to remove the
     * callback.
     *
     * @param thingHandlerCallback
     *            callback (can be null)
     */
    void setCallback(ThingHandlerCallback thingHandlerCallback);

    /**
     * Notifies the handler that a channel was linked.
     *
     * @param channelUID UID of the linked channel
     */
    void channelLinked(ChannelUID channelUID);

    /**
     * Notifies the handler that a channel was unlinked.
     *
     * @param channelUID UID of the unlinked channel
     */
    void channelUnlinked(ChannelUID channelUID);

}
