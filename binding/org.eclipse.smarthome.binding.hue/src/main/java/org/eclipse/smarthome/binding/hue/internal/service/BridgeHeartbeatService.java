/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import nl.q42.jue.FullLight;
import nl.q42.jue.HueBridge;
import nl.q42.jue.Light;
import nl.q42.jue.State;
import nl.q42.jue.exceptions.ApiException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BridgeHeartbeatService} is responsible for polling the Hue bridge
 * in order to detect changes and notify listeners.
 * 
 * @author Oliver Libutzki - Initial contribution
 * 
 */
public class BridgeHeartbeatService {

    private static Logger logger = LoggerFactory.getLogger(BridgeHeartbeatService.class);

    private HueBridge bridge;

    private WorkerThread heartbeatThread = null;

    private Map<String, FullLight> lastLightsState = new HashMap<>();

    private boolean lastBridgeConnectionState = false;

    private List<LightStatusListener> lightStatusListeners = new CopyOnWriteArrayList<>();

    private List<BridgeStatusListener> bridgeStatusListeners = new CopyOnWriteArrayList<>();

    private Runnable heartbeatRunnable = new Runnable() {

        @Override
        public void run() {
            if (isConnectionEstablished(bridge) && !lastBridgeConnectionState) {
                logger.debug("Connection to Hue Bridge {} resumed.", bridge.getIPAddress());
                for (BridgeStatusListener bridgeStatusListener : bridgeStatusListeners) {
                    lastBridgeConnectionState = true;
                    bridgeStatusListener.onConnectionResumed(bridge);
                }
            } else if (!isConnectionEstablished(bridge) && lastBridgeConnectionState) {
                logger.debug("Connection to Hue Bridge {} lost.", bridge.getIPAddress());
                for (BridgeStatusListener bridgeStatusListener : bridgeStatusListeners) {
                    lastBridgeConnectionState = false;
                    bridgeStatusListener.onConnectionLost(bridge);
                }
            }
            if (lastBridgeConnectionState) {
                Map<String, FullLight> lastLightStateCopy = new HashMap<>(lastLightsState);
                try {
                    for (final FullLight fullLight : getFullLights()) {
                        final String lightId = fullLight.getId();
                        if (lastLightStateCopy.containsKey(lightId)) {
                            final FullLight lastFullLight = lastLightStateCopy.remove(lightId);
                            final State lastFullLightState = lastFullLight.getState();
                            lastLightsState.put(lightId, fullLight);
                            if (!isEqual(lastFullLightState, fullLight.getState())) {
                                logger.debug("Status update for Hue light {} detected.", lightId);
                                for (LightStatusListener lightStatusListener : lightStatusListeners) {
                                    try {
                                        lightStatusListener.onLightStateChanged(bridge, fullLight);
                                    } catch (Exception e) {
                                        logger.error(
                                                "An exception occurred while calling the BridgeHeartbeatListener", e);
                                    }
                                }
                            }
                        } else {
                            lastLightsState.put(lightId, fullLight);
                            logger.debug("Hue light {} added.", lightId);
                            for (LightStatusListener lightStatusListener : lightStatusListeners) {
                                try {
                                    lightStatusListener.onLightAdded(bridge, fullLight);
                                } catch (Exception e) {
                                    logger.error(
                                            "An exception occurred while calling the BridgeHeartbeatListener",
                                            e);
                                }
                            }
                        }
                    }
                    // Check for removed lights
                    for (Entry<String, FullLight> fullLightEntry : lastLightStateCopy.entrySet()) {
                        lastLightsState.remove(fullLightEntry.getKey());
                        logger.debug("Hue light {} removed.", fullLightEntry.getKey());
                        for (LightStatusListener lightStatusListener : lightStatusListeners) {
                            try {
                                lightStatusListener.onLightRemoved(bridge,
                                        fullLightEntry.getValue());
                            } catch (Exception e) {
                                logger.error(
                                        "An exception occurred while calling the BridgeHeartbeatListener",
                                        e);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.debug("Error retrieving heartbeat from Hue Bridge {} lost.", bridge.getIPAddress(), e);
                }
            }
        }
    };

    private List<FullLight> getFullLights() throws IOException, ApiException {
        final List<FullLight> fullLights = new ArrayList<>();

        final List<Light> lights = bridge.getLights();
        for (Light light : lights) {
            fullLights.add(bridge.getLight(light));
        }
        return fullLights;

    }

    private boolean isEqual(State state1, State state2) {
    	try {
	    	return state1.getAlertMode().equals(state2.getAlertMode())
	                && state1.isOn() == state2.isOn()
   	                && state1.getEffect().equals(state2.getEffect())
	                && state1.getBrightness() == state2.getBrightness()
	                && state1.getColorMode().equals(state2.getColorMode())
	                && state1.getColorTemperature() == state2.getColorTemperature()
	                && state1.getHue() == state2.getHue()
	                && state1.getSaturation() == state2.getSaturation();
    	} catch(Exception e) {
    		// if a device does not support color, the Jue library throws an NPE
    		// when testing for color-related properties
    		return true;
    	}
    }

    public void initialize(HueBridge bridge) {
    	this.bridge = bridge;
    	this.lastBridgeConnectionState = isConnectionEstablished(bridge);
    	onUpdate();
    }

    private boolean isConnectionEstablished(HueBridge bridge) {
        try {
            bridge.getLights();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean registerBridgeStatusListener(BridgeStatusListener bridgeStatusListener) {
        if (bridgeStatusListener == null) {
            throw new NullPointerException("It's not allowed to pass a null BridgeStatusListener.");
        }
        boolean result = bridgeStatusListeners.add(bridgeStatusListener);
        if (result) {
            onUpdate();
        }
        return result;
    }

    public boolean unregisterBridgeStatusListener(BridgeStatusListener bridgeStatusListener) {
        boolean result = bridgeStatusListeners.remove(bridgeStatusListener);
        if (result) {
            onUpdate();
        }
        return result;
    }

    public boolean registerLightStatusListener(LightStatusListener lightStatusListener) {
        if (lightStatusListener == null) {
            throw new NullPointerException("It's not allowed to pass a null LightStatusListener.");
        }
        boolean result = lightStatusListeners.add(lightStatusListener);
        if (result) {
            onUpdate();
        }
        return result;
    }

    public boolean unregisterLightStatusListener(LightStatusListener lightStatusListener) {
        boolean result = lightStatusListeners.remove(lightStatusListener);
        if (result) {
            onUpdate();
        }
        return result;
    }

    private synchronized void onUpdate() {
    	if (bridge != null) {
    		if (!lightStatusListeners.isEmpty() || !bridgeStatusListeners.isEmpty()) {
    			if (heartbeatThread == null) {
    				heartbeatThread = new WorkerThread(heartbeatRunnable, "HueBridgeHeartbeat Worker",
    						5000);
    				heartbeatThread.start();
    			} else {
    				heartbeatThread.proceed();
    			}
    		} else {
    			if (heartbeatThread != null) {
    				heartbeatThread.pause();
    			}
    		}   		
    	}
    }

    public void dispose() {
        if (heartbeatThread != null) {
            heartbeatThread.dispose();
        }
    }
}
