/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding.builder;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;

/**
 * {@link ChannelBuilder} is responsible for creating {@link Channel}s.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Alex Tugarev - Extended about default tags
 */
public class ChannelBuilder {

    private ChannelUID channelUID;
    private String acceptedItemType;
    private Configuration configuration;
    private Set<String> defaultTags;

    private ChannelBuilder(ChannelUID channelUID, String acceptedItemType, Set<String> defaultTags) {
        this.channelUID = channelUID;
        this.acceptedItemType = acceptedItemType;
        this.defaultTags = defaultTags;
    }

    /**
     * Creates a channel builder for the given channel UID and item type.
     *
     * @param channelUID
     *            channel UID
     * @param acceptedItemType
     *            item type that is accepted by this channel
     * @return channel builder
     */
    public static ChannelBuilder create(ChannelUID channelUID, String acceptedItemType) {
        return new ChannelBuilder(channelUID, acceptedItemType, new HashSet<String>());
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
     * Appends default tags to the channel to build.
     *
     * @param defaultTags
     *            default tags
     * @return channel builder
     */
    public ChannelBuilder withDefaultTags(Set<String> defaultTags) {
        this.defaultTags = defaultTags;
        return this;
    }

    /**
     * Builds and returns the channel.
     *
     * @return channel
     */
    public Channel build() {
        return new Channel(channelUID, acceptedItemType, configuration, defaultTags);
    }
}
