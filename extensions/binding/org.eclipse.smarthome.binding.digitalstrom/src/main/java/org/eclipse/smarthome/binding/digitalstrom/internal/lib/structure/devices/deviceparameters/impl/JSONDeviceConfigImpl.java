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
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverconnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.DeviceConfig;

import com.google.gson.JsonObject;

/**
 * The {@link JSONDeviceConfigImpl} is the implementation of the {@link DeviceConfig}.
 *
 * @author Alexander Betker - initial contributer
 * @author Michael Ochel - change from SimpleJSON to GSON
 * @author Matthias Siegele - change from SimpleJSON to GSON
 */
public class JSONDeviceConfigImpl implements DeviceConfig {

    private int class_ = -1;
    private int index = -1;
    private int value = -1;

    /**
     * Creates a new {@link JSONDeviceConfigImpl}.
     *
     * @param object must not be null
     */
    public JSONDeviceConfigImpl(JsonObject object) {
        if (object.get(JSONApiResponseKeysEnum.CLASS.getKey()) != null) {
            class_ = object.get(JSONApiResponseKeysEnum.CLASS.getKey()).getAsInt();
        }
        if (object.get(JSONApiResponseKeysEnum.INDEX.getKey()) != null) {
            index = object.get(JSONApiResponseKeysEnum.INDEX.getKey()).getAsInt();
        }
        if (object.get(JSONApiResponseKeysEnum.VALUE.getKey()) != null) {
            value = object.get(JSONApiResponseKeysEnum.VALUE.getKey()).getAsInt();
        }
    }

    @Override
    public int getConfigurationClass() {
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
