/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding.builder;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;

public class ChannelBuilder {

    private Channel channel;

    private ChannelBuilder(Channel channel) {
        this.channel = channel;
    }

    public static ChannelBuilder create(ChannelUID channelUID, String acceptedItemType) {
        return new ChannelBuilder(new Channel(channelUID, acceptedItemType));
    }

    public Channel build() {
        return channel;
    }
}
