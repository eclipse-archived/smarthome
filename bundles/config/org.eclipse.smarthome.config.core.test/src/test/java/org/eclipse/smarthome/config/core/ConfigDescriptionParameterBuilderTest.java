/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Arrays;

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.junit.Test;

public class ConfigDescriptionParameterBuilderTest {

    @Test
    public void ConfigDescriptionParameterReturnExpectedValues() {
        String name = "Dummy";
        Type type = Type.INTEGER;
        BigDecimal min = new BigDecimal(2.0);
        BigDecimal max = new BigDecimal(4.0);
        BigDecimal stepSize = new BigDecimal(1.0);
        String pattern = "pattern";
        boolean verify = true;
        boolean required = false;
        boolean readOnly = true;
        boolean multiple = false;
        String context = "context";
        String defaultVal = "default";
        String label = "label";
        String description = "description";
        String unit = "m";
        String unitLabel = "unitLabel";
        ParameterOption[] options = { new ParameterOption("val", "label") };
        FilterCriteria[] criterias = { new FilterCriteria("name", "value") };
        String groupName = "groupName";
        boolean advanced = false;
        boolean limitToOptions = true;
        Integer multipleLimit = new Integer(17);
        ConfigDescriptionParameter param = ConfigDescriptionParameterBuilder.create(name, type).withMinimum(min)
                .withMaximum(max).withStepSize(stepSize).withPattern(pattern).withRequired(required)
                .withReadOnly(readOnly).withMultiple(multiple).withContext(context).withDefault(defaultVal)
                .withLabel(label).withDescription(description).withOptions(Arrays.asList(options))
                .withFilterCriteria(Arrays.asList(criterias)).withGroupName(groupName).withAdvanced(advanced)
                .withLimitToOptions(limitToOptions).withMultipleLimit(multipleLimit).withUnit(unit)
                .withUnitLabel(unitLabel).withVerify(verify).build();
        assertThat(param.getMinimum(),

                is(min));
        assertThat(param.getMaximum(), is(max));
        assertThat(param.getStepSize(), is(stepSize));
        assertThat(param.getPattern(), is(pattern));
        assertThat(param.getContext(), is(context));
        assertThat(param.getDefault(), is(defaultVal));
        assertThat(param.getLabel(), is(label));
        assertThat(param.getDescription(), is(description));
        assertThat(param.getGroupName(), is(groupName));
        assertThat(param.getMultipleLimit(), is(multipleLimit));
        assertThat(param.getFilterCriteria(), hasItems(criterias));
        assertThat(param.getOptions(), hasItems(options));
        assertThat(param.getUnit(), is(unit));
        assertThat(param.getUnitLabel(), is(unitLabel));
        assertFalse(param.isRequired());
        assertTrue(param.isReadOnly());
        assertFalse(param.isMultiple());
        assertTrue(param.isVerifyable());
        assertFalse(param.isAdvanced());
        assertTrue(param.getLimitToOptions());

        param = ConfigDescriptionParameterBuilder.create(name, type).withUnitLabel(unitLabel).build();
        assertThat(param.getUnitLabel(), is(unitLabel));
    }

    @Test
    public void GetterReturnExpectedValues() {
        ConfigDescriptionParameter param = ConfigDescriptionParameterBuilder.create("Dummy", Type.BOOLEAN)
                .withMinimum(null).withMaximum(null).withStepSize(null).withPattern(null).withRequired(null)
                .withReadOnly(null).withMultiple(null).withVerify(null).withContext(null).withDefault(null)
                .withLabel(null).withDescription(null).withOptions(null).withFilterCriteria(null).withGroupName(null)
                .withAdvanced(null).withLimitToOptions(null).withMultipleLimit(null).withUnit(null).withUnitLabel(null)
                .build();
        // nullable attributes
        assertNull(param.getMinimum());
        assertNull(param.getMaximum());
        assertNull(param.getStepSize());
        assertNull(param.getPattern());
        assertNull(param.getContext());
        assertNull(param.getDefault());
        assertNull(param.getLabel());
        assertNull(param.getDescription());
        assertNull(param.getGroupName());
        assertNull(param.getMultipleLimit());
        assertNull(param.getUnit());
        assertNull(param.getUnitLabel());
        // list attributes
        assertTrue(param.getFilterCriteria().isEmpty());
        assertTrue(param.getOptions().isEmpty());
        // boolean attributes
        assertFalse(param.isRequired());
        assertFalse(param.isReadOnly());
        assertFalse(param.isMultiple());
        assertFalse(param.isAdvanced());
        assertFalse(param.getLimitToOptions());
        ConfigDescriptionParameter param2 = new ConfigDescriptionParameter("Dummy", Type.BOOLEAN, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        assertFalse(param2.isRequired());
        assertFalse(param2.isReadOnly());
        assertFalse(param2.isMultiple());
        assertFalse(param2.isAdvanced());
        assertFalse(param2.getLimitToOptions());
        assertTrue(param2.getFilterCriteria().isEmpty());
        assertTrue(param2.getOptions().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void NameNotNull() {
        ConfigDescriptionParameterBuilder.create(null, Type.BOOLEAN).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void NameMustNotBeEmpty() {
        ConfigDescriptionParameterBuilder.create("", Type.BOOLEAN).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void TypeNotNull() {
        ConfigDescriptionParameterBuilder.create("Dummy", null).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void UnitForText() {
        ConfigDescriptionParameterBuilder.create("Dummy", Type.TEXT).withUnit("m").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void UnitForBoolean() {
        ConfigDescriptionParameterBuilder.create("Dummy", Type.BOOLEAN).withUnit("m").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void UnitLabelForText() {
        ConfigDescriptionParameterBuilder.create("Dummy", Type.TEXT).withUnitLabel("Runs").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void UnitLabelForBoolean() {
        ConfigDescriptionParameterBuilder.create("Dummy", Type.BOOLEAN).withUnitLabel("Runs").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void ParameterWithInvalidUnit() {
        ConfigDescriptionParameterBuilder.create("Dummy", Type.BOOLEAN).withUnit("invalid").build();
    }
}
