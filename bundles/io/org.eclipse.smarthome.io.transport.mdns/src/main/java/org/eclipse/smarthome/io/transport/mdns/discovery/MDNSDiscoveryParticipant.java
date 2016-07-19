/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.mdns.discovery;

import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;

/**
 * A {@link MDNSDiscoveryParticipant} that is registered as a service is picked up by the {@link MDNSDiscoveryService}
 * and can thus contribute {@link DiscoveryResult}s from
 * mDNS scans.
 *
 * @author Tobias Bräutigam - Initial contribution
 *
 */
public interface MDNSDiscoveryParticipant {

    /**
     * Defines the list of thing types that this participant can identify
     * 
     * @return a set of thing type UIDs for which results can be created
     */
    public Set<ThingTypeUID> getSupportedThingTypeUIDs();

    /**
     * Defines the mDNS service type this participant listens to
     * 
     * @return a valid mDNS service type (see: http://www.dns-sd.org/ServiceTypes.html)
     */
    public String getServiceType();

    /**
     * Creates a discovery result for a mDNS service
     * 
     * @param device the mDNS service found on the network
     * 
     * @return the according discovery result or <code>null</code>, if device is not
     *         supported by this participant
     */
    public DiscoveryResult createResult(ServiceInfo service);

    /**
     * Returns the thing UID for a mDNS service
     * 
     * @param device the mDNS service on the network
     * 
     * @return a thing UID or <code>null</code>, if device is not supported
     *         by this participant
     */
    public ThingUID getThingUID(ServiceInfo service);
}
