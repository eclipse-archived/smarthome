/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.handler;

import static org.eclipse.smarthome.binding.lifx.LifxBindingConstants.*;
import static org.eclipse.smarthome.binding.lifx.internal.LifxUtils.increaseDecreasePercentType;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.smarthome.binding.lifx.LifxBindingConstants;
import org.eclipse.smarthome.binding.lifx.internal.LifxChannelFactory;
import org.eclipse.smarthome.binding.lifx.internal.LifxLightCommunicationHandler;
import org.eclipse.smarthome.binding.lifx.internal.LifxLightCurrentStateUpdater;
import org.eclipse.smarthome.binding.lifx.internal.LifxLightOnlineStateUpdater;
import org.eclipse.smarthome.binding.lifx.internal.LifxLightPropertiesUpdater;
import org.eclipse.smarthome.binding.lifx.internal.LifxLightState;
import org.eclipse.smarthome.binding.lifx.internal.LifxLightStateChanger;
import org.eclipse.smarthome.binding.lifx.internal.fields.HSBK;
import org.eclipse.smarthome.binding.lifx.internal.fields.MACAddress;
import org.eclipse.smarthome.binding.lifx.internal.listener.LifxPropertiesUpdateListener;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetLightInfraredRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetLightPowerRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.GetWifiInfoRequest;
import org.eclipse.smarthome.binding.lifx.internal.protocol.Packet;
import org.eclipse.smarthome.binding.lifx.internal.protocol.PowerState;
import org.eclipse.smarthome.binding.lifx.internal.protocol.Products;
import org.eclipse.smarthome.binding.lifx.internal.protocol.SignalStrength;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
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
public class LifxLightHandler extends BaseThingHandler implements LifxPropertiesUpdateListener {

    private final Logger logger = LoggerFactory.getLogger(LifxLightHandler.class);

    private static final Duration FADE_TIME_DEFAULT = Duration.ofMillis(300);
    private static final Duration MIN_STATUS_INFO_UPDATE_INTERVAL = Duration.ofSeconds(1);
    private static final Duration MAX_STATE_CHANGE_DURATION = Duration.ofSeconds(4);

    private final LifxChannelFactory channelFactory;
    private Products product;

    private Duration fadeTime = FADE_TIME_DEFAULT;
    private PercentType powerOnBrightness;

    private MACAddress macAddress;
    private String macAsHex;

    private final ReentrantLock lock = new ReentrantLock();

    private CurrentLightState currentLightState;
    private LifxLightState pendingLightState;

    private Map<String, State> channelStates;
    private ThingStatusInfo statusInfo;
    private LocalDateTime lastStatusInfoUpdate = LocalDateTime.MIN;

    private LifxLightCommunicationHandler communicationHandler;
    private LifxLightCurrentStateUpdater currentStateUpdater;
    private LifxLightStateChanger lightStateChanger;
    private LifxLightOnlineStateUpdater onlineStateUpdater;
    private LifxLightPropertiesUpdater propertiesUpdater;

    public class CurrentLightState extends LifxLightState {

        public boolean isOnline() {
            return thing.getStatus() == ThingStatus.ONLINE;
        }

        public boolean isOffline() {
            return thing.getStatus() == ThingStatus.OFFLINE;
        }

        public void setOnline() {
            updateStatusIfChanged(ThingStatus.ONLINE);
        }

        public void setOffline() {
            updateStatusIfChanged(ThingStatus.OFFLINE);
        }

