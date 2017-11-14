/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.mqtt.sslcontext;

import javax.naming.ConfigurationException;
import javax.net.ssl.SSLContext;

import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;

/**
 * Implement this and provide a {@link SSLContext} instance to be used by the {@link MqttBrokerConnection} for secure
 * Mqtt broker connections where the URL starts with 'ssl://'. Register your implementation with
 * {@link MqttBrokerConnection.setSSLContextProvider}.
 *
 * @author David Graeff - Initial contribution
 */
public interface SSLContextProvider {
    /**
     * Return an {@link SSLContext} to be used by secure Mqtt broker connections. Never return null here. If you are not
     * able to create an {@link SSLContext} instance, fail with a ConfigurationException instead.
     */
    SSLContext getContext() throws ConfigurationException;
}
