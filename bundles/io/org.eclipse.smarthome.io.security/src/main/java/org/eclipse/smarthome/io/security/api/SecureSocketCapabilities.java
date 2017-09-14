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

/**
 * A set of default capabilities. Implement SecureSocketCapability and define your own for additional capabilities.
 */
public enum SecureSocketCapabilities implements SecureSocketCapability {
    /** Capability: Request a TLS capable secure socket implementation */
    TLS,
    /**
     * Capability: Request a DTLS capable secure socket implementation.
     * On Java 8 this is usually a Scandium DtlsConnector. On Java 9 it is a SSLContext.
     */
    DTLS,
    /** Capability: Request a secure socket implementation that has at least one self-signed certificate. */
    SELF_SIGNED,
    /** Capability: Request a secure socket implementation that has at least one (valid) CA signed certificate. */
    CA_SIGNED,
    /** Capability: Request a secure socket implementation that is a javax.net.ssl.SSLContext */
    SSLCONTEXT,
    /** Capability: Request a secure socket implementation that is a Scandium Connector. Usually a DTLSConnector. */
    SCANDIUM_CONNECTOR
}