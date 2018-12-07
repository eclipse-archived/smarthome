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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.eclipse.smarthome.binding.hue.internal.dto.LightState;
import org.eclipse.smarthome.binding.hue.internal.dto.updates.LightStateUpdate;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.junit.Test;

/**
 *
 * @author Markus Bösling - initial contribution
 * @author Denis Dudnik - switched to internally integrated source of Jue library
 * @author Markus Rathgeb - migrated to plain Java test
 */
public class LightStateConverterTest {
    @SuppressWarnings("null")
    @Test
    public void lightStateConverterConversionIsBijective() {
        int PERCENT_VALUE_67 = 67;
        LightStateUpdate stateUpdate = new LightStateUpdate();
        stateUpdate = LightStateConverter.toBrightnessLightState(stateUpdate, new PercentType(PERCENT_VALUE_67));
        assertThat(stateUpdate.on, notNullValue());
        assertThat(stateUpdate.bri, notNullValue());

        LightState lightState = new LightState();
        lightState.bri = stateUpdate.bri;
        assertThat(LightStateConverter.toBrightnessPercentType(lightState).intValue(), is(PERCENT_VALUE_67));
    }
}
