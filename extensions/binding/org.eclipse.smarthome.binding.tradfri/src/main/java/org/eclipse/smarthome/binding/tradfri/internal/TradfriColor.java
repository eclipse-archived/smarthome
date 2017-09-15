/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.tradfri.internal;

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;

/**
 * The {@link TradfriColor} is used for conversion between color formats.
 * Use the static methods {@link TradfriColor#fromCie(int, int, int)} and {@link TradfriColor#fromHSBType(HSBType)} for
 * construction.
 *
 * @author Holger Reichert - Initial contribution
 *
 */
public class TradfriColor {

    // Tradfri uses the CIE color space (see https://en.wikipedia.org/wiki/CIE_1931_color_space),
    // which uses x,y-coordinates.
    // Its own app comes with 3 predefined color temperature settings (0,1,2), which have those values:
    private final static double[] PRESET_X = new double[] { 24933.0, 30138.0, 33137.0 };
    private final static double[] PRESET_Y = new double[] { 24691.0, 26909.0, 27211.0 };

    /**
     * RGB color values in the range 0 to 255.
     * May be <code>null</code> if the calculation method does not support this color range.
     */
    public Integer rgbR, rgbG, rgbB;

    /**
     * CIE XY color values in the tradfri range 0 to 65535.
     * May be <code>null</code> if the calculation method does not support this color range.
     */
    public Integer xyX, xyY;

    /**
     * Brightness level in the tradfri range 0 to 254.
     * May be <code>null</code> if the calculation method does not support this color range.
     */
    public Integer brightness;

    /**
     * Private constructor based on all fields.
     *
     * @param rgbR RGB red value 0 to 255
     * @param rgbG RGB green value 0 to 255
     * @param rgbB RGB blue value 0 to 255
     * @param xyX CIE x value 0 to 65535
     * @param xyY CIE y value 0 to 65535
     * @param brightness brightness level 0 to 254
     */
    private TradfriColor(Integer rgbR, Integer rgbG, Integer rgbB, Integer xyX, Integer xyY, Integer brightness) {
        super();
        this.rgbR = rgbR;
        this.rgbG = rgbG;
        this.rgbB = rgbB;
        this.xyX = xyX;
        this.xyY = xyY;
        this.brightness = brightness;
    }

    /**
     * Construct from CIE XY values in the tradfri range.
     *
     * @param xyX x value 0 to 65535
     * @param xyY y value 0 to 65535
     * @param xyBrightness brightness from 0 to 254
     * @return {@link TradfriColor} object with converted color spaces
     */
    public static TradfriColor fromCie(int xyX, int xyY, int xyBrightness) {

        // maximum brightness limited to 254
        int brightness = xyBrightness;
        if (brightness > 254) {
            brightness = 254;
        }

        double x = unnormalize(xyX);
        double y = unnormalize(xyY);

        // calculate XYZ using xy and brightness
        double z = 1.0 - x - y;
        double Y = (brightness / 254.0);
        double X = (Y / y) * x;
        double Z = (Y / y) * z;

        // Wide RGB D65 conversion
        // math inspiration: http://www.brucelindbloom.com/index.html?Eqn_RGB_XYZ_Matrix.html
        double red = X * 1.656492 - Y * 0.354851 - Z * 0.255038;
        double green = -X * 0.707196 + Y * 1.655397 + Z * 0.036152;
        double blue = X * 0.051713 - Y * 0.121364 + Z * 1.011530;

        // cap all values to 1.0 maximum
        if (red > blue && red > green && red > 1.0) {
            green = green / red;
            blue = blue / red;
            red = 1.0;
        } else if (green > blue && green > red && green > 1.0) {
            red = red / green;
            blue = blue / green;
            green = 1.0;
        } else if (blue > red && blue > green && blue > 1.0) {
            red = red / blue;
            green = green / blue;
            blue = 1.0;
        }

        // gamma correction
        red = red <= 0.0031308 ? 12.92 * red : (1.0 + 0.055) * Math.pow(red, (1.0 / 2.4)) - 0.055;
        green = green <= 0.0031308 ? 12.92 * green : (1.0 + 0.055) * Math.pow(green, (1.0 / 2.4)) - 0.055;
        blue = blue <= 0.0031308 ? 12.92 * blue : (1.0 + 0.055) * Math.pow(blue, (1.0 / 2.4)) - 0.055;

        // target range 0 to 255
        int rgbR = (int) Math.round(red * 255.0);
        int rgbG = (int) Math.round(green * 255.0);
        int rgbB = (int) Math.round(blue * 255.0);

        return new TradfriColor(rgbR, rgbG, rgbB, xyX, xyY, brightness);
    }

