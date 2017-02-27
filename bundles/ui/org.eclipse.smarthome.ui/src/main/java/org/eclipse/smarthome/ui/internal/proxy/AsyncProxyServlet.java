/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.internal.proxy;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * This version of the proxy servlet uses asynchronous I/O and request processing, and is based on Jetty's proxy
 * servlets. It depends on Servlet API 3.0 or later.
 *
 * @author John Cocula - new version that uses Jetty proxy classes
 */
public class AsyncProxyServlet extends org.eclipse.jetty.proxy.AsyncProxyServlet {

    private static final long serialVersionUID = -4716754591953017795L;

    private final ProxyServletService service;

    AsyncProxyServlet(ProxyServletService service) {
        super();
        this.service = service;
    }

    @Override
    public String getServletInfo() {
        return "Proxy (async)";
    }

    /**
     * {@inheritDoc}
     *
     * Override <code>newHttpClient</code> so we can proxy to HTTPS URIs.
     */
    @Override
    protected HttpClient newHttpClient() {
        return new HttpClient(new SslContextFactory());
    }

    /**
     * {@inheritDoc}
     *
     * Add Basic Authentication header to request if user and password are specified in URI.
     *
     * After Jetty is upgraded past 9.2.9, change to <code>copyRequestHeaders</code> to avoid deprecation warning.
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void copyHeaders(HttpServletRequest clientRequest, Request proxyRequest) {
        super.copyHeaders(clientRequest, proxyRequest);

        service.maybeAppendAuthHeader(service.uriFromRequest(clientRequest), proxyRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String rewriteTarget(HttpServletRequest request) {
        return Objects.toString(service.uriFromRequest(request), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onProxyRewriteFailed(HttpServletRequest request, HttpServletResponse response) {
        service.sendError(request, response);
    }
}
