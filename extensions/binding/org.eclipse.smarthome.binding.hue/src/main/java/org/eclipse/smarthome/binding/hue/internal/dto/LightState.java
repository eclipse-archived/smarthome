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
package org.eclipse.smarthome.binding.hue.internal.dto;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Current state of light.
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
public class LightState {
    public boolean on;
    public int bri;
    public int hue;
    public int sat;
    public float[] xy;
    public int ct;
    public AlertMode alert;
    public Effect effect;
    public ColorMode colormode;
    public boolean reachable;

    /** Color modes of a light. */
    public static enum ColorMode {
        /** CIE color space coordinates */
        xy,
        /** Hue and saturation */
        hs,
        /** Color temperature in mirek */
        ct;
    }

    /** Alert modes of a light. */
    public static enum AlertMode {
        /** Light is not performing alert effect */
        none,
        /** Light is performing one breathe cycle for 2 seconds */
        select,
        /** Light is performing breathe cycles for 15 seconds (unless cancelled) */
        lselect
    }

    /** Effects possible for a light. */
    public static enum Effect {
        /** No effect */
        none,
        /** Cycle through all hues with current saturation and brightness */
        colorloop
    }

    /**
     * This method returns a time in <strong>milliseconds</strong> for each alarm state.
     *
     * @param alert The {@link AlertMode}
     */
    public static int getAlertDuration(@Nullable AlertMode alert) {
        if (alert == null) {
            return -1;
        }
        int delay;
        switch (alert) {
            case lselect:
                delay = 15000;
                break;
            case select:
                delay = 2000;
                break;
            case none:
            default:
                delay = -1;
                break;
        }
        return delay;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LightState)) {
            return false;
        }

        LightState other = (LightState) obj;

        return other.alert.equals(alert) && other.on == on && other.bri == bri && other.ct == ct && other.hue == hue
                && other.sat == sat && other.reachable == reachable && Objects.equals(other.colormode, colormode)
                && Objects.equals(other.effect, effect);
    }
}
