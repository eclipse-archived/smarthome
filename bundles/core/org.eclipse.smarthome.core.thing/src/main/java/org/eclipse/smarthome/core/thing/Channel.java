/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

import org.eclipse.smarthome.core.items.Item;

/**
 * {@link Channel} is a part of a {@link Thing} that represents a functionality
 * of it. Therefore {@link Item}s can be bound a to a channel. The channel only
 * accepts a specific item type which is specified by
 * {@link Channel#getAcceptedItemType()} methods.
 * 
 * @author Dennis Nobel - Initial contribution and API
 */
public class Channel {

    private String acceptedItemType;

    private ChannelUID uid;

    public Channel(ChannelUID uid, String acceptedItemType) {
        this.uid = uid;
        this.acceptedItemType = acceptedItemType;
    }

    /**
     * Returns the accepted item type.
     * 
     * @return accepted item type
     */
    public String getAcceptedItemType() {
        return this.acceptedItemType;
    }

    /**
     * Returns the unique id of the channel.
     * 
     * @return unique id of the channel
     */
    public ChannelUID getUID() {
        return this.uid;
    }
}
