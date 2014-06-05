/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.osgi.service.component.ComponentContext;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * TODO: this implementation must be changed. Its PROTOTYPE.
 */
public class ItemChannelBindingRegistry implements ThingRegistryChangeListener {

    private Multimap<String, ChannelUID> itemChannelBindings = Multimaps.synchronizedMultimap(ArrayListMultimap
            .<String, ChannelUID> create());

    private ThingRegistry thingRegistry;

    public void bind(String itemName, ChannelUID channelUID) {
        //String boundItemName = getBoundItem(channel);
        // if (boundItemName != null && !boundItemName.equals(itemName)) {
        // throw new
        // IllegalArgumentException("Channel is already bound to an item.");
        // }
        itemChannelBindings.put(itemName, channelUID);
    }

    public List<ChannelUID> getBoundChannels(String itemName) {
        return Lists.newArrayList(itemChannelBindings.get(itemName));
    }

    public String getBoundItem(ChannelUID channelUID) {
        Collection<Entry<String, ChannelUID>> entries = itemChannelBindings.entries();
        for (Entry<String, ChannelUID> entry : entries) {
            if (entry.getValue().equals(channelUID)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public boolean isBound(String itemName, ChannelUID channelUID) {
        return itemChannelBindings.containsEntry(itemName, channelUID);
    }

	@Override
	public void thingAdded(Thing thing) {
		// nothing to do
	}

	@Override
	public void thingRemoved(Thing thing) {
        List<Channel> channels = thing.getChannels();
        for (Channel channel : channels) {
            unbind(channel.getUID());
        }
	}

    public void unbind(ChannelUID channelUID) {
        String boundItemName = getBoundItem(channelUID);
        if (boundItemName != null) {
            itemChannelBindings.remove(boundItemName, channelUID);
        }
    }

    protected void activate(ComponentContext componentContext) {
        thingRegistry.addThingRegistryChangeListener(this);
    }

    protected void deactivate(ComponentContext componentContext) {
        thingRegistry.removeThingRegistryChangeListener(this);
    }

    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = null;
    }


}
