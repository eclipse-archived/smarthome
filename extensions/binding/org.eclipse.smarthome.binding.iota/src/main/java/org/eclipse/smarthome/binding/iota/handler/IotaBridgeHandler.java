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
package org.eclipse.smarthome.binding.iota.handler;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.binding.iota.IotaBindingConstants;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.iota.utils.IotaUtilsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IotaBridgeHandler} is responsible for handling the
 * bridge over IOTA's distributed ledger, the Tangle.
 *
 * @author Theo Giovanna - Initial Contribution
 */
public class IotaBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(IotaBridgeHandler.class);
    private int port;
    private String protocol;
    private String host;
    private IotaUtilsImpl utils;

    public IotaBridgeHandler(Bridge bridge, IotaUtilsImpl utilsImpl) {
        super(bridge);
        this.utils = utilsImpl;
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        // not needed
    }

    @Override
    public void initialize() {
        logger.debug("Initializing IOTA bridge");
        Configuration configuration = getThing().getConfiguration();
        setHost((String) configuration.get(IotaBindingConstants.HOST));
        setProtocol((String) configuration.get(IotaBindingConstants.PROTOCOL));
        int port = configuration.get(IotaBindingConstants.PORT) == null ? 0
                : ((BigDecimal) configuration.get(IotaBindingConstants.PORT)).intValue();
        setPort(port);

        try {
            this.utils.setIotaAPI(protocol, host, port);
        } catch (IllegalStateException e) {
            logger.warn("Wrong configuration detected: please check your configuration {}", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Node unreachable");
        }

        if (utils.checkAPI()) {
            logger.debug("IOTA API checked, connexion successfull: bridge can be set ONLINE");
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Node unreachable");
        }
    }

    protected void setPort(int port) {
        this.port = port;
    }

    protected int getPort() {
        return this.port;
    }

    protected void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    protected String getProtocol() {
        return this.protocol;
    }

    protected void setHost(String host) {
        this.host = host;
    }

    protected String getHost() {
        return this.host;
    }

    public IotaUtilsImpl getUtils() {
        return utils;
    }

    protected void setUtils(IotaUtilsImpl utils) {
        this.utils = utils;
    }
}
