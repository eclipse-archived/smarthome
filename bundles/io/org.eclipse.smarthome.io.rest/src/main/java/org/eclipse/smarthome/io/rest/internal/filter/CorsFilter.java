/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.internal.filter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.io.rest.internal.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * A PostMatching filter used to add CORS HTTP headers on responses for requests with CORS
 * headers.
 *
 * Based on http://www.w3.org/TR/cors
 *
 * This implementation does not allow specific request/response headers nor cookies (allowCredentials).
 *
 * @author Antoine Besnard - Initial contribution
 *
 */
@Provider
public class CorsFilter implements ContainerResponseFilter {

    private static final String HTTP_HEAD_METHOD = "HEAD";
    private static final String HTTP_DELETE_METHOD = "DELETE";
    private static final String HTTP_PUT_METHOD = "PUT";
    private static final String HTTP_POST_METHOD = "POST";
    private static final String HTTP_GET_METHOD = "GET";
    private static final String HTTP_OPTIONS_METHOD = "OPTIONS";

    private static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
    private static final String ACCESS_CONTROL_ALLOW_METHODS_HEADER = "Access-Control-Allow-Methods";
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
    private static final String ORIGIN_HEADER = "Origin";
    private static final String VARY_HEADER = "Vary";

    private static final String VARY_HEADER_WILDCARD = "*";
    private static final String HEADERS_SEPARATOR = ",";

    private static final List<String> ACCEPTED_HTTP_METHODS_LIST = Lists.newArrayList(HTTP_GET_METHOD, HTTP_POST_METHOD,
            HTTP_PUT_METHOD, HTTP_DELETE_METHOD, HTTP_HEAD_METHOD, HTTP_OPTIONS_METHOD);

    private static final String ACCEPTED_HTTP_METHODS = Joiner.on(HEADERS_SEPARATOR).join(ACCEPTED_HTTP_METHODS_LIST);

    private final transient Logger logger = LoggerFactory.getLogger(CorsFilter.class);

    private boolean isEnabled;

    public CorsFilter() {
        // Disable the filter by default
        this.isEnabled = false;
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {

        if (isEnabled && !processPreflight(requestContext, responseContext)) {
            processRequest(requestContext, responseContext);
        }

    }

    /**
     * Process the CORS request and response.
     *
     * @param requestContext
     * @param responseContext
     */
    private void processRequest(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        // Process the request only if if is an acceptable request method and if it is different from an OPTIONS request
        // (OPTIONS requests are not processed here)
        if (ACCEPTED_HTTP_METHODS_LIST.contains(requestContext.getMethod())
                && !HTTP_OPTIONS_METHOD.equals(requestContext.getMethod())) {

            String origin = getValue(requestContext.getHeaders(), ORIGIN_HEADER);
            if (StringUtils.isNotBlank(origin)) {
                responseContext.getHeaders().add(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, origin);
            }
        }
    }

    /**
     * Process a preflight CORS request.
     *
     * @param requestContext
     * @param responseContext
     * @return true if it is a preflight request that has been processed.
     */
    private boolean processPreflight(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        boolean isCorsPreflight = false;

        if (HTTP_OPTIONS_METHOD.equals(requestContext.getMethod())) {

            // Look for the mandatory CORS preflight request headers
            String origin = getValue(requestContext.getHeaders(), ORIGIN_HEADER);
            String realRequestMethod = getValue(requestContext.getHeaders(), ACCESS_CONTROL_REQUEST_METHOD);
            isCorsPreflight = StringUtils.isNotBlank(origin) && StringUtils.isNotBlank(realRequestMethod);

            if (isCorsPreflight) {
                responseContext.getHeaders().add(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, origin);
                responseContext.getHeaders().add(ACCESS_CONTROL_ALLOW_METHODS_HEADER, ACCEPTED_HTTP_METHODS);

                // Add the accepted request headers
                appendVaryHeader(responseContext);

            }
        }

        return isCorsPreflight;
    }

    /**
     * Get the first value of a header which may contains several values.
     *
     * @param headers
     * @param header
     * @return The first value from the given header or null if the header is
     *         not found.
     *
     */
    private String getValue(MultivaluedMap<String, String> headers, String header) {
        List<String> values = headers.get(header);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0).toString();
    }

    /**
     * Append the Vary header if necessary to the response.
     *
     * @param responseContext
     */
    private void appendVaryHeader(ContainerResponseContext responseContext) {
        String varyHeader = getValue(responseContext.getStringHeaders(), VARY_HEADER);
        if (StringUtils.isBlank(varyHeader)) {
            // If the Vary header is not present, just add it.
            responseContext.getHeaders().add(VARY_HEADER, ORIGIN_HEADER);
        } else if (!VARY_HEADER_WILDCARD.equals(varyHeader)) {
            // If it is already present and its value is not the Wildcard, append the Origin header.
            responseContext.getHeaders().putSingle(VARY_HEADER, varyHeader + HEADERS_SEPARATOR + ORIGIN_HEADER);
        }
    }

    protected void activate(Map<String, Object> properties) {
        if (properties != null) {
            String corsPropertyValue = (String) properties.get(Constants.CORS_PROPERTY);
            this.isEnabled = "true".equalsIgnoreCase(corsPropertyValue);
        }

        if(this.isEnabled) {
            logger.info("enabled CORS for REST API.");
        }
    }

}
