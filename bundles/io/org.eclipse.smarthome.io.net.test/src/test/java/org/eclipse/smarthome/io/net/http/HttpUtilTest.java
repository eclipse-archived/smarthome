/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.net.http;

import static org.junit.Assert.assertEquals;

import org.eclipse.jetty.http.HttpMethod;
import org.junit.Test;

/**
 * @author Thomas Eichstaedt-Engelen
 */
public class HttpUtilTest {

    @Test
    public void testCreateHttpMethod() {
        assertEquals(HttpMethod.GET, HttpUtil.createHttpMethod("GET"));
        assertEquals(HttpMethod.PUT, HttpUtil.createHttpMethod("PUT"));
        assertEquals(HttpMethod.POST, HttpUtil.createHttpMethod("POST"));
        assertEquals(HttpMethod.DELETE, HttpUtil.createHttpMethod("DELETE"));
    }

}
