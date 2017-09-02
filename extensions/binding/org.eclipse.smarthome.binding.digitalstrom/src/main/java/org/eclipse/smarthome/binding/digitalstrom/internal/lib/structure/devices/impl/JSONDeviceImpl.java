/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.config.Config;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.DeviceStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.ChangeableDeviceConfigEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DSID;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceSceneSpec;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceStateUpdate;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceStateUpdateImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.FunctionalColorGroupEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.JSONDeviceSceneSpecImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.OutputModeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.InternalScene;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.constants.SceneEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link JSONDeviceImpl} is the implementation of the {@link Device}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class JSONDeviceImpl implements Device {

    private static final Logger logger = LoggerFactory.getLogger(JSONDeviceImpl.class);

    private Config config;
    private DeviceStatusListener listener = null;

    private DSID dsid = null;
    private DSID meterDSID = null;
    private String dSUID = null;
    private String name = null;
    private int zoneId = 0;
    private List<Short> groupList = new LinkedList<Short>();

    private FunctionalColorGroupEnum functionalGroup = null;
    private String hwInfo;

    private boolean isPresent = false;
    private OutputModeEnum outputMode = null;

    private boolean isOn = false;
    private boolean isOpen = true;
    private short outputValue = 0;
    private short maxOutputValue = DeviceConstants.DEFAULT_MAX_OUTPUTVALUE;
    private short minOutputValue = 0;

    private short slatAngle = 0;
    private short maxSlatAngle = DeviceConstants.MAX_SLAT_ANGLE;
    private short minSlatAngle = DeviceConstants.MIN_SLAT_ANGLE;

    private int slatPosition = 0;
    private int maxSlatPosition = DeviceConstants.MAX_ROLLERSHUTTER;
    private int minSlatPosition = DeviceConstants.MIN_ROLLERSHUTTER;

    private int activePower = 0;
    private int outputCurrent = 0;
    private int electricMeter = 0;

    // for scenes
    private short activeSceneNumber = -1;
    private InternalScene activeScene = null;
    private InternalScene lastScene = null;
    private int outputValueBeforeSceneCall = 0;
    private short slatAngleBeforeSceneCall = 0;
    private boolean lastCallWasUndo = false;

    private Map<Short, DeviceSceneSpec> sceneConfigMap = Collections
            .synchronizedMap(new HashMap<Short, DeviceSceneSpec>());
    private Map<Short, Integer[]> sceneOutputMap = Collections.synchronizedMap(new HashMap<Short, Integer[]>());

    // saves outstanding commands
    private List<DeviceStateUpdate> deviceStateUpdates = Collections
            .synchronizedList(new LinkedList<DeviceStateUpdate>());

    // save the last update time of the sensor data
    private long lastElectricMeterUpdate = System.currentTimeMillis();
    private long lastOutputCurrentUpdate = System.currentTimeMillis();
    private long lastActivePowerUpdate = System.currentTimeMillis();

    // this flags are true, if a sensorJob is initiated to add it to the sensorJobExecuter by the deviceStateUpdates
    // list.
    private boolean electricMeterUpdateInitiated = false;
    private boolean outputCurrentUpdateInitiated = false;
    private boolean activePowerUpdateInitiated = false;

    // sensor data refresh priorities
    private String activePowerRefreshPriority = Config.REFRESH_PRIORITY_NEVER;
    private String electricMeterRefreshPriority = Config.REFRESH_PRIORITY_NEVER;
    private String outputCurrentRefreshPriority = Config.REFRESH_PRIORITY_NEVER;

    /*
     * Cache the last MeterValues to get MeterData directly
     * the key is the output value and the value is an Integer array for the meter data (0 = powerConsumption, 1 =
     * electricMeter, 2 =EnergyMeter)
     */
    private Map<Short, Integer[]> cachedSensorMeterData = Collections.synchronizedMap(new HashMap<Short, Integer[]>());

    /**
     * Creates a new {@link JSONDeviceImpl} from the given DigitalSTROM-Device {@link JsonObject}.
     *
     * @param group json object
     */
    public JSONDeviceImpl(JsonObject object) {
        if (object.get(JSONApiResponseKeysEnum.DEVICE_NAME.getKey()) != null) {
            this.name = object.get(JSONApiResponseKeysEnum.DEVICE_NAME.getKey()).getAsString();
        }
        if (object.get(JSONApiResponseKeysEnum.DEVICE_ID.getKey()) != null) {
            this.dsid = new DSID(object.get(JSONApiResponseKeysEnum.DEVICE_ID.getKey()).getAsString());
        } else if (object.get(JSONApiResponseKeysEnum.DEVICE_ID_QUERY.getKey()) != null) {
            this.dsid = new DSID(object.get(JSONApiResponseKeysEnum.DEVICE_ID_QUERY.getKey()).getAsString());
        }
        if (object.get(JSONApiResponseKeysEnum.DEVICE_METER_ID.getKey()) != null) {
            this.meterDSID = new DSID(object.get(JSONApiResponseKeysEnum.DEVICE_METER_ID.getKey()).getAsString());
        }
        if (object.get(JSONApiResponseKeysEnum.DEVICE_DSUID.getKey()) != null) {
            this.dSUID = object.get(JSONApiResponseKeysEnum.DEVICE_DSUID.getKey()).getAsString();
        }
        if (object.get(JSONApiResponseKeysEnum.DEVICE_ID.getKey()) != null) {
            this.hwInfo = object.get(JSONApiResponseKeysEnum.DEVICE_HW_INFO.getKey()).getAsString();
        }
        if (object.get(JSONApiResponseKeysEnum.DEVICE_ON.getKey()) != null) {
            if (!isShade()) {
                this.isOn = object.get(JSONApiResponseKeysEnum.DEVICE_ON.getKey()).getAsBoolean();
            } else {
                this.isOpen = object.get(JSONApiResponseKeysEnum.DEVICE_ON.getKey()).getAsBoolean();
            }
        }
        if (object.get(JSONApiResponseKeysEnum.DEVICE_IS_PRESENT.getKey()) != null) {
            this.isPresent = object.get(JSONApiResponseKeysEnum.DEVICE_IS_PRESENT.getKey()).getAsBoolean();
        } else if (object.get(JSONApiResponseKeysEnum.DEVICE_IS_PRESENT_QUERY.getKey()) != null) {
            this.isPresent = object.get(JSONApiResponseKeysEnum.DEVICE_IS_PRESENT_QUERY.getKey()).getAsBoolean();
        }
        if (object.get(JSONApiResponseKeysEnum.DEVICE_ZONE_ID.getKey()) != null) {
            zoneId = object.get(JSONApiResponseKeysEnum.DEVICE_ZONE_ID.getKey()).getAsInt();
        } else if (object.get(JSONApiResponseKeysEnum.DEVICE_ZONE_ID_QUERY.getKey()) != null) {
            zoneId = object.get(JSONApiResponseKeysEnum.DEVICE_ZONE_ID_QUERY.getKey()).getAsInt();
        }
        if (object.get(JSONApiResponseKeysEnum.DEVICE_GROUPS.getKey()) instanceof JsonArray) {
            JsonArray array = (JsonArray) object.get(JSONApiResponseKeysEnum.DEVICE_GROUPS.getKey());
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i) != null) {
                    short tmp = array.get(i).getAsShort();
                    if (tmp != -1) {
                        this.groupList.add(tmp);
                        if (FunctionalColorGroupEnum.containsColorGroup((int) tmp)) {
                            if (this.functionalGroup == null || !FunctionalColorGroupEnum.getColorGroup((int) tmp)
                                    .equals(FunctionalColorGroupEnum.BLACK)) {
                                this.functionalGroup = FunctionalColorGroupEnum.getColorGroup((int) tmp);
                            }
                        }
                    }
                }
            }
        }
        if (object.get(JSONApiResponseKeysEnum.DEVICE_OUTPUT_MODE.getKey()) != null) {
            int tmp = object.get(JSONApiResponseKeysEnum.DEVICE_OUTPUT_MODE.getKey()).getAsInt();
            if (tmp != -1) {
                if (OutputModeEnum.containsMode(tmp)) {
                    outputMode = OutputModeEnum.getMode(tmp);
                }
            }
        }
        init();
    }

    private void init() {
        if (groupList.contains((short) 1)) {
            maxOutputValue = DeviceConstants.MAX_OUTPUT_VALUE_LIGHT;
            if (this.isDimmable()) {
                minOutputValue = DeviceConstants.MIN_DIM_VALUE;
            }
        } else {
            maxOutputValue = DeviceConstants.DEFAULT_MAX_OUTPUTVALUE;
            minOutputValue = 0;
        }
        if (isOn) {
            outputValue = DeviceConstants.DEFAULT_MAX_OUTPUTVALUE;
        }
    }

    @Override
    public DSID getDSID() {
        return dsid;
    }

    @Override
    public String getDSUID() {
        return this.dSUID;
    }

    @Override
    public synchronized DSID getMeterDSID() {
        return this.meterDSID;
    }

    @Override
    public synchronized void setMeterDSID(String meterDSID) {
        this.meterDSID = new DSID(meterDSID);
        if (listener != null) {
            listener.onDeviceConfigChanged(ChangeableDeviceConfigEnum.METER_DSID);
        }
    }

    @Override
    public String getHWinfo() {
        return hwInfo;
    }

    @Override
    public synchronized String getName() {
        return this.name;
    }

    @Override
    public synchronized void setName(String name) {
        this.name = name;
        if (listener != null) {
            listener.onDeviceConfigChanged(ChangeableDeviceConfigEnum.DEVICE_NAME);
        }
    }

    @Override
    public List<Short> getGroups() {
        return new LinkedList<Short>(groupList);
    }

    @Override
    public void addGroup(Short groupID) {
        if (!this.groupList.contains(groupID)) {
            this.groupList.add(groupID);
        }
        if (listener != null) {
            listener.onDeviceConfigChanged(ChangeableDeviceConfigEnum.GROUPS);
        }
    }

    @Override
    public void setGroups(List<Short> newGroupList) {
        if (newGroupList != null) {
            this.groupList = newGroupList;
        }
        if (listener != null) {
            listener.onDeviceConfigChanged(ChangeableDeviceConfigEnum.GROUPS);
        }
    }

    @Override
    public synchronized int getZoneId() {
        return zoneId;
    }

    @Override
    public synchronized void setZoneId(int zoneID) {
        this.zoneId = zoneID;
        if (listener != null) {
            listener.onDeviceConfigChanged(ChangeableDeviceConfigEnum.ZONE_ID);
        }
    }

    @Override
    public synchronized boolean isPresent() {
        return isPresent;
    }

    @Override
    public synchronized void setIsPresent(boolean isPresent) {
        this.isPresent = isPresent;
        if (listener != null) {
            if (!isPresent) {
                listener.onDeviceRemoved(this);
            } else {
                listener.onDeviceAdded(this);
            }
        }
    }

    @Override
    public synchronized boolean isOn() {
        return isOn;
    }

    @Override
    public synchronized void setIsOn(boolean flag) {
        if (flag) {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ON_OFF, 1));
        } else {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ON_OFF, -1));
        }
    }

    @Override
    public synchronized boolean isOpen() {
        return this.isOpen;
    }

    @Override
    public synchronized void setIsOpen(boolean flag) {
        if (flag) {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_OPEN_CLOSE, 1));
            if (isBlind()) {
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_OPEN_CLOSE_ANGLE, 1));
            }
        } else {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_OPEN_CLOSE, -1));
            if (isBlind()) {
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_OPEN_CLOSE_ANGLE, -1));
            }
        }
    }

    @Override
    public synchronized void setOutputValue(short value) {
        if (!isShade()) {
            if (value <= 0) {
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ON_OFF, -1));

            } else if (value > maxOutputValue) {
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ON_OFF, 1));
            } else {
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_BRIGHTNESS, value));
            }
        }
    }

    @Override
    public synchronized boolean isDimmable() {
        return OutputModeEnum.outputModeIsDimmable(outputMode);
    }

    @Override
    public synchronized boolean isSwitch() {
        return OutputModeEnum.outputModeIsSwitch(outputMode);
    }

    @Override
    public synchronized boolean isDeviceWithOutput() {
        return this.outputMode != null && !this.outputMode.equals(OutputModeEnum.DISABLED);
    }

    @Override
    public synchronized FunctionalColorGroupEnum getFunctionalColorGroup() {
        return this.functionalGroup;
    }

    @Override
    public synchronized void setFunctionalColorGroup(FunctionalColorGroupEnum fuctionalColorGroup) {
        this.functionalGroup = fuctionalColorGroup;
        if (listener != null) {
            listener.onDeviceConfigChanged(ChangeableDeviceConfigEnum.FUNCTIONAL_GROUP);
        }
    }

    @Override
    public OutputModeEnum getOutputMode() {
        return outputMode;
    }

    @Override
    public synchronized void setOutputMode(OutputModeEnum newOutputMode) {
        this.outputMode = newOutputMode;
        if (listener != null) {
            listener.onDeviceConfigChanged(ChangeableDeviceConfigEnum.OUTPUT_MODE);
        }
    }

    @Override
    public synchronized void increase() {
        if (isDimmable()) {
            deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_BRIGHTNESS_INCREASE, 0));
        }
        if (isShade()) {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLAT_INCREASE, 0));
            if (isBlind()) {
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLAT_ANGLE_DECREASE, 0));
            }
        }
    }

    @Override
    public synchronized void decrease() {
        if (isDimmable()) {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_BRIGHTNESS_DECREASE, 0));
        }
        if (isShade()) {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLAT_DECREASE, 0));
            if (isBlind()) {
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLAT_ANGLE_DECREASE, 0));
            }
        }
    }

    @Override
    public synchronized void increaseSlatAngle() {
        if (isBlind()) {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLAT_ANGLE_DECREASE, 1));
        }
    }

    @Override
    public synchronized void decreaseSlatAngle() {
        if (isBlind()) {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLAT_ANGLE_DECREASE, 1));
        }
    }

    @Override
    public synchronized short getOutputValue() {
        return outputValue;
    }

    @Override
    public short getMaxOutputValue() {
        return maxOutputValue;
    }

    @Override
    public short getMinOutputValue() {
        return minOutputValue;
    }

    @Override
    public boolean isShade() {
        return OutputModeEnum.outputModeIsShade(outputMode);
    }

    @Override
    public boolean isBlind() {
        return outputMode.equals(OutputModeEnum.POSITION_CON_US);
    }

    @Override
    public synchronized int getSlatPosition() {
        return slatPosition;
    }

    @Override
    public synchronized short getAnglePosition() {
        return slatAngle;
    }

    @Override
    public synchronized void setAnglePosition(int angle) {
        if (angle == slatAngle) {
            return;
        }
        if (angle < minSlatAngle) {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLAT_ANGLE, minSlatAngle));
        } else if (angle > this.maxSlatPosition) {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLAT_ANGLE, maxSlatAngle));
        } else {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLAT_ANGLE, angle));
        }
    }

    @Override
    public synchronized void setSlatPosition(int position) {
        if (position == this.slatPosition) {
            return;
        }
        if (position < minSlatPosition) {
            this.deviceStateUpdates
                    .add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLATPOSITION, minSlatPosition));
        } else if (position > this.maxSlatPosition) {
            this.deviceStateUpdates
                    .add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLATPOSITION, maxSlatPosition));
        } else {
            this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLATPOSITION, position));
        }
    }

    @Override
    public synchronized int getActivePower() {
        return activePower;
    }

    @Override
    public synchronized void setActivePower(int activePower) {
        activePowerUpdateInitiated = false;
        if (activePower >= 0) {
            lastActivePowerUpdate = System.currentTimeMillis();
            if (activePower == this.activePower) {
                return;
            }
            int standby = Config.DEFAULT_STANDBY_ACTIVE_POWER;
            if (config != null) {
                standby = config.getStandbyActivePower();
            }
            if (outputMode.equals(OutputModeEnum.WIPE) && !isOn && activePower > standby) {
                this.updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ON_OFF, 1));
            }
            informListenerAboutStateUpdate(
                    new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ACTIVE_POWER, activePower));
            this.activePower = activePower;
            this.addActivePowerToMeterCache(this.getOutputValue(), activePower);
        }
    }

    @Override
    public synchronized int getOutputCurrent() {
        return outputCurrent;
    }

    @Override
    public synchronized void setOutputCurrent(int outputCurrent) {
        outputCurrentUpdateInitiated = false;
        if (outputCurrent >= 0) {
            lastOutputCurrentUpdate = System.currentTimeMillis();
            if (outputCurrent == this.outputCurrent) {
                return;
            }
            informListenerAboutStateUpdate(
                    new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_OUTPUT_CURRENT, outputCurrent));
            this.addEnergyMeterToMeterCache(this.getOutputValue(), outputCurrent);
            this.outputCurrent = outputCurrent;
        }
    }

    @Override
    public synchronized int getElectricMeter() {
        return electricMeter;
    }

    @Override
    public synchronized void setElectricMeter(int electricMeter) {
        electricMeterUpdateInitiated = false;
        if (electricMeter >= 0) {
            lastElectricMeterUpdate = System.currentTimeMillis();
            if (electricMeter == this.electricMeter) {
                return;
            }
            informListenerAboutStateUpdate(
                    new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ELECTRIC_METER, electricMeter));
            this.electricMeter = electricMeter;
        }
    }

    private void addActivePowerToMeterCache(short outputValue, int activePower) {
        Integer[] cachedMeterData = cachedSensorMeterData.get(outputValue);
        if (cachedMeterData == null) {
            cachedMeterData = new Integer[2];
        }
        cachedMeterData[0] = activePower;
        this.cachedSensorMeterData.put(outputValue, cachedMeterData);
    }

    private void addEnergyMeterToMeterCache(short outputValue, int energyMeter) {
        Integer[] cachedMeterData = cachedSensorMeterData.get(outputValue);
        if (cachedMeterData == null) {
            cachedMeterData = new Integer[2];
        }
        cachedMeterData[1] = energyMeter;
        this.cachedSensorMeterData.put(outputValue, cachedMeterData);
    }

    private short getDimmStep() {
        if (isDimmable()) {
            return DeviceConstants.DIM_STEP_LIGHT;
        } else if (isShade()) {
            return DeviceConstants.MOVE_STEP_ROLLERSHUTTER;
        } else {
            return DeviceConstants.DEFAULT_MOVE_STEP;
        }
    }

    @Override
    public int getMaxSlatPosition() {
        return maxSlatPosition;
    }

    @Override
    public int getMinSlatPosition() {
        return minSlatPosition;
    }

    @Override
    public int getMaxSlatAngle() {
        return maxSlatAngle;
    }

    @Override
    public int getMinSlatAngle() {
        return minSlatAngle;
    }

    /* Begin-Scenes */

    @Override
    public synchronized void callInternalScene(InternalScene scene) {
        if (lastCallWasUndo) {
            lastScene = null;
            if (activeScene != null) {
                activeScene.deactivateSceneByDevice();
            }
            activeScene = null;
        }
        internalCallScene(scene.getSceneID());
        activeScene = scene;
        lastCallWasUndo = false;
    }

    @Override
    public void checkSceneConfig(Short sceneNumber, int prio) {
        if (isDeviceWithOutput()) {
            if (!containsSceneConfig(sceneNumber)) {
                deviceStateUpdates
                        .add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SCENE_CONFIG, prio + sceneNumber));

            }
            if (sceneOutputMap.get(sceneNumber) == null) {
                deviceStateUpdates
                        .add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SCENE_OUTPUT, prio + sceneNumber));
            }
        }
    }

    @Override
    public synchronized void undoInternalScene(InternalScene scene) {
        logger.debug("undo Scene {} dSID {}", scene.getSceneID(), dsid.getValue());
        if (activeScene != null && activeScene.equals(scene)) {
            if (lastCallWasUndo) {
                lastScene = null;
                return;
            }
            if (this.lastScene != null && !lastScene.equals(activeScene)) {
                activeScene = lastScene;
                lastScene = null;
                activeScene.activateSceneByDevice();
            } else {
                internalUndoScene();
                logger.debug("internalUndo Scene dSID {}", dsid.getValue());
                this.activeScene = null;
            }
            lastCallWasUndo = true;
        }
    }

    @Override
    public synchronized void callScene(Short sceneNumber) {
        this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_CALL_SCENE, sceneNumber));
    }

    @Override
    public synchronized void internalCallScene(Short sceneNumber) {
        logger.debug("call Scene id {} dSID {}", sceneNumber, dsid.getValue());
        if (isDeviceWithOutput()) {
            activeSceneNumber = sceneNumber;
            informLastSceneAboutSceneCall(sceneNumber);
            if (!isShade()) {
                outputValueBeforeSceneCall = this.outputValue;
            } else {
                outputValueBeforeSceneCall = this.slatPosition;
                if (isBlind()) {
                    slatAngleBeforeSceneCall = this.slatAngle;
                }
            }
            if (!checkSceneNumber(sceneNumber)) {
                if (containsSceneConfig(sceneNumber)) {
                    if (doIgnoreScene(sceneNumber)) {
                        return;
                    }
                } else {
                    this.deviceStateUpdates
                            .add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SCENE_CONFIG, sceneNumber));
                }
                if (sceneOutputMap.get(sceneNumber) != null) {
                    if (!isShade()) {
                        updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_BRIGHTNESS,
                                sceneOutputMap.get(sceneNumber)[0]));
                    } else {
                        updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLATPOSITION,
                                sceneOutputMap.get(sceneNumber)[0]));
                        if (isBlind()) {
                            updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLAT_ANGLE,
                                    sceneOutputMap.get(sceneNumber)[1]));
                        }
                    }
                } else {
                    this.deviceStateUpdates
                            .add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SCENE_OUTPUT, sceneNumber));
                }
            }

        }
    }

    private boolean checkSceneNumber(Short sceneNumber) {
        if (this.outputMode.equals(OutputModeEnum.POWERSAVE)) {
            switch (SceneEnum.getScene(sceneNumber)) {
                case ABSENT:
                case DEEP_OFF:
                case SLEEPING:
                    this.updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ON_OFF, -1));
                    return true;
                case AREA_1_OFF:
                case AREA_2_OFF:
                case AREA_3_OFF:
                case AREA_4_OFF:
                case PRESET_0:
                case PRESET_10:
                case PRESET_20:
                case PRESET_30:
                case PRESET_40:
                    return true;
                default:
                    break;
            }
        }
        if (this.outputMode.equals(OutputModeEnum.WIPE)) {
            switch (SceneEnum.getScene(sceneNumber)) {
                case STANDBY:
                case AUTO_STANDBY:
                case AREA_1_OFF:
                case AREA_2_OFF:
                case AREA_3_OFF:
                case AREA_4_OFF:
                case PRESET_0:
                case PRESET_10:
                case PRESET_20:
                case PRESET_30:
                case PRESET_40:
                    this.updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ON_OFF, -1));
                    return true;
                default:
                    break;
            }
        }
        switch (SceneEnum.getScene(sceneNumber)) {
            // on scenes
            case DEVICE_ON:
            case MAXIMUM:
                if (!isShade()) {
                    this.updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ON_OFF, 1));
                } else {
                    this.updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_OPEN_CLOSE, 1));
                    if (isBlind()) {
                        this.updateInternalDeviceState(
                                new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_OPEN_CLOSE_ANGLE, 1));
                    }
                }
                return true;
            // off scenes
            case MINIMUM:
            case DEVICE_OFF:
            case AUTO_OFF:
                if (!isShade()) {
                    this.updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ON_OFF, -1));
                } else {
                    this.updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_OPEN_CLOSE, -1));
                    if (isBlind()) {
                        this.updateInternalDeviceState(
                                new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_OPEN_CLOSE_ANGLE, -1));
                    }
                }
                return true;
            // increase scenes
            case INCREMENT:
            case AREA_1_INCREMENT:
            case AREA_2_INCREMENT:
            case AREA_3_INCREMENT:
            case AREA_4_INCREMENT:
                if (isDimmable()) {
                    if (outputValue == maxOutputValue) {
                        return true;
                    }
                    this.updateInternalDeviceState(
                            new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_BRIGHTNESS_INCREASE, 0));
                }
                if (isShade()) {
                    if (slatPosition == maxSlatPosition) {
                        return true;
                    }
                    this.updateInternalDeviceState(
                            new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLAT_INCREASE, 0));
                    if (isBlind()) {
                        if (slatAngle == maxSlatAngle) {
                            return true;
                        }
                        updateInternalDeviceState(
                                new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLAT_ANGLE_INCREASE, 0));
                    }
                }
                return true;
            // decrease scenes
            case DECREMENT:
            case AREA_1_DECREMENT:
            case AREA_2_DECREMENT:
            case AREA_3_DECREMENT:
            case AREA_4_DECREMENT:
                if (isDimmable()) {
                    if (outputValue == minOutputValue) {
                        return true;
                    }
                    updateInternalDeviceState(
                            new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_BRIGHTNESS_DECREASE, 0));
                }
                if (isShade()) {
                    if (slatPosition == minSlatPosition) {
                        return true;
                    }
                    updateInternalDeviceState(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLAT_DECREASE, 0));
                    if (isBlind()) {
                        if (slatAngle == minSlatAngle) {
                            return true;
                        }
                        this.updateInternalDeviceState(
                                new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLAT_ANGLE_INCREASE, 0));
                    }
                }
                return true;
            // Stop scenes
            case AREA_1_STOP:
            case AREA_2_STOP:
            case AREA_3_STOP:
            case AREA_4_STOP:
            case DEVICE_STOP:
            case STOP:
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_OUTPUT_VALUE, 0));
                return true;
            // Area Stepping continue scenes
            case AREA_STEPPING_CONTINUE:
                // TODO: we don't know what will be happened when this scene was called. Some one know it?
                return true;
            default:
                return false;
        }
    }

    private Integer[] getStandartSceneOutput(short sceneNumber) {
        switch (SceneEnum.getScene(sceneNumber)) {
            case DEVICE_ON:
            case MAXIMUM:
                if (!isShade()) {
                    return new Integer[] { (int) maxOutputValue, -1 };
                } else {
                    if (isBlind()) {
                        return new Integer[] { (int) maxSlatPosition, (int) maxSlatAngle };
                    } else {
                        return new Integer[] { (int) maxSlatPosition, -1 };
                    }
                }
                // off scenes
            case MINIMUM:
            case DEVICE_OFF:
            case AUTO_OFF:
                if (!isShade()) {
                    return new Integer[] { (int) 0, -1 };
                } else {
                    if (isBlind()) {
                        return new Integer[] { (int) 0, 0 };
                    } else {
                        return new Integer[] { (int) 0, -1 };
                    }
                }
            default:
                break;
        }
        return null;
    }

    private void informLastSceneAboutSceneCall(short sceneNumber) {
        if (this.activeScene != null && this.activeScene.getSceneID() != sceneNumber) {
            this.activeScene.deactivateSceneByDevice();
            this.lastScene = this.activeScene;
            this.activeScene = null;
        }
    }

    @Override
    public synchronized void undoScene() {
        this.deviceStateUpdates
                .add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_UNDO_SCENE, this.activeSceneNumber));
    }

    @Override
    public synchronized void internalUndoScene() {
        if (!isShade()) {
            updateInternalDeviceState(
                    new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_BRIGHTNESS, this.outputValueBeforeSceneCall));
        } else {
            updateInternalDeviceState(
                    new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLATPOSITION, this.outputValueBeforeSceneCall));
            if (isBlind()) {
                updateInternalDeviceState(
                        new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLAT_ANGLE, this.slatAngleBeforeSceneCall));
            }
        }

        if (activeSceneNumber != -1) {
            activeSceneNumber = -1;
        }
    }

    @Override
    public InternalScene getAcitiveScene() {
        return this.activeScene;
    }

    @Override
    public Integer[] getSceneOutputValue(short sceneId) {
        synchronized (sceneOutputMap) {
            if (sceneOutputMap.containsKey(sceneId)) {
                return sceneOutputMap.get(sceneId);
            }
        }
        return new Integer[] { -1, -1 };
    }

    @Override
    public void setSceneOutputValue(short sceneId, int value) {
        synchronized (sceneOutputMap) {
            sceneOutputMap.put(sceneId, new Integer[] { value, -1 });
            if (listener != null) {
                listener.onSceneConfigAdded(sceneId);
            }
        }
    }

    @Override
    public void setSceneOutputValue(short sceneId, int value, int angle) {
        synchronized (sceneOutputMap) {
            sceneOutputMap.put(sceneId, new Integer[] { value, angle });
            if (listener != null) {
                listener.onSceneConfigAdded(sceneId);
            }
        }
    }

    @Override
    public List<Short> getSavedScenes() {
        Set<Short> bothKeySet = new HashSet<Short>(sceneOutputMap.keySet());
        bothKeySet.addAll(sceneConfigMap.keySet());
        return new LinkedList<Short>(bothKeySet);
    }

    @Override
    public void addSceneConfig(short sceneId, DeviceSceneSpec sceneSpec) {
        if (sceneSpec != null) {
            synchronized (sceneConfigMap) {
                sceneConfigMap.put(sceneId, sceneSpec);
                if (listener != null) {
                    listener.onSceneConfigAdded(sceneId);
                }
            }
        }
    }

    @Override
    public DeviceSceneSpec getSceneConfig(short sceneId) {
        synchronized (sceneConfigMap) {
            return sceneConfigMap.get(sceneId);
        }
    }

    @Override
    public boolean doIgnoreScene(short sceneId) {
        synchronized (sceneConfigMap) {
            if (this.sceneConfigMap.containsKey(sceneId)) {
                return this.sceneConfigMap.get(sceneId).isDontCare();
            }
        }
        return false;
    }

    @Override
    public boolean containsSceneConfig(short sceneId) {
        synchronized (sceneConfigMap) {
            return sceneConfigMap.containsKey(sceneId);
        }
    }

    /* End-Scenes */

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Device) {
            Device device = (Device) obj;
            return device.getDSID().equals(this.getDSID());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getDSID().hashCode();
    }

    @Override
    public boolean isActivePowerUpToDate() {
        return (outputMode.equals(OutputModeEnum.WIPE) && !isOn)
                || (isOn && !isShade()) && !this.activePowerRefreshPriority.contains(Config.REFRESH_PRIORITY_NEVER)
                        ? checkSensorRefreshTime(lastActivePowerUpdate) : true;
    }

    @Override
    public boolean isElectricMeterUpToDate() {
        return (isOn || this.electricMeter == 0) && !isShade()
                && !this.electricMeterRefreshPriority.contains(Config.REFRESH_PRIORITY_NEVER)
                        ? checkSensorRefreshTime(lastElectricMeterUpdate) : true;
    }

    @Override
    public boolean isOutputCurrentUpToDate() {
        return isOn && !isShade() && !this.outputCurrentRefreshPriority.contains(Config.REFRESH_PRIORITY_NEVER)
                ? checkSensorRefreshTime(lastOutputCurrentUpdate) : true;
    }

    private boolean checkSensorRefreshTime(long lastTime) {
        int refresh = Config.DEFAULT_SENSORDATA_REFRESH_INTERVAL;
        if (config != null) {
            refresh = config.getSensordataRefreshInterval();
        }
        return (lastTime + refresh) > System.currentTimeMillis();
    }

    @Override
    public boolean isSensorDataUpToDate() {
        return isActivePowerUpToDate() && isElectricMeterUpToDate() && isOutputCurrentUpToDate();
    }

    @Override
    public void setSensorDataRefreshPriority(String activePowerRefreshPriority, String electricMeterRefreshPriority,
            String outputCurrentRefreshPriority) {
        if (checkPriority(activePowerRefreshPriority) != null) {
            this.activePowerRefreshPriority = activePowerRefreshPriority;
        }
        if (checkPriority(electricMeterRefreshPriority) != null) {
            this.electricMeterRefreshPriority = electricMeterRefreshPriority;
        }
        if (checkPriority(outputCurrentRefreshPriority) != null) {
            this.outputCurrentRefreshPriority = outputCurrentRefreshPriority;
        }
    }

    private String checkPriority(String priority) {
        switch (priority) {
            case Config.REFRESH_PRIORITY_HIGH:
                break;
            case Config.REFRESH_PRIORITY_MEDIUM:
                break;
            case Config.REFRESH_PRIORITY_LOW:
                break;
            case Config.REFRESH_PRIORITY_NEVER:
                break;
            default:
                logger.error("Sensor data update priority do not exist! Please check the input!");
                return null;
        }
        return priority;
    }

    @Override
    public String getActivePowerRefreshPriority() {
        if (outputMode.equals(OutputModeEnum.WIPE) && !isOn) {
            return Config.REFRESH_PRIORITY_LOW;
        }
        return this.activePowerRefreshPriority;
    }

    @Override
    public String getElectricMeterRefreshPriority() {
        return this.electricMeterRefreshPriority;
    }

    @Override
    public String getOutputCurrentRefreshPriority() {
        return this.outputCurrentRefreshPriority;
    }

    @Override
    public boolean isDeviceUpToDate() {
        if (!isActivePowerUpToDate()) {
            if (!activePowerUpdateInitiated) {
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ACTIVE_POWER, 0));
                activePowerUpdateInitiated = true;
            }
        }
        if (!isElectricMeterUpToDate()) {
            if (!electricMeterUpdateInitiated) {
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ELECTRIC_METER, 0));
                electricMeterUpdateInitiated = true;
            }
        }
        if (!isOutputCurrentUpToDate()) {
            if (!outputCurrentUpdateInitiated) {
                this.deviceStateUpdates.add(new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_OUTPUT_CURRENT, 0));
                outputCurrentUpdateInitiated = true;
            }
        }
        return this.deviceStateUpdates.isEmpty();
    }

    @Override
    public DeviceStateUpdate getNextDeviceUpdateState() {
        return !this.deviceStateUpdates.isEmpty() ? this.deviceStateUpdates.remove(0) : null;
    }

    private int internalSetOutputValue(int value) {
        if (isShade()) {
            slatPosition = value;
            if (slatPosition <= 0) {
                this.slatPosition = 0;
                this.isOpen = false;
            } else {
                this.isOpen = true;
            }
            return slatPosition;
        } else {
            outputValue = (short) value;
            if (outputValue <= 0) {
                this.isOn = false;
                setActivePower(0);
                setOutputCurrent(0);
                electricMeterUpdateInitiated = false;
            } else {
                this.isOn = true;
                setCachedMeterData();
            }
            return outputValue;
        }
    }

    private short internalSetAngleValue(int value) {
        if (value < 0) {
            slatAngle = 0;
        }
        if (value > maxSlatAngle) {
            slatAngle = maxSlatAngle;
        } else {
            slatAngle = (short) value;
        }
        return slatAngle;
    }

    @Override
    public synchronized void updateInternalDeviceState(DeviceStateUpdate deviceStateUpdate) {
        if (deviceStateUpdate != null) {
            switch (deviceStateUpdate.getType()) {
                case DeviceStateUpdate.UPDATE_BRIGHTNESS_DECREASE:
                    deviceStateUpdate = new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_BRIGHTNESS_DECREASE,
                            internalSetOutputValue(outputValue - getDimmStep()));
                    break;
                case DeviceStateUpdate.UPDATE_BRIGHTNESS_INCREASE:
                    deviceStateUpdate = new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_BRIGHTNESS_INCREASE,
                            internalSetOutputValue(outputValue + getDimmStep()));
                    break;
                case DeviceStateUpdate.UPDATE_BRIGHTNESS:
                    internalSetOutputValue(deviceStateUpdate.getValue());
                    break;
                case DeviceStateUpdate.UPDATE_ON_OFF:
                    if (deviceStateUpdate.getValue() < 0) {
                        internalSetOutputValue(0);
                    } else {
                        internalSetOutputValue(maxOutputValue);
                    }
                    break;
                case DeviceStateUpdate.UPDATE_OPEN_CLOSE:
                    if (deviceStateUpdate.getValue() < 0) {
                        internalSetOutputValue(0);
                    } else {
                        internalSetOutputValue(maxSlatPosition);
                    }
                    break;
                case DeviceStateUpdate.UPDATE_OPEN_CLOSE_ANGLE:
                    if (deviceStateUpdate.getValue() < 0) {
                        internalSetAngleValue(0);
                    } else {
                        internalSetAngleValue(maxSlatAngle);
                    }
                    break;
                case DeviceStateUpdate.UPDATE_SLAT_DECREASE:
                    deviceStateUpdate = new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLAT_DECREASE,
                            internalSetOutputValue(slatPosition - getDimmStep()));
                    break;
                case DeviceStateUpdate.UPDATE_SLAT_INCREASE:
                    deviceStateUpdate = new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLAT_INCREASE,
                            internalSetOutputValue(slatPosition + getDimmStep()));
                case DeviceStateUpdate.UPDATE_SLATPOSITION:
                    internalSetOutputValue(deviceStateUpdate.getValue());
                    break;
                case DeviceStateUpdate.UPDATE_SLAT_ANGLE_DECREASE:
                    deviceStateUpdate = new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLAT_ANGLE_DECREASE,
                            internalSetAngleValue(slatAngle - DeviceConstants.ANGLE_STEP_SLAT));
                    break;
                case DeviceStateUpdate.UPDATE_SLAT_ANGLE_INCREASE:
                    deviceStateUpdate = new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLAT_ANGLE_INCREASE,
                            internalSetAngleValue(slatAngle + DeviceConstants.ANGLE_STEP_SLAT));
                    break;
                case DeviceStateUpdate.UPDATE_SLAT_ANGLE:
                    internalSetAngleValue(deviceStateUpdate.getValue());
                    break;
                case DeviceStateUpdate.UPDATE_ELECTRIC_METER:
                    setElectricMeter(deviceStateUpdate.getValue());
                    return;
                case DeviceStateUpdate.UPDATE_OUTPUT_CURRENT:
                    setOutputCurrent(deviceStateUpdate.getValue());
                    return;
                case DeviceStateUpdate.UPDATE_ACTIVE_POWER:
                    setActivePower(deviceStateUpdate.getValue());
                    return;
                case DeviceStateUpdate.UPDATE_CALL_SCENE:
                    this.internalCallScene((short) deviceStateUpdate.getValue());
                    return;
                case DeviceStateUpdate.UPDATE_UNDO_SCENE:
                    this.internalUndoScene();
                    return;
                default:
                    return;
            }
            if (activeScene != null) {
                Integer[] sceneOutput = getStandartSceneOutput(activeScene.getSceneID());
                if (sceneOutput == null) {
                    sceneOutput = sceneOutputMap.get(activeScene.getSceneID());
                }
                if (sceneOutput != null) {
                    boolean outputChanged = false;
                    if (isShade()) {
                        if (isBlind() && sceneOutput[1] != slatAngle) {
                            logger.debug("Scene output angle: {} setted output value {}", sceneOutput[1], slatAngle);
                            outputChanged = true;
                        }
                        if (sceneOutput[0] != slatPosition) {
                            logger.debug("Scene output value: {} setted output value {}", sceneOutput[0], slatPosition);
                            outputChanged = true;
                        }
                    } else {
                        if (sceneOutput[0] != outputValue) {
                            outputChanged = true;
                        }
                    }
                    if (outputChanged) {
                        logger.debug("Device output from Device with dSID {} changed deactivate scene {}",
                                dsid.getValue(), activeScene.getID());
                        activeScene.deviceSceneChanged((short) -1);
                        lastScene = null;
                        activeScene = null;
                    }

                } else {
                    lastScene = null;
                }
            }
            informListenerAboutStateUpdate(deviceStateUpdate);
        }
    }

    @Override
    public void registerDeviceStateListener(DeviceStatusListener listener) {
        if (listener != null) {
            this.listener = listener;
            listener.onDeviceAdded(this);
        }
    }

    @Override
    public DeviceStatusListener unregisterDeviceStateListener() {
        activePowerRefreshPriority = Config.REFRESH_PRIORITY_NEVER;
        electricMeterRefreshPriority = Config.REFRESH_PRIORITY_NEVER;
        outputCurrentRefreshPriority = Config.REFRESH_PRIORITY_NEVER;
        DeviceStatusListener listener = this.listener;
        this.listener = null;
        return listener;
    }

    @Override
    public boolean isListenerRegisterd() {
        return (listener != null);
    }

    private void setCachedMeterData() {
        logger.debug("load cached sensor data device with dsid {}", dsid.getValue());
        Integer[] cachedSensorData = this.cachedSensorMeterData.get(this.getOutputValue());
        if (cachedSensorData != null) {
            if (cachedSensorData[0] != null
                    && !this.activePowerRefreshPriority.contains(Config.REFRESH_PRIORITY_NEVER)) {
                this.activePower = cachedSensorData[0];
                informListenerAboutStateUpdate(
                        new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ACTIVE_POWER, cachedSensorData[0]));

            }
            if (cachedSensorData[1] != null
                    && !this.outputCurrentRefreshPriority.contains(Config.REFRESH_PRIORITY_NEVER)) {
                this.outputCurrent = cachedSensorData[1];
                informListenerAboutStateUpdate(
                        new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_OUTPUT_CURRENT, cachedSensorData[1]));
            }
        }
    }

    /**
     * if an {@link DeviceStatusListener} is registered inform him about the new state otherwise do nothing.
     *
     * @param deviceStateUpdate
     */
    private void informListenerAboutStateUpdate(DeviceStateUpdate deviceStateUpdate) {
        if (listener != null) {
            logger.debug("Inform listener about device state changed: type: {}, value: {}", deviceStateUpdate.getType(),
                    deviceStateUpdate.getValue());
            listener.onDeviceStateChanged(deviceStateUpdate);
        }
    }

    @SuppressWarnings("null")
    @Override
    public void saveConfigSceneSpecificationIntoDevice(Map<String, String> propertries) {
        if (propertries != null) {
            String sceneSave;
            for (short i = 0; i < 128; i++) {
                sceneSave = propertries.get(DigitalSTROMBindingConstants.DEVICE_SCENE + i);
                if (StringUtils.isNotBlank(sceneSave)) {
                    logger.debug("Find saved scene configuration for device with dSID {} and sceneID {}", dsid, i);
                    String[] sceneParm = sceneSave.replace(" ", "").split(",");
                    JSONDeviceSceneSpecImpl sceneSpecNew = null;
                    int sceneValue = -1;
                    int sceneAngle = -1;
                    for (int j = 0; j < sceneParm.length; j++) {
                        String[] sceneParmSplit = sceneParm[j].split(":");
                        switch (sceneParmSplit[0]) {
                            case "Scene":
                                sceneSpecNew = new JSONDeviceSceneSpecImpl(sceneParmSplit[1]);
                                break;
                            case "dontcare":
                                sceneSpecNew.setDontcare(Boolean.parseBoolean(sceneParmSplit[1]));
                                break;
                            case "localPrio":
                                sceneSpecNew.setLocalPrio(Boolean.parseBoolean(sceneParmSplit[1]));
                                break;
                            case "specialMode":
                                sceneSpecNew.setSpecialMode(Boolean.parseBoolean(sceneParmSplit[1]));
                                break;
                            case "sceneValue":
                                sceneValue = Integer.parseInt(sceneParmSplit[1]);
                                break;
                            case "sceneAngle":
                                sceneAngle = Integer.parseInt(sceneParmSplit[1]);
                                break;
                        }
                    }
                    if (sceneValue > -1) {
                        logger.debug("Saved sceneValue {}, sceneAngle {} for scene id {} into device with dsid {}",
                                sceneValue, sceneAngle, i, getDSID().getValue());
                        synchronized (sceneOutputMap) {
                            sceneOutputMap.put(i, new Integer[] { sceneValue, sceneAngle });
                        }
                    }
                    if (sceneSpecNew != null) {
                        logger.debug("Saved sceneConfig: [{}] for scene id {} into device with dsid {}",
                                sceneSpecNew.toString(), i, getDSID().getValue());
                        synchronized (sceneConfigMap) {
                            sceneConfigMap.put(i, sceneSpecNew);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setConfig(Config config) {
        this.config = config;
    }
}