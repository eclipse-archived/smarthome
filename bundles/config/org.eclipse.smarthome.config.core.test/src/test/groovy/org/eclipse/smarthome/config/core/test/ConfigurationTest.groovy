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

}
