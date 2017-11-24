/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.config.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * By implementing this interface, a {@link DiscoveryService} implementation may indicate that it requires extended
 * access to the core framework.
 *
 * The {@link DiscoveryService} will get a {@link DiscoveryServiceCallback}, which provides the extended framework
 * capabilities.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public interface ExtendedDiscoveryService {

    /**
     * Provides the callback, which a {@link DiscoveryService} may use in order to access core features.
     *
     * @param discoveryServiceCallback
     */
    public void setDiscoveryServiceCallback(DiscoveryServiceCallback discoveryServiceCallback);

}
