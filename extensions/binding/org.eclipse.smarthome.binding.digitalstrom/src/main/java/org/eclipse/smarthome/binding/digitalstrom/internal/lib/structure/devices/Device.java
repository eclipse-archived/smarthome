/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices;

import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.config.Config;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.DeviceStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DSID;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceSceneSpec;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceStateUpdate;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.FunctionalColorGroupEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.OutputModeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.InternalScene;

/**
 * The {@link Device} represents a digitalSTROM internal stored device.
 *
 * @author Alexander Betker - Initial contribution
 * @author Michael Ochel - add methods for ESH, new functionalities and JavaDoc
 * @author Mathias Siegele - add methods for ESH, new functionalities and JavaDoc
 */
public interface Device {

    /**
     * Returns the dSID of this device.
     *
     * @return {@link DSID} dSID
     */
    public DSID getDSID();

    /**
     * Returns the dSUID of this device.
     *
     * @return dSID
     */
    public String getDSUID();

    /**
     * Returns the id of the DS-Meter in which the device is registered.
     *
     * @return meterDSID
     */
    public DSID getMeterDSID();

    /**
     * Sets the id of the DS-Meter in which the device is registered.
     *
     * @param meterDSID
     */

    public void setMeterDSID(String meterDSID);

    /**
     * Returns the hardware info of this device.
     * You can see all available hardware info here
     * {@link http://www.digitalstrom.com/Partner/Support/Techn-Dokumentation/}
     *
     * @return hardware info
     */
    public String getHWinfo();

    /**
     * Returns the user defined name of this device.
     *
     * @return name of this device
     */
    public String getName();

    /**
     * Sets the name of this device;
     *
     * @param name
     */
    public void setName(String name);

    /**
     * Returns the zone id in which this device is in.
     *
     * @return zoneID
     */
    public int getZoneId();

    /**
     * Sets the zoneID of this device.
     *
     * @parm zoneID
     */
    public void setZoneId(int zoneID);

    /**
     * This device is available in his zone or not.
     * Every 24h the dSM (meter) checks, if the devices are
     * plugged in
     *
     * @return true, if device is available otherwise false
     */
    public boolean isPresent();

    /**
     * Sets this device is available in his zone or not.
     *
     * @param isPresent (true = available | false = not available)
     */
    public void setIsPresent(boolean isPresent);

    /**
     * Returns true, if this device is on, otherwise false.
     *
     * @return is on (true = on | false = off)
     */
    public boolean isOn();

    /**
     * Adds an on command as {@link DeviceStateUpdate}, if the flag is true or off command, if it is false to the list
     * of
     * outstanding commands.
     *
     * @param flag (true = on | false = off)
     */
    public void setIsOn(boolean flag);

    /**
     * Returns true, if this shade device is open, otherwise false.
     *
     * @return is on (true = open | false = closed)
     */
    public boolean isOpen();

    /**
     * Adds an open command as {@link DeviceStateUpdate}, if the flag is true or closed command, if it is false to the
     * list of outstanding commands.
     *
     * @param flag (true = open | false = closed)
     */
    public void setIsOpen(boolean flag);

    /**
     * Returns true, if this device is dimmable, otherwise false.
     *
     * @return is dimmable (true = yes | false = no)
     */
    public boolean isDimmable();

    /**
     * Returns true, if this device is a shade device (grey), otherwise false.
     *
     * @return is shade (true = yes | false = no)
     */
    public boolean isShade();

    /**
     * Returns true, if the device output mode isn't disabled.
     *
     * @return have output mode (true = yes | false = no)
     */
    public boolean isDeviceWithOutput();

    /**
     * Returns the current functional color group of this device.
     * For more informations please have a look at {@link FunctionalColorGroup}.
     *
     * @return current functional color group
     */
    public FunctionalColorGroupEnum getFunctionalColorGroup();

