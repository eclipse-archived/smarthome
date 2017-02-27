/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.mdns;

import java.io.IOException;
import java.util.Set;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

/**
 * This interface defines how to get an JmDNS instance
 * to access Bonjour/MDNS
 *
 * @author Tobias Br�utigam - Initial contribution and API
 */
public interface MDNSClient {

    /**
     * This method returns the set of JmDNS instances
     *
     * @return a set of JmDNS instances
     */
    public Set<JmDNS> getClientInstances();

    /**
     * Listen for services of a given type
     *
     * @param type: full qualified service type
     * @param listener: listener for service updates
     */
    public void addServiceListener(String type, ServiceListener listener);

    /**
     * Remove listener for services of a given type
     *
     * @param type: full qualified service type
     * @param listener: listener for service updates
     */
    public void removeServiceListener(String type, ServiceListener listener);

    /**
     * Register a service
     *
     * @param description: service to register, described by (@link ServiceDescription)
     */
    public void registerService(ServiceDescription description) throws IOException;

    /**
     * Unregister a service. The service should have been registered.
     *
     * @param description: service to remove, described by (@link ServiceDescription)
     */
    public void unregisterService(ServiceDescription description);

    /**
     * Unregister all services
     *
     */
    public void unregisterAllServices();

    /**
     * Returns a list of service infos of the specified type
     *
     * @param type: service type name
     *
     * @return an array of service instance
     */
    public ServiceInfo[] list(String type);

    /**
     * Close properly JmDNS instances
     *
     */
    public void close();
}
