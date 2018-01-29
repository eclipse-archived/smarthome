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
