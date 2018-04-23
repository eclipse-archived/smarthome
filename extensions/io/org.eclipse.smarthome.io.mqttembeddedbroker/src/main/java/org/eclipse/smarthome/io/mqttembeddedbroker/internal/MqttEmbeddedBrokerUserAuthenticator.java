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
package org.eclipse.smarthome.io.mqttembeddedbroker.internal;

import java.util.Arrays;

import io.moquette.spi.security.IAuthenticator;

/**
 * Provides a {@link IAuthenticator} for the Moquette server, that accepts given user name and password.
 * If ESH gains user credentials at some point, those should be accepted as well.
 * 
 * @author David Graeff - Initial contribution
 */
public class MqttEmbeddedBrokerUserAuthenticator implements IAuthenticator {
    final String username;
    final byte[] password;

    public MqttEmbeddedBrokerUserAuthenticator(String username, byte[] password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean checkValid(String clientId, String username, byte[] password) {
        return this.username.equals(username) && Arrays.equals(this.password, password);
    }
}