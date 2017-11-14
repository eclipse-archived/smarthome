/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.test.filter;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.core.MultivaluedHashMap
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.UriInfo

import org.eclipse.smarthome.io.rest.internal.filter.ProxyFilter
import org.glassfish.jersey.uri.internal.JerseyUriBuilder
import org.junit.Test

class ProxyFilterTest {

    private static final String PROTO_PROXY_HEADER = "x-forwarded-proto";

    private static final String HOST_PROXY_HEADER = "x-forwarded-host";

    private static final String TEST_PROTO_1 = "https";

    private static final String TEST_PROTO_2 = "http";

    private static final String TEST_HOST = "eclipse.org";

    private static final String TEST_HOST_WITH_PORT = "eclipse.org:8081";

    private static final String INVALID_HOST = "://sometext\\///";

    private static final String DEFAULT_REQUEST_URI = "http://localhost:8080/rest/test";

    private static final String DEFAULT_BASE_URI = "http://localhost:8080/rest";

    private static final String DEFAULT_REQUEST_URI_1 = "http://localhost/rest/test";

    private static final String DEFAULT_BASE_URI_1 = "http://localhost/rest";

    private static final String DEFAULT_REQUEST_PATH = "/test";

    private static final String DEFAULT_BASE_PATH = "/rest";



    private static final ProxyFilter filter = new ProxyFilter();

    @Test
    public void basicTest() {
        ContainerRequestContext context = getContextMock(TEST_PROTO_1, TEST_HOST,DEFAULT_REQUEST_URI, DEFAULT_BASE_URI)

        URI newBaseURI = new URI(TEST_PROTO_1+"://" + TEST_HOST + DEFAULT_BASE_PATH);

        URI newRequestURI = new URI(TEST_PROTO_1+"://" + TEST_HOST+ DEFAULT_BASE_PATH + DEFAULT_REQUEST_PATH);

        filter.filter(context)

        assertThat newBaseURI.equals(context.getUriInfo().getBaseUri()), is(true)
        assertThat newRequestURI.equals(context.getUriInfo().getRequestUri()), is(true)
    }

    @Test
    public void basicTest2() {
        ContainerRequestContext context = getContextMock(TEST_PROTO_2, TEST_HOST_WITH_PORT,DEFAULT_REQUEST_URI, DEFAULT_BASE_URI)

        URI newBaseURI = new URI(TEST_PROTO_2+"://" + TEST_HOST_WITH_PORT + DEFAULT_BASE_PATH);

        URI newRequestURI = new URI(TEST_PROTO_2+"://" + TEST_HOST_WITH_PORT+ DEFAULT_BASE_PATH + DEFAULT_REQUEST_PATH);

        filter.filter(context)

        assertThat newBaseURI.equals(context.getUriInfo().getBaseUri()), is(true)
        assertThat newRequestURI.equals(context.getUriInfo().getRequestUri()), is(true)
    }


    @Test
    public void noHeaderTest() {
        ContainerRequestContext context = getContextMock(null, null,DEFAULT_REQUEST_URI, DEFAULT_BASE_URI)

        URI newBaseURI = new URI(DEFAULT_BASE_URI);

        URI newRequestURI = new URI(DEFAULT_REQUEST_URI);

        filter.filter(context)

        assertThat newBaseURI.equals(context.getUriInfo().getBaseUri()), is(true)
        assertThat newRequestURI.equals(context.getUriInfo().getRequestUri()), is(true)
    }

    @Test
    public void onlySchemeTest() {
        ContainerRequestContext context = getContextMock(TEST_PROTO_1, null,DEFAULT_REQUEST_URI, DEFAULT_BASE_URI)

        URI newBaseURI = new URI(DEFAULT_BASE_URI.replace(TEST_PROTO_2, TEST_PROTO_1));

        URI newRequestURI = new URI(DEFAULT_REQUEST_URI.replace(TEST_PROTO_2, TEST_PROTO_1));

        filter.filter(context)

        assertThat newBaseURI.equals(context.getUriInfo().getBaseUri()), is(true)
        assertThat newRequestURI.equals(context.getUriInfo().getRequestUri()), is(true)
    }

    @Test
    public void onlySchemeDefaultHostWithoutPortTest() {
        ContainerRequestContext context = getContextMock(TEST_PROTO_1, null,DEFAULT_REQUEST_URI_1, DEFAULT_BASE_URI_1)

        URI newBaseURI = new URI(DEFAULT_BASE_URI_1.replace(TEST_PROTO_2, TEST_PROTO_1));

        URI newRequestURI = new URI(DEFAULT_REQUEST_URI_1.replace(TEST_PROTO_2, TEST_PROTO_1));

        filter.filter(context)

        assertThat newBaseURI.equals(context.getUriInfo().getBaseUri()), is(true)
        assertThat newRequestURI.equals(context.getUriInfo().getRequestUri()), is(true)
    }

    @Test
    public void onlyHostTest() {
        ContainerRequestContext context = getContextMock(null, TEST_HOST_WITH_PORT,DEFAULT_REQUEST_URI_1, DEFAULT_BASE_URI_1)

        URI newBaseURI = new URI(DEFAULT_BASE_URI.replace("localhost:8080", TEST_HOST_WITH_PORT));

        URI newRequestURI = new URI(DEFAULT_REQUEST_URI.replace("localhost:8080", TEST_HOST_WITH_PORT));

        filter.filter(context)

        assertThat newBaseURI.equals(context.getUriInfo().getBaseUri()), is(true)
        assertThat newRequestURI.equals(context.getUriInfo().getRequestUri()), is(true)
    }

    @Test
    public void invalidHeaderTest() {
        ContainerRequestContext context = getContextMock(TEST_PROTO_1, INVALID_HOST,DEFAULT_REQUEST_URI, DEFAULT_BASE_URI)

        URI newBaseURI = new URI(DEFAULT_BASE_URI);

        URI newRequestURI = new URI(DEFAULT_REQUEST_URI);

        filter.filter(context)

        assertThat newBaseURI.equals(context.getUriInfo().getBaseUri()), is(true)
        assertThat newRequestURI.equals(context.getUriInfo().getRequestUri()), is(true)
    }


    private ContainerRequestContext getContextMock(final String protoHeader, final String hostHeader,
            final String defaultRequestURI, final String defaultBaseURI) {


        final uriInfo = [
            requestURI : new URI(defaultRequestURI),
            baseURI : new URI(defaultBaseURI),
            getBaseUriBuilder : {
                return (new JerseyUriBuilder()).uri(defaultBaseURI)
            },
            getRequestUriBuilder: {
                return (new JerseyUriBuilder()).uri(defaultRequestURI)
            }
        ] ;

        uriInfo.getRequestUri = { return uriInfo.requestURI }
        uriInfo.getBaseUri = { return uriInfo.baseURI }


        return [

            getHeaders : {
                MultivaluedMap<String, String> headers = new MultivaluedHashMap<String, String>();
                if(protoHeader != null) {
                    headers.put(PROTO_PROXY_HEADER, [protoHeader])
                }
                if(hostHeader != null) {
                    headers.put(HOST_PROXY_HEADER, [hostHeader])
                }

                return headers;
            },

            getUriInfo: { return uriInfo as UriInfo },

            setRequestUri : { URI baseURI, URI requestURI ->
                uriInfo.baseURI = baseURI
                uriInfo.requestURI = requestURI
            }
        ] as ContainerRequestContext
    }
}
