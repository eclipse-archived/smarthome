/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.mdns;

/**
 * This interface defines how to use JmDNS based service discovery
 * to register and unregister services on Bonjour/MDNS
 *
 * @author Victor Belov
 * @author Kai Kreuzer - Initial contribution and API
 */
public interface MDNSService {

    /**
     * This method registers a service to be announced through Bonjour/MDNS
     * 
     * @param serviceDescription the {@link ServiceDescription} instance with all details to identify the service
     */
    public void registerService(ServiceDescription description);

    /**
     * This method unregisters a service not to be announced through Bonjour/MDNS
     * 
     * @param serviceDescription the {@link ServiceDescription} instance with all details to identify the service
     */
    public void unregisterService(ServiceDescription description);

}
