/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.discovery;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.config.Config;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.DsAPI;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.impl.DsAPIImpl;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link BridgeDiscoveryService} is responsible for discovering digitalSTROM-Server, if the server is in the
 * local network and is reachable through "dss.local." with default port number "8080". It uses the central
 * {@link AbstractDiscoveryService}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class BridgeDiscoveryService extends AbstractDiscoveryService {

    private Logger logger = LoggerFactory.getLogger(BridgeDiscoveryService.class);
    private final String HOST_ADDRESS = "dss.local.";

    private Runnable resultCreater = new Runnable() {

        @Override
        public void run() {
            createResult();
        }

        private void createResult() {
            ThingUID uid = getThingUID();

            if (uid != null) {
                Map<String, Object> properties = new HashMap<>(2);
                properties.put(DigitalSTROMBindingConstants.HOST, HOST_ADDRESS);
                properties.put(DigitalSTROMBindingConstants.DS_ID, uid.getId());
                DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                        .withLabel("digitalSTROM-Server").build();
                thingDiscovered(result);
            }
        }

        private ThingUID getThingUID() {
            DsAPI digitalSTROMClient = new DsAPIImpl(HOST_ADDRESS, Config.DEFAULT_CONNECTION_TIMEOUT,
                    Config.DEFAULT_READ_TIMEOUT, true);
            switch (digitalSTROMClient.checkConnection("123")) {
                case HttpURLConnection.HTTP_OK:
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                case HttpURLConnection.HTTP_FORBIDDEN:
                    String dSID = digitalSTROMClient.getDSID("123");
                    if (StringUtils.isNotBlank(dSID)) {
                        return new ThingUID(DigitalSTROMBindingConstants.THING_TYPE_DSS_BRIDGE, dSID);
                    } else {
                        logger.error("Can't get server dSID to generate ThingUID. Please add the server manually.");
                    }
            }
            return null;
        }
    };

    /**
     * Creates a new {@link BridgeDiscoveryService}.
     */
    public BridgeDiscoveryService() {
        super(Sets.newHashSet(DigitalSTROMBindingConstants.THING_TYPE_DSS_BRIDGE), 10, false);
    }

    @Override
    protected void startScan() {
        scheduler.execute(resultCreater);
    }
}
