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
package org.eclipse.smarthome.binding.lifx.internal.protocol;

/**
 * Enumerates all LIFX LAN protocol waveforms.
 *
 * The protocol supports changing the color of a light over time in accordance with the shape of a waveform. This allows
 * for combining functions such as fading, pulsing, etc by applying waveform interpolation on the modulation
 * between two colors.
 *
 * @author Wouter Born - Add light effects
 */
public enum Waveform {

    /**
     * Light interpolates linearly from current color to color. Duration of each cycle lasts for {@code period}
     * milliseconds.
     */
    SAW(0x00),

    /**
     * Color will cycle smoothly from current color to {@code color} and then end back at current color.
     * The duration of one cycle will last for {@code period} milliseconds.
     */
    SINE(0x01),

    /**
     * Light interpolates smoothly from current color to {@code color}. Duration of each cycle lasts for {@code period}
     * milliseconds.
     */
    HALF_SINE(0x02),

    /**
     * Light interpolates linearly from current color to {@code color}, then back to current color. Duration of each
     * cycle lasts for {@code period} milliseconds.
     */
    TRIANGLE(0x03),

    /**
     * Color will be set immediately to {@code color}, then to current color after the duty cycle fraction expires.
     * The duty cycle percentage is calculated by applying the {@code skewRatio} as a percentage of the {@code cycle}
     * duration.
     *
     * Examples:
     * <ul>
     * <li>With {@code skewRatio=0.5}, color will be set to color for the first 50% of the cycle period, then to current
     * color until the end of the cycle.
     * <li>With {@code skewRatio=0.25}, color will be set to color for the first 25% of the cycle period, then to
     * current color until the end of the cycle.
     * </ul>
     */
    PULSE(0x04);

    private final int value;

    private Waveform(int value) {
        this.value = value;
    }

    /**
     * Gets the integer value of this waveform.
     *
     * @return the integer value
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the {@link Waveform} for the given integer value.
     *
     * @param value the integer value
     * @return the {@link Waveform} or <code>null</code>, if no {@link Waveform} exists for the
     *         given value
     */
    public static Waveform fromValue(int value) {
        for (Waveform wf : values()) {
            if (wf.getValue() == value) {
                return wf;
            }
        }

        return null;
    }

}
