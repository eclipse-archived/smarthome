/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.handler;

import static org.eclipse.smarthome.binding.lifx.LifxBindingConstants.CHANNEL_COLOR;
import lifx.java.android.entities.LFXHSBKColor;
import lifx.java.android.entities.LFXTypes.LFXPowerState;
import lifx.java.android.light.LFXLight;
import lifx.java.android.light.LFXLight.LFXLightListener;
import lifx.java.android.light.LFXTaggedLightCollection;
import lifx.java.android.network_context.LFXNetworkContext;
import lifx.java.android.network_context.LFXNetworkContext.LFXNetworkContextListener;

import org.eclipse.smarthome.binding.lifx.LifxBindingConstants;
import org.eclipse.smarthome.binding.lifx.internal.LifxConnection;
import org.eclipse.smarthome.binding.lifx.internal.LifxConnection.LifxLightTracker;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LifxLightHandler} is responsible for handling commands, which are
 * sent to one of the light channels.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Stefan Bußweiler - Added new thing status handling 
 */
public class LifxLightHandler extends BaseThingHandler implements LifxLightTracker, LFXLightListener,
        LFXNetworkContextListener {

    private static final double INCREASE_DECREASE_STEP = 0.10;
    private LFXHSBKColor currentColorState;
    private LFXPowerState currentPowerState;
    private String deviceId;
    private LifxConnection lifxConnection;
    private LFXLight light;
    private Logger logger = LoggerFactory.getLogger(LifxLightHandler.class);

    public LifxLightHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void dispose() {
        lifxConnection.removeLightTracker(this);
        lifxConnection.getNetworkContext().removeNetworkContextListener(this);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (this.light == null) {
            logger.warn("Cannot handle command: Light with device ID '{}' is not yet connected to LIFX network.",
                    this.deviceId);
            return;
        }
        if (getThing().getStatusInfo().getStatus() != ThingStatus.ONLINE) {
            logger.warn("Cannot handle command: No connection to LIFX network.");
            return;
        }
        try {
            switch (channelUID.getId()) {
                case CHANNEL_COLOR:
                    if (command instanceof HSBType) {
                        handleHSBCommand((HSBType) command);
                    } else if (command instanceof PercentType) {
                        handlePercentCommand((PercentType) command);
                    } else if (command instanceof OnOffType) {
                        handleOnOffCommand((OnOffType) command);
                    } else if (command instanceof IncreaseDecreaseType) {
                        handleIncreaseDecreaseCommand((IncreaseDecreaseType) command);
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            logger.error("Error while updating light.", ex);
        }
    }

    @Override
    public void initialize() {
        try {
            this.deviceId = (String) getConfig().get(LifxBindingConstants.CONFIG_PROPERTY_DEVICE_ID);
            logger.debug("Initializing LIFX handler for light '{}'.", this.deviceId);
            this.lifxConnection = LifxConnection.getInstance();
            this.lifxConnection.addLightTracker(this);
            this.lifxConnection.getNetworkContext().addNetworkContextListener(this);
        } catch (Exception ex) {
            logger.error("Error occured while initializing LIFX handler: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void lightAdded(LFXLight light) {
        if (isConfiguredLight(light)) {
            logger.debug("LIFX light '{}' added. Handler is now ready to receive commands", light.getDeviceID());
            this.light = light;
            this.light.addLightListener(this);
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void lightDidChangeColor(LFXLight light, LFXHSBKColor color) {
        this.currentColorState = color;
        if (this.currentPowerState != LFXPowerState.OFF) {
            updateState(CHANNEL_COLOR, toHSBType(color));
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void lightDidChangeLabel(LFXLight light, String label) {
        // nothing to do
    }

    @Override
    public void lightDidChangePowerState(LFXLight light, LFXPowerState powerState) {
        this.currentPowerState = powerState;
        if (powerState == LFXPowerState.OFF) {
            updateState(CHANNEL_COLOR, OnOffType.OFF);
        } else if (this.currentColorState != null) {
            updateState(CHANNEL_COLOR, toHSBType(this.currentColorState));
        } else {
            updateState(CHANNEL_COLOR, OnOffType.ON);
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void lightRemoved(LFXLight light) {
        if (isConfiguredLight(light) && this.light != null) {
            this.light.removeLightListener(this);
            this.light = null;
            logger.debug("LIFX handler light removed: {} - '{}'", light.getDeviceID(), light.getLabel());
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void networkContextDidAddTaggedLightCollection(LFXNetworkContext arg0, LFXTaggedLightCollection arg1) {
        // nothing to do
    }

    @Override
    public void networkContextDidConnect(LFXNetworkContext arg0) {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void networkContextDidDisconnect(LFXNetworkContext arg0) {
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void networkContextDidRemoveTaggedLightCollection(LFXNetworkContext arg0, LFXTaggedLightCollection arg1) {
        // nothing to do
    }

    private void handleHSBCommand(HSBType hsbType) {
        light.setColor(toLFXHSBKColor(hsbType));
        if (currentPowerState != LFXPowerState.ON) {
            light.setPowerState(LFXPowerState.ON);
        }
    }

    private void handleIncreaseDecreaseCommand(IncreaseDecreaseType increaseDecreaseType) {
        if (currentColorState != null) {
            float hue = currentColorState.getHue();
            float satuation = currentColorState.getSaturation();
            float brightness = currentColorState.getBrightness();
            if (increaseDecreaseType == IncreaseDecreaseType.INCREASE) {
                brightness = (float) (brightness + INCREASE_DECREASE_STEP);
                if (brightness > 1) {
                    brightness = 1;
                }
                light.setColor(LFXHSBKColor.getColor(hue, satuation, brightness, 0));
            }
            if (increaseDecreaseType == IncreaseDecreaseType.DECREASE) {
                brightness = (float) (brightness - INCREASE_DECREASE_STEP);
                if (brightness < 0) {
                    brightness = 0;
                }
                light.setColor(LFXHSBKColor.getColor(hue, satuation, brightness, 0));
            }
        }
    }

    private void handleOnOffCommand(OnOffType onOffType) {
        LFXPowerState lfxPowerState = onOffType == OnOffType.ON ? LFXPowerState.ON : LFXPowerState.OFF;
        light.setPowerState(lfxPowerState);
    }

    private void handlePercentCommand(PercentType percentType) {
        light.setColor(toLFXHSBKColor(percentType));
        if (currentPowerState != LFXPowerState.ON) {
            light.setPowerState(LFXPowerState.ON);
        }
    }

    private boolean isConfiguredLight(LFXLight light) {
        return light.getDeviceID().equals(this.deviceId);
    }

    private HSBType toHSBType(LFXHSBKColor color) {
        DecimalType hue = new DecimalType(color.getHue());
        PercentType saturation = new PercentType((int) (color.getSaturation() * 100));
        PercentType brightness = new PercentType((int) (color.getBrightness() * 100));
        HSBType newState = new HSBType(hue, saturation, brightness);
        return newState;
    }

    private LFXHSBKColor toLFXHSBKColor(HSBType color) {
        LFXHSBKColor lfxColor = LFXHSBKColor.getColor(color.getHue().intValue(),
                color.getSaturation().floatValue() / 100, color.getBrightness().floatValue() / 100, 0);
        return lfxColor;
    }

    private LFXHSBKColor toLFXHSBKColor(PercentType percentType) {
        float dimLevel = percentType.floatValue() / 100;
        LFXHSBKColor lfxColor = LFXHSBKColor.getColor(light.getColor().getHue(), light.getColor().getSaturation(),
                dimLevel, 0);
        return lfxColor;
    }

}
