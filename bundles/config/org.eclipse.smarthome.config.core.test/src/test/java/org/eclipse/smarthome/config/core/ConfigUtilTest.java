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
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.junit.Test;

/**
 *
 * @author Simon Kaufmann - initial contribution and API.
 * @author David Graeff - From Groovy to Java
 *
 */
class ConfigUtilTest {
    private Map<String, Object> m(String a, Object b) {
        return Stream.of(new SimpleEntry<>(a, b)).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
    }

    @SuppressWarnings("null")
    @Test
    public void firstDesciptionWinsForNormalization() throws URISyntaxException {
        ConfigDescription configDescriptionInteger = new ConfigDescription(new URI("thing:fooThing"),
                Arrays.asList(new ConfigDescriptionParameter("foo", Type.INTEGER)));

        ConfigDescription configDescriptionString = new ConfigDescription(new URI("thingType:fooThing"),
                Arrays.asList(new ConfigDescriptionParameter("foo", Type.TEXT)));

        assertThat(ConfigUtil.normalizeTypes(m("foo", "1"), Arrays.asList(configDescriptionInteger)).get("foo"),
                is(instanceOf(BigDecimal.class)));
        assertThat(ConfigUtil.normalizeTypes(m("foo", "1"), Arrays.asList(configDescriptionString)).get("foo"),
                is(instanceOf(String.class)));
        assertThat(ConfigUtil
                .normalizeTypes(m("foo", "1"), Arrays.asList(configDescriptionInteger, configDescriptionString))
                .get("foo"), is(instanceOf(BigDecimal.class)));
        assertThat(ConfigUtil
                .normalizeTypes(m("foo", "1"), Arrays.asList(configDescriptionString, configDescriptionInteger))
                .get("foo"), is(instanceOf(String.class)));
    }
}