    /**
     * Construct from {@link HSBType}.
     *
     * @param hsbType {@link HSBType}
     * @return {@link TradfriColor} object with converted color spaces
     */
    public static TradfriColor fromHSBType(HSBType hsbType) {

        // hsbType gives 0 to 100, we need 0.0 to 255.0
        double red = hsbType.getRed().intValue() * 2.55;
        double green = hsbType.getGreen().intValue() * 2.55;
        double blue = hsbType.getBlue().intValue() * 2.55;

        // saved for later use in constructor call
        int rgbR = (int) red;
        int rgbG = (int) green;
        int rgbB = (int) blue;

        // gamma correction
        red = (red > 0.04045) ? Math.pow((red + 0.055) / (1.0 + 0.055), 2.4) : (red / 12.92);
        green = (green > 0.04045) ? Math.pow((green + 0.055) / (1.0 + 0.055), 2.4) : (green / 12.92);
        blue = (blue > 0.04045) ? Math.pow((blue + 0.055) / (1.0 + 0.055), 2.4) : (blue / 12.92);

        // Wide RGB D65 conversion
        // math inspiration: http://www.brucelindbloom.com/index.html?Eqn_RGB_XYZ_Matrix.html
        double X = red * 0.664511 + green * 0.154324 + blue * 0.162028;
        double Y = red * 0.283881 + green * 0.668433 + blue * 0.047685;
        double Z = red * 0.000088 + green * 0.072310 + blue * 0.986039;

        // calculate the xy values from XYZ
        double x = (X / (X + Y + Z));
        double y = (Y / (X + Y + Z));

        int xyX = normalize(x);
        int xyY = normalize(y);
        int brightness = (int) (hsbType.getBrightness().intValue() * 2.54);

        return new TradfriColor(rgbR, rgbG, rgbB, xyX, xyY, brightness);
    }

    /**
     * Construct from color temperature in percent.
     * 0 (coldest) to 100 (warmest).
     * Note: The resulting {@link TradfriColor} has only the {@link TradfriColor#xyX X} and {@link TradfriColor#xyY y}
     * values set!
     * 
     * @param percentType the color temperature in percent
     * @return {@link TradfriColor} object with x and y values
     */
    public static TradfriColor fromColorTemperature(PercentType percentType) {
        double percent = percentType.doubleValue();

        int x, y;
        if (percent < 50.0) {
            // we calculate a value that is between preset 0 and 1
            double p = percent / 50.0;
            x = (int) Math.round(PRESET_X[0] + p * (PRESET_X[1] - PRESET_X[0]));
            y = (int) Math.round(PRESET_Y[0] + p * (PRESET_Y[1] - PRESET_Y[0]));
        } else {
            // we calculate a value that is between preset 1 and 2
            double p = (percent - 50) / 50.0;
            x = (int) Math.round(PRESET_X[1] + p * (PRESET_X[2] - PRESET_X[1]));
            y = (int) Math.round(PRESET_Y[1] + p * (PRESET_Y[2] - PRESET_Y[1]));
        }

        return new TradfriColor(null, null, null, x, y, null);
    }

    /**
     * Normalize value to the tradfri range.
     *
     * @param value double in the range 0.0 to 1.0
     * @return normalized value in the range 0 to 65535
     */
    public static int normalize(double value) {
        return (int) (value * 65535 + 0.5);
    }

    /**
     * Reverse-normalize value from the tradfri range.
     *
     * @param value integer in the range 0 to 65535
     * @return unnormalized value in the range 0.0 to 1.0
     */
    public static double unnormalize(int value) {
        return (value / 65535.0);
    }

    /**
     * Calculate the color temperature from given x and y values.
     *
     * @param xyX the CIE x value
     * @param xyY the CIE y value
     * @return {@link PercentType} with color temperature (0 = coolest, 100 = warmest)
     */
    public static PercentType calculateColorTemperature(int xyX, int xyY) {
        double x = xyX;
        double y = xyY;
        double value = 0.0;
        if ((x > PRESET_X[1] && y > PRESET_Y[1]) && (x <= PRESET_X[2] && y <= PRESET_Y[2])) {
            // is it between preset 1 and 2?
            value = (x - PRESET_X[1]) / (PRESET_X[2] - PRESET_X[1]) / 2.0 + 0.5;
        } else if ((x >= PRESET_X[0] && y >= PRESET_Y[0]) && (x <= (PRESET_X[1] + 2.0) && y <= PRESET_Y[1])) {
            // is it between preset 0 and 1?
            // hint: in the above line we calculate 2.0 to PRESET_X[1] because
            // some bulbs send slighty higher x values for this preset (maybe rounding errors?)
            value = (x - PRESET_X[0]) / (PRESET_X[1] - PRESET_X[0]) / 2.0;
        } else if (x < PRESET_X[0]) {
            // cooler than coolest preset (full color bulbs)
            value = 0.0;
        } else if (x > PRESET_X[2]) {
            // warmer than warmest preset (full color bulbs)
            value = 1.0;
        }
        return new PercentType((int) Math.round(value * 100.0));
    }

}
