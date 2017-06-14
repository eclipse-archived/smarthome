/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.dto;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterBuilder;
import org.eclipse.smarthome.config.core.FilterCriteria;
import org.eclipse.smarthome.config.core.ParameterOption;

/**
 * {@link ConfigDescriptionParameterDTOMapper} maps {@link ConfigDescription}s to the data transfer object
 * {@link ConfigDescriptionDTO}.
 *
 * @author Dennis Nobel - Initial contribution
 *
 */
public class ConfigDescriptionParameterDTOMapper {

    /**
     * Maps config description parameters into DTO objects.
     *
     * @param parameters the config description parameters (not null)
     *
     * @return the parameter DTO objects (not null)
     */
    public static ConfigDescriptionParameterDTO map(ConfigDescriptionParameter configDescriptionParameter) {
        return new ConfigDescriptionParameterDTO(configDescriptionParameter.getName(),
                configDescriptionParameter.getType(), configDescriptionParameter.getMinimum(),
                configDescriptionParameter.getMaximum(), configDescriptionParameter.getStepSize(),
                configDescriptionParameter.getPattern(), configDescriptionParameter.isRequired(),
                configDescriptionParameter.isReadOnly(), configDescriptionParameter.isMultiple(),
                configDescriptionParameter.getContext(), configDescriptionParameter.getDefault(),
                configDescriptionParameter.getLabel(), configDescriptionParameter.getDescription(),
                mapOptions(configDescriptionParameter.getOptions()),
                mapFilterCriteria(configDescriptionParameter.getFilterCriteria()),
                configDescriptionParameter.getGroupName(), configDescriptionParameter.isAdvanced(),
                configDescriptionParameter.getLimitToOptions(), configDescriptionParameter.getMultipleLimit(),
                configDescriptionParameter.getUnit(), configDescriptionParameter.getUnitLabel(),
                configDescriptionParameter.isVerifyable());
    }

    public static ConfigDescriptionParameter mapDTO(ConfigDescriptionParameterDTO dto) {
        return ConfigDescriptionParameterBuilder.create(dto.name, dto.type).withAdvanced(dto.advanced)
                .withContext(dto.context).withDefault(dto.defaultValue).withDescription(dto.description)
                .withFilterCriteria(mapFilterDTO(dto.filterCriteria)).withGroupName(dto.groupName).withLabel(dto.label)
                .withLimitToOptions(dto.limitToOptions).withMaximum(dto.max).withMinimum(dto.min)
                .withMultiple(dto.multiple).withMultipleLimit(dto.multipleLimit).withOptions(mapOptionDTOs(dto.options))
                .withPattern(dto.pattern).withReadOnly(dto.readOnly).withRequired(dto.required)
                .withStepSize(dto.stepsize).withUnit(dto.unit).withUnitLabel(dto.unitLabel).withVerify(dto.verify)
                .build();
    }

    public static List<ConfigDescriptionParameter> mapDTOs(List<ConfigDescriptionParameterDTO> configDescriptions) {
        if (configDescriptions == null) {
            return null;
        }

        return configDescriptions.stream().map(dto -> mapDTO(dto)).collect(Collectors.toList());
    }

    private static List<ParameterOption> mapOptionDTOs(List<ParameterOptionDTO> options) {
        if (options == null) {
            return null;
        }
        return options.stream().map(dto -> new ParameterOption(dto.label, dto.value)).collect(Collectors.toList());
    }

    private static List<FilterCriteria> mapFilterDTO(List<FilterCriteriaDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream().map(dto -> new FilterCriteria(dto.name, dto.value)).collect(Collectors.toList());
    }

    private static List<FilterCriteriaDTO> mapFilterCriteria(List<FilterCriteria> filterCriteria) {
        if (filterCriteria == null) {
            return null;
        }
        List<FilterCriteriaDTO> result = new LinkedList<FilterCriteriaDTO>();
        for (FilterCriteria criteria : filterCriteria) {
            result.add(new FilterCriteriaDTO(criteria.getName(), criteria.getValue()));
        }
        return result;
    }

    private static List<ParameterOptionDTO> mapOptions(List<ParameterOption> options) {
        if (options == null) {
            return null;
        }
        List<ParameterOptionDTO> result = new LinkedList<ParameterOptionDTO>();
        for (ParameterOption option : options) {
            result.add(new ParameterOptionDTO(option.getValue(), option.getLabel()));
        }
        return result;
    }

}
