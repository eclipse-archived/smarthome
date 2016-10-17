/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.wemo.internal.http.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.eclipse.smarthome.binding.wemo.internal.http.WemoHttpCall
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.service.http.HttpService

/**
 * Tests for {@link WemoHttpCall}.
 *
 * @author Svilen Valkanov - Initial contribution
 */
class WemoHttpCallOSGiTest extends OSGiTest{
    
    def ORG_OSGI_SERVICE_HTTP_PORT = 8080
    def DESTINATION_URL = "http://127.0.0.1:${ORG_OSGI_SERVICE_HTTP_PORT}"
    def SERVLET_URL = "/test"
    def servlet = null

    protected registerServlet(String ServletURL, HttpServlet servlet) {
        //Register Servlet that will mock the device action responses
        HttpService httpService = getService(HttpService.class)
        assertThat(httpService, is(notNullValue()))
        httpService.registerServlet(ServletURL, servlet, null, null)
    }

    protected void unregisterServlet(def servletURL) {
        //Unregister the servlet
        HttpService httpService = getService(HttpService.class)
        httpService.unregister(servletURL)
    }

    @Before
    void setUp() {
        servlet =  new MockHttpServlet()
        registerServlet(SERVLET_URL, servlet)
    }

    @Test
    void 'assert post request contains correct content header'() {
        String soapHeader = "\"urn:Belkin:service:bridge:1#GetDeviceStatus\""
        String content = '''
        <?xml version=\"1.0\"?>
        <s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">
            <s:Body>
                <u:GetDeviceStatus xmlns:u=\"urn:Belkin:service:bridge:1\">
                    <DeviceIDs>testID</DeviceIDs>
                </u:GetDeviceStatus>
            </s:Body>
        </s:Envelope>'''

        WemoHttpCall.executeCall(DESTINATION_URL + SERVLET_URL, soapHeader, content)
        waitForAssert {
            assertThat servlet.contentType, is(notNullValue())
            assertThat servlet.contentType.toLowerCase(), is(WemoHttpCall.contentHeader.toLowerCase())
            assertThat servlet.soapHeader, is(soapHeader)
            assertThat servlet.content, is(content)
        }
    }

    @After
    void tearDown() {
        unregisterServlet(SERVLET_URL);
    }

    class MockHttpServlet extends HttpServlet {
        def contentType
        def soapHeader
        def content

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            contentType = req.getContentType()
            soapHeader = req.getHeader("SOAPACTION")
            content = req.getInputStream().getText()
            
            resp.setContentType(contentType)
        }
    }
}
