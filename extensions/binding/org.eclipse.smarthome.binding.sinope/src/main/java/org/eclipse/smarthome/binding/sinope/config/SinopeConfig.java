/**
 *
 *  Copyright (c) 2016 by the respective copyright holders.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  @author Pascal Larin
 *  https://github.com/chaton78
 *
*/

package org.eclipse.smarthome.binding.sinope.config;

/**
 * Holds Config for the Sinope Gateway
 *
 * @author Pascal Larin
 *
 */
public class SinopeConfig {
    /**
     * Hostname of the Sinope Gateway
     */
    public String hostname;
    /**
     * ip port
     */
    public Integer port;
    /**
     * Gateway ID
     */
    public String gatewayId;
    /**
     * API Key returned by the Gateway
     */
    public String apiKey;
    /**
     * The number of milliseconds between fetches from the sinope deivces
     */
    public Integer refresh;
}
