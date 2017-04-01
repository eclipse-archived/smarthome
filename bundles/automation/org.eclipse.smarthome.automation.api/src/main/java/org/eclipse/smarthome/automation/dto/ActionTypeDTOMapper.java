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

import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.CompositeActionType;

/**
 * This is a utility class to convert between the respective object and its DTO.
 *
 * @author Markus Rathgeb - Initial contribution and API
 * @author Ana Dimova - extends Action Module type DTOs with composites
 */
public class ActionTypeDTOMapper extends ModuleTypeDTOMapper {

    public static ActionTypeDTO map(final ActionType actionType) {
        return map(actionType, new ActionTypeDTO());
    }

    public static CompositeActionTypeDTO map(final CompositeActionType actionType) {
        final CompositeActionTypeDTO actionTypeDto = map(actionType, new CompositeActionTypeDTO());
        actionTypeDto.children = ActionDTOMapper.map(actionType.getChildren());
        return actionTypeDto;
    }

    public static List<ActionTypeDTO> map(final Collection<ActionType> types) {
        if (types == null) {
            return null;
        }
        final List<ActionTypeDTO> dtos = new ArrayList<ActionTypeDTO>(types.size());
        for (final ActionType type : types) {
            if (type instanceof CompositeActionType) {
                dtos.add(map((CompositeActionType) type));
            } else {
                dtos.add(map(type));
            }
        }
        return dtos;
    }

    private static <T extends ActionTypeDTO> T map(final ActionType actionType, final T actionTypeDto) {
        fillProperties(actionType, actionTypeDto);
        actionTypeDto.inputs = actionType.getInputs();
        actionTypeDto.outputs = actionType.getOutputs();
        return actionTypeDto;
    }

}
