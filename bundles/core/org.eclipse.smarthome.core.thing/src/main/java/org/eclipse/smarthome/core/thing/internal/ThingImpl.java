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

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.eclipse.smarthome.core.thing.setup.ThingSetupManager;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * The {@link ThingImpl} class is a concrete implementation of the {@link Thing}.
 * <p>
 * This class is mutable.
 *
 * @author Michael Grammling - Configuration could never be null but may be empty
 * @author Benedikt Niehues - Fix ESH Bug 450236
 *         https://bugs.eclipse.org/bugs/show_bug.cgi?id=450236 - Considering
 *         ThingType Description
 * @author Thomas Höfer - Added thing and thing type properties
 *
 */
public class ThingImpl implements Thing {

    private ThingUID bridgeUID;

    private List<Channel> channels;

    private Configuration configuration = new Configuration();

    private Map<String, String> properties = new HashMap<>();

    private ThingUID uid;

    private ThingTypeUID thingTypeUID;

    transient volatile private ThingStatusInfo status = ThingStatusInfoBuilder.create(ThingStatus.UNINITIALIZED,
            ThingStatusDetail.NONE).build();

    transient volatile private ThingHandler thingHandler;

    transient volatile private GroupItem linkedItem;

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
        return status.getStatus();
    }

    @Override
    public ThingStatusInfo getStatusInfo() {
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
    public void setStatusInfo(ThingStatusInfo status) {
        this.status = status;
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
    public Map<String, String> getProperties() {
        synchronized (this) {
            return ImmutableMap.copyOf(properties);
        }
    }

    @Override
    public String setProperty(String name, String value) {
        if (Strings.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Property name must not be null or empty");
        }
        synchronized (this) {
            if (value == null) {
                return properties.remove(name);
            }
            return properties.put(name, value);
        }
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

}
