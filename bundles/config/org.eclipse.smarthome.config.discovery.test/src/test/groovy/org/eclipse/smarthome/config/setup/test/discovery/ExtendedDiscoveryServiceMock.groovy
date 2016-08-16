/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.setup.test.discovery

import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback
import org.eclipse.smarthome.config.discovery.ExtendedDiscoveryService
import org.eclipse.smarthome.config.discovery.internal.DiscoveryResultImpl
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID


/**
 * The {@link DiscoveryServiceMock} is a mock for a {@link DiscoveryService}
 * which behaves like an @{ExtendedDiscoveryService}.<br>
 *
 * @author Simon Kaufmann - Initial Contribution
 */
class ExtendedDiscoveryServiceMock extends DiscoveryServiceMock implements ExtendedDiscoveryService {

    public DiscoveryServiceCallback discoveryServiceCallback

    public ExtendedDiscoveryServiceMock(thingType, timeout, faulty = false) {
        super(thingType, timeout)
    }

    @Override
    public void setDiscoveryServiceCallback(DiscoveryServiceCallback discoveryServiceCallback) {
        this.discoveryServiceCallback = discoveryServiceCallback
    }

    @Override
    public void startScan() {
        thingDiscovered(new DiscoveryResultImpl(new ThingUID(thingType, "foo"), null, null, null, null, DEFAULT_TTL))
    }
}
