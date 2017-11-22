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
