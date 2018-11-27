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
package org.eclipse.smarthome.binding.mqtt.generic.internal.values;

import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * Implements a color value.
 *
 * Accepts user updates from a HSBType, OnOffType or OpenClosedType.
 * Accepts MQTT state updates as comma separated HSB ("h,s,b"), RGB ("r,g,b") and on, off strings.
 * On, Off strings can be customized but "1","ON","0","OFF" are always recognized.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ColorValue implements Value {
    private State state = UnDefType.UNDEF;
    private HSBType colorValue;
    private final boolean isRGB;
    private final String onValue;
    private final String offValue;

    /**
     * Creates a non initialized color value.
     *
     * @param onValue The ON value string. This will be compared to MQTT messages.
     * @param offValue The OFF value string. This will be compared to MQTT messages.
     */
    public ColorValue(boolean isRGB, @Nullable String onValue, @Nullable String offValue) {
        this.isRGB = isRGB;
        colorValue = new HSBType();
        this.onValue = onValue == null ? "ON" : onValue;
        this.offValue = offValue == null ? "OFF" : offValue;
    }

    /**
     * Create color type from string.
     *
     * @param colorTextValue Expects hue,saturation,brightness as comma separated string. hue is in the range [0,360],
     *            saturation and brightness are in [0,100].
     * @param onValue The ON value string. This will be compared to MQTT messages.
     * @param offValue The OFF value string. This will be compared to MQTT messages.
     */
    public ColorValue(boolean isRGB, String colorTextValue, @Nullable String onValue, @Nullable String offValue) {
        this.isRGB = isRGB;
        colorValue = new HSBType(colorTextValue);
        this.onValue = onValue == null ? "ON" : onValue;
        this.offValue = offValue == null ? "OFF" : offValue;
    }

    @Override
    public State getValue() {
        return state;
    }

    /**
     * Updates the color value.
     *
     * @return Returns the color value as HSB/HSV string (hue, saturation, brightness) eg. "60, 100, 100".
     *         If rgb is enabled, an RGB string (red,green,blue) will be returned instead. red,green,blue are within
     *         [0,255].
     */
    @Override
    public String update(Command command) throws IllegalArgumentException {
        if (command instanceof OnOffType) {
            OnOffType boolValue = ((OnOffType) command);
            PercentType minOn = new PercentType(Math.max(colorValue.getBrightness().intValue(), 10));
            colorValue = new HSBType(colorValue.getHue(), colorValue.getSaturation(),
                    boolValue == OnOffType.ON ? minOn : new PercentType(0));
        } else if (command instanceof OpenClosedType) {
            OnOffType boolValue = ((OpenClosedType) command) == OpenClosedType.OPEN ? OnOffType.ON : OnOffType.OFF;
            PercentType minOn = new PercentType(Math.max(colorValue.getBrightness().intValue(), 10));
            colorValue = new HSBType(colorValue.getHue(), colorValue.getSaturation(),
                    boolValue == OnOffType.ON ? minOn : new PercentType(0));
        } else if (command instanceof HSBType) {
            colorValue = (HSBType) command;
        } else if (command instanceof StringType) {
            if (isRGB) {
                String[] split = command.toString().split(",");
                if (split.length != 3) {
                    throw new IllegalArgumentException(command.toString() + " is not a valid RGB syntax");
                }
                colorValue = HSBType.fromRGB(Integer.parseInt(split[0]), Integer.parseInt(split[1]),
                        Integer.parseInt(split[2]));
            } else {
                colorValue = new HSBType(command.toString());

            }
        } else {
            throw new IllegalArgumentException("Didn't recognise the color value " + command.toString());
        }
        state = colorValue;
        if (isRGB) {
            return colorValue.getRed() + "," + colorValue.getGreen() + "," + colorValue.getBlue();
        } else {
            return colorValue.toString();
        }
    }

    /**
     * Updates the color value.
     *
     * @param updatedValue Expects hue,saturation,brightness as comma separated string.
     *            hue is in the range [0,360], saturation and brightness are in [0,100].
     *            If rgb is enabled, a string red,green,blue is expected. red,green,blue are within [0,255].
     *            ON/OFF (case insensitive) are also accepted to set the brightness to full and off.
     *            If a single integer value is received, it is interpreted as a brightness value.
     * @return Returns the color value as HSB/HSV string (hue, saturation, brightness) eg. "60, 100, 100".
     *         If rgb is enabled, an RGB string (red,green,blue) will be returned instead. red,green,blue are within
     *         [0,255].
     */
    @Override
    public State update(String updatedValue) throws IllegalArgumentException {
        if (onValue.equals(updatedValue)) {
            PercentType minOn = new PercentType(Math.max(colorValue.getBrightness().intValue(), 10));
            colorValue = new HSBType(colorValue.getHue(), colorValue.getSaturation(), minOn);
        } else if (offValue.equals(updatedValue)) {
            colorValue = new HSBType(colorValue.getHue(), colorValue.getSaturation(), new PercentType(0));
        } else if (updatedValue.indexOf(',') > 0) {
            if (isRGB) {
                String[] split = updatedValue.toString().split(",");
                if (split.length != 3) {
                    throw new IllegalArgumentException(updatedValue + " is not a valid RGB syntax");
                }
                colorValue = HSBType.fromRGB(Integer.parseInt(split[0]), Integer.parseInt(split[1]),
                        Integer.parseInt(split[2]));
            } else {
                colorValue = new HSBType(updatedValue);
            }
        } else { // single integer value
            colorValue = new HSBType(colorValue.getHue(), colorValue.getSaturation(), new PercentType(updatedValue));
        }
        state = colorValue;
        return colorValue;
    }

    @Override
    public String getItemType() {
        return CoreItemFactory.COLOR;
    }

    @Override
    public StateDescription createStateDescription(String unit, boolean readOnly) {
        return new StateDescription(null, null, null, "%s " + unit.replace("%", "%%"), readOnly,
                Collections.emptyList());
    }

    @Override
    public void resetState() {
        state = UnDefType.UNDEF;
    }
}
