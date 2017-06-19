/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.tradfri;

/**
 * Configuration class for the gateway.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class GatewayConfig {

    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String CODE = "code";

    public String host;
    public int port = 5684; // default port
    public String code;
}
