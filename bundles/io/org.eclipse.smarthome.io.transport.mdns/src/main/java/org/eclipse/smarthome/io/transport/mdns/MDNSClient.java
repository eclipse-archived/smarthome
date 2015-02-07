/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.mdns;

import javax.jmdns.JmDNS;

/**
 * This interface defines how to get an JmDNS instance
 * to access Bonjour/MDNS
 *
 * @author Tobias Br�utigam - Initial contribution and API
 */
public interface MDNSClient {

    /**
     * This method returns an jmDNS instance
     * 
     */
    public JmDNS getClient();

}
