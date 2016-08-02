/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.StructureManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.impl.JSONResponseHandler;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DSID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link StructureManagerImpl} is the implementation of the {@link StructureManager}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public class StructureManagerImpl implements StructureManager {

    private Map<Integer, HashMap<Short, List<Device>>> zoneGroupDeviceMap;
    private Map<DSID, Device> deviceMap;
    private Map<String, DSID> dSUIDToDSIDMap;

    private Map<Integer, Object[]> zoneGroupIdNameMap = null;
    private Map<String, Object[]> zoneGroupNameIdMap = null;

    /**
     * Creates a new {@link StructureManagerImpl} with the {@link Device}s of the given referenceDeviceList.
     *
     * @param referenceDeviceList
     */
    public StructureManagerImpl(List<Device> referenceDeviceList) {
        handleStructure(referenceDeviceList);
    }

    /**
     * Creates a new {@link StructureManagerImpl} without {@link Device}s.
     */
    public StructureManagerImpl() {

    }

    private final String ZONE_GROUP_NAMES = "/json/property/query?query=/apartment/zones/*(ZoneID,name)/groups/*(group,name)";

    @Override
    public boolean generateZoneGroupNames(ConnectionManager connectionManager) {
        if (connectionManager.checkConnection()) {
            String response = connectionManager.getHttpTransport()
                    .execute(this.ZONE_GROUP_NAMES + "&token=" + connectionManager.getSessionToken());
            if (response == null) {
                return false;
            } else {
                JsonObject responsJsonObj = JSONResponseHandler.toJsonObject(response);
                if (JSONResponseHandler.checkResponse(responsJsonObj)) {
                    JsonObject resultJsonObj = JSONResponseHandler.getResultJsonObject(responsJsonObj);
                    if (resultJsonObj.get("zones") instanceof JsonArray) {
                        JsonArray zones = (JsonArray) resultJsonObj.get("zones");
                        if (this.zoneGroupIdNameMap == null) {
                            this.zoneGroupIdNameMap = new HashMap<Integer, Object[]>(zones.size());
                            this.zoneGroupNameIdMap = new HashMap<String, Object[]>(zones.size());
                        }
                        if (zones != null) {
                            for (int i = 0; i < zones.size(); i++) {
                                if (((JsonObject) zones.get(i)).get("groups") instanceof JsonArray) {
                                    JsonArray groups = (JsonArray) ((JsonObject) zones.get(i)).get("groups");
                                    if (groups.size() != 0) {
                                        Object[] zoneIdNameGroups = new Object[2];
                                        Object[] zoneNameIdGroups = new Object[2];
                                        int zoneID = ((JsonObject) zones.get(i)).get("ZoneID").getAsInt();
                                        String zoneName = ((JsonObject) zones.get(i)).get("name").getAsString();
                                        zoneIdNameGroups[0] = zoneName;
                                        zoneNameIdGroups[0] = zoneID;
                                        HashMap<Short, String> groupIdNames = new HashMap<Short, String>();
                                        HashMap<String, Short> groupNameIds = new HashMap<String, Short>();
                                        for (int k = 0; k < groups.size(); k++) {
                                            short groupID = ((JsonObject) groups.get(k)).get("group").getAsShort();
                                            String groupName = ((JsonObject) groups.get(k)).get("name").getAsString();
                                            groupIdNames.put(groupID, groupName);
                                            groupNameIds.put(groupName, groupID);
                                        }
                                        zoneIdNameGroups[1] = groupIdNames;
                                        zoneNameIdGroups[1] = groupNameIds;
                                        this.zoneGroupIdNameMap.put(zoneID, zoneIdNameGroups);
                                        this.zoneGroupNameIdMap.put(zoneName, zoneNameIdGroups);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public String getZoneName(int zoneID) {
        if (this.zoneGroupIdNameMap == null) {
            return null;
        }
        return this.zoneGroupIdNameMap.get(zoneID) != null ? (String) this.zoneGroupIdNameMap.get(zoneID)[0] : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getZoneGroupName(int zoneID, short groupID) {
        if (this.zoneGroupIdNameMap == null) {
            return null;
        }
        if (this.zoneGroupIdNameMap.get(zoneID) != null) {
            if (this.zoneGroupIdNameMap.get(zoneID)[1] != null) {
                if (((HashMap<Short, String>) this.zoneGroupIdNameMap.get(zoneID)[1]).get(groupID) != null) {
                    return ((HashMap<Short, String>) this.zoneGroupIdNameMap.get(zoneID)[1]).get(groupID);
                }
            }
        }
        return null;
    }

    @Override
    public int getZoneId(String zoneName) {
        if (this.zoneGroupNameIdMap == null) {
            return -1;
        }
        return this.zoneGroupNameIdMap.get(zoneName) != null ? (int) this.zoneGroupNameIdMap.get(zoneName)[0] : -1;
    }

    @Override
    public boolean checkZoneID(int zoneID) {
        return this.getGroupsFromZoneX(zoneID) != null;
    }

    @Override
    public boolean checkZoneGroupID(int zoneID, short groupID) {
        return this.getGroupsFromZoneX(zoneID) != null ? (this.getGroupsFromZoneX(zoneID).get(groupID) != null) : false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public short getZoneGroupId(String zoneName, String groupName) {
        if (this.zoneGroupNameIdMap == null) {
            return -1;
        }
        if (this.zoneGroupNameIdMap.get(zoneName) != null) {
            if (this.zoneGroupNameIdMap.get(zoneName)[1] != null) {
                if (((HashMap<Short, String>) this.zoneGroupNameIdMap.get(zoneName)[1]).get(groupName) != null) {
                    return ((HashMap<String, Short>) this.zoneGroupNameIdMap.get(zoneName)[1]).get(groupName);
                }
            }
        }
        return -1;
    }

    @Override
    public Map<DSID, Device> getDeviceMap() {
        return this.deviceMap != null ? new HashMap<DSID, Device>(this.deviceMap) : new HashMap<DSID, Device>();
    }

    private void putDeviceToHashMap(Device device) {
        if (deviceMap == null) {
            deviceMap = Collections.synchronizedMap(new HashMap<DSID, Device>()); // deviceList.size()
        }
        if (dSUIDToDSIDMap == null) {
            dSUIDToDSIDMap = Collections.synchronizedMap(new HashMap<String, DSID>()); // deviceList.size()
        }
        if (device.getDSID() != null) {
            deviceMap.put(device.getDSID(), device);
            dSUIDToDSIDMap.put(device.getDSUID(), device.getDSID());
        }
    }

    /**
     * This method build the digitalSTROM structure as an {@link HashMap} with the zone id as key
     * and an {@link HashMap} as value. This {@link HashMap} has the group id as key and a {@link List}
     * with all digitalSTROM {@link Device}s.<br>
     * <br>
     * <b>Note:</b> the zone id 0 is the broadcast address and the group id 0 too.
     */
    private void handleStructure(List<Device> deviceList) {
        HashMap<Short, List<Device>> groupXHashMap = new HashMap<Short, List<Device>>();
        groupXHashMap = new HashMap<Short, List<Device>>();
        groupXHashMap.put((short) 0, deviceList);

        if (this.zoneGroupDeviceMap == null) {
            zoneGroupDeviceMap = Collections.synchronizedMap(new HashMap<Integer, HashMap<Short, List<Device>>>());
        }

        this.zoneGroupDeviceMap.put(0, groupXHashMap);

        for (Device device : deviceList) {
            addDeviceToStructure(device);
        }
    }

    @Override
    public Map<DSID, Device> getDeviceHashMapReference() {
        return this.deviceMap;
    }

    @Override
    public Map<Integer, HashMap<Short, List<Device>>> getStructureReference() {
        return this.zoneGroupDeviceMap;
    }

    @Override
    public HashMap<Short, List<Device>> getGroupsFromZoneX(int zoneID) {
        return this.zoneGroupDeviceMap == null ? null : this.zoneGroupDeviceMap.get(zoneID);
    }

    @Override
    public List<Device> getReferenceDeviceListFromZoneXGroupX(int zoneID, short groupID) {
        return getGroupsFromZoneX(zoneID) == null ? null : this.zoneGroupDeviceMap.get(zoneID).get(groupID);
    }

    @Override
    public Device getDeviceByDSID(String dSID) {
        return getDeviceByDSID(new DSID(dSID));
    }

    @Override
    public Device getDeviceByDSID(DSID dSID) {
        return this.zoneGroupDeviceMap == null ? null : this.deviceMap.get(dSID);
    }

    @Override
    public Device getDeviceByDSUID(String dSUID) {
        if (this.deviceMap != null && this.dSUIDToDSIDMap != null) {
            return this.dSUIDToDSIDMap.get(dSUID) == null ? null : this.getDeviceByDSID(this.dSUIDToDSIDMap.get(dSUID));
        }
        return null;
    }

    @Override
    public void updateDevice(int oldZone, List<Short> oldGroups, Device device) {
        if (oldZone == -1) {
            oldZone = device.getZoneId();
        }
        deleteDevice(oldZone, oldGroups, device);
        addDeviceToStructure(device);
    }

    @Override
    public void updateDevice(Device device) {
        if (device != null) {
            int oldZoneID = -1;
            List<Short> oldGroups = null;
            Device internalDevice = this.getDeviceByDSID(device.getDSID());
            if (internalDevice != null) {
                if (device.getZoneId() != internalDevice.getZoneId()) {
                    oldZoneID = internalDevice.getZoneId();
                    internalDevice.setZoneId(device.getZoneId());
                }

                if (!internalDevice.getGroups().equals(device.getGroups())) {
                    oldGroups = internalDevice.getGroups();
                    internalDevice.setGroups(device.getGroups());
                }

                if (deleteDevice(oldZoneID, oldGroups, internalDevice)) {
                    addDeviceToStructure(internalDevice);
                }
            }
        }
    }

    @Override
    public void deleteDevice(Device device) {
        dSUIDToDSIDMap.remove(device.getDSUID());
        deviceMap.remove(device.getDSID());
        deleteDevice(device.getZoneId(), device.getGroups(), device);
    }

    private boolean deleteDevice(int zoneID, List<Short> groups, Device device) {
        if (groups != null || zoneID >= 0) {
            if (groups == null) {
                groups = device.getGroups();
            }
            if (zoneID == -1) {
                zoneID = device.getZoneId();
            }
            for (Short groupID : groups) {
                List<Device> deviceList = getReferenceDeviceListFromZoneXGroupX(zoneID, groupID);
                if (deviceList != null) {
                    deviceList.remove(device);
                }
                deviceList = getReferenceDeviceListFromZoneXGroupX(0, groupID);
                if (deviceList != null) {
                    deviceList.remove(device);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void addDeviceToStructure(Device device) {
        putDeviceToHashMap(device);

        addDevicetoZoneXGroupX(0, (short) 0, device);
        int zoneID = device.getZoneId();
        addDevicetoZoneXGroupX(zoneID, (short) 0, device);

        for (Short groupID : device.getGroups()) {
            addDevicetoZoneXGroupX(zoneID, groupID, device);

            if (groupID <= 16) {
                addDevicetoZoneXGroupX(0, groupID, device);
            }
        }
    }

    private void addDevicetoZoneXGroupX(int zoneID, short groupID, Device device) {
        if (zoneGroupDeviceMap == null) {
            zoneGroupDeviceMap = Collections.synchronizedMap(new HashMap<Integer, HashMap<Short, List<Device>>>());
        }
        HashMap<Short, List<Device>> groupXHashMap = this.zoneGroupDeviceMap.get(zoneID);
        if (groupXHashMap == null) {
            groupXHashMap = new HashMap<Short, List<Device>>();

            this.zoneGroupDeviceMap.put(zoneID, groupXHashMap);
        }
        List<Device> groupDeviceList = groupXHashMap.get(groupID);
        if (groupDeviceList == null) {
            groupDeviceList = new LinkedList<Device>();
            groupDeviceList.add(device);
            groupXHashMap.put(groupID, groupDeviceList);
        } else {
            if (!groupDeviceList.contains(device)) {
                groupDeviceList.add(device);
            }
        }
    }

    @Override
    public Set<Integer> getZoneIDs() {
        return this.zoneGroupDeviceMap != null ? this.zoneGroupDeviceMap.keySet() : null;
    }
}
