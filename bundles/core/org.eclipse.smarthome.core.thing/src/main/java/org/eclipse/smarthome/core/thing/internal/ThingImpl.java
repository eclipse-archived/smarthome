/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ItemLinkChangeListener;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingChangeListener;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

import com.google.common.collect.ImmutableList;

/**
 * The {@link ThingImpl} class is a concrete implementation of the {@link Thing}.
 * <p>
 * This class is mutable.
 *
 * @author Michael Grammling - Configuration could never be null but may be empty
 * @author Benedikt Niehues - Fix ESH Bug 450236
 *         https://bugs.eclipse.org/bugs/show_bug.cgi?id=450236 - Considering
 *         ThingType Description
 *
 */
public class ThingImpl implements Thing {

    private final class ItemLinkChangeListenerImplementation implements ItemLinkChangeListener {
        
        private Channel channel;

        public ItemLinkChangeListenerImplementation(Channel channel) {
            this.channel = channel;
        }
        
        @Override
        public void itemUnlinked(Item item) {
            for (ThingChangeListener listener : listeners) {
                listener.itemUnlinked(channel, item);
            }
        }

        @Override
        public void itemLinked(Item item) {
            for (ThingChangeListener listener : listeners) {
                listener.itemLinked(channel, item);
            }
        }
    }

    private ThingUID bridgeUID;

    private List<Channel> channels;

    private Configuration configuration = new Configuration();

    private ThingUID uid;

    private ThingTypeUID thingTypeUID;

    transient volatile private ThingStatus status = ThingStatus.OFFLINE;

    transient volatile private ThingHandler thingHandler;

    transient volatile private GroupItem linkedItem;

    transient volatile private List<ThingChangeListener> listeners = new CopyOnWriteArrayList<>();
    
    transient volatile private Map<ChannelUID, ItemLinkChangeListener> itemLinkChangeListeners = new HashMap<ChannelUID, ItemLinkChangeListener>();

    /**
     * Package protected default constructor to allow reflective instantiation.
     */
    ThingImpl() {
    }

    /**
     * @param thingTypeUID
     * @param thingId
     *            - TODO: change to {@link ThingUID}
     * @throws IllegalArgumentException
     */
    public ThingImpl(ThingTypeUID thingTypeUID, String thingId) throws IllegalArgumentException {
        this.uid = new ThingUID(thingTypeUID.getBindingId(), thingTypeUID.getId(), thingId);
        this.thingTypeUID = thingTypeUID;
        this.channels = new ArrayList<>(0);
    }

    /**
     * @param thingUID
     * @throws IllegalArgumentException
     */
    public ThingImpl(ThingUID thingUID) throws IllegalArgumentException {
        this.uid = thingUID;
        this.thingTypeUID = new ThingTypeUID(thingUID.getBindingId(), thingUID.getThingTypeId());
        this.channels = new ArrayList<>(0);
    }

    @Override
    public ThingUID getBridgeUID() {
        return this.bridgeUID;
    }

    @Override
    public List<Channel> getChannels() {
        return ImmutableList.copyOf(this.channels);
    }

    @Override
    public Channel getChannel(String channelId) {
        for (Channel channel : this.channels) {
            if (channel.getUID().getId().equals(channelId)) {
                return channel;
            }
        }
        return null;
    }

    public List<Channel> getChannelsMutable() {
        return this.channels;
    }

    @Override
    public Configuration getConfiguration() {
        return this.configuration;
    }

    @Override
    public ThingHandler getHandler() {
        return this.thingHandler;
    }

    @Override
    public ThingUID getUID() {
        return uid;
    }

    @Override
    public ThingStatus getStatus() {
        return status;
    }

    @Override
    public void setBridgeUID(ThingUID bridgeUID) {
        this.bridgeUID = bridgeUID;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = (configuration == null) ? new Configuration() : configuration;
    }

    @Override
    public void setHandler(ThingHandler thingHandler) {
        this.thingHandler = thingHandler;
    }

    public void setId(ThingUID id) {
        this.uid = id;
    }

    @Override
    public void setStatus(ThingStatus status) {
        this.status = status;
        for (ThingChangeListener listener : this.listeners) {
            listener.statusUpdated(status);
        }
    }

    @Override
    public ThingTypeUID getThingTypeUID() {
        return this.thingTypeUID;
    }

    public void setLinkedItem(GroupItem groupItem) {
        this.linkedItem = groupItem;
    }

    @Override
    public GroupItem getLinkedItem() {
        return this.linkedItem;
    }

    @Override
    public boolean isLinked() {
        return getLinkedItem() != null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uid == null) ? 0 : uid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ThingImpl other = (ThingImpl) obj;
        if (uid == null) {
            if (other.uid != null)
                return false;
        } else if (!uid.equals(other.uid))
            return false;
        return true;
    }

    @Override
    public void addChangeListener(ThingChangeListener thingChangeListener) {
        this.listeners.add(thingChangeListener);
        for (Channel channel : channels) {
            ItemLinkChangeListenerImplementation itemLinkChangeListener = new ItemLinkChangeListenerImplementation(channel);
            itemLinkChangeListeners.put(channel.getUID(), itemLinkChangeListener);
            channel.addItemLinkChangeListener(itemLinkChangeListener);
        }
    }

    @Override
    public void removeChangeListener(ThingChangeListener thingChangeListener) {
        this.listeners.remove(thingChangeListener);
        for (Channel channel : channels) {
            ItemLinkChangeListener itemLinkChangeListener = itemLinkChangeListeners.get(channel.getUID());
            if(itemLinkChangeListener != null) {
                channel.removeItemLinkChangeListener(itemLinkChangeListener);
            }
        }
    }

}
