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

import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.Input;

/**
 * This is a data transfer object that is used to serialize the respective class.
 *
 * @author Markus Rathgeb - Initial contribution and API
 */
public class ConditionTypeDTO extends ModuleTypeDTO {

    public List<Input> inputs;

    public ConditionTypeDTO(final ConditionType conditionType) {
        super(conditionType);
        inputs = conditionType.getInputs();
    }

    public static List<ConditionTypeDTO> toDtoList(final Collection<ConditionType> types) {
        if (types == null) {
            return null;
        }
        final List<ConditionTypeDTO> dtos = new ArrayList<>(types.size());
        for (final ConditionType type : types) {
            dtos.add(new ConditionTypeDTO(type));
        }
        return dtos;
    }
}
