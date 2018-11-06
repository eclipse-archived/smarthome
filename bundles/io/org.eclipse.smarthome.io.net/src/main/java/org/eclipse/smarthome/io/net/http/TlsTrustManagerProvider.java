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

import javax.net.ssl.X509ExtendedTrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Provides a trust manager for the given host name
 *
 * Implement this interface to request the framework to use a specific trust manager for the given host
 *
 * @author Martin van Wingerden - Initial Contribution
 */
@NonNullByDefault
public interface TlsTrustManagerProvider extends TlsProvider {

    /**
     * A X509ExtendedTrustManager for the specified host name
     *
     * Note that the implementation might call this method multiple times make sure to return the same instance in that
     * case
     *
     * @return this can for example be a trustManager extracted after importing a jks trust-store
     */
    X509ExtendedTrustManager getTrustManager();
}
