/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;

import com.google.gson.JsonObject;

/**
 * The {@link JSONDeviceConfigImpl} is the implementation of the {@link DeviceConfig}.
 *
 * @author Alexander Betker
 * @author Michael Ochel - change from SimpleJSON to GSON
 * @author Matthias Siegele - change from SimpleJSON to GSON
 */
public class JSONDeviceConfigImpl implements DeviceConfig {

    private int class_ = -1;
    private int index = -1;
    private int value = -1;

    public JSONDeviceConfigImpl(JsonObject object) {
        if (object.get(JSONApiResponseKeysEnum.DEVICE_GET_CONFIG_CLASS.getKey()) != null) {
            class_ = object.get(JSONApiResponseKeysEnum.DEVICE_GET_CONFIG_CLASS.getKey()).getAsInt();
        }
        if (object.get(JSONApiResponseKeysEnum.DEVICE_GET_CONFIG_INDEX.getKey()) != null) {
            index = object.get(JSONApiResponseKeysEnum.DEVICE_GET_CONFIG_INDEX.getKey()).getAsInt();
        }
        if (object.get(JSONApiResponseKeysEnum.DEVICE_GET_CONFIG_VALUE.getKey()) != null) {
            value = object.get(JSONApiResponseKeysEnum.DEVICE_GET_CONFIG_VALUE.getKey()).getAsInt();
        }
    }

    @Override
    public int getClass_() {
        return class_;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "class: " + this.class_ + ", " + "index: " + this.index + ", " + "value: " + this.value;
    }
}
