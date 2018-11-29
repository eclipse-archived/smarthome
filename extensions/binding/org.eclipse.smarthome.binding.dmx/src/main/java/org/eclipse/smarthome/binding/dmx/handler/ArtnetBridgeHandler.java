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
package org.eclipse.smarthome.binding.dmx.handler;

import static org.eclipse.smarthome.binding.dmx.internal.DmxBindingConstants.*;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.binding.dmx.internal.dmxoverethernet.ArtnetNode;
import org.eclipse.smarthome.binding.dmx.internal.dmxoverethernet.ArtnetPacket;
import org.eclipse.smarthome.binding.dmx.internal.dmxoverethernet.DmxOverEthernetHandler;
import org.eclipse.smarthome.binding.dmx.internal.dmxoverethernet.IpNode;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ArtnetBridgeHandler} is responsible for handling the communication
 * with ArtNet devices
 *
 * @author Jan N. Klug - Initial contribution
 */
public class ArtnetBridgeHandler extends DmxOverEthernetHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_ARTNET_BRIDGE);
    public static final int MIN_UNIVERSE_ID = 0;
    public static final int MAX_UNIVERSE_ID = 32767;

    private final Logger logger = LoggerFactory.getLogger(ArtnetBridgeHandler.class);

    public ArtnetBridgeHandler(Bridge artnetBridge) {
        super(artnetBridge);
    }

    @Override
    protected void updateConfiguration() {
        Configuration configuration = getConfig();

        setUniverse(configuration.get(CONFIG_UNIVERSE), MIN_UNIVERSE_ID, MAX_UNIVERSE_ID);
        packetTemplate.setUniverse(universe.getUniverseId());

        receiverNodes.clear();
        if (configuration.get(CONFIG_ADDRESS) == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Could not initialize sender (address not set)");
            uninstallScheduler();
            logger.debug("remote address not set for {}", this.thing.getUID());
            return;
        } else {
            try {
                receiverNodes = IpNode.fromString((String) configuration.get(CONFIG_ADDRESS), ArtnetNode.DEFAULT_PORT);
                logger.debug("using unicast mode to {} for {}", receiverNodes.toString(), this.thing.getUID());
            } catch (IllegalArgumentException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                return;
            }
        }

        if (configuration.get(CONFIG_LOCAL_ADDRESS) != null) {
            senderNode = new IpNode((String) configuration.get(CONFIG_LOCAL_ADDRESS));
        }
        logger.debug("originating address is {} for {}", senderNode, this.thing.getUID());

        if (configuration.get(CONFIG_REFRESH_MODE) != null) {
            refreshAlways = (((String) configuration.get(CONFIG_REFRESH_MODE)).equals("always"));
        }
        logger.debug("refresh mode set to always: {}", refreshAlways);

        updateStatus(ThingStatus.UNKNOWN);
        super.updateConfiguration();

        logger.debug("updated configuration for ArtNet bridge {}", this.thing.getUID());
    }

    @Override
    public void initialize() {
        logger.debug("initializing ArtNet bridge {}", this.thing.getUID());

        packetTemplate = new ArtnetPacket();
        updateConfiguration();
    }
}
