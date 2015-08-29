/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.handler;

import static org.eclipse.smarthome.binding.hue.HueBindingConstants.HOST;
import static org.eclipse.smarthome.binding.hue.HueBindingConstants.THING_TYPE_BRIDGE;
import static org.eclipse.smarthome.binding.hue.HueBindingConstants.USER_NAME;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import nl.q42.jue.Config;
import nl.q42.jue.FullConfig;
import nl.q42.jue.FullLight;
import nl.q42.jue.HueBridge;
import nl.q42.jue.State;
import nl.q42.jue.StateUpdate;
import nl.q42.jue.exceptions.ApiException;
import nl.q42.jue.exceptions.DeviceOffException;
import nl.q42.jue.exceptions.UnauthorizedException;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HueBridgeHandler} is the handler for a hue bridge and connects it to
 * the framework. All {@link HueLightHandler}s use the {@link HueBridgeHandler} to execute the actual commands.
 *
 * @author Dennis Nobel - Initial contribution of hue binding
 * @author Oliver Libutzki
 * @author Kai Kreuzer - improved state handling
 * @author Andre Fuechsel - implemented getFullLights(), startSearch()
 * @author Thomas Höfer - added thing properties
 * @author Stefan Bußweiler - Added new thing status handling
 * @author Jochen Hiller - fixed status updates, use reachable=true/false for state compare
 */
