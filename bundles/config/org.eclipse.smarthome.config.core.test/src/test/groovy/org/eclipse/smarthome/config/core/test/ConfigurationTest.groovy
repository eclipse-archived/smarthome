/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.config.core.Configuration
import org.junit.Test

/**
 * Test for Configuration class.
 *
 * @author Dennis Nobel - Initial contribution
 */
class ConfigurationTest {

    public final static class ConfigClass {
        private int intField;
        private boolean booleanField;
        private String stringField;
        private static final String CONSTANT = "SOME_CONSTANT";
    }

    @Test
    void 'assert getConfigAs works'() {

        def configuration = new Configuration([
            intField: 1,
            booleanField: false,
            stringField: "test",
            notExisitingProperty: true])

        def configClass = configuration.as(ConfigClass)

        assertThat configClass.intField, is(equalTo(1))
        assertThat configClass.booleanField, is(false)
        assertThat configClass.stringField, is("test")
    }

    @Test
    void 'assert config allows null values'() {

        def configuration = new Configuration([
            stringField: null,
            anotherField: null
        ])

        // ensure conversions are null-tolerant and don't throw exceptions
        def props = configuration.getProperties()
        def keys = configuration.keySet()
        def values = configuration.values()

        // ensure copies, not views
        configuration.put("stringField", "someValue")
        configuration.put("additionalField", "")
        assertThat props.get("stringField"), is(nullValue())
        assertThat values.first(), is(nullValue())
        assertThat values.last(), is(nullValue())
        assertThat values.size(), is(2)
        assertThat keys.size(), is(2)
    }


    @Test
    void 'assert properties can be removed'() {
        def orgProperties = new HashMap([
            intField: 1,
            booleanField: false,
            stringField: "test",
            notExisitingProperty: true]);

        def newProperties = new HashMap([
            booleanField: false,
            stringField: "test",
            notExisitingProperty: true]);

        def configuration = new Configuration(orgProperties)

        assertThat configuration.get("intField"), is(equalTo(1))

        configuration.setProperties(newProperties)

        assertThat configuration.get("intField"), is(equalTo(null))
    }
}
