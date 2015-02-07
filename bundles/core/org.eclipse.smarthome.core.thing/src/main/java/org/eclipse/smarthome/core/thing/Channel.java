/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.items.Item;

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
 */
public class Channel {

    private String acceptedItemType;

    private ChannelUID uid;

    private Configuration configuration;

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
    }

    public Channel(ChannelUID uid, String acceptedItemType, Configuration configuration) {
        this(uid, acceptedItemType, configuration, new HashSet<String>(0));
    }

    public Channel(ChannelUID uid, String acceptedItemType, Set<String> defaultTags) {
        this(uid, acceptedItemType, null, defaultTags == null ? new HashSet<String>(0) : defaultTags);
    }

    public Channel(ChannelUID uid, String acceptedItemType, Configuration configuration, Set<String> defaultTags) {
        this.uid = uid;
        this.acceptedItemType = acceptedItemType;
        this.configuration = configuration;
        this.defaultTags = Collections.<String> unmodifiableSet(new HashSet<String>(defaultTags));
        if (this.configuration == null) {
            this.configuration = new Configuration();
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
     * Returns the channel configuration
     *
     * @return channel configuration or null
     */
    public Configuration getConfiguration() {
        return configuration;
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
     * @return Set of items, which are linked to the channel
     */
    public Set<Item> getLinkedItems() {
        return ImmutableSet.copyOf(this.linkedItems);
    }

    /**
     * Returns whether at least one item is linked to the channel.
     *
     * @return true if at least one item is linked to the channel, false
     *         otherwise
     */
    public boolean isLinked() {
        return !getLinkedItems().isEmpty();
    }
}
