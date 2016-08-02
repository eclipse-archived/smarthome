/**
 * Copyright (c) 2016 Markus Rathgeb
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.automation.dto;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * This is a utility class to convert between the respective object and its DTO.
 *
 * @author Markus Rathgeb - Initial contribution and API
 */
public class ConditionDTOMapper extends ModuleDTOMapper {

    public static ConditionDTO map(final Condition condition) {
        final ConditionDTO conditionDto = new ConditionDTO();
        fillProperties(condition, conditionDto);
        conditionDto.inputs = condition.getInputs();
        return conditionDto;
    }

    public static Condition mapDto(final ConditionDTO conditionDto) {
        final Condition condition = new Condition(conditionDto.id, conditionDto.type,
                new Configuration(conditionDto.configuration), conditionDto.inputs);
        condition.setLabel(conditionDto.label);
        condition.setDescription(conditionDto.description);
        return condition;
    }

    public static List<ConditionDTO> map(final List<Condition> conditions) {
        if (conditions == null) {
            return null;
        }
        final List<ConditionDTO> dtos = new ArrayList<ConditionDTO>(conditions.size());
        for (final Condition action : conditions) {
            dtos.add(map(action));
        }
        return dtos;
    }

    public static List<Condition> mapDto(final List<ConditionDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        final List<Condition> conditions = new ArrayList<Condition>(dtos.size());
        for (final ConditionDTO dto : dtos) {
            conditions.add(mapDto(dto));
        }
        return conditions;
    }

}
