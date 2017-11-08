/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.handler;

import static org.eclipse.smarthome.binding.hue.HueBindingConstants.*;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.binding.hue.internal.FullLight;
import org.eclipse.smarthome.binding.hue.internal.HueBridge;
import org.eclipse.smarthome.binding.hue.internal.State;
import org.eclipse.smarthome.binding.hue.internal.StateUpdate;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HueLightHandler} is the handler for a hue light. It uses the {@link HueBridgeHandler} to execute the actual
 * command.
 *
 * @author Dennis Nobel - Initial contribution of hue binding
 * @author Oliver Libutzki
 * @author Kai Kreuzer - stabilized code
 * @author Andre Fuechsel - implemented switch off when brightness == 0, changed to support generic thing types, changed
 *         the initialization of properties
 * @author Thomas Höfer - added thing properties
 * @author Jochen Hiller - fixed status updates for reachable=true/false
 * @author Markus Mazurczak - added code for command handling of OSRAM PAR16 50
 *         bulbs
 * @author Yordan Zhelev - added alert and effect functions
 * @author Denis Dudnik - switched to internally integrated source of Jue library
 *
 */
public class HueLightHandler extends BaseThingHandler implements LightStatusListener {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream.of(THING_TYPE_COLOR_LIGHT,
            THING_TYPE_COLOR_TEMPERATURE_LIGHT, THING_TYPE_DIMMABLE_LIGHT, THING_TYPE_EXTENDED_COLOR_LIGHT,
            THING_TYPE_ON_OFF_LIGHT, THING_TYPE_ON_OFF_PLUG, THING_TYPE_DIMMABLE_PLUG).collect(Collectors.toSet());

