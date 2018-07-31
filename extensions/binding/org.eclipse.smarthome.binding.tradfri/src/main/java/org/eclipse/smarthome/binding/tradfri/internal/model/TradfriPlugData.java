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
package org.eclipse.smarthome.binding.tradfri.internal.model;

import static org.eclipse.smarthome.binding.tradfri.TradfriBindingConstants.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * The {@link TradfriPlugData} class is a Java wrapper for the raw JSON data about the plug state.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class TradfriPlugData extends TradfriDeviceData {

    public TradfriPlugData() {
        super(PLUG);
    }

    public TradfriPlugData(JsonElement json) {
        super(PLUG, json);
    }

    public TradfriPlugData setTransitionTime(int seconds) {
        attributes.add(TRANSITION_TIME, new JsonPrimitive(seconds));
        return this;
    }

    public int getTransitionTime() {
        JsonElement transitionTime = attributes.get(TRANSITION_TIME);
        if (transitionTime != null) {
            return transitionTime.getAsInt();
        } else {
            return 0;
        }
    }

    public TradfriPlugData setOnOffState(boolean on) {
        attributes.add(ONOFF, new JsonPrimitive(on ? 1 : 0));
        return this;
    }

    public boolean getOnOffState() {
        JsonElement onOff = attributes.get(ONOFF);
        if (onOff != null) {
            return onOff.getAsInt() == 1;
        } else {
            return false;
        }
    }

    public String getJsonString() {
        return root.toString();
    }
}
