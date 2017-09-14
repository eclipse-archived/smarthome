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
 * Implement this interface with for example a custom `enum` and define your own capabilities.
 * Capabilities are used to decide which secure socket implementation should be returned by
 * {@link SecureSocketServers}.
 *
 * @see SecureSocketCapabilities
 *
 * @author David Graeff - Initial contribution
 *
 */
public interface SecureSocketCapability {
    String name();
}