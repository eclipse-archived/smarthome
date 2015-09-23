/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.transform.regex.internal;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.transform.regex.internal.RegExTransformationService;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Thomas.Eichstaedt-Engelen
 */
public class RegExTransformationServiceTest extends AbstractTransformationServiceTest {

    private RegExTransformationService processor;

    @Before
    public void init() {
        processor = new RegExTransformationService();
    }

    @Test
    public void testTransformByRegex() throws TransformationException {

        // method under test
        String transformedResponse = processor.transform(".*?<current_conditions>.*?<temp_c data=\"(.*?)\".*", source);

        // Asserts
        assertEquals("8", transformedResponse);
    }

    @Test
    public void testTransformByRegex_noGroup() throws TransformationException {

        // method under test
        String transformedResponse = processor.transform(".*", source);

        // Asserts
        assertEquals("", transformedResponse);
    }

    @Test
    public void testTransformByRegex_moreThanOneGroup() throws TransformationException {

        // method under test
        String transformedResponse = processor
                .transform(".*?<current_conditions>.*?<temp_c data=\"(.*?)\"(.*)", source);

        // Asserts
        assertEquals("8", transformedResponse);
    }

}
