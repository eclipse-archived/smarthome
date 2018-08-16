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
package org.eclipse.smarthome.binding.onewire.internal;

import static org.junit.Assert.*;

import java.util.List;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.smarthome.core.library.dimension.Density;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Test;

/**
 * Tests cases for {@link Util}.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class UtilTest {

    @Test
    public void decimalTypeToBooleanListTest() {
        DecimalType testDecimal = new DecimalType(0b10001111);
        Boolean expectedResult[] = { true, true, true, true, false, false, false, true };
        List<Boolean> conversionResult = Util.decimalTypeToBooleanList(testDecimal);

        assertThat(conversionResult, IsIterableContainingInOrder.contains(expectedResult));
    }

    @Test
    public void convertAbsoluteHumidityTest() {
        QuantityType<Temperature> temperature = new QuantityType<>("20 °C");
        QuantityType<Dimensionless> relativeHumidity = new QuantityType<>("75%");

        QuantityType<Density> absoluteHumidity = (QuantityType<Density>) Util.calculateAbsoluteHumidity(temperature,
                relativeHumidity);
        assertEquals(12.93, absoluteHumidity.doubleValue(), 0.01);

    }

    @Test
    public void dewPointTest() {
        QuantityType<Temperature> temperature = new QuantityType<>("20 °C");
        QuantityType<Dimensionless> relativeHumidity = new QuantityType<>("75%");

        QuantityType<Temperature> dewPoint = (QuantityType<Temperature>) Util.calculateDewpoint(temperature,
                relativeHumidity);
        assertEquals(15.43, dewPoint.doubleValue(), 0.01);
    }
}
