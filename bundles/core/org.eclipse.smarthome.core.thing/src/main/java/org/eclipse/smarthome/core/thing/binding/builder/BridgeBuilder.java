/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.internal.BridgeImpl;

/**
 * This class allows the easy construction of a {@link Bridge} instance using the builder pattern.
 *
 * @author Dennis Nobel - Initial contribution and API
 * @author Kai Kreuzer - Refactoring to make BridgeBuilder a subclass of ThingBuilder
 * @author Markus Rathgeb - Override methods to return BridgeBuidler instead of ThingBuidler
 *
 */
public class BridgeBuilder extends ThingBuilder {

    private BridgeBuilder(@NonNull BridgeImpl thing) {
        super(thing);
    }

    public static BridgeBuilder create(ThingTypeUID thingTypeUID, String bridgeId) {
        BridgeImpl bridge = new BridgeImpl(thingTypeUID, bridgeId);
        bridge.setChannels(new ArrayList<Channel>());
        return new BridgeBuilder(bridge);
    }

    @Deprecated
    public static BridgeBuilder create(ThingUID thingUID) {
        BridgeImpl bridge = new BridgeImpl(thingUID);
        return new BridgeBuilder(bridge);
    }

    public static BridgeBuilder create(ThingTypeUID thingTypeUID, ThingUID thingUID) {
        BridgeImpl bridge = new BridgeImpl(thingTypeUID, thingUID);
        return new BridgeBuilder(bridge);
    }

    @Override
    public Bridge build() {
        return (Bridge) super.build();
    }

    @Override
    public BridgeBuilder withLabel(String label) {
        return (BridgeBuilder) super.withLabel(label);
    }

    @Override
    public BridgeBuilder withChannel(Channel channel) {
        return (BridgeBuilder) super.withChannel(channel);
    }

    @Override
    public BridgeBuilder withChannels(Channel... channels) {
        return (BridgeBuilder) super.withChannels(channels);
    }

    @Override
    public BridgeBuilder withChannels(List<Channel> channels) {
        return (BridgeBuilder) super.withChannels(channels);
    }

    @Override
    public BridgeBuilder withoutChannel(ChannelUID channelUID) {
        return (BridgeBuilder) super.withoutChannel(channelUID);
    }

    @Override
    public BridgeBuilder withConfiguration(Configuration thingConfiguration) {
        return (BridgeBuilder) super.withConfiguration(thingConfiguration);
    }

    @Override
    public BridgeBuilder withBridge(ThingUID bridgeUID) {
        return (BridgeBuilder) super.withBridge(bridgeUID);
    }

    @Override
    public BridgeBuilder withProperties(Map<@NonNull String, String> properties) {
        return (BridgeBuilder) super.withProperties(properties);
    }

    @Override
    public BridgeBuilder withLocation(String location) {
        return (BridgeBuilder) super.withLocation(location);
    }

}
