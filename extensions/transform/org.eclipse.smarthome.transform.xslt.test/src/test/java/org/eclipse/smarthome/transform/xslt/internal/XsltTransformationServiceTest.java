/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.transform.xslt.internal;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.transform.TransformationException;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Thomas.Eichstaedt-Engelen
 */
public class XsltTransformationServiceTest extends AbstractTransformationServiceTest {

    private XsltTransformationService processor;

    @Before
    public void init() {
        processor = new XsltTransformationService();
    }

    @Test
    public void testTransformByXSLT() throws TransformationException {

        // method under test
        String transformedResponse = processor.transform("http/google_weather.xsl", source);

        // Asserts
        assertEquals("8", transformedResponse);
    }

}
