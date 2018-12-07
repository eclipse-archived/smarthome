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
package org.eclipse.smarthome.binding.hue.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.hue.internal.dto.Light;
import org.eclipse.smarthome.binding.hue.internal.dto.LightState;
import org.eclipse.smarthome.binding.hue.internal.dto.LightState.AlertMode;
import org.eclipse.smarthome.binding.hue.internal.dto.LightState.ColorMode;
import org.eclipse.smarthome.binding.hue.internal.dto.LightState.Effect;
import org.eclipse.smarthome.binding.hue.internal.dto.updates.LightStateUpdate;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;

/**
 * The {@link LightStateConverter} is responsible for mapping Eclipse SmartHome
 * types to jue types and vice versa.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Oliver Libutzki - Adjustments
 * @author Kai Kreuzer - made code static
 * @author Andre Fuechsel - added method for brightness
 * @author Yordan Zhelev - added method for alert
 * @author Denis Dudnik - switched to internally integrated source of Jue library, minor code cleanup
 * @author Christoph Weitkamp - Added support for bulbs using CIE XY colormode only
 */
@NonNullByDefault
public class LightStateConverter {

    private static final int HUE_FACTOR = 182;
    private static final double SATURATION_FACTOR = 2.54;
    private static final double BRIGHTNESS_FACTOR = 2.54;

    private static final int MIN_COLOR_TEMPERATURE = 153;
    private static final int MAX_COLOR_TEMPERATURE = 500;
    private static final int COLOR_TEMPERATURE_RANGE = MAX_COLOR_TEMPERATURE - MIN_COLOR_TEMPERATURE;

    /**
     * {@value #ALERT_MODE_NONE}. The light is not performing an alert effect.
     */
    public static final String ALERT_MODE_NONE = "NONE";
    /**
     * {@value #ALERT_MODE_SELECT}. The light is performing one breathe cycle.
     */
    public static final String ALERT_MODE_SELECT = "SELECT";
    /**
     * {@value #ALERT_MODE_LONG_SELECT}. The light is performing breathe cycles
     * for 15 seconds or until an "alert": "none" command is received.
     */
    public static final String ALERT_MODE_LONG_SELECT = "LSELECT";

    private static final int DIM_STEPSIZE = 30;

    /**
     * Transforms the given {@link HSBType} into a light state.
     *
     * @param hsbType HSB type
     * @return light state representing the {@link HSBType}.
     */
    public static LightStateUpdate toColorLightState(LightStateUpdate LightStateUpdate, HSBType hsbType,
            LightState lightState) {
        LightStateUpdate LightStateUpdate2 = ColorMode.xy.equals(lightState.colormode)
                ? toXYColorLightState(LightStateUpdate, hsbType)
                : toHSBColorLightState(LightStateUpdate, hsbType);

        int brightness = (int) Math.round(hsbType.getBrightness().doubleValue() * BRIGHTNESS_FACTOR);
        if (brightness > 0) {
            LightStateUpdate2.bri = brightness;
        }
        return LightStateUpdate2;
    }

    public static LightStateUpdate toBrightnessLightStateRel(LightStateUpdate LightStateUpdate,
            IncreaseDecreaseType command, Light light) {
        int newBrightness = LightStateConverter.toAdjustedBrightness(command, light.state.bri);
        if (newBrightness == 0) {
            LightStateUpdate.on = false;
        } else {
            LightStateUpdate.bri = newBrightness;
            if (light.state.bri == 0 || !light.state.on) {
                LightStateUpdate.on = true;
            }
        }
        return LightStateUpdate;
    }

    private static LightStateUpdate toHSBColorLightState(LightStateUpdate LightStateUpdate, HSBType hsbType) {
        int hue = (int) Math.round(hsbType.getHue().doubleValue() * HUE_FACTOR);
        int saturation = (int) Math.round(hsbType.getSaturation().doubleValue() * SATURATION_FACTOR);

        LightStateUpdate.hue = hue;
        LightStateUpdate.sat = saturation;
        return LightStateUpdate;
    }

