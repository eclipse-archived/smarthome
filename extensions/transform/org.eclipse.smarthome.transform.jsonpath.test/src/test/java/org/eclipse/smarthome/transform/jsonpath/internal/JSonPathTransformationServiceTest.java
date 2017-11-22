/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.transform.jsonpath.internal;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.transform.TransformationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Gaël L'hopital
 */
public class JSonPathTransformationServiceTest {

    private JSonPathTransformationService processor;

    @Before
    public void init() {
        processor = new JSonPathTransformationService();
    }

    @Test
    public void testTransformByJSon() throws TransformationException {

        String json = "{'store':{'book':[{'category':'reference','author':'Nigel Rees','title': 'Sayings of the Century', 'price': 8.95  } ],  'bicycle': { 'color': 'red',  'price': 19.95} }}";
        // method under test
        String transformedResponse = processor.transform("$.store.book[0].author", json);

        // Asserts
        Assert.assertEquals("Nigel Rees", transformedResponse);
    }

    private static final String jsonArray = "[" + //
            "{ \"id\":1, \"name\":\"bob\" }," + //
            "{ \"id\":2, \"name\":\"alice\" }" + //
            "]";

    @Test
    public void testValidPath1() throws TransformationException {
        String transformedResponse = processor.transform("$[0].name", jsonArray);
        assertEquals("bob", transformedResponse);
    }

    @Test
    public void testValidPath2() throws TransformationException {
        String transformedResponse = processor.transform("$[1].id", jsonArray);
        assertEquals("2", transformedResponse);
    }

    @Test(expected = TransformationException.class)
    public void testInvalidPathThrowsException() throws TransformationException {
        processor.transform("$$", jsonArray);
    }

    @Test(expected = TransformationException.class)
    public void testPathMismatchReturnNull() throws TransformationException {
        processor.transform("$[5].id", jsonArray);
    }

    @Test(expected = TransformationException.class)
    public void testInvalidJsonReturnNull() throws TransformationException {
        processor.transform("$", "{id:");
    }

}
