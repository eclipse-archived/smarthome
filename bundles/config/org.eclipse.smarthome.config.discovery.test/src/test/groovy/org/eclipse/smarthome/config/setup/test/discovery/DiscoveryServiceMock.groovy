/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.setup.test.discovery

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService
import org.eclipse.smarthome.config.discovery.internal.DiscoveryResultImpl
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID


/**
 * The {@link DiscoveryServiceMock} is a mock for a {@link DiscoveryService}
 * which can simulate a working and faulty discovery.<br>
 * If this mock is configured to be faulty, an exception is thrown if the
 * discovery is enforced or aborted. 
 * 
 * @author Michael Grammling - Initial Contribution
 */
class DiscoveryServiceMock extends AbstractDiscoveryService {

    ThingTypeUID thingType
    int timeout
    boolean faulty

    public DiscoveryServiceMock(thingType, timeout, faulty = false) {
        super([thingType] as Set, timeout)     
        this.thingType = thingType   
        this.faulty = faulty
    }

    @Override
    public void startScan() {
        if (faulty) {
            throw new Exception()
        }
        thingDiscovered(new DiscoveryResultImpl(new ThingUID(thingType, 'abc'), null, null, null))
    }

}
