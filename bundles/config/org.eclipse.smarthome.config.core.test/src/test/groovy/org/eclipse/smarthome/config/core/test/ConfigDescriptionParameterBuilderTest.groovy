/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.test


import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterBuilder
import org.eclipse.smarthome.config.core.FilterCriteria
import org.eclipse.smarthome.config.core.ParameterOption
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type
import org.junit.Test

class ConfigDescriptionParameterBuilderTest {

    @Test
    void 'assert that created ConfigDescriptionParameter return expected values'() {
        def name = "Dummy"
        def type = Type.INTEGER
        def min = new BigDecimal(2.0);
        def max = new BigDecimal(4.0);
        def stepSize = new BigDecimal(1.0);
        def pattern = "pattern"
        def verify = true;
        def required = false
        def readOnly = true
        def multiple = false
        def context = "context"
        def defaultVal = "default"
        def label = "label"
        def description = "description"
        def unit = "m"
        def unitLabel = "unitLabel"
        def ParameterOption[] options = [
            new ParameterOption("val", "label")
        ]
        def FilterCriteria[] criterias = [
            new FilterCriteria("name", "value")
        ]
        def groupName = "groupName"
        def advanced = false
        def limitToOptions = true
        def multipleLimit = new Integer(17)
        def param = ConfigDescriptionParameterBuilder.create(name, type)
                .withMinimum(min)
                .withMaximum(max)
                .withStepSize(stepSize)
                .withPattern(pattern)
                .withRequired(required)
                .withReadOnly(readOnly)
                .withMultiple(multiple)
                .withContext(context)
                .withDefault(defaultVal)
                .withLabel(label)
                .withDescription(description)
                .withOptions(Arrays.asList(options))
                .withFilterCriteria(Arrays.asList(criterias))
                .withGroupName(groupName)
                .withAdvanced(advanced)
                .withLimitToOptions(limitToOptions)
                .withMultipleLimit(multipleLimit)
                .withUnit(unit)
                .withUnitLabel(unitLabel)
                .withVerify(verify)
                .build();
        assertThat param.getMinimum(), is(min)
        assertThat param.getMaximum(), is(max)
        assertThat param.getStepSize(), is(stepSize)
        assertThat param.getPattern(), is(pattern)
        assertThat param.getContext(), is(context)
        assertThat param.getDefault(), is(defaultVal)
        assertThat param.getLabel(), is(label)
        assertThat param.getDescription(), is(description)
        assertThat param.getGroupName(), is(groupName)
        assertThat param.getMultipleLimit(), is(multipleLimit)
        assertThat param.getFilterCriteria(), hasItems(criterias)
        assertThat param.getOptions(), hasItems(options)
        assertThat param.getUnit(), is(unit)
        assertThat param.getUnitLabel(), is(unitLabel)
        assertFalse param.isRequired()
        assertTrue param.isReadOnly()
        assertFalse param.isMultiple()
        assertTrue param.isVerifyable()
        assertFalse param.isAdvanced()
        assertTrue param.getLimitToOptions()

        param = ConfigDescriptionParameterBuilder.create(name, type).withUnitLabel(unitLabel).build()
        assertThat param.getUnitLabel(), is(unitLabel)
    }

    @Test
    void 'assert that getter for not nullable attributes initialized with null return expected values'() {
        def param = ConfigDescriptionParameterBuilder.create("Dummy", Type.BOOLEAN)
                .withMinimum(null)
                .withMaximum(null)
                .withStepSize(null)
                .withPattern(null)
                .withRequired(null)
                .withReadOnly(null)
                .withMultiple(null)
                .withVerify(null)
                .withContext(null)
                .withDefault(null)
                .withLabel(null)
                .withDescription(null)
                .withOptions(null)
                .withFilterCriteria(null)
                .withGroupName(null)
                .withAdvanced(null)
                .withLimitToOptions(null)
                .withMultipleLimit(null)
                .withUnit(null)
                .withUnitLabel(null)
                .build();
        //nullable attributes
        assertThat param.getMinimum(), is(null)
        assertThat param.getMaximum(), is(null)
        assertThat param.getStepSize(), is(null)
        assertThat param.getPattern(), is(null)
        assertThat param.getContext(), is(null)
        assertThat param.getDefault(), is(null)
        assertThat param.getLabel(), is(null)
        assertThat param.getDescription(), is(null)
        assertThat param.getGroupName(), is(null)
        assertThat param.getMultipleLimit(), is(null)
        assertThat param.getUnit(), is(null)
        assertThat param.getUnitLabel(), is(null)
        //list attributes
        assertTrue param.getFilterCriteria().isEmpty()
        assertTrue param.getOptions().isEmpty()
        //boolean attributes
        assertFalse param.isRequired()
        assertFalse param.isReadOnly()
        assertFalse param.isMultiple()
        assertFalse param.isAdvanced()
        assertFalse param.getLimitToOptions()
        def param2 = new ConfigDescriptionParameter("Dummy", Type.BOOLEAN, null, null,
                null, null, null, null, null, null,
                null, null, null, null,
                null, null, null,null,
                null, null, null, null)
        assertFalse param2.isRequired()
        assertFalse param2.isReadOnly()
        assertFalse param2.isMultiple()
        assertFalse param2.isAdvanced()
        assertFalse param2.getLimitToOptions()
        assertTrue param2.getFilterCriteria().isEmpty()
        assertTrue param2.getOptions().isEmpty()
    }

    @Test(expected=IllegalArgumentException)
    void 'assert that name must not be null'() {
        ConfigDescriptionParameterBuilder.create(null, Type.BOOLEAN).build()
    }

    @Test(expected=IllegalArgumentException)
    void 'assert that name must not be empty'() {
        ConfigDescriptionParameterBuilder.create("", Type.BOOLEAN).build()
    }

    @Test(expected=IllegalArgumentException)
    void 'assert that type must not be null'() {
        ConfigDescriptionParameterBuilder.create("Dummy", null).build()
    }

    @Test(expected=IllegalArgumentException)
    void 'assert that unit for text parameter is not allowed'() {
        ConfigDescriptionParameterBuilder.create("Dummy", Type.TEXT).withUnit("m").build()
    }

    @Test(expected=IllegalArgumentException)
    void 'assert that unit for boolean parameter is not allowed'() {
        ConfigDescriptionParameterBuilder.create("Dummy", Type.BOOLEAN).withUnit("m").build()
    }

    @Test(expected=IllegalArgumentException)
    void 'assert that unit label  for text parameter is not allowed'() {
        ConfigDescriptionParameterBuilder.create("Dummy", Type.TEXT).withUnitLabel("Runs").build()
    }

    @Test(expected=IllegalArgumentException)
    void 'assert that unit label for boolean parameter is not allowed'() {
        ConfigDescriptionParameterBuilder.create("Dummy", Type.BOOLEAN).withUnitLabel("Runs").build()
    }

    @Test(expected=IllegalArgumentException)
    void 'assert that a parameter with an invalid unit cannot be created'() {
        ConfigDescriptionParameterBuilder.create("Dummy", Type.BOOLEAN).withUnit("invalid").build()
    }
}
