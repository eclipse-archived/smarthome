/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.tradfri.internal.model;

import static org.eclipse.smarthome.binding.tradfri.TradfriBindingConstants.*;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

/**
 * The {@link TradfriWirelessDeviceData} class is a Java wrapper for the raw JSON data about wireless device state.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public abstract class TradfriWirelessDeviceData extends TradfriDeviceData {

    private final Logger logger = LoggerFactory.getLogger(TradfriWirelessDeviceData.class);

    public TradfriWirelessDeviceData(String attributesNodeName) {
        super(attributesNodeName);
    }

    public TradfriWirelessDeviceData(String attributesNodeName, JsonElement json) {
        super(attributesNodeName, json);
    }

    public DecimalType getBatteryLevel() {
        if (generalInfo.get(DEVICE_BATTERY_LEVEL) != null) {
            return new DecimalType(generalInfo.get(DEVICE_BATTERY_LEVEL).getAsInt());
        } else {
            return null;
        }
    }

    public OnOffType getBatteryLow() {
        if (generalInfo.get(DEVICE_BATTERY_LEVEL) != null) {
            return generalInfo.get(DEVICE_BATTERY_LEVEL).getAsInt() <= 10 ? OnOffType.ON : OnOffType.OFF;
        } else {
            return null;
        }
    }
}
