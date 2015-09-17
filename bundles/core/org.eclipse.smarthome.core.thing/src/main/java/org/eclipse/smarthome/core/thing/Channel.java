/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;

import com.google.common.collect.ImmutableSet;

/**
 * {@link Channel} is a part of a {@link Thing} that represents a functionality
 * of it. Therefore {@link Item}s can be linked a to a channel. The channel only
 * accepts a specific item type which is specified by {@link Channel#getAcceptedItemType()} methods.
 *
 * @author Dennis Nobel - Initial contribution and API
 * @author Alex Tugarev - Extended about default tags
 * @author Benedikt Niehues - fix for Bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=445137 considering default
 *         values
 * @author Chris Jackson - Added properties, label, description
 */
public class Channel {

    private String acceptedItemType;

    private ChannelUID uid;

    private String label;

    private String description;

    private Configuration configuration;

    private Map<String, String> properties;

    private Set<String> defaultTags;

    transient private Set<Item> linkedItems = new LinkedHashSet<>();

    /**
     * Package protected default constructor to allow reflective instantiation.
     */
    Channel() {
    }

    public Channel(ChannelUID uid, String acceptedItemType) {
        this.uid = uid;
        this.acceptedItemType = acceptedItemType;
        this.configuration = new Configuration();
        this.properties = Collections.unmodifiableMap(new HashMap<String, String>(0));
    }

    public Channel(ChannelUID uid, String acceptedItemType, Configuration configuration) {
        this(uid, acceptedItemType, configuration, new HashSet<String>(0), null, null, null);
    }

    public Channel(ChannelUID uid, String acceptedItemType, Set<String> defaultTags) {
        this(uid, acceptedItemType, null, defaultTags == null ? new HashSet<String>(0) : defaultTags, null, null, null);
    }

    public Channel(ChannelUID uid, String acceptedItemType, Configuration configuration, Set<String> defaultTags,
            Map<String, String> properties) {
        this(uid, acceptedItemType, null, defaultTags == null ? new HashSet<String>(0) : defaultTags, properties, null,
                null);
    }

    public Channel(ChannelUID uid, String acceptedItemType, Configuration configuration, Set<String> defaultTags,
            Map<String, String> properties, String label, String description) {
        this.uid = uid;
        this.acceptedItemType = acceptedItemType;
        this.configuration = configuration;
        this.label = label;
        this.description = description;
        this.properties = properties;
        this.defaultTags = Collections.<String> unmodifiableSet(new HashSet<String>(defaultTags));
        if (this.configuration == null) {
            this.configuration = new Configuration();
        }
        if (this.properties == null) {
            this.properties = Collections.unmodifiableMap(new HashMap<String, String>(0));
        }
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

    /**
     * Returns the label (if set).
     * If no label is set, getLabel will return null and the default label for the {@link Channel} is used.
     *
     * @return the label for the channel. Can be null.
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Returns the description (if set).
     * If no description is set, getDescription will return null and the default description for the {@link Channel} is
     * used.
     *
     * @return the description for the channel. Can be null.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns the channel configuration
     *
     * @return channel configuration (not null)
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Returns the channel properties
     *
     * @return channel properties (not null)
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Returns default tags of this channel.
     *
     * @return default tags of this channel.
     */
    public Set<String> getDefaultTags() {
        return defaultTags;
    }

    /**
     * Adds an linked item to the list of linked items (this is an internal method
     * that must not be called by clients).
     *
     * @param item item (must not be null)
     */
    public void addLinkedItem(Item item) {
        this.linkedItems.add(item);
    }

    /**
     * Removes an linked item from the list of linked items (this is an internal method
     * that must not be called by clients).
     *
     * @param item item (must not be null)
     */
    public void removeLinkedItem(Item item) {
        this.linkedItems.remove(item);
    }

    /**
     * Returns a set of items, which are linked to the channel.
     *
     * @deprecated Will be removed soon, because it is dynamic data which does not belong to the thing. Use
     *             {@link ItemChannelLinkRegistry} instead.
     *
     * @return Set of items, which are linked to the channel
     */
    @Deprecated
    public Set<Item> getLinkedItems() {
        return ImmutableSet.copyOf(this.linkedItems);
    }

    /**
     * Returns whether at least one item is linked to the channel.
     *
     * @deprecated Will be removed soon, because it is dynamic data which does not belong to the thing. Use
     *             {@link ItemChannelLinkRegistry} instead.
     * 
     * @return true if at least one item is linked to the channel, false
     *         otherwise
     */
    @Deprecated
    public boolean isLinked() {
        return !getLinkedItems().isEmpty();
    }
}