    private static LightStateUpdate toXYColorLightState(LightStateUpdate LightStateUpdate, HSBType hsbType) {
        PercentType[] xy = hsbType.toXY();
        float x = xy[0].floatValue() / 100.0f;
        float y = xy[1].floatValue() / 100.0f;

        LightStateUpdate.xy = new Float[] { x, y };
        return LightStateUpdate;
    }

    /**
     * Transforms the given {@link OnOffType} into a light state containing the
     * 'on' value.
     *
     * @param onOffType on or off state
     * @return light state containing the 'on' value
     */
    public static LightStateUpdate toOnOffLightState(LightStateUpdate LightStateUpdate, OnOffType onOffType) {
        LightStateUpdate.on = onOffType == OnOffType.ON;
        return LightStateUpdate;
    }

    /**
     * Transforms the given {@link PercentType} into a light state containing
     * the brightness and the 'on' value represented by {@link PercentType}.
     *
     * @param percentType brightness represented as {@link PercentType}
     * @return light state containing the brightness and the 'on' value
     */
    public static LightStateUpdate toBrightnessLightState(LightStateUpdate LightStateUpdate, PercentType percentType) {
        boolean on = !percentType.equals(PercentType.ZERO);
        LightStateUpdate.on = on;

        int brightness = (int) Math.round(percentType.doubleValue() * BRIGHTNESS_FACTOR);
        if (brightness > 0) {
            LightStateUpdate.bri = brightness;
        }
        return LightStateUpdate;
    }

    /**
     * Adjusts the given brightness using the {@link IncreaseDecreaseType} and
     * returns the updated value.
     *
     * @param command The {@link IncreaseDecreaseType} to be used
     * @param currentBrightness The current brightness
     * @return The adjusted brightness value
     */
    public static int toAdjustedBrightness(IncreaseDecreaseType command, int currentBrightness) {
        int newBrightness;
        if (command == IncreaseDecreaseType.DECREASE) {
            newBrightness = Math.max(currentBrightness - DIM_STEPSIZE, 0);
        } else {
            newBrightness = Math.min(currentBrightness + DIM_STEPSIZE, (int) (BRIGHTNESS_FACTOR * 100));
        }
        return newBrightness;
    }

    /**
     * Transforms the given {@link PercentType} into a light state containing
     * the color temperature represented by {@link PercentType}.
     *
     * @param percentType color temperature represented as {@link PercentType}
     * @return light state containing the color temperature
     */
    public static LightStateUpdate toColorTemperatureLightState(LightStateUpdate LightStateUpdate,
            PercentType percentType) {
        int colorTemperature = MIN_COLOR_TEMPERATURE
                + Math.round((COLOR_TEMPERATURE_RANGE * percentType.floatValue()) / 100);
        LightStateUpdate.ct = colorTemperature;
        return LightStateUpdate;
    }

    /**
     * Adjusts the given color temperature using the {@link IncreaseDecreaseType} and returns the updated value.
     *
     * @param type The {@link IncreaseDecreaseType} to be used
     * @param currentColorTemp The current color temperature
     * @return The adjusted color temperature value
     */
    public static int toAdjustedColorTemp(IncreaseDecreaseType type, int currentColorTemp) {
        int newColorTemp;
        if (type == IncreaseDecreaseType.DECREASE) {
            newColorTemp = Math.max(currentColorTemp - DIM_STEPSIZE, MIN_COLOR_TEMPERATURE);
        } else {
            newColorTemp = Math.min(currentColorTemp + DIM_STEPSIZE, MAX_COLOR_TEMPERATURE);
        }
        return newColorTemp;
    }

    /**
     * Transforms Hue Light {@link LightState} into {@link PercentType} representing
     * the color temperature.
     *
     * @param lightState light state
     * @return percent type representing the color temperature
     */
    public static PercentType toColorTemperaturePercentType(LightState lightState) {
        int percent = (int) Math.round(((lightState.ct - MIN_COLOR_TEMPERATURE) * 100.0) / COLOR_TEMPERATURE_RANGE);
        return new PercentType(restrictToBounds(percent));
    }

