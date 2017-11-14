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

import org.eclipse.smarthome.automation.type.CompositeTriggerType;
import org.eclipse.smarthome.automation.type.TriggerType;

/**
 * This is a utility class to convert between the respective object and its DTO.
 *
 * @author Markus Rathgeb - Initial contribution and API
 * @author Ana Dimova - extends Trigger Module type DTOs with composites
 */
public class TriggerTypeDTOMapper extends ModuleTypeDTOMapper {

    public static TriggerTypeDTO map(final TriggerType triggerType) {
        return map(triggerType, new TriggerTypeDTO());
    }

    public static CompositeTriggerTypeDTO map(final CompositeTriggerType triggerType) {
        final CompositeTriggerTypeDTO triggerTypeDto = map(triggerType, new CompositeTriggerTypeDTO());
        triggerTypeDto.children = TriggerDTOMapper.map(triggerType.getChildren());
        return triggerTypeDto;
    }

    public static List<TriggerTypeDTO> map(final Collection<TriggerType> types) {
        if (types == null) {
            return null;
        }
        final List<TriggerTypeDTO> dtos = new ArrayList<TriggerTypeDTO>(types.size());
        for (final TriggerType type : types) {
            if (type instanceof CompositeTriggerType) {
                dtos.add(map((CompositeTriggerType) type));
            } else {
                dtos.add(map(type));
            }
        }
        return dtos;
    }

    private static <T extends TriggerTypeDTO> T map(final TriggerType triggerType, final T triggerTypeDto) {
        fillProperties(triggerType, triggerTypeDto);
        triggerTypeDto.outputs = triggerType.getOutputs();
        return triggerTypeDto;
    }

}
