/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.PercentType;

/**
 * Utility class for sharing utility methods between objects.
 *
 * @author Wouter Born - Extracted methods from LifxLightHandler
 */
public final class LifxUtils {

    private static final BigDecimal INCREASE_DECREASE_STEP = new BigDecimal(10);

    private static final BigDecimal ZERO = PercentType.ZERO.toBigDecimal();
    private static final BigDecimal HUNDRED = PercentType.HUNDRED.toBigDecimal();

    private LifxUtils() {
        // hidden utility class constructor
    }

    public static PercentType increaseDecreasePercentType(IncreaseDecreaseType increaseDecreaseType, PercentType old) {
        BigDecimal delta = ZERO;
        if (increaseDecreaseType == IncreaseDecreaseType.INCREASE) {
            delta = INCREASE_DECREASE_STEP;
        } else if (increaseDecreaseType == IncreaseDecreaseType.DECREASE) {
            delta = INCREASE_DECREASE_STEP.negate();
        }

        if (delta != ZERO) {
            BigDecimal newValue = old.toBigDecimal().add(delta);
            newValue = newValue.setScale(0, RoundingMode.HALF_UP);
            newValue = newValue.min(HUNDRED);
            newValue = newValue.max(ZERO);
            return new PercentType(newValue);
        } else {
            return old;
        }
    }

    public static DecimalType hueToDecimalType(int hue) {
        return new DecimalType(hue * 360 / 65535.0f);
    }

    public static int decimalTypeToHue(DecimalType hue) {
        return (int) (hue.floatValue() / 360 * 65535.0f);
    }

    public static PercentType saturationToPercentType(int saturation) {
        return new PercentType(Math.round((saturation / 65535.0f) * 100));
    }

    public static int percentTypeToSaturation(PercentType saturation) {
        return (int) (saturation.floatValue() / 100 * 65535.0f);
    }

    public static PercentType brightnessToPercentType(int brightness) {
        return new PercentType(Math.round((brightness / 65535.0f) * 100));
    }

    public static int percentTypeToBrightness(PercentType brightness) {
        return (int) (brightness.floatValue() / 100 * 65535.0f);
    }

    public static PercentType kelvinToPercentType(int kelvin) {
        // range is from 2500-9000K
        return new PercentType((kelvin - 9000) / (-65));
    }

    public static int percentTypeToKelvin(PercentType temperature) {
        // range is from 2500-9000K
        return 9000 - (temperature.intValue() * 65);
    }
}
