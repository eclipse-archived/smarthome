/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.hue.handler;

import static org.eclipse.smarthome.binding.hue.HueBindingConstants.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.hue.HueBindingConstants;
import org.eclipse.smarthome.binding.hue.internal.HueBridge;
import org.eclipse.smarthome.binding.hue.internal.LightStateConverter;
import org.eclipse.smarthome.binding.hue.internal.LightStatusListener;
import org.eclipse.smarthome.binding.hue.internal.dto.Light;
import org.eclipse.smarthome.binding.hue.internal.dto.LightState;
import org.eclipse.smarthome.binding.hue.internal.dto.LightState.AlertMode;
import org.eclipse.smarthome.binding.hue.internal.dto.LightState.ColorMode;
import org.eclipse.smarthome.binding.hue.internal.dto.updates.LightStateUpdate;
import org.eclipse.smarthome.binding.hue.internal.exceptions.DeviceOffException;
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
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HueLightHandler} is the handler for a hue light.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Oliver Libutzki
 * @author Kai Kreuzer - stabilized code
 * @author Andre Fuechsel - switch off when brightness == 0, generic thing types, properties initialization
 * @author Thomas Höfer - added thing properties
 * @author Jochen Hiller - fixed status updates for reachable=true/false
 * @author Markus Mazurczak - code for command handling of OSRAM PAR16 50 bulbs
 * @author Yordan Zhelev - added alert and effect functions
 * @author Denis Dudnik - switched to internally integrated source of Jue library
 * @author Christoph Weitkamp - Added support for bulbs using CIE XY colormode only
 */
@NonNullByDefault
public class HueLightHandler extends BaseThingHandler implements LightStatusListener {
    private final Logger logger = LoggerFactory.getLogger(HueLightHandler.class);

    /** type Osram par16 50 TW */
    private boolean isOsramPar16 = false;
    private @NonNullByDefault({}) HueBridge hueBridge;
    protected @Nullable ScheduledFuture<?> scheduledFuture;
    protected @Nullable Light light;
    protected HueLightHandlerConfig config = new HueLightHandlerConfig();
    protected @Nullable LightStateUpdate accumulatedLightState = null;

    public HueLightHandler(Thing hueLight) {
        super(hueLight);
    }

    @Override
    public void initialize() {
        config = getConfigAs(HueLightHandlerConfig.class);
        Bridge bridge = getBridge();
        bridgeStatusChanged(
                (bridge == null) ? new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, null)
                        : bridge.getStatusInfo());
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        Bridge bridge = getBridge();
        if (bridge == null) {
            return;
        }
        HueBridgeHandler handler = (HueBridgeHandler) bridge.getHandler();
        if (handler == null) {
            return;
        }
        this.hueBridge = handler.getHueBridge();
        hueBridge.registerLightStatusListener(this);

        Light light = hueBridge.getLightById(config.lightId);
        this.light = light;
        if (light == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.light-removed");
            return;
        }

        String modelId = light.modelid.replaceAll(HueBindingConstants.NORMALIZE_ID_REGEX, "_");
        isOsramPar16 = HueBindingConstants.OSRAM_PAR16_50_TW_MODEL_ID.equals(modelId);

