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
    /** Capability: Request a TLS capable secure socket provider */
    TLS,
    /** Capability: Request a DTLS capable secure socket provider. */
    DTLS,
    /** Capability: Request a secure socket implementation that has at least one self-signed certificate. */
    SELF_SIGNED,
    /** Capability: Request a secure socket implementation that has at least one (valid) CA signed certificate. */
    CA_SIGNED,
    /**
     * Capability: Request a secure socket implementation that is a javax.net.ssl.SSLContext.
     * On Java 9 this works for TLS and DTLS. On Java 8 only TLS is supported.
     */
    SSLCONTEXT,
    /**
     * Capability: Request a Scandium DTLSConnector. This is an alternative for Java 8's missing DTLS support.
     * The framework needs the corresponding services to be loaded though.
     */
    SCANDIUM_CONNECTOR
}