/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;

import nl.q42.jue.State;
import nl.q42.jue.State.AlertMode;
import nl.q42.jue.State.Effect;
import nl.q42.jue.StateUpdate;

/**
 * The {@link LightStateConverter} is responsible for mapping Eclipse SmartHome
 * types to jue types and vice versa.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Oliver Libutzki - Adjustments
 * @author Kai Kreuzer - made code static
 * @author Andre Fuechsel - added method for brightness
 * @author Yordan Zhelev - added method for alert
 *
 */
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
    static final String ALERT_MODE_NONE = "NONE";
    /**
     * {@value #ALERT_MODE_SELECT}. The light is performing one breathe cycle.
     */
    static final String ALERT_MODE_SELECT = "SELECT";
    /**
     * {@value #ALERT_MODE_LONG_SELECT}. The light is performing breathe cycles
     * for 15 seconds or until an "alert": "none" command is received.
     */
    static final String ALERT_MODE_LONG_SELECT = "LSELECT";

    private static final int DIM_STEPSIZE = 30;

    /**
     * Transforms the given {@link HSBType} into a light state.
     *
     * @param hsbType
     *            HSB type
     * @return light state representing the {@link HSBType}.
     */
    public static StateUpdate toColorLightState(HSBType hsbType) {
        int hue = (int) Math.round(hsbType.getHue().doubleValue() * HUE_FACTOR);
        int saturation = (int) Math.round(hsbType.getSaturation().doubleValue() * SATURATION_FACTOR);
        int brightness = (int) Math.round(hsbType.getBrightness().doubleValue() * BRIGHTNESS_FACTOR);

        StateUpdate stateUpdate = new StateUpdate().setHue(hue).setSat(saturation);
        if (brightness > 0) {
            stateUpdate.setBrightness(brightness);
        }
        return stateUpdate;
    }

    /**
     * Transforms the given {@link OnOffType} into a light state containing the
     * 'on' value.
     *
     * @param onOffType
     *            on or off state
     * @return light state containing the 'on' value
     */
    public static StateUpdate toOnOffLightState(OnOffType onOffType) {
        StateUpdate stateUpdate = new StateUpdate().setOn(OnOffType.ON.equals(onOffType));
        return stateUpdate;
    }

    /**
     * Transforms the given {@link PercentType} into a light state containing
     * the brightness and the 'on' value represented by {@link PercentType}.
     *
     * @param percentType
     *            brightness represented as {@link PercentType}
     * @return light state containing the brightness and the 'on' value
     */
    public static StateUpdate toBrightnessLightState(PercentType percentType) {
        boolean on = percentType.equals(PercentType.ZERO) ? false : true;
        final StateUpdate stateUpdate = new StateUpdate().setOn(on);

        int brightness = (int) Math.round(percentType.floatValue() * BRIGHTNESS_FACTOR);
        if (brightness > 0) {
            stateUpdate.setBrightness(brightness);
        }
        return stateUpdate;
    }

    /**
     * Adjusts the given brightness using the {@link IncreaseDecreaseType} and
     * returns the updated value.
     *
     * @param type
     *            The {@link IncreaseDecreaseType} to be used
     * @param currentBrightness
     *            The current brightness
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
     * @param percentType
     *            color temperature represented as {@link PercentType}
     * @return light state containing the color temperature
     */
    public static StateUpdate toColorTemperatureLightState(PercentType percentType) {
        int colorTemperature = MIN_COLOR_TEMPERATURE
                + Math.round((COLOR_TEMPERATURE_RANGE * percentType.floatValue()) / 100);
        StateUpdate stateUpdate = new StateUpdate().setColorTemperature(colorTemperature);
        return stateUpdate;
    }

    /**
     * Adjusts the given color temperature using the {@link IncreaseDecreaseType} and returns the updated value.
     *
     * @param type
     *            The {@link IncreaseDecreaseType} to be used
     * @param currentColorTemp
     *            The current color temperature
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
     * Transforms {@link HueLightState} into {@link PercentType} representing
     * the color temperature.
     *
     * @param lightState
     *            light state
     * @return percent type representing the color temperature
     */
    public static PercentType toColorTemperaturePercentType(State lightState) {
        int percent = (int) Math
                .round(((lightState.getColorTemperature() - MIN_COLOR_TEMPERATURE) * 100.0) / COLOR_TEMPERATURE_RANGE);
        return new PercentType(restrictToBounds(percent));
    }

    /**
     * Transforms {@link HueLightState} into {@link PercentType} representing
     * the brightness.
     *
     * @param lightState
     *            light state
     * @return percent type representing the brightness
     */
    public static PercentType toBrightnessPercentType(State lightState) {
        int percent = (int) Math.round(lightState.getBrightness() / BRIGHTNESS_FACTOR);
        return new PercentType(restrictToBounds(percent));
    }

    /**
     * Transforms {@link State} into {@link StringType} representing the {@link AlertMode}.
     *
     * @param lightState
     *            light state.
     * @return string type representing the alert mode.
     */
    public static StringType toAlertStringType(State lightState) {
        return new StringType(lightState.getAlertMode().toString());
    }

    /**
     * Transforms {@link HueLightState} into {@link HSBType} representing the
     * color.
     *
     * @param lightState
     *            light state
     * @return HSB type representing the color
     */
    public static HSBType toHSBType(State lightState) {
        int hue = lightState.getHue();

        int saturationInPercent = (int) (lightState.getSaturation() / SATURATION_FACTOR);
        int brightnessInPercent = (int) (lightState.getBrightness() / BRIGHTNESS_FACTOR);

        saturationInPercent = restrictToBounds(saturationInPercent);
        brightnessInPercent = restrictToBounds(brightnessInPercent);

        HSBType hsbType = new HSBType(new DecimalType(hue / HUE_FACTOR), new PercentType(saturationInPercent),
                new PercentType(brightnessInPercent));

        return hsbType;
    }

    /**
     * Transforms the given {@link StringType} into a light state containing the {@link AlertMode} to be triggered.
     *
     * @param alertType
     *            {@link StringType} representing the required {@link AlertMode} . <br>
     *            Supported values are:
     *            <ul>
     *            <li>{@value #ALERT_MODE_NONE}.
     *            <li>{@value #ALERT_MODE_SELECT}.
     *            <li>{@value #ALERT_MODE_LONG_SELECT}.
     *            <ul>
     * @return light state containing the {@link AlertMode} or <b><code>null </code></b> if the provided
     *         {@link StringType} represents unsupported mode.
     */
    public static StateUpdate toAlertState(StringType alertType) {
        AlertMode alertMode = null;

        switch (alertType.toString()) {
            case ALERT_MODE_NONE:
                alertMode = State.AlertMode.NONE;
                break;
            case ALERT_MODE_SELECT:
                alertMode = State.AlertMode.SELECT;
                break;
            case ALERT_MODE_LONG_SELECT:
                alertMode = State.AlertMode.LSELECT;
                break;
            default:
                return null;
        }
        return new StateUpdate().setAlert(alertMode);
    }

    /**
     * Transforms the given {@link OnOffType} into a light state containing the {@link Effect} value.
     * {@link OnOffType#ON} will result in {@link Effect#COLORLOOP}. {@link OnOffType#OFF} will result in
     * {@link Effect#NONE}.
     *
     * @param onOffType
     *            on or off state
     * @return light state containing the {@link Effect} value
     */
    public static StateUpdate toOnOffEffectState(OnOffType onOffType) {
        StateUpdate stateUpdate;

        if (OnOffType.ON.equals(onOffType)) {
            stateUpdate = new StateUpdate().setEffect(Effect.COLORLOOP);
        } else {
            stateUpdate = new StateUpdate().setEffect(Effect.NONE);
        }

        return stateUpdate;
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