    /**
     * Sets the functional color group of this device.
     *
     * @param fuctionalColorGroup
     */
    public void setFunctionalColorGroup(FunctionalColorGroupEnum fuctionalColorGroup);

    /**
     * Returns the current output mode of this device.
     * Some devices are able to have different output modes e.g. the device GE-KM200 is able to
     * be in dimm mode, switch mode or disabled.
     * For more informations please have a look at {@link OutputModeEnum}.
     *
     * @return the current output mode of this device
     */
    public OutputModeEnum getOutputMode();

    /**
     * Adds an increase command as {@link DeviceStateUpdate} to the list of outstanding commands.
     */
    public void increase();

    /**
     * Adds an decrease command as {@link DeviceStateUpdate} to the list of outstanding commands.
     */
    public void decrease();

    /**
     * Returns the current slat position of this device.
     *
     * @return current slat position
     */
    public int getSlatPosition();

    /**
     * Adds an set slat position command as {@link DeviceStateUpdate} with the given slat position to the list of
     * outstanding commands.
     *
     * @return slat position
     */
    public void setSlatPosition(int slatPosition);

    /**
     * Returns the maximal slat position value of this device.
     *
     * @return maximal slat position value
     */
    public int getMaxSlatPosition();

    /**
     * Returns the minimal slat position value of this device.
     *
     * @return minimal slat position value
     */
    public int getMinSlatPosition();

    /**
     * Returns the current output value of this device.
     * This can be the slat position or the brightness of this device.
     *
     * @return current output value
     */
    public short getOutputValue();

    /**
     * Adds an set output value command as {@link DeviceStateUpdate} with the given output value to the list of
     * outstanding commands.
     *
     * @param outputValue
     */
    public void setOutputValue(short outputValue);

    /**
     * Returns the maximal output value of this device.
     *
     * @return maximal output value
     */
    public short getMaxOutputValue();

    /**
     * Returns the last recorded power consumption in watt of this device.
     *
     * @return current power consumption in watt
     */
    public int getActivePower();

    /**
     * Sets the current power consumption in watt to the given power consumption.
     *
     * @param powerConsumption in watt
     */
    public void setActivePower(int powerConsumption);

    /**
     * Returns the energy meter value in watt per hour of this device.
     *
     * @return energy meter value in watt per hour
     */
    public int getOutputCurrent();

    /**
     * Sets the last recorded energy meter value in watt per hour of this device.
     *
     * @param energy meter value in watt per hour
     */
    public void setOutputCurrent(int value);

    /**
     * Returns the last recorded electric meter value in ampere of this device.
     *
     * @return electric meter value in ampere
     */
    public int getElectricMeter();

    /**
     * Sets the last recorded electric meter value in ampere of this device.
     *
     * @param electric meter value in mA
     */
    public void setElectricMeter(int electricMeterValue);

    /**
     * Returns a list with group id's in which the device is part of.
     *
     * @return List of group id's
     */
    public List<Short> getGroups();

    /**
     * Adds the given groupID to the group list.
     *
     * @param groupID
     */
    public void addGroup(Short groupID);

    /**
     * Overrides the existing group list with the given new.
     *
     * @param newGroupList
     */
    public void setGroups(List<Short> newGroupList);

    /**
     * Returns the scene output value of this device of the given scene id as {@link Integer} array. The first field is
     * the output value and the second is the angle value or -1 if no angle value exists.
     * If the method returns null, this scene id isn't read yet.
     *
     * @return scene output value and scene angle value or null, if it isn't read out yet
     */
    public Integer[] getSceneOutputValue(short sceneID);

    /**
     * Sets the scene output value of this device for the given scene id and scene output value.
     *
     * @param sceneId
     * @param sceneOutputValue
     */
    public void setSceneOutputValue(short sceneId, int sceneOutputValue);

    /**
     * This configuration is very important. The devices can
     * be configured to not react to some commands (scene calls).
     * So you can't imply that a device automatically turns on (by default yes,
     * but if someone configured his own scenes, then maybe not) after a
     * scene call. This method returns true or false, if the configuration
     * for this sceneID already has been read
     *
     * @param sceneId the sceneID
     * @return true if this device has the configuration for this specific scene
     */
    public boolean containsSceneConfig(short sceneId);

