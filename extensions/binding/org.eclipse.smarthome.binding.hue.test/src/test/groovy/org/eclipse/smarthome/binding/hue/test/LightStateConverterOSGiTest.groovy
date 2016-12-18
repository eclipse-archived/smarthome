/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.test;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.binding.hue.handler.LightStateConverter
import org.eclipse.smarthome.core.library.types.PercentType
import org.junit.Test

import nl.q42.jue.State
import nl.q42.jue.StateUpdate

class LightStateConverterOSGiTest extends AbstractHueOSGiTest {

    @Test
    void 'assert that LightStateConverter conversion is bijective'() {
        int PERCENT_VALUE_67 = 67;
        StateUpdate stateUpdate = LightStateConverter.toBrightnessLightState(new PercentType(PERCENT_VALUE_67));
        assertThat(stateUpdate.commands.size, is(2))
        assertThat(stateUpdate.commands[1].key, is("bri"))
        State lightState = new State()
        lightState.bri = stateUpdate.commands[1].value
        assertThat(LightStateConverter.toBrightnessPercentType(lightState).intValue(), is(PERCENT_VALUE_67))
    }
}
