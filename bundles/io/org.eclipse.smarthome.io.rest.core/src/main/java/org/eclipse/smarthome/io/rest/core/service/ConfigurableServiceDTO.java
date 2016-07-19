/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.service;

/**
 * {@link ConfigurableServiceDTO} is a data transfer object for configurable services.
 *
 * @author Dennis Nobel - Initial contribution
 */
public class ConfigurableServiceDTO {

    public String id;
    public String label;
    public String category;
    public String configDescriptionURI;

    public ConfigurableServiceDTO(String id, String label, String category, String configDescriptionURI) {
        this.id = id;
        this.label = label;
        this.category = category;
        this.configDescriptionURI = configDescriptionURI;
    }

}
