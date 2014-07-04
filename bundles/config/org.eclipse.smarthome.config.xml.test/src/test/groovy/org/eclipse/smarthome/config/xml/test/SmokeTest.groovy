/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.xml.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.test.OSGiTest
import org.junit.Before
import org.junit.Test

class SmokeTest extends OSGiTest {

    @Test
    void 'assert that BundleContext is available'() {
        def bundleContext = getBundleContext()
        assertThat bundleContext, is(notNullValue())
    }
}
