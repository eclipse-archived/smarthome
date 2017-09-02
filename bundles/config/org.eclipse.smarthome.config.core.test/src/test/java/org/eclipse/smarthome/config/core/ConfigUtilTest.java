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
import java.net.*;
import java.util.*;

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.junit.Test;

/**
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
class ConfigUtilTest {
    private Map<String, Object> m(String a, Object b) {
        Map<String, Object> m = new HashMap<>();
        m.put(a, b);
        return m;
    }

    private static class L<T> extends ArrayList<T> {
        L(T... args) {
            for (T arg : args) {
                add(arg);
            }
        }
    };

    @Test
    public void firstDesciptionWinsForNormalization() throws URISyntaxException {
        ConfigDescription configDescriptionInteger = new ConfigDescription(new URI("thing:fooThing"),
                new L<>(new ConfigDescriptionParameter("foo", Type.INTEGER)));

        ConfigDescription configDescriptionString = new ConfigDescription(new URI("thingType:fooThing"),
                new L<>(new ConfigDescriptionParameter("foo", Type.TEXT)));

        assertThat(ConfigUtil.normalizeTypes(m("foo", "1"), new L<>(configDescriptionInteger)).get("foo"),
                is(instanceOf(BigDecimal.class)));
        assertThat(ConfigUtil.normalizeTypes(m("foo", "1"), new L<>(configDescriptionString)).get("foo"),
                is(instanceOf(String.class)));
        assertThat(ConfigUtil.normalizeTypes(m("foo", "1"), new L<>(configDescriptionInteger, configDescriptionString))
                .get("foo"), is(instanceOf(BigDecimal.class)));
        assertThat(ConfigUtil.normalizeTypes(m("foo", "1"), new L<>(configDescriptionString, configDescriptionInteger))
                .get("foo"), is(instanceOf(String.class)));
    }
}
