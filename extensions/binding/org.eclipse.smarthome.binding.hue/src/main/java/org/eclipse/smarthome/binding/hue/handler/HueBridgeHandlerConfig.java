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
package org.eclipse.smarthome.binding.hue.handler;

import org.eclipse.jdt.annotation.Nullable;

/**
 * The config holder for the hue bridge handler.
 *
 * @author David Graeff - Initial contribution
 */
public class HueBridgeHandlerConfig {
    public static final String HOST = "ipAddress";
    public static final String USER_NAME = "userName";
    public static final String POLLING_INTERVAL = "pollingInterval";

    public String ipAddress = "";
    public @Nullable String userName;
    /** Polling interval in seconds */
    public int pollingInterval = 10;
}