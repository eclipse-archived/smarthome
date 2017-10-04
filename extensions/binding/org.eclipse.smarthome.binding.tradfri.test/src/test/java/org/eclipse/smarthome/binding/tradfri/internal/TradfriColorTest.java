/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.tradfri.internal;

import static org.junit.Assert.*;

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.junit.Test;

/**
 * Tests for {@link TradfriColor}.
 *
 * @author Holger Reichert - Initial contribution
 */
public class TradfriColorTest {

    @Test
    public void testFromCieKnownGood1() {
        TradfriColor color = TradfriColor.fromCie(29577, 12294, 354);
        assertNotNull(color);
        assertEquals(254, (int) color.rgbR);
        assertEquals(2, (int) color.rgbG);
        assertEquals(158, (int) color.rgbB);
        assertEquals(29577, (int) color.xyX);
        assertEquals(12294, (int) color.xyY);
        assertEquals(254, (int) color.brightness);
        assertNotNull(color.hsbType);
        assertEquals(323, color.hsbType.getHue().intValue());
        assertEquals(99, color.hsbType.getSaturation().intValue());
        assertEquals(100, color.hsbType.getBrightness().intValue());
    }

    @Test
    public void testFromCieKnownGood2() {
        TradfriColor color = TradfriColor.fromCie(19983, 37417, 84);
        assertNotNull(color);
        assertEquals(30, (int) color.rgbR);
        assertEquals(86, (int) color.rgbG);
        assertEquals(7, (int) color.rgbB);
        assertEquals(19983, (int) color.xyX);
        assertEquals(37417, (int) color.xyY);
        assertEquals(84, (int) color.brightness);
        assertNotNull(color.hsbType);
        assertEquals(102, color.hsbType.getHue().intValue());
        assertEquals(89, color.hsbType.getSaturation().intValue());
        assertEquals(34, color.hsbType.getBrightness().intValue());
    }

    @Test
    public void testFromCieKnownGood3() {
        TradfriColor color = TradfriColor.fromCie(19983, 37417, 1);
        assertNotNull(color);
        assertEquals(0, (int) color.rgbR);
        assertEquals(2, (int) color.rgbG);
        assertEquals(0, (int) color.rgbB);
        assertEquals(19983, (int) color.xyX);
        assertEquals(37417, (int) color.xyY);
        assertEquals(1, (int) color.brightness);
        assertNotNull(color.hsbType);
        assertEquals(120, color.hsbType.getHue().intValue());
        assertEquals(100, color.hsbType.getSaturation().intValue());
        assertEquals(1, color.hsbType.getBrightness().intValue());
    }

    @Test
    public void testFromCieKnownGood4() {
        TradfriColor color = TradfriColor.fromCie(11469, 3277, 181);
        assertNotNull(color);
        assertEquals(12, (int) color.rgbR);
        assertEquals(0, (int) color.rgbG);
        assertEquals(183, (int) color.rgbB);
        assertEquals(11469, (int) color.xyX);
        assertEquals(3277, (int) color.xyY);
        assertEquals(181, (int) color.brightness);
        assertNotNull(color.hsbType);
        assertEquals(245, color.hsbType.getHue().intValue());
        assertEquals(100, color.hsbType.getSaturation().intValue());
        assertEquals(72, color.hsbType.getBrightness().intValue());
    }

    @Test
    public void testFromHSBTypeKnownGood1() {
        TradfriColor color = TradfriColor.fromHSBType(HSBType.RED);
        assertNotNull(color);
        assertEquals(254, (int) color.rgbR);
        assertEquals(0, (int) color.rgbG);
        assertEquals(0, (int) color.rgbB);
        assertEquals(45914, (int) color.xyX);
        assertEquals(19615, (int) color.xyY);
        assertEquals(254, (int) color.brightness);
        assertNotNull(color.hsbType);
        assertEquals(0, color.hsbType.getHue().intValue());
        assertEquals(100, color.hsbType.getSaturation().intValue());
        assertEquals(100, color.hsbType.getBrightness().intValue());
    }

    @Test
    public void testFromHSBTypeKnownGood2() {
        TradfriColor color = TradfriColor.fromHSBType(new HSBType("0,100,1"));
        assertNotNull(color);
        assertEquals(2, (int) color.rgbR);
        assertEquals(0, (int) color.rgbG);
        assertEquals(0, (int) color.rgbB);
        assertEquals(45914, (int) color.xyX);
        assertEquals(19615, (int) color.xyY);
        assertEquals(2, (int) color.brightness);
        assertNotNull(color.hsbType);
        assertEquals(0, color.hsbType.getHue().intValue());
        assertEquals(100, color.hsbType.getSaturation().intValue());
        assertEquals(1, color.hsbType.getBrightness().intValue());
    }