        Map<String, String> properties = editProperties();
        properties.put(Thing.PROPERTY_MODEL_ID, modelId);
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, light.swversion);
        for (String vendor : HueBindingConstants.VENDOR_MODEL_MAP.keySet()) {
            if (HueBindingConstants.VENDOR_MODEL_MAP.get(vendor).contains(modelId)) {
                properties.put(Thing.PROPERTY_VENDOR, vendor);
                break;
            }
        }
        properties.put(HueLightHandlerConfig.LIGHT_UNIQUE_ID, light.uniqueid);
        updateProperties(properties);

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
        if (hueBridge != null) {
            hueBridge.unregisterLightStatusListener(this);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        final Light light = this.light;
        if (light == null) {
            logger.debug("hue light not known on bridge. Cannot handle command.");
            return;
        }

        if (command instanceof RefreshType) {
            updateChannelsState(light);
            return;
        }

        LightStateUpdate lightState = accumulatedLightState;
        if (lightState == null) {
            lightState = new LightStateUpdate();
            this.accumulatedLightState = lightState;
        }

        switch (channelUID.getId()) {
            case CHANNEL_COLORTEMPERATURE:
                if (command instanceof PercentType) {
                    lightState = LightStateConverter.toColorTemperatureLightState(lightState, (PercentType) command);
                    updateLightState(lightState);
                    return;
                } else if (command instanceof OnOffType) {
                    lightState = osramWrap(LightStateConverter.toOnOffLightState(lightState, (OnOffType) command),
                            command);
                    updateLightState(lightState);
                    return;
                } else if (command instanceof IncreaseDecreaseType) {
                    lightState.ct = LightStateConverter.toAdjustedColorTemp((IncreaseDecreaseType) command,
                            light.state.ct);
                    // Do not send this state update if light is off, accumulate with another command
                    if (!light.state.on) {
                        return;
                    }
                }
                break;
            case CHANNEL_BRIGHTNESS:
                if (command instanceof PercentType) {
                    lightState = LightStateConverter.toBrightnessLightState(lightState, (PercentType) command);
                    updateLightState(lightState);
                    return;
                } else if (command instanceof OnOffType) {
                    lightState = osramWrap(LightStateConverter.toOnOffLightState(lightState, (OnOffType) command),
                            command);
                    updateLightState(lightState);
                    return;
                } else if (command instanceof IncreaseDecreaseType) {
                    lightState = LightStateConverter.toBrightnessLightStateRel(lightState,
                            (IncreaseDecreaseType) command, light);
                    updateLightState(lightState);
                    return;
                }
                break;
            case CHANNEL_SWITCH:
                if (command instanceof OnOffType) {
                    lightState = osramWrap(LightStateConverter.toOnOffLightState(lightState, (OnOffType) command),
                            command);
                    updateLightState(lightState);
                    return;
                }
                break;
            case CHANNEL_COLOR:
                if (command instanceof HSBType) {
                    HSBType hsbCommand = (HSBType) command;
                    if (hsbCommand.getBrightness().intValue() == 0) {
                        lightState = LightStateConverter.toOnOffLightState(lightState, OnOffType.OFF);
                    } else {
                        lightState = LightStateConverter.toColorLightState(lightState, hsbCommand, light.state);
                    }
                    updateLightState(lightState);
                    return;
                } else if (command instanceof PercentType) {
                    lightState = LightStateConverter.toBrightnessLightState(lightState, (PercentType) command);
                    updateLightState(lightState);
                    return;
                } else if (command instanceof OnOffType) {
                    lightState = LightStateConverter.toOnOffLightState(lightState, (OnOffType) command);
                    updateLightState(lightState);
                    return;
                } else if (command instanceof IncreaseDecreaseType) {
                    lightState = LightStateConverter.toBrightnessLightStateRel(lightState,
                            (IncreaseDecreaseType) command, light);
                    updateLightState(lightState);
                    return;
                }
                break;
            case CHANNEL_ALERT:
                if (command instanceof StringType) {
                    try {
                        lightState = LightStateConverter.toAlertState(lightState, (StringType) command);
                        scheduleAlertStateRestore(lightState.alert);
                        updateLightState(lightState);
                        return;
                    } catch (IllegalStateException e) {
                        logger.warn("Unsupported String command: {}. Supported commands are: {}, {}, {} ", command,
                                LightStateConverter.ALERT_MODE_NONE, LightStateConverter.ALERT_MODE_SELECT,
                                LightStateConverter.ALERT_MODE_LONG_SELECT);
                        return;
                    }
                }
                break;
            case CHANNEL_EFFECT:
                if (command instanceof OnOffType) {
                    lightState = LightStateConverter.toOnOffEffectState(lightState, (OnOffType) command);
                    updateLightState(lightState);
                    return;
                }
                break;
            default:
                logger.warn("Command sent to an unknown channel id: {}", channelUID);
                return;
        }
        logger.warn("Command {} not applicable for channel id: {}", command, channelUID);
    }

    /**
     * Performs a hue bridge API call to update the light state of this light.
     *
     * @param LightStateUpdate The light state updates
     */
    public void updateLightState(LightStateUpdate LightStateUpdate) {
        Light light = this.light;
        if (light == null) {
            return;
        }
        accumulatedLightState = null; // Light state send -> reset accumulation update state
        hueBridge.setLightState(light, LightStateUpdate).exceptionally(e -> {
            // Cannot apply value -> Device is off
            if (e.getCause() instanceof DeviceOffException) {
                // If there is only a change of the color temperature, we do not want the light
                // to be turned on (i.e. change its brightness).
                if (LightStateUpdate.ct != null && LightStateUpdate.bri == null && LightStateUpdate.on == null) {
                    return null;
                } else {
                    return LightStateConverter.toOnOffLightState(LightStateUpdate, OnOffType.ON);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                return null;
            }
        }).thenCompose(intermediateLightStateUpdate -> {
            if (intermediateLightStateUpdate != null) {
                // First turn on, then apply value
                return hueBridge.setLightState(light, intermediateLightStateUpdate)
                        .thenCompose(v -> hueBridge.setLightState(light, LightStateUpdate));
            } else {
                return CompletableFuture.completedFuture(null);
            }
        }).exceptionally(e -> {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return null;
        });
    }

    /*
     * Applies additional {@link LightStateUpdate} commands as a workaround for Osram
     * Lightify PAR16 TW firmware bug. Also see
     * http://www.everyhue.com/vanilla/discussion/1756/solved-lightify-turning-off
     */
    private LightStateUpdate osramWrap(LightStateUpdate lightState, Command actionType) {
        if (!isOsramPar16) {
            return lightState;
        }

        if (OnOffType.ON == (OnOffType) actionType) {
            lightState.bri = 254;
        } else {
            lightState.transitiontime = 0;
        }
        return lightState;
    }

    @Override
    public void onLightStateChanged(Light newLightValue) {
        if (!newLightValue.id.equals(config.lightId)) {
            return;
        }
        updateChannelsState(newLightValue);
    }

    @Override
    public void onLightRemoved(Light light) {
        if (light.id.equals(config.lightId)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "offline.light-removed");
        }
    }

    @Override
    public void onLightAdded(Light light) {
        if (light.id.equals(config.lightId)) {
            updateChannelsState(light);
        }
    }

    private void updateChannelsState(Light newLightValue) {
        this.light = newLightValue;
        this.accumulatedLightState = null;
        final LightState state = newLightValue.state;

        if (state.reachable) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.light-not-reachable");
        }

        HSBType hsbType = LightStateConverter.toHSBType(state);
        if (!state.on) {
            hsbType = new HSBType(hsbType.getHue(), hsbType.getSaturation(), new PercentType(0));
        }
        updateState(CHANNEL_COLOR, hsbType);

        ColorMode colorMode = state.colormode;
        if (ColorMode.ct.equals(colorMode)) {
            PercentType colorTempPercentType = LightStateConverter.toColorTemperaturePercentType(state);
            updateState(CHANNEL_COLORTEMPERATURE, colorTempPercentType);
        } else {
            updateState(CHANNEL_COLORTEMPERATURE, UnDefType.NULL);
        }

        PercentType brightnessPercentType = LightStateConverter.toBrightnessPercentType(state);
        if (!state.on) {
            brightnessPercentType = new PercentType(0);
        }
        updateState(CHANNEL_BRIGHTNESS, brightnessPercentType);

        if (state.on) {
            updateState(CHANNEL_SWITCH, OnOffType.ON);
        } else {
            updateState(CHANNEL_SWITCH, OnOffType.OFF);
        }

        if (state.alert != null) {
            updateState(CHANNEL_ALERT, new StringType(state.alert.name()));
            scheduleAlertStateRestore(state.alert);
        }
    }

    /**
     * Schedules restoration of the alert item state to {@link LightStateConverter#ALERT_MODE_NONE} after a given time.
     * <br>
     * Based on the initial command:
     * <ul>
     * <li>For "select" restoration will be triggered after <strong>2 seconds</strong>.
     * <li>For "lselect" restoration will be triggered after <strong>15 seconds</strong>.
     * </ul>
     * This method also cancels any previously scheduled restoration.
     *
     * @param alert The {@link Command} sent to the item
     */
    private void scheduleAlertStateRestore(@Nullable AlertMode alert) {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
        int delay = LightState.getAlertDuration(alert);

        if (delay > 0) {
            scheduledFuture = scheduler.schedule(
                    () -> updateState(CHANNEL_ALERT, new StringType(LightStateConverter.ALERT_MODE_NONE)), delay,
                    TimeUnit.MILLISECONDS);
        }
    }
}
