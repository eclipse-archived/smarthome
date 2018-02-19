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
package org.eclipse.smarthome.io.net.http;

/**
 * A facade to the jetty http client
 * 
 * @author Michael Bock - initial API
 */
import java.net.CookieStore;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.Connection;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Destination;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.util.Fields;

/**
 * A facade to Jetty {@link HttpClient}
 * 
 * @author Michael Bock - initial API
 */
public interface CommonHttpClient {

    /**
     * @return the cookie store associated with this instance
     */
    CookieStore getCookieStore();

    /**
     * @return the authentication store associated with this instance
     */
    AuthenticationStore getAuthenticationStore();

    /**
     * Performs a GET request to the specified URI.
     *
     * @param uri the URI to GET
     * @return the {@link ContentResponse} for the request
     * @throws InterruptedException if send threading has been interrupted
     * @throws ExecutionException the execution failed
     * @throws TimeoutException the send timed out
     * @see #GET(URI)
     */
    ContentResponse GET(String uri) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * Performs a GET request to the specified URI.
     *
     * @param uri the URI to GET
     * @return the {@link ContentResponse} for the request
     * @throws InterruptedException if send threading has been interrupted
     * @throws ExecutionException the execution failed
     * @throws TimeoutException the send timed out
     * @see #newRequest(URI)
     */
    ContentResponse GET(URI uri) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * Performs a POST request to the specified URI with the given form parameters.
     *
     * @param uri the URI to POST
     * @param fields the fields composing the form name/value pairs
     * @return the {@link ContentResponse} for the request
     * @throws InterruptedException if send threading has been interrupted
     * @throws ExecutionException the execution failed
     * @throws TimeoutException the send timed out
     */
    ContentResponse FORM(String uri, Fields fields) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * Performs a POST request to the specified URI with the given form parameters.
     *
     * @param uri the URI to POST
     * @param fields the fields composing the form name/value pairs
     * @return the {@link ContentResponse} for the request
     * @throws InterruptedException if send threading has been interrupted
     * @throws ExecutionException the execution failed
     * @throws TimeoutException the send timed out
     */
    ContentResponse FORM(URI uri, Fields fields) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * Creates a POST request to the specified URI.
     *
     * @param uri the URI to POST to
     * @return the POST request
     * @see #POST(URI)
     */
    Request POST(String uri);

    /**
     * Creates a POST request to the specified URI.
     *
     * @param uri the URI to POST to
     * @return the POST request
     */
    Request POST(URI uri);

    /**
     * Creates a new request with the "http" scheme and the specified host and port
     *
     * @param host the request host
     * @param port the request port
     * @return the request just created
     */
    Request newRequest(String host, int port);

    /**
     * Creates a new request with the specified absolute URI in string format.
     *
     * @param uri the request absolute URI
     * @return the request just created
     */
    Request newRequest(String uri);

    /**
     * Creates a new request with the specified absolute URI.
     *
     * @param uri the request absolute URI
     * @return the request just created
     */
    Request newRequest(URI uri);

    /**
     * Returns a {@link Destination} for the given scheme, host and port.
     * Applications may use {@link Destination}s to create {@link Connection}s
     * that will be outside {@link HttpClient}'s pooling mechanism, to explicitly
     * control the connection lifecycle (in particular their termination with
     * {@link Connection#close()}).
     *
     * @param scheme the destination scheme
     * @param host the destination host
     * @param port the destination port
     * @return the destination
     * @see #getDestinations()
     */
    Destination getDestination(String scheme, String host, int port);

    boolean isDefaultPort(String scheme, int port);

}