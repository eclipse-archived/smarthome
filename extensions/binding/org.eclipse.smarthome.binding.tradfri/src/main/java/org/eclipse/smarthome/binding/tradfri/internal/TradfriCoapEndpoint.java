/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.tradfri.internal;

import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.scandium.DTLSConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class wraps {@link CoapEndpoint} from californium for the sole purpose of adding some debug logging to it in
 * order to figure out when the endpoint is destroyed.
 * See https://github.com/eclipse/californium/pull/452#issuecomment-341703735
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class TradfriCoapEndpoint extends CoapEndpoint {

    private final Logger logger = LoggerFactory.getLogger(TradfriCoapEndpoint.class);

    public TradfriCoapEndpoint(DTLSConnector dtlsConnector, NetworkConfig standard) {
        super(dtlsConnector, standard);
    }

    @Override
    public synchronized void destroy() {
        if (logger.isDebugEnabled()) {
            logger.debug("Destroying CoAP endpoint.", new RuntimeException("Endpoint destroyed"));
        }
        super.destroy();
    }
}
