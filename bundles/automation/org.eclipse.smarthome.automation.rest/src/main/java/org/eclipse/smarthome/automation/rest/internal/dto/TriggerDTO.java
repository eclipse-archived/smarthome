/**
 * Copyright (c) 2016 Markus Rathgeb
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.automation.rest.internal.dto;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * This is a data transfer object that is used to serialize the respective class.
 *
 * @author Markus Rathgeb - Initial contribution and API
 */
public class TriggerDTO extends ModuleDTO {

    public TriggerDTO(final Trigger trigger) {
        super(trigger);
    }

    public Trigger createTrigger() {
        final Trigger trigger = new Trigger(id, type, new Configuration(configuration));
        trigger.setLabel(label);
        trigger.setDescription(description);
        return trigger;
    }

    public static List<TriggerDTO> toDtoList(final List<Trigger> triggers) {
        if (triggers == null) {
            return null;
        }
        final List<TriggerDTO> dtos = new ArrayList<>(triggers.size());
        for (final Trigger action : triggers) {
            dtos.add(new TriggerDTO(action));
        }
        return dtos;
    }

    public static List<Trigger> fromDtoList(final List<TriggerDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        final List<Trigger> triggers = new ArrayList<>(dtos.size());
        for (final TriggerDTO dto : dtos) {
            triggers.add(dto.createTrigger());
        }
        return triggers;
    }
}
