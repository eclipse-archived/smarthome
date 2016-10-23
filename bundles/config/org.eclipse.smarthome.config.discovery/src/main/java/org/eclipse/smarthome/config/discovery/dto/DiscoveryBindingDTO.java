/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery.dto;

/**
 * Data transfer object for serialising discovery binding information
 *
 * @author Chris Jackson - Initial implementation
 *
 */
public class DiscoveryBindingDTO {
    /**
     * Binding id
     */
    public String id;

    /**
     * Binding discovery timeout
     */
    public Integer timeout;
}
