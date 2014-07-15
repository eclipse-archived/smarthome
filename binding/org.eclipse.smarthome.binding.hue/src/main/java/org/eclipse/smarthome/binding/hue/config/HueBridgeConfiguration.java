/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.config;

import org.eclipse.smarthome.binding.hue.internal.handler.HueBridgeHandler;

/**
 * Configuration class for {@link HueBridgeHandler}.
 * 
 * @author Dennis Nobel - Initial contribution of hue binding
 */
public class HueBridgeConfiguration {

    public static final String IP_ADDRESS = "ipAddress";
	public static final String USER_NAME = "userName";
	public static final String SERIAL_NUMBER = "serialNumber";
	
    /**
     * IP address of the hue bridge
     */
    public String ipAddress;

    /**
     * User name used to connect to the hue bridge
     */
    public String userName;
    
    /**
     * Serial number of the hue bridge
     */
    public String serialNumber;

}
