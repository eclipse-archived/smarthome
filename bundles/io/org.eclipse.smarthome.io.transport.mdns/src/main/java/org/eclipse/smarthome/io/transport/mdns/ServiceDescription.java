/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.mdns;

import java.util.Hashtable;

/**
 * This is a simple data container to keep all details of a service description together.
 * 
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class ServiceDescription {

    public String serviceType;
    public String serviceName;
    public int servicePort;
    public Hashtable<String, String> serviceProperties;

    /**
     * Constructor for a {@link ServiceDescription}, which takes all details as parameters
     * 
     * @param serviceType String service type, like "_smarthome-server._tcp.local."
     * @param serviceName String service name, like "Eclipse SmartHome"
     * @param servicePort Int service port, like 8080
     * @param serviceProperties Hashtable service props, like url = "/rest"
     * @param serviceDescription String service description text, like "Eclipse SmartHome REST interface"
     */
    public ServiceDescription(String serviceType, String serviceName, int servicePort,
            Hashtable<String, String> serviceProperties) {
        this.serviceType = serviceType;
        this.serviceName = serviceName;
        this.servicePort = servicePort;
        this.serviceProperties = serviceProperties;
    }

}
