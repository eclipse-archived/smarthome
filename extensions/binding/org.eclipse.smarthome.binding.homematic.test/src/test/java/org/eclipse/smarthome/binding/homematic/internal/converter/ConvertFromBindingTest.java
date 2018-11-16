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
package org.eclipse.smarthome.binding.homematic.internal.converter;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.eclipse.smarthome.binding.homematic.internal.converter.type.AbstractTypeConverter;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDatapoint;
import org.eclipse.smarthome.binding.homematic.internal.model.HmParamsetType;
import org.eclipse.smarthome.binding.homematic.internal.model.HmValueType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.State;
import org.junit.Test;

import tec.uom.se.quantity.QuantityDimension;

/**
 * Tests for {@link AbstractTypeConverter#convertFromBinding(HmDatapoint)}.
 *
 * @author Michael Reitler - Initial Contribution
 *
 */
public class ConvertFromBindingTest {

    private final HmDatapoint floatDp = new HmDatapoint("floatDp", "", HmValueType.FLOAT, null, false,
            HmParamsetType.VALUES);
    private final HmDatapoint integerDp = new HmDatapoint("integerDp", "", HmValueType.INTEGER, null, false,
            HmParamsetType.VALUES);
    private final HmDatapoint floatQuantityDp = new HmDatapoint("floatQuantityDp", "", HmValueType.FLOAT, null, false,
            HmParamsetType.VALUES);
    private final HmDatapoint integerQuantityDp = new HmDatapoint("floatIntegerDp", "", HmValueType.INTEGER, null,
            false, HmParamsetType.VALUES);

    @Test
    public void testDecimalTypeConverter() throws ConverterException {
        State convertedState;
        TypeConverter<?> decimalConverter = ConverterFactory.createConverter("Number");

        floatDp.setValue(99.9);
        convertedState = decimalConverter.convertFromBinding(floatDp);
        assertThat(convertedState, instanceOf(DecimalType.class));
        assertThat(((DecimalType) convertedState).doubleValue(), is(99.9));

        floatDp.setValue(77.77777778);
        convertedState = decimalConverter.convertFromBinding(floatDp);
        assertThat(convertedState, instanceOf(DecimalType.class));
        assertThat(((DecimalType) convertedState).doubleValue(), is(77.777778));

        integerDp.setValue(99.0);
        convertedState = decimalConverter.convertFromBinding(integerDp);
        assertThat(convertedState, instanceOf(DecimalType.class));
        assertThat(((DecimalType) convertedState).doubleValue(), is(99.0));

        integerDp.setValue(99.9);
        convertedState = decimalConverter.convertFromBinding(integerDp);
        assertThat(convertedState, instanceOf(DecimalType.class));
        assertThat(((DecimalType) convertedState).doubleValue(), is(99.0));
    }

    @SuppressWarnings("null")
    @Test
    public void testQuantityTypeConverter() throws ConverterException {
        State convertedState;
        TypeConverter<?> temperatureConverter = ConverterFactory.createConverter("Number:Temperature");
        TypeConverter<?> frequencyConverter = ConverterFactory.createConverter("Number:Frequency");
        TypeConverter<?> timeConverter = ConverterFactory.createConverter("Number:Time");

        floatQuantityDp.setValue(10.5);
        floatQuantityDp.setUnit("°C");
        convertedState = temperatureConverter.convertFromBinding(floatQuantityDp);
        assertThat(convertedState, instanceOf(QuantityType.class));
        assertThat(((QuantityType<?>) convertedState).getDimension(), is(QuantityDimension.TEMPERATURE));
        assertThat(((QuantityType<?>) convertedState).doubleValue(), is(10.5));
        assertThat(((QuantityType<?>) convertedState).toUnit(ImperialUnits.FAHRENHEIT).doubleValue(), is(50.9));

        integerQuantityDp.setValue(50000);
        integerQuantityDp.setUnit("mHz");
        convertedState = frequencyConverter.convertFromBinding(integerQuantityDp);
        assertThat(convertedState, instanceOf(QuantityType.class));
        assertThat(((QuantityType<?>) convertedState).getDimension(),
                is(QuantityDimension.NONE.divide(QuantityDimension.TIME)));
        assertThat(((QuantityType<?>) convertedState).intValue(), is(50000));
        assertThat(((QuantityType<?>) convertedState).toUnit(SIUnits.HERTZ).intValue(), is(50));

        floatQuantityDp.setValue(12);
        floatQuantityDp.setUnit("month");
        convertedState = timeConverter.convertFromBinding(floatQuantityDp);
        assertThat(convertedState, instanceOf(QuantityType.class));
        assertThat(((QuantityType<?>) convertedState).getDimension(), is(QuantityDimension.TIME));
        assertThat(((QuantityType<?>) convertedState).doubleValue(), is(12.0));
        assertThat(((QuantityType<?>) convertedState).toUnit(SmartHomeUnits.YEAR).doubleValue(), is(1.0));

    }

}
