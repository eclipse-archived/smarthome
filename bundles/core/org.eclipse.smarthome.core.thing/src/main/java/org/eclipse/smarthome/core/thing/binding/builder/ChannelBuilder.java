/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding.builder;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;

/**
 * {@link ChannelBuilder} is responsible for creating {@link Channel}s.
 * 
 * @author Dennis Nobel - Initial contribution
 */
public class ChannelBuilder {

    private ChannelUID channelUID;
    private String acceptedItemType;
    private Configuration configuration;

    private ChannelBuilder(ChannelUID channelUID, String acceptedItemType) {
        this.channelUID = channelUID;
        this.acceptedItemType = acceptedItemType;
    }

    /**
     * Creates a channel builder for the given channel UID and item type.
     * 
     * @param channelUID
     *            channel UID
     * @param acceptedItemType
     *            item type that is accepted by this channel
     * @return channe builder
     */
    public static ChannelBuilder create(ChannelUID channelUID, String acceptedItemType) {
        return new ChannelBuilder(channelUID, acceptedItemType);
    }

    /**
     * Appends a configuration to the channel to build.
     * 
     * @param configuration
     *            configuration
     * @return channel builder
     */
    public ChannelBuilder withConfiguration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Builds and returns the channel.
     * 
     * @return channel
     */
    public Channel build() {
        return new Channel(channelUID, acceptedItemType, configuration);
    }
}
