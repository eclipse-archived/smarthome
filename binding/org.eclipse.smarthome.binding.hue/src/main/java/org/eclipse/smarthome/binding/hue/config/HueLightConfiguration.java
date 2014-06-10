/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.config;

import org.eclipse.smarthome.binding.hue.internal.handler.HueLightHandler;

/**
 * Configuration class for {@link HueLightHandler}.
 * 
 * @author Dennis Nobel - Initial contribution of hue binding
 * 
 */
public class HueLightConfiguration {

    /**
     * Light identifier
     */
    public String lightId;
    
    public static final String LIGHT_ID = "lightId";

}