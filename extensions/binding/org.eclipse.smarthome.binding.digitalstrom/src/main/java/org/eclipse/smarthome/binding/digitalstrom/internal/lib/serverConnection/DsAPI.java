/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection;

import java.util.List;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.Apartment;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.CachedMeteringValue;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DSID;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceConfig;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceParameterClassEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceSceneSpec;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.MeteringTypeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.MeteringUnitsEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.SensorEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.SensorIndexEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.constants.Scene;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.constants.SceneEnum;

/**
 * digitalSTROM-API based on dSS-Version higher then 1.14.5
 *
 * @author Alexander Betker
 * @see http://developer.digitalstrom.org/download/dss/dss-1.14.5-doc/dss-1.14.5-json_api.html
 *
 * @author Michael Ochel - add missing java-doc, update digitalSTROM-JSON-API as far as possible to the pfd version from
 *         June 19, 2014 and add checkConnection method
 * @author Matthias Siegele - add missing java-doc, update digitalSTROM-JSON-API as far as possible to the pfd version
 *         from
 *         June 19, 2014 and add checkConnection method
 * @see http://developer.digitalstrom.org/Architecture/v1.1/dss-json.pdf
 */
public interface DsAPI {

    /**
     * Calls the scene sceneNumber on all devices of the apartment. If groupID
     * or groupName are specified. Only devices contained in this group will be
     * addressed.
     *
     * @param groupID not required
     * @param groupName not required
     * @param sceneNumber required
     * @param force not required
     * @return true, if successful
     */
    public boolean callApartmentScene(String sessionToken, int groupID, String groupName, Scene sceneNumber,
            boolean force);

    /**
     * Returns all zones
     *
     * @return Apartment
     */
    public Apartment getApartmentStructure(String sessionToken);

    /**
     * Returns the list of devices in the apartment. If unassigned is true,
     * only devices that are not assigned to a zone will be returned.
     *
     * @param unassigned not required
     * @return List of devices
     */
    public List<Device> getApartmentDevices(String sessionToken, boolean unassigned);

    /**
     * Returns a list of dSID's of all meters(dSMs)
     *
     * @return String-List with dSID's
     */
    public List<String> getMeterList(String sessionToken);

    /**
     * Calls the sceneNumber on all devices in the given zone. If groupID or groupName
     * are specified only devices contained in this group will be addressed.
     *
     * @param zoneID needs either id or name
     * @param zoneName needs either id or name
     * @param groupID not required
     * @param groupName not required
     * @param sceneNumber required (only a zone/user scene is possible -> sceneNumber 0..63 )
     * @param force not required
     * @return true on success
     */
    public boolean callZoneScene(String sessionToken, int zoneID, String zoneName, int groupID, String groupName,
            SceneEnum sceneNumber, boolean force);

    /**
     * Turns the device on. This will call the scene "max" on the device.
     *
     * @param dSIDneeds either dSIDid or name
     * @param deviceName needs either dSIDid or name
     * @return true, if successful
     */
    public boolean turnDeviceOn(String sessionToken, DSID dSID, String deviceName);

    /**
     * Turns the device off. This will call the scene "min" on the device.
     *
     * @param dSID needs either dSID or name
     * @param deviceName needs either dSID or name
     * @return true, if successful
     */
    public boolean turnDeviceOff(String sessionToken, DSID dSID, String deviceName);

    /**
     * Sets the output value of device.
     *
     * @param dSID needs either dSID or name
     * @param deviceName needs either dSID or name
     * @param value required (0 - 255)
     * @return true, if successful
     */
    public boolean setDeviceValue(String sessionToken, DSID dSID, String deviceName, int value);

    /**
     * Gets the value of configuration class at offset index.
     *
     * @param dSID needs either dSID or name
     * @param deviceName needs either dSID or name
     * @param clazz required
     * @param index required
     * @return config with values
     */
    public DeviceConfig getDeviceConfig(String sessionToken, DSID dSID, String deviceName,
            DeviceParameterClassEnum clazz, int index);

    /**
     * Gets the device output value from parameter at the given offset.
     * The available parameters and offsets depend on the features of the
     * hardware components.
     *
     * @param dSID needs either dSID or name
     * @param deviceName needs either dSID or name
     * @param offset required (known offset f.e. 0)
     * @return
     */
    public int getDeviceOutputValue(String sessionToken, DSID dSID, String deviceName, int offset);

    /**
     * Sets the device output value at the given offset. The available
     * parameters and offsets depend on the features of the hardware components.
     *
     * @param dSIDneeds either dSID or name
     * @param deviceName needs either dSID or name
     * @param offset required
     * @param value required (0 - 65535)
     * @return true, if successful
     */
    public boolean setDeviceOutputValue(String sessionToken, DSID dSID, String deviceName, int offset, int value);

