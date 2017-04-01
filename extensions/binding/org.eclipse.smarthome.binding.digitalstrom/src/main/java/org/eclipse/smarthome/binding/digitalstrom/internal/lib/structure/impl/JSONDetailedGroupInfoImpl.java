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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link JSONDetailedGroupInfoImpl} is the implementation of the {@link DetailedGroupInfo}.
 *
 * @author Alexander Betker - Initial contribution
 * @author Michael Ochel - change from SimpleJSON to GSON
 * @author Matthias Siegele - change from SimpleJSON to GSON
 */
public class JSONDetailedGroupInfoImpl implements DetailedGroupInfo {

    private String name = null;
    private short groupId = 0;
    private List<String> deviceList = null;

    public JSONDetailedGroupInfoImpl(JsonObject jObject) {
        this.deviceList = new LinkedList<String>();
        if (jObject.get(JSONApiResponseKeysEnum.GROUP_NAME.getKey()) != null) {
            name = jObject.get(JSONApiResponseKeysEnum.GROUP_NAME.getKey()).getAsString();
        }
        if (jObject.get(JSONApiResponseKeysEnum.GROUP_ID.getKey()) != null) {
            this.groupId = jObject.get(JSONApiResponseKeysEnum.GROUP_ID.getKey()).getAsShort();
        }
        if (jObject.get(JSONApiResponseKeysEnum.GROUP_DEVICES.getKey()) instanceof JsonArray) {
            JsonArray array = (JsonArray) jObject.get(JSONApiResponseKeysEnum.GROUP_DEVICES.getKey());

            for (int i = 0; i < array.size(); i++) {
                if (array.get(i) != null) {
                    deviceList.add(array.get(i).getAsString());
                }
            }
        }
    }

    @Override
    public short getGroupID() {
        return groupId;
    }

    @Override
    public String getGroupName() {
        return name;
    }

    @Override
    public List<String> getDeviceList() {
        return deviceList;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DetailedGroupInfo) {
            DetailedGroupInfo group = (DetailedGroupInfo) obj;
            return group.getGroupID() == this.getGroupID();
        }
        return false;
    }
}
