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
package org.eclipse.smarthome.binding.onewire.device;

import static org.eclipse.smarthome.binding.onewire.internal.OwBindingConstants.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;

import org.eclipse.smarthome.binding.onewire.internal.OwException;
import org.eclipse.smarthome.binding.onewire.internal.device.DS2406_DS2413;
import org.eclipse.smarthome.binding.onewire.test.AbstractDeviceTest;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests cases for {@link DS2406_DS1413}.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class DS2406_DS2413Test extends AbstractDeviceTest {

    @Before
    public void setupMocks() {
        setupMocks(THING_TYPE_DIGITALIO2);
        deviceTestClazz = DS2406_DS2413.class;

        for (int i = 0; i < 2; i++) {
            addChannel(channelName(i), "Switch");
        }
    }

    @Test
    public void presenceTestOn() {
        presenceTest(OnOffType.ON);
    }

    @Test
    public void presenceTestOff() {
        presenceTest(OnOffType.OFF);
    }

    @Test
    public void digitalChannel() {
        for (int i = 0; i < 2; i++) {
            digitalChannelTest(OnOffType.ON, i);
            digitalChannelTest(OnOffType.OFF, i);
        }
    }

    private void digitalChannelTest(OnOffType state, int channelNo) {
        instantiateDevice();

        DecimalType returnValue = new DecimalType((state == OnOffType.ON) ? 255 : 0);

        try {
            Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(OnOffType.ON);
            Mockito.when(mockBridgeHandler.readDecimalType(eq(testSensorId), any())).thenReturn(returnValue);

            testDevice.configureChannels();
            testDevice.refresh(mockBridgeHandler, true);

            inOrder.verify(mockBridgeHandler, times(2)).readDecimalType(eq(testSensorId), any());
            inOrder.verify(mockThingHandler).postUpdate(eq(channelName(channelNo)), eq(state));
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }

    private String channelName(int channelNo) {
        return CHANNEL_DIGITAL + String.valueOf(channelNo);
    }
}
