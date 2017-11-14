/**
 * Copyright (c) 2016 Markus Rathgeb
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.automation.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.smarthome.automation.type.CompositeConditionType;
import org.eclipse.smarthome.automation.type.ConditionType;

/**
 * This is a utility class to convert between the respective object and its DTO.
 *
 * @author Markus Rathgeb - Initial contribution and API
 * @author Ana Dimova - extends Condition Module type DTOs with composites
 */
public class ConditionTypeDTOMapper extends ModuleTypeDTOMapper {

    public static ConditionTypeDTO map(final ConditionType conditionType) {
        return map(conditionType, new ConditionTypeDTO());
    }

    public static CompositeConditionTypeDTO map(final CompositeConditionType conditionType) {
        final CompositeConditionTypeDTO conditionTypeDto = map(conditionType, new CompositeConditionTypeDTO());
        conditionTypeDto.children = ConditionDTOMapper.map(conditionType.getChildren());
        return conditionTypeDto;
    }

    public static List<ConditionTypeDTO> map(final Collection<ConditionType> types) {
        if (types == null) {
            return null;
        }
        final List<ConditionTypeDTO> dtos = new ArrayList<ConditionTypeDTO>(types.size());
        for (final ConditionType type : types) {
            if (type instanceof CompositeConditionType) {
                dtos.add(map((CompositeConditionType) type));
            } else {
                dtos.add(map(type));
            }
        }
        return dtos;
    }

    private static <T extends ConditionTypeDTO> T map(final ConditionType conditionType, final T conditionTypeDto) {
        fillProperties(conditionType, conditionTypeDto);
        conditionTypeDto.inputs = conditionType.getInputs();
        return conditionTypeDto;
    }

}
