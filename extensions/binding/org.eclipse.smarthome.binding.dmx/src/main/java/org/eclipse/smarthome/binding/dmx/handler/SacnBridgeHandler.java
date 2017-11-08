/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.dmx.handler;

import static org.eclipse.smarthome.binding.dmx.DmxBindingConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import org.eclipse.smarthome.binding.dmx.internal.dmxoverethernet.DmxOverEthernetHandler;
import org.eclipse.smarthome.binding.dmx.internal.dmxoverethernet.IpNode;
import org.eclipse.smarthome.binding.dmx.internal.dmxoverethernet.SacnNode;
import org.eclipse.smarthome.binding.dmx.internal.dmxoverethernet.SacnPacket;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SacnBridgeHandler} is responsible for handling the communication
 * with sACN/E1.31 devices
 *
 * @author Jan N. Klug - Initial contribution
 */
public class SacnBridgeHandler extends DmxOverEthernetHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_SACN_BRIDGE);
    public static final int MIN_UNIVERSE_ID = 1;
    public static final int MAX_UNIVERSE_ID = 63999;

    private final Logger logger = LoggerFactory.getLogger(SacnBridgeHandler.class);
    private final UUID senderUUID;

    public SacnBridgeHandler(Bridge sacnBridge) {
        super(sacnBridge);
        senderUUID = UUID.randomUUID();
    }

    @Override
    protected void updateConfiguration() {
        Configuration configuration = getConfig();

        setUniverse(configuration.get(CONFIG_UNIVERSE), MIN_UNIVERSE_ID, MAX_UNIVERSE_ID);
        packetTemplate.setUniverse(universe.getUniverseId());

        receiverNodes.clear();
        if ((configuration.get(CONFIG_SACN_MODE).equals("unicast"))) {
            if (configuration.get(CONFIG_ADDRESS) == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Could not initialize unicast sender (address not set)");
                return;
            } else {
                try {
                    receiverNodes = IpNode.fromString((String) configuration.get(CONFIG_ADDRESS),
                            SacnNode.DEFAULT_PORT);
                    logger.debug("using unicast mode to {} for {}", receiverNodes.toString(), this.thing.getUID());
                } catch (IllegalArgumentException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                    return;
                }
            }
        } else {
            receiverNodes = new ArrayList<IpNode>();
            receiverNodes.add(SacnNode.getBroadcastNode(universe.getUniverseId()));
            logger.debug("using multicast mode to {} for {}", receiverNodes, this.thing.getUID());
        }

        if (configuration.get(CONFIG_LOCAL_ADDRESS) != null) {
            senderNode = new IpNode((String) configuration.get(CONFIG_LOCAL_ADDRESS));
        }
        logger.debug("originating address is {} for {}", senderNode, this.thing.getUID());

        if (configuration.get(CONFIG_REFRESH_MODE) != null) {
            refreshAlways = (((String) configuration.get(CONFIG_REFRESH_MODE)).equals("always"));
        }
        logger.debug("refresh mode set to always: {}", refreshAlways);

        super.updateConfiguration();
        updateStatus(ThingStatus.UNKNOWN);

        logger.debug("updated configuration for sACN/E1.31 bridge {}", this.thing.getUID());
    }

    @Override
    public void initialize() {
        logger.debug("initializing sACN/E1.31 bridge {}", this.thing.getUID());

        packetTemplate = new SacnPacket(senderUUID);
        updateConfiguration();
    }
}
