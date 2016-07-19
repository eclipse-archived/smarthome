/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DSID;

/**
 * The {@link StructureManager} builds the internal model of the digitalSTROM-System.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public interface StructureManager {

    /**
     * Generates the zone- and group-names.
     *
     * @param connectionManager
     * @return true, if it's generated, otherwise false
     */
    public boolean generateZoneGroupNames(ConnectionManager connectionManager);

    /**
     * Returns the name of a zone or null, if the given zoneID dose not exists.<br>
     * Note: Zone-names have to be generated over {@link #generateZoneGroupNames(ConnectionManager)}.
     *
     * @param zoneID
     * @return zone-name
     */
    public String getZoneName(int zoneID);

    /**
     * Returns the id of a given zone-name or -1, if the given zone-name dose not exists.<br>
     * Note: Zone-names have to be generated over {@link #generateZoneGroupNames(ConnectionManager)}.
     *
     * @param zoneName
     * @return zoneID
     */
    public int getZoneId(String zoneName);

    /**
     * Returns the name of the given groupID from the given zoneID or null, if the zoneID or groupID dose not exists.
     * <br>
     * Note: Zone-group-names have to be generated over {@link #generateZoneGroupNames(ConnectionManager)}.
     *
     * @param zoneID
     * @param groupID
     * @return group-name
     */
    public String getZoneGroupName(int zoneID, short groupID);

    /**
     * Returns the groupID of the given group-name from the given zone name or -1, if the zone-name or group name dose
     * not exists.<br>
     * Note: Zone-group-names have to be generated over {@link #generateZoneGroupNames(ConnectionManager)}.
     *
     * @param zoneName
     * @param groupName
     * @return group-id
     */
    public short getZoneGroupId(String zoneName, String groupName);

    /**
     * Returns a new {@link Map} of all {@link Device}'s with the {@link DSID} as key and the {@link Device} as value.
     * If no devices are found, an empty {@link Map} will be returned.
     *
     * @return device-map (cannot be null)
     */
    public Map<DSID, Device> getDeviceMap();

    /**
     * Returns a reference to the {@link Map} of all {@link Device}'s with the {@link DSID} as key and the
     * {@link Device} as value. If no devices are found, an empty {@link Map} will be returned.
     *
     * @return reference device-map
     */
    public Map<DSID, Device> getDeviceHashMapReference();

    /**
     * Returns the reference of the structure as HashMap< zoneID, HashMap< groupID, List< Device>>>.
     *
     * @return structure reference
     */
    public Map<Integer, HashMap<Short, List<Device>>> getStructureReference();

    /**
     * Returns the Map of all groups as format HashMap< Short, List< Device>>.
     *
     * @param zoneID
     * @return groups
     */
    public HashMap<Short, List<Device>> getGroupsFromZoneX(int zoneID);

    /**
     * Returns the reference {@link List} of the {@link Device}'s of an zone-group.
     *
     * @param zoneID
     * @param groupID
     * @return reference device-list
     */
    public List<Device> getReferenceDeviceListFromZoneXGroupX(int zoneID, short groupID);

    /**
     * Returns the {@link Device} of the given dSID as {@link String} or null if no {@link Device} exists.
     *
     * @param dSID
     * @return device
     */
    public Device getDeviceByDSID(String dSID);

    /**
     * Returns the {@link Device} of the given dSID as {@link DSID} or null if no {@link Device} exists.
     *
     * @param dSID
     * @return device
     */
    public Device getDeviceByDSID(DSID dSID);

    /**
     * Returns the {@link Device} of the given dSUID or null if no {@link Device} exists.
     *
     * @param dSUID
     * @return
     */
    public Device getDeviceByDSUID(String dSUID);

    /**
     * Updates a {@link Device} of the structure.
     *
     * @param oldZone
     * @param oldGroups
     * @param device
     */
    public void updateDevice(int oldZone, List<Short> oldGroups, Device device);

    /**
     * Updates a {@link Device} of the structure.
     *
     * @param device
     */
    public void updateDevice(Device device);

    /**
     * Deletes a {@link Device} from the structure.
     *
     * @param device
     */
    public void deleteDevice(Device device);

    /**
     * Adds a {@link Device} to the structure.
     *
     * @param device
     */
    public void addDeviceToStructure(Device device);

    /**
     * Returns a {@link Set} of all zoneID's
     *
     * @return zoneID's
     */
    public Set<Integer> getZoneIDs();

    /**
     * Returns true, if a zone with the given zoneID exists, otherwise false.
     *
     * @param zoneID
     * @return true = zoneID exists | false = zoneID not exists
     */
    public boolean checkZoneID(int zoneID);

    /**
     * Returns true, if a zone-group with the given zoneID and groupID exists, otherwise false.
     *
     * @param zoneID
     * @param groupID
     * @return true = zoneID or groupID exists | false = zoneID or groupID not exists
     */
    public boolean checkZoneGroupID(int zoneID, short groupID);
}
