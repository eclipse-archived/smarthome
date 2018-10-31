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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Provides some TLS validation implementation for the given host name
 *
 * Normally you would implement one of children of this interface, in order to request the framework to use a specific
 * implementation for the given host.
 *
 * @author Martin van Wingerden - Initial Contribution
 */
@NonNullByDefault
public interface TlsProvider {
    /**
     * Host name for which this tls-provider is intended
     *
     * @return a host name in string format, eg: www.eclipse.org
     */
    String getHostName();
}
