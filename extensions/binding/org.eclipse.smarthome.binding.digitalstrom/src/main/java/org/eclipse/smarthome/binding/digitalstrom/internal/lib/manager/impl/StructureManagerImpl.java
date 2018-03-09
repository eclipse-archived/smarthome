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
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.StructureManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.AbstractGeneralDeviceInformations;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Circuit;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.CachedMeteringValue;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl.DSID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link StructureManagerImpl} is the implementation of the {@link StructureManager}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class StructureManagerImpl implements StructureManager {

    private class ZoneGroupsNameAndIDMap {
        public final String zoneName;
        public final int zoneID;

        private final Map<Short, String> groupIdNames;
        private final Map<String, Short> groupNameIds;

        public ZoneGroupsNameAndIDMap(final int zoneID, final String zoneName, JsonArray groups) {
            this.zoneID = zoneID;
            this.zoneName = zoneName;

            groupIdNames = new HashMap<>(groups.size());
            groupNameIds = new HashMap<>(groups.size());
            for (int k = 0; k < groups.size(); k++) {
                short groupID = ((JsonObject) groups.get(k)).get("group").getAsShort();
                String groupName = ((JsonObject) groups.get(k)).get("name").getAsString();
                groupIdNames.put(groupID, groupName);
                groupNameIds.put(groupName, groupID);
            }
        }

        public String getGroupName(Short groupID) {
            return groupIdNames.get(groupID);
        }

        public short getGroupID(String groupName) {
            final Short tmp = groupNameIds.get(groupName);
            return tmp != null ? tmp : -1;
        }
    }

    /**
     * Query to get all zone and group names. Can be executed with {@link DsAPI#query(String, String)} or
     * {@link DsAPI#query2(String, String)}.
     */
    public static final String ZONE_GROUP_NAMES = "/apartment/zones/*(ZoneID,name)/groups/*(group,name)";

    private final Map<Integer, HashMap<Short, List<Device>>> zoneGroupDeviceMap = Collections
            .synchronizedMap(new HashMap<Integer, HashMap<Short, List<Device>>>());
    private final Map<DSID, Device> deviceMap = Collections.synchronizedMap(new HashMap<DSID, Device>());
    private final Map<DSID, Circuit> circuitMap = Collections.synchronizedMap(new HashMap<DSID, Circuit>());
    private final Map<String, DSID> dSUIDToDSIDMap = Collections.synchronizedMap(new HashMap<String, DSID>());

    private Map<Integer, ZoneGroupsNameAndIDMap> zoneGroupIdNameMap;
    private Map<String, ZoneGroupsNameAndIDMap> zoneGroupNameIdMap;

    /**
     * Creates a new {@link StructureManagerImpl} with the {@link Device}s of the given referenceDeviceList.
     *
     * @param referenceDeviceList to add
     */
    public StructureManagerImpl(List<Device> referenceDeviceList) {
        handleStructure(referenceDeviceList);
    }

    /**
     * Creates a new {@link StructureManagerImpl} with the {@link Device}s of the given referenceDeviceList.
     *
     * @param referenceDeviceList to add
     * @param referenceCircuitList to add
     */
    public StructureManagerImpl(List<Device> referenceDeviceList, List<Circuit> referenceCircuitList) {
        handleStructure(referenceDeviceList);
        addCircuitList(referenceCircuitList);
    }

    /**
     * Creates a new {@link StructureManagerImpl} without {@link Device}s.
     */
    public StructureManagerImpl() {

    }

    @Override
    public boolean generateZoneGroupNames(ConnectionManager connectionManager) {
        JsonObject resultJsonObj = connectionManager.getDigitalSTROMAPI().query(connectionManager.getSessionToken(),
                ZONE_GROUP_NAMES);
        if (resultJsonObj != null && resultJsonObj.get("zones") instanceof JsonArray) {
            JsonArray zones = (JsonArray) resultJsonObj.get("zones");
            if (zoneGroupIdNameMap == null) {
                zoneGroupIdNameMap = new HashMap<Integer, ZoneGroupsNameAndIDMap>(zones.size());
                zoneGroupNameIdMap = new HashMap<String, ZoneGroupsNameAndIDMap>(zones.size());
            }
            if (zones != null) {
                for (int i = 0; i < zones.size(); i++) {
                    if (((JsonObject) zones.get(i)).get("groups") instanceof JsonArray) {
                        JsonArray groups = (JsonArray) ((JsonObject) zones.get(i)).get("groups");
                        ZoneGroupsNameAndIDMap zoneGoupIdNameMap = new ZoneGroupsNameAndIDMap(
                                ((JsonObject) zones.get(i)).get("ZoneID").getAsInt(),
                                ((JsonObject) zones.get(i)).get("name").getAsString(), groups);

                        zoneGroupIdNameMap.put(zoneGoupIdNameMap.zoneID, zoneGoupIdNameMap);
                        zoneGroupNameIdMap.put(zoneGoupIdNameMap.zoneName, zoneGoupIdNameMap);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public String getZoneName(int zoneID) {
        if (zoneGroupIdNameMap == null) {
            return null;
        }
        final ZoneGroupsNameAndIDMap tmp = zoneGroupIdNameMap.get(zoneID);
        return tmp != null ? tmp.zoneName : null;
    }

    @Override
    public String getZoneGroupName(int zoneID, short groupID) {
        if (zoneGroupIdNameMap == null) {
            return null;
        }
        final ZoneGroupsNameAndIDMap tmp = zoneGroupIdNameMap.get(zoneID);
        return tmp != null ? tmp.getGroupName(groupID) : null;
    }

    @Override
    public int getZoneId(String zoneName) {
        if (zoneGroupNameIdMap == null) {
            return -1;
        }
        final ZoneGroupsNameAndIDMap tmp = zoneGroupNameIdMap.get(zoneName);
        return tmp != null ? tmp.zoneID : -1;
    }

    @Override
    public boolean checkZoneID(int zoneID) {
        return getGroupsFromZoneX(zoneID) != null;
    }

    @Override
    public boolean checkZoneGroupID(int zoneID, short groupID) {
        final HashMap<Short, List<Device>> tmp = getGroupsFromZoneX(zoneID);
        return tmp != null ? tmp.get(groupID) != null : false;
    }

    @Override
    public short getZoneGroupId(String zoneName, String groupName) {
        if (zoneGroupNameIdMap == null) {
            return -1;
        }
        final ZoneGroupsNameAndIDMap tmp = zoneGroupNameIdMap.get(zoneName);
        return tmp != null ? tmp.getGroupID(groupName) : -1;
    }

    @Override
    public Map<DSID, Device> getDeviceMap() {
        return new HashMap<DSID, Device>(deviceMap);
    }

    private void putDeviceToHashMap(Device device) {
        if (device.getDSID() != null) {
            deviceMap.put(device.getDSID(), device);
            addDSIDtoDSUID((AbstractGeneralDeviceInformations) device);
        }
    }

    /**
     * This method build the digitalSTROM structure as an {@link HashMap} with the zone id as key
     * and an {@link HashMap} as value. This {@link HashMap} has the group id as key and a {@link List}
     * with all digitalSTROM {@link Device}s.<br>
     * <br>
     * <b>Note:</b> the zone id 0 is the broadcast address and the group id 0, too.
     */
    private void handleStructure(List<Device> deviceList) {
        HashMap<Short, List<Device>> groupXHashMap = new HashMap<Short, List<Device>>();
        groupXHashMap.put((short) 0, deviceList);

        zoneGroupDeviceMap.put(0, groupXHashMap);

        for (Device device : deviceList) {
            addDeviceToStructure(device);
        }
    }

    @Override
    public Map<DSID, Device> getDeviceHashMapReference() {
        return deviceMap;
    }

    @Override
    public Map<Integer, HashMap<Short, List<Device>>> getStructureReference() {
        return zoneGroupDeviceMap;
    }

    @Override
    public HashMap<Short, List<Device>> getGroupsFromZoneX(int zoneID) {
        return zoneGroupDeviceMap.get(zoneID);
    }

    @Override
    public List<Device> getReferenceDeviceListFromZoneXGroupX(int zoneID, short groupID) {
        final HashMap<Short, List<Device>> tmp = getGroupsFromZoneX(zoneID);
        return tmp != null ? tmp.get(groupID) : null;
    }

    @Override
    public Device getDeviceByDSID(String dSID) {
        return getDeviceByDSID(new DSID(dSID));
    }

    @Override
    public Device getDeviceByDSID(DSID dSID) {
        return deviceMap.get(dSID);
    }

    @Override
    public Device getDeviceByDSUID(String dSUID) {
        final DSID tmp = dSUIDToDSIDMap.get(dSUID);
        return tmp != null ? getDeviceByDSID(tmp) : null;
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
        HashMap<Short, List<Device>> groupXHashMap = zoneGroupDeviceMap.get(zoneID);
        if (groupXHashMap == null) {
            groupXHashMap = new HashMap<Short, List<Device>>();
            zoneGroupDeviceMap.put(zoneID, groupXHashMap);
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
        return zoneGroupDeviceMap.keySet();
    }

    @Override
    public void addCircuitList(List<Circuit> referenceCircuitList) {
        for (Circuit circuit : referenceCircuitList) {
            addCircuit(circuit);
        }
    }

    @Override
    public Circuit addCircuit(Circuit circuit) {
        addDSIDtoDSUID((AbstractGeneralDeviceInformations) circuit);
        return circuitMap.put(circuit.getDSID(), circuit);
    }

    private void addDSIDtoDSUID(AbstractGeneralDeviceInformations deviceInfo) {
        if (deviceInfo.getDSID() != null) {
            dSUIDToDSIDMap.put(deviceInfo.getDSUID(), deviceInfo.getDSID());
        }
    }

    @Override
    public Circuit getCircuitByDSID(DSID dSID) {
        return circuitMap.get(dSID);
    }

    @Override
    public Circuit getCircuitByDSUID(String dSUID) {
        final DSID tmp = dSUIDToDSIDMap.get(dSUID);
        return tmp != null ? getCircuitByDSID(tmp) : null;
    }

    @Override
    public Circuit getCircuitByDSID(String dSID) {
        return getCircuitByDSID(new DSID(dSID));
    }

    @Override
    public Circuit updateCircuitConfig(Circuit newCircuit) {
        Circuit intCircuit = circuitMap.get(newCircuit.getDSID());
        if (intCircuit != null && !intCircuit.equals(newCircuit)) {
            for (CachedMeteringValue meteringValue : intCircuit.getAllCachedMeteringValues()) {
                newCircuit.addMeteringValue(meteringValue);
            }
            if (intCircuit.isListenerRegisterd()) {
                newCircuit.registerDeviceStatusListener(intCircuit.getDeviceStatusListener());
            }
        }
        return addCircuit(newCircuit);
    }

    @Override
    public Circuit deleteCircuit(DSID dSID) {
        return circuitMap.remove(dSID);
    }

    @Override
    public Circuit deleteCircuit(String dSUID) {
        return deleteCircuit(dSUIDToDSIDMap.get(dSUID));
    }

    @Override
    public Map<DSID, Circuit> getCircuitMap() {
        return new HashMap<DSID, Circuit>(circuitMap);
    }
}
