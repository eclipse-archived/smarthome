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

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
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
 */
public class HueLightHandler extends BaseThingHandler implements LightStatusListener {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_LCT001, THING_TYPE_LCT002,
            THING_TYPE_LCT003, THING_TYPE_LLC001, THING_TYPE_LLC006, THING_TYPE_LLC007, THING_TYPE_LLC010,
            THING_TYPE_LLC011, THING_TYPE_LLC012, THING_TYPE_LLC013, THING_TYPE_LWL001, THING_TYPE_LST001,
            THING_TYPE_LCT003, THING_TYPE_LWB004, THING_TYPE_CLASSIC_A60_RGBW, THING_TYPE_SURFACE_LIGHT_TW,
            THING_TYPE_ZLL_LIGHT, THING_TYPE_LLC020);

    private static final int DIM_STEPSIZE = 30;

    private String lightId;

    private Integer lastSentColorTemp;
    private Integer lastSentBrightness;

    private Logger logger = LoggerFactory.getLogger(HueLightHandler.class);

    private HueBridgeHandler bridgeHandler;

    public HueLightHandler(Thing hueLight) {
        super(hueLight);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing hue light handler.");
        final String configLightId = (String) getConfig().get(LIGHT_ID);
        if (configLightId != null) {
            lightId = configLightId;
            // note: this call implicitly registers our handler as a listener on the bridge
            if (getHueBridgeHandler() != null) {
                ThingStatusInfo statusInfo = getBridge().getStatusInfo();
                updateStatus(statusInfo.getStatus(), statusInfo.getStatusDetail(), statusInfo.getDescription());
                FullLight fullLight = getLight();
                if (fullLight != null) {
                    updateProperty(Thing.PROPERTY_FIRMWARE_VERSION, fullLight.getSoftwareVersion());
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
                    lightState = LightStateConverter.toColorLightState((OnOffType) command);
                } else if (command instanceof IncreaseDecreaseType) {
                    Integer colorTemp = lastSentColorTemp;
                    if (colorTemp == null) {
                        State currentState = light.getState();
                        if (currentState != null) {
                            colorTemp = currentState.getColorTemperature();
                        }
                    }
                    if (colorTemp != null) {
                        if (command == IncreaseDecreaseType.DECREASE) {
                            colorTemp -= DIM_STEPSIZE;
                            if (colorTemp < 0)
                                colorTemp = 0;
                        } else {
                            colorTemp += DIM_STEPSIZE;
                            if (colorTemp > 255)
                                colorTemp = 255;
                        }
                        lastSentColorTemp = colorTemp;
                        lightState = new StateUpdate().setColorTemperature(colorTemp);
                    }
                }
                break;
            case CHANNEL_BRIGHTNESS:
                if (command instanceof PercentType) {
                    lightState = LightStateConverter.toColorLightState((PercentType) command);
                } else if (command instanceof OnOffType) {
                    lightState = LightStateConverter.toColorLightState((OnOffType) command);
                } else if (command instanceof IncreaseDecreaseType) {
                    Integer brightness = lastSentBrightness;
                    if (brightness == null) {
                        State currentState = light.getState();
                        if (currentState != null) {
                            if (!currentState.isOn()) {
                                brightness = 0;
                            } else {
                                brightness = currentState.getBrightness();
                            }
                        }
                    }
                    if (brightness != null) {
                        lightState = new StateUpdate();
                        if (command == IncreaseDecreaseType.DECREASE) {
                            brightness -= DIM_STEPSIZE;
                            if (brightness < 1) {
                                brightness = 1;
                                lightState = lightState.setOn(false);
                            }
                        } else {
                            brightness += DIM_STEPSIZE;
                            if (brightness > 254) {
                                brightness = 254;
                            }
                        }
                        lastSentBrightness = brightness;
                        lightState.setBrightness(brightness);
                    }
                }
                break;
            case CHANNEL_COLOR:
                if (command instanceof HSBType) {
                    HSBType hsbCommand = (HSBType) command;
                    if (hsbCommand.getBrightness().intValue() == 0) {
                        lightState = LightStateConverter.toColorLightState(OnOffType.OFF);
                    } else {
                        lightState = LightStateConverter.toColorLightState(hsbCommand);
                    }
                } else if (command instanceof PercentType) {
                    lightState = LightStateConverter.toColorLightState((PercentType) command);
                } else if (command instanceof OnOffType) {
                    lightState = LightStateConverter.toColorLightState((OnOffType) command);
                } else if (command instanceof IncreaseDecreaseType) {
                    Integer brightness = lastSentBrightness;
                    if (brightness == null) {
                        State currentState = light.getState();
                        if (currentState != null) {
                            if (!currentState.isOn()) {
                                brightness = 0;
                            } else {
                                brightness = currentState.getBrightness();
                            }
                        }
                    }
                    if (brightness != null) {
                        if (command == IncreaseDecreaseType.DECREASE) {
                            brightness -= DIM_STEPSIZE;
                            if (brightness < 0)
                                brightness = 0;
                        } else {
                            brightness += DIM_STEPSIZE;
                            if (brightness > 255)
                                brightness = 255;
                        }
                        lastSentBrightness = brightness;
                        lightState = new StateUpdate().setBrightness(brightness);
                        if (brightness == 0) {
                            lightState = lightState.setOn(false);
                        }
                    }
                }
                break;
        }
        if (lightState != null) {
            hueBridge.updateLightState(light, lightState);
        } else {
            logger.warn("Command send to an unknown channel id: " + channelUID);
        }
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
        if (fullLight.getId().equals(lightId)) {
            lastSentColorTemp = null;
            lastSentBrightness = null;
            
            // update status (ONLINE, OFFLINE)
            if (fullLight.getState().isReachable()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                // we assume OFFLINE without any error, as this is an 
                // expected state (when bulb powered off)
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Bridge reports light as reachable=false");
            }

            HSBType hsbType = LightStateConverter.toHSBType(fullLight.getState());
            if (!fullLight.getState().isOn()) {
                hsbType = new HSBType(hsbType.getHue(), hsbType.getSaturation(), new PercentType(0));
            }
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_COLOR), hsbType);

            PercentType percentType = LightStateConverter.toColorTemperaturePercentType(fullLight.getState());
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_COLORTEMPERATURE), percentType);

            percentType = LightStateConverter.toBrightnessPercentType(fullLight.getState());
            if (!fullLight.getState().isOn()) {
                percentType = new PercentType(0);
            }
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_BRIGHTNESS), percentType);
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

}