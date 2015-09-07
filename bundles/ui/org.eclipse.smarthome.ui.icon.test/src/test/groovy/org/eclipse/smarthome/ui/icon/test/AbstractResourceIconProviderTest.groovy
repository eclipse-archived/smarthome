/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.icon.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.apache.commons.lang.StringUtils
import org.eclipse.smarthome.ui.icon.AbstractResourceIconProvider
import org.eclipse.smarthome.ui.icon.IconProvider
import org.eclipse.smarthome.ui.icon.IconSet.Format
import org.junit.*


/**
 * Tests for {@link AbstractResourceIconProvider}.
 *
 * @author Kai Kreuzer - Initial contribution
 */
class AbstractResourceIconProviderTest {

    IconProvider provider

    @Before
    void setUp() {

        provider = new AbstractResourceIconProvider() {
                    protected InputStream getResource(String iconset, String resourceName) {
                        if(resourceName=="x-30.png") {
                            new StringBufferInputStream("")
                        } else {
                            null
                        };
                    }
                    protected boolean hasResource(String iconset, String resourceName) {
                        String state = StringUtils.substringAfterLast(resourceName, "-");
                        state = StringUtils.substringBeforeLast(state, ".");
                        def value = Integer.parseInt(state);
                        value == 30
                    };
                    Set getIconSets(Locale locale) {};
                    Integer getPriority() {
                        0
                    };
                }
    }

    @Test
    void testScanningForState() {
        def result = provider.getIcon("x", "classic", "34", Format.PNG)
        assertThat result, notNullValue()

        result = provider.getIcon("x", "classic", "25", Format.PNG)
        assertThat result, nullValue()
    }
}
