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

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * This is a utility class to convert between the respective object and its DTO.
 *
 * @author Markus Rathgeb - Initial contribution and API
 */
public class TriggerDTOMapper extends ModuleDTOMapper {

    public static TriggerDTO map(final Trigger trigger) {
        final TriggerDTO triggerDto = new TriggerDTO();
        fillProperties(trigger, triggerDto);
        return triggerDto;
    }

    public static Trigger mapDto(final TriggerDTO triggerDto) {
        final Trigger trigger = new Trigger(triggerDto.id, triggerDto.type,
                new Configuration(triggerDto.configuration));
        trigger.setLabel(triggerDto.label);
        trigger.setDescription(triggerDto.description);
        return trigger;
    }

    public static List<TriggerDTO> map(final Collection<Trigger> triggers) {
        if (triggers == null) {
            return null;
        }
        final List<TriggerDTO> dtos = new ArrayList<TriggerDTO>(triggers.size());
        for (final Trigger trigger : triggers) {
            dtos.add(map(trigger));
        }
        return dtos;
    }

    public static List<Trigger> mapDto(final Collection<TriggerDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        final List<Trigger> triggers = new ArrayList<Trigger>(dtos.size());
        for (final TriggerDTO dto : dtos) {
            triggers.add(mapDto(dto));
        }
        return triggers;
    }

}
