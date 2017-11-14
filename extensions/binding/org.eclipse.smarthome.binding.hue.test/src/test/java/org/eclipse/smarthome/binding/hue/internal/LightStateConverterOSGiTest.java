/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.eclipse.smarthome.binding.hue.handler.LightStateConverter;
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
    public void lightStateConverterConversionIsBijective() {
        int PERCENT_VALUE_67 = 67;
        StateUpdate stateUpdate = LightStateConverter.toBrightnessLightState(new PercentType(PERCENT_VALUE_67));
        assertThat(stateUpdate.commands.size(), is(2));
        assertThat(stateUpdate.commands.get(1).key, is("bri"));
        State lightState = new State();
        lightState.bri = Integer.parseInt(stateUpdate.commands.get(1).value.toString());
        assertThat(LightStateConverter.toBrightnessPercentType(lightState).intValue(), is(PERCENT_VALUE_67));
    }
}
