/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type
import org.junit.Test


/**
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
class ConfigUtilTest {

    @Test
    def void 'assert that the first config description wins for normalization'() {
        ConfigDescription configDescriptionInteger = new ConfigDescription(new URI("thing:fooThing"), [
            new ConfigDescriptionParameter("foo", Type.INTEGER)
        ])
        ConfigDescription configDescriptionString = new ConfigDescription(new URI("thingType:fooThing"), [
            new ConfigDescriptionParameter("foo", Type.TEXT)
        ])

        assertThat ConfigUtil.normalizeTypes(["foo":"1"], [configDescriptionInteger]).get("foo"), is(instanceOf(BigDecimal))
        assertThat ConfigUtil.normalizeTypes(["foo":"1"], [configDescriptionString]).get("foo"), is(instanceOf(String))
        assertThat ConfigUtil.normalizeTypes(["foo":"1"], [
            configDescriptionInteger,
            configDescriptionString
        ]).get("foo"), is(instanceOf(BigDecimal))
        assertThat ConfigUtil.normalizeTypes(["foo":"1"], [
            configDescriptionString,
            configDescriptionInteger
        ]).get("foo"), is(instanceOf(String))
    }
}
