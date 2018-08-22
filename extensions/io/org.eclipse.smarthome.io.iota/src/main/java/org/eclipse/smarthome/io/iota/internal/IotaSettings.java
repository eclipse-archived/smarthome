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
package org.eclipse.smarthome.io.iota.internal;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IotaSettings} provides the configured and static settings for the IOTA addon
 *
 * @author Theo Giovanna - Initial Contribution
 */
public class IotaSettings {

    // Default node for MAM
    private int port = 443;
    private String protocol = "https";
    private String host = "nodes.testnet.iota.org";
    private final Logger logger = LoggerFactory.getLogger(IotaSettings.class);

    public void fill(IotaApiConfiguration config) {
        logger.debug("Updating settings for IOTA API...");
        setHost(getOrDefault(config.getHost(), getHost()));
        setProtocol(getOrDefault(config.getProtocol(), getProtocol()));
        setPort(getOrDefault(config.getPort(), getPort()));
    }

    private static String getOrDefault(Object value, String defaultValue) {
        return value != null ? (String) value : defaultValue;
    }

    private static int getOrDefault(Object value, int defaultValue) {
        return value instanceof BigDecimal ? ((BigDecimal) value).intValue() : defaultValue;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

}
