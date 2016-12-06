/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.handler;

import static org.eclipse.smarthome.binding.lifx.LifxBindingConstants.*;
import static org.eclipse.smarthome.binding.lifx.internal.LifxUtils.*;

import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.smarthome.binding.lifx.LifxBindingConstants;
import org.eclipse.smarthome.binding.lifx.internal.LifxLightCommunicationHandler;
import org.eclipse.smarthome.binding.lifx.internal.LifxLightCurrentStateUpdater;
import org.eclipse.smarthome.binding.lifx.internal.LifxLightOnlineStateUpdater;
import org.eclipse.smarthome.binding.lifx.internal.LifxLightState;
import org.eclipse.smarthome.binding.lifx.internal.LifxLightStateChanger;
import org.eclipse.smarthome.binding.lifx.internal.fields.MACAddress;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetLightPowerRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.Packet;
import org.eclipse.smarthome.binding.lifx.internal.protocol.PowerState;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LifxLightHandler} is responsible for handling commands, which are
 * sent to one of the light channels.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Stefan Bußweiler - Added new thing status handling
 * @author Karel Goderis - Rewrite for Firmware V2, and remove dependency on external libraries
 * @author Kai Kreuzer - Added configurable transition time and small fixes
 * @author Wouter Born - Decomposed class into separate objects
 */