    /**
     * Add the config for this scene. The config has the configuration
     * for the specific sceneID.
     *
     * @param sceneId scene call id
     * @param sceneSpec config for this sceneID
     */
    public void addSceneConfig(short sceneId, DeviceSceneSpec sceneSpec);

    /**
     * Get the config for this scene. The config has the configuration
     * for the specific sceneID.
     *
     * @param sceneId scene call id
     * @return sceneSpec config for this sceneID
     */
    public DeviceSceneSpec getSceneConfig(short sceneId);

    /**
     * Should the device react on this scene call or not .
     *
     * @param sceneId scene call id
     * @return true, if this device should react on this sceneID
     */
    public boolean doIgnoreScene(short sceneId);

    // follow methods added by Michael Ochel and Matthias Siegele

    /**
     * Returns true, if the power consumption is up to date or false if it has to be updated.
     *
     * @return is up to date (true = yes | false = no)
     */
    public boolean isActivePowerUpToDate();

    /**
     * Returns true, if the electric meter is up to date or false if it has to be updated.
     *
     * @return is up to date (true = yes | false = no)
     */
    public boolean isElectricMeterUpToDate();

    /**
     * Returns true, if the energy meter is up to date or false if it has to be updated.
     *
     * @return is up to date (true = yes | false = no)
     */
    public boolean isOutputCurrentUpToDate();

    /**
     * Returns true, if all sensor data are up to date or false if some have to be updated.
     *
     * @return is up to date (true = yes | false = no)
     */
    public boolean isSensorDataUpToDate();

    /**
     * Sets the priority to refresh the data of the sensors to the given priorities.
     * They can be never, low, medium or high.
     *
     * @param powerConsumptionRefreshPriority
     * @param electricMeterRefreshPriority
     * @param energyMeterRefreshPriority
     */
    public void setSensorDataRefreshPriority(String powerConsumptionRefreshPriority,
            String electricMeterRefreshPriority, String energyMeterRefreshPriority);

    /**
     * Returns the priority of the power consumption refresh.
     *
     * @return power consumption refresh priority
     */
    public String getActivePowerRefreshPriority();

    /**
     * Returns the priority of the electric meter refresh.
     *
     * @return electric meter refresh priority
     */
    public String getElectricMeterRefreshPriority();

    /**
     * Returns the priority of the energy meter refresh.
     *
     * @return energy meter refresh priority
     */
    public String getOutputCurrentRefreshPriority();

    /**
     * Returns true, if the device is up to date.
     *
     * @return digitalSTROM-Device is up to date (true = yes | false = no)
     */
    public boolean isDeviceUpToDate();

    /**
     * Returns the next {@linkDeviceStateUpdate} to update the digitalSTROM-Device on the digitalSTROM-Server.
     *
     * @return DeviceStateUpdate for digitalSTROM-Device
     */
    public DeviceStateUpdate getNextDeviceUpdateState();

    /**
     * Update the internal stored device object.
     *
     * @param deviceStateUpdate
     */
    public void updateInternalDeviceState(DeviceStateUpdate deviceStateUpdate);

    /**
     * Call the given {@link InternalScene} on this {@link Device} and updates it.
     *
     * @param scene
     */
    public void callInternalScene(InternalScene scene);

    /**
     * Undo the given {@link InternalScene} on this {@link Device} and updates it.
     *
     * @param scene to undo
     */
    public void undoInternalScene(InternalScene scene);

    /**
     * Initial a call scene for the given scene number.
     *
     * @param sceneNumber
     */
    public void callScene(Short sceneNumber);

    /**
     * Returns the current active {@link InternalScene}, otherwise null.
     *
     * @return active {@link InternalScene} or null
     */
    public InternalScene getAcitiveScene();

