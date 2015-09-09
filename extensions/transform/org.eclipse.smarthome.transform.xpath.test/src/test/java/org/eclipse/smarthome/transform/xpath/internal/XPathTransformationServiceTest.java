/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.transform.xpath.internal;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.transform.TransformationException;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Thomas.Eichstaedt-Engelen
 */
public class XPathTransformationServiceTest extends AbstractTransformationServiceTest {

    private XPathTransformationService processor;

    @Before
    public void init() {
        processor = new XPathTransformationService();
    }

    @Test
    public void testTransformByXPath() throws TransformationException {

        // method under test
        String transformedResponse = processor.transform("//current_conditions/temp_c/@data", source);

        // Asserts
        assertEquals("8", transformedResponse);
    }

}