public class HueBridgeHandler extends BaseBridgeHandler {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);

    private static final int POLLING_FREQUENCY = 10; // in seconds

    private static final String DEFAULT_USERNAME = "EclipseSmartHome";

    private Logger logger = LoggerFactory.getLogger(HueBridgeHandler.class);

    private Map<String, FullLight> lastLightStates = new HashMap<>();

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
                        Map<String, FullLight> lastLightStateCopy = new HashMap<>(lastLightStates);
                        for (final FullLight fullLight : fullConfig.getLights()) {
                            final String lightId = fullLight.getId();
                            if (lastLightStateCopy.containsKey(lightId)) {
                                final FullLight lastFullLight = lastLightStateCopy.remove(lightId);
                                final State lastFullLightState = lastFullLight.getState();
                                lastLightStates.put(lightId, fullLight);
                                if (!isEqual(lastFullLightState, fullLight.getState())) {
                                    logger.debug("Status update for Hue light {} detected.", lightId);
                                    for (LightStatusListener lightStatusListener : lightStatusListeners) {
                                        try {
                                            lightStatusListener.onLightStateChanged(bridge, fullLight);
                                        } catch (Exception e) {
                                            logger.error(
                                                    "An exception occurred while calling the BridgeHeartbeatListener",
                                                    e);
                                        }
                                    }
                                }
                            } else {
                                lastLightStates.put(lightId, fullLight);
                                logger.debug("Hue light {} added.", lightId);
                                for (LightStatusListener lightStatusListener : lightStatusListeners) {
                                    try {
                                        lightStatusListener.onLightAdded(bridge, fullLight);
                                    } catch (Exception e) {
                                        logger.error("An exception occurred while calling the BridgeHeartbeatListener",
                                                e);
                                    }
                                }
                            }
                        }
                        // Check for removed lights
                        for (Entry<String, FullLight> fullLightEntry : lastLightStateCopy.entrySet()) {
                            lastLightStates.remove(fullLightEntry.getKey());
                            logger.debug("Hue light {} removed.", fullLightEntry.getKey());
                            for (LightStatusListener lightStatusListener : lightStatusListeners) {
                                try {
                                    lightStatusListener.onLightRemoved(bridge, fullLightEntry.getValue());
                                } catch (Exception e) {
                                    logger.error("An exception occurred while calling the BridgeHeartbeatListener", e);
                                }
                            }
                        }

                        final Config config = fullConfig.getConfig();
                        if (config != null) {
                            Map<String, String> properties = editProperties();
                            properties.put(Thing.PROPERTY_SERIAL_NUMBER, config.getMACAddress());
                            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, config.getSoftwareVersion());
                            updateProperties(properties);
                        }
                    }
                } catch (UnauthorizedException | IllegalStateException e) {
                    if (isReachable(bridge.getIPAddress())) {
                        lastBridgeConnectionState = false;
                        onNotAuthenticated(bridge);
                    } else {
                        if (lastBridgeConnectionState) {
                            lastBridgeConnectionState = false;
                            onConnectionLost(bridge);
                        }
                    }
                } catch (Exception e) {
                    if (bridge != null) {
                        if (lastBridgeConnectionState) {
                            logger.debug("Connection to Hue Bridge {} lost.", bridge.getIPAddress());
                            lastBridgeConnectionState = false;
                            onConnectionLost(bridge);
                        }
                    }
                }
            } catch (Throwable t) {
                logger.error("An unexpected error occurred: {}", t.getMessage(), t);
            }
        }

        private boolean isReachable(String ipAddress) {
            try {
                // note that InetAddress.isReachable is unreliable, see
                // http://stackoverflow.com/questions/9922543/why-does-inetaddress-isreachable-return-false-when-i-can-ping-the-ip-address
                // That's why we do an HTTP access instead

                // If there is no connection, this line will fail
                bridge.authenticate("invalid");
            } catch (IOException e) {
                return false;
            } catch (ApiException e) {
                if (e.getMessage().contains("SocketTimeout") || e.getMessage().contains("ConnectException")) {
                    return false;
                } else {
                    // this seems to be only an authentication issue
                    return true;
                }
            }
            return true;
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

    public void updateLightState(FullLight light, StateUpdate stateUpdate) {

        if (bridge != null) {
            try {
                bridge.setLightState(light, stateUpdate);
            } catch (DeviceOffException e) {
                updateLightState(light, LightStateConverter.toOnOffLightState(OnOffType.ON));
                updateLightState(light, stateUpdate);
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
        if (pollingJob != null && !pollingJob.isCancelled()) {
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

        if (getConfig().get(USER_NAME) == null) {
            getConfig().put(USER_NAME, DEFAULT_USERNAME);
        }

        if (getConfig().get(HOST) != null) {
            if (bridge == null) {
                bridge = new HueBridge((String) getConfig().get(HOST));
                bridge.setTimeout(5000);
            }
            onUpdate();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot connect to hue bridge. IP address or user name not set.");
        }
    }

    private synchronized void onUpdate() {
        if (bridge != null) {
            if (pollingJob == null || pollingJob.isCancelled()) {
                pollingJob = scheduler.scheduleAtFixedRate(pollingRunnable, 1, POLLING_FREQUENCY, TimeUnit.SECONDS);
            }
        }
    }

    /**
     * This method is called whenever the connection to the given {@link HueBridge} is lost.
     * 
     * @param bridge the hue bridge the connection is lost to
     */
    public void onConnectionLost(HueBridge bridge) {
        logger.debug("Bridge connection lost. Updating thing status to OFFLINE.");
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.BRIDGE_OFFLINE);
    }

    /**
     * This method is called whenever the connection to the given {@link HueBridge} is resumed.
     * 
     * @param bridge the hue bridge the connection is resumed to
     */
    public void onConnectionResumed(HueBridge bridge) {
        logger.debug("Bridge connection resumed. Updating thing status to ONLINE.");
        updateStatus(ThingStatus.ONLINE);
        // now also re-initialize all light handlers
        for (Thing thing : getThing().getThings()) {
            ThingHandler handler = thing.getHandler();
            if (handler != null) {
                handler.initialize();
            }
        }
    }

    /**
     * This method is called whenever the connection to the given {@link HueBridge} is available,
     * but requests are not allowed due to a missing or invalid authentication.
     *
     * @param bridge the hue bridge the connection is not authorized
     */
    public void onNotAuthenticated(HueBridge bridge) {
        String userName = (String) getConfig().get(USER_NAME);
        if (userName != null) {
            try {
                bridge.authenticate(userName);
            } catch (Exception e) {
                logger.info("Hue bridge {} is not authenticated - please press the pairing button on the bridge.",
                        getConfig().get(HOST));
                try {
                    bridge.link(userName, "gateway");
                    logger.info("User '{}' has been successfully added to Hue bridge.", userName);
                } catch (Exception ex) {
                    logger.debug("Failed adding user '{}' to Hue bridge.", userName);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                            "Not authenticated - press pairing button on the bridge or change username.");
                }
            }
        }
    }

    public boolean registerLightStatusListener(LightStatusListener lightStatusListener) {
        if (lightStatusListener == null) {
            throw new NullPointerException("It's not allowed to pass a null LightStatusListener.");
        }
        boolean result = lightStatusListeners.add(lightStatusListener);
        if (result) {
            onUpdate();
            // inform the listener initially about all lights and their states
            for (FullLight light : lastLightStates.values()) {
                lightStatusListener.onLightAdded(bridge, light);
            }
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

    public FullLight getLightById(String lightId) {
        return lastLightStates.get(lightId);
    }

    public List<FullLight> getFullLights() {
        List<FullLight> lights = null;
        if (bridge != null) {
            try {
                try {
                    lights = bridge.getFullConfig().getLights();
                } catch (UnauthorizedException | IllegalStateException e) {
                    lastBridgeConnectionState = false;
                    onNotAuthenticated(bridge);
                    lights = bridge.getFullConfig().getLights();
                }
            } catch (Exception e) {
                logger.error("Bridge cannot search for new lights.", e);
            }
        }
        return lights;
    }

    public void startSearch() {
        if (bridge != null) {
            try {
                bridge.startSearch();
            } catch (Exception e) {
                logger.error("Bridge cannot start search mode", e);
            }
        }
    }

    public void startSearch(List<String> serialNumbers) {
        if (bridge != null) {
            try {
                bridge.startSearch(serialNumbers);
            } catch (Exception e) {
                logger.error("Bridge cannot start search mode", e);
            }
        }
    }

    private boolean isEqual(State state1, State state2) {
        try {
            return state1.getAlertMode().equals(state2.getAlertMode()) && state1.isOn() == state2.isOn()
                    && state1.getEffect().equals(state2.getEffect())
                    && state1.getBrightness() == state2.getBrightness()
                    && state1.getColorMode().equals(state2.getColorMode())
                    && state1.getColorTemperature() == state2.getColorTemperature()
                    && state1.getHue() == state2.getHue()
                    && state1.getSaturation() == state2.getSaturation()
                    && state1.isReachable() == state2.isReachable();
        } catch (Exception e) {
            // if a device does not support color, the Jue library throws an NPE
            // when testing for color-related properties
            return true;
        }
    }
}
