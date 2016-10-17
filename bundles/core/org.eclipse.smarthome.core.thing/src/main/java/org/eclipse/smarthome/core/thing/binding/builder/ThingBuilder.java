/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding.builder;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.internal.ThingImpl;
import org.eclipse.smarthome.core.thing.util.ThingHelper;

import com.google.common.collect.Lists;

/**
 * This class allows the easy construction of a {@link Thing} instance using the builder pattern.
 *
 * @author Dennis Nobel - Initial contribution and API
 * @author Kai Kreuzer - Refactoring to make BridgeBuilder a subclass
 *
 */
public class ThingBuilder {

    private ThingImpl thing;

    protected ThingBuilder(ThingImpl thing) {
        this.thing = thing;
    }

    public static ThingBuilder create(ThingTypeUID thingTypeUID, String thingId) {
        ThingImpl thing = new ThingImpl(thingTypeUID, thingId);
        return new ThingBuilder(thing);
    }

    @Deprecated
    public static ThingBuilder create(ThingUID thingUID) {
        ThingImpl thing = new ThingImpl(thingUID);
        return new ThingBuilder(thing);
    }

    public static ThingBuilder create(ThingTypeUID thingTypeUID, ThingUID thingUID) {
        ThingImpl thing = new ThingImpl(thingTypeUID, thingUID);
        return new ThingBuilder(thing);
    }

    public ThingBuilder withLabel(String label) {
        this.thing.setLabel(label);
        return this;
    }

    public ThingBuilder withChannel(Channel channel) {
        final Collection<Channel> mutableThingChannels = this.thing.getChannelsMutable();
        ThingHelper.ensureUniqueChannels(mutableThingChannels, channel);
        mutableThingChannels.add(channel);
        return this;
    }

    public ThingBuilder withChannels(Channel... channels) {
        ThingHelper.ensureUniqueChannels(channels);
        this.thing.setChannels(Lists.newArrayList(channels));
        return this;
    }

    public ThingBuilder withChannels(List<Channel> channels) {
        ThingHelper.ensureUniqueChannels(channels);
        this.thing.setChannels(Lists.newArrayList(channels));
        return this;
    }

    public ThingBuilder withoutChannel(ChannelUID channelUID) {
        Iterator<Channel> iterator = this.thing.getChannelsMutable().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getUID().equals(channelUID)) {
                iterator.remove();
            }
        }
        return this;
    }

    public ThingBuilder withConfiguration(Configuration thingConfiguration) {
        this.thing.setConfiguration(thingConfiguration);
        return this;
    }

    public ThingBuilder withBridge(ThingUID bridgeUID) {
        if (bridgeUID != null) {
            this.thing.setBridgeUID(bridgeUID);
        }
        return this;
    }

    public ThingBuilder withProperties(Map<String, String> properties) {
        if (properties != null) {
            for (String key : properties.keySet()) {
                this.thing.setProperty(key, properties.get(key));
            }
        }
        return this;
    }

    public ThingBuilder withLocation(String location) {
        this.thing.setLocation(location);
        return this;
    }

    public Thing build() {
        return this.thing;
    }

}
