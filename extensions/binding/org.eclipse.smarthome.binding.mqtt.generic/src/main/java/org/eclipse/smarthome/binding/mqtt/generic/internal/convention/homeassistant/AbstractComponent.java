/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homeassistant;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.internal.ChannelStateUpdateListener;
import org.eclipse.smarthome.binding.mqtt.generic.internal.MqttBindingConstants;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;

/**
 * A HomeAssistant component is comparable to an ESH channel group.
 * It has a name and consists of multiple channels.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractComponent {
    protected final ChannelGroupTypeUID channelGroupTypeUID;
    protected final Map<String, CChannel> channels = new TreeMap<>();

    public AbstractComponent(ThingUID thing, String componentID) {
        this.channelGroupTypeUID = new ChannelGroupTypeUID(MqttBindingConstants.BINDING_ID,
                thing.getId() + "_" + componentID);
    }

    /**
     * Subscribes to all state channels of the component.
     *
     * @param connection The connection
     * @param channelStateUpdateListener A listener
     * @return A future that completes as soon as all subscriptions have been performed
     */
    public CompletableFuture<Boolean> start(MqttBrokerConnection connection,
            ChannelStateUpdateListener channelStateUpdateListener) {
        return channels.values().stream().map(v -> v.channelState.start(connection, channelStateUpdateListener))
                .reduce(CompletableFuture.completedFuture(true), (f, v) -> f.thenCompose(b -> v));
    }

    /**
     * Each HomeAssistant component corresponds to an ESH Channel Group Type.
     */
    public ChannelGroupTypeUID groupTypeUID() {
        return channelGroupTypeUID;
    }

    /**
     * Component (Channel Group) name.
     */
    public abstract String name();

    /**
     * Each component consists of multiple ESH Channels.
     */
    public Map<String, CChannel> channelTypes() {
        return channels;
    }

    /**
     * Return a components channel. A HomeAssistant MQTT component consists of multiple functions
     * and those are mapped to one or more ESH channels. The channel IDs are constants within the
     * derived Component, like the {@link ComponentSwitch#switchChannelID}.
     *
     * @param channelID The channel ID
     * @return A components channel
     */
    public @Nullable CChannel channel(String channelID) {
        return channels.get(channelID);
    }

}