    /**
     * Transforms Hue Light {@link LightState} into {@link PercentType} representing
     * the brightness.
     *
     * @param lightState light state
     * @return percent type representing the brightness
     */
    public static PercentType toBrightnessPercentType(LightState lightState) {
        int percent = (int) Math.round(lightState.bri / BRIGHTNESS_FACTOR);
        return new PercentType(restrictToBounds(percent));
    }

    /**
     * Transforms Hue Light {@link LightState} into {@link HSBType} representing the
     * color.
     *
     * @param lightState light state
     * @return HSB type representing the color
     */
    public static HSBType toHSBType(LightState lightState) {
        // even if color mode is reported to be XY, xy field of lightState might be null, while hsb is available
        boolean isInXYMode = ColorMode.xy.equals(lightState.colormode) && lightState.xy != null;
        return isInXYMode ? fromXYtoHSBType(lightState) : fromHSBtoHSBType(lightState);
    }

    private static HSBType fromHSBtoHSBType(LightState lightState) {
        int hue = lightState.hue;

        int saturationInPercent = (int) Math.round(lightState.sat / SATURATION_FACTOR);
        saturationInPercent = restrictToBounds(saturationInPercent);

        int brightnessInPercent = (int) Math.round(lightState.bri / BRIGHTNESS_FACTOR);
        brightnessInPercent = restrictToBounds(brightnessInPercent);

        return new HSBType(new DecimalType(hue / HUE_FACTOR), new PercentType(saturationInPercent),
                new PercentType(brightnessInPercent));
    }

    private static HSBType fromXYtoHSBType(LightState lightState) {
        float[] xy = lightState.xy;
        HSBType hsb = HSBType.fromXY(xy[0], xy[1]);

        int brightnessInPercent = (int) Math.round(lightState.bri / BRIGHTNESS_FACTOR);
        brightnessInPercent = restrictToBounds(brightnessInPercent);

        return new HSBType(hsb.getHue(), hsb.getSaturation(), new PercentType(brightnessInPercent));
    }

    /**
     * Transforms the given {@link StringType} into a light state containing the {@link AlertMode} to be triggered.
     *
     * @param alertType {@link StringType} representing the required {@link AlertMode} . <br>
     *            Supported values are:
     *            <ul>
     *            <li>{@value #ALERT_MODE_NONE}.
     *            <li>{@value #ALERT_MODE_SELECT}.
     *            <li>{@value #ALERT_MODE_LONG_SELECT}.
     *            <ul>
     * @return light state containing the {@link AlertMode}.
     * @throws IllegalArgumentException Throws if an unsupported mode is set
     */
    public static LightStateUpdate toAlertState(LightStateUpdate LightStateUpdate, StringType alertType)
            throws IllegalArgumentException {
        LightStateUpdate.alert = AlertMode.valueOf(alertType.toString().toLowerCase());
        return LightStateUpdate;
    }

    /**
     * Transforms the given {@link OnOffType} into a light state containing the {@link Effect} value.
     * {@link OnOffType#ON} will result in {@link Effect#COLORLOOP}. {@link OnOffType#OFF} will result in
     * {@link Effect#NONE}.
     *
     * @param onOffType on or off state
     * @return light state containing the {@link Effect} value
     */
    public static LightStateUpdate toOnOffEffectState(LightStateUpdate LightStateUpdate, OnOffType onOffType) {
        if (OnOffType.ON.equals(onOffType)) {
            LightStateUpdate.effect = Effect.colorloop;
        } else {
            LightStateUpdate.effect = Effect.none;
        }

        return LightStateUpdate;
    }

    private static int restrictToBounds(int percentValue) {
        if (percentValue < 0) {
            return 0;
        } else if (percentValue > 100) {
            return 100;
        }
        return percentValue;
    }

}
