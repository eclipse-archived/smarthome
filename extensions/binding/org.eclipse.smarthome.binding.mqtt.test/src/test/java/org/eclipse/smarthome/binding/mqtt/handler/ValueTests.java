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
package org.eclipse.smarthome.binding.mqtt.handler;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;

import org.eclipse.smarthome.binding.mqtt.internal.NumberValue;
import org.eclipse.smarthome.binding.mqtt.internal.OnOffValue;
import org.eclipse.smarthome.binding.mqtt.internal.PercentValue;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.junit.Test;

/**
 * Tests cases for the value classes. They should throw exceptions if the wrong command type is used
 * for an update. The percent value class should raise an exception if the value is out of range.
 *
 * The on/off value class should be able to interpret a multitude of values including the custom defined ones.
 *
 * The string value class is not tested here, because it accepts everything and will never throw.
 *
 * @author David Graeff - Initial contribution
 */
public class ValueTests {

    @Test(expected = IllegalArgumentException.class)
    public void illegalNumberCommand() {
        NumberValue v = new NumberValue(null, null);
        v.update(OnOffType.OFF);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalPercentCommand() {
        PercentValue v = new PercentValue(null, null, null, null);
        v.update(OnOffType.OFF);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalOnOffCommand() {
        OnOffValue v = new OnOffValue(null, null, null);
        v.update(new DecimalType(101.0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalPercentUpdate() {
        PercentValue v = new PercentValue(null, null, null, null);
        v.update(new DecimalType(101.0));
    }

    @Test
    public void onoffUpdate() {
        OnOffValue v = new OnOffValue("fancyON", "fancyOff", false);
        assertThat(v.update(OnOffType.OFF), is("fancyOff"));
        assertThat(v.update(OnOffType.ON), is("fancyON"));

        assertThat(v.update(new StringType("OFF")), is("fancyOff"));
        assertThat(v.update(new StringType("ON")), is("fancyON"));

        assertThat(v.update(new StringType("0")), is("fancyOff"));
        assertThat(v.update(new StringType("1")), is("fancyON"));

        assertThat(v.update(new StringType("fancyOff")), is("fancyOff"));
        assertThat(v.update(new StringType("fancyON")), is("fancyON"));

        v.setInverse(true);
        assertThat(v.update(new StringType("1")), is("fancyOff"));
        assertThat(v.update(new StringType("0")), is("fancyON"));
    }

    @Test
    public void percentCalc() {
        PercentValue v = new PercentValue(true, new BigDecimal(10.0), new BigDecimal(110.0), new BigDecimal(1.0));
        v.update(new DecimalType(110.0));
        assertThat((PercentType) v.getValue(), is(new PercentType(100)));
        v.update(new DecimalType(10.0));
        assertThat((PercentType) v.getValue(), is(new PercentType(0)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void percentCalcInvalid() {
        PercentValue v = new PercentValue(true, new BigDecimal(10.0), new BigDecimal(110.0), new BigDecimal(1.0));
        v.update(new DecimalType(9.0));
    }
}
