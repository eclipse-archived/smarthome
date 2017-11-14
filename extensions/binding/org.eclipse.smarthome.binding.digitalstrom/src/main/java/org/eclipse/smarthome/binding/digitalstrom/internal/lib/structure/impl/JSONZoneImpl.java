/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.impl;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.DetailedGroupInfo;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.Zone;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.impl.JSONDeviceImpl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link JSONZoneImpl} is the implementation of the {@link Zone}.
 *
 * @author Alexander Betker - Initial contribution
 * @author Michael Ochel - change from SimpleJSON to GSON
 * @author Matthias Siegele - change from SimpleJSON to GSON
 */
public class JSONZoneImpl implements Zone {

    private int zoneId = -1;
    private String name = null;

    private List<DetailedGroupInfo> groupList = null;
    private List<Device> deviceList = null;

    public JSONZoneImpl(JsonObject object) {
        this.groupList = new LinkedList<DetailedGroupInfo>();
        this.deviceList = new LinkedList<Device>();

        if (object.get(JSONApiResponseKeysEnum.APARTMENT_GET_STRUCTURE_ZONES_NAME.getKey()) != null) {
            this.name = object.get(JSONApiResponseKeysEnum.APARTMENT_GET_STRUCTURE_ZONES_NAME.getKey()).getAsString();
        }
        if (object.get(JSONApiResponseKeysEnum.APARTMENT_GET_STRUCTURE_ZONES_ID.getKey()) != null) {
            zoneId = object.get(JSONApiResponseKeysEnum.APARTMENT_GET_STRUCTURE_ZONES_ID.getKey()).getAsInt();
        }
        if (zoneId == -1) {
            if (object.get(JSONApiResponseKeysEnum.QUERY_ZONE_ID.getKey()) != null) {
                zoneId = object.get(JSONApiResponseKeysEnum.QUERY_ZONE_ID.getKey()).getAsInt();
            }
        }
        if (object.get(JSONApiResponseKeysEnum.APARTMENT_GET_STRUCTURE_ZONES_DEVICES.getKey()) instanceof JsonArray) {
            JsonArray list = (JsonArray) object
                    .get(JSONApiResponseKeysEnum.APARTMENT_GET_STRUCTURE_ZONES_DEVICES.getKey());
            for (int i = 0; i < list.size(); i++) {
                this.deviceList.add(new JSONDeviceImpl((JsonObject) list.get(i)));
            }
        }
        if (object.get(JSONApiResponseKeysEnum.APARTMENT_GET_STRUCTURE_ZONES_GROUPS.getKey()) instanceof JsonArray) {
            JsonArray groupList = (JsonArray) object
                    .get(JSONApiResponseKeysEnum.APARTMENT_GET_STRUCTURE_ZONES_GROUPS.getKey());
            for (int i = 0; i < groupList.size(); i++) {
                this.groupList.add(new JSONDetailedGroupInfoImpl((JsonObject) groupList.get(i)));
            }
        }
    }

    @Override
    public int getZoneId() {
        return zoneId;
    }

    @Override
    public synchronized void setZoneId(int id) {
        if (id > 0) {
            this.zoneId = id;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public synchronized void setName(String name) {
        this.name = name;
    }

    @Override
    public List<DetailedGroupInfo> getGroups() {
        return groupList;
    }

    @Override
    public void addGroup(DetailedGroupInfo group) {
        if (group != null) {
            synchronized (groupList) {
                if (!groupList.contains(group)) {
                    groupList.add(group);
                }
            }
        }
    }

    @Override
    public List<Device> getDevices() {
        return deviceList;
    }

    @Override
    public void addDevice(Device device) {
        if (device != null) {
            synchronized (deviceList) {
                if (!deviceList.contains(device)) {
                    deviceList.add(device);
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Zone) {
            Zone other = (Zone) obj;
            return (other.getZoneId() == this.getZoneId());
        }
        return false;
    }
}
