/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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

import org.eclipse.smarthome.binding.tradfri.internal.TradfriColor;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * The {@link TradfriLightData} class is a Java wrapper for the raw JSON data about the light state.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Holger Reichert - Support for color bulbs
 * @author Christoph Weitkamp - Restructuring and refactoring of the binding
 */
public class TradfriLightData extends TradfriDeviceData {

    private final Logger logger = LoggerFactory.getLogger(TradfriLightData.class);

    public TradfriLightData() {
        super(LIGHT);
    }

    public TradfriLightData(JsonElement json) {
        super(LIGHT, json);
    }

    public TradfriLightData setBrightness(PercentType brightness) {
        attributes.add(DIMMER, new JsonPrimitive(Math.round(brightness.floatValue() / 100.0f * 254)));
        return this;
    }

    public PercentType getBrightness() {
        PercentType result = null;

        JsonElement dimmer = attributes.get(DIMMER);
        if (dimmer != null) {
            result = TradfriColor.xyBrightnessToPercentType(dimmer.getAsInt());
        }

        return result;
    }

    public TradfriLightData setTransitionTime(int seconds) {
        attributes.add(TRANSITION_TIME, new JsonPrimitive(seconds));
        return this;
    }

    @SuppressWarnings("unused")
    public int getTransitionTime() {
        JsonElement transitionTime = attributes.get(TRANSITION_TIME);
        if (transitionTime != null) {
            return transitionTime.getAsInt();
        } else {
            return 0;
        }
    }

    public TradfriLightData setColorTemperature(PercentType c) {
        TradfriColor color = TradfriColor.fromColorTemperature(c);
        int x = color.xyX;
        int y = color.xyY;
        logger.debug("New color temperature: {},{} ({} %)", x, y, c.intValue());
        attributes.add(COLOR_X, new JsonPrimitive(x));
        attributes.add(COLOR_Y, new JsonPrimitive(y));
        return this;
    }

    public PercentType getColorTemperature() {
        JsonElement colorX = attributes.get(COLOR_X);
        JsonElement colorY = attributes.get(COLOR_Y);
        if (colorX != null && colorY != null) {
            return TradfriColor.calculateColorTemperature(colorX.getAsInt(), colorY.getAsInt());
        } else {
            return null;
        }
    }

    public TradfriLightData setColor(HSBType hsb) {
        // construct new HSBType with full brightness and extract XY color values from it
        HSBType hsbFullBright = new HSBType(hsb.getHue(), hsb.getSaturation(), PercentType.HUNDRED);
        TradfriColor color = TradfriColor.fromHSBType(hsbFullBright);
        attributes.add(COLOR_X, new JsonPrimitive(color.xyX));
        attributes.add(COLOR_Y, new JsonPrimitive(color.xyY));
        return this;
    }

    public HSBType getColor() {
        // XY color coordinates plus brightness is needed for color calculation
        JsonElement colorX = attributes.get(COLOR_X);
        JsonElement colorY = attributes.get(COLOR_Y);
        JsonElement dimmer = attributes.get(DIMMER);
        if (colorX != null && colorY != null && dimmer != null) {
            int x = colorX.getAsInt();
            int y = colorY.getAsInt();
            int brightness = dimmer.getAsInt();
            // extract HSBType from converted xy/brightness
            TradfriColor color = TradfriColor.fromCie(x, y, brightness);
            return color.hsbType;
        }
        return null;
    }

    public TradfriLightData setOnOffState(boolean on) {
        attributes.add(ONOFF, new JsonPrimitive(on ? 1 : 0));
        return this;
    }

    public boolean getOnOffState() {
        JsonElement onOff = attributes.get(ONOFF);
        if (onOff != null) {
            return attributes.get(ONOFF).getAsInt() == 1;
        } else {
            return false;
        }
    }

    public String getJsonString() {
        return root.toString();
    }
}
