/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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
