/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.dto;

import java.util.List;

import org.eclipse.smarthome.config.core.ConfigDescription;

/**
 * {@link ConfigDescriptionDTO} is a data transfer object for {@link ConfigDescription}.
 *
 * @author Dennis Nobel - Initial contribution
 *
 */
public class ConfigDescriptionDTO {

    public String uri;

    public List<ConfigDescriptionParameterDTO> parameters;

    public List<ConfigDescriptionParameterGroupDTO> parameterGroups;

    public ConfigDescriptionDTO(String uri, List<ConfigDescriptionParameterDTO> parameters,
            List<ConfigDescriptionParameterGroupDTO> parameterGroups) {
        this.uri = uri;
        this.parameters = parameters;
        this.parameterGroups = parameterGroups;
    }

}
