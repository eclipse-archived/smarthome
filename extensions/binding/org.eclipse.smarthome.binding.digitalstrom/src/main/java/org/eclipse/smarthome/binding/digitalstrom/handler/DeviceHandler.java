/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.handler;

import static org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants.*;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.DeviceStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.ChangeableDeviceConfigEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceSceneSpec;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceStateUpdate;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceStateUpdateImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.FunctionalColorGroupEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.OutputModeEnum;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link DeviceHandler} is responsible for handling the configuration, load supported channels of a
 * digitalSTROM device and handling commands, which are sent to one of the channels. <br>
 * <br>
 * For that it uses the {@link BridgeHandler} and the {@link DeviceStateUpdate} mechanism of the {@link Device} to
 * execute the actual command and implements the {@link DeviceStatusListener} to get informed about changes from the
 * accompanying {@link Device}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public class DeviceHandler extends BaseThingHandler implements DeviceStatusListener {

    private Logger logger = LoggerFactory.getLogger(DeviceHandler.class);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(
            DigitalSTROMBindingConstants.THING_TYPE_GE_DEVICE, DigitalSTROMBindingConstants.THING_TYPE_SW_DEVICE,
            DigitalSTROMBindingConstants.THING_TYPE_GR_DEVICE);

    private String dSID = null;

    private Device device;

    private BridgeHandler dssBridgeHandler;

    private Command lastComand = null;

    private String currentChannel = null;

    private boolean isActivePowerChannelLoaded = false;
    private boolean isOutputCurrentChannelLoaded = false;
    private boolean isElectricMeterChannelLoaded = false;

    public DeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing DeviceHandler.");
        if (StringUtils.isNotBlank((String) getConfig().get(DigitalSTROMBindingConstants.DEVICE_DSID))) {
            dSID = getConfig().get(DigitalSTROMBindingConstants.DEVICE_DSID).toString();
            if (getBridge() != null) {
                bridgeStatusChanged(getBridge().getStatusInfo());
            } else {
                // Set status to OFFLINE if no bridge is available e.g. because the bridge has been removed and the
                // Thing was reinitialized.
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Bridge is missing!");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "dSID is missing");
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed... unregister DeviceStatusListener");
        if (dSID != null) {
            if (dssBridgeHandler != null) {
                dssBridgeHandler.unregisterDeviceStatusListener(this);
            }
        }
        if (device != null) {
            device.setSensorDataRefreshPriority(REFRESH_PRIORITY_NEVER, REFRESH_PRIORITY_NEVER, REFRESH_PRIORITY_NEVER);
        }
        device = null;
    }

    @Override
    public void handleRemoval() {
        if (getDssBridgeHandler() != null) {
            this.dssBridgeHandler.childThingRemoved(dSID);
        }
        updateStatus(ThingStatus.REMOVED);
    }

    @Override
    public void thingUpdated(Thing thing) {
        this.thing = thing;
        if (device == null) {
            initialize();
        }
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParmeters) {
        Configuration configuration = editConfiguration();
        for (Entry<String, Object> configurationParmeter : configurationParmeters.entrySet()) {
            configuration.put(configurationParmeter.getKey(), configurationParmeter.getValue());
        }

        // check device info, load sensor priorities into the device and load sensor channels of the thing
        checkDeviceInfoConfig(configuration, device);
        loadSensorChannels(configuration);

        updateConfiguration(configuration);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)) {
            if (dSID != null) {
                if (getDssBridgeHandler() != null) {
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                            "waiting for listener registration");
                    logger.debug("Set status to {}", getThing().getStatus());
                    dssBridgeHandler.registerDeviceStatusListener(this);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No dSID is set!");
            }
        }
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.OFFLINE)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.REMOVED)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Bridge has been removed.");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        BridgeHandler dssBridgeHandler = getDssBridgeHandler();
        if (dssBridgeHandler == null) {
            logger.warn("BridgeHandler not found. Cannot handle command without bridge.");
            return;
        }

        if (device == null) {
            logger.warn(
                    "Device not known on StructureManager or DeviceStatusListener is not registerd. Cannot handle command.");
            return;
        }

        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case CHANNEL_ID_ACTIVE_POWER:
                    dssBridgeHandler.sendComandsToDSS(device,
                            new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ACTIVE_POWER, 1));
                    break;
                case CHANNEL_ID_OUTPUT_CURRENT:
                    dssBridgeHandler.sendComandsToDSS(device,
                            new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_OUTPUT_CURRENT, 1));
                    break;
                case CHANNEL_ID_ELECTRIC_METER:
                    dssBridgeHandler.sendComandsToDSS(device,
                            new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ELECTRIC_METER, 1));
                    break;
                default:
                    dssBridgeHandler.sendComandsToDSS(device,
                            new DeviceStateUpdateImpl(DeviceStateUpdate.REFRESH_OUTPUT, 0));
                    break;
            }
        } else if (!device.isShade()) {
            if (this.channelIsOutputChannel(channelUID.getId())) {
                if (command instanceof PercentType) {
                    device.setOutputValue(
                            (short) fromPercentToValue(((PercentType) command).intValue(), device.getMaxOutputValue()));
                } else if (command instanceof OnOffType) {
                    if (OnOffType.ON.equals(command)) {
                        device.setIsOn(true);
                    } else {
                        device.setIsOn(false);
                    }
                } else if (command instanceof IncreaseDecreaseType) {
                    if (IncreaseDecreaseType.INCREASE.equals(command)) {
                        device.increase();
                    } else {
                        device.decrease();
                    }
                } else if (command instanceof StringType) {
                    device.setOutputValue(Short.parseShort(((StringType) command).toString()));
                }
            } else {
                logger.warn("Command sent to an unknown channel id: " + channelUID);
            }
        } else {
            if (channelUID.getId().equals(CHANNEL_ID_SHADE_ANGLE)) {
                if (command instanceof PercentType) {
                    device.setAnglePosition(
                            (short) fromPercentToValue(((PercentType) command).intValue(), device.getMaxSlatAngle()));
                } else if (command instanceof OnOffType) {
                    if (OnOffType.ON.equals(command)) {
                        device.setAnglePosition(device.getMaxSlatAngle());
                    } else {
                        device.setAnglePosition(device.getMinSlatAngle());
                    }
                } else if (command instanceof IncreaseDecreaseType) {
                    if (IncreaseDecreaseType.INCREASE.equals(command)) {
                        device.increaseSlatAngle();
                    } else {
                        device.decreaseSlatAngle();
                    }
                }
            } else if (channelUID.getId().equals(DigitalSTROMBindingConstants.CHANNEL_ID_SHADE)) {
                if (command instanceof PercentType) {
                    int percent = ((PercentType) command).intValue();
                    if (!device.getHWinfo().equals("GR-KL200")) {
                        percent = 100 - percent;
                    }
                    device.setSlatPosition(fromPercentToValue(percent, device.getMaxSlatPosition()));
                    this.lastComand = command;
                } else if (command instanceof StopMoveType) {
                    if (StopMoveType.MOVE.equals(command)) {
                        handleCommand(channelUID, this.lastComand);
                    } else {
                        dssBridgeHandler.stopOutputValue(device);
                    }
                } else if (command instanceof UpDownType) {
                    if (UpDownType.UP.equals(command)) {
                        device.setIsOpen(true);
                        this.lastComand = command;
                    } else {
                        device.setIsOpen(false);
                        this.lastComand = command;
                    }
                }
            } else {
                logger.warn("Command sent to an unknown channel id: " + channelUID);
            }
        }
    }

    private int fromPercentToValue(int percent, int max) {
        if (percent < 0 || percent == 0) {
            return 0;
        }
        if (max < 0 || max == 0) {
            return 0;
        }
        return (int) (max * ((float) percent / 100));
    }

    private synchronized BridgeHandler getDssBridgeHandler() {
        if (this.dssBridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                logger.debug("Bride cannot be found");
                return null;
            }
            ThingHandler handler = bridge.getHandler();

            if (handler instanceof BridgeHandler) {
                dssBridgeHandler = (BridgeHandler) handler;
                dssBridgeHandler.registerDeviceStatusListener(this);
            } else {
                return null;
            }
        }
        return dssBridgeHandler;
    }

    @Override
    public synchronized void onDeviceStateChanged(DeviceStateUpdate deviceStateUpdate) {
        if (device != null) {
            if (deviceStateUpdate != null) {
                logger.debug("Update ESH-State");
                if (!device.isShade()) {
                    switch (deviceStateUpdate.getType()) {
                        case DeviceStateUpdate.UPDATE_BRIGHTNESS_DECREASE:
                        case DeviceStateUpdate.UPDATE_BRIGHTNESS_INCREASE:
                        case DeviceStateUpdate.UPDATE_BRIGHTNESS:
                            switch (currentChannel) {
                                case CHANNEL_ID_COMBINED_2_STAGE_SWITCH:
                                case CHANNEL_ID_GENERAL_COMBINED_2_STAGE_SWITCH:
                                    updateState(new ChannelUID(getThing().getUID(), currentChannel),
                                            new StringType(convertStageValue((short) 2, device.getOutputValue())));
                                    break;
                                case CHANNEL_ID_COMBINED_3_STAGE_SWITCH:
                                case CHANNEL_ID_GENERAL_COMBINED_3_STAGE_SWITCH:
                                    updateState(new ChannelUID(getThing().getUID(), currentChannel),
                                            new StringType(convertStageValue((short) 3, device.getOutputValue())));
                                    break;
                                case CHANNEL_ID_BRIGHTNESS:
                                case CHANNEL_ID_GENERAL_DIMM:
                                    if (deviceStateUpdate.getValue() > 0) {
                                        updateState(new ChannelUID(getThing().getUID(), currentChannel),
                                                new PercentType(fromValueToPercent(deviceStateUpdate.getValue(),
                                                        device.getMaxOutputValue())));
                                    } else {
                                        updateState(new ChannelUID(getThing().getUID(), currentChannel), OnOffType.OFF);
                                    }
                                    break;
                            }
                            break;
                        case DeviceStateUpdate.UPDATE_ON_OFF:
                            switch (currentChannel) {
                                case CHANNEL_ID_COMBINED_2_STAGE_SWITCH:
                                case CHANNEL_ID_GENERAL_COMBINED_2_STAGE_SWITCH:
                                    updateState(new ChannelUID(getThing().getUID(), currentChannel),
                                            new StringType(convertStageValue((short) 2, device.getOutputValue())));
                                    break;
                                case CHANNEL_ID_COMBINED_3_STAGE_SWITCH:
                                case CHANNEL_ID_GENERAL_COMBINED_3_STAGE_SWITCH:
                                    updateState(new ChannelUID(getThing().getUID(), currentChannel),
                                            new StringType(convertStageValue((short) 3, device.getOutputValue())));
                                    break;
                                default:
                                    if (deviceStateUpdate.getValue() > 0) {
                                        updateState(new ChannelUID(getThing().getUID(), currentChannel), OnOffType.ON);
                                    } else {
                                        updateState(new ChannelUID(getThing().getUID(), currentChannel), OnOffType.OFF);
                                    }
                            }
                            break;
                        case DeviceStateUpdate.UPDATE_ELECTRIC_METER:
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_ELECTRIC_METER),
                                    new DecimalType(deviceStateUpdate.getValue() * 0.01));
                            break;
                        case DeviceStateUpdate.UPDATE_OUTPUT_CURRENT:
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_OUTPUT_CURRENT),
                                    new DecimalType(deviceStateUpdate.getValue()));
                            break;
                        case DeviceStateUpdate.UPDATE_ACTIVE_POWER:
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_ACTIVE_POWER),
                                    new DecimalType(deviceStateUpdate.getValue()));
                            break;
                        default:
                            return;
                    }
                } else {
                    int percent = 0;
                    switch (deviceStateUpdate.getType()) {
                        case DeviceStateUpdate.UPDATE_SLAT_DECREASE:
                        case DeviceStateUpdate.UPDATE_SLAT_INCREASE:
                        case DeviceStateUpdate.UPDATE_SLATPOSITION:
                            percent = fromValueToPercent(deviceStateUpdate.getValue(), device.getMaxSlatPosition());
                            break;
                        case DeviceStateUpdate.UPDATE_OPEN_CLOSE:
                            if (deviceStateUpdate.getValue() > 0) {
                                percent = 100;
                            }
                            break;
                        case DeviceStateUpdate.UPDATE_OPEN_CLOSE_ANGLE:
                            if (device.isBlind()) {
                                if (deviceStateUpdate.getValue() > 0) {
                                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_SHADE_ANGLE),
                                            PercentType.HUNDRED);
                                } else {
                                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_SHADE_ANGLE),
                                            PercentType.ZERO);
                                }
                            }
                            return;
                        case DeviceStateUpdate.UPDATE_SLAT_ANGLE_DECREASE:
                        case DeviceStateUpdate.UPDATE_SLAT_ANGLE_INCREASE:
                        case DeviceStateUpdate.UPDATE_SLAT_ANGLE:
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_SHADE_ANGLE), new PercentType(
                                    fromValueToPercent(deviceStateUpdate.getValue(), device.getMaxSlatAngle())));
                            return;
                        default:
                            return;
                    }
                    if (!device.getHWinfo().equals("GR-KL210")) {
                        percent = 100 - percent;
                    }
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_SHADE), new PercentType(percent));
                }
            }
        }
    }

    private int fromValueToPercent(int value, int max) {
        if (value <= 0 || max <= 0) {
            return 0;
        }
        return new BigDecimal(value * ((float) 100 / max)).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
    }

    @Override
    public synchronized void onDeviceRemoved(Device device) {
        this.device = null;
        if (this.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            if (device != null && !device.isPresent()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                        "Device is not present in the digitalSTROM-System.");
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                        "Device is not avaible in the digitalSTROM-System.");
            }

        }
        logger.debug("Set status to {}", getThing().getStatus());
    }

    @Override
    public synchronized void onDeviceAdded(Device device) {
        if (device.isPresent()) {
            this.device = device;
            ThingStatusInfo statusInfo = this.dssBridgeHandler.getThing().getStatusInfo();
            updateStatus(statusInfo.getStatus(), statusInfo.getStatusDetail(), statusInfo.getDescription());
            logger.debug("Set status to {}", getThing().getStatus());

            Configuration config = getThing().getConfiguration();

            checkDeviceInfoConfig(config, device);
            // load sensor priorities into the device and load sensor channels of the thing
            if (!device.isShade()) {
                loadSensorChannels(config);
                // check and load output channel of the thing
                checkOutputChannel();
            } else if (device.isBlind()) {
                // load channel for set the angle of jalousie devices
                loadOutputChannel(CHANNEL_TYPE_SHADE_ANGLE, "Dimmer");
            }

            // load first channel values
            onDeviceStateInitial(device);

            // load scene configurations persistently into the thing
            for (Short i : device.getSavedScenes()) {
                onSceneConfigAdded(i);
            }

            device.saveConfigSceneSpecificationIntoDevice(this.getThing().getProperties());
            logger.debug("Load saved scene specification into device");
        } else {
            onDeviceRemoved(device);
        }
    }

    /**
     * Checks the configuration and add missing configuration.
     *
     * @param thing configuration (must not be null)
     * @param device (must not be null)
     */
    private void checkDeviceInfoConfig(Configuration config, Device device) {
        boolean configChanged = false;
        // check device info
        if (config.get(DigitalSTROMBindingConstants.DEVICE_NAME) == null
                || config.get(DigitalSTROMBindingConstants.DEVICE_NAME).toString().isEmpty()) {
            if (device.getName() != null) {
                config.put(DigitalSTROMBindingConstants.DEVICE_NAME, device.getName());
                configChanged = true;
            }
        }
        if (config.get(DigitalSTROMBindingConstants.DEVICE_UID) == null
                || config.get(DigitalSTROMBindingConstants.DEVICE_UID).toString().isEmpty()) {
            if (device.getDSUID() != null) {
                config.put(DigitalSTROMBindingConstants.DEVICE_UID, device.getDSUID());
                configChanged = true;
            }
        }
        if (config.get(DigitalSTROMBindingConstants.DEVICE_HW_INFO) == null
                || config.get(DigitalSTROMBindingConstants.DEVICE_HW_INFO).toString().isEmpty()) {
            if (device.getHWinfo() != null) {
                config.put(DigitalSTROMBindingConstants.DEVICE_HW_INFO, device.getHWinfo());
                configChanged = true;
            }
        }
        if (config.get(DigitalSTROMBindingConstants.DEVICE_ZONE_ID) == null
                || config.get(DigitalSTROMBindingConstants.DEVICE_ZONE_ID).toString().isEmpty()) {
            if (device.getZoneId() != -1) {
                config.put(DigitalSTROMBindingConstants.DEVICE_ZONE_ID, device.getZoneId());
                configChanged = true;
            }
        }
        if (config.get(DigitalSTROMBindingConstants.DEVICE_GROUPS) == null
                || config.get(DigitalSTROMBindingConstants.DEVICE_GROUPS).toString().isEmpty()) {
            if (device.getGroups() != null) {
                config.put(DigitalSTROMBindingConstants.DEVICE_GROUPS, device.getGroups().toString());
                configChanged = true;
            }
        }
        if (config.get(DigitalSTROMBindingConstants.DEVICE_OUTPUT_MODE) == null
                || config.get(DigitalSTROMBindingConstants.DEVICE_OUTPUT_MODE).toString().isEmpty()) {
            if (device.getOutputMode() != null) {
                config.put(DigitalSTROMBindingConstants.DEVICE_OUTPUT_MODE, device.getOutputMode().toString());
                configChanged = true;
            }
        }
        if (config.get(DigitalSTROMBindingConstants.DEVICE_FUNCTIONAL_COLOR_GROUP) == null
                || config.get(DigitalSTROMBindingConstants.DEVICE_FUNCTIONAL_COLOR_GROUP).toString().isEmpty()) {
            if (device.getFunctionalColorGroup() != null) {
                config.put(DigitalSTROMBindingConstants.DEVICE_FUNCTIONAL_COLOR_GROUP,
                        device.getFunctionalColorGroup().toString());
                configChanged = true;
            }
        }
        if (config.get(DigitalSTROMBindingConstants.DEVICE_METER_ID) == null
                || config.get(DigitalSTROMBindingConstants.DEVICE_METER_ID).toString().isEmpty()) {
            if (device.getMeterDSID() != null) {
                config.put(DigitalSTROMBindingConstants.DEVICE_METER_ID, device.getMeterDSID().toString());
                configChanged = true;
            }
        }
        if (configChanged) {
            super.updateConfiguration(config);
            configChanged = false;
        }
    }

    private void loadSensorChannels(Configuration config) {
        if (device != null && device.isPresent()) {
            // load sensor priorities into the device
            boolean configChanged = false;
            logger.debug("Add sensor priorities to the device");

            String activePowerPrio = DigitalSTROMBindingConstants.REFRESH_PRIORITY_NEVER;
            if (config.get(DigitalSTROMBindingConstants.ACTIVE_POWER_REFRESH_PRIORITY) != null) {
                activePowerPrio = config.get(DigitalSTROMBindingConstants.ACTIVE_POWER_REFRESH_PRIORITY).toString();
            } else {
                config.put(DigitalSTROMBindingConstants.ACTIVE_POWER_REFRESH_PRIORITY,
                        DigitalSTROMBindingConstants.REFRESH_PRIORITY_NEVER);
                configChanged = true;
            }

            String outputCurrentPrio = DigitalSTROMBindingConstants.REFRESH_PRIORITY_NEVER;
            if (config.get(DigitalSTROMBindingConstants.OUTPUT_CURRENT_REFRESH_PRIORITY) != null) {
                outputCurrentPrio = config.get(DigitalSTROMBindingConstants.OUTPUT_CURRENT_REFRESH_PRIORITY).toString();
            } else {
                config.put(DigitalSTROMBindingConstants.OUTPUT_CURRENT_REFRESH_PRIORITY,
                        DigitalSTROMBindingConstants.REFRESH_PRIORITY_NEVER);
                configChanged = true;
            }

            String electricMeterPrio = DigitalSTROMBindingConstants.REFRESH_PRIORITY_NEVER;
            if (config.get(DigitalSTROMBindingConstants.ELECTRIC_METER_REFRESH_PRIORITY) != null) {
                electricMeterPrio = config.get(DigitalSTROMBindingConstants.ELECTRIC_METER_REFRESH_PRIORITY).toString();
            } else {
                config.put(DigitalSTROMBindingConstants.ELECTRIC_METER_REFRESH_PRIORITY,
                        DigitalSTROMBindingConstants.REFRESH_PRIORITY_NEVER);
                configChanged = true;
            }
            if (configChanged) {
                super.updateConfiguration(config);
                configChanged = false;
            }

            device.setSensorDataRefreshPriority(activePowerPrio, electricMeterPrio, outputCurrentPrio);
            logger.debug("add sensor prioritys: active power = " + activePowerPrio + ", output current = "
                    + outputCurrentPrio + ", electric meter = " + electricMeterPrio + " to device with id "
                    + device.getDSID());

            // check and load sensor channels of the thing
            checkSensorChannel(activePowerPrio, outputCurrentPrio, electricMeterPrio);
        }
    }

    private void checkSensorChannel(String activePowerPrio, String outputCurrentPrio, String electricMeterPrio) {
        List<Channel> channelList = new LinkedList<Channel>(this.getThing().getChannels());

        boolean channelListChanged = false;

        // if sensor channels with priority never are loaded delete these channels
        if (!channelList.isEmpty()) {
            Iterator<Channel> channelInter = channelList.iterator();
            while (channelInter.hasNext()) {
                Channel channel = channelInter.next();
                switch (channel.getUID().getId()) {
                    case CHANNEL_ID_ACTIVE_POWER:
                        if (activePowerPrio.equals(REFRESH_PRIORITY_NEVER)) {
                            logger.debug("remove active power sensor channel");
                            channelInter.remove();
                            isActivePowerChannelLoaded = false;
                            channelListChanged = true;
                        } else {
                            isActivePowerChannelLoaded = true;
                        }
                        break;
                    case CHANNEL_ID_OUTPUT_CURRENT:
                        if (outputCurrentPrio.equals(REFRESH_PRIORITY_NEVER)) {
                            logger.debug("remove output current sensor channel");
                            channelInter.remove();
                            isOutputCurrentChannelLoaded = false;
                            channelListChanged = true;
                        } else {
                            isOutputCurrentChannelLoaded = true;
                        }
                        break;
                    case CHANNEL_ID_ELECTRIC_METER:
                        if (electricMeterPrio.equals(REFRESH_PRIORITY_NEVER)) {
                            logger.debug("remove eclectric meter sensor channel");
                            channelInter.remove();
                            isElectricMeterChannelLoaded = false;
                            channelListChanged = true;
                        } else {
                            isElectricMeterChannelLoaded = true;
                        }
                        break;
                }
            }
        }
        // if sensor channels with priority unequal never are not loaded these channels will be loaded now
        if (!activePowerPrio.equals(REFRESH_PRIORITY_NEVER) && !isActivePowerChannelLoaded) {
            logger.debug("create active power sensor channel");
            Channel channel = ChannelBuilder
                    .create(new ChannelUID(this.getThing().getUID(), CHANNEL_ID_ACTIVE_POWER), "Number")
                    .withType(DigitalSTROMBindingConstants.CHANNEL_TYPE_ACTIVE_POWER).build();
            channelList.add(channel);
            isActivePowerChannelLoaded = true;
            channelListChanged = true;
        }
        if (!outputCurrentPrio.equals(REFRESH_PRIORITY_NEVER) && !isOutputCurrentChannelLoaded) {
            logger.debug("create output current sensor channel");
            Channel channel = ChannelBuilder
                    .create(new ChannelUID(this.getThing().getUID(), CHANNEL_ID_OUTPUT_CURRENT), "Number")
                    .withType(DigitalSTROMBindingConstants.CHANNEL_TYPE_OUTPUT_CURRENT).build();
            channelList.add(channel);
            isOutputCurrentChannelLoaded = true;
            channelListChanged = true;
        }
        if (!electricMeterPrio.equals(REFRESH_PRIORITY_NEVER) && !isElectricMeterChannelLoaded) {
            logger.debug("create eclectric meter sensor channel");
            Channel channel = ChannelBuilder
                    .create(new ChannelUID(this.getThing().getUID(), CHANNEL_ID_ELECTRIC_METER), "Number")
                    .withType(DigitalSTROMBindingConstants.CHANNEL_TYPE_ELECTRIC_METER).build();
            channelList.add(channel);
            isElectricMeterChannelLoaded = true;
            channelListChanged = true;
        }

        if (channelListChanged) {
            logger.debug("load new channel list");
            ThingBuilder thingBuilder = editThing();
            thingBuilder.withChannels(channelList);
            updateThing(thingBuilder.build());
        }
    }

    private void checkOutputChannel() {
        if (device == null) {
            logger.debug("Can not load a channel without a device!");
            return;
        }
        // if the device have no output channel or it is disabled all output channels will be deleted
        if (!device.isDeviceWithOutput()) {
            loadOutputChannel(null, null);
        }

        if (device.getFunctionalColorGroup().equals(FunctionalColorGroupEnum.YELLOW)) {
            if (device.isDimmable() && (currentChannel == null || currentChannel != CHANNEL_ID_BRIGHTNESS)) {
                loadOutputChannel(CHANNEL_TYPE_BRIGHTNESS, "Dimmer");
            } else if (device.isSwitch() && (currentChannel != null || currentChannel != CHANNEL_ID_LIGHT_SWITCH)) {
                loadOutputChannel(CHANNEL_TYPE_LIGHT_SWITCH, "Switch");
            } else if (device.getOutputMode().equals(OutputModeEnum.COMBINED_2_STAGE_SWITCH)
                    && (currentChannel != null || currentChannel != CHANNEL_ID_COMBINED_2_STAGE_SWITCH)) {
                loadOutputChannel(CHANNEL_TYPE_COMBINED_2_STAGE_SWITCH, "String");
            } else if (device.getOutputMode().equals(OutputModeEnum.COMBINED_3_STAGE_SWITCH)
                    && (currentChannel != null || currentChannel != CHANNEL_ID_COMBINED_3_STAGE_SWITCH)) {
                loadOutputChannel(CHANNEL_TYPE_COMBINED_3_STAGE_SWITCH, "String");
            }
        } else {
            if (device.isDimmable() && (currentChannel == null || currentChannel != CHANNEL_ID_GENERAL_DIMM)) {
                loadOutputChannel(CHANNEL_TYPE_GENERAL_DIMM, "Dimmer");
            } else if (device.isSwitch() && (currentChannel != null || currentChannel != CHANNEL_ID_GENERAL_SWITCH)) {
                loadOutputChannel(CHANNEL_TYPE_GENERAL_SWITCH, "Switch");
            } else if (device.getOutputMode().equals(OutputModeEnum.COMBINED_2_STAGE_SWITCH)
                    && (currentChannel != null || currentChannel != CHANNEL_ID_GENERAL_COMBINED_2_STAGE_SWITCH)) {
                loadOutputChannel(CHANNEL_TYPE_GENERAL_COMBINED_2_STAGE_SWITCH, "String");
            } else if (device.getOutputMode().equals(OutputModeEnum.COMBINED_3_STAGE_SWITCH)
                    && (currentChannel != null || currentChannel != CHANNEL_ID_GENERAL_COMBINED_3_STAGE_SWITCH)) {
                loadOutputChannel(CHANNEL_TYPE_GENERAL_COMBINED_3_STAGE_SWITCH, "String");
            } else {
                loadOutputChannel(null, null);
            }
        }
    }

    private void loadOutputChannel(ChannelTypeUID channelTypeUID, String acceptedItemType) {
        currentChannel = channelTypeUID.getId();

        List<Channel> channelList = new LinkedList<Channel>(this.getThing().getChannels());
        boolean channelIsAlreadyLoaded = false;
        boolean channelListChanged = false;

        if (!channelList.isEmpty()) {
            Iterator<Channel> channelInter = channelList.iterator();
            while (channelInter.hasNext()) {
                Channel eshChannel = channelInter.next();
                if (channelIsOutputChannel(eshChannel.getUID().getId())) {
                    if (!eshChannel.getUID().getId().equals(currentChannel)) {
                        channelInter.remove();
                        channelListChanged = true;
                    } else {
                        channelIsAlreadyLoaded = true;
                    }
                }
            }
        }

        if (!channelIsAlreadyLoaded && currentChannel != null) {
            Channel channel = ChannelBuilder
                    .create(new ChannelUID(this.getThing().getUID(), channelTypeUID.getId()), acceptedItemType)
                    .withType(channelTypeUID).build();
            channelList.add(channel);
            channelListChanged = true;
        }

        if (channelListChanged) {
            ThingBuilder thingBuilder = editThing();
            thingBuilder.withChannels(channelList);
            updateThing(thingBuilder.build());
            logger.debug("load channel: {} with item: {}", channelTypeUID.getAsString(), acceptedItemType);
        }

    }

    private boolean channelIsOutputChannel(String id) {
        switch (id) {
            case CHANNEL_ID_GENERAL_DIMM:
            case CHANNEL_ID_GENERAL_SWITCH:
            case CHANNEL_ID_BRIGHTNESS:
            case CHANNEL_ID_LIGHT_SWITCH:
            case CHANNEL_ID_SHADE_ANGLE:
            case CHANNEL_ID_GENERAL_COMBINED_2_STAGE_SWITCH:
            case CHANNEL_ID_GENERAL_COMBINED_3_STAGE_SWITCH:
            case CHANNEL_ID_COMBINED_2_STAGE_SWITCH:
            case CHANNEL_ID_COMBINED_3_STAGE_SWITCH:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (device != null) {
            switch (channelUID.getId()) {
                case CHANNEL_ID_GENERAL_DIMM:
                    if (device.isOn()) {
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_GENERAL_DIMM), new PercentType(
                                fromValueToPercent(device.getOutputValue(), device.getMaxOutputValue())));
                    } else {
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_GENERAL_DIMM), new PercentType(0));
                    }
                    break;
                case CHANNEL_ID_GENERAL_SWITCH:
                    if (device.isOn()) {
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_GENERAL_SWITCH), OnOffType.ON);
                    } else {
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_GENERAL_SWITCH), OnOffType.OFF);
                    }
                    break;
                case CHANNEL_ID_BRIGHTNESS:
                    if (device.isOn()) {
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_BRIGHTNESS), new PercentType(
                                fromValueToPercent(device.getOutputValue(), device.getMaxOutputValue())));
                    } else {
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_BRIGHTNESS), new PercentType(0));
                    }
                case CHANNEL_ID_LIGHT_SWITCH:
                    if (device.isOn()) {
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_LIGHT_SWITCH), OnOffType.ON);
                    } else {
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_LIGHT_SWITCH), OnOffType.OFF);
                    }
                    break;
                case CHANNEL_ID_SHADE:
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_SHADE),
                            new PercentType(fromValueToPercent(device.getSlatPosition(), device.getMaxSlatPosition())));
                    break;
                case CHANNEL_ID_SHADE_ANGLE:
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_SHADE_ANGLE),
                            new PercentType(fromValueToPercent(device.getAnglePosition(), device.getMaxSlatAngle())));
                    break;
                case CHANNEL_ID_ELECTRIC_METER:
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_ELECTRIC_METER),
                            new DecimalType(device.getElectricMeter() * 0.01));
                    break;
                case CHANNEL_ID_OUTPUT_CURRENT:
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_OUTPUT_CURRENT),
                            new DecimalType(device.getOutputCurrent()));
                    break;
                case CHANNEL_ID_ACTIVE_POWER:
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_ACTIVE_POWER),
                            new DecimalType(device.getActivePower()));
                    break;
                case CHANNEL_ID_GENERAL_COMBINED_2_STAGE_SWITCH:
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_GENERAL_COMBINED_2_STAGE_SWITCH),
                            new StringType(convertStageValue((short) 2, device.getOutputValue())));
                    break;
                case CHANNEL_ID_GENERAL_COMBINED_3_STAGE_SWITCH:
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_GENERAL_COMBINED_3_STAGE_SWITCH),
                            new StringType(convertStageValue((short) 3, device.getOutputValue())));
                    break;
                case CHANNEL_ID_COMBINED_2_STAGE_SWITCH:
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_COMBINED_2_STAGE_SWITCH),
                            new StringType(convertStageValue((short) 2, device.getOutputValue())));
                    break;
                case CHANNEL_ID_COMBINED_3_STAGE_SWITCH:
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID_COMBINED_3_STAGE_SWITCH),
                            new StringType(convertStageValue((short) 3, device.getOutputValue())));
                    break;
                default:
                    return;
            }
        }
    }

    private String convertStageValue(short stage, short value) {
        switch (stage) {
            case 2:
                if (value < 85) {
                    return "0";
                } else if (value >= 85 && value < 170) {
                    return "90";
                } else if (value >= 170 && value <= 255) {
                    return "200";
                }
            case 3:
                if (value < 64) {
                    return "0";
                } else if (value >= 64 && value < 128) {
                    return "90";
                } else if (value >= 128 && value < 192) {
                    return "130";
                } else if (value >= 192 && value <= 255) {
                    return "200";
                }
        }
        return null;
    }

    private void onDeviceStateInitial(Device device) {
        if (device != null) {
            if (currentChannel != null) {
                if (isLinked(currentChannel)) {
                    channelLinked(new ChannelUID(getThing().getUID(), currentChannel));
                }
            }
            if (!device.isShade()) {
                if (isActivePowerChannelLoaded) {
                    if (getThing().getChannel(CHANNEL_ID_ACTIVE_POWER) != null && isLinked(CHANNEL_ID_ACTIVE_POWER)) {
                        channelLinked(new ChannelUID(getThing().getUID(), CHANNEL_ID_ACTIVE_POWER));
                    }
                }
                if (isOutputCurrentChannelLoaded) {
                    if (getThing().getChannel(CHANNEL_ID_OUTPUT_CURRENT) != null
                            && isLinked(CHANNEL_ID_OUTPUT_CURRENT)) {
                        channelLinked(new ChannelUID(getThing().getUID(), CHANNEL_ID_OUTPUT_CURRENT));
                    }
                }
                if (isElectricMeterChannelLoaded) {
                    if (getThing().getChannel(CHANNEL_ID_ELECTRIC_METER) != null
                            && isLinked(CHANNEL_ID_ELECTRIC_METER)) {
                        channelLinked(new ChannelUID(getThing().getUID(), CHANNEL_ID_ELECTRIC_METER));
                    }
                }
            } else {
                if (isLinked(CHANNEL_ID_SHADE)) {
                    channelLinked(new ChannelUID(getThing().getUID(), CHANNEL_ID_SHADE));
                }
            }
        }
    }

    @Override
    public synchronized void onSceneConfigAdded(short sceneId) {
        if (device != null) {
            String saveScene = "";
            DeviceSceneSpec sceneSpec = device.getSceneConfig(sceneId);
            if (sceneSpec != null) {
                saveScene = sceneSpec.toString();
            }

            Integer[] sceneValue = device.getSceneOutputValue(sceneId);
            if (sceneValue[0] != -1) {
                saveScene = saveScene + ", sceneValue: " + sceneValue[0];
            }
            if (sceneValue[1] != -1) {
                saveScene = saveScene + ", sceneAngle: " + sceneValue[1];
            }
            String key = DigitalSTROMBindingConstants.DEVICE_SCENE + sceneId;
            if (!saveScene.isEmpty()) {
                logger.debug("Save scene configuration: [{}] to thing with UID {}", saveScene, getThing().getUID());
                if (getThing().getProperties().get(key) != null) {
                    updateProperty(key, saveScene);
                } else {
                    Map<String, String> properties = editProperties();
                    properties.put(key, saveScene);
                    updateProperties(properties);
                }
            }
        }
    }

    @Override
    public void onDeviceConfigChanged(ChangeableDeviceConfigEnum whichConfig) {
        Configuration config = editConfiguration();
        switch (whichConfig) {
            case DEVICE_NAME:
                config.put(DEVICE_NAME, device.getName());
                break;
            case METER_DSID:
                config.put(DEVICE_METER_ID, device.getMeterDSID().getValue());
                break;
            case ZONE_ID:
                config.put(DEVICE_ZONE_ID, device.getZoneId());
                break;
            case GROUPS:
                config.put(DEVICE_GROUPS, device.getGroups().toString());
                break;
            case FUNCTIONAL_GROUP:
                config.put(DEVICE_FUNCTIONAL_COLOR_GROUP, device.getFunctionalColorGroup().toString());
                checkOutputChannel();
                break;
            case OUTPUT_MODE:
                config.put(DEVICE_OUTPUT_MODE, device.getOutputMode().toString());
                checkOutputChannel();
                break;
        }
        super.updateConfiguration(config);
    }

    @Override
    public String getDeviceStatusListenerID() {
        return this.dSID;
    }
}
