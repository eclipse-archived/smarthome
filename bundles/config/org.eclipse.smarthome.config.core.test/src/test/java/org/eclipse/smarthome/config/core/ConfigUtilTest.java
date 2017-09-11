/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Simon Kaufmann - initial contribution and API.
 * @author David Graeff - From Groovy to Java
 *
 */
public class ConfigUtilTest {
    private ConfigDescriptionParameter configDescriptionParameterInteger;
    private ConfigDescriptionParameter configDescriptionParameterString;
    private ConfigDescription configDescriptionInteger;
    private ConfigDescription configDescriptionString;

    @Before
    public void setup() throws IllegalArgumentException, URISyntaxException {
        configDescriptionParameterInteger = new ConfigDescriptionParameter("foo", Type.INTEGER);
        configDescriptionInteger = new ConfigDescription(new URI("thing:fooThing"),
                Arrays.asList(configDescriptionParameterInteger));

        configDescriptionParameterString = new ConfigDescriptionParameter("foo", Type.TEXT);
        configDescriptionString = new ConfigDescription(new URI("thingType:fooThing"),
                Arrays.asList(configDescriptionParameterString));
    }

    @Test
    public void normalizeIgnoresUnknownObjects() {
        Object testObject = new Object();
        assertThat(ConfigUtil.normalizeType(testObject, null), is(testObject));
    }

    @Test
    public void normalizeWithDescriptor() {
        assertThat(ConfigUtil.normalizeType("1", configDescriptionParameterInteger), is(instanceOf(BigDecimal.class)));
        assertThat(ConfigUtil.normalizeType("foo", configDescriptionParameterString), is(instanceOf(String.class)));
    }

    @SuppressWarnings("null")
    @Test
    public void firstDesciptionWinsForNormalization() throws URISyntaxException {
        Map<String, Object> data = new HashMap<>();
        data.put("foo", 1);
        assertThat(ConfigUtil.normalizeTypes(data, Arrays.asList(configDescriptionInteger, configDescriptionString))
                .get("foo"), is(instanceOf(BigDecimal.class)));
        assertThat(ConfigUtil.normalizeTypes(data, Arrays.asList(configDescriptionString, configDescriptionInteger))
                .get("foo"), is(instanceOf(String.class)));
    }
}
