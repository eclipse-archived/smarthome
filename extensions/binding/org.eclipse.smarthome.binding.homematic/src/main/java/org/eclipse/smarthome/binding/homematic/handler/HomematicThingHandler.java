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
package org.eclipse.smarthome.binding.homematic.handler;

import static org.eclipse.smarthome.binding.homematic.HomematicBindingConstants.*;
import static org.eclipse.smarthome.binding.homematic.internal.misc.HomematicConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.smarthome.binding.homematic.internal.common.HomematicConfig;
import org.eclipse.smarthome.binding.homematic.internal.communicator.HomematicGateway;
import org.eclipse.smarthome.binding.homematic.internal.converter.ConverterException;
import org.eclipse.smarthome.binding.homematic.internal.converter.ConverterFactory;
import org.eclipse.smarthome.binding.homematic.internal.converter.ConverterTypeException;
import org.eclipse.smarthome.binding.homematic.internal.converter.TypeConverter;
import org.eclipse.smarthome.binding.homematic.internal.misc.HomematicClientException;
import org.eclipse.smarthome.binding.homematic.internal.misc.HomematicConstants;
import org.eclipse.smarthome.binding.homematic.internal.model.HmChannel;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDatapoint;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDatapointConfig;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDatapointInfo;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDevice;
import org.eclipse.smarthome.binding.homematic.internal.model.HmParamsetType;
import org.eclipse.smarthome.binding.homematic.internal.type.HomematicTypeGeneratorImpl;
import org.eclipse.smarthome.binding.homematic.internal.type.MetadataUtils;
import org.eclipse.smarthome.binding.homematic.internal.type.UidUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.validation.ConfigValidationException;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomematicThingHandler} is responsible for handling commands, which are sent to one of the channels.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class HomematicThingHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(HomematicThingHandler.class);
    private Future<?> initFuture;
    private final Object initLock = new Object();
    private volatile boolean deviceDeletionPending = false;

    public HomematicThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        if (initFuture != null) {
            return;
        }

        initFuture = scheduler.submit(() -> {
            initFuture = null;
            try {
                synchronized (initLock) {
                    doInitializeInBackground();
                }
            } catch (HomematicClientException ex) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, ex.getMessage());
            } catch (IOException ex) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
            } catch (BridgeHandlerNotAvailableException ex) {
                // ignore
            } catch (Exception ex) {
                logger.error("{}", ex.getMessage(), ex);
            }
        });
    }

    private void doInitializeInBackground()
            throws BridgeHandlerNotAvailableException, HomematicClientException, IOException {
        HomematicGateway gateway = getHomematicGateway();
        HmDevice device = gateway.getDevice(UidUtils.getHomematicAddress(getThing()));
        HmChannel channelZero = device.getChannel(0);
        loadHomematicChannelValues(channelZero);
        updateStatus(device);
        logger.debug("Initializing thing '{}' from gateway '{}'", getThing().getUID(), gateway.getId());

        // update properties
        Map<String, String> properties = editProperties();
        setProperty(properties, channelZero, PROPERTY_BATTERY_TYPE, VIRTUAL_DATAPOINT_NAME_BATTERY_TYPE);
        setProperty(properties, channelZero, Thing.PROPERTY_FIRMWARE_VERSION, VIRTUAL_DATAPOINT_NAME_FIRMWARE);
        setProperty(properties, channelZero, Thing.PROPERTY_SERIAL_NUMBER, device.getAddress());
        setProperty(properties, channelZero, PROPERTY_AES_KEY, DATAPOINT_NAME_AES_KEY);
        updateProperties(properties);

        // update data point list for reconfigurable channels
        for (HmChannel channel : device.getChannels()) {
            if (channel.isReconfigurable()) {
                loadHomematicChannelValues(channel);
                if (channel.checkForChannelFunctionChange()) {
                    gateway.updateChannelValueDatapoints(channel);
                }
            }
        }

        // update configurations
        Configuration config = editConfiguration();
        for (HmChannel channel : device.getChannels()) {
            loadHomematicChannelValues(channel);
            for (HmDatapoint dp : channel.getDatapoints()) {
                if (dp.getParamsetType() == HmParamsetType.MASTER) {
                    config.put(MetadataUtils.getParameterName(dp),
                            dp.isEnumType() ? dp.getOptionValue() : dp.getValue());
                }
            }
        }
        updateConfiguration(config);

        // update thing channel list for reconfigurable channels (relies on the new value of the
        // CHANNEL_FUNCTION datapoint fetched during configuration update)
        List<Channel> thingChannels = new ArrayList<>(getThing().getChannels());
        if (updateDynamicChannelList(device, thingChannels)) {
            updateThing(editThing().withChannels(thingChannels).build());
        }
    }

    /**
     * Update the given thing channel list to reflect the device's current datapoint set
     *
     * @return true if the list was modified, false if it was not modified
     */
    private boolean updateDynamicChannelList(HmDevice device, List<Channel> thingChannels) {
        boolean changed = false;
        for (HmChannel channel : device.getChannels()) {
            if (!channel.isReconfigurable()) {
                continue;
            }
            final String expectedFunction = channel
                    .getDatapoint(HmParamsetType.MASTER, HomematicConstants.DATAPOINT_NAME_CHANNEL_FUNCTION)
                    .getOptionValue();
            final String propertyName = String.format(PROPERTY_DYNAMIC_FUNCTION_FORMAT, channel.getNumber());

            // remove thing channels that were configured for a different function
            Iterator<Channel> channelIter = thingChannels.iterator();
            while (channelIter.hasNext()) {
                Map<String, String> properties = channelIter.next().getProperties();
                String function = properties.get(propertyName);
                if (function != null && !function.equals(expectedFunction)) {
                    channelIter.remove();
                    changed = true;
                }
            }
            for (HmDatapoint dp : channel.getDatapoints()) {
                if (HomematicTypeGeneratorImpl.isIgnoredDatapoint(dp)
                        || dp.getParamsetType() != HmParamsetType.VALUES) {
                    continue;
                }
                ChannelUID channelUID = UidUtils.generateChannelUID(dp, getThing().getUID());
                if (containsChannel(thingChannels, channelUID)) {
                    // Channel is already present -> channel configuration likely hasn't changed
                    continue;
                }

                Map<String, String> channelProps = new HashMap<>();
                channelProps.put(propertyName, expectedFunction);

                Channel thingChannel = ChannelBuilder.create(channelUID, MetadataUtils.getItemType(dp))
                        .withProperties(channelProps).withLabel(MetadataUtils.getLabel(dp))
                        .withDescription(MetadataUtils.getDatapointDescription(dp))
                        .withType(UidUtils.generateChannelTypeUID(dp)).build();
                thingChannels.add(thingChannel);
                changed = true;
            }
        }

        return changed;
    }

    /**
     * Checks whether the given list includes a channel with the given UID
     */
    private static boolean containsChannel(List<Channel> channels, ChannelUID channelUID) {
        for (Channel channel : channels) {
            ChannelUID uid = channel.getUID();
            if (StringUtils.equals(channelUID.getGroupId(), uid.getGroupId())
                    && StringUtils.equals(channelUID.getId(), uid.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets a thing property with a datapoint value.
     */
    private void setProperty(Map<String, String> properties, HmChannel channelZero, String propertyName,
            String datapointName) {
        HmDatapoint dp = channelZero
                .getDatapoint(new HmDatapointInfo(HmParamsetType.VALUES, channelZero, datapointName));
        if (dp != null) {
            properties.put(propertyName, ObjectUtils.toString(dp.getValue()));
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        handleRefresh(channelUID);
    }

    /**
     * Updates the state of the given channel.
     */
    protected void handleRefresh(ChannelUID channelUID) {
        try {
            if (thing.getStatus() == ThingStatus.ONLINE) {
                logger.debug("Updating channel '{}' from thing id '{}'", channelUID, getThing().getUID().getId());
                updateChannelState(channelUID);
            }
        } catch (Exception ex) {
            logger.warn("{}", ex.getMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command '{}' for channel '{}'", command, channelUID);
        HmDatapoint dp = null;
        try {
            HomematicGateway gateway = getHomematicGateway();
            HmDatapointInfo dpInfo = UidUtils.createHmDatapointInfo(channelUID);
            if (RefreshType.REFRESH == command) {
                logger.debug("Refreshing {}", dpInfo);
                dpInfo = new HmDatapointInfo(dpInfo.getAddress(), HmParamsetType.VALUES, 0,
                        VIRTUAL_DATAPOINT_NAME_RELOAD_FROM_GATEWAY);
                dp = gateway.getDatapoint(dpInfo);
                gateway.sendDatapoint(dp, new HmDatapointConfig(), Boolean.TRUE);
            } else {
                Channel channel = getThing().getChannel(channelUID.getId());
                if (channel == null) {
                    logger.warn("Channel '{}' not found in thing '{}' on gateway '{}'", channelUID, getThing().getUID(),
                            gateway.getId());
                } else {
                    if (StopMoveType.STOP == command && DATAPOINT_NAME_LEVEL.equals(dpInfo.getName())) {
                        // special case with stop type (rollershutter)
                        dpInfo.setName(DATAPOINT_NAME_STOP);
                        HmDatapoint stopDp = gateway.getDatapoint(dpInfo);
                        ChannelUID stopChannelUID = UidUtils.generateChannelUID(stopDp, getThing().getUID());
                        handleCommand(stopChannelUID, OnOffType.ON);
                    } else {
                        dp = gateway.getDatapoint(dpInfo);
                        TypeConverter<?> converter = ConverterFactory.createConverter(channel.getAcceptedItemType());
                        Object newValue = converter.convertToBinding(command, dp);
                        HmDatapointConfig config = getChannelConfig(channel, dp);
                        gateway.sendDatapoint(dp, config, newValue);
                    }
                }
            }
        } catch (HomematicClientException | BridgeHandlerNotAvailableException ex) {
            logger.warn("{}", ex.getMessage());
        } catch (IOException ex) {
            if (dp != null && dp.getChannel().getDevice().isOffline()) {
                logger.warn("Device '{}' is OFFLINE, can't send command '{}' for channel '{}'",
                        dp.getChannel().getDevice().getAddress(), command, channelUID);
                logger.trace("{}", ex.getMessage(), ex);
            } else {
                logger.error("{}", ex.getMessage(), ex);
            }
        } catch (ConverterTypeException ex) {
            logger.warn("{}, please check the item type and the commands in your scripts", ex.getMessage());
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage(), ex);
        }
    }

    /**
     * Evaluates the channel and datapoint for this channelUID and updates the state of the channel.
     */
    private void updateChannelState(ChannelUID channelUID)
            throws BridgeHandlerNotAvailableException, HomematicClientException, IOException, ConverterException {
        HomematicGateway gateway = getHomematicGateway();
        HmDatapointInfo dpInfo = UidUtils.createHmDatapointInfo(channelUID);
        HmDatapoint dp = gateway.getDatapoint(dpInfo);
        Channel channel = getThing().getChannel(channelUID.getId());
        updateChannelState(dp, channel);
    }

    /**
     * Sets the configuration or evaluates the channel for this datapoint and updates the state of the channel.
     */
    protected void updateDatapointState(HmDatapoint dp) {
        try {
            if (HomematicTypeGeneratorImpl.isStatusDatapoint(dp)) {
                updateStatus(dp.getChannel().getDevice());
            }
            if (dp.getParamsetType() == HmParamsetType.MASTER) {
                // update configuration
                Configuration config = editConfiguration();
                config.put(MetadataUtils.getParameterName(dp), dp.isEnumType() ? dp.getOptionValue() : dp.getValue());
                updateConfiguration(config);
            } else if (!HomematicTypeGeneratorImpl.isIgnoredDatapoint(dp)) {
                // update channel
                ChannelUID channelUID = UidUtils.generateChannelUID(dp, thing.getUID());
                Channel channel = thing.getChannel(channelUID.getId());
                if (channel != null) {
                    updateChannelState(dp, channel);
                } else {
                    logger.warn("Channel not found for datapoint '{}'", new HmDatapointInfo(dp));
                }
            }
        } catch (BridgeHandlerNotAvailableException ex) {
            // ignore
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage(), ex);
        }
    }

    /**
     * Converts the value of the datapoint to a State, updates the channel and also sets the thing status if necessary.
     */
    private void updateChannelState(final HmDatapoint dp, Channel channel)
            throws IOException, BridgeHandlerNotAvailableException, ConverterException {

        if (dp.isTrigger()) {
            if (dp.getValue() != null) {
                triggerChannel(channel.getUID(), ObjectUtils.toString(dp.getValue()));
            }
        } else if (isLinked(channel)) {
            loadHomematicChannelValues(dp.getChannel());

            TypeConverter<?> converter = ConverterFactory.createConverter(channel.getAcceptedItemType());
            State state = converter.convertFromBinding(dp);
            if (state != null) {
                updateState(channel.getUID(), state);
            } else {
                logger.debug("Failed to get converted state from datapoint '{}'", dp.getName());
            }
        }
    }

    /**
     * Loads all values for the given Homematic channel if it is not initialized.
     */
    private void loadHomematicChannelValues(HmChannel hmChannel)
            throws BridgeHandlerNotAvailableException, IOException {
        if (!hmChannel.isInitialized()) {
            synchronized (this) {
                if (!hmChannel.isInitialized()) {
                    try {
                        getHomematicGateway().loadChannelValues(hmChannel);
                    } catch (IOException ex) {
                        if (hmChannel.getDevice().isOffline()) {
                            logger.warn("Device '{}' is OFFLINE, can't update channel '{}'",
                                    hmChannel.getDevice().getAddress(), hmChannel.getNumber());
                        } else {
                            throw ex;
                        }
                    }
                }
            }
        }
    }

    /**
     * Updates the thing status based on device status.
     */
    private void updateStatus(HmDevice device) throws BridgeHandlerNotAvailableException, IOException {
        loadHomematicChannelValues(device.getChannel(0));

        ThingStatus oldStatus = thing.getStatus();
        ThingStatus newStatus = ThingStatus.ONLINE;
        ThingStatusDetail newDetail = ThingStatusDetail.NONE;

        if (device.isFirmwareUpdating()) {
            newStatus = ThingStatus.OFFLINE;
            newDetail = ThingStatusDetail.FIRMWARE_UPDATING;
        } else if (device.isUnreach()) {
            newStatus = ThingStatus.OFFLINE;
            newDetail = ThingStatusDetail.COMMUNICATION_ERROR;
        } else if (device.isConfigPending() || device.isUpdatePending()) {
            newDetail = ThingStatusDetail.CONFIGURATION_PENDING;
        }

        if (thing.getStatus() != newStatus || thing.getStatusInfo().getStatusDetail() != newDetail) {
            updateStatus(newStatus, newDetail);
        }
        if (oldStatus == ThingStatus.OFFLINE && newStatus == ThingStatus.ONLINE) {
            initialize();
        }
    }

    /**
     * Returns true, if the channel is linked at least to one item.
     */
    private boolean isLinked(Channel channel) {
        return channel != null && super.isLinked(channel.getUID().getId());
    }

    /**
     * Returns the channel config for the given datapoint.
     */
    protected HmDatapointConfig getChannelConfig(HmDatapoint dp) {
        ChannelUID channelUid = UidUtils.generateChannelUID(dp, getThing().getUID());
        Channel channel = getThing().getChannel(channelUid.getId());
        return channel != null ? getChannelConfig(channel, dp) : new HmDatapointConfig();
    }

    /**
     * Returns the config for a channel.
     */
    private HmDatapointConfig getChannelConfig(Channel channel, HmDatapoint dp) {
        return channel.getConfiguration().as(HmDatapointConfig.class);
    }

    /**
     * Returns the Homematic gateway if the bridge is available.
     */
    private HomematicGateway getHomematicGateway() throws BridgeHandlerNotAvailableException {
        final Bridge bridge = getBridge();
        if (bridge != null) {
            HomematicBridgeHandler bridgeHandler = (HomematicBridgeHandler) bridge.getHandler();
            if (bridgeHandler != null && bridgeHandler.getGateway() != null) {
                return bridgeHandler.getGateway();
            }
        }
        if (thing.getStatus() != ThingStatus.INITIALIZING) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR);
        }
        throw new BridgeHandlerNotAvailableException("BridgeHandler not yet available!");
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters)
            throws ConfigValidationException {
        validateConfigurationParameters(configurationParameters);

        try {
            HomematicGateway gateway = getHomematicGateway();
            HmDevice device = gateway.getDevice(UidUtils.getHomematicAddress(getThing()));

            for (Entry<String, Object> configurationParmeter : configurationParameters.entrySet()) {
                String key = configurationParmeter.getKey();
                Object newValue = configurationParmeter.getValue();

                if (key.startsWith("HMP_")) {
                    key = StringUtils.removeStart(key, "HMP_");
                    Integer channelNumber = NumberUtils.toInt(StringUtils.substringBefore(key, "_"));
                    String dpName = StringUtils.substringAfter(key, "_");

                    HmDatapointInfo dpInfo = new HmDatapointInfo(device.getAddress(), HmParamsetType.MASTER,
                            channelNumber, dpName);
                    HmDatapoint dp = device.getChannel(channelNumber).getDatapoint(dpInfo);

                    if (dp != null) {
                        try {
                            if (newValue != null) {
                                if (newValue instanceof BigDecimal) {
                                    final BigDecimal decimal = (BigDecimal) newValue;
                                    if (dp.isIntegerType()) {
                                        newValue = decimal.intValue();
                                    } else if (dp.isFloatType()) {
                                        newValue = decimal.doubleValue();
                                    }
                                }
                                if (ObjectUtils.notEqual(dp.isEnumType() ? dp.getOptionValue() : dp.getValue(),
                                        newValue)) {
                                    gateway.sendDatapoint(dp, new HmDatapointConfig(), newValue);
                                }
                            }
                        } catch (IOException ex) {
                            logger.error("Error setting thing property {}: {}", dpInfo, ex.getMessage());
                        }
                    } else {
                        logger.error("Can't find datapoint for thing property {}", dpInfo);
                    }
                }
            }
            gateway.triggerDeviceValuesReload(device);
        } catch (HomematicClientException | BridgeHandlerNotAvailableException ex) {
            logger.error("Error setting thing properties: {}", ex.getMessage(), ex);
        }
    }

    @SuppressWarnings("null")
    @Override
    public synchronized void handleRemoval() {
        final Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() != null
                && bridge.getConfiguration().as(HomematicConfig.class).isUnpairOnDeletion()) {
            deviceDeletionPending = true;
            ((HomematicBridgeHandler) bridge.getHandler()).deleteFromGateway(UidUtils.getHomematicAddress(thing), false,
                    false, true);
        } else {
            super.handleRemoval();
        }
    }

    /**
     * Called by the bridgeHandler when this device has been removed from the gateway.
     */
    public synchronized void deviceRemoved() {
        deviceDeletionPending = false;
        if (getThing().getStatus() == ThingStatus.REMOVING) {
            // thing removal was initiated on ESH side
            updateStatus(ThingStatus.REMOVED);
        } else {
            // device removal was initiated on homematic side, thing is not removed
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE);
        }
    }

    /**
     * Called by the bridgeHandler when the device for this thing has been added to the gateway.
     * This is used to reconnect a device that was previously unpaired.
     *
     * @param device The device that has been added to the gateway
     */
    public void deviceLoaded(HmDevice device) {
        try {
            updateStatus(device);
        } catch (BridgeHandlerNotAvailableException ex) {
            // ignore
        } catch (IOException ex) {
            logger.warn("Could not reinitialize the device '{}': {}", device.getAddress(), ex.getMessage(), ex);
        }
    }

    /**
     * Returns whether the device deletion is pending.
     *
     * @return true, if the deletion of this device on its gateway has been triggered but has not yet completed
     */
    public synchronized boolean isDeletionPending() {
        return deviceDeletionPending;
    }
}
