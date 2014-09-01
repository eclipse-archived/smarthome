/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal.handler;

import java.io.IOException;
import java.net.Inet4Address;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import nl.q42.jue.FullConfig;
import nl.q42.jue.FullLight;
import nl.q42.jue.HueBridge;
import nl.q42.jue.Light;
import nl.q42.jue.State;
import nl.q42.jue.StateUpdate;
import nl.q42.jue.exceptions.ApiException;
import nl.q42.jue.exceptions.UnauthorizedException;

import org.eclipse.smarthome.binding.hue.config.HueBridgeConfiguration;
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
public class HueBridgeHandler extends BaseBridgeHandler {

    private static final int POLLING_FREQUENCY = 10; // in seconds

	private Logger logger = LoggerFactory.getLogger(HueBridgeHandler.class);

    private Map<String, FullLight> lastLightsState = new HashMap<>();

    private boolean lastBridgeConnectionState = false;

    private List<LightStatusListener> lightStatusListeners = new CopyOnWriteArrayList<>();

    private ScheduledFuture<?> pollingJob;

	private Runnable pollingRunnable = new Runnable() {

        @Override
        public void run() {
        	try {
	        	try {
	            	FullConfig fullConfig = bridge.getFullConfig();
		            if (!lastBridgeConnectionState) {
		                logger.debug("Connection to Hue Bridge {} established.", bridge.getIPAddress());
	                    lastBridgeConnectionState = true;
	                    onConnectionResumed(bridge);
		            }
		            if (lastBridgeConnectionState) {
		                Map<String, FullLight> lastLightStateCopy = new HashMap<>(lastLightsState);
		                    for (final FullLight fullLight : fullConfig.getLights()) {
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
		            }
	            } catch (UnauthorizedException|IllegalStateException e) {
	        		boolean isReachable = Inet4Address.getByName(bridge.getIPAddress()).isReachable(2000);
	        		if(isReachable) {
		                lastBridgeConnectionState = false;
		                onNotAuthenticated(bridge);
	        		} else {
	        			if(lastBridgeConnectionState) {
			                lastBridgeConnectionState = false;
			                onConnectionLost(bridge);
	        			}
	        		}
		        } catch (Exception e) {
		        	if(bridge!=null) {
	        			if(lastBridgeConnectionState) {
				        	logger.debug("Connection to Hue Bridge {} lost.", bridge.getIPAddress());
				        	lastBridgeConnectionState = false;
			                onConnectionLost(bridge);
	        			}
		        	}
		        }
        	} catch(Throwable t) {
        		logger.error("An unexpected error occurred: {}", t.getMessage(), t);
        	}
        }
    };
    
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
            } catch (IllegalStateException e) {
                logger.trace("Error while accessing light: {}", e.getMessage());
            }
        } else {
            logger.warn("No bridge connected or selected. Cannot set light state.");
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        if(pollingJob!=null && !pollingJob.isCancelled()) {
        	pollingJob.cancel(true);
        	pollingJob = null;
        }
        if (bridge != null) {
            bridge = null;
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing hue bridge handler.");

        HueBridgeConfiguration configuration = getConfigAs(HueBridgeConfiguration.class);

        if (configuration.ipAddress != null) {
        	if (bridge == null) {
        		bridge = new HueBridge(configuration.ipAddress);
        		bridge.setTimeout(5000);
        	}
        	this.lastBridgeConnectionState = isConnectionEstablished(bridge);
        	onUpdate();
        } else {
            logger.warn("Cannot connect to hue bridge. IP address or user name not set.");
        }
    }

    private boolean isConnectionEstablished(HueBridge bridge) {
        try {
            bridge.getLights();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private synchronized void onUpdate() {
    	if (bridge != null) {
			if (pollingJob == null || pollingJob.isCancelled()) {
				pollingJob = scheduler.scheduleAtFixedRate(pollingRunnable, 0, POLLING_FREQUENCY, TimeUnit.SECONDS);
			}
    	}
    }

    /**
     * This method is called whenever the connection to the given {@link HueBridge} is lost.
     * @param bridge the hue bridge the connection is lost to
     */
    public void onConnectionLost(HueBridge bridge) {
        logger.debug("Bridge connection lost. Updating thing status to OFFLINE.");
        updateStatus(ThingStatus.OFFLINE);
    }

    /**
     * This method is called whenever the connection to the given {@link HueBridge} is resumed.
     * @param bridge the hue bridge the connection is resumed to
     */
    public void onConnectionResumed(HueBridge bridge) {
        logger.debug("Bridge connection resumed. Updating thing status to ONLINE.");
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * This method is called whenever the connection to the given {@link HueBridge} is available,
     * but requests are not allowed due to a missing or invalid authentication.
     * 
     * @param bridge the hue bridge the connection is not authorized
     */
	public void onNotAuthenticated(HueBridge bridge) {
        HueBridgeConfiguration configuration = getConfigAs(HueBridgeConfiguration.class);
    	try {
			bridge.authenticate(configuration.userName);
		} catch (Exception e) {
			logger.debug("Hue bridge {} is not authenticated - please add user '{}'.", configuration.ipAddress, configuration.userName);
		}
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
        } catch (IllegalStateException e) {
            logger.trace("Error while accessing light: {}", e.getMessage());
        }
        return null;
        
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
}
