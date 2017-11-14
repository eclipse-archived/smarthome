/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery;

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
public interface ExtendedDiscoveryService {

    /**
     * Provides the callback, which a {@link DiscoveryService} may use in order to access core features.
     *
     * @param discoveryServiceCallback
     */
    public void setDiscoveryServiceCallback(DiscoveryServiceCallback discoveryServiceCallback);

}
