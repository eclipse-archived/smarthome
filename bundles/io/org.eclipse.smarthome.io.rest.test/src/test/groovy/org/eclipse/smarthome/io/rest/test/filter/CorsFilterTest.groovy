/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.test.filter

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerResponseContext
import javax.ws.rs.core.MultivaluedHashMap
import javax.ws.rs.core.MultivaluedMap

import org.eclipse.smarthome.io.rest.internal.filter.CorsFilter
import org.junit.Before
import org.junit.Test

/**
 * Test for the {@link CorsFilter} filter.
 *
 * @author Antoine Besnard - Initial contribution
 *
 */
class CorsFilterTest {

    private static final String HTTP_HEAD_METHOD = "HEAD"
    private static final String HTTP_DELETE_METHOD = "DELETE"
    private static final String HTTP_PUT_METHOD = "PUT"
    private static final String HTTP_POST_METHOD = "POST"
    private static final String HTTP_GET_METHOD = "GET"
    private static final String HTTP_OPTIONS_METHOD = "OPTIONS"

    private static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method"
    private static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers"
    private static final String ACCESS_CONTROL_ALLOW_METHODS_HEADER = "Access-Control-Allow-Methods"
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin"
    private static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers"
    private static final String ORIGIN_HEADER = "Origin"
    private static final String VARY_HEADER = "Vary"

    private static final String VARY_HEADER_WILDCARD = "*"
    private static final String HEADERS_SEPARATOR = ","

    private static final String ECLIPSE_ORIGIN = "http://eclipse.org"
    private static final String VARY_HEADER_VALUE = "Content-Type"
    private static final String REQUEST_HEADERS = "X-Custom, X-Mine"

    private static final String ACCEPTED_HTTP_METHODS = HTTP_GET_METHOD + "," + HTTP_POST_METHOD + "," + HTTP_PUT_METHOD + "," + HTTP_DELETE_METHOD + "," + HTTP_HEAD_METHOD + "," + HTTP_OPTIONS_METHOD

    private CorsFilter filter


    @Before
    void setUp() {
        filter = new CorsFilter()
        filter.activate([
            'enable': 'true'
        ])
    }


    @Test
    public void notCorsOptionsRequestTest() {
        ContainerRequestContext requestContext = getRequestContextMock(HTTP_OPTIONS_METHOD, null, null, null)
        ContainerResponseContext responseContext = getResponseContextMock(null)

        filter.filter(requestContext, responseContext)

        // Not a CORS request, thus no CORS headers should be added.
        assertFalse containsHeaderWithValue(responseContext.getHeaders(), ACCESS_CONTROL_ALLOW_METHODS_HEADER, null)
        assertFalse containsHeaderWithValue(responseContext.getHeaders(), ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, null)
        assertFalse containsHeaderWithValue(responseContext.getHeaders(), ACCESS_CONTROL_ALLOW_HEADERS, null)
        assertFalse containsHeaderWithValue(responseContext.getHeaders(), VARY_HEADER, null)
    }

    @Test
    public void notCorsRealRequestTest() {
        ContainerRequestContext requestContext = getRequestContextMock(HTTP_GET_METHOD, null, null, null)
        ContainerResponseContext responseContext = getResponseContextMock(null)

        filter.filter(requestContext, responseContext)

        // Not a CORS request, thus no CORS headers should be added.
        assertFalse containsHeaderWithValue(responseContext.getHeaders(), ACCESS_CONTROL_ALLOW_METHODS_HEADER, null)
        assertFalse containsHeaderWithValue(responseContext.getHeaders(), ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, null)
        assertFalse containsHeaderWithValue(responseContext.getHeaders(), ACCESS_CONTROL_ALLOW_HEADERS, null)
        assertFalse containsHeaderWithValue(responseContext.getHeaders(), VARY_HEADER, null)
    }

    @Test
    public void corsPreflightRequestTest() {
        ContainerRequestContext requestContext = getRequestContextMock(HTTP_OPTIONS_METHOD, ECLIPSE_ORIGIN, HTTP_GET_METHOD, REQUEST_HEADERS)
        ContainerResponseContext responseContext = getResponseContextMock(VARY_HEADER_VALUE)

        filter.filter(requestContext, responseContext)

        assertTrue containsHeaderWithValue(responseContext.getHeaders(), ACCESS_CONTROL_ALLOW_METHODS_HEADER, ACCEPTED_HTTP_METHODS)
        assertTrue containsHeaderWithValue(responseContext.getHeaders(), ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, ECLIPSE_ORIGIN)
        assertFalse containsHeaderWithValue(responseContext.getHeaders(), ACCESS_CONTROL_ALLOW_HEADERS, null)
        assertTrue containsHeaderWithValue(responseContext.getHeaders(), VARY_HEADER, VARY_HEADER_VALUE + "," + ORIGIN_HEADER)
    }

