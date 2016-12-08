/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.handler;

import static org.eclipse.smarthome.binding.hue.HueBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.binding.hue.internal.HueConfigStatusMessage;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.q42.jue.Config;
import nl.q42.jue.FullConfig;
import nl.q42.jue.FullLight;
import nl.q42.jue.HueBridge;
import nl.q42.jue.State;
import nl.q42.jue.StateUpdate;
import nl.q42.jue.exceptions.ApiException;
import nl.q42.jue.exceptions.DeviceOffException;
import nl.q42.jue.exceptions.LinkButtonException;
import nl.q42.jue.exceptions.UnauthorizedException;

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
public class HueBridgeHandler extends ConfigStatusBridgeHandler {

    private static final String LIGHT_STATE_ADDED = "added";

    private static final String LIGHT_STATE_CHANGED = "changed";

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);

    private static final int DEFAULT_POLLING_INTERVAL = 10; // in seconds

    private static final String DEVICE_TYPE = "EclipseSmartHome";

    private Logger logger = LoggerFactory.getLogger(HueBridgeHandler.class);

    private Map<String, FullLight> lastLightStates = new ConcurrentHashMap<>();

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
                        lastBridgeConnectionState = tryResumeBridgeConnection();
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
                                    notifyLightStatusListeners(fullLight, LIGHT_STATE_CHANGED);
                                }
                            } else {
                                lastLightStates.put(lightId, fullLight);
                                logger.debug("Hue light {} added.", lightId);
                                notifyLightStatusListeners(fullLight, LIGHT_STATE_ADDED);
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
                        if (lastBridgeConnectionState || thing.getStatus() == ThingStatus.INITIALIZING) {
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
                if (e.getMessage().contains("SocketTimeout") || e.getMessage().contains("ConnectException")
                        || e.getMessage().contains("SocketException")
                        || e.getMessage().contains("NoRouteToHostException")) {
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

        if (getConfig().get(HOST) != null) {
            if (bridge == null) {
                bridge = new HueBridge((String) getConfig().get(HOST));
                bridge.setTimeout(5000);
            }
            onUpdate();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot connect to hue bridge. IP address not set.");
        }
    }

    private synchronized void onUpdate() {
        if (bridge != null) {
            if (pollingJob == null || pollingJob.isCancelled()) {
                int pollingInterval = DEFAULT_POLLING_INTERVAL;
                try {
                    Object pollingIntervalConfig = getConfig().get(POLLING_INTERVAL);
                    if (pollingIntervalConfig != null) {
                        pollingInterval = ((BigDecimal) pollingIntervalConfig).intValue();
                    } else {
                        logger.info("Polling interval not configured for this hue bridge. Using default value: {}s",
                                pollingInterval);
                    }
                } catch (NumberFormatException ex) {
                    logger.info("Wrong configuration value for polling interval. Using default value: {}s",
                            pollingInterval);
                }
                pollingJob = scheduler.scheduleAtFixedRate(pollingRunnable, 1, pollingInterval, TimeUnit.SECONDS);
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
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE);
    }

    /**
     * This method is called whenever the connection to the given {@link HueBridge} is resumed.
     *
     * @param bridge the hue bridge the connection is resumed to
     */
    public void onConnectionResumed(HueBridge bridge) {
        logger.debug("Bridge connection resumed. Updating thing status to ONLINE.");
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Check USER_NAME config for null. Call onConnectionResumed() otherwise.
     *
     * @return True if USER_NAME was not null.
     */
    private boolean tryResumeBridgeConnection() {
        logger.debug("Connection to Hue Bridge {} established.", bridge.getIPAddress());
        if (getConfig().get(USER_NAME) == null) {
            logger.warn("User name for Hue bridge authentication not available in configuration. "
                    + "Setting ThingStatus to offline.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "User name is not properly configured - please check log files");
            return false;
        } else {
            onConnectionResumed(bridge);
            return true;
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
        if (userName == null) {
            createUser(bridge);
        } else {
            try {
                bridge.authenticate(userName);
            } catch (Exception e) {
                handleAuthenticationFailure(e, userName);
            }
        }
    }

    private void createUser(HueBridge bridge) {
        try {
            String newUser = createUserOnPhysicalBridge(bridge);
            updateBridgeThingConfiguration(newUser);
        } catch (LinkButtonException ex) {
            handleLinkButtonNotPressed(ex);
        } catch (Exception ex) {
            handleExceptionWhileCreatingUser(ex);
        }
    }

    private String createUserOnPhysicalBridge(HueBridge bridge) throws IOException, ApiException {
        logger.info("Creating new user on Hue bridge {} - please press the pairing button on the bridge.",
                getConfig().get(HOST));
        String userName = bridge.link(DEVICE_TYPE);
        logger.info("User '{}' has been successfully added to Hue bridge.", userName);
        return userName;
    }

    private void updateBridgeThingConfiguration(String userName) {
        Configuration config = editConfiguration();
        config.put(USER_NAME, userName);
        try {
            updateConfiguration(config);
            logger.debug("Updated configuration parameter {} to '{}'", USER_NAME, userName);
        } catch (IllegalStateException e) {
            logger.trace("Configuration update failed.", e);
            logger.warn("Unable to update configuration of Hue bridge.");
            logger.warn("Please configure the following user name manually: {}", userName);
        }
    }

    private void handleAuthenticationFailure(Exception ex, String userName) {
        logger.warn("User {} is not authenticated on Hue bridge {}", userName, getConfig().get(HOST));
        logger.warn("Please configure a valid user or remove user from configuration to generate a new one.");
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                "Authentication failed - remove user name from configuration to generate a new one.");
    }

    private void handleLinkButtonNotPressed(LinkButtonException ex) {
        logger.debug("Failed creating new user on Hue bridge: {}", ex.getMessage());
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                "Not authenticated - press pairing button on the bridge.");
    }

    private void handleExceptionWhileCreatingUser(Exception ex) {
        logger.warn("Failed creating new user on Hue bridge", ex);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                "Failed to create new user on bridge: " + ex.getMessage());
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

    /**
     * Iterate through lightStatusListeners and notify them about a changed ot added light state.
     *
     * @param fullLight
     * @param type Can be "changed" if just a state has changed or "added" if this is a new light on the bridge.
     */
    private void notifyLightStatusListeners(final FullLight fullLight, final String type) {
        for (LightStatusListener lightStatusListener : lightStatusListeners) {
            try {
                switch (type) {
                    case LIGHT_STATE_ADDED:
                        lightStatusListener.onLightAdded(bridge, fullLight);
                        break;
                    case LIGHT_STATE_CHANGED:
                        lightStatusListener.onLightStateChanged(bridge, fullLight);
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "Could not notify lightStatusListeners for unknown event type " + type);
                }

            } catch (Exception e) {
                logger.error("An exception occurred while calling the BridgeHeartbeatListener", e);
            }
        }
    }

    /**
     * Because the State can produce NPEs on getColorMode() and getEffect(), at first we check for the common
     * properties which are set for every light type. If they equal, we additionally try to check the colorMode. If we
     * get an NPE,
     * the light does not support color mode and the common properties equality is our result: true. Otherwise if no NPE
     * occurs
     * the equality of colorMode is our result.
     *
     * @param state1 Reference state
     * @param state2 State which is checked for equality.
     * @return True if the available informations of both states are equal.
     */
    private boolean isEqual(State state1, State state2) {
        boolean commonStateIsEqual = state1.getAlertMode().equals(state2.getAlertMode())
                && state1.isOn() == state2.isOn() && state1.getBrightness() == state2.getBrightness()
                && state1.getColorTemperature() == state2.getColorTemperature() && state1.getHue() == state2.getHue()
                && state1.getSaturation() == state2.getSaturation() && state1.isReachable() == state2.isReachable();
        if (!commonStateIsEqual) {
            return false;
        }

        boolean colorModeIsEqual = true;
        boolean effectIsEqual = true;
        try {
            colorModeIsEqual = state1.getColorMode().equals(state2.getColorMode());
        } catch (NullPointerException npe) {
            logger.trace("Light does not support color mode.");
        }
        try {
            effectIsEqual = state1.getEffect().equals(state2.getEffect());
        } catch (NullPointerException npe) {
            logger.trace("Light does not support effect.");
        }
        return colorModeIsEqual && effectIsEqual;
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        // The bridge IP address to be used for checks
        final String bridgeIpAddress = (String) getThing().getConfiguration().get(HOST);
        Collection<ConfigStatusMessage> configStatusMessages;

        // Check whether an IP address is provided
        if (bridgeIpAddress == null || bridgeIpAddress.isEmpty()) {
            configStatusMessages = Collections.singletonList(ConfigStatusMessage.Builder.error(HOST)
                    .withMessageKeySuffix(HueConfigStatusMessage.IP_ADDRESS_MISSING.getMessageKey()).withArguments(HOST)
                    .build());
        } else {
            configStatusMessages = Collections.emptyList();
        }

        return configStatusMessages;
    }
}
