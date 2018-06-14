/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.homematic.internal.communicator;

import java.io.IOException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.binding.homematic.internal.common.HomematicConfig;
import org.eclipse.smarthome.binding.homematic.internal.communicator.client.RpcClient;
import org.eclipse.smarthome.binding.homematic.internal.communicator.client.XmlRpcClient;

/**
 * Factory which evaluates the type of the Homematic gateway and instantiates the appropriate class.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class HomematicGatewayFactory {

    /**
     * Creates the HomematicGateway.
     */
    public static HomematicGateway createGateway(String id, HomematicConfig config,
            HomematicGatewayAdapter gatewayAdapter, HttpClient httpClient)
            throws IOException {
        loadGatewayInfo(config, id, httpClient);
        if (config.getGatewayInfo().isCCU()) {
            return new CcuGateway(id, config, gatewayAdapter, httpClient);
        } else if (config.getGatewayInfo().isHomegear()) {
            return new HomegearGateway(id, config, gatewayAdapter, httpClient);
        } else {
            return new DefaultGateway(id, config, gatewayAdapter, httpClient);
        }
    }

    /**
     * Loads some metadata about the type of the Homematic gateway.
     */
    private static void loadGatewayInfo(HomematicConfig config, String id, HttpClient httpClient) throws IOException {
        RpcClient<String> rpcClient = new XmlRpcClient(config, httpClient);
        try {
            config.setGatewayInfo(rpcClient.getGatewayInfo(id));
        } finally {
            rpcClient.dispose();
        }
    }

}
