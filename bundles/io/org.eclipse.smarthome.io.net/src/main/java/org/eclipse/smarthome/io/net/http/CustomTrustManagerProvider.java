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

import java.util.stream.Stream;

import javax.net.ssl.TrustManager;

/**
 * Service to get custom trust managers for a given endpoint
 * 
 * @author Michael Bock - initial API
 */
public interface CustomTrustManagerProvider {

    /**
     * Provides a (potentially empty) list of trust managers to be used for an endpoint.
     * If the list is empty, the default java trust managers should be used.
     * 
     * @param endpoint the desired endpoint, protocol and host are sufficient
     * @return a (potentially empty) list of trust managers
     */
    Stream<TrustManager> getTrustManagers(String endpoint);
}
