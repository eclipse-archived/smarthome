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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration of the {@link EmbeddedBrokerServiceImpl}.
 *
 * @author David Graeff - Initial contribution
 */
public class ServiceConfiguration {
    public @Nullable Integer port;
    public @NonNull Boolean secure = false;
    public @NonNull String persistenceFile = "mqttembedded.bin";

    public @Nullable String username;
    public @Nullable String password;
}
