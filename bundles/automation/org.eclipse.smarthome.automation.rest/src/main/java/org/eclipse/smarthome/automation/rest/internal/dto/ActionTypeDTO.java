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

import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.Output;

/**
 * This is a data transfer object that is used to serialize the respective class.
 *
 * @author Markus Rathgeb - Initial contribution and API
 */
public class ActionTypeDTO extends ModuleTypeDTO {

    public List<Input> inputs;
    public List<Output> outputs;

    public ActionTypeDTO(final ActionType actionType) {
        super(actionType);
        inputs = actionType.getInputs();
        outputs = actionType.getOutputs();
    }

    public static List<ActionTypeDTO> toDtoList(final Collection<ActionType> types) {
        if (types == null) {
            return null;
        }
        final List<ActionTypeDTO> dtos = new ArrayList<>(types.size());
        for (final ActionType type : types) {
            dtos.add(new ActionTypeDTO(type));
        }
        return dtos;
    }

}
