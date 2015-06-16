/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.transform.internal;

import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.internal.service.JsonPathTransformationService;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Thomas.Eichstaedt-Engelen
 */
public class JsonPathTransformationServiceTest {

    private JsonPathTransformationService processor;
    
    private static final String jsonArray = "["
    		+ "{ \"id\":1, \"name\":\"bob\" },"
    		+ "{ \"id\":2, \"name\":\"alice\" }"
    		+ "]";

    @Before
    public void init() {
        processor = new JsonPathTransformationService();
    }

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