    @Test
    public void testConversionReverse() {
        // convert from HSBType
        TradfriColor color = TradfriColor.fromHSBType(HSBType.GREEN);
        assertNotNull(color);
        assertEquals(0, (int) color.rgbR);
        assertEquals(254, (int) color.rgbG); // 254 instead of 255 - only approximated calculation
        assertEquals(0, (int) color.rgbB);
        assertEquals(11299, (int) color.xyX);
        assertEquals(48941, (int) color.xyY);
        assertEquals(254, (int) color.brightness);
        assertNotNull(color.hsbType);
        assertEquals(120, color.hsbType.getHue().intValue());
        assertEquals(100, color.hsbType.getSaturation().intValue());
        assertEquals(100, color.hsbType.getBrightness().intValue());
        // convert the result again based on the XY values
        TradfriColor reverse = TradfriColor.fromCie(color.xyX, color.xyY, color.brightness);
        assertNotNull(reverse);
        assertEquals(0, (int) reverse.rgbR);
        assertEquals(254, (int) reverse.rgbG);
        assertEquals(0, (int) reverse.rgbB);
        assertEquals(11299, (int) reverse.xyX);
        assertEquals(48941, (int) reverse.xyY);
        assertEquals(254, (int) reverse.brightness);
        assertNotNull(reverse.hsbType);
        assertEquals(120, reverse.hsbType.getHue().intValue());
        assertEquals(100, reverse.hsbType.getSaturation().intValue());
        assertEquals(100, reverse.hsbType.getBrightness().intValue());
    }

    @Test
    public void testFromColorTemperatureMinMiddleMax() {
        // coldest color temperature -> preset 1
        TradfriColor colorMin = TradfriColor.fromColorTemperature(PercentType.ZERO);
        assertNotNull(colorMin);
        assertEquals(24933, (int) colorMin.xyX);
        assertEquals(24691, (int) colorMin.xyY);
        // middle color temperature -> preset 2
        TradfriColor colorMiddle = TradfriColor.fromColorTemperature(new PercentType(50));
        assertNotNull(colorMiddle);
        assertEquals(30138, (int) colorMiddle.xyX);
        assertEquals(26909, (int) colorMiddle.xyY);
        // warmest color temperature -> preset 3
        TradfriColor colorMax = TradfriColor.fromColorTemperature(PercentType.HUNDRED);
        assertNotNull(colorMax);
        assertEquals(33137, (int) colorMax.xyX);
        assertEquals(27211, (int) colorMax.xyY);
    }

    @Test
    public void testFromColorTemperatureInbetween() {
        // 30 percent must be between preset 1 and 2
        TradfriColor color2 = TradfriColor.fromColorTemperature(new PercentType(30));
        assertNotNull(color2);
        assertEquals(28056, (int) color2.xyX);
        assertEquals(26022, (int) color2.xyY);
        // 70 percent must be between preset 2 and 3
        TradfriColor color3 = TradfriColor.fromColorTemperature(new PercentType(70));
        assertNotNull(color3);
        assertEquals(31338, (int) color3.xyX);
        assertEquals(27030, (int) color3.xyY);
    }

    @Test
    public void testCalculateColorTemperature() {
        // preset 1 -> coldest -> 0 percent
        PercentType preset1 = TradfriColor.calculateColorTemperature(24933, 24691);
        assertEquals(0, preset1.intValue());
        // preset 2 -> middle -> 50 percent
        PercentType preset2 = TradfriColor.calculateColorTemperature(30138, 26909);
        assertEquals(50, preset2.intValue());
        // preset 3 -> warmest -> 100 percent
        PercentType preset3 = TradfriColor.calculateColorTemperature(33137, 27211);
        assertEquals(100, preset3.intValue());
        // preset 3 -> warmest -> 100 percent
        PercentType colder = TradfriColor.calculateColorTemperature(22222, 23333);
        assertEquals(0, colder.intValue());
        // preset 3 -> warmest -> 100 percent
        PercentType temp3 = TradfriColor.calculateColorTemperature(34000, 34000);
        assertEquals(100, temp3.intValue());
        // mixed case 1
        PercentType mixed1 = TradfriColor.calculateColorTemperature(0, 1000000);
        assertEquals(0, mixed1.intValue());
        // mixed case 1
        PercentType mixed2 = TradfriColor.calculateColorTemperature(1000000, 0);
        assertEquals(100, mixed2.intValue());
    }

}
