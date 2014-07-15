/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal.handler;

import java.io.IOException;
import java.util.List;

import nl.q42.jue.HueBridge;
import nl.q42.jue.Light;
import nl.q42.jue.StateUpdate;
import nl.q42.jue.exceptions.ApiException;

import org.eclipse.smarthome.binding.hue.config.HueBridgeConfiguration;
import org.eclipse.smarthome.binding.hue.internal.service.BridgeHeartbeatService;
import org.eclipse.smarthome.binding.hue.internal.service.BridgeStatusListener;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HueBridgeHandler} is the handler for a hue bridge and connects it to
 * the framework. All {@link HueLightHandler}s use the {@link HueBridgeHandler}
 * to execute the actual commands.
 * 
 * @author Dennis Nobel - Initial contribution of hue binding
 * @author Oliver Libutzki
 * @author Kai Kreuzer - improved state handling
 * 
 */
public class HueBridgeHandler extends BaseBridgeHandler implements
        BridgeStatusListener {

    private Logger logger = LoggerFactory.getLogger(HueBridgeHandler.class);

    private BridgeHeartbeatService bridgeHeartbeatService = new BridgeHeartbeatService();

    private HueBridge bridge = null;
 
    public HueBridgeHandler(Bridge hueBridge) {
        super(hueBridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // not needed
    }

    public void updateLightState(Light light, StateUpdate stateUpdate) {

        if (bridge != null) {
            try {
                bridge.setLightState(light, stateUpdate);
            } catch (IOException | ApiException e) {
                throw new RuntimeException(e);
            }
        } else {
            logger.warn("No bridge connected or selected. Cannot set light state.");
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposes. Unregistering listener.");
        if (bridge != null) {
            bridgeHeartbeatService.unregisterBridgeStatusListener(this);
            bridgeHeartbeatService.dispose();
            bridge = null;
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing hue bridge handler.");

        HueBridgeConfiguration configuration = getConfigAs(HueBridgeConfiguration.class);

        if (configuration.ipAddress != null && configuration.userName != null) {
        	if (bridge == null) {
        		bridge = new HueBridge(configuration.ipAddress);
        		bridge.setTimeout(5000);
        	}
        	bridgeHeartbeatService.initialize(bridge);
            bridgeHeartbeatService.registerBridgeStatusListener(this);
        } else {
            logger.warn("Cannot connect to hue bridge. IP address or user name not set.");
        }
    }

    @Override
    public void onConnectionLost(HueBridge bridge) {
        logger.info("Bridge connection lost. Updating thing status to OFFLINE.");
        this.bridge = null;
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void onConnectionResumed(HueBridge bridge) {
        logger.info("Bridge connection resumed. Updating thing status to ONLINE.");
        this.bridge = bridge;
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
	public void onNotAuthenticated(HueBridge bridge) {
        HueBridgeConfiguration configuration = getConfigAs(HueBridgeConfiguration.class);
    	try {
			bridge.authenticate(configuration.userName);
		} catch (Exception e) {
			logger.debug("Hue bridge is not authenticated - please add user '{}'.", configuration.userName);
		}
	}

	public BridgeHeartbeatService getBridgeHeartbeatService() {
        if (bridgeHeartbeatService == null) {
            throw new RuntimeException("The heartbeat service for bridge " + bridge.getIPAddress()
                    + " has not been initialized.");
        } else {
            return bridgeHeartbeatService;
        }
    }

    public Light getLightById(String lightId) {
        List<Light> lights;
        try {
            lights = bridge.getLights();
            for (Light light : lights) {
                if (light.getId().equals(lightId)) {
                    return light;
                }
            }
        } catch (IOException | ApiException e) {
            throw new RuntimeException(e);
        }
        return null;
        
    }


}
