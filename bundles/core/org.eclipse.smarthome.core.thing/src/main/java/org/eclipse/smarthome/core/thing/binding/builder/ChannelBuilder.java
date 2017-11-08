/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding.builder;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * {@link ChannelBuilder} is responsible for creating {@link Channel}s.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Alex Tugarev - Extended about default tags
 * @author Chris Jackson - Added properties and label/description
 */
@NonNullByDefault
public class ChannelBuilder {

    private ChannelUID channelUID;
    private @Nullable String acceptedItemType;
    private ChannelKind kind;
    private @Nullable Configuration configuration;
    private Set<String> defaultTags;
    private @Nullable Map<String, String> properties;
    private @Nullable String label;
    private @Nullable String description;
    private @Nullable ChannelTypeUID channelTypeUID;

    private ChannelBuilder(ChannelUID channelUID, @Nullable String acceptedItemType, Set<String> defaultTags) {
        this.channelUID = channelUID;
        this.acceptedItemType = acceptedItemType;
        this.defaultTags = defaultTags;
        this.kind = ChannelKind.STATE;
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
    public static ChannelBuilder create(ChannelUID channelUID, @Nullable String acceptedItemType) {
        return new ChannelBuilder(channelUID, acceptedItemType, new HashSet<String>());
    }

    /**
     * Appends the channel type to the channel to build
     *
     * @param channelTypeUID channel type UID
     * @return channel builder
     */
    public ChannelBuilder withType(@Nullable ChannelTypeUID channelTypeUID) {
        this.channelTypeUID = channelTypeUID;
        return this;
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
     * Adds properties to the channel
     *
     * @param properties properties to add
     * @return channel builder
     */
    public ChannelBuilder withProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Sets the channel label. This allows overriding of the default label set in the {@link ChannelType}
     *
     * @param label the channel label to override the label set in the {@link ChannelType}
     * @return channel builder
     */
    public ChannelBuilder withLabel(String label) {
        this.label = label;
        return this;
    }

    /**
     * Sets the channel label. This allows overriding of the default label set in the {@link ChannelType}
     *
     * @param label the channel label to override the label set in the {@link ChannelType}
     * @return channel builder
     */
    public ChannelBuilder withDescription(String description) {
        this.description = description;
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
     * Sets the kind of the channel.
     *
     * @param kind kind.
     * @return channel builder
     */
    public ChannelBuilder withKind(ChannelKind kind) {
        if (kind == null) {
            throw new IllegalArgumentException("kind must not be null");
        }

        this.kind = kind;
        return this;
    }

    /**
     * Builds and returns the channel.
     *
     * @return channel
     */
    public Channel build() {
        return new Channel(channelUID, channelTypeUID, acceptedItemType, kind, configuration, defaultTags, properties,
                label, description);
    }
}
