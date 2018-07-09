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
package org.eclipse.smarthome.binding.mqtt.generic.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

/**
 * Implements a color value.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ColorValue implements AbstractMqttThingValue {
    private HSBType colorValue;
    private final boolean isRGB;

    public ColorValue(boolean isRGB) {
        this.isRGB = isRGB;
        colorValue = new HSBType();
    }

    /**
     * Create color type from string.
     *
     * @param text Expects hue,saturation,brightness as comma separated string. hue is in the range [0,360],
     *            saturation and brightness are in [0,100].
     */
    public ColorValue(boolean isRGB, String text) {
        this.isRGB = isRGB;
        colorValue = new HSBType(text);
    }

    @Override
    public State getValue() {
        return colorValue;
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
        if (command instanceof HSBType) {
            colorValue = (HSBType) command;
        } else if (command instanceof StringType) {
            if (isRGB) {
                String[] split = command.toString().split(",");
                if (split.length != 3) {
                    throw new IllegalArgumentException(command.toString() + " is not a valid RGB syntax");
                }
                colorValue = HSBType.fromRGB(Integer.valueOf(split[0]), Integer.valueOf(split[1]),
                        Integer.valueOf(split[2]));
            } else {
                colorValue = new HSBType(command.toString());

            }
        } else {
            throw new IllegalArgumentException("Didn't recognise the color value " + command.toString());
        }
        return colorValue.toString();
    }

    /**
     * Updates the color value.
     *
     * @param updatedValue Expects hue,saturation,brightness as comma separated string. hue is in the range [0,360],
     *            saturation and brightness are in [0,100]. If rgb is enabled, a string red,green,blue is expected.
     *            red,green,blue are within [0,255].
     * @return Returns the color value as HSB/HSV string (hue, saturation, brightness) eg. "60, 100, 100".
     *         If rgb is enabled, an RGB string (red,green,blue) will be returned instead. red,green,blue are within
     *         [0,255].
     */
    @Override
    public State update(String updatedValue) throws IllegalArgumentException {
        colorValue = new HSBType(updatedValue);
        return colorValue;
    }

    @Override
    public String channelTypeID() {
        return CoreItemFactory.COLOR;
    }
}
