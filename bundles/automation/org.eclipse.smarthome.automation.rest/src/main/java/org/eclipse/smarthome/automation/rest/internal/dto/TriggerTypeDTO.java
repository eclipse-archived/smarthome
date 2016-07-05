/**
 * Copyright (c) 2016 Markus Rathgeb
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.automation.rest.internal.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.automation.type.TriggerType;

/**
 * This is a data transfer object that is used to serialize the respective class.
 *
 * @author Markus Rathgeb - Initial contribution and API
 */
public class TriggerTypeDTO extends ModuleTypeDTO {

    public List<Output> outputs;

    public TriggerTypeDTO(final TriggerType triggerType) {
        super(triggerType);
        outputs = triggerType.getOutputs();
    }

    public static List<TriggerTypeDTO> toDtoList(final Collection<TriggerType> types) {
        if (types == null) {
            return null;
        }
        final List<TriggerTypeDTO> dtos = new ArrayList<>(types.size());
        for (final TriggerType type : types) {
            dtos.add(new TriggerTypeDTO(type));
        }
        return dtos;
    }
}
