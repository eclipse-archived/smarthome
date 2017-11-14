/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.dto;

/**
 * This is a data transfer object that is used to serialize options of a
 * parameter group.
 *
 * @author Chris Jackson - Initial contribution
 *
 */
public class ConfigDescriptionParameterGroupDTO {

    public String name;
    public String context;
    public Boolean advanced;
    public String label;
    public String description;

    public ConfigDescriptionParameterGroupDTO() {
    }

    public ConfigDescriptionParameterGroupDTO(String name, String context, Boolean advanced, String label, String description) {
        this.name = name;
        this.context = context;
        this.advanced = advanced;
        this.label = label;
        this.description = description;
    }
}