    // @formatter:off
    private final static Map<String, List<String>> VENDOR_MODEL_MAP = Stream.of(
            new SimpleEntry<>("Philips",
                    Arrays.asList("LCT001", "LCT002", "LCT003", "LCT007", "LLC001", "LLC006", "LLC007", "LLC010",
                            "LLC011", "LLC012", "LLC013", "LLC020", "LST001", "LST002", "LWB004", "LWB006", "LWB007",
                            "LWL001")),
            new SimpleEntry<>("OSRAM",
                    Arrays.asList("Classic_A60_RGBW", "PAR16_50_TW", "Surface_Light_TW", "Plug_01")))
        .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue()));
    // @formatter:on

    private final static String OSRAM_PAR16_50_TW_MODEL_ID = "PAR16_50_TW";

    public static final String NORMALIZE_ID_REGEX = "[^a-zA-Z0-9_]";

    private String lightId;

    private Integer lastSentColorTemp;
    private Integer lastSentBrightness;

    private Logger logger = LoggerFactory.getLogger(HueLightHandler.class);

    // Flag to indicate whether the bulb is of type Osram par16 50 TW or not
    private boolean isOsramPar16 = false;

    private boolean propertiesInitializedSuccessfully = false;

    private HueBridgeHandler bridgeHandler;

    ScheduledFuture<?> scheduledFuture;

    public HueLightHandler(Thing hueLight) {
        super(hueLight);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing hue light handler.");
        initializeThing((getBridge() == null) ? null : getBridge().getStatus());
    }

    private void initializeThing(ThingStatus bridgeStatus) {
        logger.debug("initializeThing thing {} bridge status {}", getThing().getUID(), bridgeStatus);
        final String configLightId = (String) getConfig().get(LIGHT_ID);
        if (configLightId != null) {
            lightId = configLightId;
            // note: this call implicitly registers our handler as a listener on
            // the bridge
            if (getHueBridgeHandler() != null) {
                if (bridgeStatus == ThingStatus.ONLINE) {
                    initializeProperties();
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-light-id");
        }
    }

    private synchronized void initializeProperties() {
        if (!propertiesInitializedSuccessfully) {
            FullLight fullLight = getLight();
            if (fullLight != null) {
                String modelId = fullLight.getModelID().replaceAll(NORMALIZE_ID_REGEX, "_");
                updateProperty(Thing.PROPERTY_MODEL_ID, modelId);
                updateProperty(Thing.PROPERTY_FIRMWARE_VERSION, fullLight.getSoftwareVersion());
                String vendor = getVendor(modelId);
                if (vendor != null) {
                    updateProperty(Thing.PROPERTY_VENDOR, vendor);
                }
                updateProperty(LIGHT_UNIQUE_ID, fullLight.getUniqueID());
                isOsramPar16 = OSRAM_PAR16_50_TW_MODEL_ID.equals(modelId);
                updateThing(thing);
                propertiesInitializedSuccessfully = true;
            }
        }
    }

    private String getVendor(String modelId) {
        for (String vendor : VENDOR_MODEL_MAP.keySet()) {
            if (VENDOR_MODEL_MAP.get(vendor).contains(modelId)) {
                return vendor;
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposes. Unregistering listener.");
        if (lightId != null) {
            HueBridgeHandler bridgeHandler = getHueBridgeHandler();
            if (bridgeHandler != null) {
                bridgeHandler.unregisterLightStatusListener(this);
                this.bridgeHandler = null;
            }
            lightId = null;
        }
    }

    private FullLight getLight() {
        HueBridgeHandler bridgeHandler = getHueBridgeHandler();
        if (bridgeHandler != null) {
            return bridgeHandler.getLightById(lightId);
        }
        return null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        HueBridgeHandler hueBridge = getHueBridgeHandler();
        if (hueBridge == null) {
            logger.warn("hue bridge handler not found. Cannot handle command without bridge.");
            return;
        }

        FullLight light = getLight();
        if (light == null) {
            logger.debug("hue light not known on bridge. Cannot handle command.");
            return;
        }

        StateUpdate lightState = null;
        switch (channelUID.getId()) {
            case CHANNEL_COLORTEMPERATURE:
                if (command instanceof PercentType) {
                    lightState = LightStateConverter.toColorTemperatureLightState((PercentType) command);
                } else if (command instanceof OnOffType) {
                    lightState = LightStateConverter.toOnOffLightState((OnOffType) command);
                    if (isOsramPar16) {
                        lightState = addOsramSpecificCommands(lightState, (OnOffType) command);
                    }
                } else if (command instanceof IncreaseDecreaseType) {
                    lightState = convertColorTempChangeToStateUpdate((IncreaseDecreaseType) command, light);
                }
                break;
            case CHANNEL_BRIGHTNESS:
                if (command instanceof PercentType) {
                    lightState = LightStateConverter.toBrightnessLightState((PercentType) command);
                } else if (command instanceof OnOffType) {
                    lightState = LightStateConverter.toOnOffLightState((OnOffType) command);
                    if (isOsramPar16) {
                        lightState = addOsramSpecificCommands(lightState, (OnOffType) command);
                    }
                } else if (command instanceof IncreaseDecreaseType) {
                    lightState = convertBrightnessChangeToStateUpdate((IncreaseDecreaseType) command, light);
                }
                break;
            case CHANNEL_SWITCH:
                logger.trace("CHANNEL_SWITCH handling command {}", command);
                if (command instanceof OnOffType) {
                    lightState = LightStateConverter.toOnOffLightState((OnOffType) command);
                    if (isOsramPar16) {
                        lightState = addOsramSpecificCommands(lightState, (OnOffType) command);
                    }
                }
                break;
            case CHANNEL_COLOR:
                if (command instanceof HSBType) {
                    HSBType hsbCommand = (HSBType) command;
                    if (hsbCommand.getBrightness().intValue() == 0) {
                        lightState = LightStateConverter.toOnOffLightState(OnOffType.OFF);
                    } else {
                        lightState = LightStateConverter.toColorLightState(hsbCommand);
                    }
                } else if (command instanceof PercentType) {
                    lightState = LightStateConverter.toBrightnessLightState((PercentType) command);
                } else if (command instanceof OnOffType) {
                    lightState = LightStateConverter.toOnOffLightState((OnOffType) command);
                } else if (command instanceof IncreaseDecreaseType) {
                    lightState = convertBrightnessChangeToStateUpdate((IncreaseDecreaseType) command, light);
                }
                break;
            case CHANNEL_ALERT:
                if (command instanceof StringType) {
                    lightState = LightStateConverter.toAlertState((StringType) command);
                    if (lightState == null) {
                        // Unsupported StringType is passed. Log a warning
                        // message and return.
                        logger.warn("Unsupported String command: {}. Supported commands are: {}, {}, {} ", command,
                                LightStateConverter.ALERT_MODE_NONE, LightStateConverter.ALERT_MODE_SELECT,
                                LightStateConverter.ALERT_MODE_LONG_SELECT);
                        return;
                    } else {
                        scheduleAlertStateRestore(command);
                    }
                }
                break;
            case CHANNEL_EFFECT:
                if (command instanceof OnOffType) {
                    lightState = LightStateConverter.toOnOffEffectState((OnOffType) command);
                }
                break;
        }
        if (lightState != null) {
            hueBridge.updateLightState(light, lightState);
        } else {
            logger.warn("Command sent to an unknown channel id: {}", channelUID);
        }
    }

    /*
     * Applies additional {@link StateUpdate} commands as a workaround for Osram
     * Lightify PAR16 TW firmware bug. Also see
     * http://www.everyhue.com/vanilla/discussion
     * /1756/solved-lightify-turning-off
     */
    private StateUpdate addOsramSpecificCommands(StateUpdate lightState, OnOffType actionType) {
        if (actionType.equals(OnOffType.ON)) {
            lightState.setBrightness(254);
        } else {
            lightState.setTransitionTime(0);
        }
        return lightState;
    }

    private StateUpdate convertColorTempChangeToStateUpdate(IncreaseDecreaseType command, FullLight light) {
        StateUpdate stateUpdate = null;
        Integer currentColorTemp = getCurrentColorTemp(light.getState());
        if (currentColorTemp != null) {
            int newColorTemp = LightStateConverter.toAdjustedColorTemp(command, currentColorTemp);
            stateUpdate = new StateUpdate().setColorTemperature(newColorTemp);
            lastSentColorTemp = newColorTemp;
        }
        return stateUpdate;
    }

    private Integer getCurrentColorTemp(State lightState) {
        Integer colorTemp = lastSentColorTemp;
        if (colorTemp == null && lightState != null) {
            colorTemp = lightState.getColorTemperature();
        }
        return colorTemp;
    }

    private StateUpdate convertBrightnessChangeToStateUpdate(IncreaseDecreaseType command, FullLight light) {
        StateUpdate stateUpdate = null;
        Integer currentBrightness = getCurrentBrightness(light.getState());
        if (currentBrightness != null) {
            int newBrightness = LightStateConverter.toAdjustedBrightness(command, currentBrightness);
            stateUpdate = createBrightnessStateUpdate(currentBrightness, newBrightness);
            lastSentBrightness = newBrightness;
        }
        return stateUpdate;
    }

    private Integer getCurrentBrightness(State lightState) {
        Integer brightness = lastSentBrightness;
        if (brightness == null && lightState != null) {
            if (!lightState.isOn()) {
                brightness = 0;
            } else {
                brightness = lightState.getBrightness();
            }
        }
        return brightness;
    }

    private StateUpdate createBrightnessStateUpdate(int currentBrightness, int newBrightness) {
        StateUpdate lightUpdate = new StateUpdate();
        if (newBrightness == 0) {
            lightUpdate.turnOff();
        } else {
            lightUpdate.setBrightness(newBrightness);
            if (currentBrightness == 0) {
                lightUpdate.turnOn();
            }
        }
        return lightUpdate;
    }

    private synchronized HueBridgeHandler getHueBridgeHandler() {
        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof HueBridgeHandler) {
                this.bridgeHandler = (HueBridgeHandler) handler;
                this.bridgeHandler.registerLightStatusListener(this);
            } else {
                return null;
            }
        }
        return this.bridgeHandler;
    }

    @Override
    public void onLightStateChanged(HueBridge bridge, FullLight fullLight) {
        logger.trace("onLightStateChanged() was called");

        if (!fullLight.getId().equals(lightId)) {
            logger.trace("Received state change for another handler's light ({}). Will be ignored.", fullLight.getId());
            return;
        }

        initializeProperties();

        lastSentColorTemp = null;
        lastSentBrightness = null;

        // update status (ONLINE, OFFLINE)
        if (fullLight.getState().isReachable()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            // we assume OFFLINE without any error (NONE), as this is an
            // expected state (when bulb powered off)
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.light-not-reachable");
        }

        HSBType hsbType = LightStateConverter.toHSBType(fullLight.getState());
        if (!fullLight.getState().isOn()) {
            hsbType = new HSBType(hsbType.getHue(), hsbType.getSaturation(), new PercentType(0));
        }
        updateState(CHANNEL_COLOR, hsbType);

        PercentType percentType = LightStateConverter.toColorTemperaturePercentType(fullLight.getState());
        updateState(CHANNEL_COLORTEMPERATURE, percentType);

        percentType = LightStateConverter.toBrightnessPercentType(fullLight.getState());
        if (!fullLight.getState().isOn()) {
            percentType = new PercentType(0);
        }
        updateState(CHANNEL_BRIGHTNESS, percentType);

        if (fullLight.getState().isOn()) {
            updateState(CHANNEL_SWITCH, OnOffType.ON);
        } else {
            updateState(CHANNEL_SWITCH, OnOffType.OFF);
        }

        StringType stringType = LightStateConverter.toAlertStringType(fullLight.getState());
        if (!stringType.toString().equals("NULL")) {
            updateState(CHANNEL_ALERT, stringType);
            scheduleAlertStateRestore(stringType);
        }

    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        HueBridgeHandler handler = getHueBridgeHandler();
        if (handler != null) {
            FullLight light = handler.getLightById(lightId);
            if (light != null) {
                onLightStateChanged(null, light);
            }
        }
    }

    @Override
    public void onLightRemoved(HueBridge bridge, FullLight light) {
        if (light.getId().equals(lightId)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "offline.light-removed");
        }
    }

    @Override
    public void onLightAdded(HueBridge bridge, FullLight light) {
        if (light.getId().equals(lightId)) {
            onLightStateChanged(bridge, light);
        }
    }

    /**
     * Schedules restoration of the alert item state to {@link LightStateConverter#ALERT_MODE_NONE} after a given time.
     * <br>
     * Based on the initial command:
     * <ul>
     * <li>For {@link LightStateConverter#ALERT_MODE_SELECT} restoration will be triggered after <strong>2
     * seconds</strong>.
     * <li>For {@link LightStateConverter#ALERT_MODE_LONG_SELECT} restoration will be triggered after <strong>15
     * seconds</strong>.
     * </ul>
     * This method also cancels any previously scheduled restoration.
     *
     * @param command
     *            The {@link Command} sent to the item
     */
    private void scheduleAlertStateRestore(Command command) {
        cancelScheduledFuture();
        int delay = getAlertDuration(command);

        if (delay > 0) {
            scheduledFuture = scheduler.schedule(() -> {
                updateState(CHANNEL_ALERT, new StringType(LightStateConverter.ALERT_MODE_NONE));
            }, delay, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * This method will cancel previously scheduled alert item state
     * restoration.
     */
    private void cancelScheduledFuture() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
    }

    /**
     * This method returns the time in <strong>milliseconds</strong> after
     * which, the state of the alert item has to be restored to {@link LightStateConverter#ALERT_MODE_NONE}.
     *
     * @param command
     *            The initial command sent to the alert item.
     * @return Based on the initial command will return:
     *         <ul>
     *         <li><strong>2000</strong> for {@link LightStateConverter#ALERT_MODE_SELECT}.
     *         <li><strong>15000</strong> for {@link LightStateConverter#ALERT_MODE_LONG_SELECT}.
     *         <li><strong>-1</strong> for any command different from the previous two.
     *         </ul>
     */
    private int getAlertDuration(Command command) {
        int delay;
        switch (command.toString()) {
            case LightStateConverter.ALERT_MODE_LONG_SELECT:
                delay = 15000;
                break;
            case LightStateConverter.ALERT_MODE_SELECT:
                delay = 2000;
                break;
            default:
                delay = -1;
                break;
        }

        return delay;
    }
}