        public void setOfflineByCommunicationError() {
            updateStatusIfChanged(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }

        @Override
        public void setColors(HSBK[] colors) {
            if (!isStateChangePending() || isPendingColorStateChangesApplied(getPowerState(), colors)) {
                PowerState powerState = isStateChangePending() ? pendingLightState.getPowerState() : getPowerState();
                updateColorChannels(powerState, colors);
            }
            super.setColors(colors);
        }

        @Override
        public void setPowerState(PowerState powerState) {
            if (!isStateChangePending() || isPendingColorStateChangesApplied(powerState, getColors())) {
                HSBK[] colors = isStateChangePending() ? pendingLightState.getColors() : getColors();
                updateColorChannels(powerState, colors);
            }
            super.setPowerState(powerState);
        }

        private boolean isPendingColorStateChangesApplied(PowerState powerState, HSBK[] colors) {
            return powerState != null && powerState.equals(pendingLightState.getPowerState())
                    && Arrays.equals(colors, pendingLightState.getColors());
        }

        private void updateColorChannels(PowerState powerState, HSBK[] colors) {
            HSBK color = colors != null && colors.length > 0 ? colors[0] : null;
            HSBK updateColor = nullSafeUpdateColor(powerState, color);
            HSBType hsb = updateColor.getHSB();

            updateStateIfChanged(CHANNEL_COLOR, hsb);
            updateStateIfChanged(CHANNEL_BRIGHTNESS, hsb.getBrightness());
            updateStateIfChanged(CHANNEL_TEMPERATURE, updateColor.getTemperature());

            updateZoneChannels(powerState, colors);
        }

        private HSBK nullSafeUpdateColor(PowerState powerState, HSBK color) {
            HSBK updateColor = color != null ? color : DEFAULT_COLOR;
            if (powerState == PowerState.OFF) {
                updateColor = new HSBK(updateColor);
                updateColor.setBrightness(PercentType.ZERO);
            }
            return updateColor;
        }

        @Override
        public void setInfrared(PercentType infrared) {
            if (!isStateChangePending() || infrared.equals(pendingLightState.getInfrared())) {
                updateStateIfChanged(CHANNEL_INFRARED, infrared);
            }
            super.setInfrared(infrared);
        }

        @Override
        public void setSignalStrength(SignalStrength signalStrength) {
            updateStateIfChanged(CHANNEL_SIGNAL_STRENGTH, new DecimalType(signalStrength.toQualityRating()));
            super.setSignalStrength(signalStrength);
        }

        private void updateZoneChannels(PowerState powerState, HSBK[] colors) {
            if (!product.isMultiZone() || colors == null || colors.length == 0) {
                return;
            }

            int oldZones = getColors() != null ? getColors().length : 0;
            int newZones = colors.length;
            if (oldZones != newZones) {
                addRemoveZoneChannels(newZones);
            }

            for (int i = 0; i < colors.length; i++) {
                HSBK color = colors[i];
                HSBK updateColor = nullSafeUpdateColor(powerState, color);
                updateStateIfChanged(CHANNEL_COLOR_ZONE + i, updateColor.getHSB());
                updateStateIfChanged(CHANNEL_TEMPERATURE_ZONE + i, updateColor.getTemperature());
            }
        }

    }

    public LifxLightHandler(Thing thing, LifxChannelFactory channelFactory) {
        super(thing);
        this.channelFactory = channelFactory;
    }

