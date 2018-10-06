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
package org.eclipse.smarthome.binding.mqtt.generic.internal.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.mqtt.generic.internal.ChannelState;
import org.eclipse.smarthome.binding.mqtt.generic.internal.MqttChannelTypeProvider;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homeassistant.AbstractComponent;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homeassistant.CChannel;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homeassistant.DiscoverComponents;
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homeassistant.HandlerConfiguration;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles HomeAssistant MQTT component things.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HomeAssistantThingHandler extends AbstractMQTTThingHandler {
    private final Logger logger = LoggerFactory.getLogger(HomeAssistantThingHandler.class);
    // The timeout per attribute field subscription
    public final int attributeReceiveTimeout;
    protected HandlerConfiguration config = new HandlerConfiguration();
    protected final Map<String, AbstractComponent> haComponents = new HashMap<String, AbstractComponent>();
    protected final DiscoverComponents discoverComponents = new DiscoverComponents(thing.getUID(), scheduler);

    /**
     * Create a new thing handler for HomeAssistant MQTT components.
     * A channel type provider and a topic value receive timeout must be provided.
     *
     * @param thing The thing of this handler
     * @param provider A channel type provider
     * @param homieReceiveTimeout Timeout for the entire tree parsing and subscription. In milliseconds.
     * @param attributeReceiveTimeout The timeout per attribute field subscription. In milliseconds.
     */
    public HomeAssistantThingHandler(Thing thing, MqttChannelTypeProvider provider, int subscribeTimeout,
            int attributeReceiveTimeout) {
        super(thing, provider, null, subscribeTimeout);
        this.attributeReceiveTimeout = attributeReceiveTimeout;
    }

    @Override
    public void initialize() {
        config = thing.getConfiguration().as(HandlerConfiguration.class);
        if (config.objectid.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Object ID unknown");
            return;
        }
        discoverComponents.setDiscoveryTopic(config.basetopic, config.objectid, config.nodeid);
        super.initialize();
    }

    @Override
    protected CompletableFuture<Void> start(MqttBrokerConnection connection) {
        connection.setRetain(true);
        connection.setQos(1);
        return discoverComponents.startDiscovery(connection, 500).thenCompose(b -> {
            updateThing(editThing()
                    .withChannels(discoverComponents.applyDiscoveryResult(haComponents, channelTypeProvider)).build());

            return DiscoverComponents.startComponents(haComponents, connection, this);
        }).thenRun(() -> {
            logger.trace("HomeAssistant MQTT Thing {} Handler ready", thing.getUID());
            updateStatus(ThingStatus.ONLINE);
        }).exceptionally(e -> {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
            return null;
        });
    }

    @SuppressWarnings({ "null", "unused" })
    @Override
    protected @Nullable ChannelState getChannelState(ChannelUID channelUID) {
        String componentID = channelUID.getGroupId();
        if (componentID == null) {
            return null;
        }
        AbstractComponent component = haComponents.get(componentID);
        if (component == null) {
            return null;
        }
        CChannel componentChannel = component.channel(channelUID.getIdWithoutGroup());
        if (componentChannel == null) {
            return null;
        }
        return componentChannel.channelState;
    }
}
