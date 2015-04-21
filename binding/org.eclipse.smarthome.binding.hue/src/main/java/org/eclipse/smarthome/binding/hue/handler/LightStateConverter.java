/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.handler;

import nl.q42.jue.State;
import nl.q42.jue.StateUpdate;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;

/**
 * The {@link LightStateConverter} is responsible for mapping Eclipse SmartHome types to jue types and vice versa.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Oliver Libutzki - Adjustments
 * @author Kai Kreuzer - made code static
 * @author Andre Fuechsel - added method for brightness
 *
 */
public class LightStateConverter {

    private static final double COLOR_TEMPERATURE_FACTOR = 3.46;
    private static final int COLOR_TEMPERATURE_OFFSET = 154;
    private static final int hue_FACTOR = 182;
    private static final double SATURATION_AND_BRIGHTNESS_FACTOR = 2.54;

    /**
     * Transforms the given {@link HSBType} into a light state.
     *
     * @param hsbType
     *            HSB type
     * @return light state representing the {@link HSBType}.
     */
    public static StateUpdate toColorLightState(HSBType hsbType) {
        int hue = new Long(Math.round(hsbType.getHue().doubleValue() * hue_FACTOR)).intValue();
        int saturation = new Long(Math.round(hsbType.getSaturation().doubleValue() * SATURATION_AND_BRIGHTNESS_FACTOR))
                .intValue();
        int brightness = new Long(Math.round(hsbType.getBrightness().doubleValue() * SATURATION_AND_BRIGHTNESS_FACTOR))
                .intValue();
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
    public static StateUpdate toColorLightState(OnOffType onOffType) {
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
    public static StateUpdate toColorLightState(PercentType percentType) {
        boolean on = percentType.equals(PercentType.ZERO) ? false : true;
        final StateUpdate stateUpdate = new StateUpdate().setOn(on);

        int brightness = new Long(Math.round(percentType.doubleValue() * SATURATION_AND_BRIGHTNESS_FACTOR)).intValue();
        if (brightness > 0) {
            stateUpdate.setBrightness(brightness);
        }
        return stateUpdate;
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
        int colorTemperature = COLOR_TEMPERATURE_OFFSET + (int) (COLOR_TEMPERATURE_FACTOR * percentType.intValue());
        StateUpdate stateUpdate = new StateUpdate().setColorTemperature(colorTemperature);
        return stateUpdate;
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
        int percent = (int) ((lightState.getColorTemperature() - COLOR_TEMPERATURE_OFFSET) / COLOR_TEMPERATURE_FACTOR);
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
        int percent = (int) (lightState.getBrightness() / SATURATION_AND_BRIGHTNESS_FACTOR);
        return new PercentType(restrictToBounds(percent));
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
        int saturation = lightState.getSaturation();
        int brightness = lightState.getBrightness();

        int saturationInPercent = (int) (saturation / SATURATION_AND_BRIGHTNESS_FACTOR);
        int brightnessInPercent = (int) (brightness / SATURATION_AND_BRIGHTNESS_FACTOR);

        saturationInPercent = restrictToBounds(saturationInPercent);
        brightnessInPercent = restrictToBounds(brightnessInPercent);

        HSBType hsbType = new HSBType(new DecimalType(hue / hue_FACTOR), new PercentType(saturationInPercent),
                new PercentType(brightnessInPercent));

        return hsbType;
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