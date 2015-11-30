/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import java.util.regex.Pattern;

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterBuilder
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.config.core.FilterCriteria;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.junit.Test

class ConfigDescriptionParameterBuilderTest {

    @Test
    void 'assert that created ConfigDescriptionParameter return expected values'() {
        def name = "Dummy"
        def type = Type.BOOLEAN
        def min = new BigDecimal(2.0);
        def max = new BigDecimal(4.0);
        def stepSize = new BigDecimal(1.0);
        def pattern = "pattern"
        def required = false
        def readOnly = true
        def multiple = false
        def context = "context"
        def defaultVal = "default"
        def label = "label"
        def description = "description"
        def ParameterOption[] options = [new ParameterOption("val", "label")]
        def FilterCriteria[] criterias = [new FilterCriteria("name", "value")]
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
        assertFalse param.isRequired()
        assertTrue param.isReadOnly()
        assertFalse param.isMultiple()
        assertFalse param.isAdvanced()
        assertTrue param.getLimitToOptions()
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
        //list attributes
        assertTrue param.getFilterCriteria().isEmpty()
        assertTrue param.getOptions().isEmpty()
        //boolean attributes
        assertFalse param.isRequired()
        assertFalse param.isReadOnly()
        assertFalse param.isMultiple()
        assertFalse param.isAdvanced()
        assertFalse param.getLimitToOptions()
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
}
