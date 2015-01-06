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
import java.util.Set;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.items.Item;

/**
 * {@link Channel} is a part of a {@link Thing} that represents a functionality
 * of it. Therefore {@link Item}s can be bound a to a channel. The channel only
 * accepts a specific item type which is specified by
 * {@link Channel#getAcceptedItemType()} methods.
 * 
 * @author Dennis Nobel - Initial contribution and API
 * @author Alex Tugarev - Extended about default tags
 * @author Benedikt Niehues - fix for Bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=445137 considering default values
 */
public class Channel {

    private String acceptedItemType;

    private ChannelUID uid;

    private Configuration configuration;

    private Set<String> defaultTags;    

    public Channel(ChannelUID uid, String acceptedItemType) {
        this.uid = uid;
        this.acceptedItemType = acceptedItemType;
        this.configuration = new Configuration();
    }

    public Channel(ChannelUID uid, String acceptedItemType, Configuration configuration) {
        this(uid, acceptedItemType, configuration, new HashSet<String>(0));
    }

    public Channel(ChannelUID uid, String acceptedItemType, Set<String> defaultTags) {
        this(uid, acceptedItemType, null, defaultTags == null ? new HashSet<String>(0)
                : defaultTags);
    }

    public Channel(ChannelUID uid, String acceptedItemType, Configuration configuration,
            Set<String> defaultTags) {
        this.uid = uid;
        this.acceptedItemType = acceptedItemType;
        this.configuration = configuration;
        this.defaultTags = Collections.<String> unmodifiableSet(new HashSet<String>(defaultTags));
        if (this.configuration==null){
            this.configuration=new Configuration();
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
}
