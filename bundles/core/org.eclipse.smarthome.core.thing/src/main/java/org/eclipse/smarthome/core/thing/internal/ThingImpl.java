/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingListener;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.State;

public class ThingImpl implements Thing {

	volatile private BridgeImpl bridge;

    private List<Channel> channels;

    private Configuration configuration;

    private ThingUID uid;

    volatile private ThingStatus status;

    volatile private ThingHandler thingHandler;

    volatile private List<ThingListener> thingListeners = new CopyOnWriteArrayList<>();
    
    private String name;

    private ThingTypeUID thingTypeUID;

    /**
     * @param thingTypeUID
     * @param thingId
     *            - TODO: change to {@link ThingUID}
     * @throws IllegalArgumentException
     */
    public ThingImpl(ThingTypeUID thingTypeUID, String thingId) throws IllegalArgumentException {
        this.uid = new ThingUID(thingTypeUID.getBindingId(), thingTypeUID.getId(), thingId);
        this.thingTypeUID = thingTypeUID;
    }

    @Override
    public void addThingListener(ThingListener thingListener) {
        this.thingListeners.add(thingListener);
    }

    @Override
    public void channelUpdated(ChannelUID channelUID, State state) {
        for (ThingListener thingListener : thingListeners) {
            thingListener.channelUpdated(channelUID, state);
        }
    }

    @Override
    public Bridge getBridge() {
        return this.bridge;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    @Override
    public Configuration getConfiguration() {
        return this.configuration;
    }

    @Override
    public ThingHandler getHandler() {
        return this.thingHandler;
    }

    public ThingUID getUID() {
        return uid;
    }


    public ThingStatus getStatus() {
        return status;
    }
    
    @Override
    public String getName() {
		return name;
	}

    @Override
    public void removeThingListener(ThingListener thingListener) {
        this.thingListeners.remove(thingListener);
    }

    @Override
    public void setBridge(Bridge bridge) {
        this.bridge = (BridgeImpl) bridge;
        if (bridge != null) {
            this.bridge.addThing(this);
        } else {
            this.bridge.removeThing(this);
        }
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    public void setConfiguration(Configuration configuation) {
        this.configuration = configuation;
    }


    @Override
    public void setHandler(ThingHandler thingHandler) {
        this.thingHandler = thingHandler;
    }

    public void setId(ThingUID id) {
        this.uid = id;
    }

    public void setStatus(ThingStatus status) {
        this.status = status;
    }
    
    @Override
    public void setName(String name) {
    	this.name = name;
    }

    @Override
    public ThingTypeUID getThingTypeUID() {
        return this.thingTypeUID;
    }

}
