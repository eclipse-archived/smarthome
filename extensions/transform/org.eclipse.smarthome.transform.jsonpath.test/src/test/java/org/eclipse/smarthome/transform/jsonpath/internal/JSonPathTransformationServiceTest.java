/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.transform.jsonpath.internal;

import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.transform.jsonpath.internal.JSonPathTransformationService;
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
        String transformedResponse = processor.transform("$.store.book[0].author",json);

        // Asserts
        Assert.assertEquals("Nigel Rees", transformedResponse);
    }
    
    private static final String jsonArray = "["
            + "{ \"id\":1, \"name\":\"bob\" },"
            + "{ \"id\":2, \"name\":\"alice\" }"
            + "]";

    @Test
    public void testValidPath1() throws TransformationException {
        String transformedResponse = processor.transform("$[0].name", jsonArray);
        assert(transformedResponse == "bob");
    }

    @Test
    public void testValidPath2() throws TransformationException {
        String transformedResponse = processor.transform("$[1].id", jsonArray);
        assert(transformedResponse == "2");
    }
    
    @Test(expected = TransformationException.class)
    public void testInvalidPathThrowsException() throws TransformationException {
        processor.transform("$$", jsonArray);
    }
    
    @Test
    public void testPathMismatchReturnNull() throws TransformationException {
        String transformedResponse = processor.transform("$[5].id", jsonArray);
        assert(transformedResponse == null);
    }
    
    @Test
    public void testInvalidJsonReturnNull() throws TransformationException {
        String transformedResponse = processor.transform("$", jsonArray.substring(1));
        assert(transformedResponse == null);
    }

}