    /**
     * Undo the active scene if a scene is active.
     */
    public void undoScene();

    /**
     * Checks the scene configuration for the given scene number and initial a scene configuration reading with the
     * given priority if no scene configuration exists.
     *
     * @param sceneNumber
     * @param prio
     */
    public void checkSceneConfig(Short sceneNumber, int prio);

    /**
     * Register a {@link DeviceStatusListener} to this {@link Device}.
     *
     * @param deviceStatuslistener
     */
    public void registerDeviceStateListener(DeviceStatusListener deviceStatuslistener);

    /**
     * Unregister the {@link DeviceStatusListener} to this {@link Device} if it exists.
     *
     * @return the unregistered {@link DeviceStatusListener} or null if no one was registered
     */
    public DeviceStatusListener unregisterDeviceStateListener();

    /**
     * Returns true, if a {@link DeviceStatusListener} is registered to this {@link Device}, otherwise false.
     *
     * @return return true, if a lister is registered, otherwise false
     */
    public boolean isListenerRegisterd();

    /**
     * Sets the given output mode as new output mode of this {@link Device}.
     *
     * @param newOutputMode
     */
    public void setOutputMode(OutputModeEnum newOutputMode);

    /**
     * Returns a {@link List} of all saved scene configurations.
     *
     * @return
     */
    public List<Short> getSavedScenes();

    /**
     * Initializes a internal device update as call scene for the given scene number.
     *
     * @param sceneNumber
     */
    public void internalCallScene(Short sceneNumber);

    /**
     * Initializes a internal device update as undo scene.
     */
    public void internalUndoScene();

    /**
     * Returns true, if this {@link Device} is a device with a switch output mode.
     *
     * @return true, if it is a switch otherwise false
     */
    public boolean isSwitch();

    /**
     * Sets the given {@link config} as new {@link config}.
     *
     * @param config
     */
    public void setConfig(Config config);

    /**
     * Returns the current angle position of the {@link Device}.
     *
     * @return current angle position
     */
    public short getAnglePosition();

    /**
     * Adds an set angle value command as {@link DeviceStateUpdate} with the given angle value to the list of
     * outstanding commands.
     *
     * @param angle
     */
    public void setAnglePosition(int angle);

    /**
     * Sets the scene output value and scene output angle of this device for the given scene id, scene output value and
     * scene output angle.
     *
     * @param sceneId
     * @param value
     * @param angle
     */
    public void setSceneOutputValue(short sceneId, int value, int angle);

    /**
     * Returns the max angle value of the slat.
     *
     * @return max slat angle
     */
    public int getMaxSlatAngle();

    /**
     * Returns the min angle value of the slat.
     *
     * @return min slat angle
     */
    public int getMinSlatAngle();

    /**
     * Returns true, if it is a blind device.
     *
     * @return is blind (true = yes | false = no
     */
    public boolean isBlind();

    /**
     * Saves scene configurations from the given sceneProperties in the {@link Device]. <br>
     * The {@link Map} has to be like the following format:
     * <ul>
     * <li><b>Key:</b> scene[sceneID]</li>
     * <li><b>Value:</b> {Scene: [sceneID], dontcare: [don't care flag], localPrio: [local prio flag], specialMode:
     * [special mode flag]}(0..1),
     * {sceneValue: [sceneValue]{, sceneAngle: [scene angle]}(0..1)}(0..1)</li>
     * </ul>
     *
     * @param sceneProperties
     */
    public void saveConfigSceneSpecificationIntoDevice(Map<String, String> sceneProperties);

    /**
     * Returns the min output value.
     *
     * @return min output value
     */
    public short getMinOutputValue();

    /**
     * Adds a slat increase command as {@link DeviceStateUpdate} to the list of outstanding commands.
     */
    public void increaseSlatAngle();

    /**
     * Adds a slat decrease command as {@link DeviceStateUpdate} to the list of outstanding commands.
     */
    public void decreaseSlatAngle();
}