    /**
     * Gets the device configuration for a specific scene command.
     *
     * @param dSID needs either dSID or name
     * @param deviceName needs either dSID or name
     * @param sceneID required (0 .. 255)
     * @return scene configuration
     */
    public DeviceSceneSpec getDeviceSceneMode(String sessionTokens, DSID dSID, String deviceName, short sceneID);

    /**
     * Requests the sensor value for a given index.
     *
     * @param dSID needs either dSID or name
     * @param deviceName needs either dSID or name
     * @param sensorIndex required
     * @return sensor value
     */
    public short getDeviceSensorValue(String sessionToken, DSID dSID, String deviceName, SensorIndexEnum sensorIndex);

    /**
     * Calls scene sceneNumber on the device.
     *
     * @param dSID needs either dSID or name
     * @param deviceName needs either dSID or name
     * @param sceneNumber required
     * @param force not required
     * @return true, if successful
     */
    public boolean callDeviceScene(String sessionToken, DSID dSID, String deviceName, Scene sceneNumber, boolean force);

    /**
     * Subscribes to an event given by the name. The subscriptionID is a unique id
     * that is defined by the subscriber. It is possible to subscribe to several events,
     * using the same subscription id, this allows to retrieve a grouped output of the
     * events (i.e. get output of all subscribed by the given id).
     *
     * @param eventName required
     * @param subscriptionID required
     * @return true on success
     */
    public boolean subscribeEvent(String sessionToken, String eventName, int subscriptionID, int connectionTimeout,
            int readTimeout);

    /**
     * Unsubscribes from an event given by the name. The subscriptionID is a unique
     * id that was used in the subscribe call.
     *
     * @param eventName required
     * @param subscriptionID required
     * @return true on success
     */
    public boolean unsubscribeEvent(String sessionToken, String eventName, int subscriptionID, int connectionTimeout,
            int readTimeout);

    /**
     * Gets event information and output. The subscriptionID is a unique id
     * that was used in the subscribe call. All events, subscribed with the
     * given id will be handled by this call. A timeout, in case no events
     * are taken place, can be specified (in ms). By default the timeout
     * is disabled: 0 (zero), if no events occur the call will block.
     *
     * @param subscriptionID required
     * @param timeout optional
     * @return Event-String
     */
    public String getEvent(String sessionToken, int subscriptionID, int timeout);

    /**
     * Returns the dSS time in UTC seconds since epoch.
     *
     * @return time
     */
    public int getTime(String sessionToken);

    /**
     * Creates a new session using the registered application token
     *
     * @param applicationToken required
     * @return sessionToken
     */
    public String loginApplication(String applicationToken);

    /**
     * Creates a new session
     *
     * @param user required
     * @param password required
     */
    public String login(String user, String password);

    /**
     * Destroys the session and signs out the user
     */
    public boolean logout();

    /**
     * Returns the dSID of the digitalSTROM Server.
     *
     * @param sessionToken required
     * @return dsID
     */
    public String getDSID(String sessionToken);

    /**
     * Returns a token for passwordless login. The token will need to be approved
     * by a user first, the caller must not be logged in.
     *
     * @param applicationName required
     * @return applicationToken
     */
    public String requestAppplicationToken(String applicationName);

    /**
     * Revokes an application token, caller must be logged in.
     *
     * @param applicationToken
     */
    public boolean revokeToken(String applicationToken, String sessionToken);

    /**
     * Enables an application token, caller must be logged in.
     *
     * @param applicationToken required
     */
    public boolean enableApplicationToken(String applicationToken, String sessionToken);

    /**
     * Returns all resolutions stored on this dSS
     *
     * @return List of resolutions
     */
    public List<Integer> getResolutions(String sessionToken);

    /**
     * Returns cached energy meter value or cached power consumption
     * value in watt (W). The type parameter defines what should
     * be returned, valid types, 'energyDelta' are 'energy' and
     * 'consumption' you can also see at {@link MeteringTypeEnum}. 'energy' and 'energyDelta' are available in two
     * units: 'Wh' (default) and 'Ws' you can also see at {@link MeteringUnitsEnum}. The meterDSIDs parameter follows
     * the
     * set-syntax, currently it supports: .meters(dsid1,dsid2,...) and .meters(all)
     *
     * @param type required
     * @param meterDSIDs required
     * @param unit optional
     * @return cached metering values
     */
    public List<CachedMeteringValue> getLatest(String sessionToken, MeteringTypeEnum type, String meterDSIDs,
            MeteringUnitsEnum unit);

