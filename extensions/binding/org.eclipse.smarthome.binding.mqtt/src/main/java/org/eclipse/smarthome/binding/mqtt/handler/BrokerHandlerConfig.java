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
package org.eclipse.smarthome.binding.mqtt.handler;

import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnectionConfig;

/**
 * Holds the configuration of a {@link BrokerHandler} Thing. Parameters are inherited
 * from {@link MqttBrokerConnectionConfig}, Additionally some
 * reconnect and security related parameters are defined here.
 *
 * @author David Graeff - Initial contribution
 */
public class BrokerHandlerConfig extends MqttBrokerConnectionConfig {
    public Integer reconnectTime;
    public Integer timeoutInMs;

    // For more security, the following optional parameters can be altered

    public boolean certificatepin = false;
    public boolean publickeypin = false;
    public String certificate;
    public String publickey;
}
