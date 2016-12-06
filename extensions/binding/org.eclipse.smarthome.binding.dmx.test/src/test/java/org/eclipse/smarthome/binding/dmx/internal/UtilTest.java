/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.dmx.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.eclipse.smarthome.binding.dmx.internal.Util;
import org.eclipse.smarthome.binding.dmx.internal.multiverse.Channel;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.junit.Test;

/**
 * Tests cases Util
 *
 * @author Jan N. Klug - Initial contribution
 */
public class UtilTest {

    @Test
    public void coercingOfDmxValues() {
        // overrange
        int value = Util.toDmxValue(300);
        assertThat(value, is(Channel.MAX_VALUE));

        // underrange
        value = Util.toDmxValue(-1);
        assertThat(value, is(Channel.MIN_VALUE));

        // inrange
        value = Util.toDmxValue(100);
        assertThat(value, is(100));
    }

    @Test
    public void conversionString() {
        int value = Util.toDmxValue("100");
        assertThat(value, is(100));
    }

    @Test
    public void conversionFromPercentType() {
        // borders
        int value = Util.toDmxValue(new PercentType(100));
        assertThat(value, is(255));

        value = Util.toDmxValue(new PercentType(0));
        assertThat(value, is(0));

        // middle
        value = Util.toDmxValue(new PercentType(50));
        assertThat(value, is(127));
    }

    @Test
    public void conversionToPercentType() {
        // borders
        PercentType value = Util.toPercentValue(255);
        assertThat(value.intValue(), is(100));

        value = Util.toPercentValue(0);
        assertThat(value.intValue(), is(0));

        // middle
        value = Util.toPercentValue(127);
        assertThat(value.intValue(), is(49));
    }

    @Test
    public void fadeTimeFraction() {
        // target already reached
        int value = Util.fadeTimeFraction(123, 123, 1000);
        assertThat(value, is(0));

        // full fade
        value = Util.fadeTimeFraction(0, 255, 1000);
        assertThat(value, is(1000));

        // fraction
        value = Util.fadeTimeFraction(100, 155, 2550);
        assertThat(value, is(550));
    }

}
