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
package org.eclipse.smarthome.binding.mqtt.generic.internal.values;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.junit.Test;

/**
 * Test cases for the value classes. They should throw exceptions if the wrong command type is used
 * for an update. The percent value class should raise an exception if the value is out of range.
 *
 * The on/off value class should accept a multitude of values including the custom defined ones.
 *
 * The string value class states are tested.
 *
 * @author David Graeff - Initial contribution
 */
public class ValueTests {
    @Test(expected = IllegalArgumentException.class)
    public void illegalTextStateUpdate() {
        TextValue v = new TextValue("one,two".split(","));
        v.update("three");
    }

    public void textStateUpdate() {
        TextValue v = new TextValue("one,two".split(","));
        v.update("one");
    }

    public void colorUpdate() {
        ColorValue v = new ColorValue(true, null, null);
        v.update("255, 255, 255");

        assertThat(((HSBType) v.update("OFF")).getBrightness().intValue(), is(0));
        // Minimum brightness setting after brightness 0 is 10 for ON command
        assertThat(((HSBType) v.update("ON")).getBrightness().intValue(), is(10));

        assertThat(((HSBType) v.update("0")).getBrightness().intValue(), is(0));
        // Minimum brightness setting after brightness 0 is 10 for ON command
        assertThat(((HSBType) v.update("1")).getBrightness().intValue(), is(10));
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalColorUpdate() {
        ColorValue v = new ColorValue(true, null, null);
        v.update("255,255,abc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalNumberCommand() {
        NumberValue v = new NumberValue(null, null, null, null, false);
        v.update(OnOffType.OFF);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalPercentCommand() {
        NumberValue v = new NumberValue(null, null, null, null, false);
        v.update(OnOffType.OFF);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalOnOffCommand() {
        OnOffValue v = new OnOffValue(null, null);
        v.update(new DecimalType(101.0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalPercentUpdate() {
        NumberValue v = new NumberValue(null, null, null, null, true);
        v.update(new DecimalType(101.0));
    }

    @Test
    public void onoffUpdate() {
        OnOffValue v = new OnOffValue("fancyON", "fancyOff");
        // Test with command
        assertThat(v.update(OnOffType.OFF), is("fancyOff"));
        assertThat(v.getValue(), is(OnOffType.OFF));
        assertThat(v.update(OnOffType.ON), is("fancyON"));
        assertThat(v.getValue(), is(OnOffType.ON));

        // Test with string, representing the command
        assertThat(v.update(new StringType("OFF")), is("fancyOff"));
        assertThat(v.getValue(), is(OnOffType.OFF));
        assertThat(v.update(new StringType("ON")), is("fancyON"));
        assertThat(v.getValue(), is(OnOffType.ON));

        // Test with custom string, setup in the constructor
        assertThat(v.update(new StringType("fancyOff")), is("fancyOff"));
        assertThat(v.getValue(), is(OnOffType.OFF));
        assertThat(v.update(new StringType("fancyON")), is("fancyON"));
        assertThat(v.getValue(), is(OnOffType.ON));
    }

    @Test
    public void openCloseUpdate() {
        OpenCloseValue v = new OpenCloseValue("fancyON", "fancyOff");
        // Test with command
        assertThat(v.update(OpenClosedType.CLOSED), is("fancyOff"));
        assertThat(v.getValue(), is(OpenClosedType.CLOSED));
        assertThat(v.update(OpenClosedType.OPEN), is("fancyON"));
        assertThat(v.getValue(), is(OpenClosedType.OPEN));

        // Test with string, representing the command
        assertThat(v.update(new StringType("CLOSED")), is("fancyOff"));
        assertThat(v.getValue(), is(OpenClosedType.CLOSED));
        assertThat(v.update(new StringType("OPEN")), is("fancyON"));
        assertThat(v.getValue(), is(OpenClosedType.OPEN));

        // Test with custom string, setup in the constructor
        assertThat(v.update(new StringType("fancyOff")), is("fancyOff"));
        assertThat(v.getValue(), is(OpenClosedType.CLOSED));
        assertThat(v.update(new StringType("fancyON")), is("fancyON"));
        assertThat(v.getValue(), is(OpenClosedType.OPEN));
    }

    @Test
    public void percentCalc() {
        NumberValue v = new NumberValue(true, new BigDecimal(10.0), new BigDecimal(110.0), new BigDecimal(1.0), true);
        v.update(new DecimalType(110.0));
        assertThat((PercentType) v.getValue(), is(new PercentType(100)));
        v.update(new DecimalType(10.0));
        assertThat((PercentType) v.getValue(), is(new PercentType(0)));
    }

    @Test
    public void decimalCalc() {
        NumberValue v = new NumberValue(true, new BigDecimal(0.1), new BigDecimal(1.0), new BigDecimal(0.1), true);
        v.update(new DecimalType(1.0));
        assertThat((PercentType) v.getValue(), is(new PercentType(100)));
        v.update(new DecimalType(0.1));
        assertThat((PercentType) v.getValue(), is(new PercentType(0)));
        v.update(new DecimalType(0.2));
        assertEquals(((PercentType) v.getValue()).floatValue(), 11.11f, 0.01f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void percentCalcInvalid() {
        NumberValue v = new NumberValue(true, new BigDecimal(10.0), new BigDecimal(110.0), new BigDecimal(1.0), true);
        v.update(new DecimalType(9.0));
    }
}
