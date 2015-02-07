/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.mdns.internal;

import java.io.IOException;

import javax.jmdns.JmDNS;

import org.eclipse.smarthome.io.transport.mdns.MDNSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class starts the JmDNS and implements interface to register and unregister services.
 *
 * @author Victor Belov
 *
 */
public class MDNSClientImpl implements MDNSClient {
    private final Logger logger = LoggerFactory.getLogger(MDNSClientImpl.class);

    private JmDNS jmdns;

    @Override
    public JmDNS getClient() {
        return jmdns;
    }

    public void activate() {
        try {
            jmdns = JmDNS.create();
            logger.debug("mDNS service has been started");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public void deactivate() {
    }
}