    @Override
    public void initialize() {
        try {
            lock.lock();

            product = getProduct();
            macAddress = new MACAddress((String) getConfig().get(LifxBindingConstants.CONFIG_PROPERTY_DEVICE_ID), true);
            macAsHex = this.macAddress.getHex();

            logger.debug("Initializing the LIFX handler for light '{}'.", macAsHex);

            fadeTime = getFadeTime();
            powerOnBrightness = getPowerOnBrightness();

            channelStates = new HashMap<>();
            currentLightState = new CurrentLightState();
            pendingLightState = new LifxLightState();

            communicationHandler = new LifxLightCommunicationHandler(macAddress, scheduler, currentLightState);
            currentStateUpdater = new LifxLightCurrentStateUpdater(macAddress, scheduler, currentLightState,
                    communicationHandler, product);
            onlineStateUpdater = new LifxLightOnlineStateUpdater(macAddress, scheduler, currentLightState,
                    communicationHandler);
            propertiesUpdater = new LifxLightPropertiesUpdater(macAddress, scheduler, currentLightState,
                    communicationHandler);
            propertiesUpdater.addPropertiesUpdateListener(this);
            lightStateChanger = new LifxLightStateChanger(macAddress, scheduler, pendingLightState,
                    communicationHandler, product, fadeTime);

            communicationHandler.start();
            currentStateUpdater.start();
            onlineStateUpdater.start();
            propertiesUpdater.start();
            lightStateChanger.start();
            startOrStopSignalStrengthUpdates();
        } catch (Exception e) {
            logger.debug("Error occurred while initializing LIFX handler: {}", e.getMessage(), e);
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

            if (propertiesUpdater != null) {
                propertiesUpdater.stop();
                propertiesUpdater.removePropertiesUpdateListener(this);
                propertiesUpdater = null;
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

    private Duration getFadeTime() {
        BigDecimal fadeCfg = (BigDecimal) getConfig().get(LifxBindingConstants.CONFIG_PROPERTY_FADETIME);
        return fadeCfg == null ? FADE_TIME_DEFAULT : Duration.ofMillis(fadeCfg.longValue());
    }

    private PercentType getPowerOnBrightness() {
        Channel channel = null;

        if (product.isColor()) {
            ChannelUID channelUID = new ChannelUID(getThing().getUID(), LifxBindingConstants.CHANNEL_COLOR);
            channel = getThing().getChannel(channelUID.getId());
        } else {
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

    private Products getProduct() {
        String propertyValue = getThing().getProperties().get(LifxBindingConstants.PROPERTY_PRODUCT_ID);
        try {
            long productID = Long.parseLong(propertyValue);
            return Products.getProductFromProductID(productID);
        } catch (IllegalArgumentException e) {
            return Products.getLikelyProduct(getThing().getThingTypeUID());
        }
    }

    private void addRemoveZoneChannels(int zones) {
        List<Channel> newChannels = new ArrayList<Channel>();

        // retain non-zone channels
        for (Channel channel : getThing().getChannels()) {
            String channelId = channel.getUID().getId();
            if (!channelId.startsWith(CHANNEL_COLOR_ZONE) && !channelId.startsWith(CHANNEL_TEMPERATURE_ZONE)) {
                newChannels.add(channel);
            }
        }

        // add zone channels
        for (int i = 0; i < zones; i++) {
            newChannels.add(channelFactory.createColorZoneChannel(getThing().getUID(), i));
            newChannels.add(channelFactory.createTemperatureZoneChannel(getThing().getUID(), i));
        }

        updateThing(editThing().withChannels(newChannels).build());

        Map<String, String> properties = editProperties();
        properties.put(LifxBindingConstants.PROPERTY_ZONES, Integer.toString(zones));
        updateProperties(properties);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);
        startOrStopSignalStrengthUpdates();
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        startOrStopSignalStrengthUpdates();
    }

    private void startOrStopSignalStrengthUpdates() {
        currentStateUpdater.setUpdateSignalStrength(isLinked(CHANNEL_SIGNAL_STRENGTH));
    }

    private void sendPacket(Packet packet) {
        communicationHandler.sendPacket(packet);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof RefreshType) {
            try {
                switch (channelUID.getId()) {
                    case CHANNEL_COLOR:
                    case CHANNEL_BRIGHTNESS:
                        sendPacket(new GetLightPowerRequest());
                        sendPacket(new GetRequest());
                        break;
                    case CHANNEL_TEMPERATURE:
                        sendPacket(new GetRequest());
                        break;
                    case CHANNEL_INFRARED:
                        sendPacket(new GetLightInfraredRequest());
                        break;
                    case CHANNEL_SIGNAL_STRENGTH:
                        sendPacket(new GetWifiInfoRequest());
                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                logger.error("Error while refreshing a channel for the light: {}", ex.getMessage(), ex);
            }
        } else {
            try {
                boolean supportedCommand = true;
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
                        } else {
                            supportedCommand = false;
                        }
                        break;
                    case CHANNEL_BRIGHTNESS:
                        if (command instanceof PercentType) {
                            handlePercentCommand((PercentType) command);
                        } else if (command instanceof OnOffType) {
                            handleOnOffCommand((OnOffType) command);
                        } else if (command instanceof IncreaseDecreaseType) {
                            handleIncreaseDecreaseCommand((IncreaseDecreaseType) command);
                        } else {
                            supportedCommand = false;
                        }
                        break;
                    case CHANNEL_TEMPERATURE:
                        if (command instanceof PercentType) {
                            handleTemperatureCommand((PercentType) command);
                        } else if (command instanceof IncreaseDecreaseType) {
                            handleIncreaseDecreaseTemperatureCommand((IncreaseDecreaseType) command);
                        } else {
                            supportedCommand = false;
                        }
                        break;
                    case CHANNEL_INFRARED:
                        if (command instanceof PercentType) {
                            handleInfraredCommand((PercentType) command);
                        } else if (command instanceof IncreaseDecreaseType) {
                            handleIncreaseDecreaseInfraredCommand((IncreaseDecreaseType) command);
                        } else {
                            supportedCommand = false;
                        }
                        break;
                    default:
                        if (channelUID.getId().startsWith(CHANNEL_COLOR_ZONE)) {
                            int zoneIndex = Integer.parseInt(channelUID.getId().replace(CHANNEL_COLOR_ZONE, ""));
                            if (command instanceof HSBType) {
                                handleHSBCommand((HSBType) command, zoneIndex);
                            } else if (command instanceof PercentType) {
                                handlePercentCommand((PercentType) command, zoneIndex);
                            } else if (command instanceof IncreaseDecreaseType) {
                                handleIncreaseDecreaseCommand((IncreaseDecreaseType) command, zoneIndex);
                            } else {
                                supportedCommand = false;
                            }
                        } else if (channelUID.getId().startsWith(CHANNEL_TEMPERATURE_ZONE)) {
                            int zoneIndex = Integer.parseInt(channelUID.getId().replace(CHANNEL_TEMPERATURE_ZONE, ""));
                            if (command instanceof PercentType) {
                                handleTemperatureCommand((PercentType) command, zoneIndex);
                            } else if (command instanceof IncreaseDecreaseType) {
                                handleIncreaseDecreaseTemperatureCommand((IncreaseDecreaseType) command, zoneIndex);
                            } else {
                                supportedCommand = false;
                            }
                        } else {
                            supportedCommand = false;
                        }
                        break;
                }

                if (supportedCommand && !(command instanceof OnOffType)
                        && !CHANNEL_INFRARED.equals(channelUID.getId())) {
                    getLightStateForCommand().setPowerState(PowerState.ON);
                }
            } catch (Exception ex) {
                logger.error("Error while updating light: {}", ex.getMessage(), ex);
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
        return pendingLightState.getDurationSinceLastChange().minus(MAX_STATE_CHANGE_DURATION).isNegative();
    }

    private void handleTemperatureCommand(PercentType temperature) {
        HSBK newColor = getLightStateForCommand().getNullSafeColor();
        newColor.setSaturation(PercentType.ZERO);
        newColor.setTemperature(temperature);
        getLightStateForCommand().setColor(newColor);
    }

    private void handleTemperatureCommand(PercentType temperature, int zoneIndex) {
        HSBK newColor = getLightStateForCommand().getNullSafeColor(zoneIndex);
        newColor.setSaturation(PercentType.ZERO);
        newColor.setTemperature(temperature);
        getLightStateForCommand().setColor(newColor, zoneIndex);
    }

    private void handleHSBCommand(HSBType hsb) {
        getLightStateForCommand().setColor(hsb);
    }

    private void handleHSBCommand(HSBType hsb, int zoneIndex) {
        getLightStateForCommand().setColor(hsb, zoneIndex);
    }

    private void handlePercentCommand(PercentType brightness) {
        getLightStateForCommand().setBrightness(brightness);
    }

    private void handlePercentCommand(PercentType brightness, int zoneIndex) {
        getLightStateForCommand().setBrightness(brightness, zoneIndex);
    }

    private void handleOnOffCommand(OnOffType onOff) {
        if (powerOnBrightness != null) {
            PercentType newBrightness = onOff == OnOffType.ON ? powerOnBrightness : new PercentType(0);
            getLightStateForCommand().setBrightness(newBrightness);
        }
        getLightStateForCommand().setPowerState(onOff);
    }

    private void handleIncreaseDecreaseCommand(IncreaseDecreaseType increaseDecrease) {
        HSBK baseColor = getLightStateForCommand().getNullSafeColor();
        PercentType newBrightness = increaseDecreasePercentType(increaseDecrease, baseColor.getHSB().getBrightness());
        handlePercentCommand(newBrightness);
    }

    private void handleIncreaseDecreaseCommand(IncreaseDecreaseType increaseDecrease, int zoneIndex) {
        HSBK baseColor = getLightStateForCommand().getNullSafeColor(zoneIndex);
        PercentType newBrightness = increaseDecreasePercentType(increaseDecrease, baseColor.getHSB().getBrightness());
        handlePercentCommand(newBrightness, zoneIndex);
    }

    private void handleIncreaseDecreaseTemperatureCommand(IncreaseDecreaseType increaseDecrease) {
        PercentType baseTemperature = getLightStateForCommand().getNullSafeColor().getTemperature();
        PercentType newTemperature = increaseDecreasePercentType(increaseDecrease, baseTemperature);
        handleTemperatureCommand(newTemperature);
    }

    private void handleIncreaseDecreaseTemperatureCommand(IncreaseDecreaseType increaseDecrease, int zoneIndex) {
        PercentType baseTemperature = getLightStateForCommand().getNullSafeColor(zoneIndex).getTemperature();
        PercentType newTemperature = increaseDecreasePercentType(increaseDecrease, baseTemperature);
        handleTemperatureCommand(newTemperature, zoneIndex);
    }

    private void handleInfraredCommand(PercentType infrared) {
        getLightStateForCommand().setInfrared(infrared);
    }

    private void handleIncreaseDecreaseInfraredCommand(IncreaseDecreaseType increaseDecrease) {
        PercentType baseInfrared = getLightStateForCommand().getInfrared();
        if (baseInfrared != null) {
            PercentType newInfrared = increaseDecreasePercentType(increaseDecrease, baseInfrared);
            handleInfraredCommand(newInfrared);
        }
    }

    @Override
    public void handlePropertiesUpdate(Map<String, String> properties) {
        updateProperties(properties);
    }

    private void updateStateIfChanged(String channel, State newState) {
        State oldState = channelStates.get(channel);
        if (oldState == null || !oldState.equals(newState)) {
            updateState(channel, newState);
            channelStates.put(channel, newState);
        }
    }

    private void updateStatusIfChanged(ThingStatus status) {
        updateStatusIfChanged(status, ThingStatusDetail.NONE);
    }

    private void updateStatusIfChanged(ThingStatus status, ThingStatusDetail statusDetail) {
        ThingStatusInfo newStatusInfo = new ThingStatusInfo(status, statusDetail, null);
        Duration durationSinceLastUpdate = Duration.between(lastStatusInfoUpdate, LocalDateTime.now());
        boolean intervalElapsed = MIN_STATUS_INFO_UPDATE_INTERVAL.minus(durationSinceLastUpdate).isNegative();

        if (statusInfo == null || !statusInfo.equals(newStatusInfo) || intervalElapsed) {
            statusInfo = newStatusInfo;
            lastStatusInfoUpdate = LocalDateTime.now();
            updateStatus(status, statusDetail);
        }
    }

}
