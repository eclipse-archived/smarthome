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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Illuminance;
import javax.measure.quantity.Speed;
import javax.measure.quantity.Temperature;

import org.eclipse.smarthome.binding.homematic.internal.converter.type.AbstractTypeConverter;
import org.eclipse.smarthome.binding.homematic.internal.converter.type.DecimalTypeConverter;
import org.eclipse.smarthome.binding.homematic.internal.converter.type.QuantityTypeConverter;
import org.eclipse.smarthome.binding.homematic.internal.model.HmChannel;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDatapoint;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDevice;
import org.eclipse.smarthome.binding.homematic.internal.model.HmInterface;
import org.eclipse.smarthome.binding.homematic.internal.model.HmParamsetType;
import org.eclipse.smarthome.binding.homematic.internal.model.HmValueType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link AbstractTypeConverter#convertToBinding(org.eclipse.smarthome.core.types.Type, HmDatapoint)}.
 *
 * @author Michael Reitler - Initial Contribution
 *
 */
public class ConvertToBindingTest {

    private final HmDatapoint floatDp = new HmDatapoint("floatDp", "", HmValueType.FLOAT, null, false,
            HmParamsetType.VALUES);
    private final HmDatapoint integerDp = new HmDatapoint("integerDp", "", HmValueType.INTEGER, null, false,
            HmParamsetType.VALUES);
    private final HmDatapoint floatQuantityDp = new HmDatapoint("floatQuantityDp", "", HmValueType.FLOAT, null, false,
            HmParamsetType.VALUES);
    private final HmDatapoint integerQuantityDp = new HmDatapoint("floatIntegerDp", "", HmValueType.INTEGER, null,
            false, HmParamsetType.VALUES);

    @Before
    public void setup() {
        HmChannel stubChannel = new HmChannel("stubChannel", 0);
        stubChannel.setDevice(new HmDevice("LEQ123456", HmInterface.RF, "HM-STUB-DEVICE", "", "", ""));
        floatDp.setChannel(stubChannel);
    }

    @Test
    public void testDecimalTypeConverter() throws ConverterException {
        Object convertedValue;
        TypeConverter<?> dTypeConverter = new DecimalTypeConverter();

        convertedValue = dTypeConverter.convertToBinding(new DecimalType(99.9), floatDp);
        assertThat(convertedValue, is(99.9));

        convertedValue = dTypeConverter.convertToBinding(new DecimalType(99.9999999999999999999999999999999), floatDp);
        assertThat(convertedValue, is(100.0));

        convertedValue = dTypeConverter.convertToBinding(new DecimalType(99.0), integerDp);
        assertThat(convertedValue, is(99));

        convertedValue = dTypeConverter.convertToBinding(new DecimalType(99.9), integerDp);
        assertThat(convertedValue, is(99));
    }

    @Test
    public void testQuantityTypeConverter() throws ConverterException {
        Object convertedValue;
        TypeConverter<?> qTypeConverter = new QuantityTypeConverter();
        floatQuantityDp.setUnit("°C");

        convertedValue = qTypeConverter.convertToBinding(new QuantityType<Temperature>("99.9 °C"), floatQuantityDp);
        assertThat(convertedValue, is(99.9));

        convertedValue = qTypeConverter.convertToBinding(new QuantityType<Temperature>("451 °F"), floatQuantityDp);
        assertThat(convertedValue, is(232.777778));

        floatQuantityDp.setUnit("km/h");

        convertedValue = qTypeConverter.convertToBinding(new QuantityType<Speed>("70.07 m/s"), floatQuantityDp);
        assertThat(convertedValue, is(252.252));

        integerQuantityDp.setUnit("%");

        convertedValue = qTypeConverter.convertToBinding(new QuantityType<Dimensionless>("99.0 %"), integerQuantityDp);
        assertThat(convertedValue, is(99));

        convertedValue = qTypeConverter.convertToBinding(new QuantityType<Dimensionless>("99.9 %"), integerQuantityDp);
        assertThat(convertedValue, is(99));

        convertedValue = qTypeConverter.convertToBinding(new QuantityType<Dimensionless>("1"), integerQuantityDp);
        assertThat(convertedValue, is(100));

        integerQuantityDp.setUnit("Lux");

        convertedValue = qTypeConverter.convertToBinding(new QuantityType<Illuminance>("42 lx"), integerQuantityDp);
        assertThat(convertedValue, is(42));
    }

    @Test(expected = ConverterException.class)
    public void testQuantityTypeConverterFailsToConvertDecimalType() throws ConverterException {

        QuantityTypeConverter converter = new QuantityTypeConverter();
        converter.convertToBinding(new DecimalType(99.9), floatDp);
    }

    @Test(expected = ConverterException.class)
    public void testDecimalTypeConverterFailsToConvertQuantityType() throws ConverterException {

        DecimalTypeConverter converter = new DecimalTypeConverter();
        converter.convertToBinding(new QuantityType<Dimensionless>("99.9 %"), floatDp);
    }
}