    @Test
    public void partialCorsPreflightRequestTest() {
        ContainerRequestContext requestContext = getRequestContextMock(HTTP_OPTIONS_METHOD, ECLIPSE_ORIGIN, null, REQUEST_HEADERS)
        ContainerResponseContext responseContext = getResponseContextMock(VARY_HEADER_VALUE)

        filter.filter(requestContext, responseContext)

        // Since the requestMehod header is not present in the request, it is not a valid Preflight CORS request.
        // Thus, no CORS header should be added to the response.
        assertFalse containsHeaderWithValue(responseContext.getHeaders(), ACCESS_CONTROL_ALLOW_METHODS_HEADER, null)
        assertFalse containsHeaderWithValue(responseContext.getHeaders(), ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, null)
        assertFalse containsHeaderWithValue(responseContext.getHeaders(), ACCESS_CONTROL_ALLOW_HEADERS, null)
        assertTrue containsHeaderWithValue(responseContext.getHeaders(), VARY_HEADER, VARY_HEADER_VALUE)
    }

    @Test
    public void corsPreflightRequestWithoutRequestHeadersTest() {
        ContainerRequestContext requestContext = getRequestContextMock(HTTP_OPTIONS_METHOD, ECLIPSE_ORIGIN, HTTP_GET_METHOD, null)
        ContainerResponseContext responseContext = getResponseContextMock(VARY_HEADER_VALUE)

        filter.filter(requestContext, responseContext)

        // Since the requestMehod header is not present in the request, it is not a valid Preflight CORS request.
        // Thus, no CORS header should be added to the response.
        assertTrue containsHeaderWithValue(responseContext.getHeaders(), ACCESS_CONTROL_ALLOW_METHODS_HEADER, ACCEPTED_HTTP_METHODS)
        assertTrue containsHeaderWithValue(responseContext.getHeaders(), ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, ECLIPSE_ORIGIN)
        assertFalse containsHeaderWithValue(responseContext.getHeaders(), ACCESS_CONTROL_ALLOW_HEADERS, null)
        assertTrue containsHeaderWithValue(responseContext.getHeaders(), VARY_HEADER, VARY_HEADER_VALUE + "," + ORIGIN_HEADER)
    }

    @Test
    public void corsRealRequestTest() {
        ContainerRequestContext requestContext = getRequestContextMock(HTTP_GET_METHOD, ECLIPSE_ORIGIN, null, null)
        ContainerResponseContext responseContext = getResponseContextMock(null)

        filter.filter(requestContext, responseContext)

        // Not a CORS request, thus no CORS headers should be added.
        assertFalse containsHeaderWithValue(responseContext.getHeaders(), ACCESS_CONTROL_ALLOW_METHODS_HEADER, null)
        assertTrue containsHeaderWithValue(responseContext.getHeaders(), ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, ECLIPSE_ORIGIN)
        assertFalse containsHeaderWithValue(responseContext.getHeaders(), ACCESS_CONTROL_ALLOW_HEADERS, null)
        assertFalse containsHeaderWithValue(responseContext.getHeaders(), VARY_HEADER, null)
    }


    /**
     * Check that the given headers list contains the given header with the given value.
     * If the value, is null, only checks if the header is present.
     *
     * @param headers
     * @param header
     * @param value
     * @return
     */
    private boolean containsHeaderWithValue(MultivaluedMap<String, String> headers, String header, String value) {
        return headers.getFirst(header) && (value ? headers.getFirst(header) == value : true)
    }


    private ContainerRequestContext getRequestContextMock(final String methodValue, final String originValue, final String requestMethodValue,
            final String requestHeadersValue) {

        MultivaluedMap<String, String> headers = new MultivaluedHashMap<String, String>()
        if(originValue) {
            headers.put(ORIGIN_HEADER, [originValue])
        }
        if(requestMethodValue) {
            headers.put(ACCESS_CONTROL_REQUEST_METHOD, [requestMethodValue])
        }
        if(requestHeadersValue) {
            headers.put(ACCESS_CONTROL_REQUEST_HEADERS, [requestHeadersValue])
        }

        return [
            getHeaders : { return headers },
            getMethod: {return methodValue }
        ] as ContainerRequestContext
    }


    private ContainerResponseContext getResponseContextMock(final String varyHeaderValue) {

        MultivaluedMap<String, String> headers = new MultivaluedHashMap<String, String>()
        if(varyHeaderValue) {
            headers.put(VARY_HEADER, [varyHeaderValue])
        }

        return [
            getHeaders : { return headers },
            getStringHeaders: { return headers }
        ] as ContainerResponseContext
    }
}
