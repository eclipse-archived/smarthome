/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.net.http;

import static org.junit.Assert.assertEquals;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.junit.Test;

/**
 * @author Thomas.Eichstaedt-Engelen
 */
public class HttpUtilTest {

    @Test
    public void testExtractCredentials() {

        String expectedUsername = "userna/!&%)(me";
        String expectedPassword = "password67612/&%!$";
        String testUrl = "http://" + expectedUsername + ":" + expectedPassword + "@www.domain.org/123/user";

        // method under test
        UsernamePasswordCredentials credentials = (UsernamePasswordCredentials) HttpUtil.extractCredentials(testUrl);

        // assert
        assertEquals(expectedUsername, credentials.getUserName());
        assertEquals(expectedPassword, credentials.getPassword());
    }

    @Test
    public void testCreateHttpMethod() {
        assertEquals(GetMethod.class, HttpUtil.createHttpMethod("GET", "").getClass());
        assertEquals(PutMethod.class, HttpUtil.createHttpMethod("PUT", "").getClass());
        assertEquals(PostMethod.class, HttpUtil.createHttpMethod("POST", "").getClass());
        assertEquals(DeleteMethod.class, HttpUtil.createHttpMethod("DELETE", "").getClass());
    }

}
