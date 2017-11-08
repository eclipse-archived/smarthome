/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.dmx.handler;

import static org.eclipse.smarthome.binding.dmx.DmxBindingConstants.*;

import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.binding.dmx.internal.DmxBridgeHandler;
import org.eclipse.smarthome.binding.dmx.internal.dmxoverethernet.IpNode;
import org.eclipse.smarthome.binding.dmx.internal.multiverse.Universe;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Lib485BridgeHandler} is responsible for communication with
 * an Lib485 instance
 *
 * @author Jan N. Klug - Initial contribution
 */

public class Lib485BridgeHandler extends DmxBridgeHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_LIB485_BRIDGE);
    public static final int MIN_UNIVERSE_ID = 0;
    public static final int MAX_UNIVERSE_ID = 0;
    public static final int DEFAULT_PORT = 9020;

    private final Logger logger = LoggerFactory.getLogger(Lib485BridgeHandler.class);
    private Map<IpNode, Socket> receiverNodes = new HashMap<IpNode, Socket>();

    public Lib485BridgeHandler(Bridge lib485Bridge) {
        super(lib485Bridge);
    }

    @Override
    protected void openConnection() {
        if (!this.thing.getStatus().equals(ThingStatus.ONLINE)) {
            for (IpNode receiverNode : receiverNodes.keySet()) {
                Socket socket = receiverNodes.get(receiverNode);
                if (socket == null) {
                    try {
                        socket = new Socket(receiverNode.getAddressString(), receiverNode.getPort());
                    } catch (IOException e) {
                        logger.debug("Could not connect to {} in {}: {}", receiverNode, this.thing.getUID(), e);
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "could not connect to " + receiverNode.toString());
                        return;
                    }
                }

                if (socket.isConnected()) {
                    receiverNodes.put(receiverNode, socket);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                    receiverNodes.put(receiverNode, null);
                    return;
                }
            }
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    protected void closeConnection() {
        for (IpNode receiverNode : receiverNodes.keySet()) {
            Socket socket = receiverNodes.get(receiverNode);
            if ((socket != null) && (!socket.isClosed())) {
                try {
                    socket.close();
                } catch (IOException e) {
                    logger.warn("Could not close socket {} in {}: {}", receiverNode, this.thing.getUID(), e);
                }
            }
            receiverNodes.put(receiverNode, null);
        }
    }

    @Override
    protected void sendDmxData() {
        if (this.thing.getStatus().equals(ThingStatus.ONLINE)) {
            long now = System.currentTimeMillis();
            universe.calculateBuffer(now);
            for (IpNode receiverNode : receiverNodes.keySet()) {
                Socket socket = receiverNodes.get(receiverNode);
                if (socket.isConnected()) {
                    try {
                        socket.getOutputStream().write(universe.getBuffer());
                    } catch (IOException e) {
                        logger.debug("Could not send to {} in {}: {}", receiverNode, this.thing.getUID(), e);
                        closeConnection(ThingStatusDetail.COMMUNICATION_ERROR, "could not send DMX data");
                        return;
                    }
                } else {
                    closeConnection(ThingStatusDetail.NONE, "reconnect");
                    return;
                }
            }
        } else {
            openConnection();
        }
    }

    @Override
    protected void updateConfiguration() {
        Configuration configuration = getConfig();

        universe = new Universe(MIN_UNIVERSE_ID);

        receiverNodes.clear();
        if (configuration.get(CONFIG_ADDRESS) == null) {
            receiverNodes.put(new IpNode("localhost:9020"), null);
            logger.debug("sending to {} for {}", receiverNodes, this.thing.getUID());
        } else {
            try {
                for (IpNode receiverNode : IpNode.fromString((String) configuration.get(CONFIG_ADDRESS),
                        DEFAULT_PORT)) {
                    receiverNodes.put(receiverNode, null);
                    logger.debug("sending to {} for {}", receiverNode, this.thing.getUID());
                }
            } catch (IllegalArgumentException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                return;
            }
        }
        super.updateConfiguration();

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);

        logger.debug("updated configuration for Lib485 bridge {}", this.thing.getUID());
    }

    @Override
    public void initialize() {
        logger.debug("initializing Lib485 bridge {}", this.thing.getUID());

        updateConfiguration();
    }

}
