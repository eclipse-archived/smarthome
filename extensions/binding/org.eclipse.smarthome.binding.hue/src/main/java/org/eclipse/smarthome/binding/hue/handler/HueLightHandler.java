/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.handler;

import static org.eclipse.smarthome.binding.hue.HueBindingConstants.*;

import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import nl.q42.jue.FullLight;
import nl.q42.jue.HueBridge;
import nl.q42.jue.State;
import nl.q42.jue.StateUpdate;

/**
 * {@link HueLightHandler} is the handler for a hue light. It uses the {@link HueBridgeHandler} to execute the actual
 * command.
 *
 * @author Dennis Nobel - Initial contribution of hue binding
 * @author Oliver Libutzki
 * @author Kai Kreuzer - stabilized code
 * @author Andre Fuechsel - implemented switch off when brightness == 0
 * @author Thomas Höfer - added thing properties
 * @author Jochen Hiller - fixed status updates for reachable=true/false
 * @author Markus Mazurczak - added code for command handling of OSRAM PAR16 50
 *         bulbs
 * @author Yordan Zhelev - added alert and effect functions
 *
 */
public class HueLightHandler extends BaseThingHandler implements LightStatusListener {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_LCT001, THING_TYPE_LCT002,
            THING_TYPE_LCT003, THING_TYPE_LCT007, THING_TYPE_LLC001, THING_TYPE_LLC006, THING_TYPE_LLC007,
            THING_TYPE_LLC010, THING_TYPE_LLC011, THING_TYPE_LLC012, THING_TYPE_LLC013, THING_TYPE_LWL001,
            THING_TYPE_LST001, THING_TYPE_LST002, THING_TYPE_LCT003, THING_TYPE_LWB004, THING_TYPE_LWB006,
            THING_TYPE_LWB007, THING_TYPE_CLASSIC_A60_RGBW, THING_TYPE_SURFACE_LIGHT_TW, THING_TYPE_ZLL_LIGHT,
            THING_TYPE_LLC020, THING_TYPE_PAR16_50_TW, THING_TYPE_FLEX_RGBW);

    private String lightId;

    private Integer lastSentColorTemp;
    private Integer lastSentBrightness;

    private Logger logger = LoggerFactory.getLogger(HueLightHandler.class);

    // Flag to indicate whether the bulb is of type Osram par16 50 TW or not
    private boolean isOsramPar16 = false;

    private HueBridgeHandler bridgeHandler;

    ScheduledFuture<?> scheduledFuture;

    public HueLightHandler(Thing hueLight) {
        super(hueLight);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing hue light handler.");
        final String configLightId = (String) getConfig().get(LIGHT_ID);
        if (configLightId != null) {
            lightId = configLightId;
            // note: this call implicitly registers our handler as a listener on
            // the bridge
            if (getHueBridgeHandler() != null) {
                ThingStatusInfo statusInfo = getBridge().getStatusInfo();
                updateStatus(statusInfo.getStatus(), statusInfo.getStatusDetail(), statusInfo.getDescription());
                FullLight fullLight = getLight();
                if (fullLight != null) {
                    updateProperty(Thing.PROPERTY_FIRMWARE_VERSION, fullLight.getSoftwareVersion());
                    isOsramPar16 = THING_TYPE_PAR16_50_TW.equals(getThing().getThingTypeUID());
                }
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposes. Unregistering listener.");
        if (lightId != null) {
            HueBridgeHandler bridgeHandler = getHueBridgeHandler();
            if (bridgeHandler != null) {
                getHueBridgeHandler().unregisterLightStatusListener(this);
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
            logger.warn("Command send to an unknown channel id: " + channelUID);
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
        if (fullLight != null && fullLight.getId().equals(lightId)) {
            lastSentColorTemp = null;
            lastSentBrightness = null;

            // update status (ONLINE, OFFLINE)
            if (fullLight.getState().isReachable()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                // we assume OFFLINE without any error (NONE), as this is an
                // expected state (when bulb powered off)
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Bridge reports light as not reachable");
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

            StringType stringType = LightStateConverter.toAlertStringType(fullLight.getState());
            if (!stringType.toString().equals("NULL")) {
                updateState(CHANNEL_ALERT, stringType);
                scheduleAlertStateRestore(stringType);
            }
        }

    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        HueBridgeHandler handler = getHueBridgeHandler();
        if (handler != null) {
            onLightStateChanged(null, handler.getLightById(lightId));
        }
    }

    @Override
    public void onLightRemoved(HueBridge bridge, FullLight light) {
        if (light.getId().equals(lightId)) {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void onLightAdded(HueBridge bridge, FullLight light) {
        if (light.getId().equals(lightId)) {
            updateStatus(ThingStatus.ONLINE);
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
            scheduledFuture = scheduler.schedule(new Runnable() {

                @Override
                public void run() {
                    updateState(CHANNEL_ALERT, new StringType(LightStateConverter.ALERT_MODE_NONE));
                }
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