    /**
     * Returns cached energy meter value or cached power consumption
     * value in watt (W). The type parameter defines what should
     * be returned, valid types, 'energyDelta' are 'energy' and
     * 'consumption' you can also see at {@link MeteringTypeEnum}. 'energy' and 'energyDelta' are available in two
     * units: 'Wh' (default) and 'Ws' you can also see at {@link MeteringUnitsEnum}. <br>
     * The meterDSIDs parameter you can directly pass a {@link List} of the digitalSTROM-Meter dSID's as {@link String}.
     *
     * @param type required
     * @param meterDSIDs required
     * @param unit optional
     * @return cached metering values
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    public List<CachedMeteringValue> getLatest(String sessionToken, MeteringTypeEnum type, List<String> meterDSIDs,
            MeteringUnitsEnum unit);

    /**
     * Checks the connection and returns the HTTP-Status-Code.
     *
     * @param sessionToken required
     * @return HTTP-Status-Code
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    public int checkConnection(String sessionToken);

    /**
     * Returns the configured scene output value for the given sceneId of the digitalSTROM-Device with the given dSID.
     * <br>
     * At array position 0 is the output value and at position 1 the angle value, if the device is a blind.
     *
     * @param sessionToken required
     * @param dSID required
     * @param sceneId required
     * @return scene value at array position 0 and angle at position 1
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    public int[] getSceneValue(String sessionToken, DSID dSID, short sceneId);

    /**
     * Calls the INC scene on the digitalSTROM-Device with the given dSID and returns true if the request was success.
     *
     * @param sessionToken required
     * @param dSID required
     * @return success true otherwise false
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    public boolean increaseValue(String sessionToken, DSID dSID);

    /**
     * Calls the DEC scene on the digitalSTROM-Device with the given dSID and returns true if the request was
     * successful.
     *
     * @param sessionToken required
     * @param dSID required
     * @return success true otherwise false
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    public boolean decreaseValue(String sessionToken, DSID dSID);

    /**
     * Undos the given sceneNumer of the digitalSTROM-Device with the given dSID and returns true if the request was
     * successful.
     *
     * @param sessionToken required
     * @param dSID required
     * @param sceneNumber required
     * @return success true otherwise false
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    boolean undoDeviceScene(String sessionToken, DSID dsid, Scene sceneNumber);

    /**
     * Undo the given sceneNumer on the digitalSTROM apartment-group with the given groupID or groupName and returns
     * true
     * if the request was successful.
     *
     * @param sessionToken required
     * @param groupID needs either groupID or groupName
     * @param groupName needs either groupID or groupName
     * @param sceneNumber required
     * @return success true otherwise false
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    boolean undoApartmentScene(String sessionToken, int groupID, String groupName, Scene sceneNumber);

    /**
     * Undo the given sceneNumer on the digitalSTROM zone-group with the given zoneID or zoneName and groupID or
     * groupName and returns true if the request was successful.
     *
     * @param sessionToken
     * @param zoneID needs either zoneID or zoneName
     * @param zoneName needs either zoneID or zoneName
     * @param groupID needs either groupID or groupName
     * @param groupName needs either groupID or groupName
     * @param sceneNumber required
     * @return success true otherwise false
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    boolean undoZoneScene(String sessionToken, int zoneID, String zoneName, int groupID, String groupName,
            SceneEnum sceneNumber);

    /**
     * Returns the digitalSTROM-device sensor value for the digitalSTROM-device with the given dSID or deviceName and
     * the given sensorType. If the sensorType is supports from the device and the request was successful it returns
     * the sensor value otherwise -1.
     *
     * @param sessionToken required
     * @param dSID needs either dSID or deviceName
     * @param name
     * @param sensortype required
     * @return success sensor value otherwise -1
     *
     * @author Michael Ochel
     * @author Matthias Siegele
     */
    public short getDeviceSensorValue(String sessionToken, DSID dSID, String deviceName, SensorEnum sensortype);

    /**
     * Returns user defined name of the digitalSTROM installation.
     *
     * @param sessionToken required
     * @return name of the digitalSTROM installation
     */
    public String getInstallationName(String sessionToken);

    /**
     * Returns user defined name of the zone from the given zone id.
     *
     * @param sessionToken required
     * @param zoneID required
     * @return name of the given zone id
     */
    public String getZoneName(String sessionToken, int zoneID);

    /**
     * Returns user defined name of the device from the given dSID
     *
     * @param sessionToken required
     * @param dSID required
     * @return name of the given device dSID
     */
    public String getDeviceName(String sessionToken, DSID dSID);

    /**
     * Returns user defined name of the circuit from the given dSID.
     *
     * @param sessionToken required
     * @param dSID required
     * @return name of the given circuit dSID
     */
    public String getCircuitName(String sessionToken, DSID dSID);

    /**
     * Returns user defined name of the scene from the given zoneID, groupID and sceneID.
     *
     * @param sessionToken required
     * @param zoneID (0 is broadcast)
     * @param groupID (0 is broadcast)
     * @param sceneID (between 0 and 127)
     * @return name of the scene otherwise null
     */
    public String getSceneName(String sessionToken, int zoneID, int groupID, short sceneID);
}
