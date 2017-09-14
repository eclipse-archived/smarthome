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
package org.eclipse.smarthome.io.security.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A central service interface to provide SSL/DTLS configuration for server-like services within ESH and derived
 * products.
 * An implementation is required to return on a best effort approach a working SSLContext, if required with a
 * self-signed certificate, so that all of those services can provide a secure TLS or DTLS based endpoint from the first
 * start on.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public interface SecureSocketServers {
    /**
     * To retrieve a working secure socket implementation (usually a SSLContext object), you would call this
     * method and get an asynchronous callback as soon as the context is ready. It can take a while (some seconds)
     * to resolve a new SSLContext if there is none so far.
     *
     * The given callback object has a second purpose: You can describe what features your secure socket implementation
     * should fulfil (mandatoryCapabilities) and what features are nice to have (optionalCapabilities).
     *
     * For example if you need a Scandium DtlsConnector, you would return
     * {SecureSocketRequest.SCANDIUM_CONNECTOR,SecureSocketRequest.DTLS} for mandatoryCapabilities().
     *
     * The behaviour is undefined if you specify two contrary features like SecureSocketRequest.SCANDIUM_CONNECTOR
     * and SecureSocketRequest.SSLCONTEXT.
     */
    void requestSecureSocketImplementation(SecureSocketRequest consumer);
}
