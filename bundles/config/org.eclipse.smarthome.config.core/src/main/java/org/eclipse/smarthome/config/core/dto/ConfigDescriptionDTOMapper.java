/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterGroup;

/**
 * {@link ConfigDescriptionDTOMapper} maps {@link ConfigDescription}s to the data transfer object
 * {@link ConfigDescriptionDTO}.
 *
 * @author Dennis Nobel - Initial contribution
 *
 */
public class ConfigDescriptionDTOMapper {

    /**
     * Maps config description into config description DTO object.
     *
     * @param configDescription the config description (not null)
     * @return the config description DTO object
     */
    public static ConfigDescriptionDTO map(ConfigDescription configDescription) {
        List<ConfigDescriptionParameterGroupDTO> parameterGroups = mapParameterGroups(
                configDescription.getParameterGroups());
        List<ConfigDescriptionParameterDTO> parameters = mapParameters(configDescription.getParameters());
        return new ConfigDescriptionDTO(configDescription.getURI().toString(), parameters, parameterGroups);
    }

    /**
     * Maps config description parameters into DTO objects.
     *
     * @param parameters the config description parameters (not null)
     *
     * @return the parameter DTO objects (not null)
     */
    public static List<ConfigDescriptionParameterDTO> mapParameters(List<ConfigDescriptionParameter> parameters) {
        return parameters.stream().map(ConfigDescriptionParameterDTOMapper::map).collect(Collectors.toList());
    }

    /**
     * Maps config description parameter groups into DTO objects.
     *
     * @param parameterGroups the config description parameter groups (not null)
     *
     * @return the parameter group DTO objects (not null)
     */
    public static List<ConfigDescriptionParameterGroupDTO> mapParameterGroups(
            List<ConfigDescriptionParameterGroup> parameterGroups) {

        List<ConfigDescriptionParameterGroupDTO> parameterGroupBeans = new ArrayList<>(parameterGroups.size());

        for (ConfigDescriptionParameterGroup parameterGroup : parameterGroups) {
            parameterGroupBeans
                    .add(new ConfigDescriptionParameterGroupDTO(parameterGroup.getName(), parameterGroup.getContext(),
                            parameterGroup.isAdvanced(), parameterGroup.getLabel(), parameterGroup.getDescription()));
        }

        return parameterGroupBeans;
    }

}
