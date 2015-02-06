/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

import java.util.List;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.internal.ThingListener;
import org.eclipse.smarthome.core.types.State;

/**
 * A {@link Thing} is a representation of a connected part (e.g. physical device
 * or cloud service) from the real world. It contains a list of {@link Channel} s, which can be bound to {@link Item}s.
 * A {@link Thing} might be connected
 * through a {@link Bridge}.
 *
 * @author Dennis Nobel - Initial contribution and API
 */
public interface Thing {

    /**
     * Gets the channels.
     *
     * @return the channels
     */
    List<Channel> getChannels();

    /**
     * Gets the channel for the given id or null if no channel with the id
     * exists.
     *
     * @param channelId
     *            channel ID
     *
     * @return the channel for the given id or null if no channel with the id
     *         exists
     */
    Channel getChannel(String channelId);

    /**
     * Gets the status.
     *
     * @return the status
     */
    ThingStatus getStatus();

    /**
     * Sets the status.
     *
     * @param status
     *            the new status
     */
    void setStatus(ThingStatus status);

    /**
     * Sets the handler.
     *
     * @param thingHandler
     *            the new handler
     */
    void setHandler(ThingHandler thingHandler);

    /**
     * Gets the handler.
     *
     * @return the handler (can be null)
     */
    ThingHandler getHandler();

    /**
     * Gets the bridge UID.
     *
     * @return the bridge UID (can be null)
     */
    ThingUID getBridgeUID();

    /**
     * Sets the bridge.
     *
     * @param bridge
     *            the new bridge
     */
    void setBridgeUID(ThingUID bridgeUID);

    /**
     * This method must be called when the state of channel was changed. All {@link ThingListener}s will be informed
     * about the changed state.
     *
     * @param channelUID
     *            the unique channel id
     * @param state
     *            the state
     */
    void channelUpdated(ChannelUID channelUID, State state);

    /**
     * Gets the configuration.
     *
     * @return the configuration (not null)
     */
    Configuration getConfiguration();

    /**
     * Gets the uid.
     *
     * @return the uid
     */
    ThingUID getUID();

    /**
     * Gets the thing type UID.
     *
     * @return the thing type UID
     */
    ThingTypeUID getThingTypeUID();

    /**
     * Returns the group item, which is linked to the thing or null if no item is
     * linked.
     *
     * @return group item , which is linked to the thing or null
     */
    GroupItem getLinkedItem();

    /**
     * Returns whether the thing is linked to an item.
     *
     * @return true if thing is linked, false otherwise.
     */
    public boolean isLinked();
}
