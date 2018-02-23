/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.io.net.http.internal;

import java.net.CookieStore;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Destination;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.util.Fields;
import org.eclipse.smarthome.io.net.http.CommonHttpClient;

/**
 * A delegate to jetty {@link HttpClient}
 * 
 * @author Michael Bock - initial API
 */
public class HttpClientDelegate implements CommonHttpClient {

    private HttpClient delegate;

    public HttpClientDelegate(HttpClient delegate) {
        this.delegate = delegate;
    }

    @Override
    public CookieStore getCookieStore() {
        return delegate.getCookieStore();
    }

    @Override
    public AuthenticationStore getAuthenticationStore() {
        return delegate.getAuthenticationStore();
    }

    @Override
    public ContentResponse GET(String uri) throws InterruptedException, ExecutionException, TimeoutException {
        return delegate.GET(uri);
    }

    @Override
    public ContentResponse GET(URI uri) throws InterruptedException, ExecutionException, TimeoutException {
        return delegate.GET(uri);
    }

    @Override
    public ContentResponse FORM(String uri, Fields fields)
            throws InterruptedException, ExecutionException, TimeoutException {
        return delegate.FORM(uri, fields);
    }

    @Override
    public ContentResponse FORM(URI uri, Fields fields)
            throws InterruptedException, ExecutionException, TimeoutException {
        return delegate.FORM(uri, fields);
    }

    @Override
    public Request POST(String uri) {
        return delegate.POST(uri);
    }

    @Override
    public Request POST(URI uri) {
        return delegate.POST(uri);
    }

    @Override
    public Request newRequest(String host, int port) {
        return delegate.newRequest(host, port);
    }

    @Override
    public Request newRequest(String uri) {
        return delegate.newRequest(uri);
    }

    @Override
    public Request newRequest(URI uri) {
        return delegate.newRequest(uri);
    }

    @Override
    public Destination getDestination(String scheme, String host, int port) {
        return delegate.getDestination(scheme, host, port);
    }

    @Override
    public boolean isDefaultPort(String scheme, int port) {
        return delegate.isDefaultPort(scheme, port);
    }

}
