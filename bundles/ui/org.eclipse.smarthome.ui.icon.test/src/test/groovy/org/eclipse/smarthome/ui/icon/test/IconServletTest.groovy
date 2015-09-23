/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.icon.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.eclipse.smarthome.ui.icon.IconProvider
import org.eclipse.smarthome.ui.icon.internal.IconServlet
import org.junit.*


/**
 * Tests for {@link IconServlet}.
 *
 * @author Kai Kreuzer - Initial contribution
 */
class IconServletTest {

    IconServlet servlet
    def provider1
    def provider2
    def calledProvider
    def response

    @Before
    void setUp() {
        servlet = new IconServlet();

        provider1 = [
            hasIcon : { category, iconsetid, format ->
                if(category=="x" && iconsetid=="test" && format.toString().toLowerCase().equals("svg")) 0 else
                if(category=="y" && iconsetid=="classic" && format.toString().toLowerCase().equals("png")) 0 else
                    null
            },
            getIcon : { category, iconsetid, state, format ->
                if(category=="x" && iconsetid=="test" && format.toString().toLowerCase().equals("svg") ||
                category=="y" && iconsetid=="classic" && state=="34" && format.toString().toLowerCase().equals("png")) {
                    calledProvider = 1
                    new StringBufferInputStream("")
                }
                else null
            }
        ] as IconProvider
        provider2 = [
            hasIcon : { category, iconsetid, format ->
                if(category=="x" && iconsetid=="test" && format.toString().toLowerCase().equals("svg")) 1 else null
            },
            getIcon : { category, iconsetid, state, format ->
                if(category=="x" && iconsetid=="test" && format.toString().toLowerCase().equals("svg")) {
                    calledProvider = 2
                    new StringBufferInputStream("")
                } else null
            }
        ] as IconProvider

        response = [
            setDateHeader : { s, d -> null },
            setContentType : { s -> null },
            getOutputStream : { null },
            flushBuffer : {},
            sendError : { i -> null }
        ] as HttpServletResponse

        calledProvider = null
    }

    @Test
    void testOldUrlStyle() {
        def request = [
            getParameter : { p -> null },
            getRequestURI : { "/y-34.png" },
            getDateHeader : { s -> 0L },
        ] as HttpServletRequest
        servlet.addIconProvider(provider1)
        servlet.doGet(request, response)
        assertThat calledProvider, equalTo(1)
    }

    @Test
    void testPriority() {
        def request = [
            getParameter : { p ->
                switch(p) {
                    case "format": return "svg"
                    case "iconset": return "test"
                    case "state": return "34"
                }
                null
            },
            getRequestURI : { "/x" },
            getDateHeader : { s -> 0L },
        ] as HttpServletRequest
        servlet.addIconProvider(provider1)
        servlet.doGet(request, response)
        assertThat calledProvider, equalTo(1)

        calledProvider = null
        servlet.addIconProvider(provider2)
        servlet.doGet(request, response)
        assertThat calledProvider, equalTo(2)
    }
}