public class LifxLightHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(LifxLightHandler.class);

    private static final long FADE_TIME_DEFAULT = 300;
    private static final int MAX_STATE_CHANGE_DURATION = 2000;

    private long fadeTime = FADE_TIME_DEFAULT;
    private PercentType powerOnBrightness;

    private MACAddress macAddress = null;
    private String macAsHex;

    private ReentrantLock lock = new ReentrantLock();

    private CurrentLightState currentLightState;
    private LifxLightState pendingLightState;

    private LifxLightCommunicationHandler communicationHandler;
    private LifxLightCurrentStateUpdater currentStateUpdater;
    private LifxLightStateChanger lightStateChanger;
    private LifxLightOnlineStateUpdater onlineStateUpdater;

    public class CurrentLightState extends LifxLightState {

        public boolean isOnline() {
            return thing.getStatus() == ThingStatus.ONLINE;
        }

        public boolean isOffline() {
            return thing.getStatus() == ThingStatus.OFFLINE;
        }

        public void setOnline() {
            updateStatus(ThingStatus.ONLINE);
        }

        public void setOffline() {
            updateStatus(ThingStatus.OFFLINE);
        }

        public void setOfflineByCommunicationError() {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }

        @Override
        public void setHSB(HSBType hsb) {
            if (!isStateChangePending() || hsb.equals(pendingLightState.getHSB())) {
                if (getPowerState() == PowerState.OFF) {
                    updateState(CHANNEL_COLOR, new HSBType(hsb.getHue(), hsb.getSaturation(), PercentType.ZERO));
                    updateState(CHANNEL_BRIGHTNESS, PercentType.ZERO);
                } else if (hsb != null) {
                    updateState(CHANNEL_COLOR, hsb);
                    updateState(CHANNEL_BRIGHTNESS, hsb.getBrightness());
                }
            }
            super.setHSB(hsb);
        }

        @Override
        public void setPowerState(PowerState powerState) {
            if (!isStateChangePending() || powerState.equals(pendingLightState.getPowerState())) {
                if (powerState == PowerState.OFF) {
                    updateState(CHANNEL_COLOR,
                            new HSBType(getHSB().getHue(), getHSB().getSaturation(), PercentType.ZERO));
                    updateState(CHANNEL_BRIGHTNESS, PercentType.ZERO);
                } else if (getHSB() != null) {
                    updateState(CHANNEL_COLOR, getHSB());
                    updateState(CHANNEL_BRIGHTNESS, getHSB().getBrightness());
                } else {
                    updateState(CHANNEL_COLOR, LifxBindingConstants.DEFAULT_COLOR);
                    updateState(CHANNEL_BRIGHTNESS, LifxBindingConstants.DEFAULT_BRIGHTNESS);
                }
            }
            super.setPowerState(powerState);
        }

        @Override
        public void setTemperature(PercentType temperature) {
            if (!isStateChangePending() || temperature.equals(pendingLightState.getTemperature())) {
                updateState(CHANNEL_TEMPERATURE, temperature);
            }
            super.setTemperature(temperature);
        }
    }

    public LifxLightHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        try {
            lock.lock();

            macAddress = new MACAddress((String) getConfig().get(LifxBindingConstants.CONFIG_PROPERTY_DEVICE_ID), true);
            macAsHex = this.macAddress.getHex();

            logger.debug("Initializing the LIFX handler for bulb '{}'.", macAsHex);

            fadeTime = getFadeTime();
            powerOnBrightness = getPowerOnBrightness();

            currentLightState = new CurrentLightState();
            pendingLightState = new LifxLightState();

            communicationHandler = new LifxLightCommunicationHandler(macAddress, currentLightState);
            currentStateUpdater = new LifxLightCurrentStateUpdater(macAddress, currentLightState, communicationHandler);
            onlineStateUpdater = new LifxLightOnlineStateUpdater(macAddress, currentLightState, communicationHandler);
            lightStateChanger = new LifxLightStateChanger(macAddress, pendingLightState, communicationHandler,
                    fadeTime);

            communicationHandler.start();
            currentStateUpdater.start();
            onlineStateUpdater.start();
            lightStateChanger.start();
        } catch (Exception e) {
            logger.debug("Error occured while initializing LIFX handler: " + e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void dispose() {
        try {
            lock.lock();

            if (communicationHandler != null) {
                communicationHandler.stop();
                communicationHandler = null;
            }

            if (currentStateUpdater != null) {
                currentStateUpdater.stop();
                currentStateUpdater = null;
            }

            if (onlineStateUpdater != null) {
                onlineStateUpdater.stop();
                onlineStateUpdater = null;
            }

            if (lightStateChanger != null) {
                lightStateChanger.stop();
                lightStateChanger = null;
            }

            currentLightState = null;
            pendingLightState = null;

        } finally {
            lock.unlock();
        }
    }

    private long getFadeTime() {
        Object fadeCfg = getConfig().get(LifxBindingConstants.CONFIG_PROPERTY_FADETIME);
        if (fadeCfg == null) {
            return FADE_TIME_DEFAULT;
        }
        try {
            return Long.parseLong(fadeCfg.toString());
        } catch (NumberFormatException e) {
            logger.warn("Invalid value '{}' for transition time, using default instead.", fadeCfg.toString());
            return FADE_TIME_DEFAULT;
        }
    }

    private PercentType getPowerOnBrightness() {
        Channel channel = null;

        if (getThing().getThingTypeUID().equals(LifxBindingConstants.THING_TYPE_COLORLIGHT)) {
            ChannelUID channelUID = new ChannelUID(getThing().getUID(), LifxBindingConstants.CHANNEL_COLOR);
            channel = getThing().getChannel(channelUID.getId());
        } else if (getThing().getThingTypeUID().equals(LifxBindingConstants.THING_TYPE_WHITELIGHT)) {
            ChannelUID channelUID = new ChannelUID(getThing().getUID(), LifxBindingConstants.CHANNEL_BRIGHTNESS);
            channel = getThing().getChannel(channelUID.getId());
        }

        if (channel == null) {
            return null;
        }

        Configuration configuration = channel.getConfiguration();
        Object powerOnBrightness = configuration.get(LifxBindingConstants.CONFIG_PROPERTY_POWER_ON_BRIGHTNESS);
        return powerOnBrightness == null ? null : new PercentType(powerOnBrightness.toString());
    }

    private void sendPacket(Packet packet) {
        communicationHandler.sendPacket(packet);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof RefreshType) {
            GetLightPowerRequest powerPacket = new GetLightPowerRequest();
            GetRequest colorPacket = new GetRequest();

            try {
                switch (channelUID.getId()) {
                    case CHANNEL_COLOR:
                    case CHANNEL_BRIGHTNESS:
                        sendPacket(powerPacket);
                        sendPacket(colorPacket);
                        break;
                    case CHANNEL_TEMPERATURE:
                        sendPacket(colorPacket);
                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                logger.error("Error while refreshing a channel for the bulb: {}", ex.getMessage(), ex);
            }
        } else {
            try {
                switch (channelUID.getId()) {
                    case CHANNEL_COLOR:
                        if (command instanceof HSBType) {
                            handleHSBCommand((HSBType) command);
                            return;
                        } else if (command instanceof PercentType) {
                            handlePercentCommand((PercentType) command);
                        } else if (command instanceof OnOffType) {
                            handleColorOnOffCommand((OnOffType) command);
                        } else if (command instanceof IncreaseDecreaseType) {
                            handleIncreaseDecreaseCommand((IncreaseDecreaseType) command);
                        }
                        break;
                    case CHANNEL_BRIGHTNESS:
                        if (command instanceof PercentType) {
                            handlePercentCommand((PercentType) command);
                        } else if (command instanceof OnOffType) {
                            handleBrightnessOnOffCommand((OnOffType) command);
                        } else if (command instanceof IncreaseDecreaseType) {
                            handleIncreaseDecreaseCommand((IncreaseDecreaseType) command);
                        }
                        break;
                    case CHANNEL_TEMPERATURE:
                        if (command instanceof PercentType) {
                            handleTemperatureCommand((PercentType) command);
                        } else if (command instanceof IncreaseDecreaseType) {
                            handleIncreaseDecreaseTemperatureCommand((IncreaseDecreaseType) command);
                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                logger.error("Error while updating bulb: {}", ex.getMessage(), ex);
            }
        }
    }

    private LifxLightState getLightStateForCommand() {
        if (!isStateChangePending()) {
            pendingLightState.copy(currentLightState);
        }
        return pendingLightState;
    }

    private boolean isStateChangePending() {
        return pendingLightState.getMillisSinceLastChange() < MAX_STATE_CHANGE_DURATION;
    }

    private void handleTemperatureCommand(PercentType temperature) {
        logger.debug("The set temperature '{}' yields {} Kelvin", temperature, percentTypeToKelvin(temperature));
        getLightStateForCommand().setTemperature(temperature);
        if (getLightStateForCommand().getPowerState() != PowerState.ON) {
            getLightStateForCommand().setPowerState(PowerState.ON);
        }
    }

    private void handleHSBCommand(HSBType hsbType) {
        getLightStateForCommand().setHSB(hsbType);
        if (getLightStateForCommand().getPowerState() != PowerState.ON) {
            getLightStateForCommand().setPowerState(PowerState.ON);
        }
    }

    private void handlePercentCommand(PercentType percentType) {
        HSBType baseHSB = getLightStateForCommand().getHSB();
        if (baseHSB != null) {
            HSBType newHSB = new HSBType(baseHSB.getHue(), baseHSB.getSaturation(), percentType);
            handleHSBCommand(newHSB);
        }
    }

    private void handleColorOnOffCommand(OnOffType onOffType) {
        HSBType baseHSB = getLightStateForCommand().getHSB();
        if (baseHSB != null && powerOnBrightness != null) {
            PercentType percentType = onOffType == OnOffType.ON ? powerOnBrightness : new PercentType(0);
            HSBType newColorState = new HSBType(baseHSB.getHue(), baseHSB.getSaturation(), percentType);
            handleHSBCommand(newColorState);
        }
        getLightStateForCommand().setPowerState(onOffType);
    }

    private void handleBrightnessOnOffCommand(OnOffType onOffType) {
        getLightStateForCommand().setPowerState(onOffType);
    }

    private void handleIncreaseDecreaseCommand(IncreaseDecreaseType increaseDecreaseType) {
        HSBType baseHSB = getLightStateForCommand().getHSB();
        if (baseHSB != null) {
            PercentType newBrightness = increaseDecreasePercentType(increaseDecreaseType, baseHSB.getBrightness());
            handlePercentCommand(newBrightness);
        }
    }

    private void handleIncreaseDecreaseTemperatureCommand(IncreaseDecreaseType increaseDecreaseType) {
        PercentType baseTemperature = getLightStateForCommand().getTemperature();
        if (baseTemperature != null) {
            PercentType newTemperature = increaseDecreasePercentType(increaseDecreaseType, baseTemperature);
            handleTemperatureCommand(newTemperature);
        }
    }

}
