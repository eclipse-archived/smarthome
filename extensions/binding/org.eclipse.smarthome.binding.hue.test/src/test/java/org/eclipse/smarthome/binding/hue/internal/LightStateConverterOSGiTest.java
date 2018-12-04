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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.eclipse.smarthome.binding.hue.internal.State.ColorMode;
import org.eclipse.smarthome.binding.hue.internal.handler.LightStateConverter;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.Test;

/**
 *
 * @author Markus Bösling - initial contribution
 * @author Denis Dudnik - switched to internally integrated source of Jue library
 * @author Markus Rathgeb - migrated to plain Java test
 */
public class LightStateConverterOSGiTest extends JavaOSGiTest {

    @Test
    public void brightnessLightStateConverterConversionIsBijective() {
        final State lightState = new State();
        // 0 is an edge case
        for (int percent = 1; percent <= 100; ++percent) {
            StateUpdate stateUpdate = LightStateConverter.toBrightnessLightState(new PercentType(percent));
            assertThat(stateUpdate.commands.size(), is(2));
            assertThat(stateUpdate.commands.get(1).key, is("bri"));
            lightState.bri = Integer.parseInt(stateUpdate.commands.get(1).value.toString());
            assertThat(LightStateConverter.toBrightnessPercentType(lightState).intValue(), is(percent));
        }
    }

    @Test
    public void brightnessAlwaysGreaterThanZero() {
        final State lightState = new State();
        // 0 is an edge case
        for (int brightness = 1; brightness <= 254; ++brightness) {
            lightState.bri = brightness;
            PercentType percent = LightStateConverter.toBrightnessPercentType(lightState);
            assertTrue(percent.intValue() > 0);
        }
    }

    @Test
    public void colorLightStateConverterForBrightnessConversionIsBijective() {
        final State lightState = new State();
        lightState.colormode = ColorMode.CT.toString();
        // 0 is an edge case
        for (int percent = 1; percent <= 100; ++percent) {
            final HSBType hsbType = new HSBType(DecimalType.ZERO, PercentType.ZERO, new PercentType(percent));
            StateUpdate stateUpdate = LightStateConverter.toColorLightState(hsbType, lightState);
            assertThat(stateUpdate.commands.size(), is(3));
            assertThat(stateUpdate.commands.get(2).key, is("bri"));
            lightState.bri = Integer.parseInt(stateUpdate.commands.get(2).value.toString());
            assertThat(LightStateConverter.toHSBType(lightState).getBrightness().intValue(), is(percent));
        }
    }

    @Test
    public void hsbBrightnessAlwaysGreaterThanZero() {
        final State lightState = new State();
        lightState.colormode = ColorMode.CT.toString();
        // 0 is an edge case
        for (int brightness = 1; brightness <= 254; ++brightness) {
            lightState.bri = brightness;
            PercentType percent = LightStateConverter.toHSBType(lightState).getBrightness();
            assertTrue(percent.intValue() > 0);
        }
    }

    @Test
    public void colorLightStateConverterForSaturationConversionIsBijective() {
        final State lightState = new State();
        lightState.colormode = ColorMode.CT.toString();
        // 0 is an edge case
        for (int percent = 1; percent <= 100; ++percent) {
            final HSBType hsbType = new HSBType(DecimalType.ZERO, new PercentType(percent), PercentType.HUNDRED);
            StateUpdate stateUpdate = LightStateConverter.toColorLightState(hsbType, lightState);
            assertThat(stateUpdate.commands.size(), is(3));
            assertThat(stateUpdate.commands.get(1).key, is("sat"));
            lightState.sat = Integer.parseInt(stateUpdate.commands.get(1).value.toString());
            assertThat(LightStateConverter.toHSBType(lightState).getSaturation().intValue(), is(percent));
        }
    }

    @Test
    public void hsbSaturationAlwaysGreaterThanZero() {
        final State lightState = new State();
        lightState.colormode = ColorMode.CT.toString();
        // 0 is an edge case
        for (int saturation = 1; saturation <= 254; ++saturation) {
            lightState.sat = saturation;
            PercentType percent = LightStateConverter.toHSBType(lightState).getSaturation();
            assertTrue(percent.intValue() > 0);
        }
    }
}
