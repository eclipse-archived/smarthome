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
import org.eclipse.smarthome.binding.onewire.internal.device.DS2438;
import org.eclipse.smarthome.binding.onewire.internal.device.EDS006x;
import org.eclipse.smarthome.binding.onewire.internal.device.OwSensorType;
import org.eclipse.smarthome.binding.onewire.test.AbstractDeviceTest;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests cases for {@link DS2438}.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class EDS006xTest extends AbstractDeviceTest {

    @Before
    public void setupMocks() {
        setupMocks(THING_TYPE_EDS_ENV);
        deviceTestClazz = EDS006x.class;

        addChannel(CHANNEL_TEMPERATURE, "Number:Temperature");
        addChannel(CHANNEL_HUMIDITY, "Number:Dimensionless");
        addChannel(CHANNEL_ABSOLUTE_HUMIDITY, "Number:Density");
        addChannel(CHANNEL_DEWPOINT, "Number:Temperature");
        addChannel(CHANNEL_LIGHT, "Number:Illuminance");
        addChannel(CHANNEL_PRESSURE, "Number:Pressure");
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
    public void temperatureChannel() {
        instantiateDevice();

        try {
            Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(OnOffType.ON);
            Mockito.when(mockBridgeHandler.readDecimalType(eq(testSensorId), any())).thenReturn(new DecimalType(10.0));

            testDevice.enableChannel(CHANNEL_TEMPERATURE);
            ((EDS006x) testDevice).configureChannels(OwSensorType.EDS0068);
            testDevice.refresh(mockBridgeHandler, true);

            inOrder.verify(mockBridgeHandler).readDecimalType(eq(testSensorId), any());
            inOrder.verify(mockThingHandler).postUpdate(eq(CHANNEL_TEMPERATURE), eq(new QuantityType<>("10.0 °C")));

            inOrder.verifyNoMoreInteractions();
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }

    @Test
    public void humidityChannel() {
        instantiateDevice();

        try {
            Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(OnOffType.ON);
            Mockito.when(mockBridgeHandler.readDecimalType(eq(testSensorId), any())).thenReturn(new DecimalType(10.0));

            testDevice.enableChannel(CHANNEL_HUMIDITY);
            testDevice.enableChannel(CHANNEL_ABSOLUTE_HUMIDITY);
            testDevice.enableChannel(CHANNEL_DEWPOINT);
            ((EDS006x) testDevice).configureChannels(OwSensorType.EDS0068);
            testDevice.refresh(mockBridgeHandler, true);

            inOrder.verify(mockBridgeHandler, times(2)).readDecimalType(eq(testSensorId), any());
            inOrder.verify(mockThingHandler).postUpdate(eq(CHANNEL_HUMIDITY), eq(new QuantityType<>("10.0 %")));
            inOrder.verify(mockThingHandler).postUpdate(eq(CHANNEL_ABSOLUTE_HUMIDITY),
                    eq(new QuantityType<>("0.9381970824113001000 g/m³")));
            inOrder.verify(mockThingHandler).postUpdate(eq(CHANNEL_DEWPOINT),
                    eq(new QuantityType<>("-20.31395053870025 °C")));

            inOrder.verifyNoMoreInteractions();
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }

    @Test
    public void pressureChannel() {
        instantiateDevice();

        try {
            Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(OnOffType.ON);
            Mockito.when(mockBridgeHandler.readDecimalType(eq(testSensorId), any())).thenReturn(new DecimalType(2.0));

            testDevice.enableChannel(CHANNEL_PRESSURE);
            ((EDS006x) testDevice).configureChannels(OwSensorType.EDS0068);
            testDevice.refresh(mockBridgeHandler, true);

            inOrder.verify(mockBridgeHandler).readDecimalType(eq(testSensorId), any());
            inOrder.verify(mockThingHandler).postUpdate(eq(CHANNEL_PRESSURE), eq(new QuantityType<>("2.0 mbar")));

            inOrder.verifyNoMoreInteractions();
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }

    @Test
    public void lightChannel() {
        instantiateDevice();

        try {
            Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(OnOffType.ON);
            Mockito.when(mockBridgeHandler.readDecimalType(eq(testSensorId), any())).thenReturn(new DecimalType(100));

            testDevice.enableChannel(CHANNEL_LIGHT);
            ((EDS006x) testDevice).configureChannels(OwSensorType.EDS0068);
            testDevice.refresh(mockBridgeHandler, true);

            inOrder.verify(mockBridgeHandler).readDecimalType(eq(testSensorId), any());
            inOrder.verify(mockThingHandler).postUpdate(eq(CHANNEL_LIGHT), eq(new QuantityType<>("100 lx")));

            inOrder.verifyNoMoreInteractions();
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }

    @Test
    public void noChannel() {
        instantiateDevice();

        try {
            Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(OnOffType.ON);
            Mockito.when(mockBridgeHandler.readDecimalType(eq(testSensorId), any())).thenReturn(new DecimalType(2.0));

            ((EDS006x) testDevice).configureChannels(OwSensorType.EDS0068);
            testDevice.refresh(mockBridgeHandler, true);

            inOrder.verifyNoMoreInteractions();
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }
}
