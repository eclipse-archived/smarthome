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
package org.eclipse.smarthome.binding.digitalstrom.handler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.DeviceStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Circuit;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.GeneralDeviceInformation;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.CachedMeteringValue;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.DeviceStateUpdate;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.ChangeableDeviceConfigEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.MeteringTypeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.MeteringUnitsEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.providers.DsChannelTypeProvider;
import org.eclipse.smarthome.core.library.types.DecimalType;
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

/**
 * The {@link CircuitHandler} is responsible for handling the configuration and updating the metering channels of a
 * digitalStrom circuit. <br>
 * <br>
 * For that it uses the {@link BridgeHandler} to register this class as a {@link DeviceStatusListener} to get informed
 * about changes from the accompanying {@link Circuit}.
 *
 * @author Michael Ochel
 * @author Matthias Siegele
 */
public class CircuitHandler extends BaseThingHandler implements DeviceStatusListener {

    private final Logger logger = LoggerFactory.getLogger(CircuitHandler.class);

    /**
     * Contains all supported thing types of this handler, will be filled by DsDeviceThingTypeProvider.
     */
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<ThingTypeUID>();

    private String dSID;
    private Circuit circuit;

    private BridgeHandler dssBridgeHandler;

    /**
     * Creates a new {@link CircuitHandler}.
     *
     * @param thing must not be null
     */
    public CircuitHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing CircuitHandler.");
        if (StringUtils.isNotBlank((String) getConfig().get(DigitalSTROMBindingConstants.DEVICE_DSID))) {
            dSID = getConfig().get(DigitalSTROMBindingConstants.DEVICE_DSID).toString();
            final Bridge bridge = getBridge();
            if (bridge != null) {
                bridgeStatusChanged(bridge.getStatusInfo());
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
        circuit = null;
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
            } else {
                return null;
            }
        }
        return dssBridgeHandler;
    }

    @Override
    public void thingUpdated(Thing thing) {
        this.thing = thing;
        if (circuit == null) {
            initialize();
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)) {
            if (dSID != null) {
                if (getDssBridgeHandler() != null) {
                    if (circuit == null) {
                        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                                "waiting for listener registration");
                        dssBridgeHandler.registerDeviceStatusListener(this);
                    } else {
                        updateStatus(ThingStatus.ONLINE);
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE);
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
        logger.debug("Set status to {}", getThing().getStatusInfo());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // the same handling like total metering values
        if (dssBridgeHandler != null) {
            dssBridgeHandler.handleCommand(channelUID, command);
        }
    }

    @Override
    public void onDeviceStateChanged(DeviceStateUpdate deviceStateUpdate) {
        if (deviceStateUpdate != null && DeviceStateUpdate.UPDATE_CIRCUIT_METER.equals(deviceStateUpdate.getType())) {
            if (deviceStateUpdate.getValue() instanceof CachedMeteringValue) {
                CachedMeteringValue cachedVal = (CachedMeteringValue) deviceStateUpdate.getValue();
                if (MeteringUnitsEnum.WH.equals(cachedVal.getMeteringUnit())) {
                    if (cachedVal.getMeteringType().equals(MeteringTypeEnum.ENERGY)) {
                        updateState(getChannelID(cachedVal), new DecimalType(cachedVal.getValue() * 0.001));
                    } else {
                        updateState(getChannelID(cachedVal), new DecimalType(cachedVal.getValue()));
                    }
                }
            }
        }

    }

    @Override
    public void onDeviceRemoved(GeneralDeviceInformation device) {
        if (device instanceof Circuit) {
            this.circuit = null;
            if (this.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                if (!((Device) circuit).isPresent()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                            "Circuit is not present in the digitalSTROM-System.");
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                            "Circuit is not avaible in the digitalSTROM-System.");
                }

            }
            logger.debug("Set status to {}", getThing().getStatus());
        }
    }

    @Override
    public void onDeviceAdded(GeneralDeviceInformation device) {
        if (device instanceof Circuit) {
            this.circuit = (Circuit) device;
            if (this.circuit.isPresent()) {
                ThingStatusInfo statusInfo = this.dssBridgeHandler.getThing().getStatusInfo();
                updateStatus(statusInfo.getStatus(), statusInfo.getStatusDetail(), statusInfo.getDescription());
                logger.debug("Set status to {}", getThing().getStatus());

                checkCircuitInfoProperties(this.circuit);

                // load first channel values
                onCircuitStateInitial(this.circuit);
                return;
            }
        }
        onDeviceRemoved(device);
    }

    private void checkCircuitInfoProperties(Circuit device) {
        boolean propertiesChanged = false;
        Map<String, String> properties = editProperties();
        // check device info
        if (device.getName() != null) {
            properties.put(DigitalSTROMBindingConstants.DEVICE_NAME, device.getName());
            propertiesChanged = true;
        }
        if (device.getDSUID() != null) {
            properties.put(DigitalSTROMBindingConstants.DEVICE_UID, device.getDSUID());
            propertiesChanged = true;
        }
        if (device.getHwName() != null) {
            properties.put(DigitalSTROMBindingConstants.HW_NAME, device.getHwName());
            propertiesChanged = true;
        }
        if (device.getHwVersionString() != null) {
            properties.put(DigitalSTROMBindingConstants.HW_VERSION, device.getHwVersionString());
            propertiesChanged = true;
        }
        if (device.getSwVersion() != null) {
            properties.put(DigitalSTROMBindingConstants.SW_VERSION, device.getSwVersion());
            propertiesChanged = true;
        }
        if (device.getApiVersion() != null) {
            properties.put(DigitalSTROMBindingConstants.API_VERSION, device.getApiVersion().toString());
            propertiesChanged = true;
        }
        if (device.getDspSwVersion() != null) {
            properties.put(DigitalSTROMBindingConstants.DSP_SW_VERSION, device.getDspSwVersion().toString());
            propertiesChanged = true;
        }
        if (device.getArmSwVersion() != null) {
            properties.put(DigitalSTROMBindingConstants.ARM_SW_VERSION, device.getArmSwVersion().toString());
            propertiesChanged = true;
        }
        if (propertiesChanged) {
            super.updateProperties(properties);
            propertiesChanged = false;
        }
    }

    private void onCircuitStateInitial(Circuit circuit) {
        if (circuit != null) {
            for (CachedMeteringValue cachedMeterValue : circuit.getAllCachedMeteringValues()) {
                if (cachedMeterValue != null && MeteringUnitsEnum.WH.equals(cachedMeterValue.getMeteringUnit())) {
                    String channelID = getChannelID(cachedMeterValue);
                    if (isLinked(channelID)) {
                        channelLinked(new ChannelUID(getThing().getUID(), channelID));
                    }
                }
            }
        }
    }

    private String getChannelID(CachedMeteringValue cachedMeterValue) {
        return DsChannelTypeProvider.getMeteringChannelID(cachedMeterValue.getMeteringType(),
                cachedMeterValue.getMeteringUnit(), false);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (circuit != null) {
            MeteringTypeEnum meteringType = DsChannelTypeProvider.getMeteringType(channelUID.getId());
            double val = circuit.getMeteringValue(meteringType, MeteringUnitsEnum.WH);
            if (val > -1) {
                if (meteringType.equals(MeteringTypeEnum.ENERGY)) {
                    updateState(channelUID, new DecimalType(val * 0.001));
                } else {
                    updateState(channelUID, new DecimalType(val));
                }
            }
        }
    }

    @Override
    public void onDeviceConfigChanged(ChangeableDeviceConfigEnum whatConfig) {
        // nothing to do, will be registered again
    }

    @Override
    public void onSceneConfigAdded(short sceneID) {
        // nothing to do
    }

    @Override
    public String getDeviceStatusListenerID() {
        return this.dSID;
    }

}
